package hu.montlikadani.ragemode.config;

import java.util.List;

import org.bukkit.configuration.file.FileConfiguration;

public class FileConfig {

	private FileConfiguration c;

	public FileConfig(FileConfiguration c) {
		this.c = c;
	}

	public FileConfiguration getFC() {
		return c;
	}

	public String get(String path, String boo) {
		c.addDefault(path, boo);
		c.set(path, c.get(path));
		return c.getString(path);
	}

	public List<String> get(String path, List<String> list) {
		c.addDefault(path, list);
		c.set(path, c.get(path));
		return c.getStringList(path);
	}
}
