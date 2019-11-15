package hu.montlikadani.ragemode.config;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * @author montlikadani
 */
public class ConfigValues {

	private String lang;
	@Deprecated private String statistics; // remove in the next update
	private String databaseType;
	private String hubName;
	private String username;
	private String password;
	private String database;
	private String host;
	private String port;
	private String tablePrefix;
	private String encoding;
	private String sqlFileName;
	private String sqlTablePrefix;
	private String signGameRunning;
	private String signGameWaiting;
	private String signGameFull;
	private String signGameLocked;
	private String signBackgrType;
	private String bossbarMsg;
	private String bossbarStyle;
	private String bossbarColor;
	private String titleJoinGame;
	private String subtitleJoinGame;
	private String joinTitleTime;
	private String lobbyTitle;
	private String subtitleLobby;
	private String lobbyTitleTime;
	private String wonTitle;
	private String wonsubtitle;
	private String wonTitleTime;
	private String youwonTitle;
	private String youwonsubtitle;
	private String youwonTitleTime;
	private String tabPrefix;
	private String tabSuffix;
	private String sbTitle;
	private String chatFormat;

	private boolean checkForUpdates;
	private boolean logConsole;
	private boolean savePlayerData;
	private boolean requireEmptyInv;
	private boolean bungee;
	private boolean autoReconnect;
	private boolean useSSL;
	private boolean unicode;
	private boolean certificate;
	private boolean signsEnable;
	private boolean signBackground;
	private boolean enableLobbyTitle;
	private boolean spectatorEnable;
	private boolean chatEnableinLobby;
	private boolean playerLevelAsTimeCounter;
	private boolean damagePlayerFall;
	private boolean hidePlayerNameTag;
	private boolean cancelRedstoneActivating;
	private boolean cancelDoorUse;
	private boolean enableChatInGame;
	private boolean deathMsgs;
	private boolean bossbarEnable;
	private boolean actionbarEnable;
	private boolean switchGMForPlayers;
	private boolean enableChatAfterEnd;
	private boolean tabFormatEnable;
	private boolean tabEnable;
	private boolean scoreboardEnable;
	private boolean enableChatFormat;
	private boolean restartServer;
	private boolean stopServer;
	private boolean rewardEnable;
	private boolean rejoinDelayEnabled;

	private int signsUpdateTime;
	@Deprecated private int minPlayers; // remove in the next update
	private int gameFreezeTime;
	private int lobbyDelay;
	private int gameTime;
	private int bowKill;
	private int axeKill;
	private int axeDeath;
	private int knifeKill;
	private int explosionKill;
	private int suicide;
	private int grenadeKill;
	private int respawnProtectTime;
	private int rejoinDelayHour;
	private int rejoinDelayMinute;
	private int rejoinDelaySecond;

	private List<String> signsList;
	private List<String> actionBarActions;
	private List<Integer> lobbyTitleStartMsgs;
	private List<String> spectatorCmds;
	private List<Integer> lobbyTimeMsgs;
	private List<Integer> gameEndBc;
	private List<String> tabHeader;
	private List<String> tabFooter;
	private List<String> sbContent;
	private List<String> allowedCmds;
	private List<String> cmdsForPlayerLeave;

	public void loadValues(FileConfig f) {
		lang = f.get("language", "en");
		if (f.getOriginal("statistic") != null) {
			statistics = f.getOriginal("statistics");
		} else {
			databaseType = f.get("databaseType", "yaml");
		}
		checkForUpdates = f.get("check-for-updates", true);
		logConsole = f.get("log-console", true);
		savePlayerData = f.get("save-player-datas-to-file", false);
		requireEmptyInv = f.get("require-empty-inventory-to-join", true);
		bungee = f.get("bungee.enable", false);
		hubName = f.get("bungee.hub-name", "lobby");
		autoReconnect = f.get("MySQL.auto-reconnect", true);
		useSSL = f.get("MySQL.use-SSL", false);
		username = f.get("MySQL.username", "accountname");
		password = f.get("MySQL.password", "password");
		database = f.get("MySQL.database", "database");
		host = f.get("MySQL.host", "localhost");
		port = f.get("MySQL.port", "3306");
		tablePrefix = f.get("MySQL.table-prefix", "rm_");
		unicode = f.get("MySQL.use-unicode", true);
		certificate = f.get("MySQL.verify-server-certificate", false);
		encoding = f.get("MySQL.character-encoding", "UTF-8");
		sqlFileName = f.get("SQL.file-name", "rm.sqlite");
		sqlTablePrefix = f.get("SQL.table-prefix", "ragemode_");
		signsEnable = f.get("signs.enable", false);
		signsUpdateTime = f.get("signs.update-time", 20);
		signGameRunning = f.get("signs.game.running", "&6&oRunning...");
		signGameWaiting = f.get("signs.game.waiting", "&cWaiting...");
		signGameFull = f.get("signs.game.full", "&4FULL");
		signGameLocked = f.get("signs.game.locked", "&9Locked");
		signsList = f.get("signs.list", Arrays.asList("&3[&5RageMode&3]", "&a%game%",
				"&ePlayers&3 [%current-players%/%max-players%&3]", "%running%"));
		signBackground = f.get("signs.background.enable", false);
		signBackgrType = f.get("signs.background.type", "glass");
		actionBarActions = f.get("actionbar-messages.actions",
				Arrays.asList("start:&aGame has started!&2 We wish you a good game!"));
		bossbarMsg = f.get("bossbar-messages.message", "&bWelcome&a %player%&b to the&c %game%&b game!");
		bossbarStyle = f.get("bossbar-messages.style", "SEGMENTED_6");
		bossbarColor = f.get("bossbar-messages.color", "GREEN");
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
		minPlayers = f.get("game.global.lobby.min-players-to-start-lobby-timer", 2);
		chatEnableinLobby = f.get("game.global.lobby.enable-chat-in-lobby", true);
		playerLevelAsTimeCounter = f.get("game.global.lobby.player-level-as-time-counter", false);
		lobbyTimeMsgs = f.get("game.global.lobby.values-to-send-start-message",
				new ArrayList<Integer>(Arrays.asList(30, 20, 10, 5, 4, 3, 2, 1)));
		damagePlayerFall = f.get("game.global.damage-player-fall", false);
		respawnProtectTime = f.get("game.global.respawn-protection", 3);
		hidePlayerNameTag = f.get("game.global.hide-players-name-tag", false);
		gameEndBc = f.get("game.global.values-to-send-game-end-broadcast",
				new ArrayList<Integer>(Arrays.asList(60, 30, 20, 10, 5, 4, 3, 2, 1)));
		cancelRedstoneActivating = f.get("game.global.cancel-redstone-activating-blocks", true);
		cancelDoorUse = f.get("game.global.cancel-door-use", false);
		gameFreezeTime = f.get("game.global.game-freeze-time-at-end-game", 10);
		enableChatInGame = f.get("game.global.enable-chat-in-game", true);
		deathMsgs = f.get("game.global.defaults.death-messages", true);
		bossbarEnable = f.get("game.global.defaults.bossbar", false);
		actionbarEnable = f.get("game.global.defaults.actionbar", true);
		lobbyDelay = f.get("game.global.defaults.lobby-delay", 30);
		gameTime = f.get("game.global.defaults.gametime", 10);
		switchGMForPlayers = f.get("game.global.switch-gamemode-to-spectator-at-end-of-game", true);
		enableChatAfterEnd = f.get("game.global.enable-chat-after-end", true);
		tabFormatEnable = f.get("game.global.tablist.player-format.enable", false);
		tabPrefix = f.get("game.global.tablist.player-format.prefix", "");
		tabSuffix = f.get("game.global.tablist.player-format.suffix", "&e %kills%");
		tabEnable = f.get("game.global.tablist.list.enable", false);
		tabHeader = f.get("game.global.tablist.list.header",
				Arrays.asList("&cRage&bMode&e minigame stats", "&aYour kills/deaths:&e %kd%"));
		tabFooter = f.get("game.global.tablist.list.footer", Arrays.asList("&6Points:&e %points%"));
		scoreboardEnable = f.get("game.global.scoreboard.enable", true);
		sbTitle = f.get("game.global.scoreboard.title", "&6RageMode");
		sbContent = f.get("game.global.scoreboard.content",
				Arrays.asList("", "&7------", "&aPoints:&e %points%", "", "&5Wins:&e %wins%", "&7------", ""));
		enableChatFormat = f.get("game.global.chat-format.enable", false);
		chatFormat = f.get("game.global.chat-format.format", "&e[%points%]&r %player%&7:&r %message%");
		allowedCmds = f.get("game.global.allowed-commands",
				Arrays.asList("/rm leave", "/ragemode leave", "/ragemode stop"));
		cmdsForPlayerLeave = f.get("game.global.run-commands-for-player-left-while-playing", Collections.emptyList());
		restartServer = f.get("game.global.game-stop.restart-server", false);
		stopServer = f.get("game.global.game-stop.stop-server", false);
		rejoinDelayEnabled = f.get("rejoin-delay.enabled", false);
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
	}

	public String getLang() {
		return lang;
	}

	@Deprecated
	public String getStatistics() {
		return statistics;
	}

	public String getDatabaseType() {
		return databaseType;
	}

	public String getHubName() {
		return hubName;
	}

	public String getUsername() {
		return username;
	}

	public String getPassword() {
		return password;
	}

	public String getDatabase() {
		return database;
	}

	public String getHost() {
		return host;
	}

	public String getPort() {
		return port;
	}

	public String getTablePrefix() {
		return tablePrefix;
	}

	public String getEncoding() {
		return encoding;
	}

	public boolean isLogConsole() {
		return logConsole;
	}

	public boolean isCheckForUpdates() {
		return checkForUpdates;
	}

	public boolean isSavePlayerData() {
		return savePlayerData;
	}

	public boolean isRequireEmptyInv() {
		return requireEmptyInv;
	}

	public boolean isBungee() {
		return bungee;
	}

	public boolean isAutoReconnect() {
		return autoReconnect;
	}

	public boolean isUseSSL() {
		return useSSL;
	}

	public boolean isUnicode() {
		return unicode;
	}

	public boolean isCertificate() {
		return certificate;
	}

	public String getSqlFileName() {
		return sqlFileName;
	}

	public String getSqlTablePrefix() {
		return sqlTablePrefix;
	}

	public boolean isSignsEnable() {
		return signsEnable;
	}

	public int getSignsUpdateTime() {
		return signsUpdateTime;
	}

	public String getSignGameRunning() {
		return signGameRunning;
	}

	public String getSignGameWaiting() {
		return signGameWaiting;
	}

	public String getSignGameFull() {
		return signGameFull;
	}

	public String getSignGameLocked() {
		return signGameLocked;
	}

	public List<String> getSignsList() {
		return signsList;
	}

	public boolean isSignBackground() {
		return signBackground;
	}

	public String getSignBackground() {
		return signBackgrType;
	}

	public List<String> getActionbarActions() {
		return actionBarActions;
	}

	public String getBossbarMsg() {
		return bossbarMsg;
	}

	public String getBossbarStyle() {
		return bossbarStyle;
	}

	public String getBossbarColor() {
		return bossbarColor;
	}

	public String getTitleJoinGame() {
		return titleJoinGame;
	}

	public String getSubTitleJoinGame() {
		return subtitleJoinGame;
	}

	public String getJoinTitleTime() {
		return joinTitleTime;
	}

	public boolean isLobbyTitle() {
		return enableLobbyTitle;
	}

	public String getLobbyTitle() {
		return lobbyTitle;
	}

	public String getLobbySubTitle() {
		return subtitleLobby;
	}

	public String getLobbyTitleTime() {
		return lobbyTitleTime;
	}

	public List<Integer> getLobbyTitleStartMsgs() {
		return lobbyTitleStartMsgs;
	}

	public String getWonTitle() {
		return wonTitle;
	}

	public String getWonSubTitle() {
		return wonsubtitle;
	}

	public String getWonTitleTime() {
		return wonTitleTime;
	}

	public String getYouWonTitle() {
		return youwonTitle;
	}

	public String getYouWonSubTitle() {
		return youwonsubtitle;
	}

	public String getYouWonTitleTime() {
		return youwonTitleTime;
	}

	public boolean isSpectatorEnabled() {
		return spectatorEnable;
	}

	public List<String> getSpectatorCmds() {
		return spectatorCmds;
	}

	@Deprecated
	public int getMinPlayers() {
		return minPlayers;
	}

	public boolean isChatEnabledinLobby() {
		return chatEnableinLobby;
	}

	public boolean isPlayerLevelAsTimeCounter() {
		return playerLevelAsTimeCounter;
	}

	public List<Integer> getLobbyTimeMsgs() {
		return lobbyTimeMsgs;
	}

	public boolean isDamagePlayerFall() {
		return damagePlayerFall;
	}

	public int getRespawnProtectTime() {
		return respawnProtectTime;
	}

	public List<Integer> getGameEndBcs() {
		return gameEndBc;
	}

	public boolean isCancelRedstoneActivate() {
		return cancelRedstoneActivating;
	}

	public boolean isCancelDoorUse() {
		return cancelDoorUse;
	}

	public int getGameFreezeTime() {
		return gameFreezeTime;
	}

	public boolean isEnableChatInGame() {
		return enableChatInGame;
	}

	public boolean isDeathMsgs() {
		return deathMsgs;
	}

	public boolean isBossbarEnabled() {
		return bossbarEnable;
	}

	public boolean isActionbarEnabled() {
		return actionbarEnable;
	}

	public int getLobbyDelay() {
		return lobbyDelay;
	}

	public int getGameTime() {
		return gameTime;
	}

	public boolean isSwitchGMForPlayers() {
		return switchGMForPlayers;
	}

	public boolean isEnableChatAfterEnd() {
		return enableChatAfterEnd;
	}

	public boolean isTabFormatEnabled() {
		return tabFormatEnable;
	}

	public String getTabPrefix() {
		return tabPrefix;
	}

	public String getTabSuffix() {
		return tabSuffix;
	}

	public boolean isTabEnabled() {
		return tabEnable;
	}

	public List<String> getTabHeader() {
		return tabHeader;
	}

	public List<String> getTabFooter() {
		return tabFooter;
	}

	public boolean isScoreboardEnabled() {
		return scoreboardEnable;
	}

	public String getSbTitle() {
		return sbTitle;
	}

	public List<String> getSbContent() {
		return sbContent;
	}

	public boolean isChatFormatEnabled() {
		return enableChatFormat;
	}

	public String getChatFormat() {
		return chatFormat;
	}

	public List<String> getAllowedCmds() {
		return allowedCmds;
	}

	public List<String> getCmdsForPlayerLeave() {
		return cmdsForPlayerLeave;
	}

	public boolean isRestartServerEnabled() {
		return restartServer;
	}

	public boolean isStopServerEnabled() {
		return stopServer;
	}

	public boolean isRejoinDelayEnabled() {
		return rejoinDelayEnabled;
	}

	public int getRejoinDelayHour() {
		return rejoinDelayHour;
	}

	public int getRejoinDelayMinute() {
		return rejoinDelayMinute;
	}

	public int getRejoinDelaySecond() {
		return rejoinDelaySecond;
	}

	public boolean isRewardEnabled() {
		return rewardEnable;
	}

	public int getBowKill() {
		return bowKill;
	}

	public int getAxeKill() {
		return axeKill;
	}

	public int getAxeDeath() {
		return axeDeath;
	}

	public int getKnifeKill() {
		return knifeKill;
	}

	public int getExplosionKill() {
		return explosionKill;
	}

	public int getGrenadeKill() {
		return grenadeKill;
	}

	public int getSuicide() {
		return suicide;
	}

	public boolean isHidePlayerNameTag() {
		return hidePlayerNameTag;
	}
}
