package hu.montlikadani.ragemode.gameLogic;

import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import hu.montlikadani.ragemode.config.ConfigValues;
import hu.montlikadani.ragemode.libs.Sounds;

public class Bonus {

	public void addKillBonus(Player player) {
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
			}
		}
	}

	public int getPointBonus(Player player) {
		if (player == null) {
			return 0;
		}

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

	private String chance(String b) {
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
