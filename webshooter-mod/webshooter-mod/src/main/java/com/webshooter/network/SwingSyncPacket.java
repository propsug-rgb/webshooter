package com.webshooter.network;

import com.webshooter.client.ClientHooks;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

import java.util.UUID;
import java.util.function.Supplier;

/** Server -> clients: a player started or stopped swinging (so everyone can draw the line). */
public class SwingSyncPacket {
    private final UUID player;
    private final boolean active;
    private final double x, y, z;

    public SwingSyncPacket(UUID player, boolean active, Vec3 anchor) {
        this.player = player;
        this.active = active;
        this.x = anchor.x;
        this.y = anchor.y;
        this.z = anchor.z;
    }

    public static void encode(SwingSyncPacket msg, FriendlyByteBuf buf) {
        buf.writeUUID(msg.player);
        buf.writeBoolean(msg.active);
        buf.writeDouble(msg.x);
        buf.writeDouble(msg.y);
        buf.writeDouble(msg.z);
    }

    public static SwingSyncPacket decode(FriendlyByteBuf buf) {
        UUID id = buf.readUUID();
        boolean active = buf.readBoolean();
        Vec3 anchor = new Vec3(buf.readDouble(), buf.readDouble(), buf.readDouble());
        return new SwingSyncPacket(id, active, anchor);
    }

    public static void handle(SwingSyncPacket msg, Supplier<NetworkEvent.Context> ctx) {
        NetworkEvent.Context context = ctx.get();
        context.enqueueWork(() -> DistExecutor.unsafeRunWhenOn(Dist.CLIENT,
                () -> () -> ClientHooks.handleSwingSync(msg.player, msg.active, new Vec3(msg.x, msg.y, msg.z))));
        context.setPacketHandled(true);
    }
}
