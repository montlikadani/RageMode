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

		if (ThreadLocalRandom.current().nextInt(0, 100) > ConfigValues.getKillBonusChance()) {
			return;
		}

		List<String> list = ConfigValues.getKillBonuses();
		for (String b : list) {
			if (b.startsWith("effect:")) {
				b = b.replace("effect:", "");

				String[] split = b.split(":");

				PotionEffectType effect = null;
				if (split.length > 1) {
					effect = PotionEffectType.getByName(split[0].toUpperCase());
				}

				if (effect == null) {
					continue;
				}

				PotionEffect pe = new PotionEffect(effect, (split.length > 2 ? Integer.parseInt(split[1]) : 5) * 20,
						(split.length > 3 ? Integer.parseInt(split[2]) : 1));
				player.addPotionEffect(pe);
			} else if (b.startsWith("sound:")) {
				b = b.replace("sound:", "");

				String[] split = b.split(":");

				Sounds.playSound(player, Sounds.valueOf(split[0].toUpperCase()),
						(split.length > 2 ? Float.parseFloat(split[1]) : 1f),
						(split.length > 3 ? Float.parseFloat(split[2]) : 1f));
			}
		}
	}

	public int getPointBonus(Player player) {
		if (player == null) {
			return 0;
		}

		if (ThreadLocalRandom.current().nextInt(0, 100) > ConfigValues.getKillBonusChance()) {
			return 0;
		}

		List<String> list = ConfigValues.getKillBonuses();
		for (String b : list) {
			if (!b.startsWith("points:")) {
				continue;
			}

			String[] split = b.split("points:");

			if (split.length > 1) {
				int amount = Integer.parseInt(split[1]);
				return amount;
			}
		}

		return 0;
	}
}
