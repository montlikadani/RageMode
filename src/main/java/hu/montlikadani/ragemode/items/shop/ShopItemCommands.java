package hu.montlikadani.ragemode.items.shop;

import java.util.List;

import org.bukkit.Effect;
import org.bukkit.Particle;
import org.bukkit.potion.PotionEffectType;

import hu.montlikadani.ragemode.items.ItemHandler;
import hu.montlikadani.ragemode.items.Items;
import hu.montlikadani.ragemode.utils.ServerVersion;
import hu.montlikadani.ragemode.utils.Utils;

public class ShopItemCommands {

	private ItemSetting itemSetting;

	private final List<CommandSetting> commands = new java.util.ArrayList<>();

	private NavigationType navigationType = NavigationType.WITHOUT;

	public ShopItemCommands(ItemSetting itemSetting, CommandSetting command, NavigationType navigationType) {
		this(itemSetting, navigationType);

		if (command != null) {
			commands.add(command);
		}
	}

	public ShopItemCommands(ItemSetting itemSetting, List<CommandSetting> commands, NavigationType navigationType) {
		this(itemSetting, navigationType);

		if (commands != null) {
			this.commands.addAll(commands);
		}
	}

	private ShopItemCommands(ItemSetting itemSetting, NavigationType navigationType) {
		this.itemSetting = itemSetting;

		if (navigationType != null) {
			this.navigationType = navigationType;
		}
	}

	public ItemSetting getItemSetting() {
		return itemSetting;
	}

	public List<CommandSetting> getCommands() {
		return commands;
	}

	public NavigationType getNavigationType() {
		return navigationType;
	}

	public static final class ItemSetting {

		private double cost;
		private int points;

		private PotionEffectSetting effectSetting;
		private GiveGameItem giveGameItem;
		private ItemTrail itemTrail;

		public ItemSetting(org.bukkit.configuration.ConfigurationSection sec, String configPath) {
			cost = sec.getDouble(configPath + ".cost.value");
			points = sec.getInt(configPath + ".cost.points");

			String path = sec.getString(configPath + ".effect");
			if (path != null) {
				effectSetting = new PotionEffectSetting(path);
			}

			if ((path = sec.getString(configPath + ".giveitem")) != null) {
				giveGameItem = new GiveGameItem(path);
			}

			if ((path = sec.getString(configPath + ".trail")) != null) {
				itemTrail = new ItemTrail(path);
			}
		}

		public double getCost() {
			return cost;
		}

		public int getPoints() {
			return points;
		}

		public PotionEffectSetting getEffectSetting() {
			return effectSetting;
		}

		public GiveGameItem getGiveGameItem() {
			return giveGameItem;
		}

		public ItemTrail getItemTrail() {
			return itemTrail;
		}

		public final class ItemTrail {

			private Object particle;

			public ItemTrail(String one) {
				if (one.isEmpty()) {
					return;
				}

				if (ServerVersion.isCurrentLower(ServerVersion.v1_9_R1)) {
					try {
						// Used to check if the Effect is the type of particle
						java.lang.reflect.Field field = Effect.class.getDeclaredClasses()[0]
								.getDeclaredField("PARTICLE");

						for (Effect effect : Effect.values()) {
							if (effect.toString().equalsIgnoreCase(one) && effect.getClass()
									.getDeclaredMethod("getType").invoke(effect) == field.get(effect)) {
								particle = effect;
								break;
							}
						}
					} catch (Exception e1) {
						e1.printStackTrace();
					}
				} else {
					for (Particle part : Particle.values()) {
						if (part.toString().equalsIgnoreCase(one)) {
							particle = part;
							break;
						}
					}
				}
			}

			public Object getParticle() {
				return particle;
			}
		}

		public final class GiveGameItem {

			private ItemHandler item;
			private int amount;

			public GiveGameItem(String one) {
				String[] split = one.split(":", 2);

				if (split.length == 0) {
					return;
				}

				if ((amount = split.length > 1 ? Utils.tryParseInt(split[1]).orElse(0) : 1) < 1) {
					amount = 1;
				}

				switch (split[0].toLowerCase()) {
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
					break;
				}
			}

			public ItemHandler getItem() {
				return item;
			}

			public int getAmount() {
				return amount;
			}
		}

		public final class PotionEffectSetting {

			private PotionEffectType effectType;
			private int amplifier, duration;

			public PotionEffectSetting(String path) {
				String[] split = path.split(":", 3);

				if (split.length == 0) {
					return;
				}

				effectType = PotionEffectType.getByName(split[0]);
				duration = split.length > 1 ? Utils.tryParseInt(split[1]).orElse(0) : 5;
				amplifier = split.length > 2 ? Utils.tryParseInt(split[2]).orElse(0) : 1;
			}

			public PotionEffectType getEffectType() {
				return effectType;
			}

			public int getAmplifier() {
				return amplifier;
			}

			public int getDuration() {
				return duration;
			}
		}
	}

	public static final class CommandSetting {

		private String command;

		private SenderType type = SenderType.CONSOLE;

		public CommandSetting(String one) {
			if (one.startsWith("console:")) {
				one = one.replace("console:", "");

				if (one.startsWith("/")) {
					one = one.substring(1);
				}
			} else {
				type = SenderType.PLAYER;
			}

			command = one;
		}

		public String getCommand() {
			return command;
		}

		public SenderType getType() {
			return type;
		}

		public enum SenderType {
			CONSOLE, PLAYER
		}
	}
}
