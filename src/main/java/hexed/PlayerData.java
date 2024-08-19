package main.java.hexed;

import arc.struct.*;
import arc.math.geom.*;
import arc.util.Timer.Task;
import mindustry.game.Team;
import mindustry.gen.*;
import static mindustry.content.Blocks.air;

import static mindustry.Vars.*;

public class PlayerData {

    public static Seq<PlayerData> players = new Seq<>();
    public static Seq<RequestData> requests = new Seq<>();

    // rewrite to array[256]?
    public static IntSeq teams = new IntSeq();

    public Player player;
    public Team team;
    public boolean leader;
    public Task left;

    public PlayerData(Player player, Team team) {
        this.player = player;
        this.team = team;
        this.leader = true;
    }

    public PlayerData(Player player, Team team, boolean leader) {
        this.player = player;
        this.team = team;
        this.leader = leader;
    }

    public static PlayerData getData(Player player) {
        return players.find(data -> data.player.uuid().equals(player.uuid()));
    }

    public static PlayerData getData(Team team) {
        return players.find(data -> data.player.team().equals(team));
    }

    public static Team getTeam() {
        return Team.get(teams.removeIndex(teams.random()));
    }

    public static void addTeam(Team team) {
        teams.add(team.compareTo(team));
    }

    public static void initTeams() {
        for (int i = 1; i < 256; i++) {
            teams.add(i);
        }
    }

    public static void killPlayer(Player player) {
        player.team(Team.derelict);
        player.clearUnit();
    }

    public static void killTeam(Team team) {
        // destroy buildings
        world.tiles.eachTile(tile -> {
            if (tile.build != null && tile.block() != air && tile.team() == team) {
                tile.removeNet();
            }
        });

        // kill units
        Groups.player.each(player -> {
            if (player.team() == team) {
                killPlayer(player);
            }
        });

        // clear data
        PlayerData.players.removeAll(data -> data.team.equals(team));
    }

    public boolean isActive() {
        return player.con.isConnected() && team != Team.derelict;
    }

    public int getControlledCount() {
        return HexData.hexes.count(hex -> hex.hasCore() && world.tile(hex.x, hex.y).team() == team);
    }

    public static Seq<PlayerData> getLeaderboard() {
        return players.select(data -> data.getControlledCount() > 0 && data.leader).sort(data -> data.getControlledCount());
    }

    public static class RequestData {
        public Player sender;
        public Player recipient;

        public RequestData(Player sender, Player recipient) {
            this.sender = sender;
            this.recipient = recipient;
        }
    }

}