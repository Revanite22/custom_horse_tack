package checks;
import com.outrightwings.truly_custom_horse_tack.item.tack.TackItemData;
import net.minecraft.SharedConstants;
import net.minecraft.server.Bootstrap;
import net.minecraft.nbt.*;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.*;
import net.minecraft.world.item.component.*;

public class ItemDataTest {
    private static int checks;
    private static void check(boolean condition, String message) {
        checks++;
        if (!condition) throw new AssertionError(message);
    }
    @org.junit.jupiter.api.Test
    public void preservesItemData() {
        SharedConstants.tryDetectVersion();
        Bootstrap.bootStrap();
        // Direct holder supplies an explicit component base for this isolated data test;
        // it does not need a server data-pack reload to bind item defaults.
        ItemStack stack = new ItemStack(net.minecraft.core.Holder.direct(Items.PAPER,
                net.minecraft.core.component.DataComponentMap.EMPTY));
        CompoundTag root = new CompoundTag();
        root.putString("unrecognized", "preserve me");
        ListTag patterns = new ListTag();
        for (int i=0; i<3; i++) {
            CompoundTag layer = new CompoundTag();
            layer.putString("Pattern", i==0 ? "saddle" : i==1 ? "unknown_future_layer" : "blanket");
            layer.putInt("Color", i==0 ? 14 : i==1 ? 0x112233 : 0x000005);
            layer.putString("unknown_layer_field", "keep");
            patterns.add(layer);
        }
        root.put("Patterns", patterns);
        TackItemData.write(stack, root);
        check(TackItemData.read(stack).equals(root), "raw layer schema/order/integers and unknown data preserved");
        CompoundTag read = TackItemData.read(stack);
        read.putString("unrecognized", "changed copy");
        check(TackItemData.read(stack).getStringOr("unrecognized", "").equals("preserve me"), "read does not mutate component");
        ItemStack other = stack.copy();
        check(TackItemData.removeLastPattern(other), "washing removes one layer");
        check(TackItemData.patterns(TackItemData.read(other)).size()==2, "only final layer removed");
        check(TackItemData.patterns(TackItemData.read(stack)).size()==3, "stack copies do not alias");
        check(TackItemData.read(other).getStringOr("unrecognized", "").equals("preserve me"), "washing preserves unrelated data");
        CompoundTag persisted = (CompoundTag)CustomData.CODEC.encodeStart(NbtOps.INSTANCE, stack.get(DataComponents.CUSTOM_DATA)).getOrThrow();
        check(CustomData.CODEC.parse(NbtOps.INSTANCE, persisted).getOrThrow().copyTag().equals(root), "custom-data codec round trip");
        CompoundTag malformed = new CompoundTag();
        malformed.putString("Patterns", "unknown representation");
        TackItemData.write(other, malformed);
        check(!TackItemData.removeLastPattern(other), "unsupported schema is not erased");
        check(TackItemData.read(other).equals(malformed), "unsupported data unchanged");
        CompoundTag display = new CompoundTag();
        display.putInt("color", 0x123456);
        display.putString("unknown_display_field", "keep");
        root.put("display", display);
        TackItemData.write(stack, root);
        check(TackItemData.hasColor(stack) && TackItemData.getColor(stack)==0x123456, "legacy display.color fallback");
        check(TackItemData.read(stack).equals(root), "legacy color read does not rewrite data");
        TackItemData.setColor(stack, 0x010203);
        check(TackItemData.getColor(stack)==0x010203, "modern RGB has no added alpha bits");
        check(TackItemData.read(stack).getCompoundOrEmpty("display").getStringOr("unknown_display_field", "").equals("keep"), "setting color preserves display extras");
        TackItemData.clearColor(stack);
        check(!TackItemData.hasColor(stack), "washed color cannot resurrect from legacy fallback");
        check(TackItemData.patterns(TackItemData.read(stack)).equals(patterns), "color edits preserve every tack layer");
        check(TackItemData.getColor(stack)==0xA06540, "legacy default dye color retained");
        System.out.println("PASS: " + checks + " item-data preservation checks");
    }

    @org.junit.jupiter.api.Test
    public void legacyItemFixerPreservesUnknownModPatterns() {
        SharedConstants.tryDetectVersion();
        Bootstrap.bootStrap();
        CompoundTag original = new CompoundTag();
        ListTag layers = new ListTag();
        for (int color : new int[]{0, 15, 5, 0x112233, 0xFFFFFF}) {
            CompoundTag layer = new CompoundTag();
            layer.putString("Pattern", "unknown_pattern_" + color);
            layer.putInt("Color", color);
            layer.putString("unknown_field", "retain");
            layers.add(layer);
        }
        original.put("Patterns", layers);
        original.putString("other_mod_data", "retain");
        for (int version : new int[]{3465, 3700}) {
            CompoundTag legacy = new CompoundTag();
            legacy.putString("id", "truly_custom_horse_tack:custom_tack");
            legacy.putByte("Count", (byte)1);
            legacy.put("tag", original.copy());
            CompoundTag fixed = (CompoundTag) net.minecraft.util.datafix.DataFixers.getDataFixer().update(
                    net.minecraft.util.datafix.fixes.References.ITEM_STACK,
                    new com.mojang.serialization.Dynamic<>(NbtOps.INSTANCE, legacy), version,
                    SharedConstants.getCurrentVersion().dataVersion().version()).getValue();
            CompoundTag custom = fixed.getCompoundOrEmpty("components").getCompoundOrEmpty("minecraft:custom_data");
            org.junit.jupiter.api.Assertions.assertEquals(original, custom,
                    "Legacy fixer must retain raw unknown-mod data from " + version);
        }
    }
}
