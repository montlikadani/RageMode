package hu.montlikadani.ragemode.config;

import java.io.File;

import org.bukkit.configuration.file.YamlConfiguration;

import hu.montlikadani.ragemode.RageMode;

public class Configuration {

	private RageMode plugin;
	private YamlConfiguration config, arenas, rewards, datas;
	private File config_file, arenas_file, rewards_file, datas_file;

	public Configuration(RageMode plugin) {
		this.plugin = plugin;

		config_file = new File(plugin.getFolder(), "config.yml");
		arenas_file = new File(plugin.getFolder(), "arenas.yml");
		rewards_file = new File(plugin.getFolder(), "rewards.yml");
		datas_file = new File(plugin.getFolder(), "datas.yml");
	}

	public void loadConfig() {
		try {
			if (config_file.exists()) {
				config = YamlConfiguration.loadConfiguration(config_file);
				config.load(config_file);
			} else {
				plugin.saveResource("config.yml", false);
				config = YamlConfiguration.loadConfiguration(config_file);
				RageMode.logConsole("[RageMode] The 'config.yml' file successfully created!");
			}
			if (arenas_file.exists()) {
				arenas = YamlConfiguration.loadConfiguration(arenas_file);
				arenas.load(arenas_file);
				arenas.save(arenas_file);
			} else {
				arenas_file.createNewFile();
				arenas = YamlConfiguration.loadConfiguration(arenas_file);
				RageMode.logConsole("[RageMode] The 'arenas.yml' file successfully created!");
			}

			if (config.getBoolean("rewards.enable")) {
				if (rewards_file.exists()) {
					rewards = YamlConfiguration.loadConfiguration(rewards_file);
					rewards.load(rewards_file);
				} else {
					plugin.saveResource("rewards.yml", false);
					rewards = YamlConfiguration.loadConfiguration(rewards_file);
					RageMode.logConsole("[RageMode] The 'rewards.yml' file successfully created!");
				}
			}
			if (config.getBoolean("save-player-datas-to-file")) {
				if (datas_file.exists()) {
					datas = YamlConfiguration.loadConfiguration(datas_file);
					datas.load(datas_file);
					datas.save(datas_file);
				} else {
					datas_file.createNewFile();
					datas = YamlConfiguration.loadConfiguration(datas_file);
					RageMode.logConsole("[RageMode] The 'datas.yml' file successfully created!");
				}
			}
		} catch (Throwable e) {
			e.printStackTrace();
			plugin.throwMsg();
		}
	}

	public YamlConfiguration getCfg() {
		return config;
	}

	public YamlConfiguration getArenasCfg() {
		return arenas;
	}

	public YamlConfiguration getRewardsCfg() {
		return rewards;
	}

	public YamlConfiguration getDatasCfg() {
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
}