package hu.montlikadani.ragemode.gameLogic;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import hu.montlikadani.ragemode.RageMode;
import hu.montlikadani.ragemode.items.CombatAxe;
import hu.montlikadani.ragemode.items.RageArrow;
import hu.montlikadani.ragemode.items.RageBow;
import hu.montlikadani.ragemode.items.RageKnife;
import hu.montlikadani.ragemode.scoreboard.ScoreBoard;
import hu.montlikadani.ragemode.scoreboard.ScoreBoardHolder;
import hu.montlikadani.ragemode.signs.SignCreator;
import hu.montlikadani.ragemode.toolbox.ActionBar;
import hu.montlikadani.ragemode.toolbox.GameBroadcast;

public class GameLoader {

  private String gameName;
  private List<Location> gameSpawns = new ArrayList<>();

  public GameLoader(String gameName) {
    this.gameName = gameName;

    PlayerList.setGameRunning(gameName);
    checkTeleport();
    setInventories();

    List<String> players = Arrays.asList(PlayerList.getPlayersInGame(gameName));
    // TabGuiUpdater.setTabGui(players);
    // TabAPI.setTabGuiListOverLayForPlayers(players);

    // TabAPI.setTab(players, "Test", 0, 0);

    ScoreBoard gameBoard = new ScoreBoard(players, true);
    gameBoard.setTitle(ChatColor.GOLD + "RageMode" + ChatColor.RESET);
    String kdLine = ChatColor.YELLOW + "0 / 0 " + ChatColor.GREEN + "K/D" + ChatColor.RESET;
    gameBoard.setLine(kdLine, 0);
    String pointsLine = ChatColor.YELLOW + "0 " + ChatColor.GREEN + "Points" + ChatColor.RESET;
    gameBoard.setLine(pointsLine, 1);
    gameBoard.setScoreBoard();
    gameBoard.addToScoreBoards(gameName, true);
    for (String player : players) {
      ScoreBoardHolder holder = gameBoard.getScoreboards().get(Bukkit.getPlayer(UUID.fromString(player)));
      holder.setOldKdLine(kdLine);
      holder.setOldPointsLine(pointsLine);
    }
    new GameTimer(this.gameName);
    SignCreator.updateAllSigns(gameName);

    String initialMessage = ChatColor.DARK_AQUA + "Welcome to " + ChatColor.DARK_PURPLE + gameName + ChatColor.DARK_AQUA + " game!";
    // TODO Create bossbar
		/*if (RageMode.getInstance().getConfiguration().getArenasCfg().isSet("arenas." + gameName + ".bossbar")) {
			if (RageMode.getInstance().getConfiguration().getArenasCfg().getBoolean("arenas." + gameName + ".bossbar")) {
				for (String player : players) {
					Bukkit.createBossBar(initialMessage);
				}
			}
		} else {
			if (RageMode.getInstance().getConfiguration().getCfg().getBoolean("settings.global.bossbar")) {
				for (String player : players) {
					Bukkit.createBossBar(initialMessage);
				}
			}
		}*/

    for (String player : players) {
      if (RageMode.getInstance().getConfiguration().getArenasCfg().isSet("arenas." + gameName + ".actionbar")) {
          if (RageMode.getInstance().getConfiguration().getArenasCfg().getBoolean("arenas." + gameName + ".actionbar")) {
              ActionBar.sendActionBar(Bukkit.getPlayer(UUID.fromString(player)), initialMessage);
          }
      } else if (RageMode.getInstance().getConfiguration().getCfg().getBoolean("game.global.actionbar")) {
          ActionBar.sendActionBar(Bukkit.getPlayer(UUID.fromString(player)), initialMessage);
      }
    }
  }

  private void checkTeleport() {
    GameSpawnGetter gameSpawnGetter = new GameSpawnGetter(gameName);
    if (gameSpawnGetter.isGameReady()) {
      gameSpawns = gameSpawnGetter.getSpawnLocations();
      teleportPlayersToGameSpawns();
    } else {
      GameBroadcast.broadcastToGame(gameName, RageMode.getLang().get("game.not-set-up"));
      String[] players = PlayerList.getPlayersInGame(gameName);
      for (String player : players) {
        Player thisPlayer = Bukkit.getPlayer(UUID.fromString(player));
        PlayerList.removePlayer(thisPlayer);
      }
    }
  }

  private void teleportPlayersToGameSpawns() {
    String[] players = PlayerList.getPlayersInGame(gameName);
    for (int i = 0; i < players.length; i++) {
      Player player = Bukkit.getPlayer(UUID.fromString(players[i]));
      Location location = gameSpawns.get(i);
      player.teleport(location);
    }
  }

  private void setInventories() {
    String[] players = PlayerList.getPlayersInGame(gameName);
    for (String playerUUID : players) {
      Player player = Bukkit.getPlayer(UUID.fromString(playerUUID));
      player.getInventory().setItem(0, RageBow.getRageBow());
      player.getInventory().setItem(1, RageKnife.getRageKnife());
      player.getInventory().setItem(2, CombatAxe.getCombatAxe());
      player.getInventory().setItem(9, RageArrow.getRageArrow());
      // see inventory positions here:
      // http://redditpublic.com/images/b/b2/Items_slot_number.png
    }
  }
}
