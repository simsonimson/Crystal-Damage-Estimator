package com.meltingbutter.crystaldamageestimator.overlay;

import com.meltingbutter.crystaldamageestimator.config.ConfigHandler;
import com.meltingbutter.crystaldamageestimator.logic.EntityTracker;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.LivingEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import org.joml.Matrix4f;

import java.awt.*;

public final class CrystalDamageOverlayRenderer {
    private CrystalDamageOverlayRenderer() {}

    public static void register(ConfigHandler config, EntityTracker tracker) {
        // Remove 2D HUD rendering - only 3D above head

        // 3D world text above heads
        WorldRenderEvents.AFTER_ENTITIES.register(context -> {
            if (!config.isOverlayEnabled()) return;
            MinecraftClient client = MinecraftClient.getInstance();
            Camera camera = context.camera();
            Vec3d cameraPos = camera.getPos();

            MatrixStack matrices = context.matrixStack();
            VertexConsumerProvider consumers = context.consumers();
            TextRenderer textRenderer = client.textRenderer;

            for (EntityTracker.TrackedEntity tracked : tracker.getTracked().values()) {
                LivingEntity e = tracked.entity;
                if (e.isRemoved() || !e.isAlive()) continue;

                Vec3d pos = e.getPos().add(0, e.getStandingEyeHeight() + 0.5, 0);
                double dx = pos.x - cameraPos.x;
                double dy = pos.y - cameraPos.y;
                double dz = pos.z - cameraPos.z;

                matrices.push();
                matrices.translate(dx, dy, dz);
                matrices.multiply(camera.getRotation());
                matrices.scale(-0.03f, -0.03f, 0.03f);

                double health = e.getHealth();
                double damage = tracked.estimatedDamage;
                int color = pickColor(config, damage, health);
                String text = formatNumber(damage);
                if (tracked.distanceToCrystal != null) {
                    text += "  (" + formatNumber(tracked.distanceToCrystal) + "m)";
                }

                float x = -textRenderer.getWidth(text) / 2f;
                Matrix4f matrix4f = matrices.peek().getPositionMatrix();
                textRenderer.draw(text, x, 0, color, true, matrix4f, consumers, TextRenderer.TextLayerType.SEE_THROUGH, 0, 15728880);

                matrices.pop();
            }

            // Highlight the tracked crystal position with a simple cross
            if (tracker.getCurrentCrystalPos() != null) {
                Vec3d c = tracker.getCurrentCrystalPos();
                double dx = c.x - cameraPos.x;
                double dy = c.y - cameraPos.y;
                double dz = c.z - cameraPos.z;
                matrices.push();
                matrices.translate(dx, dy + 1.0, dz);
                matrices.multiply(camera.getRotation());
                matrices.scale(0.02f, 0.02f, 0.02f);
                String marker = "+";
                Matrix4f m = matrices.peek().getPositionMatrix();
                client.textRenderer.draw(marker, -client.textRenderer.getWidth(marker) / 2f, 0, 0xFFFF5555, true, m, consumers, TextRenderer.TextLayerType.SEE_THROUGH, 0, 15728880);
                matrices.pop();
            }
        });
    }

    private static int pickColor(ConfigHandler config, double damage, double health) {
        // Flip colors: green for high damage, red for low
        if (damage >= health) return config.getLowColor();
        if (damage >= health * 0.5) return config.getMidColor();
        return config.getLethalColor();
    }

    private static String formatNumber(double value) {
        return String.format("%.1f", value);
    }
}


