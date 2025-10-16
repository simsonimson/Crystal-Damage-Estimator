package com.meltingbutter.crystaldamageestimator.logic;

import com.meltingbutter.crystaldamageestimator.config.ConfigHandler;
import net.minecraft.entity.LivingEntity;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;

public class DamageCalculator {
    private final ConfigHandler config;

    public DamageCalculator(ConfigHandler config) {
        this.config = config;
    }

    public DamageResult estimateDamageToEntity(LivingEntity entity, Vec3d explosionPos, ClientWorld world) {
        double baseDamage = config.getBaseDamage();
        double explosionRadius = config.getExplosionRadius();

        // Use the center mass point; factor vertical alignment bias
        Vec3d targetPos = entity.getPos().add(0, entity.getStandingEyeHeight() * 0.6, 0);
        double distance = targetPos.distanceTo(explosionPos);

        double attenuation = 1.0 - (distance / explosionRadius);
        if (attenuation < 0) attenuation = 0;

        // Simple vertical alignment multiplier: more damage if crystal is below target (y lower) or if target is airborne
        double verticalDelta = targetPos.y - explosionPos.y;
        double verticalMultiplier = 1.0;
        if (verticalDelta > 0) {
            // crystal below target → increase damage slightly
            verticalMultiplier += Math.min(0.25, verticalDelta / 6.0);
        }
        if (!entity.isOnGround()) {
            verticalMultiplier += 0.15; // jumping/airborne bias
        }

        // Very light LOS check: ray from explosion to target, if blocked reduce
        boolean blocked = isObstructed(world, explosionPos, targetPos, entity);
        double losMultiplier = blocked ? 0.6 : 1.0;

        double damage = baseDamage * attenuation * verticalMultiplier * losMultiplier;
        if (damage < 0) damage = 0;
        return new DamageResult(damage, blocked);
    }

    private boolean isObstructed(ClientWorld world, Vec3d from, Vec3d to, LivingEntity target) {
        // Use box ray expansion to approximate obstruction; keep very cheap
        Box targetBox = target.getBoundingBox().expand(0.1);
        Vec3d dir = to.subtract(from);
        double length = dir.length();
        if (length <= 0.001) return false;
        Vec3d step = dir.normalize().multiply(0.5);
        int steps = (int) Math.ceil(length / 0.5);
        Vec3d pos = from;
        for (int i = 0; i < steps; i++) {
            // if inside target box, stop
            if (targetBox.contains(pos)) return false;
            // if colliding with solid block
            if (!world.getBlockState(BlockPosUtil.at(pos)).isAir()) return true;
            pos = pos.add(step);
        }
        return false;
    }

    public static final class DamageResult {
        public final double damage;
        public final boolean blocked;

        public DamageResult(double damage, boolean blocked) {
            this.damage = damage;
            this.blocked = blocked;
        }
    }
}


