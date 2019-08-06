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

	private double configVersion = 1.1;

	public Configuration(RageMode plugin) {
		this.plugin = plugin;

		config_file = new File(plugin.getFolder(), "config.yml");
		arenas_file = new File(plugin.getFolder(), "arenas.yml");
		rewards_file = new File(plugin.getFolder(), "rewards.yml");
		datas_file = new File(plugin.getFolder(), "datas.yml");
	}

	public void loadConfig() {
		String msg = "";

		try {
			if (config_file.exists()) {
				config = YamlConfiguration.loadConfiguration(config_file);
				config.load(config_file);

				if (!config.isSet("config-version") || !config.get("config-version").equals(configVersion)) {
					Debug.logConsole(Level.WARNING, "Found outdated configuration (config.yml)! (Your version: "
							+ config.getDouble("config-version") + " | Newest version: " + configVersion + ")");
				}
			} else {
				plugin.saveResource("config.yml", false);
				config = YamlConfiguration.loadConfiguration(config_file);
				msg += "The 'config.yml' file successfully created!";
			}

			if (arenas_file.exists()) {
				arenas = YamlConfiguration.loadConfiguration(arenas_file);
				arenas.load(arenas_file);
				saveFile(arenas, arenas_file);
			} else {
				arenas_file.createNewFile();
				arenas = YamlConfiguration.loadConfiguration(arenas_file);
				msg += "The 'arenas.yml' file successfully created!";
			}

			if (config.getBoolean("rewards.enable")) {
				if (rewards_file.exists()) {
					rewards = YamlConfiguration.loadConfiguration(rewards_file);
					rewards.load(rewards_file);
				} else {
					plugin.saveResource("rewards.yml", false);
					rewards = YamlConfiguration.loadConfiguration(rewards_file);
					msg += "The 'rewards.yml' file successfully created!";
				}
			}

			if (config.getBoolean("save-player-datas-to-file")) {
				if (datas_file.exists()) {
					datas = YamlConfiguration.loadConfiguration(datas_file);
					datas.load(datas_file);
					saveFile(datas, datas_file);
				} else {
					datas_file.createNewFile();
					datas = YamlConfiguration.loadConfiguration(datas_file);
					msg += "The 'datas.yml' file successfully created!";
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

	public static void saveFile(FileConfiguration conf, File file) {
		try {
			conf.save(file);
		} catch (IOException e) {
			e.printStackTrace();
			Debug.throwMsg();
		}
	}
}