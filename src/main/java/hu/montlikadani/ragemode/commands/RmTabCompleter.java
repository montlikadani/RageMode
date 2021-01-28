package hu.montlikadani.ragemode.commands;

import static hu.montlikadani.ragemode.utils.Misc.hasPerm;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.util.StringUtil;

import hu.montlikadani.ragemode.RageMode;
import hu.montlikadani.ragemode.area.GameAreaManager;
import hu.montlikadani.ragemode.database.DBType;
import hu.montlikadani.ragemode.gameUtils.GameType;

public class RmTabCompleter implements TabCompleter {

	@Override
	public List<String> onTabComplete(CommandSender sender, Command cmd, String label, String[] args) {
		if (cmd.getName().equalsIgnoreCase("ragemode") || cmd.getName().equalsIgnoreCase("rm")) {
			List<String> completionList = new ArrayList<>(), cmds = new ArrayList<>();
			String partOfCommand = null;

			if (args.length < 2) {
				if (hasPerm(sender, "ragemode.joinrandom")) {
					cmds.add("joinrandom");
				}

				getDefaultCmds(sender).forEach(cmds::add);
				getAdminCmds(sender).forEach(cmds::add);

				if (hasPerm(sender, "ragemode.admin.setgametime")) {
					cmds.add("gametime");
				}

				if (hasPerm(sender, "ragemode.admin.setlobbydelay")) {
					cmds.add("lobbydelay");
				}

				partOfCommand = args[0];
			} else if (args.length < 3) {
				if (args[0].equalsIgnoreCase("holostats")) {
					Arrays.asList("add", "remove", "tp").forEach(cmds::add);
				} else if (args[0].equalsIgnoreCase("points")) {
					Arrays.asList("set", "add", "take").forEach(cmds::add);
				} else if (args[0].equalsIgnoreCase("signupdate")) {
					RageMode.getInstance().getGames().forEach(game -> cmds.add(game.getName()));
					cmds.add("all");
				} else if (args[0].equalsIgnoreCase("convertdatabase")) {
					for (DBType type : DBType.values()) {
						cmds.add(type.name().toLowerCase());
					}
				} else if (args[0].equalsIgnoreCase("area")) {
					Arrays.asList("add", "remove", "info", "list").forEach(cmds::add);
				} else {
					for (String name : Arrays.asList("gametime", "lobbydelay", "removegame",
							"forcestart", "setlobby", "addspawn", "addzombiespawn", "join", "stop", "stopgame",
							"togglegame", "spectate", "removespawn", "removezombiespawn", "listplayers", "kick",
							"maxplayers", "minplayers", "setgametype", "setup")) {
						if (args[0].equalsIgnoreCase(name) && !RageMode.getInstance().getGames().isEmpty()) {
							RageMode.getInstance().getGames().forEach(game -> cmds.add(game.getName()));
							break;
						}
					}
				}

				partOfCommand = args[1];
			} else if (args.length < 4) {
				if (args[0].equalsIgnoreCase("removespawn") || args[0].equalsIgnoreCase("removezombiespawn")) {
					cmds.add("all");
				} else if (args[0].equalsIgnoreCase("area")) {
					if (args[1].equalsIgnoreCase("add")) {
						RageMode.getInstance().getGames().forEach(game -> cmds.add(game.getName()));
					} else if (args[1].equalsIgnoreCase("remove")) {
						GameAreaManager.getGameAreas().keySet().forEach(cmds::add);
					}
				} else if (args[0].equalsIgnoreCase("setgametype")) {
					for (GameType t : GameType.values()) {
						cmds.add(t.toString().toLowerCase());
					}
				}

				partOfCommand = args[2];
			}

			// Completes the player names
			if (cmds.isEmpty() || partOfCommand == null) {
				return null;
			}

			StringUtil.copyPartialMatches(partOfCommand, cmds, completionList);
			Collections.sort(completionList);
			return completionList;
		}

		return null;
	}

	private List<String> getDefaultCmds(CommandSender sender) {
		List<String> c = new ArrayList<>();
		for (String cmds : Arrays.asList("join", "leave", "listgames", "stats", "spectate", "listplayers")) {
			if (hasPerm(sender, "ragemode.help.playercommands") || hasPerm(sender, "ragemode." + cmds)) {
				c.add(cmds);
			}
		}

		return c;
	}

	private List<String> getAdminCmds(CommandSender sender) {
		List<String> cmds = new ArrayList<>();
		for (String cmd : Arrays.asList("addgame", "addspawn", "addzombiespawn", "setlobby", "reload", "holostats",
				"removegame", "resetplayerstats", "forcestart", "kick", "stopgame", "signupdate", "togglegame",
				"points", "givesaveditems", "removespawn", "latestart", "maxplayers", "minplayers", "convertdatabase",
				"area", "setgametype", "removezombiespawn", "setup")) {
			if (hasPerm(sender, "ragemode.admin." + cmd)) {
				cmds.add(cmd);
			}
		}

		return cmds;
	}
}
