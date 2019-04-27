package hu.montlikadani.ragemode;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class NMS {

	/**
	 * Gets the item in hand, this supports item in hand in 1.8 and item in main hand in 1.9+ versions
	 * 
	 * @param p Player
	 * @return ItemStack
	 */
	@SuppressWarnings("deprecation")
	public static ItemStack getItemInHand(Player p) {
		return Utils.getVersion().contains("1.8") ? p.getItemInHand() : p.getInventory().getItemInMainHand();
	}

	/**
	 * Sets the item in hand, this supports item in hand in 1.8 and item in main hand in 1.9+ versions
	 * 
	 * @param p Player
	 * @param item ItemStack
	 */
	@SuppressWarnings("deprecation")
	public static void setItemInHand(Player p, ItemStack item) {
		if (Utils.getVersion().equals("1.8"))
			p.setItemInHand(item);
		else
			p.getInventory().setItemInMainHand(item);
	}
}