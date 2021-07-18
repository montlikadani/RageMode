package hu.montlikadani.ragemode.commands;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import hu.montlikadani.ragemode.RageMode;
import hu.montlikadani.ragemode.area.GameAreaManager;
import hu.montlikadani.ragemode.database.DBType;
import hu.montlikadani.ragemode.gameLogic.base.BaseGame;
import hu.montlikadani.ragemode.gameUtils.GameType;

public final class RmTabCompleter implements TabCompleter {

	private final RageMode plugin;

	public RmTabCompleter(RageMode plugin) {
		this.plugin = plugin;
	}

	@Override
	public List<String> onTabComplete(CommandSender sender, Command cmd, String label, String[] args) {
		List<String> cmds = new ArrayList<>();

		if (args.length < 2) {
			if (hasPerm(sender, "ragemode.joinrandom")) {
				cmds.add("joinrandom");
			}

			cmds.addAll(getDefaultCmds(sender));
			cmds.addAll(getAdminCmds(sender));

			if (hasPerm(sender, "ragemode.admin.setgametime")) {
				cmds.add("gametime");
			}

			if (hasPerm(sender, "ragemode.admin.setlobbydelay")) {
				cmds.add("lobbydelay");
			}
		} else if (args.length < 3) {
			if (args[0].equalsIgnoreCase("holostats")) {
				for (String c : new String[] { "add", "remove", "tp" }) {
					cmds.add(c);
				}
			} else if (args[0].equalsIgnoreCase("points")) {
				for (String c : new String[] { "set", "add", "take" }) {
					cmds.add(c);
				}
			} else if (args[0].equalsIgnoreCase("signupdate")) {
				for (BaseGame game : plugin.getGames()) {
					cmds.add(game.getName());
				}

				cmds.add("all");
			} else if (args[0].equalsIgnoreCase("convertdatabase")) {
				for (DBType type : DBType.values()) {
					cmds.add(type.name().toLowerCase());
				}
			} else if (args[0].equalsIgnoreCase("area")) {
				for (String c : new String[] { "add", "remove", "info", "list" }) {
					cmds.add(c);
				}
			} else if (!plugin.getGames().isEmpty()) {
				for (String name : new String[] { "gametime", "lobbydelay", "removegame", "forcestart", "setlobby",
						"spawn", "join", "stop", "stopgame", "togglegame", "spectate", "listplayers", "maxplayers",
						"minplayers", "setgametype", "setup" }) {
					if (args[0].equalsIgnoreCase(name)) {
						for (BaseGame game : plugin.getGames()) {
							cmds.add(game.getName());
						}

						break;
					}
				}
			}
		} else if (args.length < 4) {
			if (args[0].equalsIgnoreCase("area")) {
				if (args[1].equalsIgnoreCase("add")) {
					for (BaseGame game : plugin.getGames()) {
						cmds.add(game.getName());
					}
				} else if (args[1].equalsIgnoreCase("remove")) {
					cmds.addAll(GameAreaManager.getGameAreas().keySet());
				}
			} else if (args[0].equalsIgnoreCase("setgametype")) {
				for (GameType t : GameType.values()) {
					cmds.add(t.toString().toLowerCase());
				}
			}
		}

		return cmds.isEmpty() ? null : cmds;
	}

	private List<String> getDefaultCmds(CommandSender sender) {
		List<String> c = new ArrayList<>();

		boolean havePlayerCmdsPerm = !(sender instanceof Player)
				|| sender.hasPermission("ragemode.help.playercommands");

		for (String cmds : new String[] { "join", "leave", "listgames", "stats", "spectate", "listplayers" }) {
			if (havePlayerCmdsPerm || sender.hasPermission("ragemode." + cmds)) {
				c.add(cmds);
			}
		}

		return c;
	}

	private List<String> getAdminCmds(CommandSender sender) {
		List<String> cmds = new ArrayList<>();

		boolean isPlayer = sender instanceof Player;

		for (String cmd : new String[] { "addgame", "spawn", "setlobby", "reload", "holostats", "removegame",
				"resetplayerstats", "forcestart", "kick", "stopgame", "signupdate", "togglegame", "points",
				"givesaveditems", "latestart", "maxplayers", "minplayers", "convertdatabase", "area", "setgametype",
				"setup" }) {
			if (!isPlayer || sender.hasPermission("ragemode.admin." + cmd)) {
				cmds.add(cmd);
			}
		}

		return cmds;
	}

	private boolean hasPerm(CommandSender sender, String perm) {
		return !(sender instanceof Player) || sender.hasPermission(perm);
	}
}
