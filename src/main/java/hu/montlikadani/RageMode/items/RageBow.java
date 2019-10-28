package hu.montlikadani.ragemode.items;

import java.util.List;

import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import hu.montlikadani.ragemode.RageMode;
import hu.montlikadani.ragemode.Utils;

public class RageBow {

	public static ItemStack getItem() {
		ItemStack bow = new ItemStack(Material.BOW);
		ItemMeta meta = bow.getItemMeta();
		meta.setDisplayName(getName());

		List<String> lore = RageMode.getInstance().getConfiguration().getCfg().getStringList("items.rageBow.lore");
		if (lore != null && !lore.isEmpty())
			meta.setLore(Utils.colorList(lore));

		Enchantment infinity = Enchantment.ARROW_INFINITE;
		meta.addEnchant(infinity, 1, false);
		bow.setItemMeta(meta);
		return bow;
	}

	public static String getName() {
		String iname = RageMode.getInstance().getConfiguration().getCfg().getString("items.rageBow.name", "");
		return !iname.isEmpty() ? Utils.colors(iname) : org.bukkit.ChatColor.GOLD + "RageBow";
	}
}
