package main.java.hexed.listeners;

import arc.struct.Seq;
import arc.util.CommandHandler;
import arc.util.Time;
import main.java.hexed.data.PlayerData;
import main.java.hexed.Utils;
import mindustry.game.Team;
import mindustry.gen.Call;
import mindustry.gen.Groups;
import mindustry.gen.Player;
import mindustry.ui.Menus;

import static main.java.hexed.Utils.*;
import static main.java.hexed.data.PlayerData.*;


public class ClientCommands {
    public static void load(CommandHandler handler) {
        Seq<PlayerData> votingPlayers = new Seq<>();
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

        handler.<Player>register("lb", "Display the leaderboard.", (args, player) -> {
            Call.infoPopup(player.con, "[accent]list of leaders[white]\n\n" + Utils.getLeaderboard(), 10f, 0, 0, 0, 0, 1700);
        });

        handler.<Player>register("restart", "Voting for restarting the game.", (args, player) -> {
            PlayerData data = getData(player);
            if(!votingPlayers.contains(data)) {
                votingPlayers.add(data);
            }

            if(votingPlayers.size > Groups.player.size() * 0.5) {
                Call.sendMessage("The required number of votes has been collected.");
                endGame();
            }
        });

        handler.<Player>register("join", "Join the player.", (args, sender) -> {
            Seq<PlayerData> data = players.select(d -> d.leader && d.isActive() && d.team != sender.team());
            if (data.isEmpty()) {
                sender.sendMessage("There are no available players.");
            } else {
                Menus.MenuListener listener = (player, option) -> {
                    Player recipient = data.get(option).player;

                    if (requests.contains(d -> d.sender == player && d.recipient == recipient)) {
                        player.sendMessage("you already send request.");

                    } else {
                        requests.add(new RequestData(player, recipient));
                        recipient.sendMessage(player.coloredName() + " [white]A player has sent you an invitation. To accept the player into your team, type /accept.");
                    }
                };

                String[][] options = new String[data.size][1];
                data.sort().each(d -> options[data.indexOf(d)][0] = d.player.coloredName());

                Call.menu(sender.con, Menus.registerMenu(listener), "/JOIN", "PLAYERS", options);

            }

        });

        handler.<Player>register("accept", "Accept the invitation.", (args, recipient) -> {
            Seq<RequestData> data = requests.select(d -> d.recipient == recipient);

            if (data.isEmpty()) {
                recipient.sendMessage("You have not yet received a request.");
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
