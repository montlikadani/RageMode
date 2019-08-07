package hu.montlikadani.ragemode;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.logging.Level;

import org.bukkit.Bukkit;
import org.bukkit.event.HandlerList;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;

import com.google.common.base.StandardSystemProperty;

import hu.montlikadani.ragemode.MinecraftVersion.Version;
import hu.montlikadani.ragemode.commands.RmCommand;
import hu.montlikadani.ragemode.commands.StopGame;
import hu.montlikadani.ragemode.config.Configuration;
import hu.montlikadani.ragemode.config.Language;
import hu.montlikadani.ragemode.database.MySQLConnect;
import hu.montlikadani.ragemode.events.BungeeListener;
import hu.montlikadani.ragemode.events.EventListener;
import hu.montlikadani.ragemode.events.Listeners_1_8;
import hu.montlikadani.ragemode.events.Listeners_1_9;
import hu.montlikadani.ragemode.gameLogic.GameSpawnGetter;
import hu.montlikadani.ragemode.gameLogic.GameStatus;
import hu.montlikadani.ragemode.gameLogic.PlayerList;
import hu.montlikadani.ragemode.gameUtils.BungeeUtils;
import hu.montlikadani.ragemode.gameUtils.GameUtils;
import hu.montlikadani.ragemode.gameUtils.GetGames;
import hu.montlikadani.ragemode.holder.HoloHolder;
import hu.montlikadani.ragemode.metrics.Metrics;
import hu.montlikadani.ragemode.runtimeRPP.RuntimeRPPManager;
import hu.montlikadani.ragemode.scores.RageScores;
import hu.montlikadani.ragemode.signs.SignConfiguration;
import hu.montlikadani.ragemode.signs.SignCreator;
import hu.montlikadani.ragemode.signs.SignScheduler;
import hu.montlikadani.ragemode.statistics.MySQLStats;
import hu.montlikadani.ragemode.statistics.YAMLStats;
import net.milkbowl.vault.economy.Economy;

public class RageMode extends JavaPlugin {

	private Configuration conf = null;
	private SignScheduler sign = null;
	private BungeeUtils bungee = null;
	private static Language lang = null;
	private static MySQLConnect mySQLConnect = null;
	private static MinecraftVersion mcVersion = null;

	private static Economy econ = null;

	private static RageMode instance = null;
	private BukkitTask signTask;

	private boolean hologram = false;
	private boolean vault = false;

	private List<GameSpawnGetter> spawns = new ArrayList<>();

	@Override
	public void onEnable() {
		instance = this;

		try {
			if (!checkJavaVersion()) {
				getManager().disablePlugin(this);
				return;
			}

			mcVersion = new MinecraftVersion();

			if (Utils.getVersion().contains("1.7")) {
				Bukkit.getLogger().log(Level.SEVERE, "[RageMode] This version is not supported by this plugin! Please use larger 1.8+");
				getManager().disablePlugin(this);
				return;
			}

			if (Version.isCurrentEqualOrLower(Version.v1_8_R3))
				Bukkit.getLogger().log(Level.INFO, "[RageMode] This version not fully supported by this plugin, so some options will not work.");

			conf = new Configuration(this);
			conf.loadConfig();

			lang = new Language(this);
			lang.loadLanguage(conf.getCfg().getString("language"));

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

			if (conf.getCfg().getBoolean("bungee.enable")) {
				bungee = new BungeeUtils(this);

				getServer().getMessenger().registerOutgoingPluginChannel(this, "BungeeCord");
			}

			getManager().registerEvents(new EventListener(this), this);
			if (Version.isCurrentEqualOrLower(Version.v1_8_R3))
				getManager().registerEvents(new Listeners_1_8(), this);
			else
				getManager().registerEvents(new Listeners_1_9(), this);

			registerCommands();

			if (conf.getCfg().getString("statistics").equals("") || conf.getCfg().getString("statistics").equals("yaml"))
				initYamlStatistics();
			else if (conf.getCfg().getString("statistics").equals("mysql"))
				connectMySQL();

			if (conf.getCfg().getBoolean("signs.enable")) {
				sign = new SignScheduler(this);
				getManager().registerEvents(sign, this);

				SignConfiguration.initSignConfiguration();
				SignCreator.loadSigns();

				signTask = Bukkit.getScheduler().runTaskLater(this, sign, 40L);
			}

			if (conf.getArenasCfg().contains("arenas")) {
				for (String game : GetGames.getGameNames()) {
					if (game != null) {
						if (conf.getCfg().getBoolean("bungee.enable"))
							getManager().registerEvents(new BungeeListener(game), this);

						new PlayerList();

						spawns.add(new GameSpawnGetter(game));

						if (conf.getCfg().getBoolean("signs.enable"))
							SignCreator.updateAllSigns(game);

						// Loads the game locker
						if (conf.getArenasCfg().contains("arenas." + game + ".lock")
								&& conf.getArenasCfg().getBoolean("arenas." + game + ".lock")) {
							GameUtils.setStatus(GameStatus.NOTREADY);
						} else
							GameUtils.setStatus(GameStatus.READY);

						Debug.logConsole("Loaded " + game + " game!");
					}
				}
			}

			if (conf.getCfg().getBoolean("metrics")) {
				Metrics metrics = new Metrics(this);
				metrics.addCustomChart(
						new Metrics.SimplePie("games_amount", () -> GetGames.getConfigGamesCount() + ""));

				metrics.addCustomChart(new Metrics.SimplePie("total_players", () -> {
					int totalPlayers = 0;
					if (conf.getCfg().getString("statistics").equals("mysql"))
						totalPlayers = MySQLStats.getAllPlayerStatistics().size();
					else if (conf.getCfg().getString("statistics").equals("yaml"))
						totalPlayers = YAMLStats.getAllPlayerStatistics().size();

					totalPlayers++;

					return String.valueOf(totalPlayers);
				}));

				metrics.addCustomChart(new Metrics.SimplePie("statistic_type", () -> conf.getCfg().getString("statistics")));

				Debug.logConsole("Metrics enabled.");
			}
		} catch (Throwable e) {
			e.printStackTrace();
			Debug.throwMsg();
		}
	}

	@Override
	public void onDisable() {
		if (instance == null) return;

		Thread thread = new Thread(() -> {
			Debug.logConsole("Searching games to stop...");

			String[] games = GetGames.getGameNames();
			if (games != null) {
				int i = 0;
				int imax = games.length;

				while (i < imax) {
					if (games[i] != null && PlayerList.isGameRunning(games[i])) {
						Debug.logConsole("Stopping " + games[i] + " ...");

						RageScores.calculateWinner(games[i], PlayerList.getPlayersFromList());

						for (java.util.Map.Entry<String, String> players : PlayerList.getPlayers().entrySet()) {
							org.bukkit.entity.Player p = Bukkit.getPlayer(UUID.fromString(players.getValue()));

							p.removeMetadata("killedWith", instance);
							PlayerList.removePlayer(p);
							RageScores.removePointsForPlayer(players.getValue());
						}

						PlayerList.specPlayer.values().forEach(PlayerList::removeSpectatorPlayer);

						PlayerList.setGameNotRunning(games[i]);
						GameUtils.setStatus(GameStatus.STOPPED);

						Debug.logConsole(games[i] + " has been stopped.");
					}
					i++;
				}
			}

			getServer().getScheduler().cancelTasks(instance);
			HandlerList.unregisterAll(this);
			sign = null;
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
		YAMLStats.loadPlayerStatistics();

		RuntimeRPPManager.getRPPListFromYAML();
	}

	public void connectMySQL() {
		String host = conf.getCfg().getString("MySQL.host");
		String port = conf.getCfg().getString("MySQL.port");
		String database = conf.getCfg().getString("MySQL.database");
		String username = conf.getCfg().getString("MySQL.username");
		String password = conf.getCfg().getString("MySQL.password");
		String characterEnc = conf.getCfg().getString("MySQL.character-encoding").equals("") ? "UTF-8"
				: conf.getCfg().getString("MySQL.character-encoding");
		String prefix = conf.getCfg().getString("MySQL.table-prefix").equals("") ? "rm_"
				: conf.getCfg().getString("MySQL.table-prefix");
		boolean serverCertificate = conf.getCfg().getBoolean("MySQL.verify-server-certificate");
		boolean useUnicode = conf.getCfg().getBoolean("MySQL.use-unicode");
		boolean autoReconnect = conf.getCfg().getBoolean("MySQL.auto-reconnect");
		boolean useSSL = conf.getCfg().getBoolean("MySQL.use-SSL");

		if (mySQLConnect != null) {
			MySQLStats.loadPlayerStatistics(mySQLConnect);

			RuntimeRPPManager.getRPPListFromMySQL();
		} else {
			mySQLConnect = new MySQLConnect(host, port, database, username, password, serverCertificate,
					useUnicode, characterEnc, autoReconnect, useSSL, prefix);
		}
	}

	private void initEconomy() {
		RegisteredServiceProvider<Economy> rsp = getServer().getServicesManager().getRegistration(Economy.class);
		if (rsp == null)
			return;

		econ = rsp.getProvider();
		if (econ == null)
			return;
	}

	public synchronized void reload() {
		HandlerList.unregisterAll(this);

		if (signTask != null) {
			signTask.cancel();
			signTask = null;
		}

		conf.loadConfig();
		lang.loadLanguage(conf.getCfg().getString("language"));

		if (conf.getArenasCfg().contains("arenas")) {
			spawns.clear();

			StopGame.stopAllGames();

			for (String game : GetGames.getGameNames()) {
				if (game != null) {
					if (PlayerList.isGameRunning(game)) {
						GameUtils.broadcastToGame(game, RageMode.getLang().get("game.game-stopped-for-reload"));
					}

					if (conf.getCfg().getBoolean("bungee.enable"))
						getManager().registerEvents(new BungeeListener(game), this);

					spawns.add(new GameSpawnGetter(game));

					SignCreator.updateAllSigns(game);
				}
			}
		}

		if (conf.getCfg().getBoolean("signs.enable")) {
			getManager().registerEvents(sign, this);
			SignConfiguration.initSignConfiguration();

			signTask = Bukkit.getScheduler().runTaskLater(this, sign, 40L);
		}

		getManager().registerEvents(new EventListener(this), this);
		if (Version.isCurrentEqualOrLower(Version.v1_8_R3))
			getManager().registerEvents(new Listeners_1_8(), this);
		else
			getManager().registerEvents(new Listeners_1_9(), this);

		if (conf.getCfg().getString("statistics").equals("") || conf.getCfg().getString("statistics").equals("yaml")) {
			YAMLStats.initS();
			YAMLStats.loadPlayerStatistics();
		} else if (conf.getCfg().getString("statistics").equals("mysql")) {
			if (!mySQLConnect.isConnected()) {
				connectMySQL();
			} else {
				MySQLStats.loadPlayerStatistics(mySQLConnect);
			}
		}

		if (hologram)
			HoloHolder.initHoloHolder();
	}

	private void registerCommands() {
		RmCommand rm = new RmCommand();
		getCommand("ragemode").setExecutor(rm);
		getCommand("ragemode").setTabCompleter(rm);
	}

	public File getFolder() {
		File dataFolder = getDataFolder();
		if (!dataFolder.exists())
			dataFolder.mkdir();

		return dataFolder;
	}

	/**
	 * Gets the plugin instance
	 * @return RageMode instance
	 */
	public static RageMode getInstance() {
		return instance;
	}

	/**
	 * Gets the {@link MySQLConnect} class
	 * @return mySQLConnect class
	 */
	public static MySQLConnect getMySQL() {
		return mySQLConnect;
	}

	/**
	 * Gets the {@link Language} class
	 * @return Language class
	 */
	public static Language getLang() {
		return lang;
	}

	/**
	 * Gets the {@link MinecraftVersion} class
	 * @return MinecraftVersion class
	 */
	public static MinecraftVersion getMCVersion() {
		return mcVersion;
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

	public BungeeUtils getBungeeUtils() {
		return bungee;
	}

	public SignScheduler getSignScheduler() {
		return sign;
	}

	public BukkitTask getSignTask() {
		return signTask;
	}

	public List<GameSpawnGetter> getSpawns() {
		return spawns;
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
				Debug.logConsole(Level.WARNING, "You are using an older Java that is not supported. Please use 1.8 or higher versions!");
				return false;
			}
		} catch (NumberFormatException e) {
			Debug.logConsole(Level.WARNING, "Failed to detect Java version.");
			return false;
		}
		return true;
	}
}
