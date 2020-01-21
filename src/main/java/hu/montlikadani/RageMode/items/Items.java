package hu.montlikadani.ragemode.items;

import hu.montlikadani.ragemode.RageMode;

public class Items {

	private static ItemHandler[] gi = RageMode.getInstance().getGameItems();
	private static ItemHandler[] li = RageMode.getInstance().getLobbyItems();

	public static ItemHandler getCombatAxe() {
		return gi[0];
	}

	public static ItemHandler getGrenade() {
		return gi[1];
	}

	public static ItemHandler getRageArrow() {
		return gi[2];
	}

	public static ItemHandler getRageBow() {
		return gi[3];
	}

	public static ItemHandler getRageKnife() {
		return gi[4];
	}

	public static ItemHandler getForceStarter() {
		return li[0];
	}

	public static ItemHandler getLeaveGameItem() {
		return li[1];
	}
}
