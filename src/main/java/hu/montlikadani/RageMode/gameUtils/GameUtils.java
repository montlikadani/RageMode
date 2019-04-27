package hu.montlikadani.ragemode.gameUtils;

import java.io.IOException;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;

import hu.montlikadani.ragemode.RageMode;
import hu.montlikadani.ragemode.config.Configuration;
import hu.montlikadani.ragemode.gameLogic.PlayerList;

public class GameUtils {

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
	 * Saves the player data to a yaml file
	 * 
	 * @param p Player
	 */
	public static void savePlayerData(Player p) {
		org.bukkit.inventory.PlayerInventory inv = p.getInventory();
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

		inv.clear();
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
		inv.setHelmet(null);
		inv.setChestplate(null);
		inv.setLeggings(null);
		inv.setBoots(null);
		p.setDisplayName(p.getName());
		p.setPlayerListName(p.getName());
	}
}
