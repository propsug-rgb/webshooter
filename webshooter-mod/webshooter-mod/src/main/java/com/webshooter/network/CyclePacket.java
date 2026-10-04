package com.webshooter.network;

import com.webshooter.item.ShooterActions;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

/** Client -> server: switch to the next web mode. */
public class CyclePacket {
    public CyclePacket() {}

    public static void encode(CyclePacket msg, FriendlyByteBuf buf) {}

    public static CyclePacket decode(FriendlyByteBuf buf) {
        return new CyclePacket();
    }

    public static void handle(CyclePacket msg, Supplier<NetworkEvent.Context> ctx) {
        NetworkEvent.Context context = ctx.get();
        context.enqueueWork(() -> {
            if (context.getSender() != null) {
                ShooterActions.cycle(context.getSender());
            }
        });
        context.setPacketHandled(true);
    }
}
