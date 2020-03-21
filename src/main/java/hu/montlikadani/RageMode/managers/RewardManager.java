package hu.montlikadani.ragemode.managers;

import java.util.List;
import java.util.logging.Level;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.EnchantmentStorageMeta;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.LeatherArmorMeta;

import hu.montlikadani.ragemode.Debug;
import hu.montlikadani.ragemode.NMS;
import hu.montlikadani.ragemode.RageMode;
import hu.montlikadani.ragemode.Utils;

public class RewardManager {

	private String game;

	private FileConfiguration conf = RageMode.getInstance().getConfiguration().getRewardsCfg();

	public RewardManager(String game) {
		this.game = game;
	}

	public String getGame() {
		return game;
	}

	public void rewardForWinner(Player winner) {
		List<String> cmds = conf.getStringList("rewards.end-game.winner.commands");
		for (String path : cmds) {
			String[] arg = path.split(": ");

			String cmd = path;
			if (arg.length < 2) {
				arg[0] = "console";
			} else {
				cmd = arg[1];
			}

			cmd = replacePlaceholders(cmd, winner, true);

			if (arg[0].equalsIgnoreCase("console"))
				Bukkit.dispatchCommand(Bukkit.getServer().getConsoleSender(), cmd);
			else if (arg[0].equalsIgnoreCase("player"))
				winner.performCommand(cmd);
		}

		List<String> msgs = conf.getStringList("rewards.end-game.winner.messages");
		for (String path : msgs) {
			path = replacePlaceholders(path, winner, true);
			winner.sendMessage(path);
		}

		double cash = conf.getDouble("rewards.end-game.winner.cash");
		if (cash > 0D && RageMode.getInstance().isVaultEnabled())
			RageMode.getInstance().getEconomy().depositPlayer(winner, cash);

		addItems("winner", winner);
	}

	public void rewardForPlayers(Player winner, Player pls) {
		if (winner != null && winner.equals(pls))
			return;

		List<String> cmds = conf.getStringList("rewards.end-game.players.commands");
		for (String path : cmds) {
			String[] arg = path.split(": ");
			String cmd = path;
			if (arg.length < 2) {
				arg[0] = "console";
			} else {
				cmd = arg[1];
			}

			cmd = replacePlaceholders(cmd, pls, false);

			if (arg[0].equalsIgnoreCase("console"))
				Bukkit.dispatchCommand(Bukkit.getServer().getConsoleSender(), cmd);
			else if (arg[0].equalsIgnoreCase("player"))
				pls.performCommand(cmd);
		}

		List<String> msgs = conf.getStringList("rewards.end-game.players.messages");
		if (!msgs.isEmpty()) {
			msgs.forEach(path -> pls.sendMessage(replacePlaceholders(path, pls, false)));
		}

		double cash = conf.getDouble("rewards.end-game.players.cash", 0D);
		if (cash > 0D && RageMode.getInstance().isVaultEnabled())
			RageMode.getInstance().getEconomy().depositPlayer(pls, cash);

		addItems("players", pls);
	}

	private String replacePlaceholders(String path, Player p, boolean winner) {
		double cash = conf.getDouble("rewards.end-game." + (winner ? "winner" : "players") + ".cash", 0D);

		path = path.replace("%game%", game);
		path = path.replace("%player%", p.getName());
		path = path.replace("%reward%", cash > 0D ? Double.toString(cash) : "");
		path = Utils.setPlaceholders(path, p);
		return Utils.colors(path);
	}

	private void addItems(String path, Player p) {
		if (!conf.contains("rewards.end-game." + path + ".items")) {
			return;
		}

		for (String num : conf.getConfigurationSection("rewards.end-game." + path + ".items").getKeys(false)) {
			String rewPath = "rewards.end-game." + path + ".items." + num + ".";
			String type = conf.getString(rewPath + "type", "");
			if (type.isEmpty()) {
				continue;
			}

			try {
				Material mat = Material.valueOf(type.toUpperCase());
				if (mat == null) {
					Debug.logConsole(Level.WARNING, "Unknown item name: " + type);
					Debug.logConsole("Find and double check item names using this page:");
					Debug.logConsole("https://hub.spigotmc.org/javadocs/bukkit/org/bukkit/Material.html");
					continue;
				}

				if (mat.equals(Material.AIR)) {
					Debug.logConsole("AIR is not supported.");
					continue;
				}

				ItemStack itemStack = new ItemStack(mat);
				itemStack.setAmount(conf.getInt(rewPath + "amount", 1));

				if (conf.contains(rewPath + "durability"))
					NMS.setDurability(itemStack, (short) conf.getDouble(rewPath + "durability"));

				if (conf.getBoolean(rewPath + "meta")) {
					ItemMeta itemMeta = itemStack.getItemMeta();
					String name = conf.getString(rewPath + "name", "");
					if (!name.isEmpty())
						itemMeta.setDisplayName(name.replaceAll("&", "\u00a7"));

					List<String> loreList = conf.getStringList(rewPath + "lore");
					if (!loreList.isEmpty())
						itemMeta.setLore(Utils.colorList(loreList));

					if (type.startsWith("LEATHER_")) {
						String color = conf.getString(rewPath + "color", "");
						if (!color.isEmpty() && itemMeta instanceof LeatherArmorMeta) {
							((LeatherArmorMeta) itemMeta).setColor(Utils.getColorFromString(color));
						}
					}

					/**
					String bannerColor = conf.getString(rewPath + "banner.color", "");
					String bannerType = conf.getString(rewPath + "banner.type", "");
					if (!bannerColor.isEmpty() && !bannerType.isEmpty()) {
						if (Version.isCurrentEqualOrLower(Version.v1_12_R1)) {
							if (mat.equals(Material.valueOf("BANNER")) && itemMeta instanceof BannerMeta) {
								List<Pattern> patterns = new ArrayList<>();
								patterns.add(
										new Pattern(DyeColor.valueOf(bannerColor), PatternType.valueOf(bannerType)));

								((BannerMeta) itemMeta).setBaseColor(DyeColor.valueOf(bannerColor));
								((BannerMeta) itemMeta).setPatterns(patterns);
							}
						} else if (type.endsWith("_BANNER")) {
							if (itemMeta instanceof BannerMeta) {
								List<Pattern> patterns = new ArrayList<>();
								patterns.add(
										new Pattern(DyeColor.valueOf(bannerColor), PatternType.valueOf(bannerType)));

								((Banner) itemStack).setBaseColor(DyeColor.valueOf(bannerColor));
								((BannerMeta) itemMeta).setPatterns(patterns);
							}
						}
					}
					**/

					itemStack.setItemMeta(itemMeta);

					List<String> enchantList = conf.getStringList(rewPath + "enchants");
					for (String enchant : enchantList) {
						String[] split = enchant.split(":");
						try {
							if (itemStack.getItemMeta() instanceof EnchantmentStorageMeta) {
								EnchantmentStorageMeta enchMeta = (EnchantmentStorageMeta) itemStack.getItemMeta();
								enchMeta.addStoredEnchant(NMS.getEnchant(split[0]),
										(split.length > 2 ? Integer.parseInt(split[1]) : 1), true);
								itemStack.setItemMeta(enchMeta);
							} else
								itemStack.addUnsafeEnchantment(NMS.getEnchant(split[0]), Integer.parseInt(split[1]));
						} catch (IllegalArgumentException b) {
							Debug.logConsole(Level.WARNING, "Bad enchantment name: " + split[0]);
							continue;
						}
					}
				}

				try {
					if (conf.contains(rewPath + "slot"))
						p.getInventory().setItem(conf.getInt(rewPath + "slot"), itemStack);
					else
						p.getInventory().addItem(itemStack);
				} catch (IllegalArgumentException i) {
					Debug.logConsole(Level.WARNING, "Slot is not between 0 and 8 inclusive.");
				}
			} catch (Exception e) {
				Debug.logConsole(Level.WARNING, "Problem occured with your item: " + e.getMessage());
			}
		}
	}
}
