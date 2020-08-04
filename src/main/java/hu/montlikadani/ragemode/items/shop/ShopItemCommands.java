package hu.montlikadani.ragemode.items.shop;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ShopItemCommands {

	private String configPath;
	private final List<String> commands = new ArrayList<>();
	private NavigationType navigationType = NavigationType.WITHOUT;

	public ShopItemCommands(String configPath, String command, NavigationType navigationType) {
		this(configPath, Arrays.asList(command == null ? "" : command), navigationType);
	}

	public ShopItemCommands(String configPath, List<String> commands, NavigationType navigationType) {
		this.configPath = configPath;

		if (commands != null) {
			this.commands.addAll(commands);
		}

		if (navigationType != null) {
			this.navigationType = navigationType;
		}
	}

	public String getConfigPath() {
		return configPath;
	}

	public List<String> getCommands() {
		return commands;
	}

	public NavigationType getNavigationType() {
		return navigationType;
	}
}
