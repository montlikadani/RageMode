package hu.montlikadani.ragemode.area;

import org.bukkit.Location;

public class Area {

	protected Location highPoints;
	protected Location lowPoints;

	public Area(Location startLoc, Location endLoc) {
		int highx, highy, highz, lowx, lowy, lowz;

		int startLocBlock = startLoc.getBlockX(), endLocBlock = endLoc.getBlockX();

		if (startLocBlock > endLocBlock) {
			highx = startLocBlock;
			lowx = endLocBlock;
		} else {
			highx = endLocBlock;
			lowx = startLocBlock;
		}

		startLocBlock = startLoc.getBlockY();
		endLocBlock = endLoc.getBlockY();

		if (startLocBlock > endLocBlock) {
			highy = startLocBlock;
			lowy = endLocBlock;
		} else {
			highy = endLocBlock;
			lowy = startLocBlock;
		}

		startLocBlock = startLoc.getBlockZ();
		endLocBlock = endLoc.getBlockZ();

		if (startLocBlock > endLocBlock) {
			highz = startLocBlock;
			lowz = endLocBlock;
		} else {
			highz = endLocBlock;
			lowz = startLocBlock;
		}

		org.bukkit.World world = startLoc.getWorld();

		highPoints = new Location(world, highx, highy, highz);
		lowPoints = new Location(world, lowx, lowy, lowz);
	}

	public Location getHighLoc() {
		return highPoints;
	}

	public Location getLowLoc() {
		return lowPoints;
	}
}
