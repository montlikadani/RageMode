package hu.montlikadani.ragemode.items;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import hu.montlikadani.ragemode.RageMode;

public class CombatAxe {

	public static ItemStack getCombatAxe() {
		ItemStack axe = new ItemStack(Material.IRON_AXE);
		ItemMeta meta = axe.getItemMeta();
		meta.setDisplayName(getCombatAxeName());

		List<String> lore = RageMode.getInstance().getConfiguration().getCfg().getStringList("items.combatAxe.lore");
		if (lore != null && !lore.isEmpty())
			meta.setLore(color(lore));

		axe.setItemMeta(meta);
		return axe;
	}

	private static List<String> color(List<String> lore) {
		List<String> clore = new ArrayList<>();
		for (String s : lore) {
			clore.add(RageMode.getLang().colors(s));
		}
		return clore;
	}

	public static String getCombatAxeName() {
		String iname = RageMode.getInstance().getConfiguration().getCfg().getString("items.combatAxe.name");
		return iname != null && !iname.equals("") ? RageMode.getLang().colors(iname) : org.bukkit.ChatColor.GOLD + "CombatAxe";
	}
}
