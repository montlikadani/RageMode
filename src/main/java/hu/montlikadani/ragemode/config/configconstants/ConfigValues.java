package hu.montlikadani.ragemode.config.configconstants;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bukkit.Material;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;

import hu.montlikadani.ragemode.config.CommentedConfig;
import hu.montlikadani.ragemode.signs.SignBackgrounds;
import hu.montlikadani.ragemode.utils.ServerVersion;
import hu.montlikadani.ragemode.utils.Utils;

public class ConfigValues {

	public static String databaseType;

	private static String lang, hubName, databaseTablePrefix, mySqlDatabaseConnectionCommand, mysqlUsername,
			mysqlPassword, sqlFileName, signGameRunning, signGameWaiting, signGameFull, signGameLocked,
			tabPlayerListName, sbTitle, chatFormat;

	private static boolean developerMode, checkForUpdates, downloadUpdates, logConsole, savePlayerData, requireEmptyInv,
			bungee, signsEnable, spectatorEnable, chatEnableinLobby, playerLevelAsTimeCounter,
			playersCanJoinRandomToRunningGames, perJoinPermissions, damagePlayerFall, hidePlayerNameTag,
			cancelRedstoneActivating, cancelDoorUse, useGrenadeTrails, useArrowTrails, enableChatInGame,
			kickRandomPlayerIfJoinsVipToFullGame, switchGMForPlayers, disableAllCommandsInGameFreeze,
			enableChatAfterEnd, tabEnable, scoreboardEnable, enableChatFormat, restartServer, stopServer,
			rejoinDelayEnabled, rememberRejoinDelay, freezePlayers, waitForNextSpawnAfterZombiesAreDead,
			notifySpectatorsToLeave, allowAllCommandsForOperators, allowAllCommandsForSpecOperators;

	private static int gameFreezeTime, bowKill, axeKill, axeDeath, knifeKill, explosionKill, suicide, grenadeKill,
			respawnProtectTime, rejoinDelayHour, rejoinDelayMinute, rejoinDelaySecond, delayBeforeFirstZombiesSpawn,
			delayAfterNextZombiesSpawning, playerLives, timeBetweenMessageSending;

	private static List<String> signTextLines, scoreboardContent, tabListHeader, tabListFooter;

	private static List<ShortenedCommand> allowedInGameCommands = new ArrayList<>(),
			allowedSpectatorCommands = new ArrayList<>();

	private static final List<MessageAction> messageActions = new ArrayList<>();

	private static List<Integer> lobbyBeginTimes, lobbyTitleBeginTimes, gameEndBroadcast;

	private static final Map<TitleSettings.TitleType, TitleSettings> titleSettings = new HashMap<>();

	private static SignBackgrounds signBackground;

	private static Material selectionItem;

	public static void loadValues(CommentedConfig f) {
		titleSettings.clear();
		messageActions.clear();

		f.copyDefaults(true);

		f.addComment("developerMode", "This will allows you for example to force start a game with only 1 player.");
		f.addComment("language", "Default language. Example: en, hu, de etc.",
				"You can find all supported languages on: https://github.com/montlikadani/RageMode/wiki/Languages");
		f.addComment("log-console", "Logging console messages, like useful debug messages");
		f.addComment("check-for-updates", "Check for updates on plugin startup.");
		f.addComment("download-updates", "Download new updates to releases folder",
				"This only works if the \"check-update\" is true.");
		f.addComment("save-player-datas-to-file",
				"This saves the player data, such as inventory, effects etc. to a file.",
				"This can be useful for a random server shutdown.",
				"If this false and \"require-empty-inventory-to-join\" also false then the players ",
				"loses every items from inventory.",
				"If this is false, player data will not be stored in the file but some data will still be saved.");
		f.addComment("require-empty-inventory-to-join", "Require empty inventory to join to game?",
				"If this false and \"save-player-datas-to-file\" also false then the players ",
				"loses every items from inventory.");
		f.addComment("selection-item", "Used to select the game area to be a protected area.");
		f.addComment("bungee", "Hook into bungee");
		f.addComment("bungee.hub-name",
				"Bungeecord server name when the game end and teleport players to this server.");
		f.addComment("database", "The database where to save plugin data.");
		f.addComment("database.type", "Database types: yaml, mysql, sqlite");
		f.addComment("database.table-prefix", "The database table name", "This is only for sql databases.");
		f.addComment("database.SQL", "SQLite database settings");
		f.addComment("database.MySQL", "MySQL database connection",
				"Connection variables and documentation: https://dev.mysql.com/doc/refman/5.7/en/connecting.html");
		f.addComment("signs", "Sign texts");
		f.addComment("signs.game", "Sign text when the game running or waiting for players.");
		f.addComment("signs.list", "Sign text list (max. 4 line)",
				"Use %game%, %current-players%, %max-players%, %running% placeholder.");
		f.addComment("signs.background", "Defines a background of the sign.");
		f.addComment("signs.background.type", "Possible types: glass, wool, terracotta (clay), none");
		f.addComment("message-actions",
				"Message actions to send some actionbar or bossbar messages or execute some commands to players.",
				"Documentation: https://github.com/montlikadani/RageMode/wiki/Actionbar-&-Bossbar-actions");
		f.addComment("titles", "Title texts");
		f.addComment("titles.join-game", "When a player joins to a game it will send a title.");
		f.addComment("titles.join-game.time", "Title time settings (in ticks)",
				"(fade-in) The time it takes for the title to fade into the screen.",
				"(stay) The time it takes for the title to stay on the screen.",
				"(fade-out) The time it takes for the title to fade out of the screen.");
		f.addComment("titles.lobby-waiting", "Lobby waiting timer title");
		f.addComment("titles.lobby-waiting.begin-times",
				"Intervals when the lobby timer reaches e.g. 5 interval it will send on the screen.");
		f.addComment("titles.player-won",
				"When a player won a game it will send a title for all players that in the game currently.");
		f.addComment("titles.you-won", "When you won a game it will send a title");
		f.addComment("spectator",
				"Enables the spectator mode in the already running game, to see what happens specific game.");
		f.addComment("spectator.notify-spectators-to-leave", "Notify spectators in message, how to leave the game.");
		f.addComment("spectator.notify-spectators-to-leave.time-between-message-sending",
				"Time in seconds between sending notify message.");
		f.addComment("spectator.allowed-commands", "Which spectator commands will be allowed to use in-game?");
		f.addComment("spectator.allowed-commands.allow-all-commands-for-operators",
				"Does all the commands allowed for operator players or not?");
		f.addComment("lobby", "Lobby settings");
		f.addComment("lobby.enable-chat-in-lobby", "Should be enable chat in lobby?",
				"This option will be ignored with \"ragemode.bypass.lobby.lockchat\" permission");
		f.addComment("lobby.player-level-as-time-counter",
				"Does count the player's level with the start time of the game in the lobby?");
		f.addComment("lobby.begin-times",
				"Time intervals when the lobby timer reaches, for example, 10 seconds, write it into the chat.");
		f.addComment("players-can-join-random-to-running-games",
				"Does the player can join to running games when types /rm joinrandom command to spectate?");
		f.addComment("per-join-permissions", "Enables per join permissions for joining.",
				"The permission is \"ragemode.join.gameName\"");
		f.addComment("game", "Global settings for game.");
		f.addComment("game.damage-player-fall", "If this false if player has fallen to ground then not damage.");
		f.addComment("game.respawn-protection", "Respawn protection when player dead and respawned.",
				"Counts in seconds.", "If the value 0, ignoring the protection.");
		f.addComment("game.hide-players-name-tag", "Hide the players name tag who in game and playing?",
				"It makes the game harder, but the experience is better.");
		f.addComment("game.end-broadcast-at-times",
				"Time intervals when the game timer reaches, for example, 60 seconds, it will send to the chat.");
		f.addComment("game.cancel-redstone-activating-blocks",
				"Cancel all redstone activating blocks, such as lever, comparator, pressure plate, etc.?");
		f.addComment("game.cancel-door-use", "Cancels the door opening/closing events in game.");
		f.addComment("game.zombie-apocalypse", "Zombie apocalypse game type settings");
		f.addComment("game.zombie-apocalypse.delay-before-first-spawn",
				"Delay the spawning of zombies before the first wave? (in game current seconds)");
		f.addComment("game.zombie-apocalypse.delay-after-next-zombies-spawning",
				"Delay after the next zombie wave (in game current seconds)");
		f.addComment("game.zombie-apocalypse.wait-for-next-spawn-after-zombies-are-dead",
				"Wait for the next zombie wave to spawn until all the zombies have been killed.");
		f.addComment("game.zombie-apocalypse.player-lives",
				"Defines how many times does the player respawn after dead.",
				"If the player dead 3 times, player will not be respawned again.");
		f.addComment("game.game-freeze", "Game freeze settings");
		f.addComment("game.game-freeze.time",
				"When the game is over, the players will freeze (if the game mode is not in spectator),",
				"then if the time expires, it will be automatically returned to the set location.",
				"This ignores the winner player.", "Counting in seconds!");
		f.addComment("game.game-freeze.freeze-players", "Do freeze players completely?");
		f.addComment("game.game-freeze.switch-gamemode-to-spectator",
				"Switch the game mode to spectator when the game is end?", "This will ignores the winner player.");
		f.addComment("game.game-freeze.disable-all-commands", "Disable all commands execution?");
		f.addComment("game.game-freeze.enable-chat", "Enables chat messaging");
		f.addComment("game.use-grenade-trails", "Do we use grenade effects when a player thrown?");
		f.addComment("game.use-arrow-trails", "Do you want to allow for players to buy arrow trails",
				"from lobby shop when in game?");
		f.addComment("game.enable-chat-in-game", "Should be enable chat in the game?",
				"This ignores with ragemode.bypass.game.lockchat permission.");
		f.addComment("game.kickRandomPlayerIfJoinsVipToFullGame",
				"Kicks random player from the game if that game is full, and",
				"the joining player have permission \"ragemode.vip\".");
		f.addComment("game.tablist.player-list-name",
				"Player list name format to show for example player kills, deaths or just a custom one.",
				"Leave it empty \"\" to do not set any list name.");
		f.addComment("game.tablist.list", "Tablist header/footer");
		f.addComment("game.scoreboard", "Displays the score board on the right screen.");
		f.addComment("game.chat-format", "Chat formatting");
		f.addComment("game.allowed-commands", "The in game allowed commands, which the player can use in the game.");
		f.addComment("game.allowed-commands.allow-all-commands-for-operators",
				"Does all the commands allowed for operator players or not?");
		f.addComment("game-stop", "Stop the server or restart at the end of the game?");
		f.addComment("rejoin-delay", "Rejoin delay to add how many times a player can join to games.");
		f.addComment("rejoin-delay.remember-to-database", "Save the currently running delays to the database.",
				"The loaded delays will be removed when this setting is enabled and when the server starting.");
		f.addComment("rejoin-delay.times", "The waiting time for allowing player to join again.");
		f.addComment("points", "Points for killing or deaths");
		f.addComment("points.suicide",
				"You can set to 0 to do not decrease the points amount from player if suicides itself.");

		lang = f.get("language", "en");
		developerMode = f.get("developerMode", false);
		checkForUpdates = f.get("check-for-updates", true);
		downloadUpdates = f.get("download-updates", false);
		logConsole = f.get("log-console", true);
		savePlayerData = f.get("save-player-datas-to-file", false);
		requireEmptyInv = f.get("require-empty-inventory-to-join", true);

		selectionItem = Material.matchMaterial(f.get("selection-item", "golden_shovel"));

		bungee = f.get("bungee.enable", false);
		hubName = f.get("bungee.hub-name", "lobby");
		databaseType = f.get("database.type", "yaml");

		if ((databaseTablePrefix = f.get("database.table-prefix", "ragemode_")).isEmpty()) {
			databaseTablePrefix = "rm_";
		}

		mysqlUsername = f.get("database.MySQL.username", "accountname");
		mysqlPassword = f.get("database.MySQL.password", "password");
		mySqlDatabaseConnectionCommand = f.get("database.MySQL.server-connection-command",
				"jdbc:mysql:/localhost:3306/database?verifyServerCertificate=false&maxReconnects=1&useUnicode=true&characterEncoding=UTF-8&autoReconnect=true&useSSL=false");
		sqlFileName = f.get("database.SQL.file-name", "rm.sqlite");
		signsEnable = f.get("signs.enable", false);
		signGameRunning = f.get("signs.game.running", "&6&oRunning...");
		signGameWaiting = f.get("signs.game.waiting", "&cWaiting...");
		signGameFull = f.get("signs.game.full", "&4FULL");
		signGameLocked = f.get("signs.game.locked", "&9Locked");
		spectatorEnable = f.get("spectator.enable", true);
		notifySpectatorsToLeave = f.get("spectator.notify-spectators-to-leave.enabled", true);
		timeBetweenMessageSending = f.get("spectator.notify-spectators-to-leave.time-between-message-sending", 40);
		chatEnableinLobby = f.get("lobby.enable-chat-in-lobby", true);
		playerLevelAsTimeCounter = f.get("lobby.player-level-as-time-counter", false);
		playersCanJoinRandomToRunningGames = f.get("players-can-join-random-to-running-games", true);
		perJoinPermissions = f.get("per-join-permissions", false);
		damagePlayerFall = f.get("game.damage-player-fall", false);
		respawnProtectTime = f.get("game.respawn-protection", 3);
		hidePlayerNameTag = f.get("game.hide-players-name-tag", false);

		gameEndBroadcast = f.getIntList("game.end-broadcast-at-times", Arrays.asList(60, 30, 20, 10, 5, 4, 3, 2, 1));

		cancelRedstoneActivating = f.get("game.cancel-redstone-activating-blocks", true);
		cancelDoorUse = f.get("game.cancel-door-use", false);
		gameFreezeTime = f.get("game.game-freeze.time", 10);
		freezePlayers = f.get("game.game-freeze.freeze-players", false);
		switchGMForPlayers = f.get("game.game-freeze.switch-gamemode-to-spectator", true);
		disableAllCommandsInGameFreeze = f.get("game.game-freeze.disable-all-commands", false);
		enableChatAfterEnd = f.get("game.game-freeze.enable-chat", true);
		useGrenadeTrails = f.get("game.use-grenade-trails", true);
		useArrowTrails = f.get("game.use-arrow-trails", false);
		enableChatInGame = f.get("game.enable-chat-in-game", true);
		kickRandomPlayerIfJoinsVipToFullGame = f.get("game.kickRandomPlayerIfJoinsVipToFullGame", true);
		delayBeforeFirstZombiesSpawn = f.get("game.zombie-apocalypse.delay-before-first-spawn", 30);
		delayAfterNextZombiesSpawning = f.get("game.zombie-apocalypse.delay-after-next-zombies-spawning", 30);
		waitForNextSpawnAfterZombiesAreDead = f.get("game.zombie-apocalypse.wait-for-next-spawn-after-zombies-are-dead",
				true);
		playerLives = f.get("game.zombie-apocalypse.player-lives", 3);

		tabPlayerListName = f.get("game.tablist.player-list-name", "&c%player_name%&e %kills%");
		tabEnable = f.get("game.tablist.list.enable", false);

		tabListHeader = f.get("game.tablist.list.header",
				Arrays.asList("&cRage&bMode&e minigame stats", "&aYour kills/deaths:&e %kd%"));
		tabListFooter = f.get("game.tablist.list.footer", Arrays.asList("&6Points:&e %points%"));

		scoreboardEnable = f.get("game.scoreboard.enable", true);
		sbTitle = f.get("game.scoreboard.title", "&6RageMode");

		f.get("game.scoreboard.content", Arrays.asList("", "&7------", "&aPoints:&e %points%", "",
				"&6Remaining times:&7 %game-time%", "", "&5Wins:&e %wins%", "&7------", ""));

		enableChatFormat = f.get("game.chat-format.enable", false);
		chatFormat = f.get("game.chat-format.format", "&e[%points%]&r %player%&7:&r %message%");
		restartServer = f.get("game-stop.restart-server", false);
		stopServer = f.get("game-stop.stop-server", false);
		rejoinDelayEnabled = f.get("rejoin-delay.enabled", false);
		rememberRejoinDelay = f.get("rejoin-delay.remember-to-database", true);
		rejoinDelayHour = f.get("rejoin-delay.times.hour", 0);
		rejoinDelayMinute = f.get("rejoin-delay.times.minute", 0);
		rejoinDelaySecond = f.get("rejoin-delay.times.second", 30);
		bowKill = f.get("points.bowkill", 25);
		axeKill = f.get("points.axekill", 30);
		axeDeath = f.get("points.axedeath", -50);
		knifeKill = f.get("points.knifekill", 15);
		explosionKill = f.get("points.explosionkill", 25);
		grenadeKill = f.get("points.grenadeKill", 45);
		suicide = f.get("points.suicide", -20);

		signTextLines = f.get("signs.list", Arrays.asList("&3[&5RageMode&3]", "&a%game%",
				"&ePlayers&3 [%current-players%/%max-players%&3]", "%running%"));
		lobbyTitleBeginTimes = f.getIntList("titles.lobby-waiting.begin-times", Arrays.asList(5, 4, 3, 2, 1));
		lobbyBeginTimes = f.getIntList("lobby.begin-times", Arrays.asList(30, 20, 10, 5, 4, 3, 2, 1));

		allowAllCommandsForSpecOperators = f.get("spectator.allowed-commands.allow-all-commands-for-operators", true);

		for (String l : f.get("spectator.allowed-commands.list",
				Arrays.asList("/rm;ragemode leave", "/ragemode;rm stopgame"))) {
			allowedSpectatorCommands.add(new ShortenedCommand(l));
		}

		allowAllCommandsForOperators = f.get("game.allowed-commands.allow-all-commands-for-operators", true);

		for (String line : f.get("game.allowed-commands.list",
				Arrays.asList("/rm;ragemode leave", "/ragemode;rm stopgame", "/rm;ragemode listplayers",
						"/rm;ragemode listgames", "/rm;ragemode kick", "stop", "/rm;ragemode stats"))) {
			allowedInGameCommands.add(new ShortenedCommand(line));
		}

		try {
			signBackground = SignBackgrounds.valueOf(f.get("signs.background.type", "none").toUpperCase());
		} catch (IllegalArgumentException e) {
			signBackground = SignBackgrounds.NONE;
		}

		scoreboardContent = f.get("game.scoreboard.content", Arrays.asList(""));

		for (String action : f.get("message-actions",
				Arrays.asList("[actionbar];join:&cHello&a %player%&c in this game!",
						"[actionbar];start:&aGame has started!&2 We wish you a good game!",
						"[bossbar];start:&bWelcome&a %player%&b to the&c %game%&b game!:green:segmented_6:8"))) {
			messageActions.add(new MessageAction(action));
		}

		for (TitleSettings.TitleType type : TitleSettings.TitleType.values()) {
			titleSettings.put(type, new TitleSettings(f, type));
		}

		f.save();
		f.cleanUp();
		f.save();
	}

	public static boolean isCommandAllowed(boolean spectator, String message) {
		for (ShortenedCommand allowed : spectator ? allowedSpectatorCommands : allowedInGameCommands) {
			for (String cmd : allowed.getCommandWithAliases()) {
				if (org.apache.commons.lang.StringUtils.containsIgnoreCase(message, cmd)) {
					return true;
				}
			}
		}

		return false;
	}

	public static final class ShortenedCommand {

		private String[] commandWithAliases;

		public ShortenedCommand(String line) {
			String[] split = line.split(" ", 2);

			if (split.length < 2) {
				commandWithAliases = new String[] { line };
				return;
			}

			String[] com = line.split(";", 2);

			if (com.length < 2) {
				commandWithAliases = new String[] { line };
				return;
			}

			commandWithAliases = new String[com.length];

			String sub = split[1];

			for (int i = 0; i < com.length; i++) {
				String aliase = com[i];

				int indexOf = aliase.indexOf(' ');
				if (indexOf >= 0) {
					aliase = aliase.substring(0, indexOf);
				}

				commandWithAliases[i] = (i != 0 ? '/' : "") + aliase + ' ' + sub;
			}
		}

		public String[] getCommandWithAliases() {
			return commandWithAliases;
		}
	}

	public static final class TitleSettings {

		private String title = "", subTitle = "";

		private final int[] times = new int[] { 0, 20, 5 };

		public TitleSettings(CommentedConfig c, TitleType type) {
			title = c.get("titles." + type.name + ".title", "");
			subTitle = c.get("titles." + type.name + ".subtitle", "");

			String[] split = c.get("titles." + type.name + ".time", "").split(", ", times.length);

			for (int i = 0; i < times.length; i++) {
				try {
					this.times[i] = Integer.parseInt(split[i]);
				} catch (NumberFormatException e) {
				}
			}
		}

		public String getTitle() {
			return title;
		}

		public String getSubTitle() {
			return subTitle;
		}

		public int[] getTimes() {
			return times;
		}

		public enum TitleType {
			JOIN_GAME, LOBBY_WAITING, PLAYER_WON, YOU_WON;

			private String name = name().toLowerCase().replace('_', '-');

			@Override
			public String toString() {
				return name;
			}
		}
	}

	public static final class MessageAction {

		private ActionMessageType.MessageTypes type;
		private ActionMessageType action;
		private String message;

		private BossBarSettings bossSettings;

		public MessageAction(String one) {
			String[] actionSplit = one.replace("[", "").split("];", 2);
			if (actionSplit.length == 0) {
				return;
			}

			try {
				type = ActionMessageType.MessageTypes.valueOf(actionSplit[0].toUpperCase());
			} catch (IllegalArgumentException e) {
				type = ActionMessageType.MessageTypes.ACTIONBAR;
			}

			String[] split = one.split(":", 5);
			if (split.length < 2) {
				return;
			}

			try {
				action = ActionMessageType.valueOf(split[0].toUpperCase());
			} catch (IllegalArgumentException e) {
				return;
			}

			message = split[1];

			if (type == ActionMessageType.MessageTypes.BOSSBAR) {
				if (action == ActionMessageType.LEAVE || action == ActionMessageType.KICK
						|| ServerVersion.isCurrentLower(ServerVersion.v1_9_R1)) {
					return; // We don't allow leave action for boss bar, it will be unused and old version
							// is not supported
				}

				BarColor color = BarColor.BLUE;

				if (split.length > 2) {
					try {
						color = BarColor.valueOf(split[2].toUpperCase());
					} catch (IllegalArgumentException e) {
					}
				}

				BarStyle style = BarStyle.SOLID;

				if (split.length > 3) {
					try {
						style = BarStyle.valueOf(split[3].toUpperCase());
					} catch (IllegalArgumentException e) {
					}
				}

				int bossExpirementSeconds = 6;
				if (split.length > 4) {
					bossExpirementSeconds = Utils.tryParseInt(split[4]).orElse(6);
				}

				bossSettings = new BossBarSettings(color, style, bossExpirementSeconds);
			} else if (type == ActionMessageType.MessageTypes.COMMAND) {
				if (message.charAt(0) == '/') {
					message = message.substring(1, message.length());
				}
			}
		}

		public ActionMessageType.MessageTypes getType() {
			return type;
		}

		public ActionMessageType getAction() {
			return action;
		}

		public String getMessage() {
			return message;
		}

		public BossBarSettings getBossSettings() {
			return bossSettings;
		}

		// sooo many inner classes

		// This class is for handling the boss bar enum exception and avoiding other
		// conflicts.
		public final class BossBarSettings {

			private BarColor color;
			private BarStyle style;
			private int bossExpirementSeconds;

			public BossBarSettings(BarColor color, BarStyle style, int bossExpirementSeconds) {
				this.color = color;
				this.style = style;
				this.bossExpirementSeconds = bossExpirementSeconds;
			}

			public BarColor getColor() {
				return color;
			}

			public BarStyle getStyle() {
				return style;
			}

			public int getBossExpirementSeconds() {
				return bossExpirementSeconds;
			}
		}

		/**
		 * The enum for send various message types, like bossbar to player when
		 * something happen in the game
		 */
		public enum ActionMessageType {
			JOIN, LEAVE, START, STOP, KICK;

			private String name = name().toLowerCase(java.util.Locale.ENGLISH);

			@Override
			public String toString() {
				return name;
			}

			public enum MessageTypes {
				ACTIONBAR, BOSSBAR, COMMAND
			}
		}
	}

	public static boolean isDeveloperMode() {
		return developerMode;
	}

	public static String getLang() {
		return lang;
	}

	public static String getHubName() {
		return hubName;
	}

	public static Material getSelectionItem() {
		return selectionItem;
	}

	public static boolean isLogConsole() {
		return logConsole;
	}

	public static boolean isCheckForUpdates() {
		return checkForUpdates;
	}

	public static boolean isDownloadUpdates() {
		return downloadUpdates;
	}

	public static boolean isSavePlayerData() {
		return savePlayerData;
	}

	public static boolean isRequireEmptyInv() {
		return requireEmptyInv;
	}

	public static boolean isBungee() {
		return bungee;
	}

	public static String getSqlFileName() {
		return sqlFileName;
	}

	public static String getDatabaseTablePrefix() {
		return databaseTablePrefix;
	}

	public static boolean isSignsEnable() {
		return signsEnable;
	}

	public static String getSignGameRunning() {
		return signGameRunning;
	}

	public static String getSignGameWaiting() {
		return signGameWaiting;
	}

	public static String getSignGameFull() {
		return signGameFull;
	}

	public static String getSignGameLocked() {
		return signGameLocked;
	}

	public static SignBackgrounds getSignBackground() {
		return signBackground;
	}

	public static boolean isSpectatorEnabled() {
		return spectatorEnable;
	}

	public static boolean isChatEnabledinLobby() {
		return chatEnableinLobby;
	}

	public static boolean isPlayerLevelAsTimeCounter() {
		return playerLevelAsTimeCounter;
	}

	public static boolean isPlayersCanJoinRandomToRunningGames() {
		return playersCanJoinRandomToRunningGames;
	}

	public static boolean isPerJoinPermissions() {
		return perJoinPermissions;
	}

	public static boolean isDamagePlayerFall() {
		return damagePlayerFall;
	}

	public static int getRespawnProtectTime() {
		return respawnProtectTime;
	}

	public static boolean isCancelRedstoneActivate() {
		return cancelRedstoneActivating;
	}

	public static boolean isCancelDoorUse() {
		return cancelDoorUse;
	}

	public static boolean isUseGrenadeTrails() {
		return useGrenadeTrails;
	}

	public static boolean isUseArrowTrails() {
		return useArrowTrails;
	}

	public static int getGameFreezeTime() {
		return gameFreezeTime;
	}

	public static boolean isEnableChatInGame() {
		return enableChatInGame;
	}

	public static boolean isKickRandomPlayerIfJoinsVip() {
		return kickRandomPlayerIfJoinsVipToFullGame;
	}

	public static boolean isSwitchGMForPlayers() {
		return switchGMForPlayers;
	}

	public static boolean isCommandsDisabledInEndGame() {
		return disableAllCommandsInGameFreeze;
	}

	public static boolean isEnableChatAfterEnd() {
		return enableChatAfterEnd;
	}

	public static boolean isTabEnabled() {
		return tabEnable;
	}

	public static boolean isScoreboardEnabled() {
		return scoreboardEnable;
	}

	public static String getSbTitle() {
		return sbTitle;
	}

	public static boolean isChatFormatEnabled() {
		return enableChatFormat;
	}

	public static String getChatFormat() {
		return chatFormat;
	}

	public static boolean isRestartServerEnabled() {
		return restartServer;
	}

	public static boolean isStopServerEnabled() {
		return stopServer;
	}

	public static boolean isRejoinDelayEnabled() {
		return rejoinDelayEnabled;
	}

	public static boolean isRememberRejoinDelay() {
		return rememberRejoinDelay;
	}

	public static int getRejoinDelayHour() {
		return rejoinDelayHour;
	}

	public static int getRejoinDelayMinute() {
		return rejoinDelayMinute;
	}

	public static int getRejoinDelaySecond() {
		return rejoinDelaySecond;
	}

	public static int getBowKill() {
		return bowKill;
	}

	public static int getAxeKill() {
		return axeKill;
	}

	public static int getAxeDeath() {
		return axeDeath;
	}

	public static int getKnifeKill() {
		return knifeKill;
	}

	public static int getExplosionKill() {
		return explosionKill;
	}

	public static int getGrenadeKill() {
		return grenadeKill;
	}

	public static int getSuicide() {
		return suicide;
	}

	public static boolean isHidePlayerNameTag() {
		return hidePlayerNameTag;
	}

	public static boolean isFreezePlayers() {
		return freezePlayers;
	}

	public static int getDelayBeforeFirstZombiesSpawn() {
		return delayBeforeFirstZombiesSpawn;
	}

	public static int getDelayAfterNextZombiesSpawning() {
		return delayAfterNextZombiesSpawning;
	}

	public static boolean isWaitForNextSpawnAfterZombiesAreDead() {
		return waitForNextSpawnAfterZombiesAreDead;
	}

	public static int getPlayerLives() {
		return playerLives;
	}

	public static boolean isNotifySpectatorsToLeave() {
		return notifySpectatorsToLeave;
	}

	public static int getSpecTimeBetweenMessageSending() {
		return timeBetweenMessageSending;
	}

	public static List<String> getSignTextLines() {
		return signTextLines;
	}

	public static List<ShortenedCommand> getAllowedSpectatorCommands() {
		return allowedSpectatorCommands;
	}

	public static List<ShortenedCommand> getAllowedInGameCommands() {
		return allowedInGameCommands;
	}

	public static boolean isAllowAllCommandsForOperators() {
		return allowAllCommandsForOperators;
	}

	public static boolean isAllowAllCommandsForSpecOperators() {
		return allowAllCommandsForSpecOperators;
	}

	public static List<Integer> getGameEndBroadcast() {
		return gameEndBroadcast;
	}

	public static List<Integer> getLobbyBeginTimes() {
		return lobbyBeginTimes;
	}

	public static List<Integer> getLobbyTitleBeginTimes() {
		return lobbyTitleBeginTimes;
	}

	public static List<MessageAction> getMessageActions() {
		return messageActions;
	}

	public static List<String> getScoreboardContent() {
		return scoreboardContent;
	}

	public static List<String> getTabListHeader() {
		return tabListHeader;
	}

	public static List<String> getTabListFooter() {
		return tabListFooter;
	}

	public static Map<TitleSettings.TitleType, TitleSettings> getTitleSettings() {
		return titleSettings;
	}

	public static String getTabPlayerListName() {
		return tabPlayerListName;
	}

	public static String getMySqlDatabaseConnectionCommand() {
		return mySqlDatabaseConnectionCommand;
	}

	public static String getMysqlUsername() {
		return mysqlUsername;
	}

	public static String getMysqlPassword() {
		return mysqlPassword;
	}
}
