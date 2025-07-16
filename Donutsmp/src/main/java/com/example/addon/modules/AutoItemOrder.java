package com.example.addon.modules;

import com.example.addon.AddonTemplate;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.settings.*;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.ingame.GenericContainerScreen;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.slot.Slot;
import net.minecraft.screen.slot.SlotActionType;

public class AutoItemOrder extends Module {
    private enum Stage {
        IDLE, SEND_COMMAND, SELECT_ITEM, DEPOSIT_ITEMS, CLOSE_ORDER, CONFIRM_ORDER, COMPLETE
    }

    private Stage stage = Stage.IDLE;
    private long stageStart = 0;

    private final SettingGroup sgGeneral = settings.getDefaultGroup();

    private final Setting<String> itemName = sgGeneral.add(new StringSetting.Builder()
        .name("Order Item")
        .description("Name of the item to order.")
        .defaultValue("Fangs Toes")
        .build()
    );

    private final Setting<Integer> delay = sgGeneral.add(new IntSetting.Builder()
        .name("Delay (ms)")
        .description("Delay between actions.")
        .defaultValue(100)
        .min(0)
        .sliderMax(1000)
        .build()
    );

    public AutoItemOrder() {
        super(AddonTemplate.CATEGORY, "AutoItemOrder", "Made By FANGS/GEC OWNER");
    }

    @Override
    public void onActivate() {
        stage = Stage.SEND_COMMAND;
        stageStart = System.currentTimeMillis();
    }

    @Override
    public void onDeactivate() {
        stage = Stage.IDLE;
    }

    @EventHandler
    private void onTick(TickEvent.Post event) {
        if (mc.player == null || mc.world == null) return;
        long now = System.currentTimeMillis();
        if (now - stageStart < delay.get()) return;

        switch (stage) {
            case SEND_COMMAND -> {
                mc.player.networkHandler.sendChatCommand("order " + itemName.get());
                stage = Stage.SELECT_ITEM;
                stageStart = now;
            }

            case SELECT_ITEM -> {
                if (mc.currentScreen instanceof GenericContainerScreen screen) {
                    for (Slot slot : screen.getScreenHandler().slots) {
                        if (!slot.getStack().isEmpty()) {
                            mc.interactionManager.clickSlot(screen.getScreenHandler().syncId, slot.id, 0, SlotActionType.PICKUP, mc.player);
                            stage = Stage.DEPOSIT_ITEMS;
                            stageStart = now;
                            break;
                        }
                    }
                }
            }

            case DEPOSIT_ITEMS -> {
                if (mc.currentScreen instanceof GenericContainerScreen screen) {
                    for (int i = 9; i < 36; i++) {
                        ItemStack stack = mc.player.getInventory().getStack(i);
                        if (!stack.isEmpty()) {
                            int invSlot = i < 9 ? i : 36 + i - 9;
                            mc.interactionManager.clickSlot(screen.getScreenHandler().syncId, invSlot, 0, SlotActionType.QUICK_MOVE, mc.player);
                        }
                    }
                    stage = Stage.CLOSE_ORDER;
                    stageStart = now;
                }
            }

            case CLOSE_ORDER -> {
                mc.player.closeHandledScreen();
                stage = Stage.CONFIRM_ORDER;
                stageStart = now;
            }

            case CONFIRM_ORDER -> {
                if (mc.currentScreen instanceof GenericContainerScreen screen) {
                    if (screen.getScreenHandler().slots.size() > 15) {
                        mc.interactionManager.clickSlot(screen.getScreenHandler().syncId, 15, 0, SlotActionType.PICKUP, mc.player);
                    }
                }
                mc.player.closeHandledScreen();
                stage = Stage.SEND_COMMAND;
                stageStart = now;
            }

            case COMPLETE, IDLE -> {}
        }
    }
}
