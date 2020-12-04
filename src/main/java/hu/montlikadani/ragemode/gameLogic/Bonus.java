package hu.montlikadani.ragemode.gameLogic;

import java.util.concurrent.ThreadLocalRandom;

import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import hu.montlikadani.ragemode.RageMode;
import hu.montlikadani.ragemode.items.ItemHandler;
import hu.montlikadani.ragemode.items.Items;
import hu.montlikadani.ragemode.libs.Sounds;

public class Bonus {

	public static void addKillBonus(Player player) {
		if (player == null) {
			return;
		}

		for (String b : RageMode.getInstance().getConfiguration().getRewardsCfg()
				.getStringList("rewards.in-game.bonuses.kill-bonuses.list")) {
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
				if (gameItem == null) {
					continue;
				}

				ItemHandler item;
				switch (gameItem) {
				case "grenade":
					item = Items.getGameItem(1);
					break;
				case "combataxe":
					item = Items.getGameItem(0);
					break;
				case "flash":
					item = Items.getGameItem(5);
					break;
				case "pressuremine":
				case "mine":
					item = Items.getGameItem(6);
					break;
				default:
					continue;
				}

				if (item == null) {
					continue;
				}

				int amount = 1;
				if ("grenade".equals(gameItem) || "flash".equals(gameItem) || "pressuremine".equals(gameItem)
						|| "mine".equals(gameItem)) {
					// clone the item to make sure we are not modifying anything
					item = (ItemHandler) item.clone();

					amount = split.length > 2 ? Integer.parseInt(split[1]) : 1;
					if (amount < 1) {
						amount = 1;
					}

					item.setAmount(amount).get();
				}

				org.bukkit.inventory.PlayerInventory inv = player.getInventory();
				if (!inv.contains(item.getItem()) && item.getSlot() != -1) {
					inv.setItem(item.getSlot(), item.get());
				} else {
					inv.addItem(item.get());
				}
			}
		}
	}

	public static int getPointBonus() {
		for (String b : RageMode.getInstance().getConfiguration().getRewardsCfg()
				.getStringList("rewards.in-game.bonuses.kill-bonuses.list")) {
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
