package hu.montlikadani.ragemode.config;

import java.io.File;
import java.io.IOException;
import java.util.logging.Level;

import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import hu.montlikadani.ragemode.Debug;
import hu.montlikadani.ragemode.RageMode;

public class Configuration {

	private RageMode plugin;
	private FileConfiguration config, arenas, rewards, datas;
	private File config_file, arenas_file, rewards_file, datas_file;

	private double configVersion = 1.4;

	private ConfigValues cv;

	public Configuration(RageMode plugin) {
		this.plugin = plugin;
	}

	private void loadFiles() {
		if (config_file == null) {
			config_file = new File(plugin.getFolder(), "config.yml");
		}

		if (arenas_file == null) {
			arenas_file = new File(plugin.getFolder(), "arenas.yml");
		}

		if (rewards_file == null) {
			rewards_file = new File(plugin.getFolder(), "rewards.yml");
		}

		if (datas_file == null) {
			datas_file = new File(plugin.getFolder(), "datas.yml");
		}

		if (cv == null) {
			cv = new ConfigValues();
		}
	}

	public void loadConfig() {
		String msg = "";

		loadFiles();

		try {
			if (config_file.exists()) {
				config = YamlConfiguration.loadConfiguration(config_file);
			} else {
				plugin.saveResource("config.yml", false);
				config = YamlConfiguration.loadConfiguration(config_file);
				msg += "'config.yml' file created!";
			}

			cv.loadValues(new FileConfig(config));

			if (!config.isSet("config-version") || !config.get("config-version").equals(configVersion)) {
				Debug.logConsole(Level.WARNING, "Found outdated configuration (config.yml)! (Your version: "
						+ config.getDouble("config-version") + " | Newest version: " + configVersion + ")");
			}

			if (arenas_file.exists()) {
				arenas = YamlConfiguration.loadConfiguration(arenas_file);
				loadFile(arenas, arenas_file);
				saveFile(arenas, arenas_file);
			} else {
				arenas_file.createNewFile();
				arenas = YamlConfiguration.loadConfiguration(arenas_file);
				msg += "'arenas.yml' file created!";
			}

			if (cv.isRewardEnabled()) {
				if (rewards_file.exists()) {
					rewards = YamlConfiguration.loadConfiguration(rewards_file);
					loadFile(rewards, rewards_file);
					rewards.load(rewards_file);
				} else {
					plugin.saveResource("rewards.yml", false);
					rewards = YamlConfiguration.loadConfiguration(rewards_file);
					msg += "'rewards.yml' file created!";
				}
			}

			if (cv.isSavePlayerData()) {
				if (datas_file.exists()) {
					datas = YamlConfiguration.loadConfiguration(datas_file);
					loadFile(datas, datas_file);
					saveFile(datas, datas_file);
				} else {
					datas_file.createNewFile();
					datas = YamlConfiguration.loadConfiguration(datas_file);
					msg += "'datas.yml' file created!";
				}
			}

			if (!msg.isEmpty()) {
				Debug.logConsole(msg);
			}
		} catch (IOException | InvalidConfigurationException e) {
			e.printStackTrace();
			Debug.throwMsg();
		}
	}

	public ConfigValues getCV() {
		return cv;
	}

	public FileConfiguration getCfg() {
		return config;
	}

	public FileConfiguration getArenasCfg() {
		return arenas;
	}

	public FileConfiguration getRewardsCfg() {
		return rewards;
	}

	public FileConfiguration getDatasCfg() {
		return datas;
	}

	public File getCfgFile() {
		return config_file;
	}

	public File getArenasFile() {
		return arenas_file;
	}

	public File getRewardsFile() {
		return rewards_file;
	}

	public File getDatasFile() {
		return datas_file;
	}

	public double getConfigVersion() {
		return configVersion;
	}

	public static void saveFile(FileConfiguration c, File f) {
		try {
			c.save(f);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static void loadFile(FileConfiguration c, File f) {
		try {
			c.load(f);
		} catch (IOException | InvalidConfigurationException e) {
			e.printStackTrace();
		}
	}
}