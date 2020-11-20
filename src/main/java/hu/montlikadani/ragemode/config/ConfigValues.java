package hu.montlikadani.ragemode.config;

import java.util.Arrays;
import java.util.List;

/**
 * @author montlikadani
 */
public class ConfigValues {

	public static String databaseType;

	private static String lang, hubName, selectionItem, databaseTablePrefix, username, password, database, host, port,
			encoding, sqlFileName, signGameRunning, signGameWaiting, signGameFull, signGameLocked, signBackgrType,
			titleJoinGame, subtitleJoinGame, joinTitleTime, lobbyTitle, subtitleLobby, lobbyTitleTime, wonTitle,
			wonsubtitle, wonTitleTime, youwonTitle, youwonsubtitle, youwonTitleTime, tabPrefix, tabSuffix, sbTitle,
			chatFormat;

	private static boolean checkForUpdates, downloadUpdates, logConsole, savePlayerData, requireEmptyInv, bungee,
			autoReconnect, useSSL, unicode, certificate, signsEnable, enableLobbyTitle, spectatorEnable,
			chatEnableinLobby, playerLevelAsTimeCounter, playersCanJoinRandomToRunningGames, perJoinPermissions,
			damagePlayerFall, preventFastBowEvent, hidePlayerNameTag, cancelRedstoneActivating, cancelDoorUse,
			useGrenadeTrails, useArrowTrails, enableChatInGame, kickRandomPlayerIfJoinsVipToFullGame, deathMsgs,
			bossbarEnable, actionbarEnable, switchGMForPlayers, disableAllCommandsInGameFreeze, enableChatAfterEnd,
			tabFormatEnable, tabEnable, scoreboardEnable, enableChatFormat, restartServer, stopServer, rewardEnable,
			rejoinDelayEnabled, rememberRejoinDelay, freezePlayers, waitForNextSpawnAfterZombiesAreDead,
			notifySpectatorsToLeave;

	private static int gameFreezeTime, lobbyDelay, gameTime, bowKill, axeKill, axeDeath, knifeKill, explosionKill,
			suicide, grenadeKill, respawnProtectTime, rejoinDelayHour, rejoinDelayMinute, rejoinDelaySecond,
			delayBeforeFirstZombiesSpawn, delayAfterNextZombiesSpawning, playerLives, timeBetweenMessageSending;

	private static List<String> signTextLines, messageActions, allowedSpectatorCommands, allowedInGameCommands,
			executeCommandsOnPlayerLeaveWhilePlaying;

	private static List<Integer> lobbyBeginTimes, lobbyTitleBeginTimes, gameEndBroadcast;

	public static void loadValues(CommentedConfig f) {
		f.copyDefaults(true);

		f.addComment("language", "Default language. Example: en, hu, de etc.",
				"You can find all supported languages on: https://github.com/montlikadani/RageMode/wiki/Languages");
		f.addComment("log-console", "Logging console messages, such as error description");
		f.addComment("check-for-updates", "Check for updates on plugin startup.");
		f.addComment("download-updates", "Download new updates to releases folder",
				"This only works if the \"check-update\" is true.");
		f.addComment("save-player-datas-to-file",
				"This saves the player data, such as inventory, effects etc. to a file.",
				"This can be useful for a random server shutdown.",
				"If this false and \"require-empty-inventory-to-join\" also false then the players ",
				"loses his items from inventory.",
				"If this is false, we still need some data to save that will not cause any problems ",
				"at the end of the game.",
				"This will be ignored and the data not saved when the bungee-mode is enabled.");
		f.addComment("require-empty-inventory-to-join", "Require empty inventory to join to game?",
				"If this false and \"save-player-datas-to-file\" also false then the players ",
				"loses his items from inventory.");
		f.addComment("selection-item", "Used to select the game area to be protected zone.");
		f.addComment("bungee", "Hook into bungee");
		f.addComment("bungee.hub-name",
				"Bungeecord server name when the game end and teleport players to this server.");
		f.addComment("database", "The database where to save plugin data.");
		f.addComment("database.type", "Database types: yaml, mysql, sqlite",
				"To database where the player data will be saved.");
		f.addComment("database.table-prefix", "The database table name", "This is only for sql databases.");
		f.addComment("database.SQL", "SQLite database settings");
		f.addComment("database.MySQL", "MySQL database settings");
		f.addComment("signs", "Sign texts");
		f.addComment("signs.game", "Sign text when the game running or waiting for players.");
		f.addComment("signs.list", "Sign text list (max. 4 line)",
				"Use %game%, %current-players%, %max-players%, %running% placeholder.");
		f.addComment("signs.background", "Defines a background of the sign.");
		f.addComment("signs.background.type", "Possible types: glass, wool, terracotta (clay), none");
		f.addComment("message-actions", "Actionbar/Bossbar message when started the game and sends to player.",
				"This will be ignored, when the actionbar/bossbar option is disabled, or", "this list is empty.",
				"Actionbar usage found on https://github.com/montlikadani/RageMode/wiki/Actionbar-&-Bossbar-actions");
		f.addComment("titles", "Title texts");
		f.addComment("titles.join-game", "When a player join to the game, and send title for him.");
		f.addComment("titles.join-game.time", "Title time settings (in ticks)",
				"First number (fade-in): The time it takes for the title to fade into the screen.",
				"Second number (stay): The time it takes for the title to stay on the screen.",
				"Third number (fade-out): The time it takes for the title to fade out of the screen.");
		f.addComment("titles.lobby-waiting", "Lobby waiting timer title.");
		f.addComment("titles.lobby-waiting.begin-times",
				"Intervals when the lobby timer reaches e.g. 5 intervals, then send it on the screen.");
		f.addComment("titles.player-won",
				"When a player won a game, and send title for all players that in the game currently.");
		f.addComment("titles.you-won", "When you won a game, and send title for him that in the game currently.");
		f.addComment("spectator",
				"Enables the spectator mode in the already running game, to see what happens in that game.");
		f.addComment("spectator.notify-spectators-to-leave", "Notify spectators in message, how to leave the game.");
		f.addComment("spectator.notify-spectators-to-leave.time-between-message-sending",
				"Time in seconds between sending notify message.");
		f.addComment("spectator.allowed-commands", "Which spectator commands will be allowed to use in-game?");
		f.addComment("lobby", "Lobby settings");
		f.addComment("lobby.enable-chat-in-lobby", "Should be enable chat in lobby?",
				"This ignores with ragemode.bypass.lobby.lockchat permission.");
		f.addComment("lobby.player-level-as-time-counter",
				"Does count the player's level with the start time of the game in the lobby?");
		f.addComment("lobby.begin-times",
				"Time intervals when the lobby timer reaches, for example, 10 seconds, write it into the chat.");
		f.addComment("players-can-join-random-to-running-games",
				"Does the player can join to running games when types /rm joinrandom command to spectate?");
		f.addComment("per-join-permissions", "Enables per join permissions for joining.",
				"Use \"ragemode.join.gameName\" permission.");
		f.addComment("game", "Global settings for game.");
		f.addComment("game.damage-player-fall", "If this false if player has fallen to ground then not damage.");
		f.addComment("game.prevent-fastbow-event", "This option can be useful when a player is using a FastBow cheat,",
				"so it will be disabled by the plugin if it is enabled.");
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
		f.addComment("game.defaults", "Defaults values when in the setup does not contain e.g bossbar.");
		f.addComment("game.defaults.death-messages", "Allow player death messages to be sent?");
		f.addComment("game.defaults.bossbar", "Bossbar messages when a player killed someone.");
		f.addComment("game.defaults.actionbar", "Actionbar messages when a player get points or killed someone.");
		f.addComment("game.defaults.lobby-delay", "Lobby delay to start the game in seconds.");
		f.addComment("game.defaults.gametime", "Default game time in minutes.");
		f.addComment("game.tablist.player-format",
				"Player prefix/suffix format to show for example the player kills, deaths etc.");
		f.addComment("game.tablist.list", "Tablist header/footer");
		f.addComment("game.scoreboard", "Displays the score board on the right screen.");
		f.addComment("game.chat-format", "Chat formatting");
		f.addComment("game.allowed-commands", "The in game allowed commands, which the player can use in the game.");
		f.addComment("game.execute-commands-on-player-left-while-playing",
				"Execute commands when the player left the game while playing. (Only console commands)",
				"This is related to the \"Exit from the game\" button on Esc menu.");
		f.addComment("game-stop", "Stop the server or restart at the end of the game?");
		f.addComment("rejoin-delay", "Rejoin delay to add how many times a player can join to games.");
		f.addComment("rejoin-delay.remember-to-database", "Save the currently running delays to the database.",
				"The loaded delays will be removed when this setting is enabled and when the server starting.");
		f.addComment("rejoin-delay.times", "The waiting time for allowing player to join again.");
		f.addComment("rewards", "The player rewards if the game end or doing something else in a game.",
				"Rewards can be found in the rewards.yml file.");
		f.addComment("points", "Points for killing or deaths");
		f.addComment("points.suicide",
				"You can set to 0 to do not decrease the points amount from player if suicides itself.");

		lang = f.get("language", "en");
		checkForUpdates = f.get("check-for-updates", true);
		downloadUpdates = f.get("download-updates", false);
		logConsole = f.get("log-console", true);
		savePlayerData = f.get("save-player-datas-to-file", false);
		requireEmptyInv = f.get("require-empty-inventory-to-join", true);
		selectionItem = f.get("selection-item", "golden_shovel");
		bungee = f.get("bungee.enable", false);
		hubName = f.get("bungee.hub-name", "lobby");
		databaseType = f.get("database.type", "yaml");
		databaseTablePrefix = f.get("database.table-prefix", "ragemode_");
		autoReconnect = f.get("database.MySQL.auto-reconnect", true);
		useSSL = f.get("database.MySQL.use-SSL", false);
		username = f.get("database.MySQL.username", "accountname");
		password = f.get("database.MySQL.password", "password");
		database = f.get("database.MySQL.database", "database");
		host = f.get("database.MySQL.host", "localhost");
		port = f.get("database.MySQL.port", "3306");
		unicode = f.get("database.MySQL.use-unicode", true);
		certificate = f.get("database.MySQL.verify-server-certificate", false);
		encoding = f.get("database.MySQL.character-encoding", "UTF-8");
		sqlFileName = f.get("database.SQL.file-name", "rm.sqlite");
		signsEnable = f.get("signs.enable", false);
		signGameRunning = f.get("signs.game.running", "&6&oRunning...");
		signGameWaiting = f.get("signs.game.waiting", "&cWaiting...");
		signGameFull = f.get("signs.game.full", "&4FULL");
		signGameLocked = f.get("signs.game.locked", "&9Locked");
		signBackgrType = f.get("signs.background.type", "none");
		titleJoinGame = f.get("titles.join-game.title", "&e%game%");
		subtitleJoinGame = f.get("titles.join-game.subtitle", "&3by yourname");
		joinTitleTime = f.get("titles.join-game.time", "20, 50, 20");
		enableLobbyTitle = f.get("titles.lobby-waiting.enable", true);
		lobbyTitle = f.get("titles.lobby-waiting.title", "");
		subtitleLobby = f.get("titles.lobby-waiting.subtitle", "&9%time%");
		lobbyTitleTime = f.get("titles.lobby-waiting.time", "10, 30, 10");
		wonTitle = f.get("titles.player-won.title", "&2Congratulations!");
		wonsubtitle = f.get("titles.player-won.subtitle", "&e%winner%&6 won this round!");
		wonTitleTime = f.get("titles.player-won.time", "20, 80, 20");
		youwonTitle = f.get("titles.you-won.title", "&aCongratulations!");
		youwonsubtitle = f.get("titles.you-won.subtitle", "&2You won this round!");
		youwonTitleTime = f.get("titles.you-won.time", "20, 80, 20");
		spectatorEnable = f.get("spectator.enable", true);
		notifySpectatorsToLeave = f.get("spectator.notify-spectators-to-leave.enabled", true);
		timeBetweenMessageSending = f.get("spectator.notify-spectators-to-leave.time-between-message-sending", 10);
		chatEnableinLobby = f.get("lobby.enable-chat-in-lobby", true);
		playerLevelAsTimeCounter = f.get("lobby.player-level-as-time-counter", false);
		playersCanJoinRandomToRunningGames = f.get("players-can-join-random-to-running-games", true);
		perJoinPermissions = f.get("per-join-permissions", false);
		damagePlayerFall = f.get("game.damage-player-fall", false);
		preventFastBowEvent = f.get("game.prevent-fastbow-event", true);
		respawnProtectTime = f.get("game.respawn-protection", 3);
		hidePlayerNameTag = f.get("game.hide-players-name-tag", false);
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
		deathMsgs = f.get("game.defaults.death-messages", true);
		bossbarEnable = f.get("game.defaults.bossbar", false);
		actionbarEnable = f.get("game.defaults.actionbar", true);
		lobbyDelay = f.get("game.defaults.lobby-delay", 30);
		gameTime = f.get("game.defaults.gametime", 10);
		delayBeforeFirstZombiesSpawn = f.get("game.zombie-apocalypse.delay-before-first-spawn", 30);
		delayAfterNextZombiesSpawning = f.get("game.zombie-apocalypse.delay-after-next-zombies-spawning", 30);
		waitForNextSpawnAfterZombiesAreDead = f.get("game.zombie-apocalypse.wait-for-next-spawn-after-zombies-are-dead",
				true);
		playerLives = f.get("game.zombie-apocalypse.player-lives", 3);
		tabFormatEnable = f.get("game.tablist.player-format.enable", false);
		tabPrefix = f.get("game.tablist.player-format.prefix", "");
		tabSuffix = f.get("game.tablist.player-format.suffix", "&e %kills%");
		tabEnable = f.get("game.tablist.list.enable", false);
		scoreboardEnable = f.get("game.scoreboard.enable", true);
		sbTitle = f.get("game.scoreboard.title", "&6RageMode");
		enableChatFormat = f.get("game.chat-format.enable", false);
		chatFormat = f.get("game.chat-format.format", "&e[%points%]&r %player%&7:&r %message%");
		restartServer = f.get("game-stop.restart-server", false);
		stopServer = f.get("game-stop.stop-server", false);
		rejoinDelayEnabled = f.get("rejoin-delay.enabled", false);
		rememberRejoinDelay = f.get("rejoin-delay.remember-to-database", true);
		rejoinDelayHour = f.get("rejoin-delay.times.hour", 0);
		rejoinDelayMinute = f.get("rejoin-delay.times.minute", 0);
		rejoinDelaySecond = f.get("rejoin-delay.times.second", 30);
		rewardEnable = f.get("rewards.enable", true);
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
		messageActions = f.get("message-actions",
				Arrays.asList("[actionbar];join:&cHello&a %player%&c in this game!",
						"[actionbar];start:&aGame has started!&2 We wish you a good game!",
						"[bossbar];start:&bWelcome&a %player%&b to the&c %game%&b game!:green:segmented_6:8"));
		allowedSpectatorCommands = f.get("spectator.allowed-commands", Arrays.asList("/rm leave", "/ragemode leave"));
		allowedInGameCommands = f.get("game.allowed-commands",
				Arrays.asList("/rm leave", "/ragemode leave", "/ragemode stopgame"));
		executeCommandsOnPlayerLeaveWhilePlaying = f.get("game.execute-commands-on-player-left-while-playing",
				Arrays.asList("tell %player% &cWhy you left from the game while playing?"));
		gameEndBroadcast = f.getIntList("game.end-broadcast-at-times", Arrays.asList(60, 30, 20, 10, 5, 4, 3, 2, 1));

		f.save();
	}

	public static String getLang() {
		return lang;
	}

	public static String getHubName() {
		return hubName;
	}

	public static String getSelectionItem() {
		return selectionItem;
	}

	public static String getUsername() {
		return username;
	}

	public static String getPassword() {
		return password;
	}

	public static String getDatabase() {
		return database;
	}

	public static String getHost() {
		return host;
	}

	public static String getPort() {
		return port;
	}

	public static String getEncoding() {
		return encoding;
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

	public static boolean isAutoReconnect() {
		return autoReconnect;
	}

	public static boolean isUseSSL() {
		return useSSL;
	}

	public static boolean isUnicode() {
		return unicode;
	}

	public static boolean isCertificate() {
		return certificate;
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

	public static String getSignBackground() {
		return signBackgrType;
	}

	public static String getTitleJoinGame() {
		return titleJoinGame;
	}

	public static String getSubTitleJoinGame() {
		return subtitleJoinGame;
	}

	public static String getJoinTitleTime() {
		return joinTitleTime;
	}

	public static boolean isLobbyTitle() {
		return enableLobbyTitle;
	}

	public static String getLobbyTitle() {
		return lobbyTitle;
	}

	public static String getLobbySubTitle() {
		return subtitleLobby;
	}

	public static String getLobbyTitleTime() {
		return lobbyTitleTime;
	}

	public static String getWonTitle() {
		return wonTitle;
	}

	public static String getWonSubTitle() {
		return wonsubtitle;
	}

	public static String getWonTitleTime() {
		return wonTitleTime;
	}

	public static String getYouWonTitle() {
		return youwonTitle;
	}

	public static String getYouWonSubTitle() {
		return youwonsubtitle;
	}

	public static String getYouWonTitleTime() {
		return youwonTitleTime;
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

	public static boolean isPreventFastBowEvent() {
		return preventFastBowEvent;
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

	public static boolean isDefaultDeathMessageEnabled() {
		return deathMsgs;
	}

	public static boolean isDefaultBossbarEnabled() {
		return bossbarEnable;
	}

	public static boolean isDefaultActionbarEnabled() {
		return actionbarEnable;
	}

	public static int getDefaultLobbyDelay() {
		return lobbyDelay;
	}

	public static int getDefaultGameTime() {
		return gameTime;
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

	public static boolean isTabFormatEnabled() {
		return tabFormatEnable;
	}

	public static String getTabPrefix() {
		return tabPrefix;
	}

	public static String getTabSuffix() {
		return tabSuffix;
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

	public static boolean isRewardEnabled() {
		return rewardEnable;
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

	public static List<String> getAllowedSpectatorCommands() {
		return allowedSpectatorCommands;
	}

	public static List<String> getAllowedInGameCommands() {
		return allowedInGameCommands;
	}

	public static List<String> getExecuteCommandsOnPlayerLeaveWhilePlaying() {
		return executeCommandsOnPlayerLeaveWhilePlaying;
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

	public static List<String> getMessageActions() {
		return messageActions;
	}
}
