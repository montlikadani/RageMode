package hu.montlikadani.ragemode.signs;

import java.util.List;

import org.apache.commons.lang.Validate;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.BlockState;
import org.bukkit.block.Sign;
import org.bukkit.block.data.type.WallSign;
import org.bukkit.material.Directional;

import hu.montlikadani.ragemode.RageMode;
import hu.montlikadani.ragemode.config.configconstants.ConfigValues;
import hu.montlikadani.ragemode.gameLogic.base.BaseGame;
import hu.montlikadani.ragemode.gameLogic.GameStatus;
import hu.montlikadani.ragemode.gameUtils.GameUtils;
import hu.montlikadani.ragemode.utils.MaterialUtil;
import hu.montlikadani.ragemode.utils.ServerVersion;

public final class SignData {

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
		return new Location(plugin.getServer().getWorld(world), x, y, z);
	}

	public String getGameName() {
		return gameName;
	}

	public SignPlaceholder getPlaceholder() {
		return placeholder;
	}

	public void updateSign() {
		Location location = getLocation();
		World world = location.getWorld();

		if (world == null || !world.getChunkAt(location).isLoaded()) {
			return;
		}

		BlockState state = world.getBlockAt(location).getState();
		if (!(state instanceof Sign)) {
			return;
		}

		Sign sign = (Sign) state;
		BaseGame game = GameUtils.getGame(gameName);

		if (game != null) {
			List<String> lines = placeholder.parsePlaceholder(game);

			for (int i = 0; i < 4; i++) {
				plugin.getComplement().setLine(sign, i, lines.get(i));
			}

			if (ConfigValues.getSignBackground() != SignBackgrounds.NONE && MaterialUtil.isWallSign(state.getType())) {
				changeBlockBackground(game);
			}
		} else {
			String[] errorLines = { "\u00a74ERROR:", "\u00a76Game", "\u00a76with that name", "\u00a7cnot found!" };
			for (int i = 0; i < 4; i++) {
				plugin.getComplement().setLine(sign, i, errorLines[i]);
			}
		}

		state.update();
	}

	private void updateBackground(Material mat) {
		updateBackground(mat, 0);
	}

	private void updateBackground(Material mat, int color) {
		if (mat == null) {
			return;
		}

		Location loc = getLocation();
		World world = loc.getWorld();
		BlockState s = world.getBlockAt(loc).getState();
		BlockFace bf = null;

		try {
			bf = ((Directional) s.getData()).getFacing();
		} catch (ClassCastException e) {
			org.bukkit.block.data.BlockData blockData = s.getBlockData();

			if (blockData instanceof WallSign) {
				bf = ((WallSign) blockData).getFacing();
			}
		}

		if (bf == null) {
			return;
		}

		Location loc2 = new Location(world, loc.getBlockX() - bf.getModX(), loc.getBlockY() - bf.getModY(),
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
	private void changeBlockBackground(BaseGame game) {
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