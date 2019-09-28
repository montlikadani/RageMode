package hu.montlikadani.ragemode.commands;

import static hu.montlikadani.ragemode.utils.Message.hasPerm;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.util.StringUtil;

import hu.montlikadani.ragemode.gameUtils.GetGames;

public class RmTabCompleter implements TabCompleter {

	@Override
	public List<String> onTabComplete(CommandSender sender, Command cmd, String label, String[] args) {
		if (cmd.getName().equals("ragemode") || cmd.getName().equals("rm")) {
			List<String> completionList = new ArrayList<>();
			List<String> cmds = new ArrayList<>();
			String partOfCommand = "";

			if (args.length < 2) {
				getDefaultCmds(sender).forEach(cmds::add);

				getAdminCmds(sender).forEach(cmds::add);

				getSomeCmds(sender).forEach(cmds::add);

				partOfCommand = args[0];
			} else if (args.length < 3) {
				if (args[0].equalsIgnoreCase("holostats")) {
					Arrays.asList("add", "remove", "tp").forEach(cmds::add);
					partOfCommand = args[1];
				} else if (args[0].equalsIgnoreCase("points")) {
					Arrays.asList("set", "add", "take").forEach(cmds::add);
					partOfCommand = args[1];
				} else {
					for (String game : getGameListCmds()) {
						if (args[0].equalsIgnoreCase(game) && !GetGames.getGames().isEmpty()) {
							GetGames.getGames().forEach(cmds::add);
							partOfCommand = args[1];
						}
					}
				}
			} else if (args.length < 4) {
				for (String val : getValueListCmds()) {
					if (args[0].equalsIgnoreCase(val)) {
						Arrays.asList("true", "false").forEach(cmds::add);
						partOfCommand = args[2];
					}
				}

				if (args[0].equalsIgnoreCase("removespawn")) {
					cmds.add("all");
					partOfCommand = args[2];
				}
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
			if (sender instanceof Player && !hasPerm(sender, "ragemode.help.playercommands")
					|| !hasPerm(sender, "ragemode." + cmds))
				continue;

			c.add(cmds);
		}
		return c;
	}

	private List<String> getAdminCmds(CommandSender sender) {
		List<String> c = new ArrayList<>();
		for (String cmds : Arrays.asList("addgame", "addspawn", "setlobby", "reload", "holostats", "removegame",
				"resetstats", "forcestart", "kick", "stopgame", "signupdate", "togglegame", "points", "givesaveditems",
				"removespawn", "latestart", "maxplayers")) {
			if (sender instanceof Player && !hasPerm(sender, "ragemode.admin." + cmds))
				continue;

			c.add(cmds);
		}
		return c;
	}

	private List<String> getSomeCmds(CommandSender sender) {
		List<String> c = new ArrayList<>();
		for (String cmds : Arrays.asList("actionbar", "bossbar", "globalmessages", "gametime", "lobbydelay")) {
			if (sender instanceof Player && !hasPerm(sender, "ragemode.admin.set" + cmds))
				continue;

			c.add(cmds);
		}
		return c;
	}

	private List<String> getGameListCmds() {
		return Arrays.asList("actionbar", "bossbar", "globalmessages", "gametime", "lobbydelay", "removegame",
				"forcestart", "setlobby", "addspawn", "join", "leave", "signupdate", "stop", "stopgame", "togglegame",
				"spectate", "removespawn", "listplayers");
	}

	private List<String> getValueListCmds() {
		return Arrays.asList("actionbar", "bossbar", "globalmessages");
	}
}
