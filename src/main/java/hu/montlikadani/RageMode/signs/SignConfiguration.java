package hu.montlikadani.ragemode.signs;

import java.io.File;
import java.io.IOException;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import hu.montlikadani.ragemode.Debug;
import hu.montlikadani.ragemode.RageMode;
import hu.montlikadani.ragemode.config.Configuration;

public class SignConfiguration {

	private static boolean inited = false;
	private static File yamlSignsFile;
	private static FileConfiguration signConfig;

	public static void initSignConfiguration() {
		if (inited)
			return;

		inited = true;

		File file = new File(RageMode.getInstance().getFolder(), "signs.yml");
		FileConfiguration config = null;
		yamlSignsFile = file;

		if (!file.exists()) {
			if (!file.getParentFile().exists())
				file.getParentFile().mkdirs();

			try {
				file.createNewFile();
			} catch (IOException e1) {
				e1.printStackTrace();
				Debug.throwMsg();
			}

			config = new YamlConfiguration();
			config.createSection("signs");

			Configuration.saveFile(config, file);
		} else
			config = YamlConfiguration.loadConfiguration(file);

		signConfig = config;
		Configuration.saveFile(config, file);
	}

	public static File getFile() {
		return yamlSignsFile;
	}

	public static FileConfiguration getConf() {
		return signConfig;
	}
}
