package hu.montlikadani.ragemode.gameLogic;

import java.util.Arrays;
import java.util.Collection;
import java.util.Random;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.potion.PotionEffect;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;

import hu.montlikadani.ragemode.RageMode;
import hu.montlikadani.ragemode.Utils;
import hu.montlikadani.ragemode.API.event.GameJoinAttemptEvent;
import hu.montlikadani.ragemode.API.event.GameLeaveAttemptEvent;
import hu.montlikadani.ragemode.gameUtils.GetGames;
import hu.montlikadani.ragemode.gameUtils.ScoreBoard;
import hu.montlikadani.ragemode.gameUtils.TabTitles;
import hu.montlikadani.ragemode.gameUtils.TableList;
import hu.montlikadani.ragemode.scores.RageScores;

public class PlayerList {

	public static TableList<Player, Location> oldLocations = new TableList<>();
	public static TableList<Player, ItemStack[]> oldInventories = new TableList<>();
	public static TableList<Player, ItemStack[]> oldArmor = new TableList<>();
	public static TableList<Player, Double> oldHealth = new TableList<>();
	public static TableList<Player, Integer> oldHunger = new TableList<>();
	public static TableList<Player, Collection<PotionEffect>> oldEffects = new TableList<>();
	public static TableList<Player, GameMode> oldGameMode = new TableList<>();
	public static TableList<Player, String> oldDisplayName = new TableList<>();
	public static TableList<Player, String> oldListName = new TableList<>();
	public static TableList<Player, Integer> oldFire = new TableList<>();

	private static String[] list = new String[1]; // [Gamename,Playername x overallMaxPlayers,Gamename,...]
	private static String[] runningGames = new String[1];

	private static GameStatus status = GameStatus.STOPPED;

	public PlayerList() {
		int i = 0;
		int imax = GetGames.getConfigGamesCount();
		String[] games = GetGames.getGameNames();
		list = Arrays.copyOf(list, GetGames.getConfigGamesCount() * (GetGames.getOverallMaxPlayers() + 1));
		while (i < imax) {
			list[i * (GetGames.getOverallMaxPlayers() + 1)] = games[i];
			i++;
		}
		runningGames = Arrays.copyOf(runningGames, GetGames.getConfigGamesCount());
	}

	public static String[] getPlayersInGame(String game) {
		int maxPlayers = GetGames.getMaxPlayers(game);
		if (maxPlayers == -1)
			return null;
		String[] players = new String[maxPlayers];

		int i = 0;
		int n;
		int imax = GetGames.getConfigGamesCount() * (GetGames.getOverallMaxPlayers() + 1);
		int playersPerGame = GetGames.getOverallMaxPlayers();
		while (i < imax) {
			if (list[i] != null) {
				if (list[i].equals(game)) {
					n = i;
					int x = 0;
					while (n <= GetGames.getMaxPlayers(game) + i - 1) {
						if (list[n + 1] == null)
							n++;
						else {
							players[x] = list[n + 1];
							n++;
							x++;
						}
					}
					players = Arrays.copyOf(players, x);
				}
			}
			i = i + (playersPerGame + 1);
		}
		return players;
	}

	public static boolean addPlayer(Player player, String game) {
		if (isGameRunning(game)) {
			player.sendMessage(RageMode.getLang().get("game.running"));
			return false;
		}

		int i, n;
		i = 0;
		n = 0;
		int kickposition;
		int imax = GetGames.getConfigGamesCount() * (GetGames.getOverallMaxPlayers() + 1);
		int playersPerGame = GetGames.getOverallMaxPlayers();
		while (i < imax) {
			if (list[i] != null) {
				if (player.getUniqueId().toString().equals(list[i])) {
					player.sendMessage(RageMode.getLang().get("game.player-already-in-game", "%usage%", "/rm leave"));
					return false;
				}
			}
			i++;
		}
		i = 0;
		while (i < imax) {
			if (list[i] != null) {
				if (list[i].equals(game)) {
					n = i;
					n++; // should increase performance because the game name in the list isn't checked for null

					int time = 0;
					if (!RageMode.getInstance().getConfiguration().getArenasCfg().isSet("arenas." + game + ".lobbydelay")) {
						if (RageMode.getInstance().getConfiguration().getCfg().getInt("game.global.lobby.delay") > 0)
							time = RageMode.getInstance().getConfiguration().getCfg().getInt("game.global.lobby.delay");
						else
							time = 30;
					} else
						time = RageMode.getInstance().getConfiguration().getArenasCfg().getInt("arenas." + game + ".lobbydelay");

					while (n <= (GetGames.getMaxPlayers(game) + i)) {
						if (list[n] == null) {
							list[n] = player.getUniqueId().toString();
							player.sendMessage(RageMode.getLang().get("game.you-joined-the-game", "%game%", game));

							GameJoinAttemptEvent event = new GameJoinAttemptEvent(player, game);
							Bukkit.getPluginManager().callEvent(event);
							if (event.isCancelled())
								return false;

							if (RageMode.getInstance().getConfiguration().getCfg().getInt("game.global.lobby.min-players-to-start-lobby-timer") > 1) {
								if (getPlayersInGame(game).length == RageMode.getInstance().getConfiguration().getCfg()
										.getInt("game.global.lobby.min-players-to-start-lobby-timer"))
									new LobbyTimer(game, time).sendTimerMessages();
							} else {
								if (getPlayersInGame(game).length == 2)
									new LobbyTimer(game, time).sendTimerMessages();
							}
							return true;
						}
						n++;
					}
					if (player.hasPermission("ragemode.vip") && hasRoomForVIP(game)) {
						Random random = new Random();
						boolean isVIP = false;
						Player playerToKick;

						do {
							kickposition = random.nextInt(GetGames.getMaxPlayers(game) - 1);
							kickposition = kickposition + 1 + i;
							n = 0;
							playerToKick = Bukkit.getPlayer(UUID.fromString(list[kickposition]));
							isVIP = playerToKick.hasPermission("ragemode.vip");
						} while (isVIP);

						player.setMetadata("Leaving", new FixedMetadataValue(RageMode.getInstance(), true));

						while (n < oldLocations.getFirstLength()) { // Get him back to his old location.
							if (oldLocations.getFromFirstObject(n) == playerToKick) {
								playerToKick.teleport(oldLocations.getFromSecondObject(n));
								oldLocations.removeFromBoth(n);
							}
							n++;
						}

						n = 0;

						while (n < oldInventories.getFirstLength()) { // Give him his inventory back.
							if (oldInventories.getFromFirstObject(n) == playerToKick) {
								playerToKick.getInventory().clear();
								playerToKick.getInventory().setContents(oldInventories.getFromSecondObject(n));
								oldInventories.removeFromBoth(n);
							}
							n++;
						}

						n = 0;

						while (n < oldArmor.getFirstLength()) { // Give him his armor back.
							if (oldArmor.getFromFirstObject(n) == playerToKick) {
								playerToKick.getInventory().setArmorContents(oldArmor.getFromSecondObject(n));
								oldArmor.removeFromBoth(n);
							}
							n++;
						}

						n = 0;

						while (n < oldHealth.getFirstLength()) { // Give him his health back.
							if (oldHealth.getFromFirstObject(n) == playerToKick) {
								playerToKick.setHealth(oldHealth.getFromSecondObject(n));
								oldHealth.removeFromBoth(n);
							}
							n++;
						}

						n = 0;

						while (n < oldHunger.getFirstLength()) { // Give him his hunger back.
							if (oldHunger.getFromFirstObject(n) == playerToKick) {
								playerToKick.setFoodLevel(oldHunger.getFromSecondObject(n));
								oldHunger.removeFromBoth(n);
							}
							n++;
						}

						n = 0;

						while (n < oldEffects.getFirstLength()) { // Give him his potion effects back.
							if (oldEffects.getFromFirstObject(n) == playerToKick) {
								playerToKick.addPotionEffects(oldEffects.getFromSecondObject(n));
								oldEffects.removeFromBoth(n);
							}
							n++;
						}

						n = 0;

						while (n < oldGameMode.getFirstLength()) { // Give him his gamemode back.
							if (oldGameMode.getFromFirstObject(n) == playerToKick) {
								playerToKick.setGameMode(oldGameMode.getFromSecondObject(n));
								oldGameMode.removeFromBoth(n);
							}
							n++;
						}

						n = 0;

						while (n < oldListName.getFirstLength()) { // Give him his list name back.
							if (oldListName.getFromFirstObject(n) == playerToKick) {
								playerToKick.setPlayerListName(oldListName.getFromSecondObject(n));
								oldListName.removeFromBoth(n);
							}
							n++;
						}

						n = 0;

						while (n < oldDisplayName.getFirstLength()) { // Give him his display name back.
							if (oldDisplayName.getFromFirstObject(n) == playerToKick) {
								playerToKick.setDisplayName(oldDisplayName.getFromSecondObject(n));
								oldDisplayName.removeFromBoth(n);
							}
							n++;
						}

						n = 0;

						while (n < oldFire.getFirstLength()) { // Give him his fire back.
							if (oldFire.getFromFirstObject(n) == playerToKick) {
								playerToKick.setFireTicks(oldFire.getFromSecondObject(n));
								oldFire.removeFromBoth(n);
							}
							n++;
						}

						list[kickposition] = player.getUniqueId().toString();
						playerToKick.sendMessage(RageMode.getLang().get("game.player-kicked-for-vip"));

						if (RageMode.getInstance().getConfiguration().getCfg().getInt("game.global.lobby.min-players-to-start-lobby-timer") > 1) {
							if (getPlayersInGame(game).length == RageMode.getInstance().getConfiguration().getCfg()
									.getInt("game.global.lobby.min-players-to-start-lobby-timer"))
								new LobbyTimer(game, time).sendTimerMessages();
						} else {
							if (getPlayersInGame(game).length == 2)
								new LobbyTimer(game, time).sendTimerMessages();
						}
						player.sendMessage(RageMode.getLang().get("game.you-joined-the-game", "%game%", game));
						return true;
					} else {
						player.sendMessage(RageMode.getLang().get("game.full"));
						return false;
					}
				}
			}
			i = i + playersPerGame + 1;
		}

		player.sendMessage(RageMode.getLang().get("game.does-not-exist"));
		return false;
	}

	public static boolean removePlayer(Player player) {
		if (!player.hasMetadata("leavingRageMode")) {
			player.setMetadata("leavingRageMode", new FixedMetadataValue(RageMode.getInstance(), true));

			int i = 0;
			int n = 0;
			int imax = GetGames.getConfigGamesCount() * (GetGames.getOverallMaxPlayers() + 1);

			while (i < imax) {
				if (list[i] != null) {
					if (list[i].equals(player.getUniqueId().toString())) {

						GameLeaveAttemptEvent gameLeaveEvent = new GameLeaveAttemptEvent(player, PlayerList.getPlayersGame(player));
						Bukkit.getPluginManager().callEvent(gameLeaveEvent);

						removePlayerSynced(player);

						RageScores.removePointsForPlayers(new String[] { player.getUniqueId().toString() });

						player.getInventory().clear();
						player.sendMessage(RageMode.getLang().get("game.player-left"));

						player.setMetadata("Leaving", new FixedMetadataValue(RageMode.getInstance(), true));

						while (n < oldLocations.getFirstLength()) { // Bring him back to his old location
							if (oldLocations.getFromFirstObject(n) == player) {
								player.teleport(oldLocations.getFromSecondObject(n));
								oldLocations.removeFromBoth(n);
							}
							n++;
						}

						n = 0;

						while (n < oldInventories.getFirstLength()) { // Give him his inventory back
							if (oldInventories.getFromFirstObject(n) == player) {
								player.getInventory().setContents(oldInventories.getFromSecondObject(n));
								oldInventories.removeFromBoth(n);
							}
							n++;
						}

						n = 0;

						while (n < oldArmor.getFirstLength()) {
							if (oldArmor.getFromFirstObject(n) == player) { // Give him his armor back
								player.getInventory().setArmorContents(oldArmor.getFromSecondObject(n));
								oldArmor.removeFromBoth(n);
							}
							n++;
						}

						n = 0;

						while (n < oldHealth.getFirstLength()) { // Give him his health back.
							if (oldHealth.getFromFirstObject(n) == player) {
								player.setHealth(oldHealth.getFromSecondObject(n));
								oldHealth.removeFromBoth(n);
							}
							n++;
						}

						n = 0;

						while (n < oldHunger.getFirstLength()) { // Give him his hunger back.
							if (oldHunger.getFromFirstObject(n) == player) {
								player.setFoodLevel(oldHunger.getFromSecondObject(n));
								oldHunger.removeFromBoth(n);
							}
							n++;
						}

						n = 0;

						while (n < oldEffects.getFirstLength()) { // Give him his potion effects back.
							if (oldEffects.getFromFirstObject(n) == player) {
								player.addPotionEffects(oldEffects.getFromSecondObject(n));
								oldEffects.removeFromBoth(n);
							}
							n++;
						}

						n = 0;

						while (n < oldGameMode.getFirstLength()) { // Give him his gamemode back.
							if (oldGameMode.getFromFirstObject(n) == player) {
								player.setGameMode(oldGameMode.getFromSecondObject(n));
								oldGameMode.removeFromBoth(n);
							}
							n++;
						}

						n = 0;

						while (n < oldListName.getFirstLength()) { // Give him his list name back.
							if (oldListName.getFromFirstObject(n) == player) {
								player.setPlayerListName(oldListName.getFromSecondObject(n));
								oldListName.removeFromBoth(n);
							}
							n++;
						}

						n = 0;

						while (n < oldDisplayName.getFirstLength()) { // Give him his display name back.
							if (oldDisplayName.getFromFirstObject(n) == player) {
								player.setDisplayName(oldDisplayName.getFromSecondObject(n));
								oldDisplayName.removeFromBoth(n);
							}
							n++;
						}

						n = 0;

						while (n < oldFire.getFirstLength()) { // Give him his fire back.
							if (oldFire.getFromFirstObject(n) == player) {
								player.setFireTicks(oldFire.getFromSecondObject(n));
								oldFire.removeFromBoth(n);
							}
							n++;
						}

						list[i] = null;

						player.removeMetadata("leavingRageMode", RageMode.getInstance());
						return true;
					}
				}
				i++;
			}
			player.sendMessage(RageMode.getLang().get("game.player-not-ingame"));
			return false;
		}
		player.removeMetadata("leavingRageMode", RageMode.getInstance());
		return false;
	}

	public static void removePlayerSynced(Player player) {
		if (ScoreBoard.allScoreBoards.containsKey(PlayerList.getPlayersGame(player)))
			ScoreBoard.allScoreBoards.get(PlayerList.getPlayersGame(player)).removeScoreBoard(player);

		if (TabTitles.allTabLists.containsKey(PlayerList.getPlayersGame(player)))
			TabTitles.allTabLists.get(PlayerList.getPlayersGame(player)).removeTabList(player);

		removeScoreboard(player, true);
	}

	public static boolean isGameRunning(String game) {
		int i = 0;
		int imax = runningGames.length;
		while (i < imax) {
			if (runningGames[i] != null) {
				if (runningGames[i].trim().equalsIgnoreCase(game.trim()))
					return true;
			}
			i++;
		}
		return false;
	}

	public static boolean setGameRunning(String game) {
		if (!GetGames.isGameExistent(game))
			return false;

		int i = 0;
		int imax = runningGames.length;
		while (i < imax) {
			if (runningGames[i] != null) {
				if (runningGames[i].equals(game))
					return false;
			}
			i++;
		}
		i = 0;
		while (i < imax) {
			if (runningGames[i] == null) {
				runningGames[i] = game;
				return true;
			}
			i++;
		}
		return false;
	}

	public static boolean setGameNotRunning(String game) {
		if (!GetGames.isGameExistent(game))
			return false;

		int i = 0;
		int imax = runningGames.length;

		while (i < imax) {
			if (runningGames[i] != null) {
				if (runningGames[i].equals(game)) {
					runningGames[i] = null;
					return true;
				}
			}
			i++;
		}
		return false;
	}

	public static boolean isPlayerPlaying(String player) {
		if (player != null) {
			int i = 0;
			int imax = list.length;

			while (i < imax) {
				if (list[i] != null) {
					if (list[i].equals(player))
						return true;
				}
				i++;
			}
		}
		return false;
	}

	public static boolean hasRoomForVIP(String game) {
		String[] players = getPlayersInGame(game);
		int i = 0;
		int imax = players.length;
		int vipsInGame = 0;

		while (i < imax) {
			if (players[i] != null) {
				if (Bukkit.getPlayer(UUID.fromString(players[i])).hasPermission("ragemode.vip"))
					vipsInGame++;
			}
			i++;
		}

		if (vipsInGame == players.length)
			return false;
		return true;
	}

	public static void addGameToList(String game, int maxPlayers) {
		if (GetGames.getOverallMaxPlayers() < maxPlayers) {
			String[] oldList = list;
			list = Arrays.copyOf(list, (GetGames.getConfigGamesCount() + 1) * (maxPlayers + 1));
			int i = 0;
			int imax = oldList.length;
			int n = 0;
			int nmax = (GetGames.getOverallMaxPlayers() + 1);

			while (i < imax) {
				while (n < nmax + i) {
					list[n + i] = oldList[n + i];
					n++;
				}
				i = i + maxPlayers + 1;
				n = i;
			}

			list[i] = game;
		} else {
			String[] oldList = list;
			list = Arrays.copyOf(list, (GetGames.getConfigGamesCount() + 1) * (GetGames.getOverallMaxPlayers() + 1));
			int i = 0;
			int imax = oldList.length;

			while (i < imax) {
				list[i] = oldList[i];
				i++;
			}

			list[i] = game;
		}

		String[] oldRunningGames = runningGames;
		runningGames = Arrays.copyOf(runningGames, (runningGames.length + 1));
		int i = 0;
		int imax = runningGames.length - 1;

		while (i < imax) {
			runningGames[i] = oldRunningGames[i];
			i++;
		}
	}

	public static void deleteGameFromList(String game) {
		String[] playersInGame = getPlayersInGame(game);
		if (playersInGame != null) {
			int i = 0;
			int imax = playersInGame.length;
			while (i < imax) {
				if (playersInGame[i] != null)
					removePlayer(Bukkit.getPlayer(UUID.fromString(playersInGame[i])));
				i++;
			}
		}
		int i = 0;
		int imax = list.length;
		int gamePos = imax;
		int nextGamePos = imax;

		while (i < imax) {
			if (list[i] != null) {
				if (list[i].equals(game)) {
					gamePos = i;
					int n = 0;
					int nmax = GetGames.getOverallMaxPlayers() + 1;

					while (n < nmax) {
						list[n + i] = null;
						n++;
					}
					nextGamePos = i + nmax;
				}
			}
			i++;
		}
		i = nextGamePos;

		while (i < imax) {
			list[gamePos] = list[i];
			list[i] = null;
			i++;
			gamePos++;
		}
		String[] oldList = new String[(GetGames.getConfigGamesCount() - 1) * (GetGames.getOverallMaxPlayers() + 1)];
		int g = 0;
		int gmax = oldList.length;

		while (g < gmax) {
			oldList[g] = list[g];
			g++;
		}

		list = Arrays.copyOf(list, oldList.length);

		g = 0;

		while (g < gmax) {
			list[g] = oldList[g];
			g++;
		}
	}

	public static String getPlayersGame(Player player) {
		String sPlayer = player.getUniqueId().toString();
		String game = null;

		int i = 0;
		int imax = list.length;
		int playersPerGame = GetGames.getOverallMaxPlayers();

		while (i < imax) {
			if (list[i] != null) {
				if ((i % (playersPerGame + 1)) == 0)
					game = list[i];
				if (list[i].equals(sPlayer))
					return game;
			}
			i++;
		}
		return null;
	}

	public static void setPlayerGroup(Player player, String prefix, String suffix, boolean external) {
		if (player == null) {
			throw new NullPointerException("player is null");
		}
		Scoreboard board = external ? player.getScoreboard() : Bukkit.getScoreboardManager().getNewScoreboard();
		Team team = getScoreboardTeam(board, player.getName());

		team.addEntry(player.getName());

		prefix = Utils.setPlaceholders(prefix, player);
		suffix = Utils.setPlaceholders(suffix, player);

		team.setPrefix(prefix);
		team.setSuffix(suffix);
		if (RageMode.getVersion().contains("1.13"))
			team.setColor(Utils.fromPrefix(prefix));

		player.setScoreboard(board);
	}

	public static void removeScoreboard(Player player, boolean external) {
		Scoreboard board = external ? player.getScoreboard() : Bukkit.getScoreboardManager().getNewScoreboard();
		if (getScoreboardTeam(board, player.getName()) != null) {
			getScoreboardTeam(board, player.getName()).unregister();
			player.setScoreboard(board);
		}
	}

	public static Team getScoreboardTeam(Scoreboard board, String name) {
		return board.getTeam(name) == null ? board.registerNewTeam(name) : board.getTeam(name);
	}

	public static GameStatus getStatus() {
		return status;
	}

	public static void setStatus(GameStatus status) {
		PlayerList.status = status;
	}
}
