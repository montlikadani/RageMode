package hu.montlikadani.ragemode.config;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * @author montlikadani
 */
public class ConfigValues {

	private static String lang;
	private static String hubName;
	private static String databaseType;
	private static String databaseTablePrefix;
	private static String username;
	private static String password;
	private static String database;
	private static String host;
	private static String port;
	private static String encoding;
	private static String sqlFileName;
	private static String signGameRunning;
	private static String signGameWaiting;
	private static String signGameFull;
	private static String signGameLocked;
	private static String signBackgrType;
	private static String titleJoinGame;
	private static String subtitleJoinGame;
	private static String joinTitleTime;
	private static String lobbyTitle;
	private static String subtitleLobby;
	private static String lobbyTitleTime;
	private static String wonTitle;
	private static String wonsubtitle;
	private static String wonTitleTime;
	private static String youwonTitle;
	private static String youwonsubtitle;
	private static String youwonTitleTime;
	private static String tabPrefix;
	private static String tabSuffix;
	private static String sbTitle;
	private static String chatFormat;

	private static boolean checkForUpdates;
	private static boolean downloadUpdates;
	private static boolean logConsole;
	private static boolean savePlayerData;
	private static boolean requireEmptyInv;
	private static boolean bungee;
	private static boolean autoReconnect;
	private static boolean useSSL;
	private static boolean unicode;
	private static boolean certificate;
	private static boolean signsEnable;
	@Deprecated private static boolean signBackground;
	private static boolean enableLobbyTitle;
	private static boolean spectatorEnable;
	private static boolean chatEnableinLobby;
	private static boolean playerLevelAsTimeCounter;
	private static boolean playersCanJoinRandomToRunningGames;
	private static boolean perJoinPermissions;
	private static boolean damagePlayerFall;
	private static boolean hidePlayerNameTag;
	private static boolean cancelRedstoneActivating;
	private static boolean cancelDoorUse;
	private static boolean useGrenadeTrails;
	private static boolean enableChatInGame;
	private static boolean kickRandomPlayerIfJoinsVipToFullGame;
	private static boolean deathMsgs;
	private static boolean bossbarEnable;
	private static boolean actionbarEnable;
	private static boolean switchGMForPlayers;
	private static boolean disableAllCommandsInGameFreeze;
	private static boolean enableChatAfterEnd;
	private static boolean tabFormatEnable;
	private static boolean tabEnable;
	private static boolean scoreboardEnable;
	private static boolean enableChatFormat;
	private static boolean restartServer;
	private static boolean stopServer;
	private static boolean rewardEnable;
	private static boolean rejoinDelayEnabled;
	private static boolean rememberRejoinDelay;
	private static boolean freezePlayers;

	private static int gameFreezeTime;
	private static int lobbyDelay;
	private static int gameTime;
	private static int bowKill;
	private static int axeKill;
	private static int axeDeath;
	private static int knifeKill;
	private static int explosionKill;
	private static int suicide;
	private static int grenadeKill;
	private static int respawnProtectTime;
	private static int rejoinDelayHour;
	private static int rejoinDelayMinute;
	private static int rejoinDelaySecond;
	private static int killBonusChance;

	private static List<String> signsList;
	private static List<String> actionBarActions;
	private static List<String> bossBarActions;
	private static List<Integer> lobbyTitleStartMsgs;
	private static List<String> spectatorCmds;
	private static List<Integer> lobbyTimeMsgs;
	private static List<Integer> gameEndBc;
	private static List<String> tabHeader;
	private static List<String> tabFooter;
	private static List<String> sbContent;
	private static List<String> allowedCmds;
	private static List<String> cmdsForPlayerLeave;
	private static List<String> killBonuses;

	public static void loadValues(FileConfig f) {
		lang = f.get("language", "en");
		checkForUpdates = f.get("check-for-updates", true);
		downloadUpdates = f.get("download-updates", true);
		logConsole = f.get("log-console", true);
		savePlayerData = f.get("save-player-datas-to-file", false);
		requireEmptyInv = f.get("require-empty-inventory-to-join", true);
		bungee = f.get("bungee.enable", false);
		hubName = f.get("bungee.hub-name", "lobby");
		databaseType = f.get("databaseType", "");
		if (databaseType.isEmpty()) {
			databaseType = f.get("database.type", "yaml");
		}
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
		signsList = f.get("signs.list", Arrays.asList("&3[&5RageMode&3]", "&a%game%",
				"&ePlayers&3 [%current-players%/%max-players%&3]", "%running%"));
		signBackground = f.get("signs.background.enable", false);
		signBackgrType = f.get("signs.background.type", "none");
		actionBarActions = f.get("actionbar-messages.actions",
				Arrays.asList("start:&aGame has started!&2 We wish you a good game!"));
		bossBarActions = f.get("bossbar-messages.actions",
				Arrays.asList("start:&bWelcome&a %player%&b to the&c %game%&b game!:SEGMENTED_6:GREEN"));
		titleJoinGame = f.get("titles.join-game.title", "&e%game%");
		subtitleJoinGame = f.get("titles.join-game.subtitle", "&3by yourname");
		joinTitleTime = f.get("titles.join-game.time", "20, 50, 20");
		enableLobbyTitle = f.get("titles.lobby-waiting.enable", true);
		lobbyTitle = f.get("titles.lobby-waiting.title", "");
		subtitleLobby = f.get("titles.lobby-waiting.subtitle", "&9%time%");
		lobbyTitleTime = f.get("titles.lobby-waiting.time", "10, 30, 10");
		lobbyTitleStartMsgs = f.get("titles.lobby-waiting.values-to-send-start-message",
				new ArrayList<Integer>(Arrays.asList(5, 4, 3, 2, 1)));
		wonTitle = f.get("titles.player-won.title", "&2Congratulations!");
		wonsubtitle = f.get("titles.player-won.subtitle", "&e%winner%&6 won this round!");
		wonTitleTime = f.get("titles.player-won.time", "20, 80, 20");
		youwonTitle = f.get("titles.you-won.title", "&aCongratulations!");
		youwonsubtitle = f.get("titles.you-won.subtitle", "&2You won this round!");
		youwonTitleTime = f.get("titles.you-won.time", "20, 80, 20");
		spectatorEnable = f.get("spectator.enable", true);
		spectatorCmds = f.get("spectator.allowed-spectator-commands", Arrays.asList("/rm leave", "/ragemode leave"));
		chatEnableinLobby = f.get("lobby.enable-chat-in-lobby", true);
		playerLevelAsTimeCounter = f.get("lobby.player-level-as-time-counter", false);
		lobbyTimeMsgs = f.get("lobby.values-to-send-start-message",
				new ArrayList<Integer>(Arrays.asList(30, 20, 10, 5, 4, 3, 2, 1)));
		playersCanJoinRandomToRunningGames = f.get("players-can-join-random-to-running-games", true);
		perJoinPermissions = f.get("per-join-permissions", false);
		damagePlayerFall = f.get("game.damage-player-fall", false);
		respawnProtectTime = f.get("game.respawn-protection", 3);
		hidePlayerNameTag = f.get("game.hide-players-name-tag", false);
		gameEndBc = f.get("game.values-to-send-game-end-broadcast",
				new ArrayList<Integer>(Arrays.asList(60, 30, 20, 10, 5, 4, 3, 2, 1)));
		cancelRedstoneActivating = f.get("game.cancel-redstone-activating-blocks", true);
		cancelDoorUse = f.get("game.cancel-door-use", false);
		gameFreezeTime = f.get("game.game-freeze.time", 10);
		freezePlayers = f.get("game.game-freeze.freeze-players", false);
		switchGMForPlayers = f.get("game.game-freeze.switch-gamemode-to-spectator", true);
		disableAllCommandsInGameFreeze = f.get("game.game-freeze.disable-all-commands", false);
		enableChatAfterEnd = f.get("game.game-freeze.enable-chat", true);
		useGrenadeTrails = f.get("game.use-grenade-trails", true);
		enableChatInGame = f.get("game.enable-chat-in-game", true);
		kickRandomPlayerIfJoinsVipToFullGame = f.get("game.kickRandomPlayerIfJoinsVipToFullGame", true);
		deathMsgs = f.get("game.defaults.death-messages", true);
		bossbarEnable = f.get("game.defaults.bossbar", false);
		actionbarEnable = f.get("game.defaults.actionbar", true);
		lobbyDelay = f.get("game.defaults.lobby-delay", 30);
		gameTime = f.get("game.defaults.gametime", 10);
		tabFormatEnable = f.get("game.tablist.player-format.enable", false);
		tabPrefix = f.get("game.tablist.player-format.prefix", "");
		tabSuffix = f.get("game.tablist.player-format.suffix", "&e %kills%");
		tabEnable = f.get("game.tablist.list.enable", false);
		tabHeader = f.get("game.tablist.list.header",
				Arrays.asList("&cRage&bMode&e minigame stats", "&aYour kills/deaths:&e %kd%"));
		tabFooter = f.get("game.tablist.list.footer", Arrays.asList("&6Points:&e %points%"));
		scoreboardEnable = f.get("game.scoreboard.enable", true);
		sbTitle = f.get("game.scoreboard.title", "&6RageMode");
		sbContent = f.get("game.scoreboard.content",
				Arrays.asList("", "&7------", "&aPoints:&e %points%", "", "&5Wins:&e %wins%", "&7------", ""));
		enableChatFormat = f.get("game.chat-format.enable", false);
		chatFormat = f.get("game.chat-format.format", "&e[%points%]&r %player%&7:&r %message%");
		allowedCmds = f.get("game.allowed-commands",
				Arrays.asList("/rm leave", "/ragemode leave", "/ragemode stopgame"));
		cmdsForPlayerLeave = f.get("game.run-commands-for-player-left-while-playing", Collections.emptyList());
		restartServer = f.get("game.game-stop.restart-server", false);
		stopServer = f.get("game.game-stop.stop-server", false);
		rejoinDelayEnabled = f.get("rejoin-delay.enabled", false);
		rememberRejoinDelay = f.get("rejoin-delay.remember-to-database", true);
		rejoinDelayHour = f.get("rejoin-delay.times.hour", 0);
		rejoinDelayMinute = f.get("rejoin-delay.times.minute", 0);
		rejoinDelaySecond = f.get("rejoin-delay.times.second", 30);
		rewardEnable = f.get("rewards.enable", true);
		killBonusChance = f.get("bonuses.kill-bonuses.chance", 75);
		killBonuses = f.get("bonuses.kill-bonuses.list",
				Arrays.asList("effect:regeneration:20:2", "effect:speed:30:1", "points:15"));
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

	public static List<String> getSignsList() {
		return signsList;
	}

	@Deprecated
	public static boolean isSignBackground() {
		return signBackground;
	}

	public static String getSignBackground() {
		return signBackgrType;
	}

	public static List<String> getActionbarActions() {
		return actionBarActions;
	}

	public static List<String> getBossbarActions() {
		return bossBarActions;
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

	public static List<Integer> getLobbyTitleStartMsgs() {
		return lobbyTitleStartMsgs;
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

	public static List<String> getSpectatorCmds() {
		return spectatorCmds;
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

	public static List<Integer> getLobbyTimeMsgs() {
		return lobbyTimeMsgs;
	}

	public static boolean isDamagePlayerFall() {
		return damagePlayerFall;
	}

	public static int getRespawnProtectTime() {
		return respawnProtectTime;
	}

	public static List<Integer> getGameEndBcs() {
		return gameEndBc;
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

	public static List<String> getTabHeader() {
		return tabHeader;
	}

	public static List<String> getTabFooter() {
		return tabFooter;
	}

	public static boolean isScoreboardEnabled() {
		return scoreboardEnable;
	}

	public static String getSbTitle() {
		return sbTitle;
	}

	public static List<String> getSbContent() {
		return sbContent;
	}

	public static boolean isChatFormatEnabled() {
		return enableChatFormat;
	}

	public static String getChatFormat() {
		return chatFormat;
	}

	public static List<String> getAllowedCmds() {
		return allowedCmds;
	}

	public static List<String> getCmdsForPlayerLeave() {
		return cmdsForPlayerLeave;
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

	public static List<String> getKillBonuses() {
		return killBonuses;
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
}
