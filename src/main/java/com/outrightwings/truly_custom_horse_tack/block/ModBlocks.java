package com.outrightwings.truly_custom_horse_tack.block;

import com.outrightwings.truly_custom_horse_tack.Main;
import com.outrightwings.truly_custom_horse_tack.block.entity.HeadStandBlockEntity;
import com.outrightwings.truly_custom_horse_tack.block.entity.HeadStandWallBlockEntity;
import com.outrightwings.truly_custom_horse_tack.block.entity.SaddleRackBlockEntity;
import com.outrightwings.truly_custom_horse_tack.block.entity.SaddleRackWallBlockEntity;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;

import java.util.Set;
import com.outrightwings.truly_custom_horse_tack.item.ModItems;
import net.minecraft.core.registries.Registries;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredHolder;

public class ModBlocks {
    public static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(Main.MODID);
    //Horse Jumps
    public static final DeferredBlock<WallBlock> JUMP_WHITE = BLOCKS.registerBlock("jump_white", properties -> new JumpBlock("simple_jumps", properties), ModBlocks::jumpProperties);
    public static final DeferredBlock<WallBlock> JUMP_ORANGE = BLOCKS.registerBlock("jump_orange", properties -> new JumpBlock("simple_jumps", properties), ModBlocks::jumpProperties);
    public static final DeferredBlock<WallBlock> JUMP_MAGENTA = BLOCKS.registerBlock("jump_magenta", properties -> new JumpBlock("simple_jumps", properties), ModBlocks::jumpProperties);
    public static final DeferredBlock<WallBlock> JUMP_LIGHT_BLUE = BLOCKS.registerBlock("jump_light_blue", properties -> new JumpBlock("simple_jumps", properties), ModBlocks::jumpProperties);
    public static final DeferredBlock<WallBlock> JUMP_YELLOW = BLOCKS.registerBlock("jump_yellow", properties -> new JumpBlock("simple_jumps", properties), ModBlocks::jumpProperties);
    public static final DeferredBlock<WallBlock> JUMP_LIME = BLOCKS.registerBlock("jump_lime", properties -> new JumpBlock("simple_jumps", properties), ModBlocks::jumpProperties);
    public static final DeferredBlock<WallBlock> JUMP_PINK = BLOCKS.registerBlock("jump_pink", properties -> new JumpBlock("simple_jumps", properties), ModBlocks::jumpProperties);
    public static final DeferredBlock<WallBlock> JUMP_GRAY = BLOCKS.registerBlock("jump_gray", properties -> new JumpBlock("simple_jumps", properties), ModBlocks::jumpProperties);
    public static final DeferredBlock<WallBlock> JUMP_LIGHT_GRAY = BLOCKS.registerBlock("jump_light_gray", properties -> new JumpBlock("simple_jumps", properties), ModBlocks::jumpProperties);
    public static final DeferredBlock<WallBlock> JUMP_CYAN = BLOCKS.registerBlock("jump_cyan", properties -> new JumpBlock("simple_jumps", properties), ModBlocks::jumpProperties);
    public static final DeferredBlock<WallBlock> JUMP_PURPLE = BLOCKS.registerBlock("jump_purple", properties -> new JumpBlock("simple_jumps", properties), ModBlocks::jumpProperties);
    public static final DeferredBlock<WallBlock> JUMP_BLUE = BLOCKS.registerBlock("jump_blue", properties -> new JumpBlock("simple_jumps", properties), ModBlocks::jumpProperties);
    public static final DeferredBlock<WallBlock> JUMP_BROWN = BLOCKS.registerBlock("jump_brown", properties -> new JumpBlock("simple_jumps", properties), ModBlocks::jumpProperties);
    public static final DeferredBlock<WallBlock> JUMP_GREEN = BLOCKS.registerBlock("jump_green", properties -> new JumpBlock("simple_jumps", properties), ModBlocks::jumpProperties);
    public static final DeferredBlock<WallBlock> JUMP_RED = BLOCKS.registerBlock("jump_red", properties -> new JumpBlock("simple_jumps", properties), ModBlocks::jumpProperties);
    public static final DeferredBlock<WallBlock> JUMP_BLACK = BLOCKS.registerBlock("jump_black", properties -> new JumpBlock("simple_jumps", properties), ModBlocks::jumpProperties);
    public static final DeferredBlock<Block> SADDLER = BLOCKS.registerBlock("saddler", SaddlerBlock::new, () -> BlockBehaviour.Properties.ofLegacyCopy(Blocks.OAK_FENCE).noOcclusion());
    public static final DeferredBlock<Block> SADDLE_RACK = BLOCKS.registerBlock("saddle_rack", SaddleRackBlock::new, () -> BlockBehaviour.Properties.ofLegacyCopy(Blocks.OAK_PLANKS).noOcclusion());
    public static final DeferredBlock<Block> HEAD_STAND = BLOCKS.registerBlock("head_stand", HeadStandBlock::new, () -> BlockBehaviour.Properties.ofLegacyCopy(Blocks.OAK_PLANKS).noOcclusion());
    public static final DeferredBlock<Block> SADDLE_RACK_WALL = BLOCKS.registerBlock("saddle_rack_wall", SaddleRackWallBlock::new, () -> BlockBehaviour.Properties.ofLegacyCopy(Blocks.OAK_PLANKS).noOcclusion());
    public static final DeferredBlock<Block> HEAD_STAND_WALL = BLOCKS.registerBlock("head_stand_wall", HeadStandWallBlock::new, () -> BlockBehaviour.Properties.ofLegacyCopy(Blocks.OAK_PLANKS).noOcclusion());
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES = DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, Main.MODID);
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<SaddleRackBlockEntity>> SADDLE_RACK_BE = BLOCK_ENTITIES.register("saddle_rack_block_entity", () -> new BlockEntityType<>(SaddleRackBlockEntity::new, SADDLE_RACK.get()));
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<HeadStandBlockEntity>> HEAD_STAND_BE = BLOCK_ENTITIES.register("head_stand_block_entity", () -> new BlockEntityType<>(HeadStandBlockEntity::new, HEAD_STAND.get()));
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<SaddleRackWallBlockEntity>> SADDLE_RACK_WALL_BE = BLOCK_ENTITIES.register("saddle_rack_wall_block_entity", () -> new BlockEntityType<>(SaddleRackWallBlockEntity::new, SADDLE_RACK_WALL.get()));
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<HeadStandWallBlockEntity>> HEAD_STAND_WALL_BE = BLOCK_ENTITIES.register("head_stand_wall_block_entity", () -> new BlockEntityType<>(HeadStandWallBlockEntity::new, HEAD_STAND_WALL.get()));


    public static void registerBlockItems() {
        // Display blocks use the two standing-and-wall items registered in ModItems.
        Set<String> displayBlocks = Set.of("saddle_rack", "head_stand", "saddle_rack_wall", "head_stand_wall");
        BLOCKS.getEntries().forEach(block -> {
            if (!displayBlocks.contains(block.getId().getPath())) {
                ModItems.ITEMS.registerSimpleBlockItem(block.getId().getPath(), block);
            }
        });
    }
    private static BlockBehaviour.Properties jumpProperties(){
        return BlockBehaviour.Properties.ofLegacyCopy(Blocks.STONE).requiresCorrectToolForDrops().strength(2.0F, 6.0F);
    }
}
