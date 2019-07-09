package hu.montlikadani.ragemode;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.logging.Level;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

import com.google.common.base.StandardSystemProperty;

import hu.montlikadani.ragemode.metrics.Metrics;
import hu.montlikadani.ragemode.MinecraftVersion.Version;
import hu.montlikadani.ragemode.commands.AddGame;
import hu.montlikadani.ragemode.commands.AddSpawn;
import hu.montlikadani.ragemode.commands.ForceStart;
import hu.montlikadani.ragemode.commands.HoloStats;
import hu.montlikadani.ragemode.commands.KickPlayer;
import hu.montlikadani.ragemode.commands.ListGames;
import hu.montlikadani.ragemode.commands.PlayerJoin;
import hu.montlikadani.ragemode.commands.PlayerLeave;
import hu.montlikadani.ragemode.commands.Points;
import hu.montlikadani.ragemode.commands.Reload;
import hu.montlikadani.ragemode.commands.RemoveGame;
import hu.montlikadani.ragemode.commands.RemoveSpawn;
import hu.montlikadani.ragemode.commands.ResetPlayerStats;
import hu.montlikadani.ragemode.commands.RmCommand;
import hu.montlikadani.ragemode.commands.SetActionBar;
import hu.montlikadani.ragemode.commands.SetBossBar;
import hu.montlikadani.ragemode.commands.SetGameTime;
import hu.montlikadani.ragemode.commands.SetGlobalMessages;
import hu.montlikadani.ragemode.commands.SetLobby;
import hu.montlikadani.ragemode.commands.SetLobbyDelay;
import hu.montlikadani.ragemode.commands.ShowStats;
import hu.montlikadani.ragemode.commands.SignUpdate;
import hu.montlikadani.ragemode.commands.Spectate;
import hu.montlikadani.ragemode.commands.StopGame;
import hu.montlikadani.ragemode.commands.ToggleGame;
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
import hu.montlikadani.ragemode.gameUtils.ActionBar;
import hu.montlikadani.ragemode.gameUtils.BungeeUtils;
import hu.montlikadani.ragemode.gameUtils.GameUtils;
import hu.montlikadani.ragemode.gameUtils.GetGames;
import hu.montlikadani.ragemode.holder.HoloHolder;
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

			lang = new Language(this, conf.getCfg().getString("language"));
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

			if (conf.getCfg().getBoolean("bungee.enable")) {
				bungee = new BungeeUtils(this);
				getServer().getMessenger().registerOutgoingPluginChannel(this, "BungeeCord");
			}

			initActionBar();

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

			RageScores.load();

			if (conf.getCfg().getBoolean("signs.enable")) {
				sign = new SignScheduler(this);
				getManager().registerEvents(sign, this);

				SignConfiguration.initSignConfiguration();
				SignCreator.loadSigns();

				Bukkit.getScheduler().runTaskLater(this, sign, 40L);
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

						logConsole("[RageMode] Loaded " + game + " game!");
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

				logConsole("[RageMode] Metrics enabled.");
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
			logConsole("[RageMode] Searching games to stop...");

			String[] games = GetGames.getGameNames();
			if (games != null) {
				int i = 0;
				int imax = games.length;

				while (i < imax) {
					if (games[i] != null && PlayerList.isGameRunning(games[i])) {
						logConsole("[RageMode] Stopping " + games[i] + " ...");

						String[] players = PlayerList.getPlayersInGame(games[i]);
						RageScores.calculateWinner(games[i], players);

						if (players != null) {
							int n = 0;
							int nmax = players.length;

							while (n < nmax) {
								if (players[n] != null) {
									PlayerList.removePlayerSynced(Bukkit.getPlayer(UUID.fromString(players[n])));
									PlayerList.removePlayer(Bukkit.getPlayer(UUID.fromString(players[n])));
								}
								n++;
							}
						}
						for (Player key : PlayerList.specPlayer.values()) {
							PlayerList.removeSpectatorPlayer(key);
						}
						RageScores.removePointsForPlayers(players);

						PlayerList.setGameNotRunning(games[i]);
						GameUtils.setStatus(GameStatus.STOPPED);

						logConsole("[RageMode] " + games[i] + " has been stopped.");
					}
					i++;
				}
			}

			sign = null;
			getServer().getScheduler().cancelTasks(instance);
			HandlerList.unregisterAll(this);
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

		mySQLConnect = new MySQLConnect(host, port, database, username, password, serverCertificate,
				useUnicode, characterEnc, autoReconnect, useSSL, prefix);

		if (mySQLConnect != null)
			Bukkit.getServer().getScheduler().runTaskAsynchronously(this,
					() -> RuntimeRPPManager.getRPPListFromMySQL());
	}

	private void initActionBar() {
		ActionBar.nmsver = Bukkit.getServer().getClass().getPackage().getName();
		ActionBar.nmsver = ActionBar.nmsver.substring(ActionBar.nmsver.lastIndexOf(".") + 1);

		if (ActionBar.nmsver.equalsIgnoreCase("v1_8_") && ActionBar.nmsver.equalsIgnoreCase("v1_9_")
				&& ActionBar.nmsver.equalsIgnoreCase("v1_10_") && ActionBar.nmsver.equalsIgnoreCase("v1_11_")
				&& ActionBar.nmsver.equalsIgnoreCase("v1_12_") && ActionBar.nmsver.equalsIgnoreCase("v1_13_")
				&& ActionBar.nmsver.equalsIgnoreCase("v1_14_"))
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

	public synchronized void loadListeners() {
		HandlerList.unregisterAll(this);

		if (conf.getCfg().getBoolean("bungee.enable")) {
			if (conf.getArenasCfg().contains("arenas"))
				for (String game : GetGames.getGameNames()) {
					if (game != null)
						getManager().registerEvents(new BungeeListener(game), this);
				}
		}

		if (conf.getCfg().getBoolean("signs.enable"))
			getManager().registerEvents(sign, this);

		getManager().registerEvents(new EventListener(this), this);
		if (Version.isCurrentEqualOrLower(Version.v1_8_R3))
			getManager().registerEvents(new Listeners_1_8(), this);
		else
			getManager().registerEvents(new Listeners_1_9(), this);
	}

	private void registerCommands() {
		//TODO short this if possible, maybe?
		new AddGame();
		new AddSpawn();
		new RemoveSpawn();
		new ForceStart();
		new HoloStats();
		new KickPlayer();
		new ListGames();
		new PlayerJoin();
		new PlayerLeave();
		new Points();
		new Reload();
		new RemoveGame();
		new ResetPlayerStats();
		new SetActionBar();
		new SetBossBar();
		new SetGameTime();
		new SetGlobalMessages();
		new SetLobby();
		new SetLobbyDelay();
		new ShowStats();
		new SignUpdate();
		new Spectate();
		new StopGame();
		new ToggleGame();

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
	 * 
	 * @return RageMode instance
	 */
	public static RageMode getInstance() {
		return instance;
	}

	/**
	 * Gets the {@link MySQLConnect} class
	 * 
	 * @return mySQLConnect class
	 */
	public static MySQLConnect getMySQL() {
		return mySQLConnect;
	}

	/**
	 * Gets the {@link Language} class
	 * 
	 * @return Language class
	 */
	public static Language getLang() {
		return lang;
	}

	/**
	 * Gets the {@link MinecraftVersion} class
	 * 
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

	public List<GameSpawnGetter> getSpawns() {
		return spawns;
	}

	public void throwMsg() {
		logConsole(Level.WARNING, "[RageMode] There was an error. Please report it here:\nhttps://github.com/montlikadani/RageMode/issues");
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
