package hu.montlikadani.ragemode.gameLogic;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
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
	@Deprecated
	private Map<String, Boolean> runningGame = new HashMap<>();

	private boolean running = false;
	private LobbyTimer lobbyTimer;

	public Game(String name) {
		this.name = name;
	}

	public String getName() {
		return name;
	}

	/**
	 * Get the players who added to the list.
	 * @return Unmodifiable map
	 */
	public Map<Player, PlayerManager> getPlayers() {
		return Collections.unmodifiableMap(players);
	}

	/**
	 * Gets the spectator players who added to the list.
	 * @return Unmodifiable map
	 */
	public Map<Player, PlayerManager> getSpectatorPlayers() {
		return Collections.unmodifiableMap(specPlayer);
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

		int time = GameLobby.getLobbyTime(name);
		PlayerManager pm = new PlayerManager(player, name);

		if (players.size() < GetGames.getMaxPlayers(name)) {
			players.put(player, pm);

			player.sendMessage(RageMode.getLang().get("game.you-joined-the-game", "%game%", name));

			if (GetGames.getMinPlayers(name) > 1) {
				if (players.size() == GetGames.getMinPlayers(name) && lobbyTimer == null) {
					lobbyTimer = new LobbyTimer(this, time);
					lobbyTimer.loadTimer();
				}
			} else {
				if (players.size() == 2 && lobbyTimer == null) {
					lobbyTimer = new LobbyTimer(this, time);
					lobbyTimer.loadTimer();
				}
			}

			return true;
		}

		// Gets a random player who is in game and kicks from the game to join the VIP player.
		if (player.hasPermission("ragemode.vip") && hasRoomForVIP(name)) {
			boolean isVIP = false;
			Player playerToKick;

			do {
				int kickposition = GetGames.getMaxPlayers(name) < 2 ? 0
						: ThreadLocalRandom.current().nextInt(GetGames.getMaxPlayers(name) - 1);
				playerToKick = getPlayersFromList().get(kickposition).getPlayer();
				isVIP = playerToKick.hasPermission("ragemode.vip");
			} while (isVIP);

			playerToKick.setMetadata("Leaving", new FixedMetadataValue(RageMode.getInstance(), true));

			Utils.clearPlayerInventory(playerToKick);
			getPlayerManager(playerToKick).addBackTools();
			players.remove(playerToKick);

			playerToKick.sendMessage(RageMode.getLang().get("game.player-kicked-for-vip"));

			players.put(player, pm);

			if (GetGames.getMinPlayers(name) > 1) {
				if (players.size() == GetGames.getMinPlayers(name) && lobbyTimer == null) {
					lobbyTimer = new LobbyTimer(this, time);
					lobbyTimer.loadTimer();
				}
			} else {
				if (players.size() == 2 && lobbyTimer == null) {
					lobbyTimer = new LobbyTimer(this, time);
					lobbyTimer.loadTimer();
				}
			}

			player.sendMessage(RageMode.getLang().get("game.you-joined-the-game", "%game%", name));
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

		removePlayerSynced(player);
		removePlayerFromList(player);

		if (!player.isCustomNameVisible()) {
			player.setCustomNameVisible(true);
		}

		player.sendMessage(RageMode.getLang().get("game.player-left"));

		player.setMetadata("Leaving", new FixedMetadataValue(RageMode.getInstance(), true));
		return true;
	}

	public void removePlayerSynced(Player player) {
		if (ScoreBoard.allScoreBoards.containsKey(name))
			ScoreBoard.allScoreBoards.get(name).removeScoreBoard(player, true);

		if (TabTitles.allTabLists.containsKey(name))
			TabTitles.allTabLists.get(name).remove(player);

		if (ScoreTeam.allTeams.containsKey(name))
			ScoreTeam.allTeams.get(name).remove(player);
	}

	private void removePlayerFromList(Player player) {
		for (Iterator<Entry<Player, PlayerManager>> it = players.entrySet().iterator(); it.hasNext();) {
			if (it.next().getKey().equals(player)) {
				it.remove();
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
	 * Checks whatever the game is running or not.
	 * @param game Game
	 * @return true if the game is exist and running.
	 */
	@Deprecated
	public boolean isGameRunning(String game) {
		Validate.notNull(game, "Game name can't be null!");
		Validate.notEmpty(game, "Game name can't be empty!");

		if (GetGames.isGameExistent(game) && runningGame.get(game) != null && runningGame.get(game)) {
			return true;
		}

		return false;
	}

	/**
	 * Sets the game running.
	 * @param game Game
	 * @return true if game exist and the game not running
	 */
	@Deprecated
	public boolean setGameRunning(String game) {
		Validate.notNull(game, "Game name can't be null!");
		Validate.notEmpty(game, "Game name can't be empty!");

		if (!GetGames.isGameExistent(game))
			return false;

		if (runningGame.get(game) != null && runningGame.get(game)) {
			return false;
		}

		runningGame.put(game, true);
		return true;
	}

	/**
	 * Sets the game not running
	 * @param game Game
	 * @return true if game exist and the game is running
	 */
	@Deprecated
	public boolean setGameNotRunning(String game) {
		Validate.notNull(game, "Game name can't be null!");
		Validate.notEmpty(game, "Game name can't be empty!");

		if (!GetGames.isGameExistent(game))
			return false;

		if (runningGame.get(game) != null && runningGame.get(game)) {
			runningGame.remove(game);
			return true;
		}

		return false;
	}

	/**
	 * Check whatever has free room for VIP players.
	 * @param game Game
	 * @return true if the players size not equal to vips size
	 */
	public boolean hasRoomForVIP(String game) {
		if (game == null || game.isEmpty()) {
			game = name;
		}

		if (players == null) {
			return false;
		}

		int vipsInGame = 0;

		for (Entry<Player, PlayerManager> players : players.entrySet()) {
			if (players.getValue().getGameName().equalsIgnoreCase(game)) {
				if (players.getKey().hasPermission("ragemode.vip")) {
					vipsInGame++;
				}
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

		if (players != null) {
			for (Entry<Player, PlayerManager> players : players.entrySet()) {
				if (players.getKey().getName().equalsIgnoreCase(name)) {
					return players.getKey();
				}
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

		if (specPlayer != null) {
			for (Entry<Player, PlayerManager> specs : specPlayer.entrySet()) {
				if (specs.getKey().getName().equalsIgnoreCase(name)) {
					return specs.getKey();
				}
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

		if (players != null) {
			for (Entry<Player, PlayerManager> players : players.entrySet()) {
				list.add(players.getValue());
			}
		}

		return list;
	}

	/**
	 * Gets the {@link PlayerManager} spectator players converted to list.
	 * @return list of {@link PlayerManager}
	 */
	public List<PlayerManager> getSpectatorPlayersFromList() {
		List<PlayerManager> list = new ArrayList<>();

		if (specPlayer != null) {
			for (Entry<Player, PlayerManager> players : specPlayer.entrySet()) {
				list.add(players.getValue());
			}
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

		if (specPlayer != null) {
			for (Entry<Player, PlayerManager> players : specPlayer.entrySet()) {
				if (players.getKey().equals(p)) {
					return players.getValue();
				}
			}
		}

		return null;
	}

	/**
	 * Gets the {@link PlayerManager} by player.
	 * @param p Player
	 * @return {@link PlayerManager} by player
	 */
	public PlayerManager getPlayerManager(Player p) {
		Validate.notNull(p, "Player can't be null!");

		if (players != null) {
			for (Entry<Player, PlayerManager> players : players.entrySet()) {
				if (players.getKey().equals(p)) {
					return players.getValue();
				}
			}
		}

		return null;
	}

	public LobbyTimer getLobbyTimer() {
		return lobbyTimer;
	}

	/**
	 * Cancels the lobby timer and nulls the class to create new instance
	 * for this class. This prevents that bug when the player re-join to the
	 * game, then lobby timer does not start.
	 */
	public void cancelLobbyTimer() {
		if (lobbyTimer != null) {
			lobbyTimer.cancel();
			lobbyTimer = null;
		}
	}
}
