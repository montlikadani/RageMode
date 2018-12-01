package hu.montlikadani.ragemode.commands;

import hu.montlikadani.ragemode.RageMode;
import hu.montlikadani.ragemode.events.EventListener;
import hu.montlikadani.ragemode.gameLogic.PlayerList;
import hu.montlikadani.ragemode.holo.HoloHolder;
import hu.montlikadani.ragemode.runtimeRPP.RuntimeRPPManager;
import hu.montlikadani.ragemode.scores.PlayerPoints;
import hu.montlikadani.ragemode.scores.RageScores;
import hu.montlikadani.ragemode.signs.SignCreator;
import hu.montlikadani.ragemode.statistics.YAMLStats;
import hu.montlikadani.ragemode.toolbox.GameBroadcast;
import hu.montlikadani.ragemode.toolbox.GetGames;
import hu.montlikadani.ragemode.toolbox.Titles;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.logging.Level;

public class StopGame extends RmCommand {

    public StopGame(CommandSender sender, Command cmd, String label, String[] args) {
        if(!sender.hasPermission("ragemode.admin.stopgame")) {
            sender.sendMessage(RageMode.getLang().get("no-permission"));
            return;
        }
        if(args.length >= 2) {
            if(PlayerList.isGameRunning(args[1])) {
                String[] players = PlayerList.getPlayersInGame(args[1]);

                RageScores.calculateWinner(args[1], players);

                if(players != null) {
                    int i = 0;
                    int imax = players.length;

                    while(i < imax) {
                        if(players[i] != null) {
                            PlayerList.removePlayer(Bukkit.getPlayer(UUID.fromString(players[i])));
                            PlayerList.removePlayerSynced(Bukkit.getPlayer(UUID.fromString(players[i])));
                        }
                        i++;
                    }
                }
                GameBroadcast.broadcastToGame(args[1], RageMode.getLang().get("game.stopped", "%game%", args[1]));

                RageScores.removePointsForPlayers(players);
                PlayerList.setGameNotRunning(args[1]);
                SignCreator.updateAllSigns(args[1]);
            } else
                sender.sendMessage(RageMode.getLang().get("game.not-running"));
        } else
            sender.sendMessage(RageMode.getLang().get("missing-arguments", "%usage%", "/rm stopgame <gameName>"));
        return;
    }

    public static void stopGame(String game) {
        boolean winnervalid = false;
        if(PlayerList.isGameRunning(game)) {
            String[] players = PlayerList.getPlayersInGame(game);
            String winnerUUID = RageScores.calculateWinner(game, players);
            if(winnerUUID != null) {
                if(UUID.fromString(winnerUUID) != null) {
                    if(Bukkit.getPlayer(UUID.fromString(winnerUUID)) != null) {
                        winnervalid = true;
                        Player winner = Bukkit.getPlayer(UUID.fromString(winnerUUID));
                        for(String playerUUID : players) {
                            Player player = Bukkit.getPlayer(UUID.fromString(playerUUID));
                            String title = ChatColor.DARK_GREEN + winner.getName() + ChatColor.GOLD + " won this round!";
                            String subtitle = ChatColor.GOLD + "He has got " + ChatColor.DARK_PURPLE
                                    + Integer.toString(RageScores.getPlayerPoints(winnerUUID).getPoints()) + ChatColor.GOLD
                                    + " points and his K/D is " + ChatColor.DARK_PURPLE
                                    + Integer.toString(RageScores.getPlayerPoints(winnerUUID).getKills()) + " / "
                                    + Integer.toString(RageScores.getPlayerPoints(winnerUUID).getDeaths()) + ChatColor.GOLD
                                    + ".";
                            PlayerList.removePlayerSynced(player);
                            Titles.sendTitle(player, 20, 200, 20, title, subtitle);
                            //player.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, 200, 5, false, false), true);
                        }
                    }
                }
            }
            if(winnervalid == false) {
                for(String playerUUID : players) {
                    Player player = Bukkit.getPlayer(UUID.fromString(playerUUID));
                    GameBroadcast.broadcastToGame(game, "&cNo one won this game.");
                    PlayerList.removePlayerSynced(player);
                }
            }

            if(!EventListener.waitingGames.containsKey(game))
                EventListener.waitingGames.put(game, true);
            else {
                EventListener.waitingGames.remove(game);
                EventListener.waitingGames.put(game, true);
            }

            final String gameName = game;
            RageMode.getInstance().getServer().getScheduler().scheduleSyncDelayedTask(RageMode.getInstance(), new Runnable() {
                @Override
                public void run() {
                    finishStopping(gameName);
                    if(EventListener.waitingGames.containsKey(gameName))
                        EventListener.waitingGames.remove(gameName);
                }
            }, 200);
        }
    }

    private static void finishStopping(String game) {
        if(PlayerList.isGameRunning(game)) {
            final String[] players = PlayerList.getPlayersInGame(game);
            int f = 0;
            int fmax = players.length;
            List<PlayerPoints> lPP = new ArrayList<>();
            while(f < fmax) {
                if(RageScores.getPlayerPoints(players[f]) != null) {
                    final PlayerPoints pP = RageScores.getPlayerPoints(players[f]);
                    lPP.add(pP);

                    Bukkit.getServer().getScheduler().runTaskAsynchronously(RageMode.getInstance(), new Runnable() {
                        @Override
                        public void run() {
                            RuntimeRPPManager.updatePlayerEntry(pP);
                            Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(RageMode.getInstance(), new Runnable() {

                                @Override
                                public void run() {
                                    for(String playerUUID : players) {
                                        HoloHolder.updateHolosForPlayer(Bukkit.getPlayer(UUID.fromString(playerUUID)));
                                    }
                                }
                            });
                        }
                    });
                }
                f++;
            }
            Thread thread = new Thread(YAMLStats.createPlayersStats(lPP));
            thread.start();

            if(players != null) {
                int i = 0;
                int imax = players.length;

                while(i < imax) {
                    if(players[i] != null)
                        PlayerList.removePlayer(Bukkit.getPlayer(UUID.fromString(players[i])));
                    i++;
                }
            }
            RageScores.removePointsForPlayers(players);

            GameBroadcast.broadcastToGame(game, RageMode.getLang().get("game.stopped", "%game%", game));
            PlayerList.setGameNotRunning(game);
            SignCreator.updateAllSigns(game);
        }
    }

    public static void stopAllGames() {
        Bukkit.getLogger().log(Level.INFO, "Searching games to stop...");

        String[] games = GetGames.getGameNames();

        int i = 0;
        int imax = games.length;

        while(i < imax) {
            if(PlayerList.isGameRunning(games[i])) {

                Bukkit.getLogger().log(Level.INFO, "Stopping " + games[i] + " ...");

                String[] players = PlayerList.getPlayersInGame(games[i]);
                RageScores.calculateWinner(games[i], players);

                if(players != null) {
                    int n = 0;
                    int nmax = players.length;

                    while(n < nmax) {
                        if(players[n] != null)
                            PlayerList.removePlayer(Bukkit.getPlayer(UUID.fromString(players[n])));
                        n++;
                    }
                }
                RageScores.removePointsForPlayers(players);
                PlayerList.setGameNotRunning(games[i]);
                SignCreator.updateAllSigns(games[i]);
                Bukkit.getLogger().log(Level.INFO, games[i] + " has been stopped.");
            }
            i++;
        }
        Bukkit.getLogger().log(Level.INFO, "RageMode: All games stopped!");
    }
}
