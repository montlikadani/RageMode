package hu.montlikadani.ragemode.gameLogic;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ThreadLocalRandom;

import org.apache.commons.lang.Validate;
import org.bukkit.entity.Player;
import org.bukkit.metadata.FixedMetadataValue;

import hu.montlikadani.ragemode.RageMode;
import hu.montlikadani.ragemode.Utils;
import hu.montlikadani.ragemode.API.event.RMGameJoinAttemptEvent;
import hu.montlikadani.ragemode.API.event.SpectatorJoinToGameEvent;
import hu.montlikadani.ragemode.API.event.SpectatorLeaveGameEvent;
import hu.montlikadani.ragemode.config.ConfigValues;
import hu.montlikadani.ragemode.gameUtils.GameLobby;
import hu.montlikadani.ragemode.gameUtils.GetGames;
import hu.montlikadani.ragemode.gameUtils.ScoreBoard;
import hu.montlikadani.ragemode.gameUtils.ScoreTeam;
import hu.montlikadani.ragemode.gameUtils.TabTitles;
import hu.montlikadani.ragemode.managers.PlayerManager;

public class Game {

	private String name;

	private Map<Player, PlayerManager> players = new HashMap<>();
	private Map<Player, PlayerManager> specPlayer = new HashMap<>();

	private boolean running = false;
	private LobbyTimer lobbyTimer;

	// TODO: In the future add ability to work with ids
	public Game(String name) {
		this.name = name;
	}

	public String getName() {
		return name;
	}

	/**
	 * Get the players who added to the list.
	 * @return Modifiable map, {@link #players}
	 */
	public Map<Player, PlayerManager> getPlayers() {
		return players;
	}

	/**
	 * Gets the spectator players who added to the list.
	 * @return Modifiable map, {@link #specPlayer}
	 */
	public Map<Player, PlayerManager> getSpectatorPlayers() {
		return specPlayer;
	}

	private boolean isSpectator(Player p) {
		return specPlayer.containsKey(p);
	}

	private boolean isInList(Player p) {
		return players.containsKey(p);
	}

	public boolean addPlayer(Player player) {
		if (isGameRunning()) {
			player.sendMessage(RageMode.getLang().get("game.running"));
			return false;
		}

		if (isInList(player)) {
			player.sendMessage(RageMode.getLang().get("game.player-already-in-game", "%usage%", "/rm leave"));
			return false;
		}

		RMGameJoinAttemptEvent event = new RMGameJoinAttemptEvent(this, player);
		Utils.callEvent(event);
		if (event.isCancelled())
			return false;

		PlayerManager pm = new PlayerManager(player, name);

		int time = GameLobby.getLobbyTime(name);
		int maxPlayers = GetGames.getMaxPlayers(name);
		int minPlayers = GetGames.getMinPlayers(name);

		if (players.size() < maxPlayers) {
			players.put(player, pm);

			player.sendMessage(RageMode.getLang().get("game.you-joined-the-game", "%game%", name));

			if (players.size() == minPlayers) {
				lobbyTimer = new LobbyTimer(this, time);
				lobbyTimer.loadTimer();
			}

			return true;
		}

		// Gets a random player who is in game and kicks from the game to join the VIP player.
		if (ConfigValues.isKickRandomPlayerIfJoinsVip() && player.hasPermission("ragemode.vip") && hasRoomForVIP()) {
			boolean isVIP = false;
			Player playerToKick;

			do {
				int kickposition = maxPlayers < 2 ? 0 : ThreadLocalRandom.current().nextInt(maxPlayers - 1);
				playerToKick = getPlayersFromList().get(kickposition).getPlayer();
				isVIP = playerToKick.hasPermission("ragemode.vip");
			} while (isVIP);

			playerToKick.setMetadata("Leaving", new FixedMetadataValue(RageMode.getInstance(), true));

			Utils.clearPlayerInventory(playerToKick);
			getPlayerManager(playerToKick).addBackTools();
			players.remove(playerToKick);

			playerToKick.sendMessage(RageMode.getLang().get("game.player-kicked-for-vip"));

			players.put(player, pm);

			player.sendMessage(RageMode.getLang().get("game.you-joined-the-game", "%game%", name));

			if (players.size() == minPlayers) {
				lobbyTimer = new LobbyTimer(this, time);
				lobbyTimer.loadTimer();
			}

			return true;
		}

		player.sendMessage(RageMode.getLang().get("game.full"));
		return false;
	}

	public boolean addSpectatorPlayer(Player player, String game) {
		PlayerManager pm = new PlayerManager(player, game);
		specPlayer.put(player, pm);

		if (!ConfigValues.isBungee()) {
			pm.storePlayerTools(true);
		}
		Utils.clearPlayerInventory(player);

		Utils.callEvent(new SpectatorJoinToGameEvent(this, player));

		return isSpectator(player);
	}

	public boolean removeSpectatorPlayer(Player player) {
		if (!ConfigValues.isSpectatorEnabled())
			return false;

		if (!isSpectator(player)) {
			return false;
		}

		Utils.clearPlayerInventory(player);
		getSpectatorPlayerManager(player).addBackTools(true);

		Utils.callEvent(new SpectatorLeaveGameEvent(this, player));

		specPlayer.remove(player);
		return true;
	}

	public boolean removePlayer(final Player player) {
		if (!isInList(player)) {
			player.sendMessage(RageMode.getLang().get("game.player-not-ingame"));
			return false;
		}

		Utils.clearPlayerInventory(player);
		getPlayerManager(player).addBackTools();

		players.remove(player);
		removePlayerSynced(player);

		if (!player.isCustomNameVisible()) {
			player.setCustomNameVisible(true);
		}

		player.sendMessage(RageMode.getLang().get("game.player-left"));

		player.setMetadata("Leaving", new FixedMetadataValue(RageMode.getInstance(), true));
		return true;
	}

	public void removePlayerSynced(Player player) {
		if (ScoreBoard.allScoreBoards.containsKey(name))
			ScoreBoard.allScoreBoards.get(name).remove(player);

		if (TabTitles.allTabLists.containsKey(name))
			TabTitles.allTabLists.get(name).remove(player);

		if (ScoreTeam.allTeams.containsKey(name))
			ScoreTeam.allTeams.get(name).remove(player);
	}

	/**
	 * Checks whatever the game is running or not.
	 * @return true if the game running currently.
	 */
	public boolean isGameRunning() {
		return running;
	}

	/**
	 * Sets the game to running.
	 * @return true if the game not running currently
	 */
	public boolean setGameRunning() {
		if (running) {
			return false;
		}

		running = true;
		return true;
	}

	/**
	 * Sets the game not running.
	 * @return true if the game is running currently
	 */
	public boolean setGameNotRunning() {
		if (running) {
			running = false;
			return true;
		}

		return false;
	}

	/**
	 * Check whatever has free room for VIP players.
	 * @return true if the players size not equal to vips size
	 */
	public boolean hasRoomForVIP() {
		int vipsInGame = 0;

		for (Entry<Player, PlayerManager> players : players.entrySet()) {
			if (players.getValue().getGameName().equalsIgnoreCase(name)
					&& players.getKey().hasPermission("ragemode.vip")) {
				vipsInGame++;
			}
		}

		if (vipsInGame == players.size())
			return false;

		return true;
	}

	/**
	 * Get the player by name from list.
	 * @param name Player name
	 * @return Player
	 */
	public Player getPlayer(String name) {
		Validate.notNull(name, "Name can't be null!");
		Validate.notEmpty(name, "Name can't be empty!");

		for (Entry<Player, PlayerManager> players : players.entrySet()) {
			if (players.getKey().getName().equalsIgnoreCase(name)) {
				return players.getKey();
			}
		}

		return null;
	}

	/**
	 * Get the spectator player by name from list.
	 * @param name Player name
	 * @return Player
	 */
	public Player getSpectatorPlayer(String name) {
		Validate.notNull(name, "Name can't be null!");
		Validate.notEmpty(name, "Name can't be empty!");

		for (Entry<Player, PlayerManager> specs : specPlayer.entrySet()) {
			if (specs.getKey().getName().equalsIgnoreCase(name)) {
				return specs.getKey();
			}
		}

		return null;
	}

	/**
	 * Gets the {@link PlayerManager} player converted to list.
	 * @return list of {@link PlayerManager}
	 */
	public List<PlayerManager> getPlayersFromList() {
		List<PlayerManager> list = new ArrayList<>();

		for (Entry<Player, PlayerManager> players : players.entrySet()) {
			list.add(players.getValue());
		}

		return list;
	}

	/**
	 * Gets the {@link PlayerManager} spectator players converted to list.
	 * @return list of {@link PlayerManager}
	 */
	public List<PlayerManager> getSpectatorPlayersFromList() {
		List<PlayerManager> list = new ArrayList<>();

		for (Entry<Player, PlayerManager> players : specPlayer.entrySet()) {
			list.add(players.getValue());
		}

		return list;
	}

	/**
	 * Gets the {@link PlayerManager} by spectator player.
	 * @param p Spectator player
	 * @return {@link PlayerManager} by spectator player
	 */
	public PlayerManager getSpectatorPlayerManager(Player p) {
		Validate.notNull(p, "Player can't be null!");

		return specPlayer.get(p);
	}

	/**
	 * Gets the {@link PlayerManager} by player.
	 * @param p Player
	 * @return {@link PlayerManager} by player
	 */
	public PlayerManager getPlayerManager(Player p) {
		Validate.notNull(p, "Player can't be null!");

		return players.get(p);
	}

	public LobbyTimer getLobbyTimer() {
		return lobbyTimer;
	}

	/**
	 * Cancels the lobby timer.
	 */
	public void cancelLobbyTimer() {
		if (lobbyTimer != null) {
			lobbyTimer.cancel();
		}
	}
}
