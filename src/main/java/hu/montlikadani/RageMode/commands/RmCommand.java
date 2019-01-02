package hu.montlikadani.ragemode.commands;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.util.StringUtil;

import hu.montlikadani.ragemode.RageMode;
import hu.montlikadani.ragemode.gameUtils.GetGames;

public class RmCommand implements CommandExecutor, TabCompleter {

	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		if (cmd.getName().equals("ragemode")) {
			if (args.length == 0) {
				String msg = "";
				msg += "&6[&3RageMode&c commands list&6]\n";
				if (hasPerm(sender, "ragemode.help.playercommands"))
					msg += "&7-&6 /ragemode (or rm)&a - Main command for RageMode plugin.\n";

				if (sender instanceof Player) {
					if (hasPerm(sender, "ragemode.help.playercommands") || hasPerm(sender, "ragemode.join"))
						msg += "&7-&6 /rm join <gameName>&a - Join to the specified game.\n";

					if (hasPerm(sender, "ragemode.help.playercommands") || hasPerm(sender, "ragemode.leave"))
						msg += "&7-&6 /rm leave&a - Leave from the current game.\n";

					if (hasPerm(sender, "ragemode.help.playercommands") || hasPerm(sender, "ragemode.stats"))
						msg += "&7-&6 /rm showstats [player]&a - Showing statistic of a target player.\n";

					if (hasPerm(sender, "ragemode.admin.resetstats"))
						msg += "&7-&6 /rm resetstats [player]&a - Reset the player's stat.\n";
				} else {
					msg += "&7-&6 /rm showstats <player>&a - Showing statistic of a target player.\n";

					msg += "&7-&6 /rm resetstats <player>&a - Reset the player's stat.\n";
				}

				if (hasPerm(sender, "ragemode.help.playercommands") || hasPerm(sender, "ragemode.listgames"))
					msg += "&7-&6 /rm listgames&a - Listing available games.\n \n"; // New row in empty

				if (hasPerm(sender, "ragemode.admin.reload"))
					msg += "&7-&6 /rm reload&a - Reloads the plugin and configuration.\n";

				if (sender instanceof Player) {
					// Setup
					if (hasPerm(sender, "ragemode.admin.addgame"))
						msg += "&7-&6 /rm addgame <gameName>&a - Adds a new game.\n";

					if (hasPerm(sender, "ragemode.admin.setlobby"))
						msg += "&7-&6 /rm setlobby <gameName>&a - Adds a lobby spawn for the new game.\n";

					if (hasPerm(sender, "ragemode.admin.addspawn"))
						msg += "&7-&6 /rm addspawn <gameName>&a - Adds a new spawn location.\n";

					if (hasPerm(sender, "ragemode.admin.holostats"))
						msg += "&7-&6 /rm holostats <add/remove/tp>&a - Adds/remove or teleport a new hologram.\n";

					if (hasPerm(sender, "ragemode.admin.setactionbar"))
						msg += "'&7-&6 /rm actionbar <gameName> <true/false>&a - Actionbar on/off which display on the game.\n";

					if (hasPerm(sender, "ragemode.admin.setbossbar"))
						msg += "&7-&6 /rm bossbar <gameName> <true/false>&a - Bossbar on/off which display on the game.\n";

					if (hasPerm(sender, "ragemode.admin.setgametime"))
						msg += "&7-&6 /rm gametime <gameName> <minutes>&a - Adding game time (in minutes) to game.\n";

					if (hasPerm(sender, "ragemode.admin.setglobalmessages"))
						msg += "&7-&6 /rm globalmessages <gameName> <true/false>&a - Global messages on/off which showing death or other messages.\n";

					if (hasPerm(sender, "ragemode.admin.setlobbydelay"))
						msg += "&7-&6 /rm lobbydelay <gameName> <seconds>&a - Lobby waiting time in seconds.\n";

					if (hasPerm(sender, "ragemode.admin.removegame"))
						msg += "&7-&6 /rm removegame <gameName>&a - Removes the specified game.\n";

					// Other
					if (hasPerm(sender, "ragemode.admin.stopgame"))
						msg += "&7-&6 /rm stop <gameName>&a - Stops the specified game.\n";

					if (hasPerm(sender, "ragemode.admin.kick"))
						msg += "&7-&6 /rm kick <gameName> <player>&a - Kick a player from the game.\n";

					if (hasPerm(sender, "ragemode.admin.forcestart"))
						msg += "&7-&6 /rm forcestart <gameName>&a - Forces the specified game to start.";
				}
				sender.sendMessage(RageMode.getLang().colors(msg));
				return true;
			}

			switch (args[0]) {
			case "addgame":
				new AddGame(sender, cmd, label, args);
				return true;
			case "setlobby":
				new SetLobby(sender, cmd, label, args);
				return true;
			case "addspawn":
				new AddSpawn(sender, cmd, label, args);
				return true;
			case "holostats":
			case "holo":
				new HoloStats(sender, cmd, label, args);
				return true;
			case "listgames":
			case "list":
				new ListGames(sender, cmd, label, args);
				return true;
			case "join":
				new PlayerJoin(sender, cmd, label, args);
				return true;
			case "leave":
				new PlayerLeave(sender, cmd, label, args);
				return true;
			case "reload":
			case "rl":
				new Reload(sender, cmd, label, args);
				return true;
			case "removegame":
			case "remove":
				new RemoveGame(sender, cmd, label, args);
				return true;
			case "actionbar":
				new SetActionBar(sender, cmd, label, args);
				return true;
			case "bossbar":
				new SetBossBar(sender, cmd, label, args);
				return true;
			case "resetstats":
				new ResetPlayerStats(sender, cmd, label, args);
				return true;
			case "gametime":
				new SetGameTime(sender, cmd, label, args);
				return true;
			case "globalmessages":
			case "globalmsgs":
				new SetGlobalMessages(sender, cmd, label, args);
				return true;
			case "lobbydelay":
				new SetLobbyDelay(sender, cmd, label, args);
				return true;
			case "forcestart":
				new ForceStart(sender, cmd, label, args);
				return true;
			case "stats":
			case "showstats":
				new ShowStats(sender, cmd, label, args);
				return true;
			case "kickplayer":
			case "kick":
				new KickPlayer(sender, cmd, label, args);
				return true;
			case "stop":
			case "stopgame":
				new StopGame(sender, cmd, label, args);
				return true;
			}
		}
		return true;
	}

	@Override
	public List<String> onTabComplete(CommandSender sender, Command cmd, String label, String[] args) {
		List<String> completionList = new ArrayList<>();

		if (args.length < 2) {
			List<String> cmds = new ArrayList<>();
			for (String com : getDefaultCmds(sender)) {
				cmds.add(com);
			}
			for (String com : getAdminCmds(sender)) {
				cmds.add(com);
			}
			for (String com : getSomeCmds(sender)) {
				cmds.add(com);
			}

			StringUtil.copyPartialMatches(args[0], cmds, completionList);
		}
		if (args.length < 3 && args[1].equals(getGameListCmds())) {
			List<String> games = new ArrayList<>();
			for (String scmd : GetGames.getGameNames()) {
				games.add(scmd);
			}
			StringUtil.copyPartialMatches(args[1], games, completionList);
		}
		Collections.sort(completionList);
		return completionList;
	}

	private List<String> getDefaultCmds(CommandSender sender) {
		List<String> c = new ArrayList<>();
		for (String cmds : new String[] { "join", "leave", "listgames", "stats" }) {
			if (!hasPerm(sender, "ragemode.help.playercommands") || !hasPerm(sender, "ragemode." + cmds)) {
				continue;
			}
			c.add(cmds);
		}
		return c;
	}

	private List<String> getAdminCmds(CommandSender sender) {
		List<String> c = new ArrayList<>();
		for (String cmds : new String[] { "addgame", "addspawn", "setlobby", "reload", "holostats", "removegame", "resetstats", "forcestart",
				"kick" }) {
			if (!hasPerm(sender, "ragemode.admin." + cmds)) {
				continue;
			}
			c.add(cmds);
		}
		return c;
	}

	private List<String> getSomeCmds(CommandSender sender) {
		List<String> c = new ArrayList<>();
		for (String cmds : new String[] { "actionbar", "bossbar", "globalmessages", "gametime", "lobbydelay" }) {
			if (!hasPerm(sender, "ragemode.admin.set" + cmds)) {
				continue;
			}
			c.add(cmds);
		}
		return c;
	}

	private List<String> getGameListCmds() {
		List<String> c = new ArrayList<>();
		for (String cmds : new String[] { "actionbar", "bossbar", "globalmessages", "gametime", "lobbydelay", "removegame", "forcestart", "setlobby",
				"addspawn", "join", "leave" }) {
			c.add(cmds);
		}
		return c;
	}

	private boolean hasPerm(CommandSender sender, String perm) {
		if (sender.hasPermission(perm))
			return true;
		return false;
	}
}
