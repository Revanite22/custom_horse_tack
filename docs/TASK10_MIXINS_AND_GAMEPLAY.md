# Task 10: mixins, equipment and flight

Baseline: `9641860681f9a4a47236e2a8514437239a82faed`, branch `port/26.1.2`.
Target: Minecraft 26.1.2, NeoForge 26.1.2.126, Java 25 and the checksum-pinned
RHG 26.1.2-13.5-dev.2 artifact. RHG and its source repository were not modified.

## Original mixins and their replacements

| Original mixin | Original purpose | Task 10 result |
| --- | --- | --- |
| AbstractHorseGeneticMixin | Equip/remove banners, rods, ribbons and leg elytra; automatic ridden gliding, glide events and fall protection | Migrated. Keeps the actual RHG private itemInteract injection, adds an aiStep hook to restore automatic takeoff, and provides modern canGlide/updateFallFlying/causeFallDamage overrides. Policies live in HorseAccessories/HorseFlight. |
| LivingEntityMixin | During authoritative ridden flight, set ridden speed, multiply velocity by 1.02, travel with derived rider input and check blocks | Migrated. Vanilla now supplies the speed, derived input, travel and block-effects work. Inject only the original velocity boost immediately before the existing travel invocation, avoiding local-variable capture. |
| HorseArmorRendererMixin | Custom tack textures, decorations and equipped decorative visuals; otherwise draw RHG armor textures | Removed. Task 9's public render-state modifier and HorseTackLayer provide custom rendering, while ordinary equipment remains on RHG's existing layers. |
| SaddleMixin | Hide the separate saddle texture while custom tack supplies its own saddle visuals | Removed. Task 9 already clears saddleTexture only for custom BODY tack. |

The old rendering mixins are removed from both source and configuration. Existing
regression checks cover texture substitution, ordinary-equipment preservation,
all three genetic equine classes, model/texture/color order and queued drawing.
See TASK10_RENDERING_HOOKS.md for their verified obsolete descriptors.

## Verified injection/override targets

RHG bytecode inspected from the exact Build #2 JAR:

- `AbstractHorseGenetic.itemInteract(Player, ItemStack, InteractionHand): boolean`
  is **private and declared by RHG**, not inherited. Inject at HEAD, cancellable,
  remap=false. Its enclosing mobInteract retains RHG's feeding, riding, secondary
  use and equipment interaction flow. Unhandled interactions continue through RHG.
- `AbstractHorseGenetic.aiStep(): void` is declared by RHG. Inject at HEAD,
  remap=false, to update the flight flag on the server before vanilla tests it.
- AbstractHorseGenetic inherits modern AbstractChestedHorse/AbstractHorse. Its
  `canGlide(): boolean` and `updateFallFlying(): void` overrides are protected;
  `causeFallDamage(double, float, DamageSource): boolean` is public. The former
  float fall-distance signature is obsolete.

Cached target Minecraft sources were also inspected:

- `LivingEntity.travelRidden(Player, Vec3): void` is private. Its call to
  `LivingEntity.travel(Vec3): void` occurs after tickRidden and setSpeed, and only
  on the native movement authority. The injection runs just before that call.
- `LivingEntity.aiStep()` now calls updateFallFlying only when already flying;
  a pure legacy override would therefore never start auto-gliding. The RHG
  aiStep injection restores the original automatic eligibility behavior.
- Modern travelFallFlying also checks canGlide. Vanilla requires GLIDER plus an
  Equippable slot match, which rejects ordinary CHEST elytra in the mod's LEGS
  slot and cannot recognize wing patterns. The equine override uses the original
  restrictions and accepts the modern GLIDER capability without the slot match.
- isLocalInstanceAuthoritative is the current native ownership check; vanilla
  canSimulateMovement uses it too. Block effects are applied by the modern travel
  caller, so the removed tryCheckInsideBlocks call is not replaced with a second
  movement/block-effects pass.

Mixin configuration stays required with defaultRequire=1. It uses JAVA_25,
verified as supported by the bundled sponge-mixin 0.17.4+mixin.0.8.7. Only the
current WallBlock connection access transformer remains. Obsolete inventory,
setFlag and updateFallFlying transformer names are no longer needed.

## Equipment behavior and persistence

The original decorative order remains MAINHAND, OFFHAND, FEET, HEAD. LEGS stays
reserved for elytra. BODY and SADDLE are protected for modern RHG equipment;
legacy CHEST stays excluded. Duplicate checks stop at the same first matching
item/banner or first empty slot as before. Recognized equipping attempts consume
the action even when full; no existing item is replaced. Splitting one held item
also preserves the original creative-mode consumption behavior.

Axe removal drops one item from each removable accessory slot, including LEGS,
while retaining full customization and any remaining count. BODY/SADDLE are not
removed. If no accessories remain, the interaction falls through so RHG can
perform its existing chest removal. Only the server mutates equipment/stacks or
spawns drops; clients acknowledge the action and receive native tracking updates.

No tack equipment storage or serialization format was introduced. Native
EntityEquipment persists slots and components; native equipment-change tracking
sends ClientboundSetEquipmentPacket updates. Tests verify both codec and packet
round trips, including empty removals. RHG HorseSaveData.modernHorse already
converts legacy HandItems/ArmorItems through the vanilla donkey schema. Tests
exercise its actual adapter with 1.20.1 data version 3465, leg elytra damage and
ribbon RGB/unknown custom data. No RHG changes or additional save fields are needed.

## Flight behavior retained

- Airborne, not itself a passenger, no levitation, and a controlling passenger.
- Usable leg glider (GLIDER, not about to break) OR a BODY pattern containing
  `wings`, including the original substring matching for unknown future patterns.
- Server-authoritative native shared flag 7, ten-tick ELYTRA_GLIDE game events,
  and no elytra durability consumption from flight itself, matching the original.
- Native gliding physics with the original 1.02 velocity multiplier during
  authoritative ridden travel. No new keys, accelerations or flight inventory.
- Fall protection still applies with leg ELYTRA or wing tack, even when unridden
  or when the elytra is broken. This is the original rule, not a new restriction.

## Validation and remaining limits

All 71 focused regression tests pass: 55 existing and 16 new equipment/flight
checks. The fixture loads the **production mixin configuration**. Reflection
confirms both target classes are transformed; tests call the injected automatic
start handler, transformed canGlide/fall-damage/update overrides, and the actual
private travelRidden method. Production mixin/helper class bytes match those used
by the fixture exactly. This validates application/behavior on mock entities;
it is not a full client/server gameplay or physics test.

Full Java compilation has zero errors. Resource processing and assemble succeed.
The development JAR is `build/libs/truly_custom_horse_tack-26.1.2-1.6.0.jar`.
Package inspection verifies expanded mod metadata and required RHG dev.2 identity,
JAVA_25 mixin JSON/classes, current access transformer, license, all 440 asset
files and all datapack JSON. Retired mixins, test classes and RHG/dependency JARs
are absent. Generated outputs remain ignored and uncommitted.

Client/server startup, actual GUI/GPU output, resource reload, multiplayer
movement authority and real world save/reload remain to be tested. Modern item
asset definitions outside the two tinted items were explicitly outside Task 8;
check inventory icons/resource diagnostics before assuming all legacy model
assets work in a live client. No rendering rewrite was attempted in Task 10.

Recommended first in-game checks, using disposable copies of worlds:

1. Start a client and dedicated server with only this tack JAR and the exact RHG
   Build #2 JAR. Inspect logs, registries, item icons and original GUI/display art.
2. On each genetic horse/donkey/mule, equip custom BODY tack and a saddle. Equip
   banners, rod, RGB ribbon and leg elytra; try duplicates and occupied slots.
3. Remove accessories with an axe: verify exact drops/counts and unchanged BODY
   tack/saddle; then exercise RHG chest removal, save/relog and chunk unloading.
4. Ride off a ledge with wing tack and then with elytra. Verify automatic gliding,
   motion/animations, no flight durability wear, landing and fall protection.
   Check dismounting, being a passenger, levitation and broken elytra restrictions.
5. Repeat with a second player observing and reconnecting, and migrate a backed-up
   1.20.1 test world containing customized equipment/accessories. Verify slot,
   color, count, durability, selection and synchronization preservation.
