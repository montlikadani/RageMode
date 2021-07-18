package hu.montlikadani.ragemode.gameLogic;

import org.apache.commons.lang.Validate;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.plugin.java.JavaPlugin;

import hu.montlikadani.ragemode.RageMode;
import hu.montlikadani.ragemode.config.Configuration;
import hu.montlikadani.ragemode.gameLogic.base.BaseGame;

public final class GameLobby {

	/**
	 * Returns or sets the game lobby time in seconds
	 */
	public int lobbyTime = 30;

	/**
	 * Gets or sets the given game lobby location.
	 */
	public Location location;

	private final BaseGame game;
	private final LobbyTimer lobbyTimer;

	public GameLobby(BaseGame game) {
		Validate.notNull(game, "Game cannot be null");

		this.game = game;

		loadFromConfig();
		lobbyTimer = new LobbyTimer(game);
	}

	private void loadFromConfig() {
		org.bukkit.configuration.MemorySection conf = JavaPlugin.getPlugin(RageMode.class).getConfiguration()
				.getArenasCfg();

		String path = "arenas." + game.getName() + ".lobby";
		if (!conf.contains(path)) {
			return;
		}

		path += ".";

		int lobbyDelay = conf.getInt(path + "lobbydelay");
		lobbyTime = lobbyDelay > 5 ? lobbyDelay : 30;

		String worldName = conf.getString(path + "world", "");
		if (worldName.isEmpty()) {
			return;
		}

		org.bukkit.World world = Bukkit.getWorld(worldName);
		if (world == null) {
			return;
		}

		double lobbyX = conf.getDouble(path + "x"), lobbyY = conf.getDouble(path + "y"),
				lobbyZ = conf.getDouble(path + "z"), lobbyYaw = conf.getDouble(path + "yaw"),
				lobbyPitch = conf.getDouble(path + "pitch");

		location = new Location(world, lobbyX, lobbyY, lobbyZ, (float) lobbyYaw, (float) lobbyPitch);
	}

	public void saveToConfig() {
		org.bukkit.World world = location.getWorld();

		if (world == null) {
			return;
		}

		final RageMode plugin = JavaPlugin.getPlugin(RageMode.class);
		org.bukkit.configuration.file.FileConfiguration conf = plugin.getConfiguration().getArenasCfg();

		String path = "arenas." + game.getName() + ".lobby.";

		conf.set(path + "world", world.getName());
		conf.set(path + "x", location.getX());
		conf.set(path + "y", location.getY());
		conf.set(path + "z", location.getZ());
		conf.set(path + "yaw", location.getYaw());
		conf.set(path + "pitch", location.getPitch());

		conf.set(path + "lobbydelay", lobbyTime);

		Configuration.saveFile(conf, plugin.getConfiguration().getArenasFile());
	}

	/**
	 * Returns the lobby timer
	 * 
	 * @return {@link LobbyTimer}
	 */
	@org.jetbrains.annotations.NotNull
	public LobbyTimer getLobbyTimer() {
		return lobbyTimer;
	}
}
