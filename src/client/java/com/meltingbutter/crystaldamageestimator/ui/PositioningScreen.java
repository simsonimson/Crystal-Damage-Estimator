package com.meltingbutter.crystaldamageestimator.ui;

import com.meltingbutter.crystaldamageestimator.config.ConfigHandler;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;

public class PositioningScreen extends Screen {
    private final ConfigHandler config;
    private final Screen parent;
    private boolean dragging;
    private double dragOffsetX;
    private double dragOffsetY;

    public PositioningScreen(Text title, ConfigHandler config, Screen parent) {
        super(title);
        this.config = config;
        this.parent = parent;
    }

    @Override
    protected void init() {
        this.addDrawableChild(ButtonWidget.builder(Text.literal("Scale -"), b -> adjustScale(-0.1)).dimensions(this.width / 2 - 100, this.height - 40, 60, 20).build());
        this.addDrawableChild(ButtonWidget.builder(Text.literal("Scale +"), b -> adjustScale(0.1)).dimensions(this.width / 2 - 35, this.height - 40, 60, 20).build());
        this.addDrawableChild(ButtonWidget.builder(Text.literal("Done"), b -> this.client.setScreen(parent)).dimensions(this.width / 2 + 35, this.height - 40, 60, 20).build());
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        double x = config.getHudX();
        double y = config.getHudY();
        double s = config.getHudScale();
        if (mouseX >= x && mouseX <= x + 140 * s && mouseY >= y && mouseY <= y + 24 * s) {
            dragging = true;
            dragOffsetX = mouseX - x;
            dragOffsetY = mouseY - y;
            return true;
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        dragging = false;
        return super.mouseReleased(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY) {
        if (dragging) {
            config.setHudX(Math.max(0, mouseX - dragOffsetX));
            config.setHudY(Math.max(0, mouseY - dragOffsetY));
            return true;
        }
        return super.mouseDragged(mouseX, mouseY, button, deltaX, deltaY);
    }

    private void adjustScale(double delta) {
        double s = Math.max(0.5, Math.min(3.0, config.getHudScale() + delta));
        config.setHudScale(s);
    }

    @Override
    public void render(DrawContext drawContext, int mouseX, int mouseY, float delta) {
        this.renderBackground(drawContext, mouseX, mouseY, delta);
        super.render(drawContext, mouseX, mouseY, delta);
        drawContext.drawCenteredTextWithShadow(this.textRenderer, this.title, this.width / 2, 15, 0xFFFFFF);

        // Draw a preview rect at HUD position
        int x = (int) config.getHudX();
        int y = (int) config.getHudY();
        int w = (int) (140 * config.getHudScale());
        int h = (int) (24 * config.getHudScale());
        drawContext.fill(x - 1, y - 1, x + w + 1, y + h + 1, 0x80000000);
        drawContext.drawText(this.textRenderer, Text.literal("Drag here"), x + 4, y + 6, 0xFFFFFFFF, true);
    }
}



