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

		highPoints = new Location(startLoc.getWorld(), highx, highy, highz);
		lowPoints = new Location(startLoc.getWorld(), lowx, lowy, lowz);
	}

	public long getSize() {
		int xsize = (Math.abs(highPoints.getBlockX() - lowPoints.getBlockX())) + 1,
				zsize = (Math.abs(highPoints.getBlockZ() - lowPoints.getBlockZ())) + 1,
				ysize = (Math.abs(highPoints.getBlockY() - lowPoints.getBlockY())) + 1;

		return xsize * ysize * zsize;
	}

	public int getXSize() {
		return (highPoints.getBlockX() - lowPoints.getBlockX()) + 1;
	}

	public int getYSize() {
		return (highPoints.getBlockY() - lowPoints.getBlockY()) + 1;
	}

	public int getZSize() {
		return (highPoints.getBlockZ() - lowPoints.getBlockZ()) + 1;
	}

	public Location getHighLoc() {
		return highPoints;
	}

	public Location getLowLoc() {
		return lowPoints;
	}
}
