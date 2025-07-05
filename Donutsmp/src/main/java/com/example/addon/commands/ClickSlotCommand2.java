package com.example.addon.commands;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import meteordevelopment.meteorclient.MeteorClient;
import meteordevelopment.meteorclient.commands.Command;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.command.CommandSource;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.network.packet.c2s.play.ClickSlotC2SPacket;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.slot.SlotActionType;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;

import static com.mojang.brigadier.Command.SINGLE_SUCCESS;

public class ClickSlotCommand2 extends Command {
    public ClickSlotCommand2() {
        super("clickslot2", "Simulate a click on a slot in the currently opened GUI.");
    }

    @Override
    public void build(LiteralArgumentBuilder<CommandSource> builder) {
        builder.then(literal("click")
            .then(argument("clickType", StringArgumentType.string())
                .then(argument("shift", StringArgumentType.string())
                    .then(argument("slot", IntegerArgumentType.integer(0))
                        .executes(context -> {
                            String clickType = context.getArgument("clickType", String.class);
                            boolean shiftClick = context.getArgument("shift", String.class).equalsIgnoreCase("yes");
                            int slot = context.getArgument("slot", Integer.class);

                            executeClick(clickType, shiftClick, slot);
                            return SINGLE_SUCCESS;
                        })
                    )
                )
            )
        );
    }

    private void executeClick(String clickType, boolean shiftClick, int slot) {
        MinecraftClient mc = MeteorClient.mc;
        ClientPlayerEntity player = mc.player;

        if (player == null || mc.player.currentScreenHandler == null) {
            MeteorClient.LOG.info("No open screen handler or player is null.");
            return; // Ensure the player is valid and there is an open GUI
        }

        // Get the current screen handler (GUI handler)
        ScreenHandler handler = mc.player.currentScreenHandler;

        if (handler == null) {
            MeteorClient.LOG.info("No screen handler found.");
            return; // Ensure there is a valid handler (i.e., we are in a valid GUI)
        }

        // Debug output
        MeteorClient.LOG.info("Attempting to click slot: " + slot + " with action: " + clickType);

        // Determine the action based on the click type
        SlotActionType actionType = SlotActionType.PICKUP;  // Default action type

        if (clickType.equalsIgnoreCase("rightclick")) {
            actionType = SlotActionType.PICKUP_ALL; // Right-click action
        } else if (clickType.equalsIgnoreCase("middleclick")) {
            actionType = SlotActionType.QUICK_CRAFT; // Middle-click action
        } else if (clickType.equalsIgnoreCase("leftclick")) {
            actionType = SlotActionType.PICKUP; // Left-click action
        } else {
            MeteorClient.LOG.error("Invalid click type provided: " + clickType);
            return;  // Handle invalid click type input
        }

        // Prepare the item stack for the cursor (normally it's empty, but you can modify this if needed)
        ItemStack cursorStack = handler.getCursorStack().copy();

        // Log the state of the cursor stack (for debugging)
        MeteorClient.LOG.info("Cursor Stack: " + cursorStack);

        // Send the packet to simulate the click on the specified slot
        try {
            // Create an empty map for the Int2ObjectMap<ItemStack>
            Int2ObjectOpenHashMap<ItemStack> itemStackMap = new Int2ObjectOpenHashMap<>();

            mc.getNetworkHandler().sendPacket(new ClickSlotC2SPacket(
                handler.syncId,               // syncId of the GUI
                handler.getRevision(),        // revision of the screen handler
                slot,                         // the slot number to click
                shiftClick ? 1 : 0,           // Shift-click (1 for true, 0 for false)
                actionType,                   // The action type (left, right, middle click)
                cursorStack,                  // The cursor item stack
                itemStackMap                  // Provide the empty map
            ));
            MeteorClient.LOG.info("Packet sent successfully.");
        } catch (Exception e) {
            MeteorClient.LOG.error("Error sending packet: ", e);
        }
    }
}
