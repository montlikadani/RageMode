package hu.montlikadani.ragemode.gameLogic.Reward;

import java.util.List;
import java.util.UUID;
import java.util.logging.Level;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.EnchantmentStorageMeta;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.LeatherArmorMeta;

import hu.montlikadani.ragemode.NMS;
import hu.montlikadani.ragemode.RageMode;
import hu.montlikadani.ragemode.Utils;
import hu.montlikadani.ragemode.gameLogic.PlayerList;

public class Reward {

	private String game;
	private YamlConfiguration conf;
	private boolean enable = false;
	private String type;

	public Reward(String type, String game) {
		this.game = game;
		this.type = type;

		conf = RageMode.getInstance().getConfiguration().getRewardsCfg();
		enable = RageMode.getInstance().getConfiguration().getCfg().getBoolean("rewards.enable");
	}

	public void rewardForWinner(Player winner) {
		if (!enable)
			return;

		List<String> cmds = conf.getStringList("rewards.end-game.winner.commands");
		List<String> msgs = conf.getStringList("rewards.end-game.winner.messages");
		double cash = conf.getDouble("rewards.end-game.winner.cash");
		ConfigurationSection item = conf.getConfigurationSection("rewards.end-game.winner.items");

		if (cmds != null && !cmds.isEmpty()) {
			for (String path : cmds) {
				String[] arg = path.split(": ");
				String cmd = arg[1];
				cmd = replacePlaceholders(cmd, winner, true);

				if (arg[0].equals("console"))
					Bukkit.dispatchCommand(Bukkit.getServer().getConsoleSender(), cmd);
				else if (arg[0].equals("player"))
					winner.performCommand(cmd);
			}
		}

		if (msgs != null && !msgs.isEmpty()) {
			for (String path : msgs) {
				path = replacePlaceholders(path, winner, true);

				winner.sendMessage(path);
			}
		}

		if (cash > 0D && RageMode.getInstance().isVaultEnabled())
			RageMode.getInstance().getEconomy().depositPlayer(winner, cash);

		if (item != null && conf.isConfigurationSection("rewards.end-game.winner.items"))
			getItems("winner", winner);
	}

	public void rewardForPlayers(Player winner) {
		if (!enable)
			return;

		for (String playerUUID : PlayerList.getPlayersInGame(game)) {
			Player player = Bukkit.getPlayer(UUID.fromString(playerUUID));
			if (winner != null && player == winner)
				return;

			switch (type.toLowerCase()) {
			case "end-game":
				List<String> cmds = conf.getStringList("rewards.end-game.players.commands");
				List<String> msgs = conf.getStringList("rewards.end-game.players.messages");
				double cash = conf.getDouble("rewards.end-game.players.cash");
				ConfigurationSection item = conf.getConfigurationSection("rewards.end-game.players.items");

				if (cmds != null && !cmds.isEmpty()) {
					for (String path : cmds) {
						String[] arg = path.split(": ");
						String cmd = arg[1];
						cmd = replacePlaceholders(cmd, player, false);

						if (arg[0].equals("console"))
							Bukkit.dispatchCommand(Bukkit.getServer().getConsoleSender(), cmd);
						else if (arg[0].equals("player"))
							player.performCommand(cmd);
					}
				}

				if (msgs != null && !msgs.isEmpty()) {
					for (String path : msgs) {
						path = replacePlaceholders(path, player, false);

						player.sendMessage(path);
					}
				}

				if (cash > 0D && RageMode.getInstance().isVaultEnabled())
					RageMode.getInstance().getEconomy().depositPlayer(player, cash);

				if (item != null && conf.isConfigurationSection("rewards.end-game.players.items"))
					getItems("players", player);
				break;
			/*case "suicide":
				List<PotionEffect> effects = new ArrayList<>();
				List<String> list = conf.getStringList("rewards.suicide.potion-effects");
				if (list != null && !list.isEmpty()) {
					for (String s : list) {
						String[] effect = s.split("\\:");
						try {
							effects.add(new PotionEffect(PotionEffectType.getByName(effect[0].toUpperCase()),
									Integer.parseInt(effect[2]) * 20, (Integer.parseInt(effect[1])) - 1));
						} catch (IllegalArgumentException e) {
							RageMode.logConsole(Level.WARNING, "Unknown potion effect type: " + e.getMessage());
						}
					}
					player.addPotionEffects(effects);
				}*/
			default:
				break;
			}
		}
	}

	private String replacePlaceholders(String path, Player p, boolean winner) {
		double cash = winner ? conf.getDouble("rewards.end-game.winner.cash") : conf.getDouble("rewards.end-game.players.cash");

		path = path.replace("%game%", game);
		path = path.replace("%player%", p.getName());
		path = path.replace("%online-ingame-players%", Integer.toString(PlayerList.getPlayersInGame(game).length));
		path = path.replace("%reward%", cash > 0D ? Double.toString(cash) : "");
		path = Utils.setPlaceholders(path, p);
		return RageMode.getLang().colors(path);
	}

	@SuppressWarnings("deprecation")
	private void getItems(String path, Player p) {
		for (String num : conf.getConfigurationSection("rewards.end-game." + path + ".items").getKeys(false)) {
			String type = conf.getString("rewards.end-game." + path + ".items." + num + ".type");
			if (type != null) {
				try {
					Material mat = Material.valueOf(type);
					if (mat == null) {
						RageMode.logConsole(Level.WARNING, "[RageMode] Unknown item name: " + type);
						RageMode.logConsole("[RageMode] Find and double check item names using this page:");
						RageMode.logConsole("[RageMode] https://hub.spigotmc.org/javadocs/bukkit/org/bukkit/Material.html");
						return;
					}

					if (mat.equals(Material.AIR)) {
						RageMode.logConsole("[RageMode] AIR is not supported.");
						return;
					}

					ItemStack itemStack = new ItemStack(mat);
					if (conf.contains("rewards.end-game." + path + ".items." + num + ".amount"))
						itemStack.setAmount(conf.getInt("rewards.end-game." + path + ".items." + num + ".amount"));
					else
						itemStack.setAmount(1);

					if (conf.contains("rewards.end-game." + path + ".items." + num + ".durability"))
						NMS.setDurability(itemStack, (short) conf.getDouble("rewards.end-game." + path + ".items." + num + ".durability"));

					if (conf.getBoolean("rewards.end-game." + path + ".items." + num + ".meta")) {
						ItemMeta itemMeta = itemStack.getItemMeta();
						String str2 = conf.getString("rewards.end-game." + path + ".items." + num + ".name");
						if (str2 != null && !str2.equals(""))
							itemMeta.setDisplayName(str2.replaceAll("&", "\u00a7"));

						List<String> loreList = conf.getStringList("rewards.end-game." + path + ".items." + num + ".lore");
						if (loreList != null && !loreList.isEmpty())
							itemMeta.setLore(Utils.colorList(loreList));


						if (mat.equals(Material.LEATHER_BOOTS) || mat.equals(Material.LEATHER_LEGGINGS) || mat.equals(Material.LEATHER_CHESTPLATE) ||
								mat.equals(Material.LEATHER_HELMET)) {
							String str3 = conf.getString("rewards.end-game." + path + ".items." + num + ".color");
							if (str3 != null && !str3.equals("")) {
								if (itemMeta instanceof LeatherArmorMeta)
									((LeatherArmorMeta) itemMeta).setColor(Utils.getColorFromString(str3));
							}
						}

						itemStack.setItemMeta(itemMeta);

						List<String> enchantList = conf.getStringList("rewards.end-game." + path + ".items." + num + ".enchants");
						if (enchantList != null && !enchantList.isEmpty()) {
							for (String str4 : enchantList) {
								String[] split = str4.split(":");
								if (itemStack.getItemMeta() instanceof EnchantmentStorageMeta) {
									EnchantmentStorageMeta enchMeta = (EnchantmentStorageMeta) itemStack.getItemMeta();
									enchMeta.addStoredEnchant(Enchantment.getByName(split[0]), Integer.parseInt(split[1]), true);
									itemStack.setItemMeta(enchMeta);
								} else
									itemStack.addUnsafeEnchantment(Enchantment.getByName(split[0]), Integer.parseInt(split[1]));
							}
						}
					}
					try {
						if (conf.contains("rewards.end-game." + path + ".items." + num + ".slot"))
							p.getInventory().setItem(conf.getInt("rewards.end-game." + path + ".items." + num + ".slot"), itemStack);
						else
							p.getInventory().addItem(itemStack);
					} catch (IllegalArgumentException i) {
						RageMode.logConsole(Level.WARNING, "[RageMode] Slot is not between 0 and 8 inclusive.");
					}
				} catch (IllegalArgumentException e) {
					e.printStackTrace();
					RageMode.logConsole(Level.WARNING, "[RageMode] Problem occured with your item: " + e.getMessage());
				}
			}
		}
	}

	public boolean isEnabled() {
		return enable;
	}

	public String getType() {
		return type;
	}
}
