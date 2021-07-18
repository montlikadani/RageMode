package hu.montlikadani.ragemode.gameLogic;

import java.util.Optional;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.plugin.java.JavaPlugin;

import hu.montlikadani.ragemode.RageMode;
import hu.montlikadani.ragemode.config.Configuration;
import hu.montlikadani.ragemode.gameUtils.GameType;

public class GameSettings {

	private String gameName = "";

	/**
	 * The property for defining or retrieving the world name where this game was
	 * saved or will be saved.
	 */
	public String worldName = "";

	/**
	 * The field for defining or retrieving the max players size.
	 */
	public int maxPlayers = 0;

	/**
	 * The field for defining or retrieving the minimum players size.
	 */
	public int minPlayers = 0;

	/**
	 * The field for defining or retrieving the game time.
	 */
	public int gameTime = 0;

	/**
	 * The field to determine if the actionbar should be enabled for this game, or
	 * not.
	 */
	public boolean actionbarEnabled = false;

	/**
	 * The field to determine if the bossbar should be enabled for this game or not.
	 */
	public boolean bossbarEnabled = false;

	/**
	 * The field for determining if the spawning for zombies should be random or
	 * not.
	 */
	public boolean randomSpawnForZombies = false;

	public GameSettings(String gameName) {
		org.apache.commons.lang.Validate.notEmpty(gameName, "Game name cannot be null/empty");

		this.gameName = gameName;
		loadFromConfig();
	}

	private void loadFromConfig() {
		org.bukkit.configuration.MemorySection conf = JavaPlugin.getPlugin(RageMode.class).getConfiguration()
				.getArenasCfg();
		String path = "arenas." + gameName + ".";

		maxPlayers = conf.getInt(path + "maxplayers", 2);
		minPlayers = conf.getInt(path + "minplayers", 1);

		int gt = conf.getInt(path + "gametime");
		gameTime = gt < 0 ? 5 : gt;

		worldName = conf.getString(path + "world");
		actionbarEnabled = conf.getBoolean(path + "actionbar");
		bossbarEnabled = conf.getBoolean(path + "bossbar");
		randomSpawnForZombies = conf.getBoolean(path + "randomspawnforzombies");
	}

	public final void saveGamesSettings() {
		final RageMode plugin = JavaPlugin.getPlugin(RageMode.class);

		org.bukkit.configuration.file.FileConfiguration conf = plugin.getConfiguration().getArenasCfg();
		String path = "arenas." + gameName + ".";

		conf.set(path + "maxplayers", maxPlayers);
		conf.set(path + "minplayers", minPlayers);
		conf.set(path + "gametime", gameTime);
		conf.set(path + "world", worldName);
		conf.set(path + "actionbar", actionbarEnabled);
		conf.set(path + "bossbar", bossbarEnabled);

		if (randomSpawnForZombies && GameType.getByName(conf.getString(path + "gametype", "")) == GameType.APOCALYPSE) {
			conf.set(path + "zombie-spawns", null);
			conf.set(path + "randomspawnforzombies", randomSpawnForZombies);
		}

		Configuration.saveFile(conf, plugin.getConfiguration().getArenasFile());
	}

	/**
	 * Returns the world for this game.
	 * 
	 * @return the world object if present, otherwise {@link Optional#empty()}
	 */
	@org.jetbrains.annotations.NotNull
	public final Optional<World> getWorld() {
		return worldName == null ? Optional.empty() : Optional.ofNullable(Bukkit.getServer().getWorld(worldName));
	}
}
