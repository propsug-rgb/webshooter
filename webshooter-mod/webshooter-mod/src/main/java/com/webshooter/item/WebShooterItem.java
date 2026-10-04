package com.webshooter.item;

import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class WebShooterItem extends Item {
    private static final String MODE_KEY = "WebMode";

    public WebShooterItem(Properties properties) {
        super(properties);
    }

    public static WebMode getMode(ItemStack stack) {
        CompoundTag tag = stack.getTag();
        return tag == null ? WebMode.SWING : WebMode.byId(tag.getInt(MODE_KEY));
    }

    public static void setMode(ItemStack stack, WebMode mode) {
        stack.getOrCreateTag().putInt(MODE_KEY, mode.ordinal());
    }

    /**
     * Finds the web shooter the player is using: main hand, then off hand,
     * then anywhere in the inventory (so the keys work with it in your hotbar or bag).
     */
    public static ItemStack findShooter(Player player) {
        ItemStack main = player.getMainHandItem();
        if (main.getItem() instanceof WebShooterItem) {
            return main;
        }
        ItemStack off = player.getOffhandItem();
        if (off.getItem() instanceof WebShooterItem) {
            return off;
        }
        for (ItemStack stack : player.getInventory().items) {
            if (stack.getItem() instanceof WebShooterItem) {
                return stack;
            }
        }
        return ItemStack.EMPTY;
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable("tooltip.webshooter.mode", getMode(stack).displayName())
                .withStyle(ChatFormatting.AQUA));
        tooltip.add(Component.translatable("tooltip.webshooter.controls1").withStyle(ChatFormatting.GRAY));
        tooltip.add(Component.translatable("tooltip.webshooter.controls2").withStyle(ChatFormatting.GRAY));
        tooltip.add(Component.translatable("tooltip.webshooter.inventory").withStyle(ChatFormatting.DARK_GRAY));
    }
}
