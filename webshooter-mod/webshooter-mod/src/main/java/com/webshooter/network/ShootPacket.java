package com.webshooter.network;

import com.webshooter.item.ShooterActions;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

/** Client -> server: fire the currently selected web. */
public class ShootPacket {
    public ShootPacket() {}

    public static void encode(ShootPacket msg, FriendlyByteBuf buf) {}

    public static ShootPacket decode(FriendlyByteBuf buf) {
        return new ShootPacket();
    }

    public static void handle(ShootPacket msg, Supplier<NetworkEvent.Context> ctx) {
        NetworkEvent.Context context = ctx.get();
        context.enqueueWork(() -> {
            if (context.getSender() != null) {
                ShooterActions.shoot(context.getSender());
            }
        });
        context.setPacketHandled(true);
    }
}
