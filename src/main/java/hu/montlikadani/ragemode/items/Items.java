package hu.montlikadani.ragemode.items;

import org.jetbrains.annotations.Nullable;

import hu.montlikadani.ragemode.RageMode;

public class Items {

	private static final RageMode PLUGIN = org.bukkit.plugin.java.JavaPlugin.getPlugin(RageMode.class);

	/**
	 * Returns the game item by its index.
	 * <p>
	 * Indexes:
	 * 
	 * <pre>
	 * 0 - combatAxe
	 * 1 - grenade
	 * 2 - ragearrow
	 * 3 - ragebow
	 * 4 - rageknife
	 * 5 - flash
	 * 6 - pressure mine
	 * </pre>
	 * 
	 * @param index the array index of item (>= 0 && < length)
	 * @return {@link ItemHandler}, or null if the index is out of bounds or the
	 *         element at the given index was not set
	 */
	@Nullable
	public static ItemHandler getGameItem(int index) {
		return (index >= 0 && index < PLUGIN.getGameItems().length) ? PLUGIN.getGameItems()[index] : null;
	}

	/**
	 * Returns the lobby item by its index.
	 * <p>
	 * Indexes:
	 * 
	 * <pre>
	 * 0 - force starter
	 * 1 - leave game
	 * 2 - shop item
	 * 3 - hide messages item
	 * </pre>
	 * 
	 * @param index the array index of item (>= 0 && < length)
	 * @return {@link ItemHandler}, or null if the index is out of bounds or the
	 *         element at the given index was not set
	 */
	@Nullable
	public static ItemHandler getLobbyItem(int index) {
		return (index >= 0 && index < PLUGIN.getLobbyItems().length) ? PLUGIN.getLobbyItems()[index] : null;
	}
}
