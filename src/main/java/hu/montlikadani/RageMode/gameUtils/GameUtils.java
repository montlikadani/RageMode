package hu.montlikadani.ragemode.gameUtils;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.potion.PotionEffect;

import hu.montlikadani.ragemode.RageMode;
import hu.montlikadani.ragemode.Utils;
import hu.montlikadani.ragemode.config.Configuration;
import hu.montlikadani.ragemode.gameLogic.GameSpawnGetter;
import hu.montlikadani.ragemode.gameLogic.GameStatus;
import hu.montlikadani.ragemode.gameLogic.PlayerList;
import hu.montlikadani.ragemode.holder.HoloHolder;
import hu.montlikadani.ragemode.items.ForceStarter;
import hu.montlikadani.ragemode.items.LeaveGame;
import hu.montlikadani.ragemode.signs.SignCreator;

public class GameUtils {

	private static GameStatus status = GameStatus.STOPPED;

	/**
	 * Broadcast for in-game players to the specified game
	 * 
	 * @param game Game
	 * @param message Message
	 */
	public static void broadcastToGame(String game, String message) {
		String[] playersInGame = PlayerList.getPlayersInGame(game);
		int i = 0;
		int imax = playersInGame.length;

		while (i < imax) {
			if (playersInGame[i] != null && Bukkit.getPlayer(UUID.fromString(playersInGame[i])) != null)
				Bukkit.getPlayer(UUID.fromString(playersInGame[i])).sendMessage(message);
			i++;
		}
	}

	/**
	 * Checks whatever the specified game is exists or no.
	 * 
	 * @param game Game
	 * @return true if game exists
	 */
	public static boolean isGameWithNameExists(String game) {
		return GetGames.isGameExistent(game);
	}

	/**
	 * Gets the game by name.
	 * 
	 * @param name Game name
	 * @return game name if exist
	 */
	public static String getGameByName(String name) {
		String gName = null;
		if (isGameWithNameExists(name))
			gName = name;

		return gName;
	}

	/**
	 * Get the player who in game.
	 * 
	 * @param game Game
	 * @return Player
	 */
	public static Player getPlayerInGame(String game) {
		String[] players = PlayerList.getPlayersInGame(game);
		Player player = null;
		if (players != null) {
			int i = 0;
			int imax = players.length;

			while (i < imax) {
				if (players[i] != null)
					player = Bukkit.getPlayer(UUID.fromString(players[i]));

				i++;
			}
		}
		return player != null ? player : null;
	}

	/**
	 * Get the game spawn by name.
	 * 
	 * @param name Game name
	 * @return GameSpawnGetter
	 */
	public static GameSpawnGetter getGameSpawnByName(String name) {
		for (GameSpawnGetter gsg : RageMode.getInstance().getSpawns()) {
			if (gsg.getGameName().equalsIgnoreCase(name))
				return gsg;
		}
		return null;
	}

	/**
	 * Saves the player data to a yaml file
	 * <p>
	 * This prevents losing the player data when the server has stopped randomly.
	 * 
	 * @param p Player
	 */
	public static void savePlayerData(Player p) {
		PlayerInventory inv = p.getInventory();
		Configuration conf = RageMode.getInstance().getConfiguration();

		PlayerList.oldLocations.addToBoth(p, p.getLocation());
		PlayerList.oldInventories.addToBoth(p, inv.getContents());
		PlayerList.oldArmor.addToBoth(p, inv.getArmorContents());
		PlayerList.oldHealth.addToBoth(p, p.getHealth());
		PlayerList.oldHunger.addToBoth(p, p.getFoodLevel());
		if (!p.getActivePotionEffects().isEmpty())
			PlayerList.oldEffects.addToBoth(p, p.getActivePotionEffects());

		PlayerList.oldGameMode.addToBoth(p, p.getGameMode());

		if (!p.getDisplayName().equals(p.getDisplayName()))
			PlayerList.oldDisplayName.addToBoth(p, p.getDisplayName());

		if (!p.getPlayerListName().equals(p.getPlayerListName()))
			PlayerList.oldListName.addToBoth(p, p.getPlayerListName());

		if (p.getFireTicks() > 0)
			PlayerList.oldFire.addToBoth(p, p.getFireTicks());

		if (p.getExp() > 0d)
			PlayerList.oldExp.addToBoth(p, p.getExp());

		if (p.getLevel() > 0)
			PlayerList.oldExpLevel.addToBoth(p, p.getLevel());

		if (p.isInsideVehicle())
			PlayerList.oldVehicle.addToBoth(p, p.getVehicle());

		if (conf.getDatasFile() != null && conf.getDatasFile().exists()) {
			org.bukkit.configuration.file.YamlConfiguration data = conf.getDatasCfg();
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

			try {
				data.save(conf.getDatasFile());
			} catch (IOException o) {
				o.printStackTrace();
				RageMode.getInstance().throwMsg();
			}
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

		if (status == GameStatus.RUNNING) {
			if (conf.getCfg().getBoolean("spectator.enable")) {
				if (!PlayerList.isPlayerPlaying(p.getUniqueId().toString())) {
					if (PlayerList.addSpectatorPlayer(p)) {
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
			if (PlayerList.isPlayerPlaying(p.getUniqueId().toString())) {
				p.sendMessage(RageMode.getLang().get("game.player-already-in-game", "%usage%", "/rm leave"));
				return;
			}

			MapChecker mapChecker = new MapChecker(game);
			if (mapChecker.isValid()) {
				if (conf.getCfg().getBoolean("require-empty-inventory-to-join")) {
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
				} else if (conf.getCfg().getBoolean("save-player-datas-to-file"))
					savePlayerData(p);
				else {
					clearPlayerTools(p);

					// We still need some data saving
					PlayerList.oldLocations.addToBoth(p, p.getLocation());
					PlayerList.oldGameMode.addToBoth(p, p.getGameMode());
					p.setGameMode(GameMode.SURVIVAL);
				}

				if (PlayerList.addPlayer(p, game)) {
					p.teleport(GetGameLobby.getLobbyLocation(game));

					if (conf.getCfg().contains("items.leavegameitem"))
						inv.setItem(conf.getCfg().getInt("items.leavegameitem.slot"), LeaveGame.getItem());

					if (conf.getCfg().contains("items.force-start") && p.hasPermission("ragemode.admin.item.forcestart"))
						inv.setItem(conf.getCfg().getInt("items.force-start.slot"), ForceStarter.getItem());

					broadcastToGame(game, RageMode.getLang().get("game.player-joined", "%player%", p.getName()));

					String title = conf.getCfg().getString("titles.join-game.title");
					String subtitle = conf.getCfg().getString("titles.join-game.subtitle");
					if (title != null || subtitle != null) {
						title = title.replace("%game%", game);
						subtitle = subtitle.replace("%game%", game);
						Titles.sendTitle(p, conf.getCfg().getInt("titles.join-game.fade-in"), conf.getCfg().getInt("titles.join-game.stay"),
								conf.getCfg().getInt("titles.join-game.fade-out"), title, subtitle);
					}
					SignCreator.updateAllSigns(game);
				} else
					RageMode.logConsole(RageMode.getLang().get("game.player-could-not-join", "%player%", p.getName(), "%game%", game));
			} else
				p.sendMessage(mapChecker.getMessage());
		}
	}

	/**
	 * Kicks the specified player from the game and server.
	 * 
	 * @param p Player
	 */
	public static void kickPlayer(Player p) {
		// Just removes the spec player
		PlayerList.removeSpectatorPlayer(p);

		if (status == GameStatus.RUNNING && PlayerList.isPlayerPlaying(p.getUniqueId().toString())) {
			if (PlayerList.removePlayer(p)) {
				RageMode.logConsole("[RageMode] Player " + p.getName() + " left the server while playing.");

				List<String> list = RageMode.getInstance().getConfiguration().getCfg()
						.getStringList("game.global.run-commands-for-player-left-while-playing");
				if (list != null && !list.isEmpty()) {
					for (String cmds : list) {
						cmds = cmds.replace("%player%", p.getName());
						cmds = cmds.replace("%player-ip%", p.getAddress().getAddress().getHostAddress());
						Bukkit.dispatchCommand(Bukkit.getServer().getConsoleSender(), RageMode.getLang().colors(cmds));
					}
				}
			}
		}
		HoloHolder.deleteHoloObjectsOfPlayer(p);
	}

	private static void clearPlayerTools(Player p) {
		Utils.clearPlayerInventory(p);
		p.setGameMode(GameMode.SURVIVAL);
		p.setHealth(20);
		p.setFoodLevel(20);
		p.setFireTicks(0);
		p.setExp(0);
		p.setLevel(0);
		if (p.isInsideVehicle())
			p.leaveVehicle();
		for (PotionEffect e : p.getActivePotionEffects()) {
			p.removePotionEffect(e.getType());
		}
		p.setDisplayName(p.getName());
		p.setPlayerListName(p.getName());
	}

	/**
	 * Get the GameStatus
	 * 
	 * @return {@link GameStatus}
	 */
	public static GameStatus getStatus() {
		return status;
	}

	/**
	 * Sets the game status to new status
	 * 
	 * @param status
	 */
	public static void setStatus(GameStatus status) {
		GameUtils.status = status;
	}
}
