package com.webshooter.client;

import net.minecraft.client.KeyMapping;
import org.lwjgl.glfw.GLFW;

public final class ModKeys {
    public static final String CATEGORY = "key.categories.webshooter";

    /** Hold to swing from the spot you are looking at. */
    public static final KeyMapping SWING = new KeyMapping("key.webshooter.swing", GLFW.GLFW_KEY_G, CATEGORY);
    /** Fire the selected web (combat / bomb / shock). */
    public static final KeyMapping SHOOT = new KeyMapping("key.webshooter.shoot", GLFW.GLFW_KEY_R, CATEGORY);
    /** Cycle swing -> combat -> bomb -> shock. */
    public static final KeyMapping CYCLE = new KeyMapping("key.webshooter.cycle", GLFW.GLFW_KEY_V, CATEGORY);

    private ModKeys() {}
}
