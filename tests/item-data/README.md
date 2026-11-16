# Isolated migration regression checks

Run from the tack checkout with Java 25:

```sh
bash gradlew -p tests/item-data test
```

This ModDevGradle fixture uses NeoForge's supported JUnit bootstrap. It compiles
real production item-data, recipe, block-registration, block and display-entity
classes, the Saddler menu/registration, pattern catalog/items, mixed dye and
configuration without client GUI rendering or other unported gameplay. Generic
fixture items use the real registry identities; pattern items and mixed dye use
their actual production implementations. A fixture display exercises the shared
production block entity. Two clearly marked fixture substitutes provide the main
mod ID and item register. The production Saddler menu has replaced Task 6's menu
substitute. The fixture mod is not included in the tack artifact or a substitute
for in-game equipment testing. Task 8 also compiles the
actual standalone display renderers, registration helper, baked models and tint
source. Display texture resolution is injected, so tests need no RHG texture
class or GPU upload for the standalone display checks.

Task 9 adds checks against the exact RHG Build #2 JAR. The fixture reuses the root
`gradle/rhg-dependency.gradle` mechanism and reads its version/hash pins from the
root properties. Supply `-PrhgJar=...` or the stable HTTPS artifact URL described
in BUILDING.md; substitute releases are rejected. Artifact copies stay in the
ignored fixture build directory. Earlier checks retain their fixture registries.

Task 10 also compiles and loads the production mixins using the fixture metadata.
Mixin/helper bytecode is verified against the main build. The fixture elytra
prototype supplies vanilla durability/GLIDER components so legacy damage tests
exercise the same item semantics as a game with its component resources loaded.

The suite currently has 71 tests (zero failures/errors/skips in Task 10):

- Original item-data checks: pattern order, dye IDs/RGB, unknown fields and
  patterns, copies, washing, codec round trips and vanilla item conversion from
  data versions 3465 and 3700.
- Duplication/merging: ingredient rejection, unchanged inputs, ordered output
  layers, retained originals/remainders and protection of malformed data.
- Custom recipe JSON/network serializer round trips; target-codec decoding of
  all 35 recipe resources (33 originals plus two explicit dye operations).
- Mixed-dye/ribbon crafting includes legacy color, retains durability and
  unknown fields, and does not mutate source stacks.
- Display save/reload and update-tag handling, empty-slot persistence, stack
  counts/removal, legacy inventory conversion and missing/malformed archives.
- Archive component handoff through display drops and placement, excluding
  active contents to avoid duplication; all four display loot-table codecs.
- Production registration of all 21 blocks and four display-entity types;
  saved-state codec round trips for every state, including all 324 states of
  each jump. Original state names and allowed values remain intact.
- Sixteen head-stand yaw positions, floor/wall placement, vertical-face fallback,
  rotations, display angles, display shapes and actual entity factories.
- Jump rail width, outline/collision heights, inherited waterlogging and fluid
  scheduling. A vanilla wall neighbor update verifies virtual dispatch through
  the modern access-transformer rule and rejects ordinary solid blocks/walls.
- Customized display insertion/retrieval, full-inventory recovery and invalid
  item rejection; server-only Saddler provider opening, without creating a menu.
- All 21 block loot-table codecs and modern jump/mining tag resource paths.
- Actual Saddler registration/provider, 40-slot layout, input restrictions,
  pattern groups/order/selection, eight existing layers plus a new layer, dye
  IDs and both legacy/component RGB, and preservation of unknown customization.
- Selection without consumption, server-only output consumption, dye count,
  mixed-dye durability/final use and disabled-consumption configuration.
- Invalid buttons/inputs/malformed data, all input/output shift-click routes,
  full-inventory rejection, workstation-distance validation and returning only
  the inputs on close, with no crafted output duplication.
- All four renderer registrations and real provider construction; extraction
  for every display orientation, immutable ordered color/pattern snapshots,
  malformed/empty/saddle state handling and reuse without stale rendering data.
- Original ground/center transform across sixteen headings, decorative display
  restrictions and poses, folded wings, ribbon counter-rotation and untinted
  overlays. Draw submissions preserve material order, light, breaking overlay,
  packed colors and the caller's pose stack.
- Legacy and modern item RGB tinting, actual tint-source registration, and full
  target-codec decoding of both new item definitions with original model IDs
  and the untinted mixed-dye tin layer.
- Exact texture catalog/RHG layer-group order, base/overlay colors, dye IDs and
  RGB channels, immutable snapshots and unknown customization retention.
- RHG state-modifier registration and BODY snapshots for actual horse/donkey/mule
  classes (mock entities), ordinary-equipment fallback and pooled-state cleanup.
- Head/ribbon/bell/wing poses and reset behavior, decorative textures and colors,
  banner mask order, and equipment/body/pattern queue ordering. The actual RHG
  armor model accepts its GeneticHorseRenderState.
- Accessory slot order across all three genetic equines, duplicate/full-slot and
  client-side no-mutation handling, leg elytra and axe removal without removing
  BODY/SADDLE or losing colors/counts/durability.
- Native EntityEquipment and equipment-packet round trips, and RHG's actual
  HandItems/ArmorItems migration from data version 3465 with accessory data.
- Rider/ground/passenger/levitation/durability flight restrictions, wing fallback,
  original fall immunity, ten-tick server glide events and no flight wear.
- Actual transformed target classes: the auto-start injection, canGlide,
  updateFallFlying/fall immunity overrides and private vanilla travelRidden with
  derived input, native ridden speed and the 1.02 authority-side boost.

Unit tests do not run a server data-pack reload. Resource-codec checks supply
named holders for five tags verified in vanilla/NeoForge resources; they do not
prove live tag membership. Dye assembly tests use a direct red-dye ingredient
for the same reason. Explicit component bases are bound to fixture items for
registry-aware serialization. The connection test temporarily binds the jump
tag to one holder and restores it afterward; it does not run a datapack reload.
Block interaction tests use Mockito's subclass mock maker (no attached Java
agent) for levels, players and placement contexts. These checks exercise the
production block methods, but live world breaking/placement, explosions,
automation and multiplayer still need gameplay tests. In particular, unit tests
do not prove world removal/drop execution, actual client GUI extraction, RHG
preview visuals or network packet transport. Menu tests use real inventories
and the production block's provider; only inventory-return packet sending is
disabled in their fixture inventory. Server levels/players are mocks. Live GUI,
preview sizing and multiplayer synchronization require a working client/server
after the remaining rendering/mixin migration. Rendering tests use a mock draw
collector and a probe registration event. They compile and exercise the real
render paths without issuing GPU commands. They do not test texture composition
upload/reload, client event-bus discovery, actual resource-pack baking or visual
appearance. The RHG-backed TextureCache is checked by full-source compilation
and direct inspection of the pinned RHG JAR API. Texture composition descriptions
are also tested against actual RHG TextureLayerGroup/Color classes. Integration
tests use real RHG class definitions and production tack code, mock entities,
a fake texture identifier and mock collectors. They do not start a complete RHG
world/client or upload textures. The two current entity mixins are actually
applied by the fixture; retired rendering mixins are absent. No RHG model/state
copy or simplified substitute is used. Direct calls through transformed methods
still use mock entity state; they do not prove real flight physics, player
movement synchronization or full client/dedicated-server startup.

Reports: `tests/item-data/build/test-results/test/` and
`tests/item-data/build/reports/tests/test/`.
