package hu.montlikadani.ragemode.gameUtils;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Map.Entry;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;
import java.util.logging.Level;

import org.apache.commons.lang.Validate;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.util.Vector;

import hu.montlikadani.ragemode.Debug;
import hu.montlikadani.ragemode.RageMode;
import hu.montlikadani.ragemode.Utils;
import hu.montlikadani.ragemode.API.event.GameStopEvent;
import hu.montlikadani.ragemode.commands.RmCommand;
import hu.montlikadani.ragemode.config.Configuration;
import hu.montlikadani.ragemode.events.EventListener;
import hu.montlikadani.ragemode.gameLogic.GameSpawnGetter;
import hu.montlikadani.ragemode.gameLogic.GameStatus;
import hu.montlikadani.ragemode.gameLogic.Reward.Reward;
import hu.montlikadani.ragemode.gameLogic.Game;
import hu.montlikadani.ragemode.holder.HoloHolder;
import hu.montlikadani.ragemode.items.CombatAxe;
import hu.montlikadani.ragemode.items.ForceStarter;
import hu.montlikadani.ragemode.items.Grenade;
import hu.montlikadani.ragemode.items.LeaveGame;
import hu.montlikadani.ragemode.items.RageArrow;
import hu.montlikadani.ragemode.items.RageBow;
import hu.montlikadani.ragemode.items.RageKnife;
import hu.montlikadani.ragemode.runtimePP.RuntimePPManager;
import hu.montlikadani.ragemode.scores.PlayerPoints;
import hu.montlikadani.ragemode.scores.RageScores;
import hu.montlikadani.ragemode.signs.SignCreator;
import hu.montlikadani.ragemode.statistics.MySQLThread;
import hu.montlikadani.ragemode.statistics.SQLThread;
import hu.montlikadani.ragemode.statistics.YAMLStats;

import static hu.montlikadani.ragemode.utils.Message.sendMessage;

public class GameUtils {

	private static Map<String, GameStatus> status = new HashMap<>();

	/**
	 * Broadcast for in-game players to the specified game
	 * @param game Game
	 * @param message Message
	 */
	public static void broadcastToGame(String game, String message) {
		for (Entry<String, String> players : Game.getPlayers().entrySet()) {
			if (players != null) {
				Player p = Bukkit.getPlayer(UUID.fromString(players.getValue()));
				if (p != null && Game.getPlayersGame(p).equals(game))
					p.sendMessage(message);
			}
		}
	}

	/**
	 * Checks the game name if contains special chars, too long or contains
	 * a ragemode command.
	 * @param pl Player
	 * @param name Game
	 * @return false if:
	 * <br>- contains special chars
	 * <br>- the name is too long
	 * <br>- in the name contains a ragemode command
	 */
	public static boolean checkName(Player pl, String name) {
		if (!name.matches("^[a-zA-Z0-9\\_\\-]+$")) {
			if (pl != null) {
				sendMessage(pl, RageMode.getLang().get("setup.addgame.special-chars"));
			}
			return false;
		} else if (name.length() > 20) {
			if (pl != null) {
				sendMessage(pl, RageMode.getLang().get("setup.addgame.name-greater"));
			}
			return false;
		} else if (reservedNames.contains(name)) { // hehe
			if (pl != null) {
				sendMessage(pl, RageMode.getLang().get("setup.addgame.bad-name"));
			}
			return false;
		}

		return true;
	}


	/**
	 * Checks whatever the specified game is exists or no.
	 * @param game Game
	 * @return true if game exists
	 */
	public static boolean isGameWithNameExists(String game) {
		return GetGames.isGameExistent(game);
	}

	/**
	 * Get the game spawn by name.
	 * @param name Game name
	 * @return GameSpawnGetter
	 */
	public static GameSpawnGetter getGameSpawnByName(String name) {
		Validate.notNull(name, "Game name can't be null!");
		Validate.notEmpty(name, "Game name can't be empty!");

		for (GameSpawnGetter gsg : RageMode.getInstance().getSpawns()) {
			if (gsg.getGameName().equalsIgnoreCase(name))
				return gsg;
		}
		return null;
	}

	/**
	 * Give game items to the specified player. If the item slot not found
	 * in configuration, then adds the item to the inventory.
	 * @param p Player
	 * @param clear - if true clears the player inventory before add items
	 */
	public static void addGameItems(Player p, boolean clear) {
		PlayerInventory inv = p.getInventory();
		if (clear)
			Utils.clearPlayerInventory(p);

		ItemStack result = null;
		FileConfiguration f = RageMode.getInstance().getConfiguration().getCfg();
		String path = "items.";
		if (f.contains(path + "rageBow.slot"))
			inv.setItem(f.getInt("items.rageBow.slot"), RageBow.getItem());
		else
			result = RageBow.getItem();

		if (f.contains(path + "rageKnife.slot"))
			inv.setItem(f.getInt("items.rageKnife.slot"), RageKnife.getItem());
		else
			result = RageKnife.getItem();

		if (f.contains(path + "combatAxe.slot"))
			inv.setItem(f.getInt("items.combatAxe.slot"), CombatAxe.getItem());
		else
			result = CombatAxe.getItem();

		if (f.contains(path + "rageArrow.slot"))
			inv.setItem(f.getInt("items.rageArrow.slot"), RageArrow.getItem());
		else
			result = RageArrow.getItem();

		if (f.contains(path + "grenade.slot"))
			inv.setItem(f.getInt("items.grenade.slot"), Grenade.getItem());
		else
			result = Grenade.getItem();

		if (result != null)
			inv.addItem(result);
	}

	/**
	 * Saves the player data to a yaml file
	 * <p>This prevents losing the player data when the server has stopped randomly.
	 * @param p Player
	 */
	public static void savePlayerData(Player p) {
		PlayerInventory inv = p.getInventory();
		Configuration conf = RageMode.getInstance().getConfiguration();

		Game.oldLocation = p.getLocation();
		Game.oldInventories = inv.getContents();
		Game.oldArmor = inv.getArmorContents();
		Game.oldHealth = p.getHealth();
		Game.oldHunger = p.getFoodLevel();

		if (!p.getActivePotionEffects().isEmpty())
			Game.oldEffects = p.getActivePotionEffects();

		Game.oldGameMode = p.getGameMode();

		if (!p.getDisplayName().equals(p.getDisplayName()))
			Game.oldDisplayName = p.getDisplayName();

		if (!p.getPlayerListName().equals(p.getPlayerListName()))
			Game.oldListName = p.getPlayerListName();

		if (p.getFireTicks() > 0)
			Game.oldFire = p.getFireTicks();

		if (p.getExp() > 0d)
			Game.oldExp = p.getExp();

		if (p.getLevel() > 0)
			Game.oldExpLevel = p.getLevel();

		if (p.isInsideVehicle())
			Game.oldVehicle = p.getVehicle();

		if (conf.getDatasFile() != null && conf.getDatasFile().exists()) {
			org.bukkit.configuration.file.FileConfiguration data = conf.getDatasCfg();
			String path = "datas." + p.getName() + ".";

			data.set(path + "location", p.getLocation());
			data.set(path + "contents", inv.getContents());
			data.set(path + "armor-contents", inv.getArmorContents());
			data.set(path + "health", p.getHealth());
			data.set(path + "food", p.getFoodLevel());
			if (!p.getActivePotionEffects().isEmpty())
				data.set(path + "potion-effects", p.getActivePotionEffects());

			// Using the gamemode name to prevent InvalidConfiguration error
			data.set(path + "game-mode", p.getGameMode().name());

			if (!p.getDisplayName().equals(p.getDisplayName()))
				data.set(path + "display-name", p.getDisplayName());

			if (!p.getPlayerListName().equals(p.getPlayerListName()))
				data.set(path + "list-name", p.getPlayerListName());

			if (p.getFireTicks() > 0)
				data.set(path + "fire-ticks", p.getFireTicks());

			if (p.getExp() > 0d)
				data.set(path + "exp", p.getExp());

			if (p.getLevel() > 0)
				data.set(path + "level", p.getLevel());

			if (p.isInsideVehicle()) {
				data.set(path + "vehicle", p.getVehicle().getType());
				data.set(path + "vehicle", p.getVehicle().getLocation());
			}

			Configuration.saveFile(data, conf.getDatasFile());
		}

		clearPlayerTools(p);
	}

	/**
	 * Connect the specified player to the game. If the game is
	 * running and the player is not playing, then if want to
	 * join to the game, switching to spectator mode.
	 * 
	 * @param p Player
	 * @param game GameName
	 */
	public static void joinPlayer(Player p, String game) {
		PlayerInventory inv = p.getInventory();
		Configuration conf = RageMode.getInstance().getConfiguration();

		if (getStatus(game) == GameStatus.RUNNING) {
			if (conf.getCV().isSpectatorEnabled()) {
				if (!Game.isPlayerPlaying(p.getUniqueId().toString())) {
					if (Game.addSpectatorPlayer(p, game)) {
						getGameSpawnByName(game).randomSpawn(p);

						p.setAllowFlight(true);
						p.setFlying(true);
						p.setGameMode(GameMode.SPECTATOR);

						if (conf.getCfg().contains("items.leavegameitem"))
							inv.setItem(conf.getCfg().getInt("items.leavegameitem.slot"), LeaveGame.getItem());
					}
				} else
					p.sendMessage(RageMode.getLang().get("game.player-not-switch-spectate"));
			} else
				p.sendMessage(RageMode.getLang().get("game.player-already-in-game", "%usage%", "/rm leave"));
		} else {
			if (getStatus(game) == GameStatus.NOTREADY) {
				p.sendMessage(RageMode.getLang().get("commands.join.game-locked"));
				return;
			}

			if (Game.isPlayerPlaying(p.getUniqueId().toString())) {
				p.sendMessage(RageMode.getLang().get("game.player-already-in-game", "%usage%", "/rm leave"));
				return;
			}

			MapChecker mapChecker = new MapChecker(game);
			if (!mapChecker.isValid()) {
				p.sendMessage(mapChecker.getMessage());
				return;
			}

			if (conf.getCV().isRequireEmptyInv()) {
				for (ItemStack armor : inv.getArmorContents()) {
					if (armor != null && !armor.getType().equals(Material.AIR)) {
						p.sendMessage(RageMode.getLang().get("commands.join.empty-inventory.armor"));
						return;
					}
				}

				for (ItemStack content : inv.getContents()) {
					if (content != null && !content.getType().equals(Material.AIR)) {
						p.sendMessage(RageMode.getLang().get("commands.join.empty-inventory.contents"));
						return;
					}
				}
			} else if (conf.getCV().isSavePlayerData())
				savePlayerData(p);

			if (Game.addPlayer(p, game)) {
				p.teleport(GetGameLobby.getLobbyLocation(game));

				runCommands(p, game, "join");
				sendActionBarMessages(p, game, "join");

				if (conf.getCfg().contains("items.leavegameitem"))
					inv.setItem(conf.getCfg().getInt("items.leavegameitem.slot"), LeaveGame.getItem());

				if (conf.getCfg().contains("items.force-start") && p.hasPermission("ragemode.admin.item.forcestart"))
					inv.setItem(conf.getCfg().getInt("items.force-start.slot"), ForceStarter.getItem());

				broadcastToGame(game, RageMode.getLang().get("game.player-joined", "%player%", p.getName()));

				String title = conf.getCV().getTitleJoinGame();
				String subtitle = conf.getCV().getSubTitleJoinGame();
				if (title != null && subtitle != null) {
					title = title.replace("%game%", game);
					subtitle = subtitle.replace("%game%", game);

					String[] split = conf.getCV().getJoinTitleTime().split(", ");
					if (split.length == 3) {
						Titles.sendTitle(p, Integer.parseInt(split[0]), Integer.parseInt(split[1]),
								Integer.parseInt(split[2]), title, subtitle);
					}
				}

				SignCreator.updateAllSigns(game);
			} else
				Bukkit.getConsoleSender().sendMessage(
						RageMode.getLang().get("game.player-could-not-join", "%player%", p.getName(), "%game%", game));
		}
	}

	/**
	 * Kicks the specified player from the game.
	 * @param p Player
	 */
	public static void kickPlayer(Player p) {
		// Just removes the spec player
		Game.removeSpectatorPlayer(p);

		if (Game.isPlayerPlaying(p.getUniqueId().toString()) && getStatus(Game.getPlayersGame(p)) == GameStatus.RUNNING) {
			if (Game.removePlayer(p)) {
				Debug.logConsole("Player " + p.getName() + " left the server while playing.");

				List<String> list = RageMode.getInstance().getConfiguration().getCV().getCmdsForPlayerLeave();
				if (list != null && !list.isEmpty()) {
					for (String cmds : list) {
						cmds = cmds.replace("%player%", p.getName());
						// For ipban
						cmds = cmds.replace("%player-ip%", p.getAddress().getAddress().getHostAddress());
						Bukkit.dispatchCommand(Bukkit.getServer().getConsoleSender(), RageMode.getLang().colors(cmds));
					}
				}
			}
		}

		HoloHolder.deleteHoloObjectsOfPlayer(p);
	}

	/**
	 * Fully clears the specified player inventory, remove effects, food and health set to 0 and
	 * more related to player.
	 * @param p Player
	 */
	public static void clearPlayerTools(Player p) {
		Utils.clearPlayerInventory(p);
		p.setGameMode(GameMode.SURVIVAL);
		p.setFlying(false);
		p.setHealth(20);
		p.setFoodLevel(20);
		p.setFireTicks(0);
		p.setExp(0);
		p.setLevel(0);

		if (p.isInsideVehicle())
			p.leaveVehicle();

		if (!p.getActivePotionEffects().isEmpty())
			p.getActivePotionEffects().forEach(e -> p.removePotionEffect(e.getType()));

		p.setDisplayName(p.getName());
		p.setPlayerListName(p.getName());
	}

	/**
	 * Run commands in game, when the player doing something in game
	 * such as it died, joining, starting or stopping game.
	 * @param game Game name
	 * @param cmdType Command type, such as death, join or other
	 */
	public static void runCommandsForAll(String game, String cmdType) {
		for (Entry<String, String> players : Game.getPlayers().entrySet()) {
			Player p = Game.getPlayerByUUID(players.getValue());
			runCommands(p, game, cmdType);
		}
	}

	/**
	 * Run commands in game, when the player doing something in game
	 * such as it died, joining, starting or stopping game.
	 * @param p Player
	 * @param game Game name
	 * @param cmdType Command type, such as death, join or other
	 */
	public static void runCommands(Player p, String game, String cmdType) {
		if (!RageMode.getInstance().getConfiguration().getCV().isRewardEnabled()) {
			return;
		}

		List<String> list = RageMode.getInstance().getConfiguration().getRewardsCfg()
				.getStringList("rewards.in-game.run-commands");

		if (list != null && !list.isEmpty()) {
			for (String cmd : list) {
				if (cmd.split(":").length < 3 && cmd.split(":").length > 4) {
					Debug.logConsole(Level.WARNING, "In the rewards file the in-game commands the split length is equal to 3.");
					continue;
				}

				if (cmd.contains("chance:")) {
					String value = cmd;
					value = value.split("chance:")[1].replaceAll("[^0-9]+", "");
					double chance = Double.parseDouble(value);

					if (ThreadLocalRandom.current().nextInt(0, 100) > chance)
						continue;

					cmd = cmd.replace("chance:" + value + "-", "");

					String type = cmd.split(":")[0];
					if (type.equals(cmdType)) {
						String consoleOrPlayer = cmd.split(":")[1];

						cmd = cmd.split(":")[2].replace("%world%", p.getWorld().getName())
								.replace("%game%", game)
								.replace("%player%", p.getName());
						cmd = RageMode.getLang().colors(cmd);

						if (consoleOrPlayer.equals("console"))
							Bukkit.dispatchCommand(Bukkit.getConsoleSender(), cmd);
						else if (consoleOrPlayer.equals("player"))
							p.performCommand(cmd);
					}
				} else {
					String type = cmd.split(":")[0];
					if (type.equals(cmdType)) {
						String consoleOrPlayer = cmd.split(":")[1];

						cmd = cmd.split(":")[2].replace("%world%", p.getWorld().getName())
								.replace("%game%", game)
								.replace("%player%", p.getName());
						cmd = RageMode.getLang().colors(cmd);

						if (consoleOrPlayer.equals("console"))
							Bukkit.dispatchCommand(Bukkit.getConsoleSender(), cmd);
						else if (consoleOrPlayer.equals("player"))
							p.performCommand(cmd);
					}
				}
			}
		}
	}

	/**
	 * Send action bar messages to the player when doing something,
	 * such as joining, leave, starting or stopping game.
	 * <p>This returns if the actionbar option is disabled in configurations.
	 * @param p Player
	 * @param game Game name
	 * @param type Action type
	 */
	public static void sendActionBarMessages(Player p, String game, String type) {
		Configuration conf = RageMode.getInstance().getConfiguration();

		if (conf.getArenasCfg().isSet("arenas." + game + ".actionbar")) {
			if (!conf.getArenasCfg().getBoolean("arenas." + game + ".actionbar"))
				return;
		} else if (!conf.getCV().isActionbarEnabled())
			return;

		List<String> list = conf.getCV().getActionbarActions();

		if (list != null && !list.isEmpty()) {
			for (String msg : list) {
				if (msg.split(":").length < 2 && msg.split(":").length > 2) {
					Debug.logConsole(Level.WARNING, "In the config file the actionbar messages the split length is equal to 2.");
					continue;
				}

				String action = msg.split(":")[0];
				if (action.equals(type)) {
					String message = msg.split(":")[1];
					message = message.replace("%game%", game).replace("%player%", p.getName());
					ActionBar.sendActionBar(p, RageMode.getLang().colors(message));
				}
			}
		}
	}

	/**
	 * Teleports players to a random location.
	 * This will return if the spawns size 0 because with value 0 are not possible.
	 * @param spawn GameSpawnGetter
	 */
	public static void teleportPlayersToGameSpawns(GameSpawnGetter spawn) {
		for (Entry<String, String> uuids : Game.getPlayers().entrySet()) {
			Player player = Bukkit.getPlayer(UUID.fromString(uuids.getValue()));
			teleportPlayerToGameSpawn(player, spawn);
		}
	}

	/**
	 * Teleports the specified player to a random location.
	 * This will return if the spawns size 0 because with value 0 are not possible.
	 * @param p Player who is in game
	 * @param spawn GameSpawnGetter
	 */
	public static void teleportPlayerToGameSpawn(Player p, GameSpawnGetter spawn) {
		if (spawn.getSpawnLocations().size() > 0) {
			Random r = new Random();
			int x = r.nextInt(spawn.getSpawnLocations().size());
			Location location = spawn.getSpawnLocations().get(x);
			p.teleport(location);
		}
	}

	public static boolean getLookingAt(Player player, LivingEntity livingEntity) {
		Location eye = player.getEyeLocation();
		Vector toEntity = livingEntity.getLocation().toVector().subtract(eye.toVector());
		double dot = toEntity.normalize().dot(eye.getDirection());

		return dot >= 0.99D;
	}

	private static List<String> reservedNames = buildReservedNameList();

	private static List<String> buildReservedNameList() {
		List<String> reservedNames = new java.util.ArrayList<>();
		for (Entry<String, String> cmds : RmCommand.arg.entrySet()) {
			reservedNames.add(cmds.getKey());
		}

		return reservedNames;
	}

	/**
	 * Stops the specified game if running.
	 * This calculates the players who has the highest points and announcing
	 * to a title message. If there are no winner player valid, players
	 * will be removed from the game with some rewards.
	 * This will saves the player statistic to the database and finally stopping the game.
	 * @param game name
	 */
	public static void stopGame(final String game) {
		Validate.notNull(game, "Game name can't be null!");
		Validate.notEmpty(game, "Game name can't be empty!");

		if (!Game.isGameRunning(game)) {
			return;
		}

		boolean winnervalid = false;
		final List<String> players = Game.getPlayersFromList();

		GameStopEvent gameStopEvent = new GameStopEvent(game, players);
		Utils.callEvent(gameStopEvent);

		String winnerUUID = RageScores.calculateWinner(game, players);
		if (winnerUUID != null) {
			if (UUID.fromString(winnerUUID) != null) {
				if (Bukkit.getPlayer(UUID.fromString(winnerUUID)) != null) {
					winnervalid = true;
					Player winner = Bukkit.getPlayer(UUID.fromString(winnerUUID));

					for (String playersUUID : players) {
						Player player = Bukkit.getPlayer(UUID.fromString(playersUUID));
						hu.montlikadani.ragemode.config.ConfigValues cv = RageMode.getInstance().getConfiguration()
								.getCV();
						String wonTitle = cv.getWonTitle();
						String wonSubtitle = cv.getWonSubTitle();

						String youWonTitle = cv.getYouWonTitle();
						String youWonSubtitle = cv.getYouWonSubTitle();

						wonTitle = wonTitle.replace("%winner%", winner.getName());
						wonTitle = replaceVariables(wonTitle, winnerUUID);

						youWonTitle = replaceVariables(youWonTitle, winnerUUID);

						wonSubtitle = wonSubtitle.replace("%winner%", winner.getName());
						wonSubtitle = replaceVariables(wonSubtitle, winnerUUID);

						youWonSubtitle = replaceVariables(youWonSubtitle, winnerUUID);

						String[] split = null;
						if (player != winner) {
							split = cv.getWonTitleTime().split(", ");
							if (split.length == 3) {
								Titles.sendTitle(player, Integer.parseInt(split[0]), Integer.parseInt(split[1]),
										Integer.parseInt(split[2]), wonTitle, wonSubtitle);
							}

							if (cv.isSwitchGMForPlayers())
								player.setGameMode(GameMode.SPECTATOR);
						} else {
							split = cv.getYouWonTitleTime().split(", ");
							if (split.length == 3) {
								Titles.sendTitle(winner, Integer.parseInt(split[0]), Integer.parseInt(split[1]),
										Integer.parseInt(split[2]), youWonTitle, youWonSubtitle);
							}
						}

						Game.removePlayerSynced(player);
					}
				}
			}
		}
		if (!winnervalid) {
			for (String playerUUID : players) {
				Player player = Bukkit.getPlayer(UUID.fromString(playerUUID));
				broadcastToGame(game, RageMode.getLang().get("game.no-won"));
				// Why?
				Bukkit.getScheduler().scheduleSyncDelayedTask(RageMode.getInstance(),
						() -> player.setGameMode(GameMode.SPECTATOR));

				Game.removePlayerSynced(player);
			}
		}

		if (!EventListener.waitingGames.containsKey(game))
			EventListener.waitingGames.put(game, true);
		else {
			EventListener.waitingGames.remove(game);
			EventListener.waitingGames.put(game, true);
		}

		setStatus(game, GameStatus.GAMEFREEZE);

		final Player winner = winnerUUID != null ? Bukkit.getPlayer(UUID.fromString(winnerUUID)) : null;
		RageMode.getInstance().getServer().getScheduler().scheduleSyncDelayedTask(RageMode.getInstance(),
				new Runnable() {
					@Override
					public void run() {
						setStatus(game, null);

						if (EventListener.waitingGames.containsKey(game))
							EventListener.waitingGames.remove(game);

						finishStopping(game, winner);
					}
				}, RageMode.getInstance().getConfiguration().getCV().getGameFreezeTime() * 20);
	}

	private static void finishStopping(String game, Player winner) {
		if (Game.isGameRunning(game)) {
			List<String> players = Game.getPlayersFromList();

			String stats = RageMode.getInstance().getConfiguration().getCV().getStatistics();
			for (String playersUUID : players) {
				if (playersUUID != null && RageScores.getPlayerPoints(playersUUID) != null) {
					final PlayerPoints pP = RageScores.getPlayerPoints(playersUUID);

					Thread th = null;
					switch (stats) {
					case "yaml":
						th = new Thread(YAMLStats.createPlayersStats(pP));
						break;
					case "mysql":
						th = new Thread(new MySQLThread(pP));
						break;
					case "sql":
					case "sqlite":
						th = new Thread(new SQLThread(pP));
						break;
					default:
						break;
					}

					if (th != null) {
						th.start();
					}

					Bukkit.getServer().getScheduler().runTaskAsynchronously(RageMode.getInstance(), () -> {
						RuntimePPManager.updatePlayerEntry(pP);

						Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(RageMode.getInstance(), () ->
							HoloHolder.updateHolosForPlayer(Bukkit.getPlayer(UUID.fromString(playersUUID))));
					});
				}
			}

			if (RageMode.getInstance().getConfiguration().getCV().isRewardEnabled()) {
				Reward reward = new Reward(game);

				for (String playerUUID : players) {
					Utils.clearPlayerInventory(Bukkit.getPlayer(UUID.fromString(playerUUID)));

					if (winner != null) {
						if (Bukkit.getPlayer(UUID.fromString(playerUUID)) == winner) {
							Utils.clearPlayerInventory(winner);

							reward.rewardForWinner(winner);
						}
					}

					reward.rewardForPlayers(winner, Bukkit.getPlayer(UUID.fromString(playerUUID)));
				}
			}

			broadcastToGame(game, RageMode.getLang().get("game.stopped", "%game%", game));
			Game.setGameNotRunning(game);
			runCommandsForAll(game, "stop");
			SignCreator.updateAllSigns(game);

			for (String playersUUID : players) {
				if (playersUUID != null) {
					sendActionBarMessages(Bukkit.getPlayer(UUID.fromString(playersUUID)), game, "stop");
					RageScores.removePointsForPlayer(playersUUID);
					Game.removePlayer(Bukkit.getPlayer(UUID.fromString(playersUUID)));
				}
			}

			for (Iterator<Entry<UUID, String>> it = Game.getSpectatorPlayers().entrySet().iterator(); it
					.hasNext();) {
				Player pl = Bukkit.getPlayer(it.next().getKey());
				Game.removeSpectatorPlayer(pl);
			}

			if (RageMode.getInstance().getConfiguration().getCV().isRestartServerEnabled()) {
				try {
					Class.forName("org.spigotmc.SpigotConfig");

					Bukkit.spigot().restart();
				} catch (ClassNotFoundException e) {
					Bukkit.getServer().dispatchCommand(Bukkit.getConsoleSender(), "restart");
				}
			} else if (RageMode.getInstance().getConfiguration().getCV().isStopServerEnabled())
				Bukkit.shutdown();
		}
	}

	/**
	 * Stops all currently running games. If there are no running games
	 * will returns.
	 */
	public static void stopAllGames() {
		Debug.logConsole("Searching games to stop...");

		String[] games = GetGames.getGameNames();
		if (games == null)
			return;

		int i = 0;
		int imax = games.length;

		while (i < imax) {
			if (games[i] != null && Game.isGameRunning(games[i])) {

				Debug.logConsole("Stopping " + games[i] + " ...");

				Game.getPlayersFromList()
						.forEach(uuids -> Game.removePlayer(Bukkit.getPlayer(UUID.fromString(uuids))));

				for (Iterator<Entry<UUID, String>> it = Game.getSpectatorPlayers().entrySet().iterator(); it
						.hasNext();) {
					Player pl = Bukkit.getPlayer(it.next().getKey());
					Game.removeSpectatorPlayer(pl);
				}

				Game.setGameNotRunning(games[i]);
				setStatus(games[i], null);

				Debug.logConsole(games[i] + " has been stopped.");
			}
			i++;
		}
	}

	private static String replaceVariables(String s, String uuid) {
		PlayerPoints score = RageScores.getPlayerPoints(uuid);

		if (s.contains("%points%"))
			s = s.replace("%points%", Integer.toString(score.getPoints()));
		if (s.contains("%kills%"))
			s = s.replace("%kills%", Integer.toString(score.getKills()));
		if (s.contains("%deaths%"))
			s = s.replace("%deaths%", Integer.toString(score.getDeaths()));
		return s;
	}

	/**
	 * Gets the specified game current set GameStatus by player.
	 * @param p Player
	 * @return {@link GameStatus}
	 */
	public static GameStatus getStatus(Player p) {
		return getStatus(Game.getPlayersGame(p));
	}

	/**
	 * Gets the specified game current set GameStatus.
	 * @param game Game name
	 * @return {@link GameStatus}
	 */
	public static GameStatus getStatus(String game) {
		Validate.notNull(game, "Game name can't be null!");
		Validate.notEmpty(game, "Game name can't be null!");

		return status.get(game);
	}

	/**
	 * Sets the game status to new status. If the status param is null
	 * will set to stopped status.
	 * @param game the game name to set
	 * @param status the new status to be set for the game
	 */
	public static void setStatus(String game, GameStatus status) {
		setStatus(game, status, true);
	}

	/**
	 * Sets the game status to new status.
	 * @param game the game name to set
	 * @param status the new status to be set for the game
	 * @param forceRemove to force remove the existing game status from list
	 */
	public static void setStatus(String game, GameStatus status, boolean forceRemove) {
		Validate.notNull(game, "Game name can't be null!");
		Validate.notEmpty(game, "Game name can't be null!");

		if (forceRemove && GameUtils.status.containsKey(game)) {
			GameUtils.status.remove(game);
		}

		if (status == null) {
			status = GameStatus.STOPPED;
		}

		GameUtils.status.put(game, status);
	}
}
