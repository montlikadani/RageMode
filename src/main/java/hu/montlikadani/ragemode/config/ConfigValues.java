package hu.montlikadani.ragemode.config;

/**
 * @author montlikadani
 */
public class ConfigValues {

	private static String lang, hubName, selectionItem, databaseType, databaseTablePrefix, username, password, database, host, port,
			encoding, sqlFileName, signGameRunning, signGameWaiting, signGameFull, signGameLocked, signBackgrType,
			titleJoinGame, subtitleJoinGame, joinTitleTime, lobbyTitle, subtitleLobby, lobbyTitleTime, wonTitle,
			wonsubtitle, wonTitleTime, youwonTitle, youwonsubtitle, youwonTitleTime, tabPrefix, tabSuffix, sbTitle,
			chatFormat;

	@Deprecated private static boolean signBackground;

	private static boolean checkForUpdates, downloadUpdates, logConsole, savePlayerData, requireEmptyInv, bungee,
			autoReconnect, useSSL, unicode, certificate, signsEnable, enableLobbyTitle, spectatorEnable,
			chatEnableinLobby, playerLevelAsTimeCounter, playersCanJoinRandomToRunningGames, perJoinPermissions,
			damagePlayerFall, preventFastBowEvent, hidePlayerNameTag, cancelRedstoneActivating, cancelDoorUse,
			useGrenadeTrails, useArrowTrails, enableChatInGame, kickRandomPlayerIfJoinsVipToFullGame, deathMsgs,
			bossbarEnable, actionbarEnable, switchGMForPlayers, disableAllCommandsInGameFreeze, enableChatAfterEnd,
			tabFormatEnable, tabEnable, scoreboardEnable, enableChatFormat, restartServer, stopServer, rewardEnable,
			rejoinDelayEnabled, rememberRejoinDelay, freezePlayers, waitForNextSpawnAfterZombiesAreDead;

	private static int gameFreezeTime, lobbyDelay, gameTime, bowKill, axeKill, axeDeath, knifeKill, explosionKill,
			suicide, grenadeKill, respawnProtectTime, rejoinDelayHour, rejoinDelayMinute, rejoinDelaySecond,
			killBonusChance, delayBeforeFirstZombiesSpawn, delayAfterNextZombiesSpawning, playerLives;

	public static void loadValues(FileConfig f) {
		lang = f.get("language", "en");
		checkForUpdates = f.get("check-for-updates", true);
		downloadUpdates = f.get("download-updates", true);
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
		signBackground = f.get("signs.background.enable", false);
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
		waitForNextSpawnAfterZombiesAreDead = f.get("game.zombie-apocalypse.wait-for-next-spawn-after-zombies-are-dead", true);
		playerLives = f.get("game.zombie-apocalypse.player-lives", 3);
		tabFormatEnable = f.get("game.tablist.player-format.enable", false);
		tabPrefix = f.get("game.tablist.player-format.prefix", "");
		tabSuffix = f.get("game.tablist.player-format.suffix", "&e %kills%");
		tabEnable = f.get("game.tablist.list.enable", false);
		scoreboardEnable = f.get("game.scoreboard.enable", true);
		sbTitle = f.get("game.scoreboard.title", "&6RageMode");
		enableChatFormat = f.get("game.chat-format.enable", false);
		chatFormat = f.get("game.chat-format.format", "&e[%points%]&r %player%&7:&r %message%");
		restartServer = f.get("game.game-stop.restart-server", false);
		stopServer = f.get("game.game-stop.stop-server", false);
		rejoinDelayEnabled = f.get("rejoin-delay.enabled", false);
		rememberRejoinDelay = f.get("rejoin-delay.remember-to-database", true);
		rejoinDelayHour = f.get("rejoin-delay.times.hour", 0);
		rejoinDelayMinute = f.get("rejoin-delay.times.minute", 0);
		rejoinDelaySecond = f.get("rejoin-delay.times.second", 30);
		rewardEnable = f.get("rewards.enable", true);
		killBonusChance = f.get("bonuses.kill-bonuses.chance", 75);
		bowKill = f.get("points.bowkill", 25);
		axeKill = f.get("points.axekill", 30);
		axeDeath = f.get("points.axedeath", -50);
		knifeKill = f.get("points.knifekill", 15);
		explosionKill = f.get("points.explosionkill", 25);
		grenadeKill = f.get("points.grenadeKill", 45);
		suicide = f.get("points.suicide", -20);
	}

	public static String getLang() {
		return lang;
	}

	public static String getDatabaseType() {
		return databaseType;
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

	@Deprecated
	public static boolean isSignBackground() {
		return signBackground;
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

	public static int getKillBonusChance() {
		return killBonusChance;
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
}
