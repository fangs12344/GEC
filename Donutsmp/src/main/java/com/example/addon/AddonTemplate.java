package com.example.addon;

import com.example.addon.commands.*;
import com.example.addon.hud.HudExample;
import com.example.addon.modules.*;
import com.mojang.logging.LogUtils;
import meteordevelopment.meteorclient.addons.GithubRepo;
import meteordevelopment.meteorclient.addons.MeteorAddon;
import meteordevelopment.meteorclient.commands.Commands;
import meteordevelopment.meteorclient.systems.hud.Hud;
import meteordevelopment.meteorclient.systems.hud.HudGroup;
import meteordevelopment.meteorclient.systems.modules.Category;
import meteordevelopment.meteorclient.systems.modules.Modules;
import meteordevelopment.meteorclient.systems.modules.movement.Anchor;
import net.minecraft.client.MinecraftClient;
import org.slf4j.Logger;

public class AddonTemplate extends MeteorAddon {
    public static final MinecraftClient MC = MinecraftClient.getInstance();
    public static final Logger LOG = LogUtils.getLogger();
        public static final Category CATEGORY = new Category("GEC");
    public static final HudGroup HUD_GROUP = new HudGroup("Example");

    @Override
    public void onInitialize() {
        LOG.info("Initializing Meteor Addon Template");

        // Modules
        Modules.get().add(new SpawnerProtect());
        Modules.get().add(new AhSniper());
        Modules.get().add(new TunnelBaseFinder());
        Modules.get().add(new AutoBoneDropper());
        Modules.get().add(new AutoShulkerOrder());
     // Commands
        Commands.add(new CommandExample());
        Commands.add(new ClickSlotCommand2());
        Commands.add(new KickCommand());
        Commands.add(new WaitCommand());


        // HUD
        Hud.get().register(HudExample.INFO);
    }

    @Override
    public void onRegisterCategories() {
        Modules.registerCategory(CATEGORY);
    }

    @Override
    public String getPackage() {
        return "com.example.addon";
    }

    @Override
    public GithubRepo getRepo() {
        return new GithubRepo("MeteorDevelopment", "meteor-addon-template");
    }
}
