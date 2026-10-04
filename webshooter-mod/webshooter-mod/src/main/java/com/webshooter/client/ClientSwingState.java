package com.webshooter.client;

import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.Vec3;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/** Plain client-side data about who is swinging. Deliberately free of client-only imports. */
public final class ClientSwingState {
    public static final double MAX_RANGE = 55.0;
    public static final double MAX_LENGTH = 50.0;

    // the local player
    public static boolean active;
    public static Vec3 anchor = Vec3.ZERO;
    public static BlockPos anchorBlock = BlockPos.ZERO;
    public static double length;

    // other players (synced from the server)
    public static final Map<UUID, Vec3> OTHERS = new HashMap<>();

    public static void reset() {
        active = false;
        OTHERS.clear();
    }

    private ClientSwingState() {}
}
