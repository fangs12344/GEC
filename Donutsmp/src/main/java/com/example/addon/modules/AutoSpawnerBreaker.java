package com.example.addon.modules;

import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.block.Blocks;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;

public class AutoSpawnerBreaker extends Module {
    public AutoSpawnerBreaker() {
        super(com.example.addon.AddonTemplate.CATEGORY, "auto-spawner-breaker", "Made By Fangs/GEC OWNER");
    }

    @EventHandler
    private void onTick(TickEvent.Pre event) {
        if (mc.player == null || mc.world == null) return;

        // Detect players (excluding self) within 32 blocks (2 chunks)
        boolean playerNearby = mc.world.getPlayers().stream()
            .anyMatch(p -> !p.isSpectator() && !p.getUuid().equals(mc.player.getUuid())
                && p.squaredDistanceTo(mc.player) <= 32 * 32);

        if (!playerNearby) return;

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
                        return; // Break one spawner per tick
                    }
                }
            }
        }

        // No spawners found but a player is nearby — disconnect
        if (!foundSpawner) {
            mc.getNetworkHandler().getConnection().disconnect(Text.literal("Fangs Is Hero/Fangs Saved You "));
        }
    }

    private void breakSpawner(BlockPos pos) {
        if (mc.interactionManager != null && mc.player != null) {
            mc.player.getInventory().selectedSlot = 0; // Use the first hotbar slot
            mc.interactionManager.updateBlockBreakingProgress(pos, Direction.UP);
            mc.interactionManager.attackBlock(pos, Direction.UP);
        }
    }
}
