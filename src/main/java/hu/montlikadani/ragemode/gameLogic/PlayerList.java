package hu.montlikadani.ragemode.gameLogic;

import java.util.Arrays;
import java.util.Random;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.metadata.FixedMetadataValue;

import hu.montlikadani.ragemode.RageMode;
import hu.montlikadani.ragemode.scoreboard.ScoreBoard;
import hu.montlikadani.ragemode.scores.RageScores;
import hu.montlikadani.ragemode.toolbox.GetGames;
import hu.montlikadani.ragemode.toolbox.TableList;

public class PlayerList {

	public static TableList<Player, Location> oldLocations = new TableList<>();
	public static TableList<Player, ItemStack[]> oldInventories = new TableList<>();
	public static TableList<Player, ItemStack[]> oldArmor = new TableList<>();
	public static TableList<Player, Double> oldHealth = new TableList<>();
	public static TableList<Player, Integer> oldHunger = new TableList<>();
	public static TableList<Player, GameMode> oldGameMode = new TableList<>();

	private static String[] list = new String[1]; // [Gamename,Playername x overallMaxPlayers,Gamename,...]
	private static String[] runningGames = new String[1];

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
					while (n <= (GetGames.getMaxPlayers(game) + i)) {
						if (list[n] == null) {
							list[n] = player.getUniqueId().toString();
							player.sendMessage(RageMode.getLang().get("game.you-joined-the-game", "%game%", game));

							if (getPlayersInGame(game).length == 2)
								new LobbyTimer(game);
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

						while (n < oldGameMode.getFirstLength()) { // Give him his gamemode back.
							if (oldGameMode.getFromFirstObject(n) == playerToKick) {
								playerToKick.setGameMode(oldGameMode.getFromSecondObject(n));
								oldGameMode.removeFromBoth(n);
							}
							n++;
						}

						list[kickposition] = player.getUniqueId().toString();
						playerToKick.sendMessage(RageMode.getLang().get("game.player-kicked-for-vip"));

						if (getPlayersInGame(game).length == 2)
							new LobbyTimer(game);
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
						// TabGuiUpdater.removeTabForPlayer(player);

						// org.mcsg.double0negative.tabapi.TabAPI.disableTabForPlayer(player);
						// org.mcsg.double0negative.tabapi.TabAPI.updatePlayer(player);

						// if(ScoreBoard.allScoreBoards.containsKey(PlayerList.getPlayersGame(player)))
						// ScoreBoard.allScoreBoards.get(PlayerList.getPlayersGame(player)).removeScoreBoard(player);

						RageScores.removePointsForPlayers(new String[] { player.getUniqueId().toString() });

						// BossbarLib.getHandler().clearBossbar(player);

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
								player.getInventory().clear();
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

		int n = 0;
		while (n < oldGameMode.getFirstLength()) { // Give him his gamemode back.
			if (oldGameMode.getFromFirstObject(n) == player) {
				player.setGameMode(oldGameMode.getFromSecondObject(n));
				oldGameMode.removeFromBoth(n);
			}
			n++;
		}
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
}
