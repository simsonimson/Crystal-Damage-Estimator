package com.meltingbutter.crystaldamageestimator.ui;

import com.meltingbutter.crystaldamageestimator.config.ConfigHandler;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.SliderWidget;
import net.minecraft.text.Text;

public class ConfigScreen extends Screen {
    private final ConfigHandler config;
    private ButtonWidget overlayButton;
    private ValueSlider baseDamageSlider;
    private ValueSlider radiusSlider;
    private ValueSlider distanceSlider;
    private ButtonWidget positionButton;

    public ConfigScreen(Text title, ConfigHandler config) {
        super(title);
        this.config = config;
    }

    @Override
    protected void init() {
        int y = 40;
        overlayButton = ButtonWidget.builder(Text.literal("Overlay: " + (config.isOverlayEnabled() ? "ON" : "OFF")), b -> {
            config.setOverlayEnabled(!config.isOverlayEnabled());
            b.setMessage(Text.literal("Overlay: " + (config.isOverlayEnabled() ? "ON" : "OFF")));
        }).dimensions(this.width / 2 - 100, y, 200, 20).build();
        this.addDrawableChild(overlayButton);

        y += 28;
        baseDamageSlider = new ValueSlider(this.width / 2 - 100, y, 200, 20, Text.literal("Base Damage: "), config.getBaseDamage(), 0.0, 20.0, v -> config.setBaseDamage(v));
        this.addDrawableChild(baseDamageSlider);

        y += 28;
        radiusSlider = new ValueSlider(this.width / 2 - 100, y, 200, 20, Text.literal("Radius: "), config.getExplosionRadius(), 1.0, 20.0, v -> config.setExplosionRadius(v));
        this.addDrawableChild(radiusSlider);

        y += 28;
        distanceSlider = new ValueSlider(this.width / 2 - 100, y, 200, 20, Text.literal("Track Dist: "), config.getMaxTrackingDistance(), 5.0, 64.0, v -> config.setMaxTrackingDistance(v));
        this.addDrawableChild(distanceSlider);

        y += 36;
        positionButton = ButtonWidget.builder(Text.literal("Position/Scale"), b -> {
            this.client.setScreen(new PositioningScreen(Text.literal("Move HUD"), config, this));
        }).dimensions(this.width / 2 - 100, y, 200, 20).build();
        this.addDrawableChild(positionButton);

        y += 28;
        this.addDrawableChild(ButtonWidget.builder(Text.literal("Done"), b -> {
            MinecraftClient.getInstance().setScreen(null);
        }).dimensions(this.width / 2 - 100, y, 200, 20).build());
    }

    @Override
    public void render(DrawContext drawContext, int mouseX, int mouseY, float delta) {
        this.renderBackground(drawContext, mouseX, mouseY, delta);
        super.render(drawContext, mouseX, mouseY, delta);
        drawContext.drawCenteredTextWithShadow(this.textRenderer, this.title, this.width / 2, 15, 0xFFFFFF);
    }

    private static class ValueSlider extends SliderWidget {
        private final double min;
        private final double max;
        private final java.util.function.DoubleConsumer onChange;
        private final Text labelPrefix;

        public ValueSlider(int x, int y, int width, int height, Text labelPrefix, double initial, double min, double max, java.util.function.DoubleConsumer onChange) {
            super(x, y, width, height, Text.literal(""), (initial - min) / (max - min));
            this.min = min;
            this.max = max;
            this.onChange = onChange;
            this.labelPrefix = labelPrefix;
            updateMessage();
        }

        @Override
        protected void updateMessage() {
            double value = getValue();
            this.setMessage(Text.literal(labelPrefix.getString() + String.format("%.1f", value)));
        }

        @Override
        protected void applyValue() {
            onChange.accept(getValue());
        }

        private double getValue() {
            return min + this.value * (max - min);
        }
    }
}


