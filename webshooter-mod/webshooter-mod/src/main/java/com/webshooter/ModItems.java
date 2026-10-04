package com.webshooter;

import com.webshooter.item.WebShooterItem;
import net.minecraft.world.item.Item;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public final class ModItems {
    public static final DeferredRegister<Item> ITEMS =
            DeferredRegister.create(ForgeRegistries.ITEMS, WebShooterMod.MOD_ID);

    public static final RegistryObject<Item> WEB_SHOOTER = ITEMS.register("web_shooter",
            () -> new WebShooterItem(new Item.Properties().stacksTo(1)));

    private ModItems() {}
}
