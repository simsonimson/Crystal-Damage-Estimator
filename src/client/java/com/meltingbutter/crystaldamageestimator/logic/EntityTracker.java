package com.meltingbutter.crystaldamageestimator.logic;

import com.meltingbutter.crystaldamageestimator.config.ConfigHandler;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.Entity;
import net.minecraft.entity.decoration.EndCrystalEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.math.Vec3d;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class EntityTracker {
    private final ConfigHandler config;
    private final DamageCalculator damageCalculator;
    private final Map<UUID, TrackedEntity> tracked = new HashMap<>();
    private Vec3d currentCrystalPos;
    private UUID currentCrystalId;

    public EntityTracker(ConfigHandler config, DamageCalculator damageCalculator) {
        this.config = config;
        this.damageCalculator = damageCalculator;
    }

    public void update(ClientWorld world, ClientPlayerEntity player) {
        if (!config.isOverlayEnabled()) {
            tracked.clear();
            return;
        }

        tracked.clear();

        double maxDistance = config.getMaxTrackingDistance();
        Vec3d playerPos = player.getPos();

        // Find nearest End Crystal to the player within tracking distance
        double nearestSq = Double.MAX_VALUE;
        Vec3d nearestPos = null;
        UUID nearestId = null;
        for (Entity e : world.getEntities()) {
            if (e instanceof EndCrystalEntity) {
                double dSq = e.getPos().squaredDistanceTo(playerPos);
                if (dSq < nearestSq && Math.sqrt(dSq) <= maxDistance) {
                    nearestSq = dSq;
                    nearestPos = e.getPos();
                    nearestId = e.getUuid();
                }
            }
        }
        this.currentCrystalPos = nearestPos;
        this.currentCrystalId = nearestId;

        // If no crystal found, do not compute entity damages
        if (this.currentCrystalPos == null) {
            return;
        }

        for (Entity entity : world.getEntities()) {
            if (!(entity instanceof LivingEntity living)) continue;
            if (entity == player) continue;
            if (entity.isRemoved() || !entity.isAlive()) continue;

            double distance = entity.getPos().distanceTo(playerPos);
            if (distance > maxDistance) continue;

            Vec3d explosionPos = this.currentCrystalPos;
            DamageCalculator.DamageResult result = damageCalculator.estimateDamageToEntity(living, explosionPos, world);
            Double distanceToCrystal = living.getPos().distanceTo(this.currentCrystalPos);
            tracked.put(entity.getUuid(), new TrackedEntity(living, result.damage, distanceToCrystal, result.blocked));
        }
    }

    public Map<UUID, TrackedEntity> getTracked() {
        return tracked;
    }

    public Vec3d getCurrentCrystalPos() {
        return currentCrystalPos;
    }

    public UUID getCurrentCrystalId() {
        return currentCrystalId;
    }

    public static class TrackedEntity {
        public final LivingEntity entity;
        public final double estimatedDamage;
        public final Double distanceToCrystal;
        public final boolean blocked;

        public TrackedEntity(LivingEntity entity, double estimatedDamage, Double distanceToCrystal, boolean blocked) {
            this.entity = entity;
            this.estimatedDamage = estimatedDamage;
            this.distanceToCrystal = distanceToCrystal;
            this.blocked = blocked;
        }
    }
}


