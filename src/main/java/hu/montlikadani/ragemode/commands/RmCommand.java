package hu.montlikadani.ragemode.commands;

import java.util.List;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import hu.montlikadani.ragemode.toolbox.GetGames;

public class RmCommand implements CommandExecutor, TabCompleter {

	public RmCommand() {
	}

	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		if (cmd.getName().equals("ragemode")) {
			if (args.length == 0) {
				String msg = "";
				msg += "&6[&3RageMode&c commands list&6]\n";
				if (hasPerm(sender, "ragemode.help.playercommands"))
					msg += "&7-&6 /ragemode (or rm)&a - Main command for RageMode plugin.\n";

				if (hasPerm(sender, "ragemode.help.playercommands") || hasPerm(sender, "ragemode.join"))
					msg += "&7-&6 /rm join <gameName>&a - Join to the specified game.\n";

				if (hasPerm(sender, "ragemode.help.playercommands") || hasPerm(sender, "ragemode.leave"))
					msg += "&7-&6 /rm leave <gameName>&a - Leave from the current game.\n";

				if (hasPerm(sender, "ragemode.help.playercommands") || hasPerm(sender, "ragemode.stats"))
					msg += "&7-&6 /rm showstats [player]&a - Showing statistic of a target player.\n";

				if (hasPerm(sender, "ragemode.help.playercommands") || hasPerm(sender, "ragemode.list"))
					msg += "&7-&6 /rm listgames&a - Listing available games.\n \n";

				if (hasPerm(sender, "ragemode.admin.reload"))
					msg += "&7-&6 /rm reload&a - Reloads the plugin and configuration.\n";

				if (hasPerm(sender, "ragemode.admin.stats.reset"))
					msg += "&7-&6 /rm resetstats [player]&a - Reset the player's stat.\n";

				if (sender instanceof Player) {
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

					if (hasPerm(sender, "ragemode.admin.stopgame"))
						msg += "&7-&6 /rm stop <gameName>&a - Stops the specified game.\n";

					if (hasPerm(sender, "ragemode.admin.removegame"))
						msg += "&7-&6 /rm removegame <gameName>&a - Removes the specified game.\n";

					if (hasPerm(sender, "ragemode.admin.kick"))
						msg += "&7-&6 /rm kick <gameName> <player>&a - Kick a player from the game.\n";

					if (hasPerm(sender, "ragemode.admin.forcestart"))
						msg += "&7-&6 /rm forcestart <gameName>&a - Forces the specified game to start.";
				}
				sender.sendMessage(ChatColor.translateAlternateColorCodes('&', msg));
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
			case "globalmsgs":
			case "globalmessages":
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
				new StopGame(sender, cmd, label, args);
				return true;
			}
		}
		return true;
	}

	private boolean hasPerm(CommandSender sender, String perm) {
		if (sender.hasPermission(perm))
			return true;
		return false;
	}

	@Override
	public List<String> onTabComplete(CommandSender sender, Command cmd, String label, String[] args) {
		if (hasPerm(sender, "ragemode.commands.tabcomplete")) {
			List<String> cmds = new java.util.ArrayList<>();
			if (cmd.getName().equals("ragemode") || cmd.getName().equals("rm")) {
				if (args.length < 2) {
					String[] sub = new String[] { "addgame", "setlobby", "addspawn", "holostats", "listgames", "join", "leave", "reload",
							"removegame", "actionbar", "bossbar", "resetstats", "gametime", "globalmessages", "lobbydelay", "forcestart",
							"showstats", "stop", "kickplayer" };
					for (String com : sub) {
						cmds.add(com);
					}
				}
				if (args.length < 3) {
					if (args[0].equals("addspawn") || args[0].equals("forcestart") || args[0].equals("kick") || args[0].equals("kickplayer") ||
							args[0].equals("join") || args[0].equals("removegame") || args[0].equals("remove") || args[0].equals("actionbar") ||
							args[0].equals("bossbar") || args[0].equals("globalmessages") || args[0].equals("globalmsgs") || args[0].equals("setlobby") ||
							args[0].equals("lobbydelay") || args[0].equals("stop")) {
						for (String scmd : GetGames.getGameNames()) {
							cmds.add(scmd);
						}
					}
				}
			}
			return cmds;
		}
		return null;
	}
}
