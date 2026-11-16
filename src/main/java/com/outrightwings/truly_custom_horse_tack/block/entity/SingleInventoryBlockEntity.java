package com.outrightwings.truly_custom_horse_tack.block.entity;

import com.outrightwings.truly_custom_horse_tack.item.tack.TackItemData;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponentGetter;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.ListTag;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.Containers;
import net.minecraft.world.WorldlyContainer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

public abstract class SingleInventoryBlockEntity extends BlockEntity implements WorldlyContainer{
    private NonNullList<ItemStack> stacks;
    private ListTag preservedItems = new ListTag();
    private CompoundTag carriedCustomData = new CompoundTag();
    public SingleInventoryBlockEntity(BlockEntityType<?> be, BlockPos pos, BlockState state) {
        super(be, pos, state);
        this.stacks = NonNullList.withSize(this.getContainerSize(), ItemStack.EMPTY);
    }

    public void drops() {
        Containers.dropItemStack(this.level, this.worldPosition.getX(),this.worldPosition.getY(),this.worldPosition.getZ(), this.stacks.get(0));
    }
    @Override
    public int[] getSlotsForFace(Direction dir) {
        return new int[1];
    }

    @Override
    public boolean canPlaceItemThroughFace(int p_19235_, ItemStack p_19236_, @Nullable Direction p_19237_) {
        return true;
    }

    @Override
    public boolean canTakeItemThroughFace(int p_19239_, ItemStack p_19240_, Direction p_19241_) {
        return true;
    }

    @Override
    public int getContainerSize() {
        return 1;
    }

    @Override
    public boolean isEmpty() {
        return this.stacks.get(0).isEmpty();
    }

    @Override
    public ItemStack getItem(int slot) {
        return stacks.get(0);
    }

    @Override
    public ItemStack removeItem(int slot, int amount) {
        ItemStack removed = ContainerHelper.removeItem(this.stacks, slot, amount);
        if (!removed.isEmpty()) setChanged();
        return removed;
    }

    @Override
    public ItemStack removeItemNoUpdate(int p_18951_) {
        return ContainerHelper.takeItem(this.stacks, p_18951_);
    }

    @Override
    public void setItem(int p_18944_, ItemStack item) {
        this.stacks.set(0,item.copy());
        setChanged();
    }

    @Override
    public boolean stillValid(Player p_18946_) {
        return false;
    }
    public boolean validItem(ItemStack item) {
        return TackItemData.isTack(item) || item.is(Items.SADDLE);
    }
    @Override
    public void clearContent() {
        this.stacks = NonNullList.withSize(this.getContainerSize(), ItemStack.EMPTY);
        setChanged();
    }

    @Override
    public void setChanged() {
        super.setChanged();
        if (this.level != null && !this.level.isClientSide()) {
            this.level.sendBlockUpdated(this.worldPosition, this.getBlockState(), this.getBlockState(), Block.UPDATE_CLIENTS);
        }
    }
    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        var loaded = DisplayInventoryData.load(input.read(DisplayInventoryData.ROOT).orElseGet(CompoundTag::new), input.lookup());
        this.stacks = NonNullList.withSize(this.getContainerSize(), ItemStack.EMPTY);
        this.stacks.set(0, loaded.stack());
        this.preservedItems = loaded.preserved();
        this.carriedCustomData = input.read("tcht_carried_custom_data", CompoundTag.CODEC).orElseGet(CompoundTag::new);
        if (this.level != null && this.level.isClientSide()) this.requestModelDataUpdate();
    }

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        DisplayInventoryData.save(output, this.stacks, preservedItems);
        if (!carriedCustomData.isEmpty()) output.store("tcht_carried_custom_data", CompoundTag.CODEC, carriedCustomData);
    }

    @Override
    protected void collectImplicitComponents(DataComponentMap.Builder components) {
        super.collectImplicitComponents(components);
        CompoundTag carried = carriedCustomData.copy();
        if (!preservedItems.isEmpty()) carried.put(DisplayInventoryData.PRESERVED_ITEMS, preservedItems.copy());
        if (!carried.isEmpty()) components.set(DataComponents.CUSTOM_DATA, CustomData.of(carried));
        // Active Items are deliberately excluded: vanilla drops Container contents.
    }

    @Override
    protected void applyImplicitComponents(DataComponentGetter components) {
        super.applyImplicitComponents(components);
        CompoundTag carried = components.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
        if (carried.get(DisplayInventoryData.PRESERVED_ITEMS) instanceof ListTag list) {
            preservedItems = list.copy();
            carried.remove(DisplayInventoryData.PRESERVED_ITEMS);
        }
        carriedCustomData = carried;
    }

    @Override
    public ClientboundBlockEntityDataPacket getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider lookup) {
        return this.saveWithoutMetadata(lookup);
    }

}
