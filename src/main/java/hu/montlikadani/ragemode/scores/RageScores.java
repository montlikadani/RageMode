package hu.montlikadani.ragemode.scores;

import java.util.HashMap;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import hu.montlikadani.ragemode.RageMode;
import hu.montlikadani.ragemode.gameLogic.PlayerList;
import hu.montlikadani.ragemode.scoreboard.ScoreBoard;
import hu.montlikadani.ragemode.scoreboard.ScoreBoardHolder;

public class RageScores {

  private static HashMap<String, PlayerPoints> playerpoints = new HashMap<>();
  // private static TableList<String, String> playergame = new
  // TableList<String, String>(); ----> User PlayerList.getPlayersInGame
  // instead
  private static int totalPoints = 0;

  public static void addPointsToPlayer(Player killer, Player victim, String killCause) {
    if (!killer.getUniqueId().toString().equals(victim.getUniqueId().toString())) {
      String killerUUID = killer.getUniqueId().toString();
      PlayerPoints killerPoints;
      String victimUUID = victim.getUniqueId().toString();
      PlayerPoints victimPoints;

      switch (killCause.toLowerCase().trim()) {
        case "ragebow":
          int bowPoints = 25;
          totalPoints = addPoints(killer, PlayerList.getPlayersGame(killer), bowPoints, true);
          addPoints(victim, PlayerList.getPlayersGame(victim), 0, false);

          killerPoints = playerpoints.get(killerUUID);
          int oldDirectArrowKills = killerPoints.getDirectArrowKills();
          int newDirectArrowKills = oldDirectArrowKills + 1;
          killerPoints.setDirectArrowKills(newDirectArrowKills);

          victimPoints = playerpoints.get(victimUUID);
          int oldDirectArrowDeaths = victimPoints.getDirectArrowDeaths();
          int newDirectArrowDeaths = oldDirectArrowDeaths + 1;
          victimPoints.setDirectArrowDeaths(newDirectArrowDeaths);

          killer.sendMessage(RageMode.getLang().get("game.message.arrow-kill", "%victim%", victim.getName(), "%points%", "+" + Integer.toString(bowPoints)));

          victim.sendMessage(RageMode.getLang().get("game.message.arrow-death", "%killer%", killer.getName(), "%points%", ""));

          killer.sendMessage(RageMode.getLang().get("game.message.current-points", "%points%", Integer.toString(totalPoints)));
          break;
        case "combataxe":
          int axePoints = 30;
          int axeMinusPoints = -50;
          totalPoints = addPoints(killer, PlayerList.getPlayersGame(killer), axePoints, true);
          addPoints(victim, PlayerList.getPlayersGame(victim), axeMinusPoints, false);

          killerPoints = playerpoints.get(killerUUID);
          int oldAxeKills = killerPoints.getAxeKills();
          int newAxeKills = oldAxeKills + 1;
          killerPoints.setAxeKills(newAxeKills);

          victimPoints = playerpoints.get(victimUUID);
          int oldAxeDeaths = victimPoints.getAxeDeaths();
          int newAxeDeaths = oldAxeDeaths + 1;
          victimPoints.setAxeDeaths(newAxeDeaths);

          killer.sendMessage(RageMode.getLang().get("game.message.axe-kill", "%victim%", victim.getName(), "%points%", "+" + Integer.toString(axePoints)));

          victim.sendMessage(RageMode.getLang().get("game.message.axe-death", "%killer%", killer.getName(), "%points%", Integer.toString(axeMinusPoints)));

          killer.sendMessage(RageMode.getLang().get("game.message.current-points", "%points%", Integer.toString(totalPoints)));
          break;
        case "rageknife":
          int knifePoints = 15;
          totalPoints = addPoints(killer, PlayerList.getPlayersGame(killer), knifePoints, true);
          addPoints(victim, PlayerList.getPlayersGame(victim), 0, false);

          killerPoints = playerpoints.get(killerUUID);
          int oldKnifeKills = killerPoints.getKnifeKills();
          int newKnifeKills = oldKnifeKills + 1;
          killerPoints.setKnifeKills(newKnifeKills);

          victimPoints = playerpoints.get(victimUUID);
          int oldKnifeDeaths = victimPoints.getKnifeDeaths();
          int newKnifeDeaths = oldKnifeDeaths + 1;
          victimPoints.setKnifeDeaths(newKnifeDeaths);

          killer.sendMessage(RageMode.getLang().get("game.message.knife-kill", "%victim%", victim.getName(), "%points%", "+" + Integer.toString(knifePoints)));

          victim.sendMessage(RageMode.getLang().get("game.message.knife-death", "%killer%", killer.getName(), "%points%", ""));

          killer.sendMessage(RageMode.getLang().get("game.message.current-points", "%points%", Integer.toString(totalPoints)));
          break;
        case "explosion":
          int explosionPoints = 25;
          totalPoints = addPoints(killer, PlayerList.getPlayersGame(killer), explosionPoints, true);
          addPoints(victim, PlayerList.getPlayersGame(victim), 0, false);

          killerPoints = playerpoints.get(killerUUID);
          int oldExplosionKills = killerPoints.getExplosionKills();
          int newExplosionKills = oldExplosionKills + 1;
          killerPoints.setExplosionKills(newExplosionKills);

          victimPoints = playerpoints.get(victimUUID);
          int oldExplosionDeaths = victimPoints.getExplosionDeaths();
          int newExplosionDeaths = oldExplosionDeaths + 1;
          victimPoints.setExplosionDeaths(newExplosionDeaths);

          killer.sendMessage(RageMode.getLang().get("game.message.explosion-kill", "%victim%", victim.getName(), "%points%",
              "+" + Integer.toString(explosionPoints)));

          victim.sendMessage(RageMode.getLang().get("game.message.explosion-death", "%killer%", killer.getName(), "%points%", ""));

          killer.sendMessage(RageMode.getLang().get("game.message.current-points", "%points%", Integer.toString(totalPoints)));
          break;
        default:
          break;
      }

      // -------KillStreak messages-------

      PlayerPoints currentPoints = playerpoints.get(killerUUID);
      int currentStreak = currentPoints.getCurrentStreak();
      if (currentStreak == 3 || currentStreak % 5 == 0) {
        currentPoints.setPoints(currentPoints.getPoints() + (currentStreak * 10));

        killer.sendMessage(RageMode.getLang().get("game.message.streak", "%number%", Integer.toString(currentStreak), "%points%",
            "+" + Integer.toString(currentStreak * 10)));
      }
      // -------End of KillStreak messages-------

      ScoreBoard board = ScoreBoard.allScoreBoards.get(PlayerList.getPlayersGame(killer));
      updateScoreBoard(killer, board);
      updateScoreBoard(victim, board);
    } else {
      killer.sendMessage(RageMode.getLang().get("game.message.suicide"));
    }
    // TabGuiUpdater.updateTabGui(PlayerList.getPlayersGame(killer));
    // TabAPI.updateTabGuiListOverlayForGame(PlayerList.getPlayersGame(killer));
  }

  private static void updateScoreBoard(Player player, ScoreBoard scoreBoard) {
    PlayerPoints points = playerpoints.get(player.getUniqueId().toString());
    ScoreBoardHolder holder = scoreBoard.getScoreboards().get(player);

    String oldKD = holder.getOldKdLine();
    String newKD = ChatColor.YELLOW + Integer.toString(points.getKills()) + " / "
        + Integer.toString(points.getDeaths()) + " " + ChatColor.GREEN + "K/D" + ChatColor.RESET;
    holder.setOldKdLine(newKD);
    scoreBoard.updateLine(player, oldKD, newKD, 0);

    String oldPoints = holder.getOldPointsLine();
    String newPoints = ChatColor.YELLOW + Integer.toString(points.getPoints()) + " " + ChatColor.GREEN + "Points" + ChatColor.RESET;
    holder.setOldPointsLine(newPoints);
    scoreBoard.updateLine(player, oldPoints, newPoints, 1);
  }

  public static void removePointsForPlayers(String[] playerUUIDs) {
    for (String playerUUID : playerUUIDs) {
      playerpoints.remove(playerUUID);
    }
  }

  public static PlayerPoints getPlayerPoints(String playerUUID) {
    return playerpoints.getOrDefault(playerUUID, null);
  }

  public static void resetPlayerStats(String playerUUID) {
    PlayerPoints points = playerpoints.get(playerUUID);
    if (points.getAxeDeaths() > 0 || points.getAxeKills() > 0 || points.getCurrentStreak() > 0 || points.getDeaths() > 0 ||
        points.getDirectArrowDeaths() > 0 || points.getDirectArrowKills() > 0 || points.getExplosionDeaths() > 0 ||
        points.getExplosionKills() > 0 || points.getKills() > 0 || points.getKnifeDeaths() > 0 || points.getKnifeKills() > 0 ||
        points.getLongestStreak() > 0 || points.getPoints() > 0) {
      points.setAxeDeaths(0);
    }
    points.setAxeKills(0);
    points.setCurrentStreak(0);
    points.setDeaths(0);
    points.setDirectArrowDeaths(0);
    points.setDirectArrowKills(0);
    points.setExplosionDeaths(0);
    points.setExplosionKills(0);
    points.setKills(0);
    points.setKnifeDeaths(0);
    points.setKnifeKills(0);
    points.setLongestStreak(0);
    points.setPoints(0);
  }

  private static int addPoints(Player player, String gameName, int points, boolean killer) {
    // returns total points
    String playerUUID = player.getUniqueId().toString();
    if (playerpoints.containsKey(playerUUID)) {
      PlayerPoints pointsHolder = playerpoints.get(playerUUID);
      int oldPoints = pointsHolder.getPoints();
      int oldKills = pointsHolder.getKills();
      int oldDeaths = pointsHolder.getDeaths();
      // playerpoints.remove(playerUUID);
      int totalPoints = oldPoints + points;
      int totalKills = oldKills;
      int totalDeaths = oldDeaths;
      int currentStreak = 0;
      int longestStreak = 0;
      if (killer) {
        totalKills++;
        currentStreak = pointsHolder.getCurrentStreak() + 1;
      } else {
        totalDeaths++;
        currentStreak = 0;
      }
      longestStreak = (currentStreak > pointsHolder.getLongestStreak()) ? currentStreak : pointsHolder.getLongestStreak();

      pointsHolder.setPoints(totalPoints);
      pointsHolder.setKills(totalKills);
      pointsHolder.setDeaths(totalDeaths);
      pointsHolder.setCurrentStreak(currentStreak);
      pointsHolder.setLongestStreak(longestStreak);
      // playerpoints.put(playerUUID, pointsHolder);
      return totalPoints;
    } else {
      int totalKills = 0;
      int totalDeaths = 0;
      int currentStreak = 0;
      int longestStreak = 0;
      if (killer) {
        totalKills = 1;
        currentStreak = 1;
        longestStreak = 1;
      } else {
        totalDeaths = 1;
        currentStreak = 0;
      }
      PlayerPoints pointsHolder = new PlayerPoints(playerUUID);
      pointsHolder.setPoints(points);
      pointsHolder.setKills(totalKills);
      pointsHolder.setDeaths(totalDeaths);
      pointsHolder.setCurrentStreak(currentStreak);
      pointsHolder.setLongestStreak(longestStreak);
      playerpoints.put(playerUUID, pointsHolder);
      return points;
    }
  }

  public static String calculateWinner(String game, String[] players) {
    String highest = UUID.randomUUID().toString();
    String goy = highest;
    int highestPoints = -200000000;
    int i = 0;
    int imax = players.length;
    while (i < imax) {
      if (playerpoints.containsKey(players[i])) {
        // Bukkit.broadcastMessage(Bukkit.getPlayer(UUID.fromString(players[i])).getName()
        // + " " + Integer.toString(i) + " " +
        // playerpoints.get(players[i]).getPoints() + " " +
        // Integer.toString(highestPoints));
        if (playerpoints.get(players[i]).getPoints() > highestPoints) {
          highest = players[i];
          highestPoints = playerpoints.get(players[i]).getPoints();
        }
        // else
        // Bukkit.broadcastMessage("nothighest"+players[i]);
      }
      // else
      // Bukkit.broadcastMessage(players[i]);
      i++;

    }

    if (goy.equals(highest)) {
      i = 0;
      while (i < imax) {
        Bukkit.getPlayer(UUID.fromString(players[i])).sendMessage(RageMode.getLang().get("game.message.player-won", "%player%",
            "Herobrine", "%game%", game));
        i++;
      }
      return null;
    } else {
      playerpoints.get(highest).setWinner(true);

      i = 0;
      while (i < imax) {
        if (players[i].equals(highest)) {
          Bukkit.getPlayer(UUID.fromString(highest)).sendMessage(RageMode.getLang().get("game.message.you-won", "%game%", game));
        } else {
          Bukkit.getPlayer(UUID.fromString(players[i])).sendMessage(RageMode.getLang().get("game.message.player-won", "%player%",
              Bukkit.getPlayer(UUID.fromString(highest)).getName(), "%game%", game));
        }
        i++;
      }
    }
    return highest;
  }

  // TODO Statistics for games and for the server globally. (Maybe also for
  // each map separately) (Total Axe kills, total bow kills,..., best axe
  // killer, best total killer,..., best victim,...

}