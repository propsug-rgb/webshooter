package com.webshooter.network;

import com.webshooter.swing.SwingManager;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

/** Client -> server: the player attached a web line to this point. */
public class SwingStartPacket {
    private final double x, y, z;

    public SwingStartPacket(Vec3 anchor) {
        this.x = anchor.x;
        this.y = anchor.y;
        this.z = anchor.z;
    }

    public static void encode(SwingStartPacket msg, FriendlyByteBuf buf) {
        buf.writeDouble(msg.x);
        buf.writeDouble(msg.y);
        buf.writeDouble(msg.z);
    }

    public static SwingStartPacket decode(FriendlyByteBuf buf) {
        return new SwingStartPacket(new Vec3(buf.readDouble(), buf.readDouble(), buf.readDouble()));
    }

    public static void handle(SwingStartPacket msg, Supplier<NetworkEvent.Context> ctx) {
        NetworkEvent.Context context = ctx.get();
        context.enqueueWork(() -> {
            if (context.getSender() != null) {
                SwingManager.start(context.getSender(), new Vec3(msg.x, msg.y, msg.z));
            }
        });
        context.setPacketHandled(true);
    }
}
