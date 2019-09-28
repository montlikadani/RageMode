package hu.montlikadani.ragemode.items;

import java.util.List;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import hu.montlikadani.ragemode.RageMode;
import hu.montlikadani.ragemode.Utils;

public class CombatAxe {

	public static ItemStack getItem() {
		ItemStack axe = new ItemStack(Material.IRON_AXE);
		ItemMeta meta = axe.getItemMeta();
		meta.setDisplayName(getName());

		List<String> lore = RageMode.getInstance().getConfiguration().getCfg().getStringList("items.combatAxe.lore");
		if (lore != null && !lore.isEmpty())
			meta.setLore(Utils.colorList(lore));

		axe.setItemMeta(meta);
		return axe;
	}

	public static String getName() {
		String iname = RageMode.getInstance().getConfiguration().getCfg().getString("items.combatAxe.name", "");
		return !iname.equals("") ? Utils.colors(iname) : org.bukkit.ChatColor.GOLD + "CombatAxe";
	}
}
