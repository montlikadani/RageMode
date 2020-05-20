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
import hu.montlikadani.ragemode.API.event.RMGameStatusChangeEvent;
import hu.montlikadani.ragemode.API.event.SpectatorJoinToGameEvent;
import hu.montlikadani.ragemode.API.event.SpectatorLeaveGameEvent;
import hu.montlikadani.ragemode.config.ConfigValues;
import hu.montlikadani.ragemode.gameUtils.ActionMessengers;
import hu.montlikadani.ragemode.gameUtils.GameLobby;
import hu.montlikadani.ragemode.gameUtils.GetGames;
import hu.montlikadani.ragemode.items.shop.LobbyShop;
import hu.montlikadani.ragemode.managers.PlayerManager;

public class Game {

	private String name;

	private GameStatus status = GameStatus.STOPPED;

	private final Map<Player, PlayerManager> players = new HashMap<>();
	private final Map<Player, PlayerManager> specPlayer = new HashMap<>();

	private boolean running = false;
	private LobbyTimer lobbyTimer;

	private LobbyShop shop = new LobbyShop();

	private final List<ActionMessengers> acList = new ArrayList<>();

	// TODO: In the future add ability to work with ids
	public Game(String name) {
		this.name = name == null ? "" : name;
	}

	/**
	 * @return the game name that can't be null.
	 */
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

	/**
	 * Checks if the player is in spectator list.
	 * @param p Player
	 * @return true if in the list
	 */
	public boolean isSpectatorInList(Player p) {
		return specPlayer.containsKey(p);
	}

	/**
	 * Checks if the player is in list.
	 * @param p Player
	 * @return true if in the list
	 */
	public boolean isPlayerInList(Player p) {
		return players.containsKey(p);
	}

	public boolean addPlayer(Player player) {
		if (running) {
			player.sendMessage(RageMode.getLang().get("game.running"));
			return false;
		}

		if (isPlayerInList(player)) {
			player.sendMessage(RageMode.getLang().get("game.player-already-in-game", "%usage%", "/rm leave"));
			return false;
		}

		RMGameJoinAttemptEvent event = new RMGameJoinAttemptEvent(this, player);
		Utils.callEvent(event);
		if (event.isCancelled())
			return false;

		ActionMessengers ac = new ActionMessengers(this, player);
		acList.add(ac);

		PlayerManager pm = new PlayerManager(player, name);

		int time = GameLobby.getLobbyTime(name),
				maxPlayers = GetGames.getMaxPlayers(name),
				minPlayers = GetGames.getMinPlayers(name);

		if (players.size() < maxPlayers) {
			players.put(player, pm);

			player.sendMessage(RageMode.getLang().get("game.you-joined-the-game", "%game%", name));

			if (players.size() == minPlayers) {
				lobbyTimer = new LobbyTimer(this, time);
				lobbyTimer.loadTimer();
			}

			return true;
		}

		// Gets a random player who is in game and kicks from the game to join the VIP
		// player.
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

			shop.removeShop(playerToKick);

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

		return isSpectatorInList(player);
	}

	public boolean removeSpectatorPlayer(Player player) {
		if (!ConfigValues.isSpectatorEnabled())
			return false;

		if (!isSpectatorInList(player)) {
			return false;
		}

		Utils.clearPlayerInventory(player);
		getSpectatorPlayerManager(player).addBackTools(true);

		Utils.callEvent(new SpectatorLeaveGameEvent(this, player));

		specPlayer.remove(player);
		return true;
	}

	public boolean removePlayer(final Player player) {
		if (!isPlayerInList(player)) {
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

		shop.removeShop(player);

		player.sendMessage(RageMode.getLang().get("game.player-left"));
		player.setMetadata("Leaving", new FixedMetadataValue(RageMode.getInstance(), true));
		return true;
	}

	/**
	 * Removes all of scoreboard, tablist and score teams things from player.
	 * @param player Player
	 */
	public void removePlayerSynced(Player player) {
		if (player == null) {
			return;
		}

		for (ActionMessengers action : acList) {
			if (action.getPlayer().equals(player)) {
				action.getScoreboard().remove(player);
				action.getTabTitles().remove();
				action.getScoreTeam().remove();
				break;
			}
		}
	}

	/**
	 * Checks whatever the game is running or not.
	 * @return true if the game running currently.
	 */
	public boolean isGameRunning() {
		return running;
	}

	/**
	 * Sets the game to running state.
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
	 * Sets the game to not running state.
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
	 * Gets the game current set GameStatus.
	 * @return {@link GameStatus}
	 */
	public GameStatus getStatus() {
		return status;
	}

	/**
	 * Sets the game status to new status.
	 * @param status the new status to be set for the game
	 */
	public void setStatus(GameStatus status) {
		if (status == null) {
			status = GameStatus.STOPPED;
		}

		RMGameStatusChangeEvent event = new RMGameStatusChangeEvent(this, status);
		Utils.callEvent(event);

		this.status = status;
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

	/**
	 * Gets the lobby shop.
	 * @return {@link LobbyShop}
	 */
	public LobbyShop getShop() {
		return shop;
	}

	public LobbyTimer getLobbyTimer() {
		return lobbyTimer;
	}

	public List<ActionMessengers> getActionMessengers() {
		return acList;
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
