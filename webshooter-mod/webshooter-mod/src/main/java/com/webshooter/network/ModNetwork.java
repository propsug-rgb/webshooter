package com.webshooter.network;

import com.webshooter.WebShooterMod;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.simple.SimpleChannel;

public final class ModNetwork {
    private static final String PROTOCOL = "1";

    public static final SimpleChannel CHANNEL = NetworkRegistry.newSimpleChannel(
            new ResourceLocation(WebShooterMod.MOD_ID, "main"),
            () -> PROTOCOL, PROTOCOL::equals, PROTOCOL::equals);

    public static void register() {
        int id = 0;
        CHANNEL.registerMessage(id++, SwingStartPacket.class, SwingStartPacket::encode, SwingStartPacket::decode, SwingStartPacket::handle);
        CHANNEL.registerMessage(id++, SwingStopPacket.class, SwingStopPacket::encode, SwingStopPacket::decode, SwingStopPacket::handle);
        CHANNEL.registerMessage(id++, ShootPacket.class, ShootPacket::encode, ShootPacket::decode, ShootPacket::handle);
        CHANNEL.registerMessage(id++, CyclePacket.class, CyclePacket::encode, CyclePacket::decode, CyclePacket::handle);
        CHANNEL.registerMessage(id++, SwingSyncPacket.class, SwingSyncPacket::encode, SwingSyncPacket::decode, SwingSyncPacket::handle);
    }

    private ModNetwork() {}
}
