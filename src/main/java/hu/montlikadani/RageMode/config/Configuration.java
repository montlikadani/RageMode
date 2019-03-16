package hu.montlikadani.ragemode.config;

import java.io.File;
import java.util.logging.Level;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import hu.montlikadani.ragemode.RageMode;

public class Configuration {

	private RageMode plugin;
	private FileConfiguration config, arenas;
	private File config_file, arenas_file;

	public Configuration(RageMode plugin) {
		this.plugin = plugin;

		config_file = new File(plugin.getFolder(), "config.yml");
		arenas_file = new File(plugin.getFolder(), "arenas.yml");
	}

	public void loadConfig() {
		try {
			if (config_file.exists()) {
				config = YamlConfiguration.loadConfiguration(config_file);
				config.load(config_file);
			} else {
				plugin.saveResource("config.yml", false);
				config = YamlConfiguration.loadConfiguration(config_file);
				RageMode.logConsole(Level.INFO, "[RageMode] The 'config.yml' file successfully created!");
			}
			if (arenas_file.exists()) {
				arenas = YamlConfiguration.loadConfiguration(arenas_file);
				arenas.load(arenas_file);
				arenas.save(arenas_file);
			} else {
				arenas_file.createNewFile();
				arenas = YamlConfiguration.loadConfiguration(arenas_file);
				RageMode.logConsole(Level.INFO, "[RageMode] The 'arenas.yml' file successfully created!");
			}
		} catch (Throwable e) {
			e.printStackTrace();
			plugin.throwMsg();
		}
	}

	public FileConfiguration getCfg() {
		return config;
	}

	public FileConfiguration getArenasCfg() {
		return arenas;
	}

	public File getCfgFile() {
		return config_file;
	}

	public File getArenasFile() {
		return arenas_file;
	}
}