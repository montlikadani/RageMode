package hu.montlikadani.ragemode.config;

import java.util.ArrayList;
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

	public String get(String path, String boo) {
		return get(path, boo, false);
	}

	public List<String> get(String path, List<String> list) {
		return get(path, list, false);
	}

	public List<Integer> get(String path, ArrayList<Integer> list) {
		return get(path, list, false);
	}

	public int get(String path, int def) {
		return get(path, def, false);
	}

	public boolean get(String path, boolean def) {
		return get(path, def, false);
	}

	public String get(String path, String boo, boolean set) {
		if (set) {
			c.addDefault(path, boo);
			c.set(path, c.get(path));
		}
		return c.getString(path);
	}

	public List<String> get(String path, List<String> list, boolean set) {
		if (set) {
			c.addDefault(path, list);
			c.set(path, c.get(path));
		}
		return c.getStringList(path);
	}

	public List<Integer> get(String path, ArrayList<Integer> list, boolean set) {
		if (set) {
			c.addDefault(path, list);
			c.set(path, c.get(path));
		}
		return c.getIntegerList(path);
	}

	public int get(String path, int def, boolean set) {
		if (set) {
			c.addDefault(path, def);
			c.set(path, c.get(path));
		}
		return c.getInt(path);
	}

	public boolean get(String path, boolean def, boolean set) {
		if (set) {
			c.addDefault(path, def);
			c.set(path, c.get(path));
		}
		return c.getBoolean(path);
	}
}
