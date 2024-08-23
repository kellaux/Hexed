package main.java.hexed;

import arc.Events;
import arc.files.Fi;
import arc.struct.ObjectMap;
import arc.struct.Seq;
import arc.struct.StringMap;
import arc.util.CommandHandler;
import arc.util.Log;
import arc.util.Time;
import arc.util.Timer;

import main.java.hexed.PlayerData.RequestData;
import mindustry.content.Items;
import mindustry.core.GameState.State;
import mindustry.game.*;
import mindustry.game.EventType.BlockBuildEndEvent;
import mindustry.game.EventType.BlockDestroyEvent;

import mindustry.game.EventType.PlayerJoin;
import mindustry.game.EventType.PlayerLeave;
import mindustry.game.EventType.Trigger;
import mindustry.game.Schematic.Stile;
import mindustry.game.Teams.TeamData;
import mindustry.gen.Call;
import mindustry.gen.Groups;
import mindustry.gen.Player;
import mindustry.maps.Map;
import mindustry.mod.Plugin;
import mindustry.net.WorldReloader;
import mindustry.type.ItemStack;
import mindustry.type.Planet;
import mindustry.ui.Menus;
import mindustry.world.Tile;
import mindustry.world.blocks.storage.CoreBlock;
import mindustry.world.blocks.storage.CoreBlock.CoreBuild;

import static main.java.hexed.GenerateType.maps;
import static mindustry.content.Blocks.*;
import static mindustry.content.Planets.*;
import static mindustry.type.ItemStack.list;
import static mindustry.Vars.*;
import static main.java.hexed.PlayerData.*;
import static main.java.hexed.GenerateType.*;

public class Main extends Plugin {
    public static final Rules rules = new Rules();
    public static final ObjectMap<Planet, PlanetData> planets = new ObjectMap<>();

    public static final float leftTeamDestroyTime = 90f;
    public static final float roundTime = 60 * 60 * 90f;
    public static final int itemRequirement = 100;
    public static final int winCaptureCount = 41;


    public static float counter = roundTime;
    public static boolean restarting;

    public static GenerateType type;
    public static Fi baseFile;

    @Override
    public void init() {
        GenerateTypes.load();
        //set default rules
        rules.enemyCoreBuildRadius = hexRadius * tilesize;
        rules.canGameOver = false;
        rules.coreCapture = true;
        rules.buildSpeedMultiplier = 2f;
        rules.blockHealthMultiplier = 1.5f;
        rules.unitBuildSpeedMultiplier = 1.75f;

        // uses for tests
        rules.infiniteResources = true;

        planets.put(serpulo, new PlanetData(
                Seq.with(oreCopper, oreLead, oreScrap, oreCoal, oreTitanium, oreThorium),
                Schematics.readBase64("bXNjaAB4nE2SgY7CIAyGC2yDsXkXH2Tvcq+AkzMmc1tQz/j210JpXDL8hu3/lxYY4FtBs4ZbBLvG1ync4wGO87bvMU2vsCzTEtIlwvCxBW7e1r/43hKYkGY4nFN4XqbfMD+29IbhvmHOtIc1LjCmuIcrfm3X9QH2PofHIyYY5y3FaX3OS3ze4fiRwX7dLa5nDHTPddkCkT3l1DcA/OALihZNq4H6NHnV+HZCVshJXA9VYZC9kfVU+VQGKSsbjVT1lOgp1qO4rGIo9yvnquxH1ORIohap6HVIDbtpaNlDi4cWD80eFJdrNhbJc8W61Jzdqi/3wrRIRii7GYdelvWMZDQs1kNbqtYe9/KuGvDX5zD6d5SML66+5dwRqXgQee5GK3Edxw1ITfb3SJ71OomzUAdjuWsWqZyJavd8Issdb5BqVbaoGCVzJqrddaUGTWSFHPs67m6H5HlaTqbqpFc91Kfn+2eQSp9pr96/Xtx6cevZjeKKDuUOklvvXy9uPGdNZFjZi7IXZS/n8Hyf/wFbjj/q"),
                list(Items.copper, 350, Items.lead, 250, Items.graphite, 150, Items.metaglass, 150, Items.silicon, 250, Items.titanium, 50)
        ));

        // I want to put it in a file but I cant :(

        Events.run(Trigger.update, () -> {
            if (!state.isPlaying()) return;

            players.each(data -> {
                updateText(data.player);
                if (data.getControlledCount() >= winCaptureCount) endGame();
            });

            counter -= Time.delta;
            if (counter <= 0) endGame();
        });

        Events.on(PlayerJoin.class, event -> {
            PlayerData data = getData(event.player);
            if (data == null) {
                loadout(event.player);
            } else {
                if (data.leader) data.left.cancel();
                event.player.team(data.team);
                setCamera(event.player);
            }
        });

        Events.on(PlayerLeave.class, event -> {
            PlayerData data = getData(event.player);
            if (data != null && data.leader) {
                data.left = Timer.schedule(() -> {
                    killTeam(data.team);
                    removeTeamData(data.team);
                }, leftTeamDestroyTime);
            }
        });

        Events.on(BlockBuildEndEvent.class, event -> {
            Hex hex = HexData.getClosestHex(event.tile);
            if (!hex.hasCore() && hex.isCaptureProgress(event.tile)) {
                world.tile(hex.x, hex.y).setNet(type.planet.defaultCore, event.team, 0);
            }
        });

        // TODO
        Events.on(BlockDestroyEvent.class, event -> {
            if (event.tile.block() instanceof CoreBlock) {
                TeamData data = state.teams.get(event.tile.team());
                if (data.noCores()) {
                    killTeam(data.team);
                }
            }
        });

    }

    // TODO
    public void loadout(Player player) {
        Hex hex = HexData.getFirstHex();
        if (hex == null) {
            player.sendMessage("There are no hexes available");
        } else {
            PlanetData data = planets.get(type.planet);
            Seq<ItemStack> loadout = data.items;
            Schematic schematic = data.schematic;

            player.team(getTeam());
            players.add(new PlayerData(player, player.team()));

            Stile coreTile = schematic.tiles.find(stile -> stile.block instanceof CoreBlock);
            schematic.tiles.each(stile -> {
                if (stile == null) return;

                int ox = hex.x - coreTile.x;
                int oy = hex.y - coreTile.y;

                Tile tile = world.tile(stile.x + ox, stile.y + oy);
                tile.setNet(stile.block, player.team(), stile.rotation);
                tile.build.configureAny(stile.config);

                if (stile == coreTile)
                    loadout.each(stack -> Call.setItem(tile.build, stack.item, stack.amount));
            });

            Call.setCameraPosition(player.con, hex.wx, hex.wy);

        }
    }

    public void startGame(GenerateType next) {
        type = next;

        HexData.initHexes();
        PlayerData.initTeams();

        WorldReloader reloader = new WorldReloader();
        reloader.begin();

        HexGenerator generator = new HexGenerator();
        world.loadGenerator(width, height, generator);
        state.rules = type.applyRules(rules.copy());
        logic.play();

        reloader.end();
    }

    public void endGame() {
        if (restarting) return;
        restarting = true;

        Time.runTask(60f * 15f, this::reload);

        Seq<PlayerData> data = PlayerData.getLeaderboard();
        Call.infoMessage(data.first().player.coloredName() + " won round\n\n " + getLeaderboard());

    }

    public void reload() {
        // clear data's
        HexData.hexes.clear();
        players.clear();
        teams.clear();
        requests.clear();

        Seq<Player> players = Groups.player.copy(new Seq<>());
        counter = roundTime;
        startGame(type.nextMap());
        players.each(this::loadout);
    }


    public void updateText(Player player) {
        Hex hex = HexData.getClosestHex(player);
        if (hex == null) return;

        Team team = hex.getController();
        float progress = hex.getProgressPercent(player.team());
        StringBuilder message = new StringBuilder("[white]Hex # " + hex.id + "\n");

        if (team == null) {
            if (progress > 0) {
                message.append("[lightgray]Capture progress: [accent]").append((int) (progress)).append("%");
            } else {
                message.append("[lightgray][[Empty]");
            }
        } else if (team == player.team()) {
            message.append("[yellow][[Captured]");
        } else {
            PlayerData data = players.find(d -> d.team == player.team() && d.leader);
            message.append("[lightgray]Captured by ").append(data.player.name());
        }

        Call.setHudText(player.con, message.toString());
    }

    public String getLeaderboard() {
        StringBuilder builder = new StringBuilder();

        Seq<PlayerData> leaderboard = PlayerData.getLeaderboard();
        leaderboard.truncate(5);
        leaderboard.each(data -> builder.append(data.player.coloredName()).append("[orange] ").append(data.getControlledCount()).append(" hexes\n[white]"));

        return builder.toString();
    }

    public void setCamera(Player player) {
        CoreBuild core = state.teams.get(player.team()).core();
        Call.setCameraPosition(player.con, core.tileX(), core.tileY());
    }

    // TODO
    @Override
    public void registerServerCommands(CommandHandler handler) {
        handler.register("hexed", "Begin hosting with the Hexed game mode.", args -> {
            if (!state.is(State.menu)) {
                Log.err("Stop the server first.");
                return;
            }
            startGame(type = maps.random());
            netServer.openServer();
            state.map = new Map(StringMap.of("name", type.name));
        });
    }

    public void registerClientCommands(CommandHandler handler) {
        handler.<Player>register("spectate", "Enter spectator mode. This destroys your base.", (args, player) -> {
            PlayerData data = getData(player);
            if (data == null) {
                loadout(player);
            } else {
                if (data.leader) {
                    killTeam(data.team);
                    removeTeamData(data.team);
                } else {
                    killPlayer(data.player);
                    players.remove(data);
                }

            }
        });

        handler.<Player>register("lb", "Display the leaderboard", (args, player) -> player.sendMessage(getLeaderboard()));

        handler.<Player>register("join", "Join the player.", (args, sender) -> {
            Seq<PlayerData> data = players.select(d -> d.leader && d.isActive() && d.team != sender.team());

            if (data.isEmpty()) {
                sender.sendMessage("There are no available players");
            } else {
                Menus.MenuListener listener = (sender1, option) -> {
                    Player recipient = data.get(option).player;

                    if (requests.contains(d -> d.sender == sender1 && d.recipient == recipient)) {
                        sender1.sendMessage("you already send request");

                    } else {
                        requests.add(new RequestData(sender1, recipient));
                        recipient.sendMessage(sender1.coloredName() + " [white]A player has sent you an invitation. To accept the player into your team, type /accept.");
                    }
                };

                String[][] options = new String[data.size][1];
                data.sort().each(d -> options[data.indexOf(d)][0] = d.player.coloredName());

                Call.menu(sender.con, Menus.registerMenu(listener), "/JOIN", "PLAYERS", options);

            }

        });

        handler.<Player>register("accept", "Accept the invitation", (args, recipient) -> {
            Seq<RequestData> data = requests.select(d -> d.recipient == recipient);

            if (data.isEmpty()) {
                recipient.sendMessage("You have not yet received a request");
            } else {
                Menus.MenuListener listener = (player, option) -> {
                    Player sender = Groups.player.find(p -> p == data.get(option).sender);
                    PlayerData senderData = getData(sender);
                    Team playerTeam = player.team();
                    if (senderData == null) {
                        players.add(new PlayerData(sender, playerTeam, false));
                    } else {
                        if (senderData.leader) {
                            killTeam(sender.team());
                            senderData.leader = false;
                        } else {
                            killPlayer(sender);
                        }
                        senderData.team = playerTeam;
                    }

                    sender.team(playerTeam);
                    requests.remove(data.get(option));
                    setCamera(player);
                };

                String[][] options = new String[data.size][1];
                data.sort().each(d -> options[data.indexOf(d)][0] = d.sender.coloredName());

                Call.menu(recipient.con, Menus.registerMenu(listener), "/ACCEPT", "PLAYERS", options);
            }
        });
    }

}
