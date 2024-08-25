package main.java.hexed.listeners;

import arc.struct.StringMap;
import arc.util.CommandHandler;
import arc.util.Log;
import mindustry.core.GameState;
import mindustry.maps.Map;

import static main.java.hexed.Utils.*;
import static main.java.hexed.generation.GenerateType.maps;
import static mindustry.Vars.netServer;
import static mindustry.Vars.state;


public class ServerCommands {
    public static void load(CommandHandler handler) {
        handler.register("hexed", "Begin hosting with the Hexed game mode.", args -> {
            if (!state.is(GameState.State.menu)) {
                Log.err("Stop the server first.");
                return;
            }

            startGame(type = maps.random());
            netServer.openServer();
            state.map = new Map(StringMap.of("name", type.name));
        });
    }
}
