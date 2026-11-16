# Rendering hooks verified against RHG Build #2

Reference artifact: `realistic_horse_genetics-26.1.2-13.5-dev.2.jar`, SHA-256
`22e6eeaea331381c4e6a28013ee938de3028b762b30e9def7ccc2174fc4701e8`.
All signatures and behavior below were inspected using `javap -p -c -s` on
that exact JAR and the cached Minecraft/NeoForge 26.1.2.126 source artifacts.
No RHG file was modified.

## Task 9 uses public hooks, not new mixins

`ClientEvents.registerHorseRenderState` handles
`net.neoforged.neoforge.client.renderstate.RegisterRenderStateModifiersEvent`.
`HorseTackIntegration.registerStateModifiers` registers a modifier for the
concrete `sekelsta.horse_colors.client.renderer.HorseGeneticRenderer` class.
NeoForge runs it after renderer extraction and resets render-data attachments
before the next extraction. It attaches an immutable HorseTackRenderData through
a ContextKey; this is transient rendering data, not entity serialization.

RHG's exact typed extractor is:

```text
HorseGeneticRenderer.extractRenderState(
  AbstractHorseGenetic, GeneticHorseRenderState, float): void
(Lsekelsta/horse_colors/entity/AbstractHorseGenetic;
 Lsekelsta/horse_colors/client/renderer/GeneticHorseRenderState;F)V
```

It copies `getArmor()` into `bodyArmorItem`, copies SADDLE equipment, and fills
armorTexture/saddleTexture, colors, animation and genetic model fields.
`AbstractHorseGenetic.getArmor()` reads `EquipmentSlot.BODY`. The tack modifier
uses that snapshot and suppresses armorTexture/saddleTexture only for custom
tack. RHG's normal extraction restores those fields on every subsequent frame.
Ordinary equipment keeps RHG's texture and tint values.

`ClientEvents.registerHorseLayers` handles EntityRenderersEvent.AddLayers.
It adds HorseTackLayer to each HorseGeneticRenderer instance, without replacing
or subclassing the registered renderer. RHG ClientRegistration.registerRenderers
binds all three genetic entity types to this exact renderer class. Identity
checking prevents duplicate layer attachment for aliased renderer instances.
The tack layer uses an RHG HorseGeneticModel baked from
HorseArmorLayer.HORSE_ARMOR_LAYER. RHG's model setup, genetic features and parent
renderer scale continue to apply.

## Obsolete rendering mixins: leave implementation to Task 10

These files and the mixin JSON have NOT been edited in Task 9. Their obsolete
source calls still prevent full compilation and their required injections would
prevent a client from loading even after Java errors were worked around.

1. `mixin/HorseArmorRendererMixin.java` targets HorseArmorLayer's legacy `render`
   method and a renderToBuffer invocation with float RGBA/local captures.
   That render method no longer exists. Its shadow also assumes the old generic
   HorseGeneticModel; the current RHG model is not generic.

   The actual modern layer entry point is:

   ```text
   HorseArmorLayer.submit(PoseStack, SubmitNodeCollector, int,
                         GeneticHorseRenderState, float, float): void
   (Lcom/mojang/blaze3d/vertex/PoseStack;
    Lnet/minecraft/client/renderer/SubmitNodeCollector;I
    Lsekelsta/horse_colors/client/renderer/GeneticHorseRenderState;FF)V
   ```

   It returns immediately when armorTexture is null, then queues its model at
   order 1 and foil at order 2. HorseSaddleLayer has the same typed submit
   descriptor and uses saddleTexture, orders 3/4. Neither exposes the legacy
   renderToBuffer injection/local-capture point.

   **Recommended Task 10 action:** retire this mixin and its client JSON entry,
   because state modifiers and HorseTackLayer replace its rendering duties.
   Do not add a new submit injection alongside the new layer: that would draw
   equipment/decorations twice. If an injection is later required, the exact
   typed submit descriptor above is the verified target; no such injection is
   necessary or implemented by Task 9.

2. `mixin/SaddleMixin.java` injects into HorseArmorer.getSaddleTexture using a
   ResourceLocation return descriptor. The actual method is:

   ```text
   HorseArmorer.getSaddleTexture(AbstractHorseGenetic): Identifier
   (Lsekelsta/horse_colors/entity/AbstractHorseGenetic;)
     Lnet/minecraft/resources/Identifier;
   ```

   **Recommended Task 10 action:** retire this mixin and its client JSON entry.
   For custom BODY tack, the post-extraction modifier already sets saddleTexture
   to null. No saddle-helper injection is necessary.

Other entity/accessory/flight mixins are outside this rendering stage. Task 9
only reads existing equipment/animation values to preserve visual behavior;
it does not provide or change accessory storage, movement or flight mechanics.

## Validation boundary

The focused fixture compiles the production integration, models, catalog and
texture-building method against the pinned RHG artifact. It tests state modifier
registration, all three genetic entity classes (mock entities), RHG armor-model
setup/submission, colors, poses, banners and queue order. It does not start a full
RHG client, validate GPU output, load the old mixins, or test actual client event
bus startup. Live horse/donkey/mule and Saddler preview visuals still require
client testing after Task 10 and the remaining compilation blockers are handled.
