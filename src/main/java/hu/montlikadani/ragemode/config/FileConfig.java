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

	public String getL(String path, String boo) {
		c.addDefault(path, boo);
		c.set(path, c.get(path));
		return c.getString(path);
	}

	public List<String> getL(String path, List<String> list) {
		c.addDefault(path, list);
		c.set(path, c.get(path));
		return c.getStringList(path);
	}

	public String get(String path, String def) {
		return c.getString(path, def);
	}

	public int get(String path, int def) {
		return c.getInt(path, def);
	}

	public boolean get(String path, boolean def) {
		return c.getBoolean(path, def);
	}
}
