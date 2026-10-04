package com.webshooter;

import com.webshooter.block.TempWebBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.material.PushReaction;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public final class ModBlocks {
    public static final DeferredRegister<Block> BLOCKS =
            DeferredRegister.create(ForgeRegistries.BLOCKS, WebShooterMod.MOD_ID);

    /** Temporary sticky web that disappears by itself after a few seconds. */
    public static final RegistryObject<Block> WEB_TRAP = BLOCKS.register("web_trap",
            () -> new TempWebBlock(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.WOOL)
                    .noCollission()
                    .noOcclusion()
                    .sound(SoundType.COBWEB)
                    .strength(0.2F)
                    .pushReaction(PushReaction.DESTROY)));

    private ModBlocks() {}
}
