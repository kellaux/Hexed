package main.java.hexed;

import arc.struct.Seq;
import arc.util.Time;
import main.java.hexed.data.HexData;
import main.java.hexed.data.PlanetData;
import main.java.hexed.data.PlayerData;
import main.java.hexed.generation.GenerateType;
import main.java.hexed.generation.HexedGenerator;
import mindustry.game.Schematic;
import mindustry.game.Team;
import mindustry.gen.Call;
import mindustry.gen.Groups;
import mindustry.gen.Player;
import mindustry.net.WorldReloader;
import mindustry.type.ItemStack;
import mindustry.world.Tile;
import mindustry.world.blocks.storage.CoreBlock;

import static main.java.hexed.Vars.*;
import static main.java.hexed.data.PlayerData.getTeam;
import static main.java.hexed.generation.GenerateType.height;
import static main.java.hexed.generation.GenerateType.width;
import static main.java.hexed.data.PlayerData.*;
import static main.java.hexed.Utils.*;
import static mindustry.Vars.*;
import static mindustry.Vars.state;

public class Utils {
    public static boolean restarting;
    public static float counter = roundTime;
    public static GenerateType type;

    public static void loadout(Player player) {
        Hex hex = HexData.getFirstHex();
        if (hex == null) {
            player.sendMessage("There are no hexes available");
        } else {
            player.team(getTeam());
            players.add(new PlayerData(player, player.team()));

            PlanetData data = planets.get(type.planet);
            Seq<ItemStack> loadout = data.items;
            Schematic schematic = data.schematic;
            Schematic.Stile coreTile = schematic.tiles.find(stile -> stile.block instanceof CoreBlock);
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

//            PlanetData planet = planets.get(type.planet);
//            Seq<PlanetData.Loadout> blocks = planet.blocks;

//            player.team(getTeam());
//            players.add(new PlayerData(player, player.team()));

//            for(int i = 0; i < 600; i++) {
//                int firstAngleFactor = (int) (Math.floor(Math.random() * 3) * 2);
//                int secondAngleFactor = firstAngleFactor + 2;
//
//                double x1 = Math.cos(Math.PI * secondAngleFactor / 3) * side;
//                double y1 = Math.sin(Math.PI * secondAngleFactor / 3) * side;
//                double x2 = Math.cos(Math.PI * firstAngleFactor / 3) * side;
//                double y2 = Math.sin(Math.PI * firstAngleFactor / 3) * side;
//
//                double r1 = getRandomValue(new double[]{0, 0.6, 0.8, 1}, new double[]{0, 0.2, 0.5, 1});
//                double r2 = getRandomValue(new double[]{0, 0.6, 0.8, 1}, new double[]{0, 0.2, 0.5, 1});
//                double Lx = x1 * r1 + x2 * r2 + hex.x;
//                double Ly = y1 * r1 + y2 * r2 + hex.y;
//
//                Tile tile = world.tile((int) Lx, (int) Ly);
//                if(tile.block() == air)
//                    tile.setNet(arc);
//            }

//            planet.blocks.each(block -> {
//                HexedGenerator.randomPointInHexagon(block, player.team(), hex, 10);
//            });
//
//            world.tile(hex.x, hex.y).setNet(coreNucleus, player.team(), 0);

            Call.setCameraPosition(player.con, hex.wx, hex.wy);

        }
    }

    static double getRandomValue(double[] xArray, double[] yArray) {
        // xArray[0] = 0, xArray[xArray.length - 1] = 1, xArray[n] пренадлежит [0; 1]
        double result = 0;
        double value = Math.random();
        for (int i = 1; i < xArray.length; i++) {
            double cy = yArray[i] + 1;
            double py = yArray[i - 1] + 1;
            if (value <= xArray[i] && value >= xArray[i - 1]) {
                result = (value - xArray[i - 1]) / (py + (cy - py) / (xArray[i] - xArray[i - 1]) * (value - xArray[i - 1]));
            }
        }
        return result;
    }

    public static void startGame(GenerateType next) {
        type = next;

        HexData.initHexes();
        PlayerData.initTeams();

        WorldReloader reload = new WorldReloader();
        reload.begin();

        HexedGenerator generator = new HexedGenerator();
        world.loadGenerator(width, height, generator);
        state.rules = type.applyRules(rules.copy());
        logic.play();

        reload.end();
    }

    static void endGame() {
        if (restarting) return;
        restarting = true;

        Time.runTask(60f * 15f, Utils::reload);

        Seq<PlayerData> data = PlayerData.getLeaderboard();
        Call.infoMessage(data.first().player.coloredName() + " won round\n\n " + getLeaderboard());

    }

    static void reload() {
        // clear data's
        HexData.hexes.clear();
        players.clear();
        teams.clear();
        requests.clear();

        Seq<Player> players = Groups.player.copy(new Seq<>());
        counter = roundTime;
        startGame(type.nextMap());
        players.each(Utils::loadout);
    }

    public static void updateText(Player player) {
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


    public static String getLeaderboard() {
        StringBuilder builder = new StringBuilder();

        Seq<PlayerData> leaderboard = PlayerData.getLeaderboard();
        leaderboard.truncate(5);
        leaderboard.each(data -> builder.append(data.player.coloredName()).append("[orange] ").append(data.getControlledCount()).append(" hexes\n[white]"));

        return builder.toString();
    }

    //TODO
    public static void setCamera(Player player) {
        CoreBlock.CoreBuild core = state.teams.get(player.team()).core();
        Call.setCameraPosition(player.con, core.tileX(), core.tileY());
    }
}
