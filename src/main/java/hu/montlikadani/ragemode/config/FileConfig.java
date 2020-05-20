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

	public String get(String path, String def) {
		return c.getString(path, def);
	}

	@SuppressWarnings("unchecked")
	public List<String> get(String path, List<String> list) {
		return (List<String>) c.getList(path, list);
	}

	@SuppressWarnings("unchecked")
	public List<Integer> get(String path, ArrayList<Integer> list) {
		return (List<Integer>) c.getList(path, list);
	}

	public int get(String path, int def) {
		return c.getInt(path, def);
	}

	public boolean get(String path, boolean def) {
		return c.getBoolean(path, def);
	}

	public String getOriginal(String path) {
		return c.getString(path);
	}

	public int getOriginalInt(String path) {
		return c.getInt(path);
	}

	public boolean getOriginalBoo(String path) {
		return c.getBoolean(path);
	}
}
