package main.java.hexed;

import arc.util.*;

import main.java.hexed.listeners.ClientCommands;
import main.java.hexed.listeners.PluginEvents;
import main.java.hexed.listeners.ServerCommands;
import main.java.hexed.generation.GenerateTypes;

import mindustry.mod.Plugin;

public class Main extends Plugin {
    @Override
    public void init() {
        GenerateTypes.load();
        PluginEvents.load();
        Vars.load();
    }

    @Override
    public void registerClientCommands(CommandHandler handler) {
        ClientCommands.load(handler);
    }

    @Override
    public void registerServerCommands(CommandHandler handler) {
        ServerCommands.load(handler);
    }

}
