package hu.montlikadani.ragemode.items.shop;

public class ShopItemCommands {

	private String configPath;
	private String command;
	private ShopType type;

	public ShopItemCommands(String configPath, String command, ShopType type) {
		this.configPath = configPath;
		this.command = command;
		this.type = type;
	}

	public String getConfigPath() {
		return configPath;
	}

	public String getCommand() {
		return command;
	}

	public ShopType getType() {
		return type;
	}
}
