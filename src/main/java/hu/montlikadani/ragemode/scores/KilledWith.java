package hu.montlikadani.ragemode.scores;

import org.bukkit.inventory.ItemStack;

import hu.montlikadani.ragemode.items.ItemHandler;
import hu.montlikadani.ragemode.items.Items;

public enum KilledWith {

	RAGEBOW(Items.getGameItem(3)), COMBATAXE(Items.getGameItem(0)), RAGEKNIFE(Items.getGameItem(4)), EXPLOSION,
	GRENADE("explosion", Items.getGameItem(1)), RAGEARROW("explosion", Items.getGameItem(2)),
	PRESSUREMINE("explosion", Items.getGameItem(6)), UNKNOWN;

	private String metaName;
	private ItemStack item;

	private KilledWith() {
	}

	private KilledWith(ItemHandler item) {
		this("", item);
	}

	private KilledWith(String metaName, ItemHandler item) {
		this.metaName = metaName;
		this.item = item.get();
	}

	public String getMetaName() {
		return metaName;
	}

	public ItemStack asItemStack() {
		return item;
	}

	public static KilledWith getByName(String name) {
		for (KilledWith kw : values()) {
			if (kw.toString().equalsIgnoreCase(name.trim())) {
				return kw;
			}
		}

		return UNKNOWN;
	}
}
