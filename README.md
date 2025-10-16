## Crystal Damage Estimator (Client-only Fabric mod)

Purpose: visual combat aid for learning and analysis. Client-only, safe, and non-invasive. No automation or unfair advantage. Purely a visual overlay for educational or analytical use. Runs entirely client-side.

### Features
- Real-time overlay estimating End Crystal explosion damage to nearby entities
- Color-coded text: red (lethal), yellow (>=50% health), green (<50% health)
- Simple JSON config at `.minecraft/config/crystaldamageestimator.json`

### Build
1. Ensure JDK 21 is installed and on PATH
2. In this folder, run:
```bash
./gradlew build
```
3. The mod jar will be at `build/libs/`

### Install
- Copy the built jar into your Minecraft `mods/` folder on a Fabric 1.21.5 client with Fabric API

### Configuration
- A config file is created on first run at `.minecraft/config/crystaldamageestimator.json`
- Options include enabling overlay, lethal sound toggle, color values, and max tracking distance

### Ethics
- Does NOT automate combat or crystal placement
- Does NOT modify game mechanics or send server packets
- Purely client-side visualization



