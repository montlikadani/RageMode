package hu.montlikadani.ragemode;

import java.io.File;
import java.util.logging.Level;

import org.bukkit.Bukkit;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

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
import hu.montlikadani.ragemode.signs.SignScheduler;
import hu.montlikadani.ragemode.statistics.YAMLStats;

public class RageMode extends JavaPlugin {

	private Configuration conf = null;
	private PlayerList playerList = null;
	private SignScheduler sign = null;
	private static Language lang = null;
	private static MySQLConnect mySQLConnect = null;

	private static RageMode instance = null;

	private boolean hologram = false;
	private static String packageVersion, version = "";

	@Override
	public void onEnable() {
		instance = this;
		try {
			if (instance == null) {
				logConsole(Level.SEVERE, "Plugin instance is null. Disabling...");
				getManager().disablePlugin(this);
				return;
			}

			conf = new Configuration(this);
			if (conf == null) {
				logConsole(Level.SEVERE, "Configuration path is invalid. Disabling...");
				getManager().disablePlugin(this);
				return;
			}
			conf.loadConfig();

			lang = new Language(this, conf.getCfg().getString("language"));
			if (lang == null) {
				logConsole(Level.SEVERE, "Language path is invalid. Disabling...");
				getManager().disablePlugin(this);
				return;
			}
			lang.loadLanguage();

			if (getManager().isPluginEnabled("HolographicDisplays")) {
				hologram = true;
				HoloHolder.initHoloHolder();
			} else
				hologram = false;

			if (getManager().isPluginEnabled("PlaceholderAPI"))
				new Placeholder().register();

			packageVersion = Bukkit.getServer().getClass().getPackage().getName().replace(".", ",").split(",")[3];
			version = Bukkit.getVersion();
			playerList = new PlayerList();
			initActionBar();

			getManager().registerEvents(new EventListener(this), this);
			getCommand("ragemode").setExecutor(new RmCommand());
			getCommand("ragemode").setTabCompleter(new RmCommand());

			if (conf.getCfg().getString("statistics").equals("") || conf.getCfg().getString("statistics").equals("yaml"))
				initYamlStatistics();
			else if (conf.getCfg().getString("statistics").equals("mysql"))
				connectMySQL();

			if (conf.getCfg().getBoolean("signs.enable")) {
				sign = new SignScheduler(this);

				Bukkit.getScheduler().runTaskLater(this, sign, 40L);

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
		} catch (Throwable e) {
			e.printStackTrace();
			throwMsg();
		}
	}

	@Override
	public void onDisable() {
		if (instance == null) return;

		Thread thread = new Thread(new Runnable() {
			@Override
			public void run() {
				StopGame.stopAllGames();

				sign = null;
				getServer().getScheduler().cancelTasks(instance);
				instance = null;
			}
		});

		thread.start();
		while (thread.isAlive()) {
			try {
				Thread.sleep(500);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
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
		String host = conf.getCfg().getString("MySQL.host").equals("") ? "localhost" : conf.getCfg().getString("MySQL.host");
		String port = conf.getCfg().getString("MySQL.port").equals("") ? "1152" : conf.getCfg().getString("MySQL.port");
		String database = conf.getCfg().getString("MySQL.database").equals("") ? "myDB" : conf.getCfg().getString("MySQL.database");
		String username = conf.getCfg().getString("MySQL.username").equals("") ? "testName" : conf.getCfg().getString("MySQL.username");
		String password = conf.getCfg().getString("MySQL.password").equals("") ? "myPassword123" : conf.getCfg().getString("MySQL.password");
		String characterEnc = conf.getCfg().getString("MySQL.character-encoding").equals("") ? "UTF-8" : conf.getCfg().getString("MySQL.character-encoding");
		boolean serverCertificate = conf.getCfg().getBoolean("MySQL.verify-server-certificate");
		boolean useUnicode = conf.getCfg().getBoolean("MySQL.use-unicode");
		boolean autoReconnect = conf.getCfg().getBoolean("MySQL.auto-reconnect");
		boolean useSSL = conf.getCfg().getBoolean("MySQL.use-SSL");
		mySQLConnect = new MySQLConnect(host, port, database, username, password, serverCertificate, useUnicode, characterEnc, autoReconnect, useSSL);
		Bukkit.getServer().getScheduler().runTaskAsynchronously(this, new Runnable() {
			@Override
			public void run() {
				if (mySQLConnect != null)
					RuntimeRPPManager.getRPPListFromMySQL();
			}
		});
	}

	private void initActionBar() {
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
		return mySQLConnect == null ? null : mySQLConnect;
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
		logConsole(Level.WARNING, "There was an error. Please report it here:\nhttps://github.com/montlikadani/RageMode/issues");
		return;
	}

	public static void logConsole(Level lvl, String msg) {
		if (msg != null && !msg.equals(""))
			Bukkit.getLogger().log(lvl, msg);
	}

	public static String getPackageVersion() {
		return packageVersion == null ? "1_8_R3" : packageVersion;
	}

	public static String getVersion() {
		return version == null ? "1.8" : version;
	}

	public PluginManager getManager() {
		return Bukkit.getPluginManager();
	}
}
