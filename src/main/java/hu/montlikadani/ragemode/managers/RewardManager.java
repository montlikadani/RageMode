package hu.montlikadani.ragemode.managers;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.EnchantmentStorageMeta;
import org.bukkit.inventory.meta.LeatherArmorMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import hu.montlikadani.ragemode.RageMode;
import hu.montlikadani.ragemode.gameLogic.Game;
import hu.montlikadani.ragemode.items.ItemHandler;
import hu.montlikadani.ragemode.items.Items;
import hu.montlikadani.ragemode.utils.Misc;
import hu.montlikadani.ragemode.utils.SchedulerUtil;
import hu.montlikadani.ragemode.utils.Utils;

public class RewardManager {

	private final RageMode plugin;

	// We uses arrayList to allow duplications
	private final List<RewardCommand> endGameWinnerCommands = new ArrayList<>(),
			endGamePlayerCommands = new ArrayList<>();
	private final List<RewardItem> endGameWinnerRewardItems = new ArrayList<>(),
			endGamePlayerRewardItems = new ArrayList<>();
	private final List<InGameCommand> inGameCommands = new ArrayList<>();
	private final List<Bonuses> bonuses = new ArrayList<>();

	private boolean enabled;

	public RewardManager(RageMode plugin) {
		this.plugin = plugin;
		load();
	}

	public final boolean isEnabled() {
		return enabled;
	}

	private void load() {
		endGameWinnerCommands.clear();
		endGamePlayerCommands.clear();
		endGameWinnerRewardItems.clear();
		endGamePlayerRewardItems.clear();
		inGameCommands.clear();
		bonuses.clear();

		org.bukkit.configuration.MemorySection conf = plugin.getConfiguration().getRewardsCfg();

		enabled = conf.getBoolean("enabled");

		for (String one : conf.getStringList("rewards.end-game.winner.commands")) {
			endGameWinnerCommands.add(new RewardCommand(one));
		}

		for (String c : conf.getStringList("rewards.end-game.players.commands")) {
			endGamePlayerCommands.add(new RewardCommand(c));
		}

		for (String bonus : conf.getStringList("rewards.in-game.bonuses.kill-bonuses.list")) {
			bonuses.add(new Bonuses(bonus));
		}

		ConfigurationSection section = conf.getConfigurationSection("rewards.end-game.winner.items");
		if (section != null) {
			for (String n : section.getKeys(false)) {
				endGameWinnerRewardItems.add(new RewardItem(section, n));
			}
		}

		if ((section = conf.getConfigurationSection("rewards.end-game.players.items")) != null) {
			for (String n : section.getKeys(false)) {
				endGamePlayerRewardItems.add(new RewardItem(section, n));
			}
		}

		if ((section = conf.getConfigurationSection("rewards.in-game.run-commands")) != null) {
			for (String key : section.getKeys(false)) {
				for (String cmd : section.getStringList(key)) {
					inGameCommands.add(new InGameCommand(key, cmd));
				}
			}
		}
	}

	public void rewardForWinner(Player winner, Game game) {
		SchedulerUtil.submitSync(() -> {
			for (RewardCommand reward : endGameWinnerCommands) {
				if (reward.command.isEmpty()) {
					continue;
				}

				String cmd = replacePlaceholders(reward.command, winner, game);

				if (reward.type == SenderType.PLAYER) {
					winner.performCommand(cmd);
				} else if (reward.type == SenderType.CONSOLE) {
					plugin.getServer().dispatchCommand(plugin.getServer().getConsoleSender(), cmd);
				}
			}

			for (RewardItem rewardItem : endGameWinnerRewardItems) {
				if (rewardItem.slot >= 0) {
					winner.getInventory().setItem(rewardItem.slot, rewardItem.itemStack);
				} else {
					winner.getInventory().addItem(rewardItem.itemStack);
				}
			}

			return true;
		});
	}

	public void rewardForPlayers(Player player, Game game) {
		SchedulerUtil.submitSync(() -> {
			for (RewardCommand reward : endGamePlayerCommands) {
				if (reward.command.isEmpty()) {
					continue;
				}

				String cmd = replacePlaceholders(reward.command, player, game);

				if (reward.type == SenderType.PLAYER) {
					player.performCommand(cmd);
				} else if (reward.type == SenderType.CONSOLE) {
					plugin.getServer().dispatchCommand(plugin.getServer().getConsoleSender(), cmd);
				}
			}

			for (RewardItem rewardItem : endGamePlayerRewardItems) {
				if (rewardItem.slot >= 0) {
					player.getInventory().setItem(rewardItem.slot, rewardItem.itemStack);
				} else {
					player.getInventory().addItem(rewardItem.itemStack);
				}
			}

			return true;
		});
	}

	public void performGameCommands(Player player, Game game, InGameCommand.CommandType commandType) {
		SchedulerUtil.submitSync(() -> {
			for (InGameCommand command : inGameCommands) {
				if (command.command.isEmpty()
						|| (command.chance > -1 && ThreadLocalRandom.current().nextInt(0, 100) > command.chance)) {
					continue;
				}

				if (command.commandType == commandType) {
					String cmd = replacePlaceholders(command.command, player, game);

					if (command.type == SenderType.CONSOLE) {
						plugin.getServer().dispatchCommand(plugin.getServer().getConsoleSender(), cmd);
					} else if (command.type == SenderType.PLAYER) {
						player.performCommand(cmd);
					}
				}
			}

			return true;
		});
	}

	public void giveBonuses(Player player) {
		SchedulerUtil.submitSync(() -> {
			for (Bonuses bonus : bonuses) {
				if (bonus.chance > -1 && ThreadLocalRandom.current().nextInt(0, 100) > bonus.chance) {
					continue;
				}

				if (bonus.gameItem != null) {
					if (bonus.gameItem.getSlot() >= 0) {
						player.getInventory().setItem(bonus.gameItem.getSlot(), bonus.gameItem.get());
					} else {
						player.getInventory().addItem(bonus.gameItem.get());
					}
				}

				if (bonus.sound != null) {
					player.playSound(player.getLocation(), bonus.sound, bonus.soundVolume, bonus.soundPitch);
				}

				if (bonus.potionEffect != null) {
					player.addPotionEffect(bonus.potionEffect);
				}
			}

			return true;
		});
	}

	public int getPointBonus() {
		int points = 0;

		for (Bonuses bonus : bonuses) {
			if (bonus.chance > -1 && ThreadLocalRandom.current().nextInt(0, 100) > bonus.chance) {
				continue;
			}

			points += bonus.points;
		}

		return points;
	}

	private String replacePlaceholders(String str, Player player, Game game) {
		str = str.replace("%game%", game.getName());
		str = str.replace("%player%", player.getName());
		str = str.replace("%world%", player.getWorld().getName());
		str = Utils.setPlaceholders(str, player);
		return Utils.colors(str);
	}

	public static final class InGameCommand {

		private String command = "";
		private double chance = -1;

		private SenderType type = SenderType.CONSOLE;
		private CommandType commandType;

		public InGameCommand(String key, String one) {
			try {
				commandType = CommandType.valueOf(key.toUpperCase());
			} catch (IllegalArgumentException e) {
				commandType = CommandType.JOIN;
			}

			if (one.startsWith("chance:")) {
				one = one.replace("chance:", "");

				String[] chSplit = one.split("_", 2);
				if (chSplit.length > 0) {
					try {
						chance = Double.parseDouble(chSplit[0].replaceAll("[^0-9]+", ""));
					} catch (NumberFormatException e) {
					}

					one = chSplit[1];
				}
			}

			String[] split = one.split(":", 2);
			if (split.length == 0) {
				return;
			}

			if (split[0].equalsIgnoreCase("player")) {
				type = SenderType.PLAYER;
			}

			if (split.length > 1) {
				this.command = split[1];
			}
		}

		public String getCommand() {
			return command;
		}

		public double getChance() {
			return chance;
		}

		public SenderType getType() {
			return type;
		}

		public CommandType getCommandType() {
			return commandType;
		}

		public enum CommandType {
			JOIN, DEATH, LEAVE, START, STOP
		}
	}

	public final class RewardItem {

		private ItemStack itemStack;
		private int slot = -1;

		public ItemStack getItemStack() {
			return itemStack;
		}

		public int getSlot() {
			return slot;
		}

		public RewardItem(ConfigurationSection section, String num) {
			String type = section.getString(num + ".type", "");
			if (type.isEmpty()) {
				return;
			}

			Material mat = Material.matchMaterial(type.toUpperCase());
			if (mat == null || mat == Material.AIR) {
				return;
			}

			ItemStack itemStack = new ItemStack(mat, section.getInt(num + ".amount", 1));

			if (section.isDouble(num + ".durability"))
				Misc.setDurability(itemStack, (short) section.getDouble(num + ".durability"));

			org.bukkit.inventory.meta.ItemMeta itemMeta = itemStack.getItemMeta();
			if (itemMeta == null) {
				return;
			}

			String name = section.getString(num + ".name", "");
			if (!name.isEmpty())
				plugin.getComplement().setDisplayName(itemMeta, name);

			List<String> loreList = section.getStringList(num + ".lore");
			if (!loreList.isEmpty())
				plugin.getComplement().setLore(itemMeta, Utils.colorList(loreList));

			if (mat.toString().startsWith("LEATHER_")) {
				String str = section.getString(num + ".color", "");

				if (!str.isEmpty() && itemMeta instanceof LeatherArmorMeta) {
					String[] split = str.split(",", 3);

					if (split.length > 0) {
						int red = Utils.tryParseInt(split[0]).orElse(0);
						int green = split.length > 1 ? Utils.tryParseInt(split[1]).orElse(0) : 0;
						int blue = split.length > 2 ? Utils.tryParseInt(split[2]).orElse(0) : 0;

						try {
							((LeatherArmorMeta) itemMeta).setColor(org.bukkit.Color.fromRGB(red, green, blue));
						} catch (IllegalArgumentException e) {
						}
					}
				}
			}

			itemStack.setItemMeta(itemMeta);

			for (String one : section.getStringList(num + ".enchants")) {
				String[] split = one.split(":", 2);

				if (split.length == 0) {
					continue;
				}

				org.bukkit.enchantments.Enchantment enchant = Misc.getEnchant(split[0]);
				if (enchant == null) {
					continue;
				}

				int level = split.length > 1 ? Utils.tryParseInt(split[1]).orElse(1) : 1;
				if (level <= 0) {
					level = 1;
				}

				if (itemMeta instanceof EnchantmentStorageMeta) {
					EnchantmentStorageMeta enchMeta = (EnchantmentStorageMeta) itemMeta;
					enchMeta.addStoredEnchant(enchant, level, true);
					itemStack.setItemMeta(enchMeta);
				} else
					itemStack.addUnsafeEnchantment(enchant, level);
			}

			this.itemStack = itemStack;

			int slot = section.getInt(num + ".slot", -1);
			if (slot > -1 && slot <= 40) {
				this.slot = slot;
			}
		}
	}

	public static final class RewardCommand {

		private String command = "";
		private SenderType type = SenderType.CONSOLE;

		public RewardCommand(String command) {
			String[] split = command.split(": ", 2);

			if (split.length == 0) {
				split = new String[] { "console", command };
			}

			if (split.length < 2) {
				return;
			}

			String line = split[1];

			if (line.charAt(0) == '/') {
				line = line.substring(1, line.length());
			}

			if (split[0].equalsIgnoreCase("player")) {
				type = SenderType.PLAYER;
			}

			this.command = line;
		}

		public String getCommand() {
			return command;
		}

		public SenderType getType() {
			return type;
		}
	}

	public final class Bonuses {

		private ItemHandler gameItem;
		private PotionEffect potionEffect;
		private Sound sound;

		private int chance = -1, points = 0;
		private float soundPitch = 1f, soundVolume = 0f;

		public Bonuses(String one) {
			if (one.startsWith("chance:")) {
				one = one.replace("chance:", "");

				String[] split = one.split("_", 2);
				if (split.length != 0) {
					chance = Utils.tryParseInt(split[0]).orElse(-1);
				}

				one = one.replace(chance + "_", "");
			}

			if (one.contains("points:")) {
				one = one.replace("points:", "");

				String[] split = one.split(":", 2);

				if (split.length > 1) {
					points = Utils.tryParseInt(split[1]).orElse(0);
				}
			}

			if (one.contains("effect:")) {
				one = one.replace("effect:", "");

				String[] split = one.split(":", 3);
				if (split.length == 0) {
					return;
				}

				PotionEffectType effect = PotionEffectType.getByName(split[0]);
				if (effect == null) {
					return;
				}

				int duration = (split.length > 1 ? Utils.tryParseInt(split[1]).orElse(5) : 5) * 20;
				int amplifier = split.length > 2 ? Utils.tryParseInt(split[2]).orElse(1) : 1;

				potionEffect = new PotionEffect(effect, duration, amplifier);
			} else if (one.contains("sound:")) {
				one = one.replace("sound:", "");

				String[] split = one.split(":", 3);

				try {
					sound = Sound.valueOf(split[0].toUpperCase());
				} catch (IllegalArgumentException e) {
					return;
				}

				soundVolume = split.length > 1 ? Utils.tryParseFloat(split[1]).orElse(1f) : 1f;
				soundPitch = split.length > 2 ? Utils.tryParseFloat(split[2]).orElse(1f) : 1f;
			} else if (one.contains("givegameitem:")) {
				one = one.replace("givegameitem:", "");

				String[] split = one.split(":", 2);
				if (split.length == 0) {
					return;
				}

				String itemName = split[0].toLowerCase();

				switch (itemName) {
				case "grenade":
					gameItem = Items.getGameItem(1);
					break;
				case "combataxe":
					gameItem = Items.getGameItem(0);
					break;
				case "flash":
					gameItem = Items.getGameItem(5);
					break;
				case "pressuremine":
				case "mine":
					gameItem = Items.getGameItem(6);
					break;
				default:
					return;
				}

				if (gameItem == null) {
					return;
				}

				switch (itemName) {
				case "grenade":
				case "flash":
				case "pressuremine":
				case "mine":
					// clone the item to make sure we are not modifying anything
					gameItem = (ItemHandler) gameItem.clone();

					int amount = split.length > 1 ? Utils.tryParseInt(split[1]).orElse(1) : 1;
					if (amount < 1) {
						amount = 1;
					}

					gameItem = gameItem.setAmount(amount);
					break;
				default:
					break;
				}
			}
		}

		public PotionEffect getPotionEffect() {
			return potionEffect;
		}

		public Sound getSound() {
			return sound;
		}

		public float getSoundPitch() {
			return soundPitch;
		}

		public float getSoundVolume() {
			return soundVolume;
		}

		public int getChance() {
			return chance;
		}

		public ItemHandler getGameItem() {
			return gameItem;
		}

		public int getPoints() {
			return points;
		}
	}

	public enum SenderType {
		CONSOLE, PLAYER
	}
}
