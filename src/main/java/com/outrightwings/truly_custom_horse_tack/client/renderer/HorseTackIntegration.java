package com.outrightwings.truly_custom_horse_tack.client.renderer;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

import com.outrightwings.truly_custom_horse_tack.item.tack.TackItemData;
import net.minecraft.core.component.DataComponents;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import net.minecraft.util.context.ContextKey;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.*;
import net.minecraft.world.level.block.entity.BannerPatternLayers;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.renderstate.RegisterRenderStateModifiersEvent;
import sekelsta.horse_colors.client.renderer.*;
import sekelsta.horse_colors.entity.AbstractHorseGenetic;
import java.util.*;
import java.util.function.Function;

@OnlyIn(Dist.CLIENT)
public final class HorseTackIntegration {
    public static final ContextKey<HorseTackRenderData> DATA = new ContextKey<>(Identifier.parse("truly_custom_horse_tack:horse_tack"));
    private HorseTackIntegration() {}

    public static void registerStateModifiers(RegisterRenderStateModifiersEvent event) {
        event.registerEntityModifier(HorseGeneticRenderer.class, (AbstractHorseGenetic entity, GeneticHorseRenderState state) -> extract(entity, state, TextureCache::getTexture));
    }

    public static void registerLayers(EntityRenderersEvent.AddLayers event) {
        Set<HorseGeneticRenderer> installed = Collections.newSetFromMap(new IdentityHashMap<>());
        for (var type : event.getEntityTypes()) {
            if (event.getRenderer(type) instanceof HorseGeneticRenderer renderer && installed.add(renderer)) {
                renderer.addLayer(new HorseTackLayer(renderer,
                        new HorseGeneticModel(event.getEntityModels().bakeLayer(HorseArmorLayer.HORSE_ARMOR_LAYER)), event.getContext().getSprites()));
            }
        }
    }

    public static void extract(AbstractHorseGenetic entity, GeneticHorseRenderState state, Function<ItemStack, Identifier> textures) {
        boolean custom = TackItemData.isTack(state.bodyArmorItem);
        Identifier texture = custom ? textures.apply(state.bodyArmorItem.copy()) : null;
        var patterns = custom ? HorseTackRenderData.patterns(state.bodyArmorItem) : List.<HorseTackRenderData.Decoration>of();
        List<HorseTackRenderData.Visual> equipment = new ArrayList<>();
        HorseTackRenderData.Banner lastBanner = null;
        for (var slot : EquipmentSlot.values()) {
            // BODY replaces legacy CHEST tack. Do not alter any equipment or accessory storage.
            if (slot == EquipmentSlot.BODY || slot == EquipmentSlot.CHEST) continue;
            var stack = entity.getItemBySlot(slot);
            if (stack.getItem() instanceof BannerItem banner) {
                lastBanner = new HorseTackRenderData.Banner(banner.getColor(), stack.getOrDefault(DataComponents.BANNER_PATTERNS, BannerPatternLayers.EMPTY));
                equipment.add(lastBanner);
            } else if (stack.is(Items.END_ROD)) equipment.add(new HorseTackRenderData.Decoration("horn", -1));
            else if (stack.is(Items.ELYTRA)) equipment.add(new HorseTackRenderData.Decoration("wings", -1));
            else if (stack.getItem().builtInRegistryHolder().is(Identifier.parse("truly_custom_horse_tack:ribbon")))
                equipment.add(new HorseTackRenderData.Decoration("ribbon_ear", 0xFF000000 | (TackItemData.getColor(stack) & 0xFFFFFF)));
        }
        // Legacy FlagModel selected the last equipped banner for each banner draw.
        var selectedBanner = lastBanner;
        equipment.replaceAll(visual -> visual instanceof HorseTackRenderData.Banner ? selectedBanner : visual);
        float phase = ((float)Math.floorMod(entity.level().getGameTime(), 100L) + state.partialTick) / 100.0F;
        var motion = new HorseTackRenderData.Motion(state.legacyHeadRotation, state.legacyPitch, state.walkAnimationPos, state.walkAnimationSpeed,
                state.eatAnimation, state.standAnimation, Mth.clamp(entity.getFallFlyingTicks()-5, 0, 10)/10.0f, entity.isFallFlying(), phase);
        state.setRenderData(DATA, new HorseTackRenderData(texture, motion, patterns, equipment));
        if (custom) {
            // Suppress RHG's default BODY and saddle passes only for our custom tack.
            state.armorTexture = null;
            state.saddleTexture = null;
        }
    }
}
