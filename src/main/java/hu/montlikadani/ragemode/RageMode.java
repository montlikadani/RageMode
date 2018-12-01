package hu.montlikadani.ragemode;

import java.io.File;

import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

import hu.montlikadani.ragemode.commands.RmCommand;
import hu.montlikadani.ragemode.commands.StopGame;
import hu.montlikadani.ragemode.config.Configuration;
import hu.montlikadani.ragemode.config.Language;
import hu.montlikadani.ragemode.events.EventListener;
import hu.montlikadani.ragemode.gameLogic.PlayerList;
import hu.montlikadani.ragemode.holo.HoloHolder;
import hu.montlikadani.ragemode.runtimeRPP.RuntimeRPPManager;
import hu.montlikadani.ragemode.signs.SignConfiguration;
import hu.montlikadani.ragemode.signs.SignCreator;
import hu.montlikadani.ragemode.statistics.YAMLStats;
import hu.montlikadani.ragemode.toolbox.GetGames;

public class RageMode extends JavaPlugin {

  private static Language lang = null;
  private static RageMode instance = null;
  private Configuration conf = null;
  private PlayerList playerList = null;
  private boolean hologram = false;

  public static RageMode getInstance() {
    return instance;
  }

  public static Language getLang() {
    return lang;
  }

  @Override
  public void onEnable() {
    try {
      instance = this;
      conf = new Configuration(this);
      conf.loadConfig();

      lang = new Language(this, conf.getCfg().getString("language"));
      lang.loadLanguage();

      hologram = Bukkit.getPluginManager().isPluginEnabled("HolographicDisplays");

      playerList = new PlayerList();
      getServer().getPluginManager().registerEvents(new EventListener(this), this);
      //getServer().getPluginManager().registerEvents(new Achievement(this), this);
      getCommand("ragemode").setExecutor(new RmCommand());

      if (conf.getCfg().getBoolean("statistics")) {
        initYamlStatistics();
      }
      SignConfiguration.initSignConfiguration();
      HoloHolder.initHoloHolder();

      String[] games = GetGames.getGameNames();
      for (String game : games) {
        SignCreator.updateAllSigns(game);
      }
    } catch (Exception e) {
      e.printStackTrace();
      throwMsg();
    }
  }

  @Override
  public void onDisable() {
    if (!isEnabled()) {
      return;
    }

    try {
      Thread thread = new Thread(() -> StopGame.stopAllGames());

      thread.start();
      while (thread.isAlive()) {
        try {
          Thread.sleep(500);
        } catch (InterruptedException e) {
          e.printStackTrace();
        }
      }
    } catch (Exception e) {
      e.printStackTrace();
      throwMsg();
    }
  }

  private void initYamlStatistics() {
    YAMLStats.initS();

    Bukkit.getServer().getScheduler().runTaskAsynchronously(this, () -> RuntimeRPPManager.getRPPListFromYAML());
  }

  public File getFolder() {
    File dataFolder = getDataFolder();
    if (!dataFolder.exists()) {
      dataFolder.mkdir();
    }
    return dataFolder;
  }

  public PlayerList getPlayerList() {
    return playerList;
  }

  public boolean getHologramAvailabe() {
    return hologram;
  }

  public Configuration getConfiguration() {
    return conf;
  }

  public void throwMsg() {
    Bukkit.getLogger().warning("There was an error. Please report it here:\nhttps://github.com/montlikadani/RageMode/issues");
  }
}
