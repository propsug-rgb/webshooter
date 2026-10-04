package com.webshooter.client;

import net.minecraft.client.Minecraft;
import net.minecraft.world.phys.Vec3;

import java.util.UUID;

/** Entry points called from network packets on the client. */
public final class ClientHooks {

    public static void handleSwingSync(UUID id, boolean active, Vec3 anchor) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player != null && mc.player.getUUID().equals(id)) {
            return; // the local player drives its own swing state
        }
        if (active) {
            ClientSwingState.OTHERS.put(id, anchor);
        } else {
            ClientSwingState.OTHERS.remove(id);
        }
    }

    private ClientHooks() {}
}
