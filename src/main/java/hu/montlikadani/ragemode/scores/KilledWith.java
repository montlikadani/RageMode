package hu.montlikadani.ragemode.scores;

import org.bukkit.inventory.ItemStack;

import hu.montlikadani.ragemode.items.ItemHandler;
import hu.montlikadani.ragemode.items.Items;

public enum KilledWith {

	RAGEBOW(Items.getRageBow()), COMBATAXE(Items.getCombatAxe()), RAGEKNIFE(Items.getRageKnife()), EXPLOSION,
	GRENADE(Items.getGrenade()), UNKNOWN;

	private ItemStack item;

	private KilledWith() {
	}

	private KilledWith(ItemHandler item) {
		this.item = item.build();
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
