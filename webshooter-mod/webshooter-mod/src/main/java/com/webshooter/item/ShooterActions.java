package com.webshooter.item;

import com.webshooter.entity.WebProjectile;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

/** Server-side actions triggered by key presses or right-click. */
public final class ShooterActions {

    public static void shoot(ServerPlayer player) {
        ItemStack stack = WebShooterItem.findShooter(player);
        if (stack.isEmpty()) {
            return;
        }
        Item item = stack.getItem();
        if (player.getCooldowns().isOnCooldown(item)) {
            return;
        }
        WebMode mode = WebShooterItem.getMode(stack);
        if (mode == WebMode.SWING) {
            mode = WebMode.COMBAT; // in swing mode the shoot key fires a plain combat web
        }
        WebProjectile web = new WebProjectile(player.level(), player, mode);
        web.shootFromRotation(player, player.getXRot(), player.getYRot(), 0.0F, 2.5F, 0.4F);
        player.level().addFreshEntity(web);
        player.level().playSound(null, player.getX(), player.getY(), player.getZ(),
                SoundEvents.SNOWBALL_THROW, SoundSource.PLAYERS, 0.6F, 1.5F);
        player.getCooldowns().addCooldown(item, mode.cooldown);
    }

    public static void cycle(ServerPlayer player) {
        ItemStack stack = WebShooterItem.findShooter(player);
        if (stack.isEmpty()) {
            return;
        }
        WebMode next = WebShooterItem.getMode(stack).next();
        WebShooterItem.setMode(stack, next);
        player.displayClientMessage(
                net.minecraft.network.chat.Component.translatable("message.webshooter.mode", next.displayName()), true);
        player.level().playSound(null, player.getX(), player.getY(), player.getZ(),
                SoundEvents.LEVER_CLICK, SoundSource.PLAYERS, 0.4F, 1.6F);
    }

    private ShooterActions() {}
}
