package main.java.hexed.listeners;

import arc.util.Time;
import arc.util.Timer;
import main.java.hexed.Hex;
import main.java.hexed.data.HexData;
import main.java.hexed.data.PlayerData;
import mindustry.game.EventType;
import mindustry.game.Teams;
import mindustry.world.blocks.storage.CoreBlock;

import static main.java.hexed.Vars.*;
import static main.java.hexed.data.PlayerData.*;
import static main.java.hexed.Utils.*;
import static mindustry.Vars.state;
import static mindustry.Vars.world;

public class PluginEvents {
    public static void load() {
        arc.Events.run(EventType.Trigger.update, () -> {
            if (!state.isPlaying()) return;

            players.each(data -> {
                updateText(data.player);
                if (data.getControlledCount() >= winCaptureCount) endGame();
            });

            counter -= Time.delta;
            if (counter <= 0) endGame();
        });

        arc.Events.on(EventType.PlayerJoin.class, event -> {
            PlayerData data = getData(event.player);
            if (data == null) {
                loadout(event.player);
            } else {
                if (data.leader) data.left.cancel();
                event.player.team(data.team);
                setCamera(event.player);
            }
        });

        arc.Events.on(EventType.PlayerLeave.class, event -> {
            PlayerData data = getData(event.player);
            if (data != null && data.leader) {
                data.left = Timer.schedule(() -> {
                    killTeam(data.team);
                    removeTeamData(data.team);
                }, leftTeamDestroyTime);
            }
        });

        arc.Events.on(EventType.BlockBuildEndEvent.class, event -> {
            Hex hex = HexData.getClosestHex(event.tile);
            if (!hex.hasCore() && hex.isCaptureProgress(event.team)) {
                world.tile(hex.x, hex.y).setNet(type.planet.defaultCore, event.team, 0);
            }
        });

        arc.Events.on(EventType.BlockDestroyEvent.class, event -> {
            if (event.tile.block() instanceof CoreBlock) {
                Teams.TeamData data = state.teams.get(event.tile.team());
                if (data.noCores()) {
                    killTeam(data.team);
                }
            }
        });
    }
}
