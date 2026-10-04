package com.webshooter;

import com.webshooter.entity.WebProjectile;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public final class ModEntities {
    public static final DeferredRegister<EntityType<?>> ENTITIES =
            DeferredRegister.create(ForgeRegistries.ENTITY_TYPES, WebShooterMod.MOD_ID);

    public static final RegistryObject<EntityType<WebProjectile>> WEB_PROJECTILE = ENTITIES.register("web_projectile",
            () -> EntityType.Builder.<WebProjectile>of(WebProjectile::new, MobCategory.MISC)
                    .sized(0.25F, 0.25F)
                    .clientTrackingRange(4)
                    .updateInterval(10)
                    .build("web_projectile"));

    private ModEntities() {}
}
