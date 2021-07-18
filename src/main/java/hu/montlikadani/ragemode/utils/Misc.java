package hu.montlikadani.ragemode.utils;

import java.util.Optional;

import org.bukkit.Color;
import org.bukkit.Effect;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.command.CommandSender;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;

@SuppressWarnings("deprecation")
public final class Misc {

	private Misc() {
	}

	public static void sendMessage(CommandSender sender, String msg) {
		if (sender != null && msg != null && !msg.isEmpty()) {
			sender.sendMessage(Utils.colors(msg));
		}
	}

	/**
	 * Gets the current item in the player hand.
	 * 
	 * @param human {@link HumanEntity}
	 * @return {@link ItemStack}
	 */
	public static ItemStack getItemInHand(HumanEntity human) {
		return ServerVersion.isCurrentEqualOrLower(ServerVersion.v1_8_R3) ? human.getInventory().getItemInHand()
				: human.getInventory().getItemInMainHand();
	}

	/**
	 * Sets the given item in the player hand
	 * 
	 * @param human {@link HumanEntity}
	 * @param item  the {@link ItemStack} to set
	 */
	public static void setItemInHand(HumanEntity human, ItemStack item) {
		if (ServerVersion.isCurrentEqualOrLower(ServerVersion.v1_8_R3))
			human.setItemInHand(item);
		else
			human.getInventory().setItemInMainHand(item);
	}

	/**
	 * Sets the given item durability
	 * 
	 * @param item   {@link ItemStack}
	 * @param number the amount of item durability in 16-bit signed
	 */
	public static void setDurability(ItemStack item, short number) {
		if (ServerVersion.isCurrentEqualOrHigher(ServerVersion.v1_13_R1)) {
			Optional.ofNullable(item.getItemMeta()).filter(meta -> meta instanceof Damageable)
					.ifPresent(meta -> ((Damageable) meta).setDamage(number));
		} else
			item.setDurability(number);
	}

	/**
	 * Returns the instance of {@link Enchantment} from the given name if present,
	 * otherwise null.
	 * 
	 * @param type a valid enchantment name
	 * @return {@link Enchantment} or null if not present
	 */
	public static Enchantment getEnchant(String type) {
		if (ServerVersion.isCurrentEqualOrHigher(ServerVersion.v1_13_R1))
			return Enchantment.getByKey(org.bukkit.NamespacedKey.minecraft(type.toLowerCase()));

		return Enchantment.getByName(type.toUpperCase());
	}

	/**
	 * Gets the player's max health.
	 * 
	 * @param p {@link Player}
	 * @return the amount of player's max health
	 */
	public static double getMaxHealth(Player p) {
		if (ServerVersion.isCurrentLower(ServerVersion.v1_9_R1)) {
			return p.getMaxHealth();
		}

		Optional<org.bukkit.attribute.AttributeInstance> ai = Optional
				.ofNullable(p.getAttribute(org.bukkit.attribute.Attribute.GENERIC_MAX_HEALTH));
		return ai.isPresent() ? ai.get().getDefaultValue() : 20D;
	}

	public static void spawnParticle(Particle particle, Object effect, Location loc, int count) {
		org.bukkit.World world = loc.getWorld();

		if (world == null)
			return;

		if (ServerVersion.isCurrentLower(ServerVersion.v1_9_R1)) {
			world.playEffect(loc, (Effect) effect, 1);
		} else if (ServerVersion.isCurrentEqualOrHigher(ServerVersion.v1_13_R2) && particle == Particle.REDSTONE) {
			world.spawnParticle(particle, loc, count, new Particle.DustOptions(Color.RED, 2));
		} else if (particle == Particle.ITEM_CRACK) {
			world.spawnParticle(particle, loc, count, new ItemStack(world.getBlockAt(loc).getType()));
		} else if (particle == Particle.BLOCK_CRACK || particle == Particle.BLOCK_DUST
				|| particle == Particle.FALLING_DUST) {
			world.spawnParticle(particle, loc, count, world.getBlockAt(loc).getType().createBlockData());
		} else if (ServerVersion.isCurrentEqualOrHigher(ServerVersion.v1_13_R2)
				&& (particle == Particle.LEGACY_BLOCK_CRACK || particle == Particle.LEGACY_BLOCK_DUST
						|| particle == Particle.LEGACY_FALLING_DUST)) {
			org.bukkit.Material type = world.getBlockAt(loc).getType();

			try {
				world.spawnParticle(particle, loc, count, type.getData().getDeclaredConstructor().newInstance(type));
			} catch (Exception e) {
				e.printStackTrace();
			}
		} else {
			world.spawnParticle(particle, loc, count);
		}
	}
}
