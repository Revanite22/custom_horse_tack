package com.outrightwings.truly_custom_horse_tack.screen;

import com.outrightwings.truly_custom_horse_tack.Main;
import com.outrightwings.truly_custom_horse_tack.item.ModItems;
import com.outrightwings.truly_custom_horse_tack.item.tack.TackPattern;
import com.outrightwings.truly_custom_horse_tack.item.tack.TackItemData;
import com.outrightwings.truly_custom_horse_tack.item.tack.TackTagUtility;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.animal.allay.Allay;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.*;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import sekelsta.horse_colors.entity.HorseGeneticEntity;
import sekelsta.horse_colors.entity.ModEntities;

import java.util.List;

@OnlyIn(Dist.CLIENT)
public class SaddlerBlockScreen extends AbstractContainerScreen<SaddlerBlockMenu> {
    private static final Identifier BG_LOCATION = Identifier.fromNamespaceAndPath(Main.MODID, "textures/gui/container/saddler.png");
    private static final int PATTERN_COLUMNS = 4;
    private static final int PATTERN_ROWS = 4;
    private static final int SCROLLER_WIDTH = 5;
    private static final int SCROLLER_HEIGHT = 15;
    private static final int PATTERN_IMAGE_SIZE = 14;
    private static final int SCROLLER_FULL_HEIGHT = 56;
    private static final int PATTERNS_X = 55;
    private static final int PATTERNS_Y = 13;
    private static final int SCROLL_X = 113;
    private static final int SCROLL_Y = 13;
    private static final int HORSE_X = 143;
    private static final int HORSE_Y = 65;
    private static final int PREVIEW_X = 163 ;
    private static final int PREVIEW_Y = 65;
    private static final int PREVIEW_WIDTH = 4;
    private static final int PREVIEW_HEIGHT = 4;
    private static final int PREVIEW_BUTTON = 208;
    private static final int MAX_HORSE_PREVIEWS = 2;
    private int currentPreview = 0;
    private ItemStack saddleStack = ItemStack.EMPTY;
    private ItemStack dyeStack = ItemStack.EMPTY;
    private ItemStack patternStack = ItemStack.EMPTY;
    private boolean displayPatterns;
    private float scrollOffs;
    private boolean scrolling;
    private int startRow;
    public HorseGeneticEntity horsePreview;
    private Allay previewPassenger;
    public SaddlerBlockScreen(SaddlerBlockMenu menu, Inventory inventory, Component name) {
        super(menu, inventory, name);
        menu.registerUpdateListener(this::containerChanged);
        this.titleLabelY -= 2;

    }

    protected void init() {
        super.init();
        discardPreview();
        if (Minecraft.getInstance().level != null){
            horsePreview = new HorseGeneticEntity(ModEntities.HORSE_GENETIC.get(), Minecraft.getInstance().level);
            baseHorseSetup();
            containerChanged();
        }
    }

    private int totalRowCount() {
        return Mth.positiveCeilDiv(this.menu.getSelectablePatterns().size(), PATTERN_COLUMNS);
    }
    @Override
    public void removed() {
        discardPreview();
        this.menu.registerUpdateListener(() -> {});
        super.removed();
    }

    private void discardPreview() {
        if (previewPassenger != null) {
            previewPassenger.remove(Entity.RemovalReason.DISCARDED);
            previewPassenger = null;
        }
        if (horsePreview != null) {
            horsePreview.remove(Entity.RemovalReason.DISCARDED);
            horsePreview = null;
        }
    }

    @Override
    public void extractBackground(GuiGraphicsExtractor poseStack, int mouseX, int mouseY, float tick) {
        super.extractBackground(poseStack, mouseX, mouseY, tick);
        int i = this.leftPos;
        int j = this.topPos;
        blit(poseStack, i, j, 0, 0, this.imageWidth, this.imageHeight);
        Slot slot = this.menu.getSaddleSlot();
        Slot slot1 = this.menu.getDyeSlot();
        Slot slot2 = this.menu.getPatternSlot();
        Slot slot3 = this.menu.getResultSlot();
        if (!slot.hasItem()) {
            blit(poseStack, i + slot.x, j + slot.y, this.imageWidth, 0, 16, 16);
        }

        if (!slot1.hasItem()) {
            blit(poseStack, i + slot1.x, j + slot1.y, this.imageWidth + 16, 0, 16, 16);
        }

        if (!slot2.hasItem()) {
            blit(poseStack, i + slot2.x, j + slot2.y, this.imageWidth + 32, 0, 16, 16);
        }

        int k = (int)(41.0F * this.scrollOffs);
        blit(poseStack, i + SCROLL_X, j + SCROLL_Y + k, 232 + (this.displayPatterns ? 0 : SCROLLER_WIDTH ), 0, SCROLLER_WIDTH, SCROLLER_HEIGHT);

        boolean isHoveredPrev = mouseX >= i+ PREVIEW_X && mouseY >= j+PREVIEW_Y && mouseX < i+PREVIEW_X + PREVIEW_WIDTH && mouseY < j+PREVIEW_Y + PREVIEW_HEIGHT;
        int cornerButton;
        if(isHoveredPrev){
            cornerButton = PREVIEW_BUTTON + PREVIEW_HEIGHT;
        }else{
            cornerButton = PREVIEW_BUTTON;
        }
        blit(poseStack, i + PREVIEW_X, j+ PREVIEW_Y, 0, cornerButton, PREVIEW_WIDTH, PREVIEW_HEIGHT);

        if (this.displayPatterns) {
            int startingX = i + PATTERNS_X;
            int startingY = j + PATTERNS_Y;
            List<TackPattern> list = this.menu.getSelectablePatterns();

            label63:
            for(int row = 0; row < PATTERN_ROWS; ++row) {
                for(int col = 0; col < PATTERN_COLUMNS; ++col) {
                    int currentRow = row + this.startRow;
                    int currentSlot = currentRow * PATTERN_COLUMNS + col;
                    if (currentSlot >= list.size()) {
                        break label63;
                    }

                    int cornerX = startingX + col * PATTERN_IMAGE_SIZE;
                    int cornerY = startingY + row * PATTERN_IMAGE_SIZE;
                    boolean isHovered = mouseX >= cornerX && mouseY >= cornerY && mouseX < cornerX + PATTERN_IMAGE_SIZE && mouseY < cornerY + PATTERN_IMAGE_SIZE;
                    int startingCorner;
                    if (currentSlot == this.menu.getSelectedSlotIndex()) {
                        startingCorner = this.imageHeight + PATTERN_IMAGE_SIZE;
                    } else if (isHovered) {
                        startingCorner = this.imageHeight + (PATTERN_IMAGE_SIZE*2);
                    } else {
                        startingCorner = this.imageHeight;
                    }
                    blit(poseStack, cornerX, cornerY, 0, startingCorner, PATTERN_IMAGE_SIZE, PATTERN_IMAGE_SIZE);
                    poseStack.blit(RenderPipelines.GUI_TEXTURED, list.get(currentSlot).getPatternIconLocation(), cornerX, cornerY, 0, 0, PATTERN_IMAGE_SIZE, PATTERN_IMAGE_SIZE, PATTERN_IMAGE_SIZE, PATTERN_IMAGE_SIZE);
                    if(isHovered){
                        poseStack.setTooltipForNextFrame(Component.translatable(list.get(currentSlot).getTranslationKey()), mouseX, mouseY);
                    }
                }
            }
        }
        if (horsePreview != null) {
            // Modern GUI extraction delegates to RHG's registered renderer. Custom tack
            // layers still require the later horse-rendering migration.
            InventoryScreen.extractEntityInInventoryFollowsMouse(poseStack,
                    i + HORSE_X - 23, j + HORSE_Y - 55, i + HORSE_X + 23, j + HORSE_Y + 15,
                    25, 0.0F, mouseX, mouseY, horsePreview);
        }
    }
    private static void blit(GuiGraphicsExtractor graphics, int x, int y, int u, int v, int width, int height) {
        graphics.blit(RenderPipelines.GUI_TEXTURED, BG_LOCATION, x, y, u, v, width, height, 256, 256);
    }

    private void baseHorseSetup(){
        ItemStack horsesTack = createBaseTack();
        horsePreview.setItemSlot(EquipmentSlot.SADDLE, new ItemStack(Items.SADDLE));
        horsePreview.setItemSlot(EquipmentSlot.BODY, horsesTack);
        saddleUpPartner();
    }
    private void saddleUpPartner(){
        previewPassenger = new Allay(EntityType.ALLAY, this.minecraft.level);
        previewPassenger.startRiding(horsePreview);
    }
    private ItemStack createBaseTack(){
        ItemStack horsesTack = new ItemStack(ModItems.CUSTOM_TACK_ITEM.get());

        CompoundTag armorTag = new CompoundTag();
        CompoundTag patternTag = new CompoundTag();
        patternTag.putString("Pattern","wood"+currentPreview);
        patternTag.putInt("Color",1);

        ListTag listtag = new ListTag();
        listtag.add(patternTag);
        armorTag.put("Patterns", listtag);

        TackItemData.write(horsesTack, armorTag);
        return horsesTack;
    }

    private void updateHorsePreview(ItemStack outputSlotItem){
        if (horsePreview == null) return;
        var woodToTack = TackTagUtility.mergeTack(createBaseTack(),outputSlotItem);
        horsePreview.setItemSlot(EquipmentSlot.BODY,woodToTack);
        horsePreview.setChest(TackTagUtility.has(TackItemData.read(outputSlotItem), "chest"));
    }
    private void containerChanged() {
        ItemStack saddleItem = this.menu.getSaddleSlot().getItem();
        ItemStack dyeItem = this.menu.getDyeSlot().getItem();
        ItemStack patternItem = this.menu.getPatternSlot().getItem();
        ItemStack outputItem = this.menu.getResultSlot().getItem();

        if (!ItemStack.matches(saddleItem, this.saddleStack) || !ItemStack.matches(dyeItem, this.dyeStack) || !ItemStack.matches(patternItem, this.patternStack)) {
            this.displayPatterns = !saddleItem.isEmpty() && !dyeItem.isEmpty() && !this.menu.getSelectablePatterns().isEmpty();
        }

        if (this.startRow >= this.totalRowCount()) {
            this.startRow = 0;
            this.scrollOffs = 0.0F;
        }

        if(!outputItem.isEmpty()){
            updateHorsePreview(outputItem);
        } else if(!saddleItem.isEmpty()){
            updateHorsePreview(saddleItem);
        } else{
            updateHorsePreview(createBaseTack());
        }

        this.saddleStack = saddleItem.copy();
        this.dyeStack = dyeItem.copy();
        this.patternStack = patternItem.copy();

    }
    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
        double mouseX = event.x();
        double mouseY = event.y();
        this.scrolling = false;
        if (this.displayPatterns) {
            int i = this.leftPos + PATTERNS_X;
            int j = this.topPos + PATTERNS_Y;

            for(int k = 0; k < PATTERN_ROWS; ++k) {
                for(int l = 0; l < PATTERN_COLUMNS; ++l) {
                    double d0 = mouseX - (double)(i + l * PATTERN_IMAGE_SIZE);
                    double d1 = mouseY - (double)(j + k * PATTERN_IMAGE_SIZE);
                    int i1 = k + this.startRow;
                    int j1 = i1 * PATTERN_COLUMNS + l;
                    if (d0 >= 0.0D && d1 >= 0.0D && d0 < PATTERN_IMAGE_SIZE && d1 < PATTERN_IMAGE_SIZE && this.menu.clickMenuButton(this.minecraft.player, j1)) {
                        Minecraft.getInstance().getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.UI_LOOM_SELECT_PATTERN, 1.0F));
                        this.minecraft.gameMode.handleInventoryButtonClick((this.menu).containerId, j1);
                        return true;
                    }
                }
            }

            i = this.leftPos + SCROLL_X;
            j = this.topPos + SCROLL_Y;
            if (mouseX >= (double)i && mouseX < (double)(i + SCROLLER_WIDTH) && mouseY >= (double)j && mouseY < (double)(j + SCROLLER_FULL_HEIGHT)) {
                this.scrolling = true;
            }
        }
        if(mouseX >= this.leftPos + PREVIEW_X && mouseY >= this.topPos + PREVIEW_Y && mouseX < this.leftPos + PREVIEW_X + PREVIEW_WIDTH && mouseY < this.topPos + PREVIEW_Y + PREVIEW_HEIGHT){
            currentPreview = currentPreview < MAX_HORSE_PREVIEWS-1 ? ++currentPreview : 0;
            containerChanged();
            return true;
        }
        return super.mouseClicked(event, doubleClick);
    }

    @Override
    public boolean mouseDragged(MouseButtonEvent event, double dx, double dy) {
        double mouseY = event.y();
        int i = this.totalRowCount() - 4;
        if (this.scrolling && this.displayPatterns && i > 0) {
            int j = this.topPos + SCROLL_Y;
            int k = j + SCROLLER_FULL_HEIGHT;
            this.scrollOffs = ((float)mouseY - (float)j - ((float)SCROLLER_HEIGHT)/2) / ((float)(k - j) - ((float)SCROLLER_HEIGHT));
            this.scrollOffs = Mth.clamp(this.scrollOffs, 0.0F, 1.0F);
            this.startRow = Math.max((int)((double)(this.scrollOffs * (float)i) + 0.5D), 0);
            return true;
        } else {
            return super.mouseDragged(event, dx, dy);
        }
    }

    @Override
    public boolean mouseReleased(MouseButtonEvent event) {
        this.scrolling = false;
        return super.mouseReleased(event);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        int i = this.totalRowCount() - 4;
        if (this.displayPatterns && i > 0) {
            float f = (float)scrollY / (float)i;
            this.scrollOffs = Mth.clamp(this.scrollOffs - f, 0.0F, 1.0F);
            this.startRow = Math.max((int)(this.scrollOffs * (float)i + 0.5F), 0);
        }

        return true;
    }

}
