# Isolated item, crafting, persistence, block and menu regression checks

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
substitute. The fixture mod is not included in the tack artifact and does not
load RHG or establish in-game equipment compatibility.

The suite currently has 36 tests (zero failures/errors/skips in Task 7):

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
after the remaining rendering/mixin migration.

Reports: `tests/item-data/build/test-results/test/` and
`tests/item-data/build/reports/tests/test/`.
