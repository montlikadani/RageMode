package hu.montlikadani.ragemode.signs;

import java.util.List;
import java.util.Optional;
import java.util.logging.Level;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.BlockState;
import org.bukkit.block.Sign;
import org.bukkit.material.Directional;

import hu.montlikadani.ragemode.ServerVersion.Version;
import hu.montlikadani.ragemode.config.ConfigValues;
import hu.montlikadani.ragemode.gameLogic.GameStatus;
import hu.montlikadani.ragemode.gameUtils.GameUtils;
import hu.montlikadani.ragemode.gameUtils.GetGames;
import hu.montlikadani.ragemode.utils.MaterialUtil;

public class SignData {

	private String world;
	private int x;
	private int y;
	private int z;

	private String game;
	private SignPlaceholder placeholder;

	public SignData(Location loc, String game, SignPlaceholder placeholder) {
		this.world = loc.getWorld().getName();
		this.x = loc.getBlockX();
		this.y = loc.getBlockY();
		this.z = loc.getBlockZ();

		this.game = game;
		this.placeholder = placeholder;
	}

	public String getWorld() {
		return world;
	}

	public void setX(int x) {
		this.x = x;
	}

	public void setY(int y) {
		this.y = y;
	}

	public void setZ(int z) {
		this.z = z;
	}

	public Location getLocation() {
		return new Location(Bukkit.getWorld(world), x, y, z);
	}

	public String getGame() {
		return game;
	}

	public SignPlaceholder getPlaceholder() {
		return placeholder;
	}

	@SuppressWarnings("deprecation")
	public void updateSign() {
		Location location = getLocation();
		// TODO: Remove or change that to fix server crash on startup
		if (!location.getWorld().getChunkAt(location).isLoaded()) {
			return;
		}

		Block b = location.getBlock();
		if (!(b.getState() instanceof Sign)) {
			return;
		}

		Sign sign = (Sign) b.getState();
		if (placeholder != null && game != null && GameUtils.isGameWithNameExists(game)) {
			if (placeholder.getLines().size() > 4 || placeholder.getLines().size() < 4) {
				Bukkit.getLogger().log(Level.INFO, "In the configuration the signs lines is equal to 4.");
				return;
			}

			List<String> lines = placeholder.parsePlaceholder(game);
			for (int i = 0; i < 4; i++) {
				sign.setLine(i, lines.get(i));
			}

			if ((ConfigValues.isSignBackground() || !ConfigValues.getSignBackground().equalsIgnoreCase("none"))
					&& MaterialUtil.isWallSign(sign.getType())) {
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
		Location loc = getLocation();
		BlockState s = loc.getBlock().getState();
		BlockFace bf = null;
		try {
			bf = ((Directional) s.getData()).getFacing();
		} catch (ClassCastException e) {
			org.bukkit.block.data.type.WallSign data = (org.bukkit.block.data.type.WallSign) s.getBlockData();
			bf = data.getFacing();
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
		Optional<GameStatus> status = GameUtils.getStatus(game);
		if (!status.isPresent()) {
			return;
		}

		String type = ConfigValues.getSignBackground().toLowerCase();

		if (Version.isCurrentEqualOrHigher(Version.v1_13_R1)) {
			if (status.get() == GameStatus.READY) {
				if (type.equals("wool"))
					updateBackground(Material.YELLOW_WOOL);
				else if (type.equals("glass"))
					updateBackground(Material.YELLOW_STAINED_GLASS);
				else if (type.equals("terracotta") || type.equals("clay"))
					updateBackground(Material.YELLOW_TERRACOTTA);
			}

			if (status.get() == GameStatus.WAITING
					&& GameUtils.getGame(game).getPlayers().size() == GetGames.getMaxPlayers(game)) {
				if (type.equals("wool"))
					updateBackground(Material.BLUE_WOOL);
				else if (type.equals("glass"))
					updateBackground(Material.BLUE_STAINED_GLASS);
				else if (type.equals("terracotta") || type.equals("clay"))
					updateBackground(Material.BLUE_TERRACOTTA);
			}

			if (status.get() == GameStatus.RUNNING && GameUtils.getGame(game).isGameRunning()) {
				if (type.equals("wool"))
					updateBackground(Material.LIME_WOOL);
				else if (type.equals("glass"))
					updateBackground(Material.LIME_STAINED_GLASS);
				else if (type.equals("terracotta") || type.equals("clay"))
					updateBackground(Material.LIME_TERRACOTTA);
			} else if (status.get() == GameStatus.STOPPED || status.get() == GameStatus.NOTREADY) {
				if (type.equals("wool"))
					updateBackground(Material.RED_WOOL);
				else if (type.equals("glass"))
					updateBackground(Material.RED_STAINED_GLASS);
				else if (type.equals("terracotta") || type.equals("clay"))
					updateBackground(Material.RED_TERRACOTTA);
			}
		} else {
			if (status.get() == GameStatus.READY) {
				if (type.equals("wool"))
					updateBackground(Material.getMaterial("WOOL"), 4);
				else if (type.equals("glass"))
					updateBackground(Material.getMaterial("STAINED_GLASS"), 4);
				else if (type.equals("terracotta") || type.equals("clay"))
					updateBackground(Material.getMaterial("STAINED_CLAY"), 4);
			}

			if (status.get() == GameStatus.WAITING
					&& GameUtils.getGame(game).getPlayers().size() == GetGames.getMaxPlayers(game)) {
				if (type.equals("wool"))
					updateBackground(Material.getMaterial("WOOL"), 11);
				else if (type.equals("glass"))
					updateBackground(Material.getMaterial("STAINED_GLASS"), 11);
				else if (type.equals("terracotta") || type.equals("clay"))
					updateBackground(Material.getMaterial("STAINED_CLAY"), 11);
			}

			if (status.get() == GameStatus.RUNNING && GameUtils.getGame(game).isGameRunning()) {
				if (type.equals("wool"))
					updateBackground(Material.getMaterial("WOOL"), 5);
				else if (type.equals("glass"))
					updateBackground(Material.getMaterial("STAINED_GLASS"), 5);
				else if (type.equals("terracotta") || type.equals("clay"))
					updateBackground(Material.getMaterial("STAINED_CLAY"), 5);
			} else if (status.get() == GameStatus.STOPPED || status.get() == GameStatus.NOTREADY) {
				if (type.equals("wool"))
					updateBackground(Material.getMaterial("WOOL"), 14);
				else if (type.equals("glass"))
					updateBackground(Material.getMaterial("STAINED_GLASS"), 14);
				else if (type.equals("terracotta") || type.equals("clay"))
					updateBackground(Material.getMaterial("STAINED_CLAY"), 14);
			}
		}
	}
}