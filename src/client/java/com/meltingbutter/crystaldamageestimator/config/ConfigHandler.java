package com.meltingbutter.crystaldamageestimator.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.fabricmc.loader.api.FabricLoader;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;

public class ConfigHandler {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final String FILE_NAME = "crystaldamageestimator.json";

    private boolean overlayEnabled = true;
    private boolean lethalSoundEnabled = false;
    private double baseDamage = 12.0;
    private double explosionRadius = 12.0;
    private double maxTrackingDistance = 20.0;

    private int lethalColor = 0xFFFF5555; // red
    private int midColor = 0xFFFFFF55;    // yellow
    private int lowColor = 0xFF55FF55;    // green

    // HUD widget position and scale
    private double hudX = 10.0;
    private double hudY = 10.0;
    private double hudScale = 1.0;

    // Notification threshold: if damage/health >= threshold, display message
    private double notifyRatio = 0.5; // 0.5 = 50%
    private boolean notificationsEnabled = false;

    public static ConfigHandler loadOrCreate() {
        Path configPath = FabricLoader.getInstance().getConfigDir().resolve(FILE_NAME);
        if (Files.exists(configPath)) {
            try (Reader reader = Files.newBufferedReader(configPath)) {
                ConfigHandler cfg = GSON.fromJson(reader, ConfigHandler.class);
                if (cfg != null) return cfg;
            } catch (IOException ignored) {}
        }
        ConfigHandler defaults = new ConfigHandler();
        defaults.save();
        return defaults;
    }

    public void save() {
        Path configPath = FabricLoader.getInstance().getConfigDir().resolve(FILE_NAME);
        try {
            Files.createDirectories(configPath.getParent());
            try (Writer writer = Files.newBufferedWriter(configPath)) {
                GSON.toJson(this, writer);
            }
        } catch (IOException ignored) {}
    }

    public boolean isOverlayEnabled() { return overlayEnabled; }
    public boolean isLethalSoundEnabled() { return lethalSoundEnabled; }
    public double getBaseDamage() { return baseDamage; }
    public double getExplosionRadius() { return explosionRadius; }
    public double getMaxTrackingDistance() { return maxTrackingDistance; }
    public int getLethalColor() { return lethalColor; }
    public int getMidColor() { return midColor; }
    public int getLowColor() { return lowColor; }
    public double getHudX() { return hudX; }
    public double getHudY() { return hudY; }
    public double getHudScale() { return hudScale; }
    public double getNotifyRatio() { return notifyRatio; }
    public boolean isNotificationsEnabled() { return notificationsEnabled; }

    public void setOverlayEnabled(boolean value) { this.overlayEnabled = value; save(); }
    public void setLethalSoundEnabled(boolean value) { this.lethalSoundEnabled = value; save(); }
    public void setBaseDamage(double value) { this.baseDamage = value; save(); }
    public void setExplosionRadius(double value) { this.explosionRadius = value; save(); }
    public void setMaxTrackingDistance(double value) { this.maxTrackingDistance = value; save(); }
    public void setLethalColor(int value) { this.lethalColor = value; save(); }
    public void setMidColor(int value) { this.midColor = value; save(); }
    public void setLowColor(int value) { this.lowColor = value; save(); }
    public void setHudX(double value) { this.hudX = value; save(); }
    public void setHudY(double value) { this.hudY = value; save(); }
    public void setHudScale(double value) { this.hudScale = value; save(); }
    public void setNotifyRatio(double value) { this.notifyRatio = value; save(); }
    public void setNotificationsEnabled(boolean value) { this.notificationsEnabled = value; save(); }
}


