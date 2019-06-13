package hu.montlikadani.ragemode.signs;

import java.util.List;
import java.util.logging.Level;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Tag;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.BlockState;
import org.bukkit.block.Sign;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.material.Directional;

import hu.montlikadani.ragemode.RageMode;
import hu.montlikadani.ragemode.Utils;
import hu.montlikadani.ragemode.gameLogic.GameStatus;
import hu.montlikadani.ragemode.gameLogic.PlayerList;
import hu.montlikadani.ragemode.gameUtils.GameUtils;
import hu.montlikadani.ragemode.gameUtils.GetGames;

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

	public int getX() {
		return x;
	}

	public int getY() {
		return y;
	}

	public int getZ() {
		return z;
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

	public void updateSign() {
		Location location = getLocation();

		if (location.getWorld().getChunkAt(location).isLoaded()) {
			Block b = location.getBlock();
			if (b.getState() instanceof Sign) {
				Sign sign = (Sign) b.getState();
				if (placeholder != null) {
					List<String> lines = placeholder.parsePlaceholder(game);
					if (placeholder.getLines().size() >= 5 || placeholder.getLines().size() <= 3) {
						Bukkit.getLogger().log(Level.INFO, "[RageMode] In the configuration the signs lines not more/less than 4.");
						return;
					}

					for (int i = 0; i < 4; i++) {
						sign.setLine(i, lines.get(i).toString());

						YamlConfiguration cfg = RageMode.getInstance().getConfiguration().getCfg();
						String path = "signs.background.";
						if (cfg.getBoolean(path + "enable")) {
							if (!Utils.getVersion().contains("1.8")) {
								if (Utils.getVersion().contains("1.14") && Tag.WALL_SIGNS.isTagged(sign.getType())
										|| !Utils.getVersion().contains("1.14") && sign.getType() == Material.getMaterial("WALL_SIGN")) {
									if (GameUtils.getStatus() == GameStatus.WAITING) {
										if (PlayerList.getPlayersInGame(game).length == GetGames.getMaxPlayers(game)) {
											if (cfg.getString(path + "type").equals("wool"))
												updateBackground(Material.BLUE_WOOL);
											else if (cfg.getString(path + "type").equals("glass"))
												updateBackground(Material.BLUE_STAINED_GLASS);
											else if (cfg.getString(path + "type").equals("terracotta"))
												updateBackground(Material.BLUE_TERRACOTTA);
										} else {
											if (cfg.getString(path + "type").equals("wool"))
												updateBackground(Material.LIME_WOOL);
											else if (cfg.getString(path + "type").equals("glass"))
												updateBackground(Material.LIME_STAINED_GLASS);
											else if (cfg.getString(path + "type").equals("terracotta"))
												updateBackground(Material.LIME_TERRACOTTA);
										}
									}

									if (PlayerList.isGameRunning(game)) {
										if (cfg.getString(path + "type").equals("wool"))
											updateBackground(Material.LIME_WOOL);
										else if (cfg.getString(path + "type").equals("glass"))
											updateBackground(Material.LIME_STAINED_GLASS);
										else if (cfg.getString(path + "type").equals("terracotta"))
											updateBackground(Material.LIME_TERRACOTTA);
									} else if (GameUtils.getStatus() == GameStatus.STOPPED) {
										if (cfg.getString(path + "type").equals("wool"))
											updateBackground(Material.RED_WOOL);
										else if (cfg.getString(path + "type").equals("glass"))
											updateBackground(Material.RED_STAINED_GLASS);
										else if (cfg.getString(path + "type").equals("terracotta"))
											updateBackground(Material.RED_TERRACOTTA);
									}
								}
							} else {
								if (sign.getType() == Material.getMaterial("WALL_SIGN")) {
									if (GameUtils.getStatus() == GameStatus.WAITING) {
										if (PlayerList.getPlayersInGame(game).length == GetGames.getMaxPlayers(game)) {
											if (cfg.getString(path + "type").equals("wool"))
												updateBackground(Material.getMaterial("WOOL"));
											else if (cfg.getString(path + "type").equals("glass"))
												updateBackground(Material.getMaterial("STAINED_GLASS"));
											else if (cfg.getString(path + "type").equals("terracotta"))
												updateBackground(Material.getMaterial("STAINED_CLAY"));
										} else {
											if (cfg.getString(path + "type").equals("wool"))
												updateBackground(Material.getMaterial("WOOL"));
											else if (cfg.getString(path + "type").equals("glass"))
												updateBackground(Material.getMaterial("STAINED_GLASS"));
											else if (cfg.getString(path + "type").equals("terracotta"))
												updateBackground(Material.getMaterial("STAINED_CLAY"));
										}
									}

									if (GameUtils.getStatus() == GameStatus.RUNNING && PlayerList.isGameRunning(game)) {
										if (cfg.getString(path + "type").equals("wool"))
											updateBackground(Material.getMaterial("WOOL"));
										else if (cfg.getString(path + "type").equals("glass"))
											updateBackground(Material.getMaterial("STAINED_GLASS"));
										else if (cfg.getString(path + "type").equals("terracotta"))
											updateBackground(Material.getMaterial("STAINED_CLAY"));
									} else if (GameUtils.getStatus() == GameStatus.STOPPED) {
										if (cfg.getString(path + "type").equals("wool"))
											updateBackground(Material.getMaterial("WOOL"));
										else if (cfg.getString(path + "type").equals("glass"))
											updateBackground(Material.getMaterial("STAINED_GLASS"));
										else if (cfg.getString(path + "type").equals("terracotta"))
											updateBackground(Material.getMaterial("STAINED_CLAY"));
									}
								}
							}
						}
						sign.update();
					}
				}
			}
		}
	}

	private void updateBackground(Material mat/*, int c*/) {
		Location loc = getLocation();
		BlockState s = (Sign) loc.getBlock().getState();
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
		// In 1.8 this works
		//wall.setData((byte) c);
	}
}