package hu.montlikadani.ragemode;

import java.io.File;
import java.util.logging.Level;

import org.bukkit.Bukkit;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

import com.google.common.base.StandardSystemProperty;

import hu.montlikadani.ragemode.commands.RmCommand;
import hu.montlikadani.ragemode.commands.StopGame;
import hu.montlikadani.ragemode.config.Configuration;
import hu.montlikadani.ragemode.config.Language;
import hu.montlikadani.ragemode.database.MySQLConnect;
import hu.montlikadani.ragemode.events.EventListener;
import hu.montlikadani.ragemode.events.Listeners_1_8;
import hu.montlikadani.ragemode.events.Listeners_1_9;
import hu.montlikadani.ragemode.gameLogic.PlayerList;
import hu.montlikadani.ragemode.gameUtils.ActionBar;
import hu.montlikadani.ragemode.gameUtils.GetGames;
import hu.montlikadani.ragemode.holo.HoloHolder;
import hu.montlikadani.ragemode.metrics.Metrics;
import hu.montlikadani.ragemode.runtimeRPP.RuntimeRPPManager;
import hu.montlikadani.ragemode.scores.RageScores;
import hu.montlikadani.ragemode.signs.SignConfiguration;
import hu.montlikadani.ragemode.signs.SignCreator;
import hu.montlikadani.ragemode.signs.SignScheduler;
import hu.montlikadani.ragemode.statistics.YAMLStats;
import net.milkbowl.vault.economy.Economy;

public class RageMode extends JavaPlugin {

	private Configuration conf = null;
	private PlayerList playerList = null;
	private SignScheduler sign = null;
	private static Language lang = null;
	private static MySQLConnect mySQLConnect = null;
	private static Economy econ = null;

	private static RageMode instance = null;

	private boolean hologram = false;
	private boolean vault = false;

	@Override
	public void onEnable() {
		instance = this;

		try {
			if (instance == null) {
				logConsole(Level.SEVERE, "Plugin instance is null. Disabling...");
				getManager().disablePlugin(this);
				return;
			}

			if (!checkJavaVersion()) {
				getManager().disablePlugin(this);
				return;
			}

			if (Utils.getVersion().contains("1.7")) {
				logConsole(Level.SEVERE, "[RageMode] This version is not supported by this plugin! Please use larger 1.8+");
				getManager().disablePlugin(this);
				return;
			}

			if (Utils.getVersion().contains("1.8"))
				logConsole("[RageMode] This version not fully supported by this plugin, so some options will not work.");

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

			if (getManager().isPluginEnabled("Vault")) {
				vault = true;
				initEconomy();
			} else
				vault = false;

			if (getManager().isPluginEnabled("PlaceholderAPI"))
				new Placeholder().register();

			playerList = new PlayerList();
			initActionBar();

			getManager().registerEvents(new EventListener(this), this);
			if (Utils.getVersion().contains("1.8"))
				getManager().registerEvents(new Listeners_1_8(), this);
			else
				getManager().registerEvents(new Listeners_1_9(), this);

			getCommand("ragemode").setExecutor(new RmCommand());
			getCommand("ragemode").setTabCompleter(new RmCommand());

			if (conf.getCfg().getString("statistics").equals("") || conf.getCfg().getString("statistics").equals("yaml"))
				initYamlStatistics();
			else if (conf.getCfg().getString("statistics").equals("mysql"))
				connectMySQL();

			if (conf.getCfg().getBoolean("signs.enable")) {
				sign = new SignScheduler(this);
				getManager().registerEvents(sign, this);

				SignConfiguration.initSignConfiguration();

				SignCreator.loadSigns();
				SignCreator.updateAllSigns(GetGames.getGameNames()[GetGames.getConfigGamesCount() - 1]);

				Bukkit.getScheduler().runTaskLater(this, sign, 40L);
			}

			if (conf.getCfg().getBoolean("metrics")) {
				Metrics metrics = new Metrics(this);
				// TODO Metrics statistic
				logConsole("Metrics enabled.");
			}
		} catch (Throwable e) {
			e.printStackTrace();
			throwMsg();
		}
	}

	@Override
	public void onDisable() {
		if (instance == null) return;

		Thread thread = new Thread(() -> {
			StopGame.stopAllGames();

			sign = null;
			getServer().getScheduler().cancelTasks(instance);
			instance = null;
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

		Bukkit.getServer().getScheduler().runTaskAsynchronously(this, () -> RuntimeRPPManager.getRPPListFromYAML());

		RageScores.load();
	}

	public void connectMySQL() {
		String host = conf.getCfg().getString("MySQL.host");
		String port = conf.getCfg().getString("MySQL.port");
		String database = conf.getCfg().getString("MySQL.database");
		String username = conf.getCfg().getString("MySQL.username");
		String password = conf.getCfg().getString("MySQL.password");
		String characterEnc = conf.getCfg().getString("MySQL.character-encoding").equals("") ? "UTF-8"
				: conf.getCfg().getString("MySQL.character-encoding");
		boolean serverCertificate = conf.getCfg().getBoolean("MySQL.verify-server-certificate");
		boolean useUnicode = conf.getCfg().getBoolean("MySQL.use-unicode");
		boolean autoReconnect = conf.getCfg().getBoolean("MySQL.auto-reconnect");
		boolean useSSL = conf.getCfg().getBoolean("MySQL.use-SSL");

		mySQLConnect = new MySQLConnect(host, port, database, username, password, serverCertificate,
				useUnicode, characterEnc, autoReconnect, useSSL);

		Bukkit.getServer().getScheduler().runTaskAsynchronously(this, () -> {
			if (mySQLConnect != null)
				RuntimeRPPManager.getRPPListFromMySQL();
		});
	}

	private void initActionBar() {
		ActionBar.nmsver = Bukkit.getServer().getClass().getPackage().getName();
		ActionBar.nmsver = ActionBar.nmsver.substring(ActionBar.nmsver.lastIndexOf(".") + 1);

		if (ActionBar.nmsver.equalsIgnoreCase("v1_8_") && ActionBar.nmsver.equalsIgnoreCase("v1_9_")
				&& ActionBar.nmsver.equalsIgnoreCase("v1_10_") && ActionBar.nmsver.equalsIgnoreCase("v1_11_")
				&& ActionBar.nmsver.equalsIgnoreCase("v1_12_") && ActionBar.nmsver.equalsIgnoreCase("v1_13_")
				&& ActionBar.nmsver.equalsIgnoreCase("v1_14_") || ActionBar.nmsver.startsWith("v1_7_"))
			ActionBar.useOldMethods = true;
	}

	private void initEconomy() {
		RegisteredServiceProvider<Economy> rsp = getServer().getServicesManager().getRegistration(Economy.class);
		if (rsp == null)
			return;

		econ = rsp.getProvider();
		if (econ == null)
			return;

		return;
	}

	public File getFolder() {
		File dataFolder = getDataFolder();
		if (!dataFolder.exists())
			dataFolder.mkdir();
		return dataFolder;
	}

	/**
	 * Gets the plugin instance
	 * 
	 * @return RageMode instance
	 */
	public static RageMode getInstance() {
		return instance;
	}

	/**
	 * Gets the MySQLConnect class
	 * 
	 * @return if mySQLConnect instance is not null
	 */
	public static MySQLConnect getMySQL() {
		return mySQLConnect == null ? null : mySQLConnect;
	}

	public PlayerList getPlayerList() {
		return playerList;
	}

	/**
	 * Gets the Language class
	 * 
	 * @return Language class
	 */
	public static Language getLang() {
		return lang;
	}

	public boolean isHologramEnabled() {
		return hologram;
	}

	public boolean isVaultEnabled() {
		return vault;
	}

	public Configuration getConfiguration() {
		return conf;
	}

	public void throwMsg() {
		logConsole(Level.WARNING, "There was an error. Please report it here:\nhttps://github.com/montlikadani/RageMode/issues");
		return;
	}

	/**
	 * Logging to console to write debug messages<br>
	 * Using the <b>Level.INFO</b> level by default
	 * 
	 * @param msg Error message
	 */
	public static void logConsole(String msg) {
		logConsole(Level.INFO, msg);
	}

	/**
	 * Logging to console to write debug messages
	 * 
	 * @param lvl Logging level
	 * @param msg Error message
	 */
	public static void logConsole(Level lvl, String msg) {
		if (msg != null && !msg.equals("") && instance.conf.getCfg().getBoolean("log-console"))
			Bukkit.getLogger().log(lvl, msg);
	}

	public PluginManager getManager() {
		return Bukkit.getPluginManager();
	}

	public Economy getEconomy() {
		return econ;
	}

	private boolean checkJavaVersion() {
		try {
			if (Float.parseFloat(StandardSystemProperty.JAVA_CLASS_VERSION.value()) < 52.0) {
				logConsole(Level.WARNING, "[RageMode] You are using an older Java that is not supported. Please use 1.8 or higher versions!");
				return false;
			}
		} catch (NumberFormatException e) {
			logConsole(Level.WARNING, "[RageMode] Failed to detect Java version.");
			return false;
		}
		return true;
	}
}
