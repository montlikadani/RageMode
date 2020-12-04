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

import hu.montlikadani.ragemode.area.GameAreaManager;
import hu.montlikadani.ragemode.database.DBType;
import hu.montlikadani.ragemode.gameUtils.GameType;
import hu.montlikadani.ragemode.gameUtils.GetGames;

public class RmTabCompleter implements TabCompleter {

	@Override
	public List<String> onTabComplete(CommandSender sender, Command cmd, String label, String[] args) {
		if (cmd.getName().equalsIgnoreCase("ragemode") || cmd.getName().equalsIgnoreCase("rm")) {
			List<String> completionList = new ArrayList<>(), cmds = new ArrayList<>();
			String partOfCommand = null;

			if (args.length < 2) {
				if (args[0].equalsIgnoreCase("joinrandom") && hasPerm(sender, "ragemode.joinrandom")) {
					cmds.add("joinrandom");
				}

				getDefaultCmds(sender).forEach(cmds::add);
				getAdminCmds(sender).forEach(cmds::add);
				getSomeCmds(sender).forEach(cmds::add);

				partOfCommand = args[0];
			} else if (args.length < 3) {
				if (args[0].equalsIgnoreCase("holostats")) {
					Arrays.asList("add", "remove", "tp").forEach(cmds::add);
				} else if (args[0].equalsIgnoreCase("points")) {
					Arrays.asList("set", "add", "take").forEach(cmds::add);
				} else if (args[0].equalsIgnoreCase("signupdate")) {
					GetGames.getGames().forEach(cmds::add);
					cmds.add("all");
				} else if (args[0].equalsIgnoreCase("convertdatabase")) {
					for (DBType type : DBType.values()) {
						cmds.add(type.name().toLowerCase());
					}
				} else if (args[0].equalsIgnoreCase("area")) {
					Arrays.asList("add", "remove", "info", "list").forEach(cmds::add);
				} else {
					for (String game : Arrays.asList("actionbar", "bossbar", "globalmessages", "gametime", "lobbydelay",
							"removegame", "forcestart", "setlobby", "addspawn", "addzombiespawn", "join", "stop",
							"stopgame", "togglegame", "spectate", "removespawn", "removezombiespawn", "listplayers",
							"kick", "maxplayers", "minplayers", "setgametype")) {
						if (args[0].equalsIgnoreCase(game) && !GetGames.getGames().isEmpty()) {
							GetGames.getGames().forEach(cmds::add);
							break;
						}
					}
				}

				partOfCommand = args[1];
			} else if (args.length < 4) {
				for (String c : Arrays.asList("actionbar", "bossbar", "globalmessages")) {
					if (args[0].equalsIgnoreCase(c)) {
						Arrays.asList("true", "false").forEach(cmds::add);
						partOfCommand = args[2];
						break;
					}
				}

				if (args[0].equalsIgnoreCase("removespawn") || args[0].equalsIgnoreCase("removezombiespawn")) {
					cmds.add("all");
					partOfCommand = args[2];
				} else if (args[0].equalsIgnoreCase("area")) {
					if (args[1].equalsIgnoreCase("add")) {
						GetGames.getGames().forEach(cmds::add);
					} else if (args[1].equalsIgnoreCase("remove")) {
						GameAreaManager.getGameAreas().keySet().forEach(cmds::add);
					}

					partOfCommand = args[2];
				} else if (args[0].equalsIgnoreCase("setgametype")) {
					Arrays.asList(GameType.values()).forEach(t -> cmds.add(t.toString().toLowerCase()));
					partOfCommand = args[2];
				}
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
				"area", "setgametype", "removezombiespawn")) {
			if (hasPerm(sender, "ragemode.admin." + cmd)) {
				cmds.add(cmd);
			}
		}

		return cmds;
	}

	private List<String> getSomeCmds(CommandSender sender) {
		List<String> c = new ArrayList<>();
		for (String cmds : Arrays.asList("actionbar", "bossbar", "globalmessages", "gametime", "lobbydelay")) {
			if (hasPerm(sender, "ragemode.admin.set" + cmds)) {
				c.add(cmds);
			}
		}

		return c;
	}
}
