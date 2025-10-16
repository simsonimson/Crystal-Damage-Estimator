package com.meltingbutter.crystaldamageestimator;

import com.meltingbutter.crystaldamageestimator.config.ConfigHandler;
import com.meltingbutter.crystaldamageestimator.logic.DamageCalculator;
import com.meltingbutter.crystaldamageestimator.logic.EntityTracker;
import com.meltingbutter.crystaldamageestimator.overlay.CrystalDamageOverlayRenderer;
import com.meltingbutter.crystaldamageestimator.overlay.HudWidgetRenderer;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.text.Text;
import org.lwjgl.glfw.GLFW;

import com.meltingbutter.crystaldamageestimator.ui.ConfigScreen;

public class CrystalDamageEstimator implements ClientModInitializer {
    private static ConfigHandler config;
    private static EntityTracker entityTracker;
    private static DamageCalculator damageCalculator;
    private static KeyBinding toggleOverlayKey;
    private static KeyBinding openConfigKey;

    @Override
    public void onInitializeClient() {
        config = ConfigHandler.loadOrCreate();
        damageCalculator = new DamageCalculator(config);
        entityTracker = new EntityTracker(config, damageCalculator);

        toggleOverlayKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.crystaldamageestimator.toggle_overlay",
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_O,
                "key.categories.crystaldamageestimator"
        ));

        openConfigKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.crystaldamageestimator.open_config",
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_P,
                "key.categories.crystaldamageestimator"
        ));

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (client.world == null || client.player == null) return;
            while (toggleOverlayKey.wasPressed()) {
                configToggleOverlay();
            }
            while (openConfigKey.wasPressed()) {
                openConfigScreen(client);
            }
            entityTracker.update(client.world, client.player);
        });

        CrystalDamageOverlayRenderer.register(config, entityTracker);
        HudWidgetRenderer.register(config, entityTracker);
    }

    public static ConfigHandler getConfig() {
        return config;
    }

    private static void configToggleOverlay() {
        boolean newVal = !config.isOverlayEnabled();
        config.setOverlayEnabled(newVal);
        MinecraftClient.getInstance().inGameHud.getChatHud().addMessage(Text.literal("Crystal Damage Overlay: " + (newVal ? "ON" : "OFF")));
    }

    private static void openConfigScreen(MinecraftClient client) {
        client.execute(() -> client.setScreen(new ConfigScreen(Text.translatable("screen.crystaldamageestimator.title"), config)));
    }

    // no-op helper removed
}


