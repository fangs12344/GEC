package com.example.addon.modules;

import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.settings.IntSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.settings.StringListSetting;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.block.Blocks;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;

import java.util.List;
import java.util.Random;

public class AutoSpawnerBreaker extends Module {
    private final SettingGroup sgGeneral = settings.getDefaultGroup();

    private final Setting<List<String>> friends = sgGeneral.add(new StringListSetting.Builder()
        .name("friends")
        .description("Names of players considered friends.")
        .defaultValue(List.of())
        .build()
    );

    // Random delay settings
    private final Setting<Integer> minDelay = sgGeneral.add(new IntSetting.Builder()
        .name("min-delay-ms")
        .description("Minimum delay between breaking spawners (in milliseconds).")
        .defaultValue(300)
        .min(0)
        .sliderMax(2000)
        .build()
    );

    private final Setting<Integer> maxDelay = sgGeneral.add(new IntSetting.Builder()
        .name("max-delay-ms")
        .description("Maximum delay between breaking spawners (in milliseconds).")
        .defaultValue(1000)
        .min(0)
        .sliderMax(5000)
        .build()
    );

    private final Random random = new Random();
    private long nextBreakTime = 0;

    public AutoSpawnerBreaker() {
        super(com.example.addon.AddonTemplate.CATEGORY, "auto-spawner-breaker", "Made By Fangs/GEC OWNER");
    }

    @EventHandler
    private void onTick(TickEvent.Pre event) {
        if (mc.player == null || mc.world == null) return;

        // Check for ANY non-friend player within 5 blocks (instant logout)
        for (var p : mc.world.getPlayers()) {
            if (!p.isSpectator()
                && !p.getUuid().equals(mc.player.getUuid())
                && !friends.get().contains(p.getGameProfile().getName())
                && p.squaredDistanceTo(mc.player) <= 5 * 5) {

                mc.getNetworkHandler().getConnection().disconnect(Text.literal("AutoLogout: Player too close!"));
                return;
            }
        }

        // Detect players (excluding self and friends) within 128 blocks (spawner breaker logic)
        boolean playerNearby = mc.world.getPlayers().stream()
            .anyMatch(p -> !p.isSpectator()
                && !p.getUuid().equals(mc.player.getUuid())
                && !friends.get().contains(p.getGameProfile().getName())
                && p.squaredDistanceTo(mc.player) <= 128 * 128);

        if (!playerNearby) return;

        // Respect cooldown
        long now = System.currentTimeMillis();
        if (now < nextBreakTime) return;

        boolean foundSpawner = false;
        BlockPos origin = mc.player.getBlockPos();

        // Scan a 5x5x5 cube around the player for spawners
        for (int x = -2; x <= 2; x++) {
            for (int y = -2; y <= 2; y++) {
                for (int z = -2; z <= 2; z++) {
                    BlockPos pos = origin.add(x, y, z);
                    if (mc.world.getBlockState(pos).getBlock() == Blocks.SPAWNER) {
                        foundSpawner = true;
                        breakSpawner(pos);

                        // Set random cooldown
                        int delay = random.nextInt(maxDelay.get() - minDelay.get() + 1) + minDelay.get();
                        nextBreakTime = now + delay;
                        return;
                    }
                }
            }
        }

        // No spawner found but a non-friend player is nearby
        if (!foundSpawner) {
            mc.getNetworkHandler().getConnection().disconnect(Text.literal("Fangs Is Hero/Fangs Saved You"));
        }
    }

    private void breakSpawner(BlockPos pos) {
        if (mc.interactionManager != null && mc.player != null) {
            mc.player.getInventory().selectedSlot = 0;
            mc.interactionManager.updateBlockBreakingProgress(pos, Direction.UP);
            mc.interactionManager.attackBlock(pos, Direction.UP);
        }
    }
}
