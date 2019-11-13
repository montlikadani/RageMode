package hu.montlikadani.ragemode.signs;

import java.util.List;
import java.util.logging.Level;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.BlockState;
import org.bukkit.block.Sign;
import org.bukkit.material.Directional;

import hu.montlikadani.ragemode.MinecraftVersion.Version;
import hu.montlikadani.ragemode.RageMode;
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

	protected void updateSign() {
		Location location = getLocation();

		if (location.getWorld().getChunkAt(location).isLoaded()) {
			Block b = location.getBlock();
			if (b.getState() instanceof Sign) {
				Sign sign = (Sign) b.getState();
				if (placeholder != null && game != null && GameUtils.isGameWithNameExists(game)) {
					List<String> lines = placeholder.parsePlaceholder(game);
					if (placeholder.getLines().size() > 4 || placeholder.getLines().size() < 4) {
						Bukkit.getLogger().log(Level.INFO, "In the configuration the signs lines is equal to 4.");
						return;
					}

					for (int i = 0; i < 4; i++) {
						sign.setLine(i, lines.get(i));

						if (RageMode.getInstance().getConfiguration().getCV().isSignBackground()
								&& MaterialUtil.isWallSign(sign.getType())) {
							chooseFromType();
						}
					}
				} else {
					String[] errorLines = { "\u00a74ERROR:", "\u00a76Game", "\u00a76with that name", "\u00a7cnot found!" };
					for (int i = 0; i < 4; i++) {
						sign.setLine(i, editLine(errorLines[i], i));
					}
				}

				sign.update();
			}
		}
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

	private String editLine(String text, int num) {
		int length = text.length();

		if (num == 2) {
			if (Version.isCurrentEqualOrHigher(Version.v1_14_R1) && length > 25) {
				text = text.substring(0, 25);
				text = text + "...";
				return text;
			}

			if (length > 15) {
				text = text.substring(0, 11);
				text = text + "...";
				return text;
			}
		}

		if (Version.isCurrentEqualOrHigher(Version.v1_14_R1) && length > 25) {
			return text = text.substring(0, 25);
		}

		if (length > 16) {
			text = text.substring(0, 16);
		}

		return text;
	}

	private void chooseFromType() {
		String type = RageMode.getInstance().getConfiguration().getCV().getSignBackground();

		if (Version.isCurrentEqualOrHigher(Version.v1_13_R1)) {
			if (GameUtils.getStatus(game) == GameStatus.WAITING) {
				if (GameUtils.getGame(game).getPlayers().size() == GetGames.getMaxPlayers(game)) {
					if (type.equals("wool"))
						updateBackground(Material.BLUE_WOOL);
					else if (type.equals("glass"))
						updateBackground(Material.BLUE_STAINED_GLASS);
					else if (type.equals("terracotta") || type.equals("clay"))
						updateBackground(Material.BLUE_TERRACOTTA);
				} else {
					if (type.equals("wool"))
						updateBackground(Material.LIME_WOOL);
					else if (type.equals("glass"))
						updateBackground(Material.LIME_STAINED_GLASS);
					else if (type.equals("terracotta") || type.equals("clay"))
						updateBackground(Material.LIME_TERRACOTTA);
				}
			}

			if (GameUtils.getGame(game).isGameRunning()) {
				if (type.equals("wool"))
					updateBackground(Material.LIME_WOOL);
				else if (type.equals("glass"))
					updateBackground(Material.LIME_STAINED_GLASS);
				else if (type.equals("terracotta") || type.equals("clay"))
					updateBackground(Material.LIME_TERRACOTTA);
			} else if (GameUtils.getStatus(game) == GameStatus.STOPPED) {
				if (type.equals("wool"))
					updateBackground(Material.RED_WOOL);
				else if (type.equals("glass"))
					updateBackground(Material.RED_STAINED_GLASS);
				else if (type.equals("terracotta") || type.equals("clay"))
					updateBackground(Material.RED_TERRACOTTA);
			}
		} else {
			if (GameUtils.getStatus(game) == GameStatus.WAITING) {
				if (GameUtils.getGame(game).getPlayers().size() == GetGames.getMaxPlayers(game)) {
					if (type.equals("wool"))
						updateBackground(Material.getMaterial("WOOL"), 11);
					else if (type.equals("glass"))
						updateBackground(Material.getMaterial("STAINED_GLASS"), 11);
					else if (type.equals("terracotta") || type.equals("clay"))
						updateBackground(Material.getMaterial("STAINED_CLAY"), 11);
				} else {
					if (type.equals("wool"))
						updateBackground(Material.getMaterial("WOOL"), 5);
					else if (type.equals("glass"))
						updateBackground(Material.getMaterial("STAINED_GLASS"), 5);
					else if (type.equals("terracotta") || type.equals("clay"))
						updateBackground(Material.getMaterial("STAINED_CLAY"), 5);
				}
			}

			if (GameUtils.getStatus(game) == GameStatus.RUNNING && GameUtils.getGame(game).isGameRunning()) {
				if (type.equals("wool"))
					updateBackground(Material.getMaterial("WOOL"), 5);
				else if (type.equals("glass"))
					updateBackground(Material.getMaterial("STAINED_GLASS"), 5);
				else if (type.equals("terracotta") || type.equals("clay"))
					updateBackground(Material.getMaterial("STAINED_CLAY"), 5);
			} else if (GameUtils.getStatus(game) == GameStatus.STOPPED) {
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