package com.webshooter.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.webshooter.WebShooterMod;
import com.webshooter.item.WebMode;
import com.webshooter.item.WebShooterItem;
import com.webshooter.network.CyclePacket;
import com.webshooter.network.ModNetwork;
import com.webshooter.network.ShootPacket;
import com.webshooter.network.SwingStartPacket;
import com.webshooter.network.SwingStopPacket;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.client.event.RenderLevelStageEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.Map;
import java.util.UUID;

@Mod.EventBusSubscriber(modid = WebShooterMod.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
public final class ClientEvents {

    /** How far above the crosshair we also look for something to attach to, in degrees. */
    private static final float[] LIFTS = {0F, 12F, 24F, 36F, 48F};

    private static boolean prevWantSwing;
    private static boolean prevUseDown;

    // ------------------------------------------------------------------ input

    @SubscribeEvent
    public static void onInteract(InputEvent.InteractionKeyMappingTriggered event) {
        if (!event.isUseItem()) {
            return;
        }
        LocalPlayer player = Minecraft.getInstance().player;
        if (player == null || !(player.getMainHandItem().getItem() instanceof WebShooterItem)) {
            return;
        }
        // Holding the shooter in the main hand: right-click never places blocks or uses the off hand.
        event.setCanceled(true);
        if (event.getHand() != InteractionHand.MAIN_HAND) {
            return;
        }
        if (player.isShiftKeyDown()) {
            if (!prevUseDown) {
                ModNetwork.CHANNEL.sendToServer(new CyclePacket());
            }
        } else if (WebShooterItem.getMode(player.getMainHandItem()) != WebMode.SWING) {
            ModNetwork.CHANNEL.sendToServer(new ShootPacket());
            event.setSwingHand(true);
        }
    }

    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) {
            return;
        }
        Minecraft mc = Minecraft.getInstance();
        LocalPlayer player = mc.player;
        if (player == null || mc.level == null) {
            ClientSwingState.reset();
            prevWantSwing = false;
            return;
        }

        ItemStack shooter = WebShooterItem.findShooter(player);
        boolean hasShooter = !shooter.isEmpty();

        while (ModKeys.SHOOT.consumeClick()) {
            if (hasShooter) {
                ModNetwork.CHANNEL.sendToServer(new ShootPacket());
            }
        }
        while (ModKeys.CYCLE.consumeClick()) {
            if (hasShooter) {
                ModNetwork.CHANNEL.sendToServer(new CyclePacket());
            }
        }

        ItemStack main = player.getMainHandItem();
        boolean useToSwing = mc.screen == null
                && main.getItem() instanceof WebShooterItem
                && WebShooterItem.getMode(main) == WebMode.SWING
                && mc.options.keyUse.isDown()
                && !player.isShiftKeyDown();
        boolean wantSwing = hasShooter && mc.screen == null && (ModKeys.SWING.isDown() || useToSwing);

        if (wantSwing && !prevWantSwing) {
            startSwing(player);
        } else if (!wantSwing && ClientSwingState.active) {
            stopSwing(player);
        }
        prevWantSwing = wantSwing;
        prevUseDown = mc.options.keyUse.isDown();

        if (ClientSwingState.active) {
            if (!hasShooter || !player.isAlive() || player.isInWater() || player.isFallFlying()
                    || player.isPassenger() || mc.level.getBlockState(ClientSwingState.anchorBlock).isAir()) {
                stopSwing(player);
            } else {
                tickSwing(mc, player);
            }
        }
    }

    // ------------------------------------------------------------------ swinging

    private static BlockHitResult findAnchor(LocalPlayer player) {
        Vec3 eye = player.getEyePosition();
        for (float lift : LIFTS) {
            float pitch = Mth.clamp(player.getXRot() - lift, -90.0F, 90.0F);
            Vec3 dir = Vec3.directionFromRotation(pitch, player.getYRot());
            Vec3 end = eye.add(dir.scale(ClientSwingState.MAX_RANGE));
            BlockHitResult hit = player.level().clip(new ClipContext(
                    eye, end, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, player));
            if (hit.getType() == HitResult.Type.BLOCK && hit.getLocation().distanceTo(eye) > 3.0) {
                return hit;
            }
        }
        return null;
    }

    private static Vec3 chest(LocalPlayer player) {
        return player.position().add(0.0, player.getBbHeight() * 0.6, 0.0);
    }

    private static void startSwing(LocalPlayer player) {
        BlockHitResult hit = findAnchor(player);
        if (hit == null) {
            player.displayClientMessage(Component.translatable("message.webshooter.no_anchor"), true);
            return;
        }
        ClientSwingState.active = true;
        ClientSwingState.anchor = hit.getLocation();
        ClientSwingState.anchorBlock = hit.getBlockPos();
        ClientSwingState.length = Math.min(ClientSwingState.MAX_LENGTH,
                Math.max(3.0, chest(player).distanceTo(hit.getLocation())));
        if (player.onGround()) {
            player.setDeltaMovement(player.getDeltaMovement().add(0.0, 0.3, 0.0));
        }
        ModNetwork.CHANNEL.sendToServer(new SwingStartPacket(hit.getLocation()));
    }

    private static void stopSwing(LocalPlayer player) {
        if (!ClientSwingState.active) {
            return;
        }
        ClientSwingState.active = false;
        Vec3 v = player.getDeltaMovement();
        player.setDeltaMovement(v.x * 1.1, v.y + 0.12, v.z * 1.1); // small launch on release
        ModNetwork.CHANNEL.sendToServer(new SwingStopPacket());
    }

    /** Pendulum constraint: the player may not get farther from the anchor than the rope is long. */
    private static void tickSwing(Minecraft mc, LocalPlayer player) {
        Vec3 anchor = ClientSwingState.anchor;
        if (mc.options.keyJump.isDown()) {
            ClientSwingState.length = Math.max(2.5, ClientSwingState.length - 0.25); // reel in
        }
        if (mc.options.keyShift.isDown()) {
            ClientSwingState.length = Math.min(ClientSwingState.MAX_LENGTH, ClientSwingState.length + 0.25); // let out
        }

        Vec3 rope = chest(player).subtract(anchor);
        double dist = rope.length();
        Vec3 v = player.getDeltaMovement();

        if (dist > ClientSwingState.length && dist > 1.0E-4) {
            Vec3 dir = rope.scale(1.0 / dist);
            double outward = v.dot(dir);
            if (outward > 0.0) {
                v = v.subtract(dir.scale(outward)); // cancel velocity that would stretch the rope
            }
            v = v.add(dir.scale(-(dist - ClientSwingState.length) * 0.25)); // pull back onto the circle
        }

        if (mc.options.keyUp.isDown()) { // pump the swing forwards
            Vec3 look = player.getLookAngle();
            Vec3 flat = new Vec3(look.x, 0.0, look.z);
            if (flat.lengthSqr() > 1.0E-4) {
                v = v.add(flat.normalize().scale(0.03));
            }
        }

        if (!player.onGround()) {
            v = new Vec3(v.x * 1.09, v.y, v.z * 1.09); // give back most of vanilla's horizontal air drag
        }
        player.setDeltaMovement(v);
        player.fallDistance = 0.0F;
    }

    // ------------------------------------------------------------------ rendering the web lines

    @SubscribeEvent
    public static void onRenderLevel(RenderLevelStageEvent event) {
        if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_PARTICLES) {
            return;
        }
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null || mc.player == null) {
            return;
        }
        boolean any = ClientSwingState.active || !ClientSwingState.OTHERS.isEmpty();
        if (!any) {
            return;
        }

        PoseStack poseStack = event.getPoseStack();
        float partial = event.getPartialTick();
        Vec3 cam = event.getCamera().getPosition();
        MultiBufferSource.BufferSource buffers = mc.renderBuffers().bufferSource();
        VertexConsumer lines = buffers.getBuffer(RenderType.lines());

        poseStack.pushPose();
        poseStack.translate(-cam.x, -cam.y, -cam.z);
        PoseStack.Pose pose = poseStack.last();

        if (ClientSwingState.active) {
            drawWeb(lines, pose, handPos(mc.player, partial), ClientSwingState.anchor);
        }
        for (Map.Entry<UUID, Vec3> entry : ClientSwingState.OTHERS.entrySet()) {
            AbstractClientPlayer other = (AbstractClientPlayer) mc.level.getPlayerByUUID(entry.getKey());
            if (other != null) {
                drawWeb(lines, pose, handPos(other, partial), entry.getValue());
            }
        }

        poseStack.popPose();
        buffers.endBatch(RenderType.lines());
    }

    private static Vec3 handPos(net.minecraft.world.entity.player.Player player, float partial) {
        Vec3 base = player.getPosition(partial);
        Vec3 right = Vec3.directionFromRotation(0.0F, player.getYRot() + 90.0F);
        Vec3 forward = Vec3.directionFromRotation(0.0F, player.getYRot());
        return base.add(0.0, player.getEyeHeight() - 0.45, 0.0)
                .add(right.scale(-0.3)) // left wrist, where the bracelet sits
                .add(forward.scale(0.35));
    }

    private static void drawWeb(VertexConsumer vc, PoseStack.Pose pose, Vec3 from, Vec3 to) {
        Vec3 d = to.subtract(from);
        if (d.lengthSqr() < 1.0E-4) {
            return;
        }
        Vec3 dir = d.normalize();
        Vec3 side = dir.cross(new Vec3(0.0, 1.0, 0.0));
        side = side.lengthSqr() < 1.0E-4 ? new Vec3(1.0, 0.0, 0.0) : side.normalize();
        Vec3 up = dir.cross(side).normalize();
        // three hair-thin strands so the web line reads as a thicker silk thread
        line(vc, pose, from, to, dir);
        line(vc, pose, from.add(side.scale(0.012)), to.add(side.scale(0.012)), dir);
        line(vc, pose, from.add(up.scale(0.012)), to.add(up.scale(0.012)), dir);
    }

    private static void line(VertexConsumer vc, PoseStack.Pose pose, Vec3 a, Vec3 b, Vec3 n) {
        vc.vertex(pose.pose(), (float) a.x, (float) a.y, (float) a.z)
                .color(235, 240, 245, 255)
                .normal(pose.normal(), (float) n.x, (float) n.y, (float) n.z)
                .endVertex();
        vc.vertex(pose.pose(), (float) b.x, (float) b.y, (float) b.z)
                .color(235, 240, 245, 255)
                .normal(pose.normal(), (float) n.x, (float) n.y, (float) n.z)
                .endVertex();
    }

    private ClientEvents() {}
}
