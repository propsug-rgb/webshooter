package com.webshooter.entity;

import com.webshooter.ModBlocks;
import com.webshooter.ModEntities;
import com.webshooter.item.WebMode;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.ThrowableProjectile;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.entity.projectile.ItemSupplier;
import org.jetbrains.annotations.Nullable;

import java.util.Comparator;
import java.util.List;

public class WebProjectile extends ThrowableProjectile implements ItemSupplier {
    private static final EntityDataAccessor<Integer> MODE =
            SynchedEntityData.defineId(WebProjectile.class, EntityDataSerializers.INT);

    public WebProjectile(EntityType<? extends WebProjectile> type, Level level) {
        super(type, level);
    }

    public WebProjectile(Level level, LivingEntity owner, WebMode mode) {
        super(ModEntities.WEB_PROJECTILE.get(), owner, level);
        setMode(mode);
    }

    @Override
    protected void defineSynchedData() {
        this.entityData.define(MODE, WebMode.COMBAT.ordinal());
    }

    public WebMode getMode() {
        return WebMode.byId(this.entityData.get(MODE));
    }

    public void setMode(WebMode mode) {
        this.entityData.set(MODE, mode.ordinal());
    }

    @Override
    protected float getGravity() {
        return 0.01F;
    }

    @Override
    public ItemStack getItem() {
        return new ItemStack(Items.COBWEB);
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putInt("WebMode", getMode().ordinal());
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        setMode(WebMode.byId(tag.getInt("WebMode")));
    }

    @Override
    public void tick() {
        super.tick();
        if (level().isClientSide) {
            switch (getMode()) {
                case BOMB -> level().addParticle(ParticleTypes.SMOKE, getX(), getY(), getZ(), 0, 0, 0);
                case SHOCK -> level().addParticle(ParticleTypes.ELECTRIC_SPARK, getX(), getY(), getZ(), 0, 0, 0);
                default -> level().addParticle(ParticleTypes.CLOUD, getX(), getY(), getZ(), 0, 0, 0);
            }
        } else if (tickCount > 80) {
            discard();
        }
    }

    @Override
    protected void onHitEntity(EntityHitResult result) {
        super.onHitEntity(result);
        if (level().isClientSide) {
            return;
        }
        Entity hit = result.getEntity();
        if (hit == getOwner()) {
            return;
        }
        impact(hit.blockPosition(), hit);
        discard();
    }

    @Override
    protected void onHitBlock(BlockHitResult result) {
        super.onHitBlock(result);
        if (level().isClientSide) {
            return;
        }
        impact(result.getBlockPos().relative(result.getDirection()), null);
        discard();
    }

    // ---------------------------------------------------------------- impact effects

    private void impact(BlockPos center, @Nullable Entity directHit) {
        ServerLevel sl = (ServerLevel) level();
        Entity owner = getOwner();
        Vec3 pos = Vec3.atCenterOf(center);

        switch (getMode()) {
            case BOMB -> {
                sl.sendParticles(ParticleTypes.POOF, pos.x, pos.y, pos.z, 25, 0.6, 0.6, 0.6, 0.05);
                sl.playSound(null, center, SoundEvents.SLIME_BLOCK_BREAK, SoundSource.PLAYERS, 1.0F, 0.6F);
                AABB box = new AABB(pos, pos).inflate(4.0);
                for (LivingEntity target : sl.getEntitiesOfClass(LivingEntity.class, box,
                        e -> e != owner && e.isAlive())) {
                    target.hurt(damageSources().thrown(this, owner), 4.0F);
                    entangle(target, 140);
                }
                for (int x = -2; x <= 2; x++) {
                    for (int y = -2; y <= 2; y++) {
                        for (int z = -2; z <= 2; z++) {
                            if (x * x + y * y + z * z <= 5 && random.nextFloat() < 0.75F) {
                                placeWeb(center.offset(x, y, z));
                            }
                        }
                    }
                }
            }
            case SHOCK -> {
                sl.sendParticles(ParticleTypes.ELECTRIC_SPARK, pos.x, pos.y, pos.z, 20, 0.4, 0.4, 0.4, 0.3);
                if (directHit instanceof LivingEntity victim) {
                    zap(victim, 5.0F, owner);
                    List<LivingEntity> near = sl.getEntitiesOfClass(LivingEntity.class,
                            victim.getBoundingBox().inflate(5.0),
                            e -> e != victim && e != owner && e.isAlive());
                    near.sort(Comparator.comparingDouble(e -> e.distanceToSqr(victim)));
                    for (int i = 0; i < Math.min(3, near.size()); i++) {
                        zap(near.get(i), 3.0F, owner);
                        arc(sl, victim.position().add(0, 1, 0), near.get(i).position().add(0, 1, 0));
                    }
                }
            }
            default -> { // COMBAT
                if (directHit instanceof LivingEntity victim) {
                    victim.hurt(damageSources().thrown(this, owner), 2.0F);
                    entangle(victim, 80);
                    placeWeb(victim.blockPosition());
                    placeWeb(victim.blockPosition().above());
                } else {
                    placeWeb(center);
                }
                sl.playSound(null, center, SoundEvents.SLIME_BLOCK_PLACE, SoundSource.PLAYERS, 0.8F, 1.3F);
            }
        }
    }

    /** Roots a mob or player in place: no walking, no jumping, slow mining. */
    private static void entangle(LivingEntity target, int ticks) {
        target.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, ticks, 6));
        target.addEffect(new MobEffectInstance(MobEffects.JUMP, ticks, 128));
        target.addEffect(new MobEffectInstance(MobEffects.DIG_SLOWDOWN, ticks, 2));
    }

    private void zap(LivingEntity target, float damage, @Nullable Entity owner) {
        target.hurt(damageSources().thrown(this, owner), damage);
        target.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 50, 3));
        target.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 80, 1));
        level().playSound(null, target.blockPosition(), SoundEvents.LIGHTNING_BOLT_IMPACT,
                SoundSource.PLAYERS, 0.4F, 2.0F);
    }

    private static void arc(ServerLevel level, Vec3 from, Vec3 to) {
        for (int i = 0; i <= 8; i++) {
            Vec3 p = from.lerp(to, i / 8.0);
            level.sendParticles(ParticleTypes.ELECTRIC_SPARK, p.x, p.y, p.z, 1, 0.05, 0.05, 0.05, 0.0);
        }
    }

    private void placeWeb(BlockPos pos) {
        if (level().getBlockState(pos).isAir()) {
            level().setBlock(pos, ModBlocks.WEB_TRAP.get().defaultBlockState(), 3);
        }
    }
}
