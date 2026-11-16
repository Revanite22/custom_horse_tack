package com.outrightwings.truly_custom_horse_tack;

import net.neoforged.neoforge.common.ModConfigSpec;
import net.neoforged.fml.event.config.ModConfigEvent;

public class Config {
    private static final ModConfigSpec.Builder BUILDER = new ModConfigSpec.Builder();
    private static final ModConfigSpec.BooleanValue CONSUME_DYE = BUILDER
            .comment("Should dye  be consumed in Saddler Table?")
            .define("consume_dye",true);
    private static final ModConfigSpec.DoubleValue MAX_JUMP = BUILDER
            .comment("Max Jump allowed by potions")
            .defineInRange("max_jump",1f,0,100);
    private static final ModConfigSpec.DoubleValue MAX_SPEED = BUILDER
            .comment("Max Speed allowed by potions")
            .defineInRange("max_speed",0.3375f,0,100);
    static final ModConfigSpec SPEC = BUILDER.build();

    public static float max_jump;
    public static float max_speed;

    public static boolean consume_dye;
    static void onLoad(final ModConfigEvent event)
    {
        if (event.getConfig().getSpec() != SPEC) return;
        max_jump = (float) (MAX_JUMP.get() * 1);
        max_speed = (float) (MAX_SPEED.get() * 1);
        consume_dye = CONSUME_DYE.get();
    }
}
