package com.webshooter.item;

import net.minecraft.network.chat.Component;

public enum WebMode {
    SWING("swing", 0),
    COMBAT("combat", 10),
    BOMB("bomb", 40),
    SHOCK("shock", 30);

    public final String id;
    public final int cooldown;

    WebMode(String id, int cooldown) {
        this.id = id;
        this.cooldown = cooldown;
    }

    public WebMode next() {
        WebMode[] all = values();
        return all[(ordinal() + 1) % all.length];
    }

    public Component displayName() {
        return Component.translatable("mode.webshooter." + id);
    }

    public static WebMode byId(int id) {
        WebMode[] all = values();
        return id >= 0 && id < all.length ? all[id] : SWING;
    }
}
