package hu.montlikadani.ragemode.gameLogic;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;

import hu.montlikadani.ragemode.RageMode;
import hu.montlikadani.ragemode.commands.StopGame;
import hu.montlikadani.ragemode.scoreboard.ScoreBoard;
import hu.montlikadani.ragemode.scores.PlayerPoints;
import hu.montlikadani.ragemode.scores.RageScores;
import hu.montlikadani.ragemode.toolbox.ActionBar;

public class GameTimer {

  private String gameName;
  private int secondsRemaining;
  private Timer t;

  public GameTimer(String gameName) {
    this.gameName = gameName;

    PlayerList.setGameRunning(gameName);
    getMinutesToGo();
    startCounting();
  }

  private void getMinutesToGo() {
    if (!RageMode.getInstance().getConfiguration().getArenasCfg().isSet("arenas." + gameName + ".gametime")) {
      if (RageMode.getInstance().getConfiguration().getCfg().getInt("game.global.gametime") > 0) {
        secondsRemaining = RageMode.getInstance().getConfiguration().getCfg().getInt("game.global.gametime") * 60;
      } else {
        secondsRemaining = 300;
      }
    } else {
      secondsRemaining = RageMode.getInstance().getConfiguration().getArenasCfg().getInt("arenas." + gameName + ".gametime") * 60;
    }
  }

  private void startCounting() {
    t = new Timer();

    int totalTimerMillis = (((secondsRemaining * 1000) + 5000) / 10000) * (10000);
    if (totalTimerMillis == 0) {
      totalTimerMillis = 10000;
    }
    final int timeMillisForLoop = totalTimerMillis;

    t.scheduleAtFixedRate(new TimerTask() {
      private int totalMessages = timeMillisForLoop / 60000;
      private int secondsRemaining = timeMillisForLoop / 1000;
      private int pointer = 0;
      // private int totalTimesLooped = 0;
      // private final long startTimeMillis = System.currentTimeMillis();

      public void run() {
        if (totalMessages > 0 && PlayerList.getPlayersInGame(gameName).length >= 2) {

          // long timeMillisNow = System.currentTimeMillis();
          // long timeMillisPassed = timeMillisNow - startTimeMillis;

          // if (fullMinute % 1 == 0) {
          // String[] playerUUIDs =
          // PlayerList.getPlayersInGame(gameName);
          // for (int i = 0; i < playerUUIDs.length; i++) {
          // Bukkit.getPlayer(UUID.fromString(playerUUIDs[i]))
          // .sendMessage(ConstantHolder.RAGEMODE_PREFIX +
          // ChatColor.BLUE
          // + "This round will end in " + ChatColor.YELLOW
          // + Integer.toString(totalMessages) + ChatColor.BLUE + "
          // minutes.");
          // }
          // totalMessages--;
          // }
          double minutes = Math.floor(secondsRemaining / 60);
          double seconds = secondsRemaining % 60;
          secondsRemaining--;
          String minutesString;
          String secondsString;
          if (minutes < 10) {
            minutesString = "0" + Integer.toString((int) minutes);
          } else {
            minutesString = Integer.toString((int) minutes);
          }
          secondsString = (seconds < 10) ? "0" + Integer.toString((int) seconds) : Integer.toString((int) seconds);
          ScoreBoard gameBoard = ScoreBoard.allScoreBoards.get(gameName);
          gameBoard.setTitle(ChatColor.GOLD + "RageMode" + ChatColor.RESET + " " + ChatColor.DARK_AQUA + minutesString + ":" + secondsString);
          if (secondsRemaining == 0) {
            this.cancel();
            RageMode.getInstance().getServer().getScheduler().scheduleSyncDelayedTask(RageMode.getInstance(), () -> StopGame.stopGame(gameName));
          }

          // -------ActionBar update-------
          if (secondsRemaining % 2 == 0) {
            String[] playerUUIDs = PlayerList.getPlayersInGame(gameName);
            List<PlayerPoints> playerPoints = new ArrayList<>();
            for (String playerUUID : playerUUIDs) {
              PlayerPoints points = RageScores.getPlayerPoints(playerUUID);
              if (points != null) {
                playerPoints.add(points);
              }
            }
            Collections.sort(playerPoints);
            if (playerPoints.size() > pointer) {
              PlayerPoints points = playerPoints.get(pointer);
              String message = ChatColor.DARK_GREEN + Integer.toString(pointer + 1) + ": "
                  + ChatColor.DARK_AQUA
                  + Bukkit.getPlayer(UUID.fromString(points.getPlayerUUID())).getName() + " - "
                  + ChatColor.GOLD + Integer.toString(points.getPoints()) + ChatColor.DARK_AQUA
                  + " points. " + ChatColor.GOLD + Integer.toString(points.getKills()) + " / "
                  + Integer.toString(points.getDeaths()) + ChatColor.DARK_AQUA + " K/D.";
              for (String playerUUID : playerUUIDs) {
                /** TODO Create bossbar
                 * if (RageMode.getInstance().getConfiguration().getCfg().getBoolean("settings.global.bossbar")) {
                 Bukkit.createBossBar(message);
                 }*/
                if (!RageMode.getInstance().getConfiguration().getArenasCfg().isSet("arenas." + gameName + ".actionbar")) {
                  if (RageMode.getInstance().getConfiguration().getCfg().getBoolean("game.global.actionbar")) {
                    ActionBar.sendActionBar(Bukkit.getPlayer(UUID.fromString(playerUUID)), message);
                  }
                } else if (RageMode.getInstance().getConfiguration().getCfg().getBoolean("arenas." + gameName + ".actionbar")) {
                  ActionBar.sendActionBar(Bukkit.getPlayer(UUID.fromString(playerUUID)), message);
                }
              }
              pointer++;
            } else {
              if (pointer >= PlayerList.getPlayersInGame(gameName).length) {
                pointer = 0;
              }
            }
          }
          // -------End of ActionBar update-------
        } else {
          this.cancel();
          RageMode.getInstance().getServer().getScheduler().scheduleSyncDelayedTask(RageMode.getInstance(), () -> StopGame.stopGame(gameName));
        }
      }
    }, 0, 1000);
  }
}
