package hu.montlikadani.ragemode;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;

import hu.montlikadani.ragemode.MinecraftVersion.Version;

@SuppressWarnings("deprecation")
public class NMS {

	/**
	 * Gets the item in hand, this supports item in hand in 1.8 and item in main hand in 1.9+ versions
	 * 
	 * @param p Player
	 * @return ItemStack
	 */
	public static ItemStack getItemInHand(Player p) {
		return Version.isCurrentEqualOrLower(Version.v1_8_R3) ? p.getInventory().getItemInHand() : p.getInventory().getItemInMainHand();
	}

	/**
	 * Sets the item in hand, this supports item in hand in 1.8 and item in main hand in 1.9+ versions
	 * 
	 * @param p Player
	 * @param item ItemStack
	 */
	public static void setItemInHand(Player p, ItemStack item) {
		if (Version.isCurrentEqualOrLower(Version.v1_8_R3))
			p.setItemInHand(item);
		else
			p.getInventory().setItemInMainHand(item);
	}

	/**
	 * Sets the item durability, this prevents error when the durability is fully removed in the next mc version.
	 * 
	 * @param item ItemStack
	 * @param number short item durability
	 */
	public static void setDurability(ItemStack item, short number) {
		if (Version.isCurrentEqualOrHigher(Version.v1_13_R1))
			((Damageable) item.getItemMeta()).setDamage(number);
		else
			item.setDurability(number);
	}
}