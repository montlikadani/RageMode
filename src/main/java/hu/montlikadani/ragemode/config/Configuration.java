package hu.montlikadani.ragemode.config;

import java.io.File;
import java.io.IOException;
import java.util.logging.Level;

import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import hu.montlikadani.ragemode.RageMode;

public class Configuration {

	private RageMode plugin;
	private CommentedConfig config;
	private FileConfiguration arenas, rewards, datas, items, gameAreas, holosConfig;
	private File config_file, arenas_file, rewards_file, datas_file, items_file, areas_File, holosFile;

	public Configuration(RageMode plugin) {
		this.plugin = plugin;
	}

	private void loadFiles() {
		File folder = plugin.getFolder();
		if (config_file == null) {
			config_file = new File(folder, "config.yml");
		}

		if (arenas_file == null) {
			arenas_file = new File(folder, "arenas.yml");
		}

		if (rewards_file == null) {
			rewards_file = new File(folder, "rewards.yml");
		}

		if (datas_file == null) {
			datas_file = new File(folder, "datas.yml");
		}

		if (items_file == null) {
			items_file = new File(folder, "items.yml");
		}

		if (areas_File == null) {
			areas_File = new File(folder, "gameareas.yml");
		}

		if (holosFile == null) {
			holosFile = new File(folder, "holos.yml");
		}
	}

	public void loadConfig() {
		loadFiles();

		if (!config_file.exists()) {
			plugin.saveResource("config.yml", false);
		}

		(config = new CommentedConfig(config_file)).load();

		ConfigValues.loadValues(config);

		if (arenas_file.exists()) {
			arenas = YamlConfiguration.loadConfiguration(arenas_file);
			loadFile(arenas, arenas_file);
			saveFile(arenas, arenas_file);
		} else {
			arenas = createFile(arenas_file, "arenas.yml", true);
		}

		if (ConfigValues.isRewardEnabled()) {
			if (rewards_file.exists()) {
				rewards = YamlConfiguration.loadConfiguration(rewards_file);
				loadFile(rewards, rewards_file);
			} else {
				rewards = createFile(rewards_file, "rewards.yml", false);
			}
		}

		if (ConfigValues.isSavePlayerData()) {
			if (datas_file.exists()) {
				datas = YamlConfiguration.loadConfiguration(datas_file);
				loadFile(datas, datas_file);
				saveFile(datas, datas_file);
			} else {
				datas = createFile(datas_file, "datas.yml", true);
			}
		}

		if (items_file.exists()) {
			items = YamlConfiguration.loadConfiguration(items_file);
			loadFile(items, items_file);
		} else {
			items = createFile(items_file, "items.yml", false);
		}

		if (areas_File.exists()) {
			gameAreas = YamlConfiguration.loadConfiguration(areas_File);
			loadFile(gameAreas, areas_File);
		} else {
			gameAreas = createFile(areas_File, "gameareas.yml", true);
		}

		if (holosFile.exists()) {
			holosConfig = YamlConfiguration.loadConfiguration(holosFile);
			holosConfig.createSection("data");
			loadFile(holosConfig, holosFile);
			saveFile(holosConfig, holosFile);
		} else {
			holosConfig = createFile(holosFile, "holos.yml", true);
		}
	}

	FileConfiguration createFile(File file, String name, boolean newFile) {
		if (newFile) {
			try {
				file.createNewFile();
			} catch (IOException e) {
				e.printStackTrace();
			}
		} else {
			plugin.saveResource(name, false);
		}

		plugin.getLogger().log(Level.INFO, "{0} file created!", name);
		return YamlConfiguration.loadConfiguration(file);
	}

	public CommentedConfig getCfg() {
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

	public FileConfiguration getItemsCfg() {
		return items;
	}

	public FileConfiguration getAreasCfg() {
		return gameAreas;
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

	public File getItemsFile() {
		return items_file;
	}

	public File getAreasFile() {
		return areas_File;
	}

	public FileConfiguration getHolosConfig() {
		return holosConfig;
	}

	public File getHolosFile() {
		return holosFile;
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