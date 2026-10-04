package com.webshooter.network;

import com.webshooter.swing.SwingManager;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

/** Client -> server: the player let go of the web line. */
public class SwingStopPacket {
    public SwingStopPacket() {}

    public static void encode(SwingStopPacket msg, FriendlyByteBuf buf) {}

    public static SwingStopPacket decode(FriendlyByteBuf buf) {
        return new SwingStopPacket();
    }

    public static void handle(SwingStopPacket msg, Supplier<NetworkEvent.Context> ctx) {
        NetworkEvent.Context context = ctx.get();
        context.enqueueWork(() -> {
            if (context.getSender() != null) {
                SwingManager.stop(context.getSender());
            }
        });
        context.setPacketHandled(true);
    }
}
