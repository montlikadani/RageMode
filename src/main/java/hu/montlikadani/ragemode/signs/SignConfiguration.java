package hu.montlikadani.ragemode.signs;

import java.io.File;
import java.io.IOException;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import hu.montlikadani.ragemode.RageMode;

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

		if (!file.exists()) {
			file.getParentFile().mkdirs();

			try {
				file.createNewFile();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		signConfig = YamlConfiguration.loadConfiguration(file);
		signsFile = file;
		return signConfig;
	}
}
