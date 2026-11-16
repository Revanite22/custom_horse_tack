package com.outrightwings.truly_custom_horse_tack.client.renderer.model;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

import com.outrightwings.truly_custom_horse_tack.Main;
import com.outrightwings.truly_custom_horse_tack.client.renderer.DisplayRenderState;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/** Fixed display poses; parts are never shared with a horse or mutated after submission. */
@OnlyIn(Dist.CLIENT)
public final class DisplayTackParts {
    public record Surface(ModelPart part, Identifier texture, boolean tinted) {}
    private final Map<String, List<Surface>> parts = new HashMap<>();

    public DisplayTackParts(DisplayRenderState.Kind kind) {
        if (kind.stand) {
            ModelPart horn = DisplayTackGeometry.createHornLayer().bakeRoot().getChild("horn");
            headPose(horn);
            parts.put("horn", List.of(surface(horn, "horn", false)));
            parts.put("horn_color", List.of(surface(horn, "horn_color", true)));
            ModelPart right = DisplayTackGeometry.createRibbonRightLayer().bakeRoot().getChild("ribbon");
            ModelPart left = DisplayTackGeometry.createRibbonLeftLayer().bakeRoot().getChild("ribbon");
            for (var ribbon : List.of(right, left)) {
                headPose(ribbon);
                ribbon.getChild("flower").xRot = -ribbon.xRot;
                ribbon.getChild("longs").xRot = -ribbon.xRot;
            }
            parts.put("ribbon_ear", List.of(surface(right, "ribbon", true)));
            parts.put("ribbon_ear_left", List.of(surface(left, "ribbon", true)));
        } else {
            ModelPart neck = DisplayTackGeometry.createBellsNeckLayer().bakeRoot().getChild("bells");
            ModelPart rear = DisplayTackGeometry.createBellsRearLayer().bakeRoot().getChild("bells");
            ModelPart wings = DisplayTackGeometry.createWingsLayer().bakeRoot().getChild("wings");
            for (var part : List.of(neck, rear, wings)) part.setPos(-1, kind.wall ? 23 : 17, kind.wall ? 5 : 6);
            wings.setRotation(0, 0, 0);
            wings.getChild("left_wing").setRotation(Mth.DEG_TO_RAD * 90, Mth.DEG_TO_RAD * 332.5f, Mth.DEG_TO_RAD * 270);
            wings.getChild("right_wing").setRotation(Mth.DEG_TO_RAD * 90, Mth.DEG_TO_RAD * -332.5f, Mth.DEG_TO_RAD * -270);
            parts.put("bells_neck", List.of(surface(neck, "bell", false)));
            parts.put("bells_rear", List.of(surface(rear, "bell", false)));
            parts.put("wings", List.of(surface(wings, "wings", false)));
            parts.put("wings_dyed", List.of(surface(wings, "wings_dyed", true)));
            parts.put("wings_butterfly", List.of(surface(wings, "wings_butterfly", true), surface(wings, "wings_butterfly_overlay", false)));
        }
    }

    private static void headPose(ModelPart part) {
        part.setPos(0, 18, -5);
        part.setRotation(0.261799f, 0, 0);
    }

    private static Surface surface(ModelPart part, String texture, boolean tinted) {
        return new Surface(part, Identifier.fromNamespaceAndPath(Main.MODID, "textures/entity/horse/special_tack/" + texture + ".png"), tinted);
    }

    public List<Surface> surfaces(String pattern) { return parts.getOrDefault(pattern, List.of()); }
}
