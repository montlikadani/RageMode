package hu.montlikadani.ragemode.signs;

import java.io.IOException;
import java.util.Set;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.block.Sign;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.event.block.SignChangeEvent;

import hu.montlikadani.ragemode.gameLogic.PlayerList;
import hu.montlikadani.ragemode.toolbox.GetGames;

public class SignCreator {

  private static FileConfiguration fileConf = SignConfiguration.getSignConfiguration();

  public synchronized static boolean createNewSign(Sign sign, String game) {
    Set<String> signs = fileConf.getConfigurationSection("signs").getKeys(false);
    if (signs != null) {
      if (signs.contains(sign.getWorld().getName())) {
        return false;
      }
    }

    String path = "signs." + sign.getWorld().getName() + ".";

    fileConf.set(path + "game", game);
    fileConf.set(path + "x", sign.getX());
    fileConf.set(path + "y", sign.getY());
    fileConf.set(path + "z", sign.getZ());

    try {
      fileConf.save(SignConfiguration.getYamlSignsFile());
      return true;
    } catch (IOException e) {
      e.printStackTrace();
      return false;
    }
  }

  public synchronized static boolean removeSign(Sign sign) {
    String path = "signs." + sign.getWorld().getName();

    if (fileConf.contains(path)) {
      fileConf.set(path, null);
      try {
        fileConf.save(SignConfiguration.getYamlSignsFile());
        return true;
      } catch (IOException e) {
        e.printStackTrace();
        return false;
      }
    }
    return false;
  }

  public static boolean updateSign(SignChangeEvent sign) {
    String path = "signs." + sign.getBlock().getState().getWorld().getName();

    if (fileConf.contains(path)) {
      String game = fileConf.getString(path + ".game");
      sign.setLine(0, ChatColor.DARK_AQUA + "[" + ChatColor.DARK_PURPLE + "RageMode" + ChatColor.DARK_AQUA + "]");
      sign.setLine(1, game);
      sign.setLine(2,
          "Players " + ChatColor.DARK_AQUA + "[" + Integer.toString(PlayerList.getPlayersInGame(game).length)
              + " / " + GetGames.getMaxPlayers(game) + "]");
      String running = (PlayerList.isGameRunning(game))
          ? ChatColor.GOLD.toString() + ChatColor.ITALIC.toString() + "Running..."
          : ChatColor.DARK_GREEN.toString() + "Waiting...";
      sign.setLine(3, running);
      if (sign.getBlock().getState().update()) {
        return true;
      }
    }
    return false;
  }

  /**
   * Updates all JoinSigns for the given game which are properly configured.
   *
   * @param gameName The name of the game for which the signs should be updated.
   * @return True if at least one sign was updated successfully for the given game.
   */
  public static boolean updateAllSigns(String gameName) {
    if (fileConf.get("signs") == null) {
      return true;
    }

    Set<String> signs = fileConf.getConfigurationSection("signs").getKeys(false);
    if (signs != null) {
      for (String signString : signs) {
        String path = "signs." + signString;
        String game = fileConf.getString(path + ".game");
        if (game != null && gameName != null) {
          if (game.trim().equalsIgnoreCase(gameName.trim())) {
            int x = fileConf.getInt(path + ".x");
            int y = fileConf.getInt(path + ".y");
            int z = fileConf.getInt(path + ".z");
            String world = fileConf.getString(path + ".world");
            Location signLocation = new Location(Bukkit.getWorld(world), x, y, z);
            if (signLocation.getBlock().getState() instanceof Sign) {
              Sign sign = (Sign) signLocation.getBlock().getState();
              sign.setLine(0, ChatColor.DARK_AQUA + "[" + ChatColor.DARK_PURPLE + "RageMode"
                  + ChatColor.DARK_AQUA + "]");
              sign.setLine(1, game);
              sign.setLine(2, "Players " + ChatColor.DARK_AQUA + "["
                  + Integer.toString(PlayerList.getPlayersInGame(game).length) + " / "
                  + GetGames.getMaxPlayers(game) + "]");
              String running = (PlayerList.isGameRunning(game))
                  ? ChatColor.GOLD.toString() + ChatColor.ITALIC.toString() + "Running..."
                  : ChatColor.DARK_GREEN.toString() + "Waiting...";
              sign.setLine(3, running);
              sign.update();
            } else {
              Bukkit.getConsoleSender().sendMessage("A funny jester tried to modify the signs.yml without commands.");
              Bukkit.getConsoleSender().sendMessage("Skipping update of " + ChatColor.DARK_PURPLE + "(1)" + ChatColor.RESET + " sign...");
            }
          }
        } else {
          return false;
        }
      }
      return true;
    }
    return false;
  }

  /**
   * Checks whether the given Sign is properly configured to be an JoinSign or
   * not.
   *
   * @param sign The Sign for which the check should be done.
   * @return True if the signs.yml contains a correct value for the given Sign instance.
   */
  public static boolean isJoinSign(Sign sign) {
    Location signPosition = sign.getLocation();

    Set<String> signs = fileConf.getConfigurationSection("signs").getKeys(false);
    if (signs != null) {
      for (String signString : signs) {
        String path = "signs." + signString;
        String game = fileConf.getString(path + ".game");
        for (String gameName : GetGames.getGameNames()) {
          if (game.trim().equalsIgnoreCase(gameName.trim())) {
            int x = fileConf.getInt(path + ".x");
            int y = fileConf.getInt(path + ".y");
            int z = fileConf.getInt(path + ".z");
            String world = fileConf.getString(path + ".world");
            Location signLocation = new Location(Bukkit.getWorld(world), x, y, z);
            if (signPosition.getBlockX() == x && signPosition.getBlockY() == y && signPosition.getBlockZ() == z) {
              if (signLocation.getBlock().getState() instanceof Sign) {
                return true;
              }
            }
          }
        }
      }
    }
    return false;
  }

  public static String getGameFromSign(Sign sign) {
    int x = sign.getX();
    int y = sign.getY();
    int z = sign.getZ();
    String world = sign.getWorld().getName();
    String signString = Integer.toString(x) + Integer.toString(y) + Integer.toString(z) + world;
    String path = "signs." + signString;
    String game = fileConf.getString(path + ".game");
    for (String gameName : GetGames.getGameNames()) {
      if (game.trim().equalsIgnoreCase(gameName.trim())) {
        return game;
      }
    }
    return "";
  }
}
