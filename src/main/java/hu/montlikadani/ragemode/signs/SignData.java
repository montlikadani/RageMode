package hu.montlikadani.ragemode.signs;

import java.util.List;

import org.apache.commons.lang.Validate;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.BlockState;
import org.bukkit.block.Sign;
import org.bukkit.block.data.type.WallSign;
import org.bukkit.material.Directional;

import hu.montlikadani.ragemode.config.ConfigValues;
import hu.montlikadani.ragemode.gameLogic.Game;
import hu.montlikadani.ragemode.gameLogic.GameStatus;
import hu.montlikadani.ragemode.gameUtils.GameUtils;
import hu.montlikadani.ragemode.utils.MaterialUtil;
import hu.montlikadani.ragemode.utils.ServerVersion.Version;

public class SignData {

	private String world;

	private double x, y, z;

	private String gameName;
	private SignPlaceholder placeholder;

	public SignData(Location loc, String gameName) {
		Validate.notNull(loc, "loc cannot be null");

		this.world = loc.getWorld().getName();
		this.x = loc.getBlockX();
		this.y = loc.getBlockY();
		this.z = loc.getBlockZ();

		this.gameName = gameName == null ? "" : gameName;
		this.placeholder = new SignPlaceholder(ConfigValues.getSignTextLines());
	}

	public String getWorld() {
		return world;
	}

	public void setX(double x) {
		this.x = x;
	}

	public void setY(double y) {
		this.y = y;
	}

	public void setZ(double z) {
		this.z = z;
	}

	public Location getLocation() {
		return new Location(Bukkit.getWorld(world), x, y, z);
	}

	public String getGameName() {
		return gameName;
	}

	public SignPlaceholder getPlaceholder() {
		return placeholder;
	}

	public void updateSign() {
		Location location = getLocation();
		if (!location.getWorld().getChunkAt(location).isLoaded()) {
			return;
		}

		Block b = location.getBlock();
		if (!(b.getState() instanceof Sign)) {
			return;
		}

		Sign sign = (Sign) b.getState();
		if (GameUtils.isGameExist(gameName)) {
			List<String> lines = placeholder.parsePlaceholder(gameName);
			for (int i = 0; i < 4; i++) {
				sign.setLine(i, lines.get(i));
			}

			if (!ConfigValues.getSignBackground().equalsIgnoreCase("none") && MaterialUtil.isWallSign(sign.getType())) {
				changeBlockBackground();
			}
		} else {
			String[] errorLines = { "\u00a74ERROR:", "\u00a76Game", "\u00a76with that name", "\u00a7cnot found!" };
			for (int i = 0; i < 4; i++) {
				sign.setLine(i, errorLines[i]);
			}
		}

		sign.update();
	}

	private void updateBackground(Material mat) {
		updateBackground(mat, 0);
	}

	private void updateBackground(Material mat, int color) {
		if (mat == null) {
			return;
		}

		Location loc = getLocation();
		BlockState s = loc.getBlock().getState();
		BlockFace bf = null;
		try {
			bf = ((Directional) s.getData()).getFacing();
		} catch (ClassCastException e) {
			if (s.getBlockData() instanceof WallSign) {
				bf = ((WallSign) s.getBlockData()).getFacing();
			}
		}

		if (bf == null) {
			return;
		}

		Location loc2 = new Location(loc.getWorld(), loc.getBlockX() - bf.getModX(), loc.getBlockY() - bf.getModY(),
				loc.getBlockZ() - bf.getModZ());
		Block wall = loc2.getBlock();

		wall.setType(mat);

		if (Version.isCurrentLower(Version.v1_13_R1)) {
			try {
				Block.class.getMethod("setData", byte.class).invoke(wall, (byte) color);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	private void changeBlockBackground() {
		Game game = GameUtils.getGame(gameName);
		GameStatus status = game.getStatus();
		String type = ConfigValues.getSignBackground().toLowerCase();

		if (Version.isCurrentEqualOrHigher(Version.v1_13_R1)) {
			if (status == GameStatus.READY) {
				if (type.contentEquals("wool"))
					updateBackground(Material.YELLOW_WOOL);
				else if (type.contentEquals("glass"))
					updateBackground(Material.YELLOW_STAINED_GLASS);
				else if (type.contentEquals("terracotta") || type.contentEquals("clay"))
					updateBackground(Material.YELLOW_TERRACOTTA);
			}

			if (status == GameStatus.WAITING && game.getPlayers().size() == game.maxPlayers) {
				if (type.contentEquals("wool"))
					updateBackground(Material.BLUE_WOOL);
				else if (type.contentEquals("glass"))
					updateBackground(Material.BLUE_STAINED_GLASS);
				else if (type.contentEquals("terracotta") || type.contentEquals("clay"))
					updateBackground(Material.BLUE_TERRACOTTA);
			}

			if (status == GameStatus.RUNNING && game.isGameRunning()) {
				if (type.contentEquals("wool"))
					updateBackground(Material.LIME_WOOL);
				else if (type.contentEquals("glass"))
					updateBackground(Material.LIME_STAINED_GLASS);
				else if (type.contentEquals("terracotta") || type.contentEquals("clay"))
					updateBackground(Material.LIME_TERRACOTTA);
			} else if (status == GameStatus.STOPPED || status == GameStatus.NOTREADY) {
				if (type.contentEquals("wool"))
					updateBackground(Material.RED_WOOL);
				else if (type.contentEquals("glass"))
					updateBackground(Material.RED_STAINED_GLASS);
				else if (type.contentEquals("terracotta") || type.contentEquals("clay"))
					updateBackground(Material.RED_TERRACOTTA);
			}
		} else {
			if (status == GameStatus.READY) {
				if (type.contentEquals("wool"))
					updateBackground(Material.getMaterial("WOOL"), 4);
				else if (type.contentEquals("glass"))
					updateBackground(Material.getMaterial("STAINED_GLASS"), 4);
				else if (type.contentEquals("terracotta") || type.contentEquals("clay"))
					updateBackground(Material.getMaterial("STAINED_CLAY"), 4);
			}

			if (status == GameStatus.WAITING && game.getPlayers().size() == game.maxPlayers) {
				if (type.contentEquals("wool"))
					updateBackground(Material.getMaterial("WOOL"), 11);
				else if (type.contentEquals("glass"))
					updateBackground(Material.getMaterial("STAINED_GLASS"), 11);
				else if (type.contentEquals("terracotta") || type.contentEquals("clay"))
					updateBackground(Material.getMaterial("STAINED_CLAY"), 11);
			}

			if (status == GameStatus.RUNNING && game.isGameRunning()) {
				if (type.contentEquals("wool"))
					updateBackground(Material.getMaterial("WOOL"), 5);
				else if (type.contentEquals("glass"))
					updateBackground(Material.getMaterial("STAINED_GLASS"), 5);
				else if (type.contentEquals("terracotta") || type.contentEquals("clay"))
					updateBackground(Material.getMaterial("STAINED_CLAY"), 5);
			} else if (status == GameStatus.STOPPED || status == GameStatus.NOTREADY) {
				if (type.contentEquals("wool"))
					updateBackground(Material.getMaterial("WOOL"), 14);
				else if (type.contentEquals("glass"))
					updateBackground(Material.getMaterial("STAINED_GLASS"), 14);
				else if (type.contentEquals("terracotta") || type.contentEquals("clay"))
					updateBackground(Material.getMaterial("STAINED_CLAY"), 14);
			}
		}
	}
}