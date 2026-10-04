package com.webshooter.swing;

import com.webshooter.item.WebShooterItem;
import com.webshooter.network.ModNetwork;
import com.webshooter.network.SwingSyncPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.network.PacketDistributor;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Server side of swinging. The physics run on the swinging player's own client (movement is
 * client-authoritative in Minecraft); the server validates the anchor point, cancels fall damage
 * while swinging and tells nearby players so they can draw the web line.
 */
@Mod.EventBusSubscriber(modid = com.webshooter.WebShooterMod.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public final class SwingManager {
    public static final double MAX_RANGE = 60.0;

    private static final Map<UUID, Vec3> ACTIVE = new HashMap<>();

    public static boolean start(ServerPlayer player, Vec3 anchor) {
        if (WebShooterItem.findShooter(player).isEmpty()) {
            return false;
        }
        Vec3 eye = player.getEyePosition();
        if (eye.distanceTo(anchor) > MAX_RANGE) {
            return false;
        }
        // The line of sight to the anchor must end at (or very near) that point.
        BlockHitResult hit = player.level().clip(new ClipContext(
                eye, anchor, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, player));
        if (hit.getType() != HitResult.Type.MISS && hit.getLocation().distanceTo(anchor) > 2.0) {
            return false;
        }
        ACTIVE.put(player.getUUID(), anchor);
        broadcast(player, true, anchor);
        return true;
    }

    public static void stop(ServerPlayer player) {
        Vec3 removed = ACTIVE.remove(player.getUUID());
        if (removed != null) {
            broadcast(player, false, removed);
        }
    }

    public static boolean isSwinging(ServerPlayer player) {
        return ACTIVE.containsKey(player.getUUID());
    }

    private static void broadcast(ServerPlayer player, boolean active, Vec3 anchor) {
        ModNetwork.CHANNEL.send(PacketDistributor.TRACKING_ENTITY_AND_SELF.with(() -> player),
                new SwingSyncPacket(player.getUUID(), active, anchor));
    }

    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END || !(event.player instanceof ServerPlayer player)) {
            return;
        }
        Vec3 anchor = ACTIVE.get(player.getUUID());
        if (anchor == null) {
            return;
        }
        if (!player.isAlive() || player.isSpectator()
                || WebShooterItem.findShooter(player).isEmpty()
                || player.position().distanceTo(anchor) > MAX_RANGE + 20.0) {
            stop(player);
            return;
        }
        player.fallDistance = 0.0F; // no fall damage while hanging on the web
    }

    @SubscribeEvent
    public static void onStartTracking(PlayerEvent.StartTracking event) {
        if (event.getTarget() instanceof ServerPlayer target
                && event.getEntity() instanceof ServerPlayer viewer) {
            Vec3 anchor = ACTIVE.get(target.getUUID());
            if (anchor != null) {
                ModNetwork.CHANNEL.send(PacketDistributor.PLAYER.with(() -> viewer),
                        new SwingSyncPacket(target.getUUID(), true, anchor));
            }
        }
    }

    @SubscribeEvent
    public static void onLogout(PlayerEvent.PlayerLoggedOutEvent event) {
        ACTIVE.remove(event.getEntity().getUUID());
    }

    private SwingManager() {}
}
