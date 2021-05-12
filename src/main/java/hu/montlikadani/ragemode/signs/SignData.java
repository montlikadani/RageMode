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

import hu.montlikadani.ragemode.RageMode;
import hu.montlikadani.ragemode.config.configconstants.ConfigValues;
import hu.montlikadani.ragemode.gameLogic.Game;
import hu.montlikadani.ragemode.gameLogic.GameStatus;
import hu.montlikadani.ragemode.gameUtils.GameUtils;
import hu.montlikadani.ragemode.utils.MaterialUtil;
import hu.montlikadani.ragemode.utils.ServerVersion;

public class SignData {

	private final String world, gameName;
	private final SignPlaceholder placeholder;

	private double x, y, z;

	private final RageMode plugin = org.bukkit.plugin.java.JavaPlugin.getPlugin(RageMode.class);

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
		Game game = GameUtils.getGame(gameName);

		if (game != null) {
			List<String> lines = placeholder.parsePlaceholder(game);

			for (int i = 0; i < 4; i++) {
				plugin.getComplement().setLine(sign, i, lines.get(i));
			}

			if (ConfigValues.getSignBackground() != SignBackgrounds.NONE && MaterialUtil.isWallSign(sign.getType())) {
				changeBlockBackground(game);
			}
		} else {
			String[] errorLines = { "\u00a74ERROR:", "\u00a76Game", "\u00a76with that name", "\u00a7cnot found!" };
			for (int i = 0; i < 4; i++) {
				plugin.getComplement().setLine(sign, i, errorLines[i]);
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

		if (ServerVersion.isCurrentLower(ServerVersion.v1_13_R1)) {
			try {
				Block.class.getMethod("setData", byte.class).invoke(wall, (byte) color);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	// TODO Disgusting
	private void changeBlockBackground(Game game) {
		GameStatus status = game.getStatus();

		if (ServerVersion.isCurrentEqualOrHigher(ServerVersion.v1_13_R1)) {
			if (status == GameStatus.READY) {
				if (ConfigValues.getSignBackground() == SignBackgrounds.WOOL)
					updateBackground(Material.YELLOW_WOOL);
				else if (ConfigValues.getSignBackground() == SignBackgrounds.GLASS)
					updateBackground(Material.YELLOW_STAINED_GLASS);
				else if (ConfigValues.getSignBackground() == SignBackgrounds.TERRACOTTA
						|| ConfigValues.getSignBackground() == SignBackgrounds.CLAY)
					updateBackground(Material.YELLOW_TERRACOTTA);
			}

			if (status == GameStatus.WAITING && game.getPlayers().size() >= game.maxPlayers) {
				if (ConfigValues.getSignBackground() == SignBackgrounds.WOOL)
					updateBackground(Material.BLUE_WOOL);
				else if (ConfigValues.getSignBackground() == SignBackgrounds.GLASS)
					updateBackground(Material.BLUE_STAINED_GLASS);
				else if (ConfigValues.getSignBackground() == SignBackgrounds.TERRACOTTA
						|| ConfigValues.getSignBackground() == SignBackgrounds.CLAY)
					updateBackground(Material.BLUE_TERRACOTTA);
			}

			if (status == GameStatus.RUNNING && game.isRunning()) {
				if (ConfigValues.getSignBackground() == SignBackgrounds.WOOL)
					updateBackground(Material.LIME_WOOL);
				else if (ConfigValues.getSignBackground() == SignBackgrounds.GLASS)
					updateBackground(Material.LIME_STAINED_GLASS);
				else if (ConfigValues.getSignBackground() == SignBackgrounds.TERRACOTTA
						|| ConfigValues.getSignBackground() == SignBackgrounds.CLAY)
					updateBackground(Material.LIME_TERRACOTTA);
			} else if (status == GameStatus.STOPPED || status == GameStatus.NOTREADY) {
				if (ConfigValues.getSignBackground() == SignBackgrounds.WOOL)
					updateBackground(Material.RED_WOOL);
				else if (ConfigValues.getSignBackground() == SignBackgrounds.GLASS)
					updateBackground(Material.RED_STAINED_GLASS);
				else if (ConfigValues.getSignBackground() == SignBackgrounds.TERRACOTTA
						|| ConfigValues.getSignBackground() == SignBackgrounds.CLAY)
					updateBackground(Material.RED_TERRACOTTA);
			}
		} else {
			if (status == GameStatus.READY) {
				if (ConfigValues.getSignBackground() == SignBackgrounds.WOOL)
					updateBackground(Material.getMaterial("WOOL"), 4);
				else if (ConfigValues.getSignBackground() == SignBackgrounds.GLASS)
					updateBackground(Material.getMaterial("STAINED_GLASS"), 4);
				else if (ConfigValues.getSignBackground() == SignBackgrounds.TERRACOTTA
						|| ConfigValues.getSignBackground() == SignBackgrounds.CLAY)
					updateBackground(Material.getMaterial("STAINED_CLAY"), 4);
			}

			if (status == GameStatus.WAITING && game.getPlayers().size() >= game.maxPlayers) {
				if (ConfigValues.getSignBackground() == SignBackgrounds.WOOL)
					updateBackground(Material.getMaterial("WOOL"), 11);
				else if (ConfigValues.getSignBackground() == SignBackgrounds.GLASS)
					updateBackground(Material.getMaterial("STAINED_GLASS"), 11);
				else if (ConfigValues.getSignBackground() == SignBackgrounds.TERRACOTTA
						|| ConfigValues.getSignBackground() == SignBackgrounds.CLAY)
					updateBackground(Material.getMaterial("STAINED_CLAY"), 11);
			}

			if (status == GameStatus.RUNNING && game.isRunning()) {
				if (ConfigValues.getSignBackground() == SignBackgrounds.WOOL)
					updateBackground(Material.getMaterial("WOOL"), 5);
				else if (ConfigValues.getSignBackground() == SignBackgrounds.GLASS)
					updateBackground(Material.getMaterial("STAINED_GLASS"), 5);
				else if (ConfigValues.getSignBackground() == SignBackgrounds.TERRACOTTA
						|| ConfigValues.getSignBackground() == SignBackgrounds.CLAY)
					updateBackground(Material.getMaterial("STAINED_CLAY"), 5);
			} else if (status == GameStatus.STOPPED || status == GameStatus.NOTREADY) {
				if (ConfigValues.getSignBackground() == SignBackgrounds.WOOL)
					updateBackground(Material.getMaterial("WOOL"), 14);
				else if (ConfigValues.getSignBackground() == SignBackgrounds.GLASS)
					updateBackground(Material.getMaterial("STAINED_GLASS"), 14);
				else if (ConfigValues.getSignBackground() == SignBackgrounds.TERRACOTTA
						|| ConfigValues.getSignBackground() == SignBackgrounds.CLAY)
					updateBackground(Material.getMaterial("STAINED_CLAY"), 14);
			}
		}
	}
}