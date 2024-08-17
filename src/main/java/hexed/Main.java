
package main.java.hexed;

import static arc.Core.app;
import static mindustry.Vars.logic;
import static mindustry.Vars.netServer;
import static mindustry.Vars.state;
import static mindustry.Vars.tilesize;
import static mindustry.Vars.world;
import static mindustry.content.Blocks.air;

import javax.swing.event.MenuEvent;
import javax.swing.event.MenuListener;

import arc.Events;
import arc.files.Fi;
import arc.struct.Seq;
import arc.util.CommandHandler;
import arc.util.Log;
import arc.util.Time;
import arc.util.Timer;
import main.java.hexed.HexData.PlayerData;
import main.java.hexed.HexData.PlayerData.RequestData;
import mindustry.content.Blocks;
import mindustry.core.GameState.State;
import mindustry.game.EventType.BlockBuildEndEvent;
import mindustry.game.EventType.BlockDestroyEvent;
import mindustry.game.EventType.MenuOptionChooseEvent;
import mindustry.game.EventType.PlayerJoin;
import mindustry.game.EventType.PlayerLeave;
import mindustry.game.EventType.Trigger;
import mindustry.game.Rules;
import mindustry.game.Schematic;
import mindustry.game.Schematic.Stile;
import mindustry.game.Schematics;
import mindustry.game.Team;
import mindustry.gen.Call;
import mindustry.gen.Groups;
import mindustry.gen.Player;
import mindustry.mod.Plugin;
import mindustry.net.WorldReloader;
import mindustry.type.ItemStack;
import mindustry.ui.Menus;
import mindustry.world.Build;
import mindustry.world.Tile;
import mindustry.world.blocks.storage.CoreBlock;

public class Main extends Plugin {

	public static final float leftTeamDestroyTime = 90f;
	public static final float roundTime = 60 * 60 * 90f;
	public static final int itemRequirment = 100;
	public static final int winCaptureCount = 41;
	public static final Rules rules = new Rules();

	public static float counter = roundTime;
	public Schematic baseSchematic;
	public Fi baseFile;

	@Override
	public void init() {
		rules.enemyCoreBuildRadius = HexGenerator.hexRadius * tilesize;
		rules.canGameOver = false;
		rules.coreCapture = true;
		rules.buildSpeedMultiplier = 2f;
		rules.blockHealthMultiplier = 1.5f;
		rules.unitBuildSpeedMultiplier = 1.75f;
		rules.infiniteResources = true;

		// i want to put it in a file but i cant :(
		baseSchematic = Schematics.readBase64(
				"bXNjaAB4nE2SgY7CIAyGC2yDsXkXH2Tvcq+AkzMmc1tQz/j210JpXDL8hu3/lxYY4FtBs4ZbBLvG1ync4wGO87bvMU2vsCzTEtIlwvCxBW7e1r/43hKYkGY4nFN4XqbfMD+29IbhvmHOtIc1LjCmuIcrfm3X9QH2PofHIyYY5y3FaX3OS3ze4fiRwX7dLa5nDHTPddkCkT3l1DcA/OALihZNq4H6NHnV+HZCVshJXA9VYZC9kfVU+VQGKSsbjVT1lOgp1qO4rGIo9yvnquxH1ORIohap6HVIDbtpaNlDi4cWD80eFJdrNhbJc8W61Jzdqi/3wrRIRii7GYdelvWMZDQs1kNbqtYe9/KuGvDX5zD6d5SML66+5dwRqXgQee5GK3Edxw1ITfb3SJ71OomzUAdjuWsWqZyJavd8Issdb5BqVbaoGCVzJqrddaUGTWSFHPs67m6H5HlaTqbqpFc91Kfn+2eQSp9pr96/Xtx6cevZjeKKDuUOklvvXy9uPGdNZFjZi7IXZS/n8Hyf/wFbjj/q");

		Events.run(Trigger.update, () -> {
			if (!state.isPlaying())
				return;

			PlayerData.players.each(data -> {
				updateText(data.player);
				if (data.getControlledCount() >= winCaptureCount) {
					Groups.player.each(player -> {
						player.sendMessage(data.player.coloredName() + " win this round");
					});
					endGame();
				}
			});

			counter -= Time.delta;
			if (counter <= 0)
				endGame();
		});

		Events.on(PlayerJoin.class, event -> {
			PlayerData data = PlayerData.getData(event.player);
			if (data == null) {

				loadout(event.player);
			} else {
				data.left.cancel();
				event.player.team(data.team);
			}
		});

		Events.on(PlayerLeave.class, event -> {
			PlayerData data = PlayerData.getData(event.player);
			if (data != null) {
				data.left = Timer.schedule(() -> killTeam(data.team), leftTeamDestroyTime);
			}
		});

		Events.on(BlockBuildEndEvent.class, event -> {
			Hex hex = HexData.getClosestHex(event.tile);
			if (!hex.hasCore() && hex.isCaptured(event.tile)) {
				world.tile(hex.x, hex.y).setNet(Blocks.coreShard, event.team, 0);
			}
		});

		// TODO
		Events.on(BlockDestroyEvent.class, event -> {
			if (event.tile.block() instanceof CoreBlock) {
				Team team = event.tile.team();
				if (team.cores().size == 0) {
					killTeam(team);
				}
			}
		});

	}

	// TODO
	public void loadout(Player player) {
		Hex hex = HexData.getFirstHex();
		if (hex == null) {

		} else {

			player.team(PlayerData.getTeam());
			PlayerData.players.add(new PlayerData(player, player.team()));

			Stile coreTile = baseSchematic.tiles.find(stile -> stile.block instanceof CoreBlock);
			baseSchematic.tiles.each(stile -> {
				if (stile == null)
					return;
				int ox = hex.x - coreTile.x;
				int oy = hex.y - coreTile.y;
				Tile tile = world.tile(stile.x + ox, stile.y + oy);
				tile.setNet(stile.block, player.team(), stile.rotation);
				tile.build.configureAny(stile.config);
				if (stile == coreTile) {
					for (ItemStack stack : state.rules.loadout) {
						Call.setItem(tile.build, stack.item, stack.amount);
					}
				}
			});
			Call.setCameraPosition(player.con, hex.wx, hex.wy);

		}
	}

	public void killTeam(Team team) {
		// destroy buildings
		world.tiles.eachTile(tile -> {
			if (tile.build != null && tile.block() != air && tile.team() == team) {
				tile.removeNet();
			}
		});

		// kill units
		Groups.player.each(player -> {
			if (player.team() == team) {
				player.team(Team.derelict);
				player.clearUnit();
			}
		});

		// clear datas
		PlayerData.players.each(data -> {
			if (data.team.equals(team)) {
				data.player.clearUnit();
				data.player.team(Team.derelict);
				PlayerData.players.remove(data);
			}
		});
	}

	public void startGame() {
		WorldReloader reloader = new WorldReloader();
		reloader.begin();

		HexData.initHexes();
		PlayerData.initTeams();

		HexGenerator generator = new HexGenerator();
		world.loadGenerator(HexGenerator.width, HexGenerator.height, generator);

		state.rules = rules.copy();
		logic.play();

		reloader.end();
	}

	public void endGame() {
		Events.fire("Gameover");
		Seq<Player> players = Groups.player.copy(new Seq<>());

		// clear datas
		HexData.hexes.clear();
		PlayerData.players.clear();
		PlayerData.teams.clear();
		PlayerData.requests.clear();

		// reload map
		startGame();
		counter = roundTime;

		// reload players
		players.each(player -> {
			loadout(player);
		});
	}

	public void updateText(Player player) {
		Hex hex = HexData.getClosestHex(player);
		if (hex == null)
			return;

		Team team = hex.getController();
		float progress = hex.getProgressPrecent(player.team());
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
			PlayerData data = PlayerData.players.find(d -> d.team == player.team() && d.leader);
			message.append("Captured by ").append(data.player.name());
		}

		Call.setHudText(player.con, message.toString());
	}

	public String getLeaderboard() {
		Seq<PlayerData> leaderboard = PlayerData.getLeaderboard();
		StringBuilder builder = new StringBuilder();
		for (int i = 0; i < leaderboard.size; i++) {
			if (i > 4) {
				break;
			}
			PlayerData data = leaderboard.get(i);
			builder.append(data.player.name).append("[orange] (").append(data.getControlledCount())
					.append(" hexes)\n[white]");
		}
		return builder.toString();
	}

	// TODO
	@Override
	public void registerServerCommands(CommandHandler handler) {
		handler.register("hexed", "Begin hosting with the Hexed gamemode.", args -> {
			if (!state.is(State.menu)) {
				Log.err("Stop the server first.");
				return;
			}

			startGame();
			netServer.openServer();
		});
	}

	public void registerClientCommands(CommandHandler handler) {
		handler.<Player>register("spectate", "Enter spectator mode. This destroys your base.", (args, player) -> {
			PlayerData data = PlayerData.getData(player);
			if (data.leader) {
				killTeam(player.team());
			} else if (!data.leader) {

			} else {
				loadout(player);
			}
		});

		handler.<Player>register("lb", "Display the leaderboard", (args, player) -> {
			player.sendMessage(getLeaderboard());
		});

		// TODO
		handler.<Player>register("join", "Join the player.", (args, sender) -> {
			Seq<PlayerData> data = PlayerData.players.select(d -> d.leader &&
					d.isActive() && d.team != sender.team());
			if (data.isEmpty()) {

			} else {
				String[][] options = new String[data.size][1];
				Menus.MenuListener listener = new Menus.MenuListener() {
					@Override
					public void get(Player sender, int option) {
						Player recipient = data.get(option).player;
						PlayerData.requests.add(new RequestData(sender, recipient));
						recipient.sendMessage(
								sender.coloredName()
										+ " [white]has sent you aninvitation. Type /accept to accpet it into team");

					}
				};

				for (int i = 0; i < data.size; i++) {
					options[i][0] = data.get(i).player.coloredName();
				}

				int mainid = Menus.registerMenu(listener);
				Call.menu(sender.con, mainid, "JOIN", "PLAYERS", options);

			}

		});

		handler.<Player>register("accept", "Accept the invitation", (args, recipient) -> {
			Seq<RequestData> data = PlayerData.requests
					.select(d -> d.recipient.equals(recipient));
			if (data.isEmpty()) {

			} else {
				String[][] options = new String[data.size][1];
				Menus.MenuListener listener = new Menus.MenuListener() {
					@Override
					public void get(Player recipient, int option) {
						Player sender = Groups.player.find(p -> p.equals(data.get(option).sender));
						if (sender == null) {

						} else {
							killTeam(sender.team());
							sender.team(recipient.team());
							PlayerData.players.add(new PlayerData(sender, sender.team(), false));
							PlayerData.requests.remove(data.get(option));
						}

					}
				};

				for (int i = 0; i < data.size; i++) {
					options[i][0] = data.get(i).sender.coloredName();
				}

				int mainid = Menus.registerMenu(listener);
				Call.menu(recipient.con, mainid, "JOIN", "PLAYERS", options);
			}
		});
	}

}
