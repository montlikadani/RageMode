package hu.montlikadani.ragemode.gameLogic.Reward;

import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import hu.montlikadani.ragemode.RageMode;
import hu.montlikadani.ragemode.Utils;
import hu.montlikadani.ragemode.gameLogic.PlayerList;

public class Reward {

	private String game;
	//private ConfigurationSection rewards;
	private YamlConfiguration conf;
	private boolean enable;
	private String type;

	public Reward(String type, String game) {
		this.game = game;
		this.type = type;

		conf = RageMode.getInstance().getConfiguration().getRewardsCfg();
		enable = RageMode.getInstance().getConfiguration().getCfg().getBoolean("rewards.enable");
		//rewards = conf.getConfigurationSection("rewards");
	}

	public void executeRewards(Player player, boolean winner) {
		if (!enable/* || rewards != null*/)
			return;

		RageMode plugin = RageMode.getInstance();

		//for (String reward : rewards.getKeys(false)) {
			//ConfigurationSection section = rewards.getConfigurationSection(reward);

			switch (type) {
			case "end-game":
				List<String> cmds = null;
				List<String> msgs = null;
				double cash = 0D;

				if (winner) {
					cmds = conf.getStringList("rewards.end-game.winner.commands");
					msgs = conf.getStringList("rewards.end-game.winner.messages");
					cash = conf.getDouble("rewards.end-game.winner.cash");

					if (cmds != null && !cmds.isEmpty()) {
						for (String path : cmds) {
							String[] arg = path.split(": ");
							String cmd = arg[1];
							cmd = replacePlaceholders(cmd, player, conf, true);

							if (arg[0].equals("console"))
								Bukkit.dispatchCommand(Bukkit.getServer().getConsoleSender(), cmd);
							else if (arg[0].equals("player"))
								player.performCommand(cmd);
						}
					}

					if (msgs != null && !msgs.isEmpty()) {
						for (String path : msgs) {
							path = replacePlaceholders(path, player, conf, true);

							player.sendMessage(path);
						}
					}

					if (cash > 0D && plugin.isVaultEnabled())
						plugin.getEconomy().depositPlayer(player, cash);

				} else {
					cmds = conf.getStringList("rewards.end-game.players.commands");
					msgs = conf.getStringList("rewards.end-game.players.messages");
					cash = conf.getDouble("rewards.end-game.players.cash");

					if (cmds != null && !cmds.isEmpty()) {
						for (String path : cmds) {
							String[] arg = path.split(": ");
							String cmd = arg[1];
							cmd = replacePlaceholders(cmd, player, conf, false);

							if (arg[0].equals("console"))
								Bukkit.dispatchCommand(Bukkit.getServer().getConsoleSender(), cmd);
							else if (arg[0].equals("player"))
								player.performCommand(cmd);
						}
					}

					if (msgs != null && !msgs.isEmpty()) {
						for (String path : msgs) {
							path = replacePlaceholders(path, player, conf, false);

							player.sendMessage(path);
						}
					}

					if (cash > 0D && plugin.isVaultEnabled())
						plugin.getEconomy().depositPlayer(player, cash);
				}
				break;
				default:
					break;
			}
		//}
	}

	private String replacePlaceholders(String path, Player p, ConfigurationSection section, boolean winner) {
		double cash = winner ? section.getDouble("end-game.winner.cash") : section.getDouble("end-game.players.cash");

		path = path.replace("%game%", game);
		path = path.replace("%player%", p.getName());
		path = path.replace("%online-ingame-players%", Integer.toString(PlayerList.getPlayersInGame(game).length));
		path = path.replace("%reward%", cash > 0D ? Double.toString(cash) : "");
		path = Utils.setPlaceholders(path, p);
		return RageMode.getLang().colors(path);
	}

	public boolean isEnabled() {
		return enable;
	}

	public String getType() {
		return type;
	}
}
