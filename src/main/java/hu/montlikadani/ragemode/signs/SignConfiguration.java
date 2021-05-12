package hu.montlikadani.ragemode.signs;

import java.io.File;
import java.io.IOException;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import hu.montlikadani.ragemode.RageMode;
import hu.montlikadani.ragemode.config.Configuration;

public class SignConfiguration {

	private static File signsFile;
	private static FileConfiguration signConfig;

	public static File getSignFile() {
		return signsFile;
	}

	public static FileConfiguration getSignConfig() {
		if (signConfig != null) {
			return signConfig;
		}

		File file = new File(org.bukkit.plugin.java.JavaPlugin.getPlugin(RageMode.class).getFolder(), "signs.yml");
		FileConfiguration config;
		signsFile = file;

		if (!file.exists()) {
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

		return signConfig;
	}
}
