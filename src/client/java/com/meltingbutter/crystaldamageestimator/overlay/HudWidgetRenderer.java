package com.meltingbutter.crystaldamageestimator.overlay;

import com.meltingbutter.crystaldamageestimator.config.ConfigHandler;
import com.meltingbutter.crystaldamageestimator.logic.EntityTracker;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public final class HudWidgetRenderer {
    private static final Identifier ICON = Identifier.of("crystaldamageestimator", "icon.png");

    private HudWidgetRenderer() {}

    public static void register(ConfigHandler config, EntityTracker tracker) {
        HudRenderCallback.EVENT.register((drawContext, tickDelta) -> {
            if (!config.isOverlayEnabled()) return;
            MinecraftClient client = MinecraftClient.getInstance();
            if (client.player == null) return;

            int baseX = (int) Math.round(config.getHudX());
            int baseY = (int) Math.round(config.getHudY());
            float scale = (float) config.getHudScale();

            drawContext.getMatrices().push();
            drawContext.getMatrices().translate(baseX, baseY, 0);
            drawContext.getMatrices().scale(scale, scale, 1f);

            // icon 16x16
            drawContext.drawTexture(RenderLayer::getGuiTextured, ICON, 0, 0, 0f, 0f, 16, 16, 16, 16);

            TextRenderer tr = client.textRenderer;

            // If no crystal, show message
            if (tracker.getCurrentCrystalPos() == null) {
                drawContext.drawText(tr, Text.literal("No crystals placed"), 20, 3, 0xFFFFFFFF, true);
                drawContext.getMatrices().pop();
                return;
            }

            // Top 5 entities by estimated damage
            List<EntityTracker.TrackedEntity> top = tracker.getTracked().values().stream()
                    .sorted(Comparator.comparingDouble((EntityTracker.TrackedEntity t) -> t.estimatedDamage).reversed())
                    .limit(5)
                    .collect(Collectors.toList());

            int y = 0;
            int x = 20;
            if (top.isEmpty()) {
                drawContext.drawText(tr, Text.literal("No targets"), x, y + 3, 0xFFFFFFFF, true);
            } else {
                for (EntityTracker.TrackedEntity te : top) {
                    double health = te.entity.getHealth();
                    double dmg = te.estimatedDamage;
                    int color = pickColor(dmg, health, config);
                    String dist = te.distanceToCrystal != null ? " (" + format(d -> d, te.distanceToCrystal) + "m)" : "";
                    String blocked = te.blocked ? " (blocked)" : "";
                    String label = shorten(te.entity.getName().getString(), 12) + ": " + format(v -> v, dmg) + dist + blocked;
                    if (te.entity instanceof PlayerEntity p) {
                        Identifier skin = MinecraftClient.getInstance().getSkinProvider().getSkinTextures(p.getGameProfile()).texture();
                        drawContext.drawTexture(RenderLayer::getGuiTextured, skin, x - 12, y - 1, 8f, 8f, 8, 8, 64, 64);
                    }
                    drawContext.drawText(tr, label, x, y + 3, color, true);
                    y += 10;
                }
            }

            // Notification: show hint to blow up if any ratio exceeds threshold
            if (config.isNotificationsEnabled()) {
                boolean trigger = top.stream().anyMatch(te -> te.entity.getHealth() > 0 && te.estimatedDamage / te.entity.getHealth() >= config.getNotifyRatio());
                if (trigger) {
                    drawContext.drawText(tr, Text.literal("Notification: Blow up crystal!"), 0, -12, 0xFFFF5555, true);
                }
            }

            drawContext.getMatrices().pop();
        });
    }

    private static int pickColor(double damage, double health, ConfigHandler config) {
        // Green (good to crystal) for lethal/high damage, yellow mid, red low
        if (damage >= health) return config.getLowColor();
        if (damage >= health * 0.5) return config.getMidColor();
        return config.getLethalColor();
    }

    private static String format(java.util.function.DoubleUnaryOperator op, double v) {
        return String.format("%.1f", op.applyAsDouble(v));
    }

    private static String shorten(String s, int max) {
        if (s.length() <= max) return s;
        return s.substring(0, Math.max(0, max - 1)) + "…";
    }
}


