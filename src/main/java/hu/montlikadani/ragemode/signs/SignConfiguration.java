package hu.montlikadani.ragemode.signs;

import java.io.File;
import java.io.IOException;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import hu.montlikadani.ragemode.RageMode;
import hu.montlikadani.ragemode.config.Configuration;

public class SignConfiguration {

	private static boolean inited = false;
	private static File yamlSignsFile;
	private static FileConfiguration signConfig;

	public static FileConfiguration initSignConfiguration() {
		if (inited) {
			return signConfig;
		}

		File file = new File(RageMode.getInstance().getFolder(), "signs.yml");
		FileConfiguration config;
		yamlSignsFile = file;

		if (!file.exists()) {
			if (!file.getParentFile().exists())
				file.getParentFile().mkdirs();

			try {
				file.createNewFile();
			} catch (IOException e) {
				e.printStackTrace();
			}

			config = new YamlConfiguration();
			config.createSection("signs");

			Configuration.saveFile(config, file);
		} else
			config = YamlConfiguration.loadConfiguration(file);

		signConfig = config;
		Configuration.saveFile(config, file);
		inited = true;

		return signConfig;
	}

	public static File getFile() {
		return yamlSignsFile;
	}

	public static FileConfiguration getConf() {
		return signConfig;
	}
}
