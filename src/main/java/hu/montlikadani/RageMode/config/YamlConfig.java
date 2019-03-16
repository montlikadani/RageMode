package hu.montlikadani.ragemode.config;

import java.util.List;

import org.bukkit.configuration.file.YamlConfiguration;

public class YamlConfig {

	private YamlConfiguration c;

	public YamlConfig(YamlConfiguration c) {
		this.c = c;
	}

	public YamlConfiguration getYC() {
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