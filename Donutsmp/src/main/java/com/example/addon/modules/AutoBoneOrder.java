package com.example.addon.modules;

import com.example.addon.AddonTemplate;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.settings.*;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.client.gui.screen.ingame.GenericContainerScreen;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.slot.Slot;
import net.minecraft.screen.slot.SlotActionType;

public class AutoBoneOrder extends Module {
    private enum Stage {
        START,
        WAIT_FOR_GUI,
        SELECT_BONE_ORDER,
        PUT_BONES,
        CLOSE_BONE_GUI,
        PRESS_CONFIRM,
        DONE
    }

    private Stage stage = Stage.START;
    private long stageStart = 0;

    private final SettingGroup sgGeneral = settings.getDefaultGroup();

    private final Setting<Integer> delayMs = sgGeneral.add(new IntSetting.Builder()
        .name("Delay (ms)")
        .description("Delay between each stage.")
        .defaultValue(150)
        .min(0)
        .sliderMax(1000)
        .build()
    );

    public AutoBoneOrder() {
        super(AddonTemplate.CATEGORY, "AutoBoneOrder", "Made By FANGS/GEC OWNER");
    }

    @Override
    public void onActivate() {
        stage = Stage.START;
        stageStart = System.currentTimeMillis();
    }

    @Override
    public void onDeactivate() {
        stage = Stage.DONE;
    }

    @EventHandler
    private void onTick(TickEvent.Pre event) {
        if (mc.player == null || mc.world == null) return;

        long now = System.currentTimeMillis();
        if (now - stageStart < delayMs.get()) return;

        switch (stage) {
            case START -> {
                mc.player.networkHandler.sendChatCommand("order bone");
                stage = Stage.WAIT_FOR_GUI;
                stageStart = now;
            }

            case WAIT_FOR_GUI -> {
                if (mc.currentScreen instanceof GenericContainerScreen) {
                    stage = Stage.SELECT_BONE_ORDER;
                    stageStart = now;
                }
            }

            case SELECT_BONE_ORDER -> {
                if (!(mc.currentScreen instanceof GenericContainerScreen screen)) return;
                ScreenHandler handler = screen.getScreenHandler();
                for (Slot slot : handler.slots) {
                    ItemStack stack = slot.getStack();
                    if (!stack.isEmpty() && stack.getItem() == Items.BONE) {
                        mc.interactionManager.clickSlot(handler.syncId, slot.id, 0, SlotActionType.PICKUP, mc.player);
                        stage = Stage.PUT_BONES;
                        stageStart = now;
                        return;
                    }
                }
            }

            case PUT_BONES -> {
                if (!(mc.currentScreen instanceof GenericContainerScreen screen)) return;
                ScreenHandler handler = screen.getScreenHandler();

                boolean movedAny = false;
                for (int i = 0; i < mc.player.getInventory().size(); i++) {
                    ItemStack stack = mc.player.getInventory().getStack(i);
                    if (!stack.isEmpty() && stack.getItem() == Items.BONE) {
                        int invSlot = i < 9 ? i : 36 + i - 9;
                        for (Slot slot : handler.slots) {
                            if (slot.inventory != mc.player.getInventory() && slot.getStack().isEmpty()) {
                                mc.interactionManager.clickSlot(handler.syncId, invSlot, 0, SlotActionType.QUICK_MOVE, mc.player);
                                movedAny = true;
                                break;
                            }
                        }
                    }
                }

                if (movedAny) {
                    stage = Stage.CLOSE_BONE_GUI;
                    stageStart = now;
                }
            }

            case CLOSE_BONE_GUI -> {
                mc.player.closeHandledScreen();
                stage = Stage.PRESS_CONFIRM;
                stageStart = now;
            }

            case PRESS_CONFIRM -> {
                if (mc.currentScreen instanceof GenericContainerScreen screen) {
                    ScreenHandler handler = screen.getScreenHandler();
                    if (handler.slots.size() > 15) {
                        mc.interactionManager.clickSlot(handler.syncId, 15, 0, SlotActionType.PICKUP, mc.player);
                    }
                }
                mc.player.closeHandledScreen();
                stage = Stage.START;
                stageStart = now;
            }

            case DONE -> {
            }
        }
    }
}
