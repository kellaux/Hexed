package main.java.hexed;

import arc.struct.*;
import arc.math.geom.*;
import arc.util.Timer.Task;
import mindustry.game.Team;
import mindustry.gen.*;

import static mindustry.Vars.*;

public class HexData {
	// TODO
	public static Seq<Hex> hexes = new Seq<>();

	public static void initHexes() {
		HexGenerator.getHexesPos((x, y) -> hexes.add(new Hex(hexes.size + 1, x, y)));
	}

	public static Hex getFirstHex() {
		return hexes.select(hex -> !hex.hasCore()).random();
	}

	public static Hex getClosestHex(Position position) {
		return hexes.min(hex -> position.dst(hex.wx, hex.wy));
	}

	public static class PlayerData {

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

	public static class HexCaptureEvent {
		public Hex hex;
		public Player player;

		public HexCaptureEvent(Hex hex, Player player) {
			this.hex = hex;
			this.player = player;
		}
	}

}
