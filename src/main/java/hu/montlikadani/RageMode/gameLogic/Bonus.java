package hu.montlikadani.ragemode.gameLogic;

import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import hu.montlikadani.ragemode.config.ConfigValues;
import hu.montlikadani.ragemode.items.ItemHandler;
import hu.montlikadani.ragemode.items.Items;
import hu.montlikadani.ragemode.libs.Sounds;

public class Bonus {

	public static void addKillBonus(Player player) {
		if (player == null) {
			return;
		}

		List<String> list = ConfigValues.getKillBonuses();
		for (String b : list) {
			if (b.contains("effect:")) {
				b = b.replace("effect:", "");

				if ((b = chance(b)) == null) {
					continue;
				}

				String[] split = b.split(":");
				PotionEffectType effect = split.length > 1 ? PotionEffectType.getByName(split[0].toUpperCase()) : null;
				if (effect == null) {
					continue;
				}

				PotionEffect pe = new PotionEffect(effect, (split.length > 2 ? Integer.parseInt(split[1]) : 5) * 20,
						(split.length > 3 ? Integer.parseInt(split[2]) : 1));
				player.addPotionEffect(pe);
			} else if (b.contains("sound:")) {
				b = b.replace("sound:", "");

				if ((b = chance(b)) == null) {
					continue;
				}

				String[] split = b.split(":");
				Sounds.playSound(player, split[0].toUpperCase(), (split.length > 2 ? Float.parseFloat(split[1]) : 1f),
						(split.length > 3 ? Float.parseFloat(split[2]) : 1f));
			} else if (b.contains("givegameitem:")) {
				b = b.replace("givegameitem:", "");

				if ((b = chance(b)) == null) {
					continue;
				}

				String[] split = b.split(":");
				String gameItem = split.length > 1 ? split[0].toLowerCase() : null;

				ItemHandler item = null;
				if ("grenade".equals(gameItem)) {
					item = Items.getGrenade();
				} else if ("combataxe".equals(gameItem)) {
					item = Items.getCombatAxe();
				}

				if (item == null) {
					continue;
				}

				int amount = 1;
				if ("grenade".equals(gameItem)) {
					// clone the item to make sure we are not modifying anything
					item = (ItemHandler) item.clone();

					amount = split.length > 2 ? Integer.parseInt(split[1]) : 1;
					if (amount < 1) {
						amount = 1;
					}

					item.setAmount(amount).build();
				}

				org.bukkit.inventory.PlayerInventory inv = player.getInventory();
				if (!inv.contains(item.getItem()) && item.getSlot() != -1) {
					inv.setItem(item.getSlot(), item.build());
				} else {
					inv.addItem(item.build());
				}
			}
		}
	}

	public static int getPointBonus() {
		List<String> list = ConfigValues.getKillBonuses();
		for (String b : list) {
			if (!b.contains("points:")) {
				continue;
			}

			b = b.replace("points:", "");

			if ((b = chance(b)) == null) {
				continue;
			}

			String[] split = b.split(":");
			if (split.length > 1) {
				return Integer.parseInt(split[1]);
			}
		}

		return 0;
	}

	private static String chance(String b) {
		if (!b.startsWith("chance:")) {
			return b;
		}

		String[] rSplit = b.split("chance:");
		int r = Integer.parseInt(rSplit[1].split("_")[0]);
		if (ThreadLocalRandom.current().nextInt(0, 100) > r) {
			return null;
		}

		b = b.replace("chance:" + r + "_", "");
		return b;
	}
}
