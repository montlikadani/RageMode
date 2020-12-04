package hu.montlikadani.ragemode.items;

import hu.montlikadani.ragemode.RageMode;

public class Items {

	private static ItemHandler[] gi = RageMode.getInstance().getGameItems();
	private static ItemHandler[] li = RageMode.getInstance().getLobbyItems();

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
	 * @param index the array index of item (>= 0 && <= length)
	 */
	public static ItemHandler getGameItem(int index) {
		return (index >= 0 && index <= gi.length) ? gi[index] : null;
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
	 * @param index the array index of item (>= 0 && <= length)
	 * @return
	 */
	public static ItemHandler getLobbyItem(int index) {
		return (index >= 0 && index <= li.length) ? li[index] : null;
	}
}
