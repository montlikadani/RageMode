package hu.montlikadani.ragemode.gameLogic;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.Map.Entry;
import java.util.concurrent.ThreadLocalRandom;

import org.apache.commons.lang.Validate;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.metadata.FixedMetadataValue;

import hu.montlikadani.ragemode.RageMode;
import hu.montlikadani.ragemode.Utils;
import hu.montlikadani.ragemode.API.event.RMGameJoinAttemptEvent;
import hu.montlikadani.ragemode.API.event.SpectatorJoinToGameEvent;
import hu.montlikadani.ragemode.API.event.SpectatorLeaveGameEvent;
import hu.montlikadani.ragemode.config.Configuration;
import hu.montlikadani.ragemode.gameUtils.GameUtils;
import hu.montlikadani.ragemode.gameUtils.GetGameLobby;
import hu.montlikadani.ragemode.gameUtils.GetGames;
import hu.montlikadani.ragemode.gameUtils.ScoreBoard;
import hu.montlikadani.ragemode.gameUtils.ScoreTeam;
import hu.montlikadani.ragemode.gameUtils.TabTitles;
import hu.montlikadani.ragemode.managers.PlayerManager;

public class Game {

	private String name;

	private Map<Player, PlayerManager> players = new HashMap<>();
	private Map<Player, PlayerManager> specPlayer = new HashMap<>();
	private Map<String, Boolean> running = new HashMap<>();

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

	public boolean addPlayer(Player player, String game) {
		if (isGameRunning(game)) {
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

		Configuration conf = RageMode.getInstance().getConfiguration();
		int time = GetGameLobby.getLobbyTime(game);

		PlayerManager pm = new PlayerManager(player, game);

		if (players.size() < GetGames.getMaxPlayers(game)) {
			players.put(player, pm);

			player.sendMessage(RageMode.getLang().get("game.you-joined-the-game", "%game%", game));

			if (conf.getCV().getMinPlayers() > 1) {
				if (players.size() == conf.getCV().getMinPlayers() && lobbyTimer == null) {
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

		// Gets a random player who is in game and kicks from the game
		// to join the VIP player.
		if (player.hasPermission("ragemode.vip") && hasRoomForVIP(game)) {
			boolean isVIP = false;
			Player playerToKick;

			do {
				int kickposition = ThreadLocalRandom.current().nextInt(GetGames.getMaxPlayers(game) - 1);
				playerToKick = getPlayersFromList().get(kickposition).getPlayer();
				isVIP = playerToKick.hasPermission("ragemode.vip");
			} while (isVIP);

			player.setMetadata("Leaving", new FixedMetadataValue(RageMode.getInstance(), true));
			Utils.clearPlayerInventory(playerToKick);
			pm.addBackTools(false);

			playerToKick.sendMessage(RageMode.getLang().get("game.player-kicked-for-vip"));

			final Player pl = playerToKick;
			players.entrySet().removeIf(u -> u.getKey().equals(pl));

			if (conf.getCV().getMinPlayers() > 1) {
				if (players.size() == conf.getCV().getMinPlayers()) {
					if (lobbyTimer == null) {
						lobbyTimer = new LobbyTimer(this, time);
						lobbyTimer.loadTimer();
					}
				} else {
					lobbyTimer = null;
					return false;
				}
			} else {
				if (players.size() == 2) {
					if (lobbyTimer == null) {
						lobbyTimer = new LobbyTimer(this, time);
						lobbyTimer.loadTimer();
					}
				} else {
					lobbyTimer = null;
					return false;
				}
			}

			player.sendMessage(RageMode.getLang().get("game.you-joined-the-game", "%game%", game));
			return true;
		}

		player.sendMessage(RageMode.getLang().get("game.full"));
		return false;
	}

	public boolean addSpectatorPlayer(Player player, String game) {
		PlayerManager pm = new PlayerManager(player, game);
		specPlayer.put(player, pm);

		if (!RageMode.getInstance().getConfiguration().getCV().isBungee()) {
			pm.storePlayerTools(true);
			Utils.clearPlayerInventory(player);
		}

		Utils.callEvent(new SpectatorJoinToGameEvent(this, player));

		return isSpectator(player);
	}

	public boolean removeSpectatorPlayer(Player player) {
		if (!RageMode.getInstance().getConfiguration().getCV().isSpectatorEnabled())
			return false;

		if (!isSpectator(player)) {
			return false;
		}

		if (!RageMode.getInstance().getConfiguration().getCV().isBungee()) {
			Utils.clearPlayerInventory(player);
			getSpectatorPlayerManager(player).addBackTools(true);
		}

		Utils.callEvent(
				new SpectatorLeaveGameEvent(GameUtils.getGame(specPlayer.get(player).getGameName()), player));

		specPlayer.remove(player);
		return true;
	}

	public boolean removePlayer(Player player) {
		if (!isInList(player)) {
			player.sendMessage(RageMode.getLang().get("game.player-not-ingame"));
			return false;
		}

		String game = getPlayersGame(player);
		if (game == null) {
			return false;
		}

		Utils.clearPlayerInventory(player);
		getPlayerManager(player).addBackTools(false);

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
		String game = getPlayersGame(player);
		// Just a null check if the player not find in the list
		if (game == null) {
			return;
		}

		if (ScoreBoard.allScoreBoards.containsKey(game))
			ScoreBoard.allScoreBoards.get(game).removeScoreBoard(player, true);

		if (TabTitles.allTabLists.containsKey(game))
			TabTitles.allTabLists.get(game).removeTabList(player);

		if (ScoreTeam.allTeams.containsKey(game))
			ScoreTeam.allTeams.get(game).removeTeam(player);
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
	 * @param game Game
	 * @return true if the game is exist and running.
	 */
	public boolean isGameRunning(String game) {
		Validate.notNull(game, "Game name can't be null!");
		Validate.notEmpty(game, "Game name can't be empty!");

		if (GetGames.isGameExistent(game) && running.get(game) != null && running.get(game)) {
			return true;
		}

		return false;
	}

	/**
	 * Sets the game running.
	 * @param game Game
	 * @return true if game exist and the game not running
	 */
	public boolean setGameRunning(String game) {
		Validate.notNull(game, "Game name can't be null!");
		Validate.notEmpty(game, "Game name can't be empty!");

		if (!GetGames.isGameExistent(game))
			return false;

		if (running.get(game) != null && running.get(game)) {
			return false;
		}

		running.put(game, true);
		return true;
	}

	/**
	 * Sets the game not running
	 * @param game Game
	 * @return true if game exist and the game is running
	 */
	public boolean setGameNotRunning(String game) {
		Validate.notNull(game, "Game name can't be null!");
		Validate.notEmpty(game, "Game name can't be empty!");

		if (!GetGames.isGameExistent(game))
			return false;

		if (running.get(game) != null && running.get(game)) {
			running.remove(game);
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
		Validate.notNull(game, "Game name can't be null!");
		Validate.notEmpty(game, "Game name can't be empty!");

		if (players != null) {
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
	 * Gets the specified player game from list.
	 * @param player Player
	 * @return game if player playing
	 */
	public String getPlayersGame(Player player) {
		Validate.notNull(player, "Player can't be null!");

		if (players != null) {
			for (Entry<Player, PlayerManager> players : players.entrySet()) {
				if (players.getKey().equals(player)) {
					return players.getValue().getGameName();
				}
			}
		}

		return null;
	}

	/**
	 * Gets the game by player uuid from list.
	 * @param uuid Player UUID
	 * @return game if player playing
	 */
	public String getPlayersGame(String uuid) {
		Validate.notNull(uuid, "UUID can't be null!");
		Validate.notEmpty(uuid, "UUID can't be empty!");

		return getPlayersGame(Bukkit.getPlayer(UUID.fromString(uuid)));
	}

	/**
	 * Gets the spectator player game by uuid from list.
	 * @param p Spectator player
	 * @return game if the spectator player is in game
	 */
	public String getSpectatorPlayerGame(Player p) {
		Validate.notNull(p, "Player can't be null!");

		if (specPlayer != null) {
			for (Entry<Player, PlayerManager> spec : specPlayer.entrySet()) {
				if (spec.getKey().equals(p)) {
					return spec.getValue().getGameName();
				}
			}
		}

		return null;
	}

	/**
	 * Gets the spectator player game by uuid from list.
	 * @param uuid Spectator player UUID
	 * @return game if the spectator player is in game
	 */
	public String getSpectatorPlayerGame(String uuid) {
		Validate.notNull(uuid, "UUID can't be null!");
		Validate.notEmpty(uuid, "UUID can't be empty!");

		return getSpectatorPlayerGame(UUID.fromString(uuid));
	}

	/**
	 * Gets the spectator player game by player from list.
	 * @param p Player
	 * @return game if the spectator player is in game
	 */
	public String getSpectatorPlayerGame(UUID uuid) {
		Validate.notNull(uuid, "UUID can't be null!");

		return getSpectatorPlayerGame(Bukkit.getPlayer(uuid));
	}

	/**
	 * Gets the spectator players converted to list.
	 * @return List of spectator players
	 */
	public List<Player> getSpectatorPlayersInList() {
		List<Player> list = new ArrayList<>();

		if (specPlayer != null) {
			for (Entry<Player, PlayerManager> entries : specPlayer.entrySet()) {
				list.add(entries.getKey());
			}
		}

		return list;
	}

	/**
	 * Get the player who playing in game.
	 * @param game Game
	 * @return Player who in game currently.
	 */
	public Player getPlayerInGame(String game) {
		Validate.notNull(game, "Game name can't be null!");
		Validate.notEmpty(game, "Game name can't be empty!");

		if (players != null) {
			for (Entry<Player, PlayerManager> players : players.entrySet()) {
				if (players.getValue().getGameName().equalsIgnoreCase(game)) {
					return players.getKey();
				}
			}
		}

		return null;
	}

	/**
	 * Gets the spectator player in the specified game.
	 * @param game Game
	 * @return Player if the player is in spectator
	 */
	public Player getSpectatorPlayer(String game) {
		Validate.notNull(game, "Game name can't be null!");
		Validate.notEmpty(game, "Game name can't be empty!");

		if (specPlayer != null) {
			for (Entry<Player, PlayerManager> spec : specPlayer.entrySet()) {
				if (spec.getValue().getGameName().equalsIgnoreCase(game)) {
					return spec.getKey();
				}
			}
		}

		return null;
	}

	/**
	 * Get the player by uuid from list.
	 * @param uuid Player UUID
	 * @return Player
	 */
	public Player getPlayerByUUID(String uuid) {
		Validate.notNull(uuid, "UUID can't be null!");
		Validate.notEmpty(uuid, "UUID can't be empty!");

		if (players != null) {
			for (Entry<Player, PlayerManager> players : players.entrySet()) {
				if (players.getKey().getUniqueId().equals(UUID.fromString(uuid))) {
					return players.getKey();
				}
			}
		}

		return null;
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
	 * Gets the players converted to list.
	 * @return List of players
	 */
	public List<Player> getPlayersInList() {
		List<Player> list = new ArrayList<>();

		if (players != null) {
			for (Entry<Player, PlayerManager> players : players.entrySet()) {
				list.add(players.getKey());
			}
		}

		return list;
	}

	/**
	 * Gets the {@link #PlayerManager} player converted to list.
	 * @return list of {@link #PlayerManager}
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
	 * Gets the {@link #PlayerManager} spectator players converted to list.
	 * @return list of {@link #PlayerManager}
	 */
	public List<PlayerManager> getSpectatorPlayersFromList() {
		List<PlayerManager> list = new ArrayList<>();

		if (players != null) {
			for (Entry<Player, PlayerManager> players : specPlayer.entrySet()) {
				list.add(players.getValue());
			}
		}

		return list;
	}

	/**
	 * Gets the {@link #PlayerManager} by spectator player.
	 * @param p Spectator player
	 * @return {@link #PlayerManager} by spectator player
	 */
	public PlayerManager getSpectatorPlayerManager(Player p) {
		Validate.notNull(p, "Player can't be null!");

		if (players != null) {
			for (Entry<Player, PlayerManager> players : specPlayer.entrySet()) {
				if (players.getKey().equals(p)) {
					return players.getValue();
				}
			}
		}

		return null;
	}

	/**
	 * Gets the {@link #PlayerManager} by player.
	 * @param p Player
	 * @return {@link #PlayerManager} by player
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
