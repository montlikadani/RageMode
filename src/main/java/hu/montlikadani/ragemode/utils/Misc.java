package hu.montlikadani.ragemode.utils;

import java.util.Optional;

import org.bukkit.Color;
import org.bukkit.Effect;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Particle.DustOptions;
import org.bukkit.command.CommandSender;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;

import hu.montlikadani.ragemode.Utils;
import hu.montlikadani.ragemode.ServerVersion.Version;

@SuppressWarnings("deprecation")
public class Misc {

	public static boolean hasPerm(CommandSender sender, String perm) {
		return !(sender instanceof Player) || sender.hasPermission(perm);
	}

	public static void sendMessage(CommandSender sender, String msg) {
		sendMessage(sender, msg, false);
	}

	public static void sendMessage(CommandSender sender, String msg, boolean colored) {
		if (sender != null && msg != null && !msg.isEmpty()) {
			sender.sendMessage(colored ? Utils.colors(msg) : msg);
		}
	}

	/**
	 * Gets the item in hand, this supports item in hand in 1.8 and item in main
	 * hand in 1.9+ versions
	 * 
	 * @param p Player
	 * @return ItemStack
	 */
	public static ItemStack getItemInHand(Player p) {
		return Version.isCurrentEqualOrLower(Version.v1_8_R3) ? p.getInventory().getItemInHand()
				: p.getInventory().getItemInMainHand();
	}

	/**
	 * Sets the item in hand, this supports item in hand in 1.8 and item in main
	 * hand in 1.9+ versions
	 * 
	 * @param p    Player
	 * @param item ItemStack
	 */
	public static void setItemInHand(Player p, ItemStack item) {
		if (Version.isCurrentEqualOrLower(Version.v1_8_R3))
			p.setItemInHand(item);
		else
			p.getInventory().setItemInMainHand(item);
	}

	/**
	 * Sets the item durability, this prevents error when the durability is fully
	 * removed in the next mc version.
	 * 
	 * @param item   ItemStack
	 * @param number short item durability
	 */
	public static void setDurability(ItemStack item, short number) {
		if (Version.isCurrentEqualOrHigher(Version.v1_13_R1)) {
			Optional.ofNullable(item.getItemMeta()).filter(meta -> meta instanceof Damageable)
					.ifPresent(meta -> ((Damageable) meta).setDamage(number));
		} else
			item.setDurability(number);
	}

	/**
	 * Gets the Enchantment name in item, this prevents the deprecated getByName()
	 * using in 1.13+ versions.
	 * <p>
	 * The latest is very different from the older enchantment name. Such as the new
	 * enchant name for DAMAGE_ALL (old) replaced to sharpness (new).
	 * 
	 * @param type Enchantment name
	 * @return Enchantment type
	 */
	public static Enchantment getEnchant(String type) {
		if (Version.isCurrentEqualOrHigher(Version.v1_13_R1))
			return Enchantment.getByKey(org.bukkit.NamespacedKey.minecraft(type.toLowerCase()));

		return Enchantment.getByName(type.toUpperCase());
	}

	/**
	 * Gets the player max health.
	 * 
	 * @param p Player
	 * @return player max health
	 */
	public static double getMaxHealth(Player p) {
		if (Version.isCurrentLower(Version.v1_9_R1)) {
			return p.getMaxHealth();
		}

		Optional<org.bukkit.attribute.AttributeInstance> ai = Optional
				.ofNullable(p.getAttribute(org.bukkit.attribute.Attribute.GENERIC_MAX_HEALTH));
		return ai.isPresent() ? ai.get().getDefaultValue() : 20D;
	}

	public static void spawnParticle(Particle particle, Object effect, Location loc, int count) {
		if (Version.isCurrentLower(Version.v1_9_R1)) {
			loc.getWorld().playEffect(loc, (Effect) effect, 1);
		} else if (particle == Particle.REDSTONE) {
			DustOptions dustOptions = new DustOptions(Color.RED, 2);
			loc.getWorld().spawnParticle(particle, loc, count, dustOptions);
		} else if (particle == Particle.ITEM_CRACK) {
			ItemStack itemCrackData = new ItemStack(loc.getBlock().getType());
			loc.getWorld().spawnParticle(particle, loc, count, itemCrackData);
		} else if (particle == Particle.BLOCK_CRACK || particle == Particle.BLOCK_DUST
				|| particle == Particle.FALLING_DUST) {
			loc.getWorld().spawnParticle(particle, loc, count, loc.getBlock().getType().createBlockData());
		} else {
			loc.getWorld().spawnParticle(particle, loc, count);
		}
	}
}
