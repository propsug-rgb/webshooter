# Web Shooter (Minecraft 1.20.1, Forge 47.3.0)

A web shooter with swing mechanics and three web types. No other mods needed.

## Features
- **Swing**: aim at any block (up to ~55 blocks) and hold to swing. Pendulum physics, no fall damage while hanging.
  - Jump = reel the web in, Sneak = let it out, W = pump the swing forward. Release for a small launch.
  - The aim assist also checks a bit above your crosshair, so you can swing while looking forward.
- **Webs** (fired as projectiles):
  - **Combat Web**: roots the target (no walking or jumping, slow mining) and leaves a short-lived web.
  - **Web Bomb**: bursts on impact, damages and entangles everything within 4 blocks, spawns temporary webs.
  - **Shock Web**: electric hit that slows and weakens the target and arcs to up to 3 nearby mobs.
- **No dependencies**: only Forge is required. The keys work with the shooter in your main hand, off hand, or anywhere in your inventory.
- Temporary webs (`webshooter:web_trap`) remove themselves after 5-8 seconds.

## Controls
| Action | Held in main hand | Anywhere in inventory |
|---|---|---|
| Swing | hold Right-click in *Swing* mode | hold **G** |
| Shoot web | Right-click in Combat/Bomb/Shock mode | **R** |
| Change mode | Sneak + Right-click | **V** |

In *Swing* mode the **R** key fires a Combat Web. All keys are rebindable under Controls > Web Shooter.

## Crafting
```
I S I      I = iron ingot
R B R      S = string
I S I      R = redstone, B = slime ball
```

## Building
1. Install JDK 17.
2. Easiest route: download the Forge **1.20.1-47.3.0 MDK** from files.minecraftforge.net, then copy this project's
   `src/` folder, `build.gradle`, `gradle.properties` and `settings.gradle` over the MDK (keep the MDK's `gradlew` files).
3. Run `./gradlew build` (jar ends up in `build/libs/`) or `./gradlew runClient` to test.
4. Put the jar in your `mods` folder. Only Forge 1.20.1 is needed.

## Notes
- The code was written without being compiled in this environment (no access to Forge's Maven), so expect to fix a
  small API mismatch or two on first build.
