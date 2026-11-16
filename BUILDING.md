# Minecraft 26.1.2 build baseline

Work on `port/26.1.2` in the existing checkout. Tooling is pinned to Java 25,
Gradle 9.2.1, ModDevGradle 2.0.148, and NeoForge 26.1.2.126. Use a Java 25 JDK
as `JAVA_HOME`; the wrapper verifies the distribution SHA-256. Invoke it with
`bash gradlew` on Unix if the checkout does not mark it executable.

The build baseline, core initialization/registration, items, recipes and display
inventory persistence have been migrated. Other gameplay, GUI/rendering implementations, mixin targets
and access-transformer member names still target legacy APIs. Compilation and game startup are not
expected to work yet. Required mixins
remain enabled; no gameplay feature has been disabled to make configuration pass.

## Exact RHG development dependency

Required mod: `horse_colors`, version `26.1.2-13.5-dev.2`. Reference source:
`Revanite22/realistic-horse-genetics-port`, commit
`005ad15d7082b630b78a69d0942ba8cb47538e97`. Its `MIGRATION.md` records the tested JAR:

- `realistic_horse_genetics-26.1.2-13.5-dev.2.jar` (also distributed with an RHG name).
- SHA-256: `22e6eeaea331381c4e6a28013ee938de3028b762b30e9def7ccc2174fc4701e8`.

The owner-supplied JAR has been verified against this checksum and is retained
locally under ignored `.gradle/rhg-artifacts/`. This instance's Gradle user home
`.gradle/task2-home` sets `rhgJar` to that file. Use that `GRADLE_USER_HOME` with
Java 25, or supply `-PrhgJar` explicitly in another environment. The attachment
and RHG repository are unchanged. No public release is substituted and no RHG
source build is triggered. A rebuild is not assumed to reproduce the tested bytes.

Preferred durable setup: make the existing tested JAR available at a stable,
version-specific HTTPS artifact URL (an immutable release asset or artifact
store). That publication is an owner action, not performed by this build.
Set `rhgArtifactUrl` in user-level Gradle properties or pass:

```sh
bash gradlew prepareRhgDependency -PrhgArtifactUrl=https://HOST/PATH/EXACT-JAR.jar
```

There is deliberately no guessed default URL. Do not put credentials or signed
expiring URLs in tracked files. The current downloader uses ordinary HTTPS;
private stores need the environment's supported authentication, such as a scoped
proxy binding. Do not disable TLS or checksum verification.

For an existing local copy, including one restored by an environment snapshot:

```sh
bash gradlew prepareRhgDependency '-PrhgJar=/absolute/path/RHG 26.1.2-13.5-dev.2.jar'
```

Both paths verify the pinned SHA-256 and cache the JAR under ignored
`build/dependencies/`. Compilation and development runs depend on this task;
RHG is on compile/runtime classpaths but is not bundled into tack. Configuration
checks can run without RHG. Cleaning the build directory removes this cache;
the durable URL allows reacquisition in a fresh environment without uploading.

The RHG workflow uploads CI artifacts with 30-day retention. Those archives are
not a durable dependency registry, and the inspected workflow does not publish a
Maven package or release. Private artifact API access was unavailable in this
environment, so no exact hosted artifact could be retrieved or verified.

## Configuration-stage checks

```sh
bash gradlew --version
bash gradlew help
bash gradlew tasks --all
bash gradlew processResources
bash gradlew resolveCompileDependencies
bash gradlew prepareRhgDependency
```

`resolveCompileDependencies` resolves external compile artifacts, not local RHG
or the full generated Minecraft development workspace. `prepareRhgDependency`
must fail clearly until the exact artifact is available. None of these commands
compiles the legacy Java. After Java migration, `build`, `runClient` and
`runServer` become the normal validation commands. Runs use `run/client` and
`run/server`; server startup still requires the owner's Minecraft EULA acceptance.

Task 2 validation used Temurin Java 25.0.1 and passed `help`, `tasks --all`,
`processResources` and external compile dependency resolution (80 artifacts).
The regenerated wrapper JAR matches Gradle's published 9.2.1 SHA-256. Missing RHG
and wrong-checksum rejection checks behaved as intended. The subsequently supplied
exact RHG artifact passes checksum verification and dependency preparation.
An isolated Gradle classpath check confirms RHG is on both compile and runtime
classpaths and contains all 12 checked integration classes with Java 25 bytecode
(entities, renderer/model/state, texture compositor, armor helper and color).
No Java compilation, Minecraft workspace generation or game startup was run
during Task 2.

## Task 3: initialization and registration

The entry point uses the injected NeoForge bus and mod container. Deferred
item/block factories supply registry keys; the existing 17 ordinary block items
are populated before register attachment. Configuration values and all content
IDs are preserved. Creative contents stay in the existing vanilla tabs, and RHG
handles its own creative entries. Client screen/renderer subscriptions are
client-only; their implementations are not migrated.

Banner patterns now use the target's data-pack registry: five JSON definitions
retain the original registry/texture IDs and translation keys. The banner-pattern
item uses the modern delayed pattern-set component. This does not change the
custom tack's saved Patterns list or migrate player data.

Recipe serializer registrations use the target MapCodec/StreamCodec pair. Their
factories still require the two legacy recipe implementations to adopt modern
constructors/interfaces. Task 4 replaces the custom tack item's obsolete
HorseArmorItem superclass. Legacy item tint callbacks and the
four block-entity renderer implementations remain pending rendering migration.

Validation: the Minecraft/NeoForge workspace was generated successfully. A
Java 25 `compileJava` attempt with `-Xmaxerrs 10000` reported 387 diagnostics.
No diagnostics named Main, Config, ModBlocks, ModMenus or ModBannerPatterns;
this is not a claim of runtime registration success. Other registration errors
depend on the unported item, recipe and renderer implementations. Full compiler
output is retained locally in `.gradle/task3-compile.log`; workspace generation
is recorded in `.gradle/task3-workspace.log`. No game was launched. Asset files,
tack customization utilities, mixins and RHG remained unchanged.

## Task 4: items and BODY equipment

`CustomTackItem` is now a normal Item with a delayed BODY EQUIPPABLE component,
empty armor modifiers, no damage-on-hurt, and the existing vanilla
`can_wear_horse_armor` tag (which RHG extends to all three genetic equines).
Equip-on-interact remains false so RHG controls taming, inventory and equipment
acceptance. No vanilla equipment asset is assigned: custom visual rendering is
still pending. The `custom_tack` identifier and single-item stack size remain.

`TackItemData` reads copies of vanilla CUSTOM_DATA, keeping the legacy root
Patterns list with its original Pattern strings, Color integers and order. No
new tack schema, format marker, count limit, or automatic rewrite is introduced.
Unknown fields/patterns and malformed representations are retained. Explicit
washing removes one final layer only. Existing texture helpers have only their
identifier/data access adapted; the renderer and model submission are unchanged.

Mixed dye and ribbon read modern DYED_COLOR or the legacy CUSTOM_DATA
display.color fallback. Reads never rewrite the item. Explicit dye edits/washing
remove stale legacy color only, retaining other display/custom data. RGB access
does not introduce opaque alpha bits. The original default color and dye
durability remain. Stat potion steps, limits and cumulative permanent modifiers
remain; modern modifier IDs are unique per use. Item mutations and stat messages
are server-authoritative to avoid duplicate client-side changes/messages.

Validation: full `compileJava` reports 305 errors, down from 387, with no
diagnostics in the migrated item/item-data/stat-helper classes. The isolated
fixture in `tests/item-data` tests the real production data adapter under the
supported NeoForge JUnit bootstrap; it does not load the unfinished tack mod or
RHG. See that fixture's README for commands, coverage and reports.

At the end of Task 4, GUI/recipe/mixin callers and dye recipe definitions still
needed migration; Task 5 addresses recipes and display persistence. Item
rendering and the GUI/mixins remain pending. No recipe or block gameplay was
changed during Task 4. Actual equipping,
rendering, dispenser behavior, world migration and multiplayer remain untested.
The raw legacy item-fixer tests cover the data boundary, not recovery of missing
items from RHG's archival records or migration of historical equipment slots.

## Task 5: crafting and display inventories

Both custom recipes now use CraftingInput and the target MapCodec/StreamCodec
serializer pair. Duplication consumes blank tack and retains the decorated
source; merging appends the second source's ordered layers to a copy of the
first and retains both originals. Pattern/color values and unknown source data
are not rewritten. Malformed Patterns representations are rejected as blank
ingredients rather than consumed. A marker retains custom-tack subclass
recognition without loading the item's deferred client helpers in recipes.

All 33 original recipe resource IDs are retained under the target singular
`recipe/` directory. Ingredient/result JSON uses the modern schema. Legacy
`forge:dyes` becomes the published NeoForge `c:dyes` tag. Two explicit dye recipes
replace the former implicit DyeableLeatherItem behavior. Their thin vanilla
DyeRecipe adapter includes CUSTOM_DATA display.color in mixing on a working
copy, preserving durability and unknown fields while leaving inputs unchanged.

The shared display block entity uses ValueInput/ValueOutput, registry-aware item
codecs and modern update tags. The active inventory remains `Items`, slot zero.
Legacy Count/tag records are upgraded with Minecraft's item fixer (default
source version 3465 for the 1.20.1 baseline; explicit DataVersion is honored).
Unavailable, malformed, duplicate or out-of-range records are retained under
`tcht_preserved_items`, never automatically overlaid onto current contents.
Recovery of an unavailable item remains an explicit future operation.

The four display loot tables move to `loot_table/` with the same IDs and drops.
Their copy-components function carries archived records in the display item's
CUSTOM_DATA; the shared block entity restores that data when placed in either
orientation. Other carried fields remain preserved. Usable contents are NOT
copied into the display drop: Minecraft's native Container removal hook drops
those separately. Jump loot tables and other block gameplay remain outside this
stage. Container removal honors requested amounts without truncating loaded
counts; block transfers insert one item and restore it if the player's inventory
cannot accept a removal. Synchronization notifications are server-only.

Validation: all 14 isolated regression tests pass, with zero failures/errors/
skips. All 35 recipe resources and four display loot definitions decode with
current codecs; fixture tag limitations are documented in `tests/item-data`.
Resource processing passes. Full `compileJava` reports 268 errors (down from
305), with no diagnostics in this task's recipe or persistence classes. Logs:
`.gradle/task5-tests.log` and `.gradle/task5-compile.log`. Rendering, Saddler UI,
other block implementations, accessories/flight and their legacy API callers
remain deferred. No live world or multiplayer validation is claimed.

## Legacy configuration decisions

- Remove NeoGradle's `minecraft` block, `fg.deobf`, `reobfJar`, MixinGradle and
  the old Mixin annotation processor. Modern NeoForge supplies Mixin and uses
  unobfuscated names; metadata registers the existing mixin configuration.
- Remove the generated legacy refmap reference. Keep the existing mixin list,
  `required: true` and Java 17 compatibility level: the mixin source itself has
  not changed, while the project compiler targets Java 25.
- Register the existing access-transformer file with ModDevGradle and NeoForge
  metadata. Its legacy member names are intentionally unchanged pending the
  Java/API migration; Minecraft workspace generation may reject them.
- Remove the obsolete pack-format-15 `pack.mcmeta`; rely on NeoForge's mod-pack
  metadata, matching the RHG port. All asset/data identifiers remain unchanged.
- Preserve mod ID `truly_custom_horse_tack`, mod version `1.6.0`, original authors,
  README credits and MIT license. The archive version is `26.1.2-1.6.0`.
- Do not load the legacy optional JEI, Better Third Person or Ride Along JARs
  into a 26.1.2 development run. Reassess optional integrations later.
