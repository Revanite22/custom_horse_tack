package com.outrightwings.truly_custom_horse_tack.client.renderer.model;

import net.minecraft.client.model.Model;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.resources.Identifier;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

import java.util.function.Function;
@OnlyIn(Dist.CLIENT)
public abstract class DisplayModel extends Model<net.minecraft.util.Unit> {
    public final ModelPart stand;
    public final ModelPart body;

    public DisplayModel(ModelPart root, Function<Identifier, RenderType> prop) {
        super(root, prop);
        this.stand = getStand(root);
        this.body = getBody(root);
    }

    protected ModelPart getStand(ModelPart root) {
        return root.getChild("stand");
    }
    protected ModelPart getBody(ModelPart root) {
        return root.getChild("body");
    }
}
