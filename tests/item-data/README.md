# Isolated crafting and persistence regression checks

Run from the tack checkout with Java 25:

```sh
bash gradlew -p tests/item-data test
```

This ModDevGradle fixture uses NeoForge's supported JUnit bootstrap. It compiles
real production item-data, recipe and display-inventory classes without the
unported GUI, rendering or other gameplay. Generic fixture items use the real
registry identities; a fixture display exercises the shared production block
entity. The fixture mod is not included in the tack artifact and does not load
RHG or establish in-game equipment compatibility.

The suite currently has 14 tests (zero failures/errors/skips in Task 5):

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

Unit tests do not run a server data-pack reload. Resource-codec checks supply
named holders for five tags verified in vanilla/NeoForge resources; they do not
prove live tag membership. Dye assembly tests use a direct red-dye ingredient
for the same reason. Explicit component bases are bound to fixture items for
registry-aware serialization. Live world breaking/placement, explosions,
automation, inventory-full interaction and multiplayer still need gameplay tests.

Reports: `tests/item-data/build/test-results/test/` and
`tests/item-data/build/reports/tests/test/`.
