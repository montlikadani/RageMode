package hu.montlikadani.ragemode;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;
import java.util.logging.Level;
import java.util.stream.Stream;

import org.bukkit.Material;
import org.bukkit.event.HandlerList;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

import com.google.common.base.StandardSystemProperty;

import hu.montlikadani.ragemode.ServerVersion.Version;
import hu.montlikadani.ragemode.commands.RmCommand;
import hu.montlikadani.ragemode.commands.RmTabCompleter;
import hu.montlikadani.ragemode.config.ConfigValues;
import hu.montlikadani.ragemode.config.Configuration;
import hu.montlikadani.ragemode.config.Language;
import hu.montlikadani.ragemode.database.MySQLConnect;
import hu.montlikadani.ragemode.database.SQLConnect;
import hu.montlikadani.ragemode.events.BungeeListener;
import hu.montlikadani.ragemode.events.EventListener;
import hu.montlikadani.ragemode.events.GameListener;
import hu.montlikadani.ragemode.events.Listeners_1_8;
import hu.montlikadani.ragemode.events.Listeners_1_9;
import hu.montlikadani.ragemode.gameLogic.Game;
import hu.montlikadani.ragemode.gameLogic.GameSpawn;
import hu.montlikadani.ragemode.gameLogic.GameStatus;
import hu.montlikadani.ragemode.gameUtils.BungeeUtils;
import hu.montlikadani.ragemode.gameUtils.GameUtils;
import hu.montlikadani.ragemode.gameUtils.GetGames;
import hu.montlikadani.ragemode.holder.HoloHolder;
import hu.montlikadani.ragemode.items.ItemHandler;
import hu.montlikadani.ragemode.managers.BossbarManager;
import hu.montlikadani.ragemode.metrics.Metrics;
import hu.montlikadani.ragemode.runtimePP.RuntimePPManager;
import hu.montlikadani.ragemode.scores.PlayerPoints;
import hu.montlikadani.ragemode.signs.SignConfiguration;
import hu.montlikadani.ragemode.signs.SignCreator;
import hu.montlikadani.ragemode.storage.MySQLDB;
import hu.montlikadani.ragemode.storage.SQLDB;
import hu.montlikadani.ragemode.storage.YAMLDB;
import net.milkbowl.vault.economy.Economy;

public class RageMode extends JavaPlugin {

	private Configuration conf = null;
	private BungeeUtils bungee = null;
	private BossbarManager bossManager = null;

	private static RageMode instance = null;
	private static Language lang = null;
	private static MySQLConnect mySQLConnect = null;
	private static SQLConnect sqlConnect = null;
	private static ServerVersion serverVersion = null;

	private Economy econ = null;

	private boolean hologram = false;
	private boolean vault = false;
	private static boolean isSpigot = false;

	private final List<Game> games = new ArrayList<>();
	private final List<GameSpawn> spawns = new ArrayList<>();
	private final ItemHandler[] gameItems = new ItemHandler[5];
	private final ItemHandler[] lobbyItems = new ItemHandler[2];

	@Override
	public void onEnable() {
		instance = this;

		try {
			if (!checkJavaVersion()) {
				getManager().disablePlugin(this);
				return;
			}

			serverVersion = new ServerVersion();

			if (Version.isCurrentLower(Version.v1_8_R1)) {
				getLogger().log(Level.SEVERE, "[RageMode] This version is not supported by this plugin! Please use larger 1.8+");
				getManager().disablePlugin(this);
				return;
			}

			try {
				Class.forName("org.spigotmc.SpigotConfig");
				isSpigot = true;
			} catch (ClassNotFoundException c) {
				isSpigot = false;
			}

			if (Version.isCurrentEqualOrLower(Version.v1_8_R3))
				getLogger().log(Level.INFO, "[RageMode] This version not fully supported by this plugin, so some options will not work.");

			conf = new Configuration(this);
			conf.loadConfig();

			lang = new Language(this);
			lang.loadLanguage(ConfigValues.getLang());

			loadHooks();

			if (ConfigValues.isBungee()) {
				bungee = new BungeeUtils(this);
				getServer().getMessenger().registerOutgoingPluginChannel(this, "BungeeCord");
			}

			if (ConfigValues.isCheckForUpdates()) {
				Debug.logConsole(checkVersion("console"));
			}

			registerListeners();
			registerCommands();

			switch (ConfigValues.getDatabaseType()) {
			case "mysql":
				connectMySQL();
				break;
			case "sql":
			case "sqlite":
				connectSQL();
				break;
			default:
				initYamlDatabase();
				break;
			}

			RuntimePPManager.loadPPListFromDatabase();
			loadGames();

			bossManager = new BossbarManager(this);

			if (ConfigValues.isSignsEnable()) {
				SignConfiguration.initSignConfiguration();
				SignCreator.loadSigns();
			}

			Metrics metrics = new Metrics(this, 5076);
			if (metrics.isEnabled()) {
				metrics.addCustomChart(new Metrics.SingleLineChart("amount_of_games", games::size));

				metrics.addCustomChart(new Metrics.SimplePie("total_players", () -> {
					int totalPlayers = 0;
					switch (ConfigValues.getDatabaseType()) {
					case "mysql":
						totalPlayers = MySQLDB.getAllPlayerStatistics().size();
						break;
					case "sql":
					case "sqlite":
						totalPlayers = SQLDB.getAllPlayerStatistics().size();
						break;
					case "yaml":
						totalPlayers = YAMLDB.getAllPlayerStatistics().size();
						break;
					default:
						break;
					}

					return String.valueOf(totalPlayers);
				}));

				metrics.addCustomChart(new Metrics.SimplePie("statistic_type", ConfigValues::getDatabaseType));

				Debug.logConsole("Metrics enabled.");
			}
		} catch (Throwable e) {
			e.printStackTrace();
		}
	}

	@Override
	public void onDisable() {
		if (instance == null) return;

		GameUtils.stopAllGames();
		saveDatabase();

		getServer().getScheduler().cancelTasks(this);
		HandlerList.unregisterAll(this);
		instance = null;
	}

	private void loadHooks() {
		if (getManager().isPluginEnabled("HolographicDisplays")) {
			hologram = true;
			HoloHolder.initHoloHolder();
		} else {
			hologram = false;
		}

		if (getManager().isPluginEnabled("Vault") && initEconomy()) {
			vault = true;
		} else {
			vault = false;
		}

		if (getManager().isPluginEnabled("PlaceholderAPI")) {
			new Placeholder().register();
		}
	}

	private void initYamlDatabase() {
		YAMLDB.initFile();
		YAMLDB.loadPlayerStatistics();
		YAMLDB.loadJoinDelay();
	}

	private void saveDatabase() {
		if (!ConfigValues.isRejoinDelayEnabled() || !ConfigValues.isRememberRejoinDelay()) {
			return;
		}

		switch (ConfigValues.getDatabaseType()) {
		case "yaml":
			YAMLDB.saveJoinDelay();
			break;
		case "sql":
		case "sqlite":
			SQLDB.saveJoinDelay();
			break;
		case "mysql":
			MySQLDB.saveJoinDelay();
			break;
		default:
			break;
		}
	}

	private void connectMySQL() {
		try {
			Class.forName("com.mysql.jdbc.Driver");
		} catch (ClassNotFoundException c) {
			c.printStackTrace();
			Debug.logConsole(Level.WARNING, "Could not connect to the MySQL database. No MySql found.");
			return;
		}

		String host = ConfigValues.getHost();
		String port = ConfigValues.getPort();
		String database = ConfigValues.getDatabase();
		String username = ConfigValues.getUsername();
		String password = ConfigValues.getPassword();
		String charEncode = ConfigValues.getEncoding();
		String prefix = ConfigValues.getDatabaseTablePrefix();
		boolean serverCertificate = ConfigValues.isCertificate();
		boolean useUnicode = ConfigValues.isUnicode();
		boolean autoReconnect = ConfigValues.isAutoReconnect();
		boolean useSSL = ConfigValues.isUseSSL();

		if (charEncode.isEmpty()) {
			charEncode = "UTF-8";
		}

		if (prefix.isEmpty()) {
			prefix = "rm_";
		}

		mySQLConnect = new MySQLConnect(host, port, database, username, password, serverCertificate, useUnicode,
				charEncode, autoReconnect, useSSL, prefix);
		if (mySQLConnect == null) {
			return;
		}

		if (!mySQLConnect.isValid()) {
			return;
		}

		MySQLDB.loadPlayerStatistics();

		if (mySQLConnect.isConnected()) {
			Debug.logConsole("Successfully connected to MySQL!");
		}
	}

	private void connectSQL() {
		try {
			Class.forName("org.sqlite.JDBC");
		} catch (ClassNotFoundException c) {
			c.printStackTrace();
			Debug.logConsole(Level.WARNING, "Could not connect to the SQL database. No Sql found.");
			return;
		}

		File sqlFile = new File(getFolder(), ConfigValues.getSqlFileName() + ".db");
		if (!sqlFile.exists()) {
			try {
				sqlFile.createNewFile();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		String prefix = ConfigValues.getDatabaseTablePrefix();
		if (prefix.isEmpty()) {
			prefix = "rm_";
		}

		sqlConnect = new SQLConnect(sqlFile, prefix);
		if (sqlConnect == null) {
			return;
		}

		if (!sqlConnect.isValid()) {
			return;
		}

		SQLDB.loadPlayerStatistics();
		SQLDB.loadJoinDelay();

		if (sqlConnect.isConnected()) {
			Debug.logConsole("Successfully connected to SQL!");
		}
	}

	private void loadDatabases() {
		switch (ConfigValues.getDatabaseType()) {
		case "mysql":
			if (mySQLConnect == null || !mySQLConnect.isConnected()) {
				connectMySQL();
			} else {
				MySQLDB.loadPlayerStatistics();
			}

			break;
		case "sql":
		case "sqlite":
			if (sqlConnect == null || !sqlConnect.isConnected()) {
				connectSQL();
			} else {
				SQLDB.loadPlayerStatistics();
			}

			break;
		default:
			YAMLDB.initFile();
			YAMLDB.loadPlayerStatistics();
			break;
		}

		RuntimePPManager.loadPPListFromDatabase();
	}

	private boolean initEconomy() {
		RegisteredServiceProvider<Economy> rsp = getServer().getServicesManager().getRegistration(Economy.class);
		if (rsp == null)
			return false;

		econ = rsp.getProvider();
		return econ != null;
	}

	public synchronized boolean reload() {
		HandlerList.unregisterAll(this);

		for (Game game : games) {
			if (game == null) {
				continue;
			}

			if (game.isGameRunning()) {
				GameUtils.stopGame(game, false);
				GameUtils.broadcastToGame(game, RageMode.getLang().get("game.game-stopped-for-reload"));
			} else if (GameUtils.getStatus(game.getName()).isPresent()
					&& GameUtils.getStatus(game.getName()).get() == GameStatus.WAITING) {
				GameUtils.kickAllPlayers(game);
			}
		}

		games.clear();
		spawns.clear();

		conf.loadConfig();
		lang.loadLanguage(ConfigValues.getLang());

		loadGames();

		if (ConfigValues.isSignsEnable()) {
			SignConfiguration.initSignConfiguration();
			SignCreator.loadSigns();
		}

		registerListeners();
		loadDatabases();

		if (hologram)
			HoloHolder.initHoloHolder();

		return true;
	}

	private void registerCommands() {
		org.bukkit.command.PluginCommand cmd = getCommand("ragemode");
		cmd.setExecutor(new RmCommand());
		cmd.setTabCompleter(new RmTabCompleter());
	}

	private void registerListeners() {
		Stream.of(new EventListener(this), new GameListener(this))
				.forEach(l -> getServer().getPluginManager().registerEvents(l, this));

		if (ConfigValues.isBungee()) {
			getManager().registerEvents(new BungeeListener(), this);
		}

		if (Version.isCurrentEqualOrLower(Version.v1_8_R3))
			getManager().registerEvents(new Listeners_1_8(), this);
		else
			getManager().registerEvents(new Listeners_1_9(), this);
	}

	private void loadGames() {
		if (conf.getArenasCfg().contains("arenas")) {
			for (String game : GetGames.getGameNames()) {
				if (game == null) {
					continue;
				}

				Game g = new Game(game);
				games.add(g);

				spawns.add(new GameSpawn(g));

				// Loads the game locker
				GameUtils.setStatus(game,
						conf.getArenasCfg().getBoolean("arenas." + game + ".lock", false) ? GameStatus.NOTREADY
								: GameStatus.READY);

				Debug.logConsole("Loaded {0} game!", game);
			}
		}

		loadItems();
	}

	private void loadItems() {
		org.bukkit.configuration.file.FileConfiguration c = conf.getCfg();
		if (!c.contains("items")) {
			return;
		}

		if (c.contains("items.combatAxe")) {
			ItemHandler itemHandler = new ItemHandler();
			itemHandler.setItem(Material.IRON_AXE)
					.setDisplayName(Utils.colors(c.getString("items.combatAxe.name", "&6CombatAxe")))
					.setLore(Utils.colorList(c.getStringList("items.combatAxe.lore")))
					.setSlot(c.getInt("items.combatAxe.slot", 2)).build();
			gameItems[0] = itemHandler;
		}

		if (c.contains("items.grenade")) {
			ItemHandler itemHandler = new ItemHandler();
			itemHandler.setItem(Material.EGG)
					.setDisplayName(Utils.colors(c.getString("items.grenade.name", "&8Grenade")))
					.setCustomName(Utils.colors(c.getString("items.grenade.custom-name", "")))
					.setLore(Utils.colorList(c.getStringList("items.grenade.lore")))
					.setSlot(c.getInt("items.grenade.slot", 6)).build();
			gameItems[1] = itemHandler;
		}

		if (c.contains("items.rageArrow")) {
			ItemHandler itemHandler = new ItemHandler();
			itemHandler.setItem(Material.ARROW)
					.setDisplayName(Utils.colors(c.getString("items.rageArrow.name", "&6RageArrow")))
					.setLore(Utils.colorList(c.getStringList("items.rageArrow.lore")))
					.setSlot(c.getInt("items.rageArrow.slot", 9)).build();
			gameItems[2] = itemHandler;
		}

		if (c.contains("items.rageBow")) {
			ItemHandler itemHandler = new ItemHandler();
			itemHandler.setItem(Material.BOW)
					.setDisplayName(Utils.colors(c.getString("items.rageBow.name", "&6RageBow")))
					.setLore(Utils.colorList(c.getStringList("items.rageBow.lore")))
					.setSlot(c.getInt("items.rageBow.slot", 0))
					.build(org.bukkit.enchantments.Enchantment.ARROW_INFINITE, 1);
			gameItems[3] = itemHandler;
		}

		if (c.contains("items.rageKnife")) {
			ItemHandler itemHandler = new ItemHandler();
			itemHandler.setItem(Material.SHEARS)
					.setDisplayName(Utils.colors(c.getString("items.rageKnife.name", "&6RageKnife")))
					.setLore(Utils.colorList(c.getStringList("items.rageKnife.lore")))
					.setSlot(c.getInt("items.rageKnife.slot", 1)).build();
			gameItems[4] = itemHandler;
		}

		// Lobby items
		if (c.contains("items.force-start")) {
			ItemHandler itemHandler = new ItemHandler();
			itemHandler.setItem(Material.valueOf(c.getString("items.force-start.item")))
					.setDisplayName(Utils.colors(c.getString("items.force-start.name", "&2Force the game start")))
					.setLore(Utils.colorList(c.getStringList("items.force-start.lore")))
					.setSlot(c.getInt("items.force-start.slot", 3)).build();
			lobbyItems[0] = itemHandler;
		}

		if (c.contains("items.leavegameitem")) {
			ItemHandler itemHandler = new ItemHandler();
			itemHandler.setItem(Material.valueOf(c.getString("items.leavegameitem.item")))
					.setDisplayName(Utils.colors(c.getString("items.leavegameitem.name", "&cExit")))
					.setLore(Utils.colorList(c.getStringList("items.leavegameitem.lore")))
					.setSlot(c.getInt("items.leavegameitem.slot", 5)).build();
			lobbyItems[1] = itemHandler;
		}
	}

	public File getFolder() {
		File dataFolder = getDataFolder();
		if (!dataFolder.exists())
			dataFolder.mkdir();

		return dataFolder;
	}

	/**
	 * Gets the given player statistics from database.
	 * @param uuid Player uuid
	 * @return {@link PlayerPoints}
	 */
	public static PlayerPoints getPPFromDatabase(UUID uuid) {
		switch (ConfigValues.getDatabaseType()) {
		case "sql":
		case "sqlite":
			return SQLDB.getPlayerStatsFromData(uuid);
		case "mysql":
			return MySQLDB.getPlayerStatsFromData(uuid);
		default:
			return YAMLDB.getPlayerStatsFromData(uuid);
		}
	}

	/**
	 * Removes a game from the list.
	 * @see #removeGame(String)
	 * @param game Game
	 */
	public void removeGame(Game game) {
		removeGame(game.getName());
	}

	/**
	 * Removes a game from the list by name.
	 * @param name Game name
	 */
	public void removeGame(String name) {
		for (Iterator<Game> gt = games.iterator(); gt.hasNext();) {
			if (gt.next().getName().equalsIgnoreCase(name)) {
				gt.remove();
				break;
			}
		}
	}

	/**
	 * Removes the given game all spawns.
	 * @see #removeSpawn(String)
	 * @param game Game
	 */
	public void removeSpawn(Game game) {
		removeSpawn(game.getName());
	}

	/**
	 * Removes the given game name all spawns.
	 * @param name Game name
	 */
	public void removeSpawn(String name) {
		for (Iterator<GameSpawn> it = spawns.iterator(); it.hasNext();) {
			if (it.next().getGame().getName().equalsIgnoreCase(name)) {
				it.remove();
			}
		}
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
	 * Gets the {@link SQLConnect} class
	 * @return SQLConnect class
	 */
	public static SQLConnect getSQL() {
		return sqlConnect;
	}

	/**
	 * Gets the {@link Language} class
	 * @return Language class
	 */
	public static Language getLang() {
		return lang;
	}

	/**
	 * Gets the {@link ServerVersion} class
	 * @return ServerVersion class
	 */
	public static ServerVersion getServerVersion() {
		return serverVersion;
	}

	public static boolean isSpigot() {
		return isSpigot;
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

	public BossbarManager getBossbarManager() {
		return bossManager;
	}

	public List<Game> getGames() {
		return games;
	}

	public List<GameSpawn> getSpawns() {
		return spawns;
	}

	public ItemHandler[] getGameItems() {
		return gameItems;
	}

	public ItemHandler[] getLobbyItems() {
		return lobbyItems;
	}

	private PluginManager getManager() {
		return getServer().getPluginManager();
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

	public String checkVersion(String sender) {
		String msg = "";
		String[] nVersion;
		String[] cVersion;
		String lineWithVersion = "";
		try {
			URL githubUrl = new URL("https://raw.githubusercontent.com/montlikadani/RageMode/master/src/main/resources/plugin.yml");
			BufferedReader br = new BufferedReader(new InputStreamReader(githubUrl.openStream()));
			String s;
			while ((s = br.readLine()) != null) {
				String line = s;
				if (line.toLowerCase().contains("version")) {
					lineWithVersion = line;
					break;
				}
			}

			String versionString = lineWithVersion.split(": ")[1];
			nVersion = versionString.replaceAll("[^0-9.]", "").split("\\.");
			double newestVersionNumber = Double.parseDouble(nVersion[0] + "." + nVersion[1]);

			cVersion = getDescription().getVersion().replaceAll("[^0-9.]", "").split("\\.");
			double currentVersionNumber = Double.parseDouble(cVersion[0] + "." + cVersion[1]);

			if (newestVersionNumber > currentVersionNumber) {
				if (sender.equals("player")) {
					msg = "&8&m&l--------------------------------------------------\n"
							+ "&a A new update is available for RageMode!&4 Version:&7 " + versionString
							+ "\n&6Download:&c &nhttps://www.spigotmc.org/resources/69169/"
							+ "\n&8&m&l--------------------------------------------------";
				} else if (sender.equals("console")) {
					msg = "New version (" + versionString + ") is available at https://www.spigotmc.org/resources/69169/";
				}
			} else if (sender.equals("console")) {
				msg = "You're running the latest version.";
			}
		} catch (Throwable e) {
			e.printStackTrace();
			Debug.logConsole(Level.WARNING,
					"Failed to compare versions. Please report it here:\nhttps://github.com/montlikadani/RageMode/issues");
		}

		return msg;
	}
}
