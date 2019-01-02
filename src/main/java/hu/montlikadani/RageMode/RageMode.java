package hu.montlikadani.ragemode;

import java.io.File;
import java.util.logging.Level;

import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

import hu.montlikadani.ragemode.achievements.AchievementHandler;
import hu.montlikadani.ragemode.commands.RmCommand;
import hu.montlikadani.ragemode.commands.StopGame;
import hu.montlikadani.ragemode.config.Configuration;
import hu.montlikadani.ragemode.config.Language;
import hu.montlikadani.ragemode.events.EventListener;
import hu.montlikadani.ragemode.gameLogic.PlayerList;
import hu.montlikadani.ragemode.gameUtils.ActionBar;
import hu.montlikadani.ragemode.gameUtils.GetGames;
import hu.montlikadani.ragemode.holo.HoloHolder;
import hu.montlikadani.ragemode.metrics.Metrics;
import hu.montlikadani.ragemode.runtimeRPP.RuntimeRPPManager;
import hu.montlikadani.ragemode.signs.SignConfiguration;
import hu.montlikadani.ragemode.signs.SignCreator;
import hu.montlikadani.ragemode.statistics.YAMLStats;

public class RageMode extends JavaPlugin {

	private Configuration conf = null;
	private PlayerList playerList = null;
	private static Language lang = null;
	private static MySQLConnect mySQLConnect = null;

	private static RageMode instance = null;

	private boolean hologram = false;
	private static String version;

	@Override
	public void onEnable() {
		try {
			instance = this;
			conf = new Configuration(this);
			conf.loadConfig();

			lang = new Language(this, conf.getCfg().getString("language"));
			lang.loadLanguage();

			if (Bukkit.getPluginManager().isPluginEnabled("HolographicDisplays")) {
				hologram = true;
				HoloHolder.initHoloHolder();
			} else
				hologram = false;

			if (Bukkit.getPluginManager().isPluginEnabled("PlaceholderAPI"))
				new Placeholder().register();

			version = Bukkit.getServer().getClass().getPackage().getName().replace(".", ",").split(",")[3];
			playerList = new PlayerList();
			initActionBar();
			getServer().getPluginManager().registerEvents(new EventListener(this), this);
			getServer().getPluginManager().registerEvents(new AchievementHandler(this), this);
			getCommand("ragemode").setExecutor(new RmCommand());
			getCommand("ragemode").setTabCompleter(new RmCommand());

			if (conf.getCfg().getString("statistics").equals("yaml"))
				initYamlStatistics();
			else if (conf.getCfg().getString("statistics").equals("mysql"))
				connectMySQL();

			if (conf.getCfg().getBoolean("signs.enable")) {
				SignConfiguration.initSignConfiguration();

				for (String game : GetGames.getGameNames()) {
					SignCreator.updateAllSigns(game);
				}
			}

			if (conf.getCfg().getBoolean("metrics")) {
				Metrics metrics = new Metrics(this);
				// TODO Metrics statistic
				logConsole(Level.INFO, "Metrics enabled.");
			}
		} catch (Exception e) {
			e.printStackTrace();
			throwMsg();
		}
	}

	@Override
	public void onDisable() {
		try {
			StopGame.stopAllGames();

			ActionBar.plugin = null;
			getServer().getScheduler().cancelTasks(this);
			instance = null;
		} catch (Exception e) {
			e.printStackTrace();
			throwMsg();
		}
	}

	private void initYamlStatistics() {
		YAMLStats.initS();

		Bukkit.getServer().getScheduler().runTaskAsynchronously(this, new Runnable() {
			@Override
			public void run() {
				RuntimeRPPManager.getRPPListFromYAML();
			}
		});
	}

	public void connectMySQL() {
		String host = conf.getCfg().getString("MySQL.host");
		String port = conf.getCfg().getString("MySQL.port");
		String database = conf.getCfg().getString("MySQL.database");
		String username = conf.getCfg().getString("MySQL.username");
		String password = conf.getCfg().getString("MySQL.password");
		mySQLConnect = new MySQLConnect(host, port, database, username, password);
		Bukkit.getServer().getScheduler().runTaskAsynchronously(this, new Runnable() {
			@Override
			public void run() {
				if (mySQLConnect != null)
					RuntimeRPPManager.getRPPListFromMySQL();
			}
		});
	}

	private void initActionBar() {
		ActionBar.plugin = this;
		ActionBar.nmsver = Bukkit.getServer().getClass().getPackage().getName();
		ActionBar.nmsver = ActionBar.nmsver.substring(ActionBar.nmsver.lastIndexOf(".") + 1);

		if (ActionBar.nmsver.equalsIgnoreCase("v1_8_") && ActionBar.nmsver.equalsIgnoreCase("v1_9_")
				&& ActionBar.nmsver.equalsIgnoreCase("v1_10_") && ActionBar.nmsver.equalsIgnoreCase("v1_11_")
				&& ActionBar.nmsver.equalsIgnoreCase("v1_12_") && ActionBar.nmsver.equalsIgnoreCase("v1_13_")
				|| ActionBar.nmsver.startsWith("v1_7_")) {
			ActionBar.useOldMethods = true;
		}
	}

	public File getFolder() {
		File dataFolder = getDataFolder();
		if (!dataFolder.exists())
			dataFolder.mkdir();
		return dataFolder;
	}

	public static RageMode getInstance() {
		return instance;
	}

	public static MySQLConnect getMySQL() {
		return mySQLConnect;
	}

	public PlayerList getPlayerList() {
		return playerList;
	}

	public static Language getLang() {
		return lang;
	}

	public boolean getHologramAvailable() {
		return hologram;
	}

	public Configuration getConfiguration() {
		return conf;
	}

	public void throwMsg() {
		Bukkit.getLogger().warning("There was an error. Please report it here:\nhttps://github.com/montlikadani/RageMode/issues");
		return;
	}

	public static void logConsole(Level lvl, String msg) {
		Bukkit.getLogger().log(lvl, msg);
	}

	public static String getVer() {
		return version;
	}
}
