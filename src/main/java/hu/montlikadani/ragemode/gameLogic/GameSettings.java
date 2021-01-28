package hu.montlikadani.ragemode.gameLogic;

import java.util.Optional;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.configuration.file.FileConfiguration;

import hu.montlikadani.ragemode.RageMode;
import hu.montlikadani.ragemode.config.Configuration;

public class GameSettings {

	private String worldName, gameName = "";

	/**
	 * Returns the max players size from this game.
	 */
	public int maxPlayers = 0;

	/**
	 * Returns the minimum players size from this game.
	 */
	public int minPlayers = 0;

	/**
	 * Returns the game time for this game.
	 */
	public int gameTime = 0;

	/**
	 * Returns the actionbar boolean if it is enabled for this game, otherwise
	 * false.
	 */
	public boolean actionbarEnabled = false;

	/**
	 * Returns the bossbar boolean if it is enabled for this game, otherwise false.
	 */
	public boolean bossbarEnabled = false;

	public GameSettings(String gameName) {
		if (gameName != null) {
			this.gameName = gameName;
			loadFromConfig();
		}
	}

	private void loadFromConfig() {
		FileConfiguration conf = RageMode.getInstance().getConfiguration().getArenasCfg();
		String path = "arenas." + gameName + ".";

		maxPlayers = conf.getInt(path + "maxplayers", 2);
		minPlayers = conf.getInt(path + "minplayers", 1);
		gameTime = conf.getInt(path + "gametime") < 0 ? 5 : conf.getInt(path + "gametime");
		worldName = conf.getString(path + "world");
		actionbarEnabled = conf.getBoolean(path + "actionbar", false);
		bossbarEnabled = conf.getBoolean(path + "bossbar", false);
	}

	public void saveGamesSettings() {
		if (gameName.isEmpty()) {
			return;
		}

		FileConfiguration conf = RageMode.getInstance().getConfiguration().getArenasCfg();
		String path = "arenas." + gameName + ".";

		conf.set(path + "maxplayers", maxPlayers);
		conf.set(path + "minplayers", minPlayers);
		conf.set(path + "gametime", gameTime);
		conf.set(path + "world", worldName);
		conf.set(path + "actionbar", actionbarEnabled);
		conf.set(path + "bossbar", bossbarEnabled);

		Configuration.saveFile(conf, RageMode.getInstance().getConfiguration().getArenasFile());
	}

	/**
	 * Returns the world for this game.
	 * 
	 * @return the world object if present, otherwise {@link Optional#empty()}
	 */
	public Optional<World> getWorld() {
		return worldName == null ? Optional.empty() : Optional.ofNullable(Bukkit.getWorld(worldName));
	}
}
