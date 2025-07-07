package com.example.addon.modules;

import com.example.addon.AddonTemplate;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.settings.*;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.slot.SlotActionType;

public class AutoBoneDropper extends Module {
    private final SettingGroup sgGeneral = settings.getDefaultGroup();

    private final Setting<Integer> dropDelay = sgGeneral.add(new IntSetting.Builder()
        .name("Drop Delay")
        .description("Delay after dropping bones.")
        .defaultValue(2)
        .min(1)
        .sliderMax(20)
        .build()
    );

    private final Setting<Integer> pageDelay = sgGeneral.add(new IntSetting.Builder()
        .name("Page Delay")
        .description("Delay after going to the next page.")
        .defaultValue(10)
        .min(1)
        .sliderMax(40)
        .build()
    );

    private int tickDelay = 0;
    private boolean scanning = false;
    private int emptyPages = 0;
    private boolean selling = false;

    public AutoBoneDropper() {
        super(AddonTemplate.CATEGORY, "AutoBoneDropper", "Made By FANGS/GEC OWNER.");
    }

    @Override
    public void onActivate() {
        tickDelay = 0;
        scanning = true;
        emptyPages = 0;
        selling = false;
    }

    @Override
    public void onDeactivate() {
        scanning = false;
        selling = false;
    }

    @EventHandler
    private void onTick(TickEvent.Pre event) {
        if (!scanning || mc.player == null || mc.currentScreen == null) return;

        ScreenHandler handler = mc.player.currentScreenHandler;
        if (handler == null || handler.slots.size() < 54) return;

        if (tickDelay > 0) {
            tickDelay--;
            return;
        }

        if (selling) {
            mc.interactionManager.clickSlot(handler.syncId, 15, 0, SlotActionType.PICKUP, mc.player);
            info("Sold items. Module disabled.");
            toggle();
            return;
        }

        boolean hasBone = false;
        boolean hasOtherItem = false;

        for (int i = 0; i <= 53; i++) {
            ItemStack stack = handler.getSlot(i).getStack();
            if (!stack.isEmpty()) {
                if (stack.getItem() == Items.BONE) {
                    hasBone = true;
                } else {
                    hasOtherItem = true;
                }
            }
        }

        if (hasBone) {
            emptyPages = 0;

            if (!hasOtherItem) {
                mc.interactionManager.clickSlot(handler.syncId, 50, 0, SlotActionType.PICKUP, mc.player);
                tickDelay = dropDelay.get();
            } else {
                for (int i = 0; i <= 53; i++) {
                    ItemStack stack = handler.getSlot(i).getStack();
                    if (stack.getItem() == Items.BONE) {
                        mc.interactionManager.clickSlot(handler.syncId, i, 1, SlotActionType.THROW, mc.player);
                        tickDelay = dropDelay.get();
                        return;
                    }
                }
            }
        } else {
            emptyPages++;

            if (emptyPages >= 10) {
                info("No bones found. Selling...");
                mc.interactionManager.clickSlot(handler.syncId, 48, 0, SlotActionType.PICKUP, mc.player);
                tickDelay = 5;
                selling = true;
                return;
            }

            mc.interactionManager.clickSlot(handler.syncId, 53, 0, SlotActionType.PICKUP, mc.player);
            tickDelay = pageDelay.get();
        }
    }
}
