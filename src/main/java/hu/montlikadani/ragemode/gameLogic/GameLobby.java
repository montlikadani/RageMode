package hu.montlikadani.ragemode.gameLogic;

import org.apache.commons.lang.Validate;
import org.bukkit.Bukkit;
import org.bukkit.Location;

import hu.montlikadani.ragemode.RageMode;
import hu.montlikadani.ragemode.config.Configuration;

public class GameLobby {

	/**
	 * Returns or sets the game lobby time in seconds
	 */
	public int lobbyTime = 30;

	/**
	 * Gets or sets the given game lobby location.
	 */
	public Location location;

	private Game game;
	private LobbyTimer lobbyTimer;

	public GameLobby(Game game) {
		Validate.notNull(game, "Game cannot be null");

		this.game = game;

		loadFromConfig();
		lobbyTimer = new LobbyTimer(game);
	}

	private void loadFromConfig() {
		org.bukkit.configuration.file.FileConfiguration conf = RageMode.getInstance().getConfiguration().getArenasCfg();

		String path = "arenas." + game.getName() + ".lobby";
		if (!conf.contains(path)) {
			return;
		}

		path += ".";

		if (conf.getInt(path + "lobbydelay") > 5) {
			lobbyTime = conf.getInt(path + "lobbydelay");
		}

		String world = conf.getString(path + "world", "");
		if (world.isEmpty()) {
			return;
		}

		double lobbyX = conf.getDouble(path + "x"), lobbyY = conf.getDouble(path + "y"),
				lobbyZ = conf.getDouble(path + "z"), lobbyYaw = conf.getDouble(path + "yaw"),
				lobbyPitch = conf.getDouble(path + "pitch");

		location = new Location(Bukkit.getWorld(world), lobbyX, lobbyY, lobbyZ, (float) lobbyYaw, (float) lobbyPitch);
	}

	public void saveToConfig() {
		org.bukkit.configuration.file.FileConfiguration conf = RageMode.getInstance().getConfiguration().getArenasCfg();

		String path = "arenas." + game.getName() + ".lobby.";

		conf.set(path + "world", location.getWorld().getName());
		conf.set(path + "x", location.getX());
		conf.set(path + "y", location.getY());
		conf.set(path + "z", location.getZ());
		conf.set(path + "yaw", location.getYaw());
		conf.set(path + "pitch", location.getPitch());

		conf.set(path + "lobbydelay", lobbyTime);

		Configuration.saveFile(conf, RageMode.getInstance().getConfiguration().getArenasFile());
	}

	/**
	 * Returns the lobby timer
	 * 
	 * @return {@link LobbyTimer}
	 */
	public LobbyTimer getLobbyTimer() {
		return lobbyTimer;
	}
}
