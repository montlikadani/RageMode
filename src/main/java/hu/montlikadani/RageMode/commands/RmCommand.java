package hu.montlikadani.ragemode.commands;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.WeakHashMap;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.util.StringUtil;

import hu.montlikadani.ragemode.RageMode;
import hu.montlikadani.ragemode.gameUtils.GetGames;

public class RmCommand implements CommandExecutor, TabCompleter {

	private Map<String, String> arg = new WeakHashMap<>();

	public RmCommand() {
		arg.clear();

		//TODO How to short this?
		arg.put("addgame", AddGame.class.getName());
		arg.put("addspawn", AddSpawn.class.getName());
		arg.put("forcestart", ForceStart.class.getName());
		arg.put("holostats", HoloStats.class.getName());
		arg.put("holo", HoloStats.class.getName());

		arg.put("kick", KickPlayer.class.getName());
		arg.put("listgames", ListGames.class.getName());
		arg.put("list", ListGames.class.getName());

		arg.put("join", PlayerJoin.class.getName());
		arg.put("leave", PlayerLeave.class.getName());
		arg.put("points", Points.class.getName());
		arg.put("reload", Reload.class.getName());
		arg.put("rl", Reload.class.getName());

		arg.put("removegame", RemoveGame.class.getName());
		arg.put("resetstats", ResetPlayerStats.class.getName());
		arg.put("actionbar", SetActionBar.class.getName());
		arg.put("bossbar", SetBossBar.class.getName());
		arg.put("gametime", SetGameTime.class.getName());
		arg.put("globalmessages", SetGlobalMessages.class.getName());
		arg.put("globalmsgs", SetGlobalMessages.class.getName());

		arg.put("setlobby", SetLobby.class.getName());
		arg.put("lobbydelay", SetLobbyDelay.class.getName());
		arg.put("stats", ShowStats.class.getName());
		arg.put("signupdate", SignUpdate.class.getName());
		arg.put("spectate", Spectate.class.getName());
		arg.put("spec", Spectate.class.getName());

		arg.put("stopgame", StopGame.class.getName());
		arg.put("stop", StopGame.class.getName());

		arg.put("togglegame", ToggleGame.class.getName());
		arg.put("givesaveditems", GiveSavedItems.class.getName());
		arg.put("removespawn", RemoveSpawn.class.getName());
		arg.put("latestart", LateStart.class.getName());
		arg.put("listplayers", ListPlayers.class.getName());
	}

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
						msg += "&7-&6 /rm stats [player]&a - Showing statistic of a target player.\n";

					if (hasPerm(sender, "ragemode.help.playercommands") || hasPerm(sender, "ragemode.spectate"))
						msg += "&7-&6 /rm spectate <gameName>&a - Join to the game with spectator mode.\n";
				} else {
					msg += "&7-&6 /rm stats <player>&a - Showing statistic of a target player.\n";

					msg += "&7-&6 /rm resetstats <player>&a - Reset the player's stat.\n";
				}

				if (hasPerm(sender, "ragemode.help.playercommands") || hasPerm(sender, "ragemode.listgames"))
					msg += "&7-&6 /rm listgames&a - Listing available games.\n \n"; // Style

				if (hasPerm(sender, "ragemode.admin.help"))
					msg += "&7-&6 /rm admin&a - Lists all admin commands.";

				if (hasPerm(sender, "ragemode.listplayers"))
					msg += "&7-&6 /rm listplayers [game]&a - Lists all currently playing players.";

				if (!msg.isEmpty())
					sendMessage(sender, RageMode.getLang().colors(msg));
				return true;
			}

			if (args.length == 1 && args[0].equals("admin")) {
				if (!hasPerm(sender, "ragemode.admin.help")) {
					sendMessage(sender, "no-permission");
					return true;
				}
				String msg = "";
				if (hasPerm(sender, "ragemode.admin.reload"))
					msg += "&7-&6 /rm reload&a - Reloads the plugin and configuration.\n";

				if (hasPerm(sender, "ragemode.admin.resetstats"))
					msg += "&7-&6 /rm resetstats [player]&a - Reset the player's stat.\n";

				if (hasPerm(sender, "ragemode.admin.stopgame"))
					msg += "&7-&6 /rm stop <gameName>&a - Stops the specified game.\n";

				if (hasPerm(sender, "ragemode.admin.signupdate"))
					msg += "&7-&6 /rm signupdate <gameName/all>&a - Refresh the specified or all game signs.\n";

				if (hasPerm(sender, "ragemode.admin.togglegame"))
					msg += "&7-&6 /rm togglegame <gameName>&a - Toggles the specified game, to a player not be able to join.\n";

				if (hasPerm(sender, "ragemode.admin.points"))
					msg += "&7-&6 /rm points set/add/take <player> <amount>&a - Changes the player points.\n";

				if (sender instanceof Player) {
					// Setup
					if (hasPerm(sender, "ragemode.admin.addgame"))
						msg += "&7-&6 /rm addgame <gameName>&a - Adds a new game.\n";

					if (hasPerm(sender, "ragemode.admin.setlobby"))
						msg += "&7-&6 /rm setlobby <gameName>&a - Adds a lobby spawn for the new game.\n";

					if (hasPerm(sender, "ragemode.admin.addspawn"))
						msg += "&7-&6 /rm addspawn <gameName>&a - Adds a new spawn location.\n";

					if (hasPerm(sender, "ragemode.admin.removespawn"))
						msg += "&7-&6 /rm removespawn <gameName> <id/all>&a - Removes the game spawn from id, or removes all.\n";

					if (hasPerm(sender, "ragemode.admin.holostats"))
						msg += "&7-&6 /rm holostats <add/remove/tp>&a - Adds/remove/teleports a new hologram.\n";

					if (hasPerm(sender, "ragemode.admin.setactionbar"))
						msg += "'&7-&6 /rm actionbar <gameName> <true/false>&a - Actionbar on/off which display in the game.\n";

					if (hasPerm(sender, "ragemode.admin.setbossbar"))
						msg += "&7-&6 /rm bossbar <gameName> <true/false>&a - Bossbar on/off which display in the game.\n";

					if (hasPerm(sender, "ragemode.admin.setgametime"))
						msg += "&7-&6 /rm gametime <gameName> <minutes>&a - Adding game time (in minutes) to game.\n";

					if (hasPerm(sender, "ragemode.admin.setglobalmessages"))
						msg += "&7-&6 /rm globalmessages <gameName> <true/false>&a - Global messages on/off which showing death or other messages.\n";

					if (hasPerm(sender, "ragemode.admin.setlobbydelay"))
						msg += "&7-&6 /rm lobbydelay <gameName> <seconds>&a - Lobby waiting time in seconds.\n";

					if (hasPerm(sender, "ragemode.admin.removegame"))
						msg += "&7-&6 /rm removegame <gameName>&a - Removes the specified game.\n";

					// Other
					if (hasPerm(sender, "ragemode.admin.forcestart"))
						msg += "&7-&6 /rm forcestart <gameName>&a - Forces the specified game to start.\n";

					if (hasPerm(sender, "ragemode.admin.givesaveditems"))
						msg += "&7-&6 /rm givesaveditems <player> [true]&a - Returns the saved inventory to the player"
								+ " (optional: actually remove from file).\n";

					if (hasPerm(sender, "ragemode.admin.latestart"))
						msg += "&7-&6 /rm latestart <timeInSeconds>&a - Increases the current lobby waiting time.";
				}
				if (hasPerm(sender, "ragemode.admin.kick"))
					msg += "&7-&6 /rm kick <gameName> <player>&a - Kick a player from the game.";

				if (!msg.isEmpty())
					sendMessage(sender, RageMode.getLang().colors(msg));
				return true;
			}

			for (Entry<String, String> a : arg.entrySet()) {
				if (args[0].equalsIgnoreCase(a.getKey())) {
					try {
						String className = a.getValue();
						Class<?> fclass = Class.forName(className);
						Object run = fclass.newInstance();
						Class<?>[] paramTypes = new Class<?>[] { RageMode.class, CommandSender.class, Command.class, String[].class };
						Method printMethod = null;
						try {
							printMethod = run.getClass().getDeclaredMethod("run", paramTypes);
						} catch (NoSuchMethodException e2) {
							paramTypes = new Class<?>[] { RageMode.class, CommandSender.class, String[].class };
						}

						try {
							printMethod = run.getClass().getDeclaredMethod("run", paramTypes);
						} catch (NoSuchMethodException e3) {
							paramTypes = new Class<?>[] { CommandSender.class, String[].class };
						}

						try {
							printMethod = run.getClass().getDeclaredMethod("run", paramTypes);
						} catch (NoSuchMethodException e4) {
							paramTypes = new Class<?>[] { RageMode.class, CommandSender.class };
						}

						try {
							printMethod = run.getClass().getDeclaredMethod("run", paramTypes);
						} catch (NoSuchMethodException e5) {
							paramTypes = new Class<?>[] { CommandSender.class };
						}

						if (printMethod == null)
							printMethod = run.getClass().getDeclaredMethod("run", paramTypes);

						Object[] arguments = new Object[] { RageMode.getInstance(), sender, cmd, args };
						try {
							printMethod.invoke(run, arguments);
							return true;
						} catch (IllegalArgumentException e1) {
							arguments = new Object[] { RageMode.getInstance(), sender, args };
						}

						try {
							printMethod.invoke(run, arguments);
							return true;
						} catch (IllegalArgumentException e2) {
							arguments = new Object[] { sender, args };
						}

						try {
							printMethod.invoke(run, arguments);
							return true;
						} catch (IllegalArgumentException e3) {
							arguments = new Object[] { RageMode.getInstance(), sender };
						}

						try {
							printMethod.invoke(run, arguments);
							return true;
						} catch (IllegalArgumentException e4) {
							arguments = new Object[] { sender };
						}
						printMethod.invoke(run, arguments);
					} catch (Throwable t) {
						t.printStackTrace();
					}
				}
			}
		}
		return true;
	}

	@Override
	public List<String> onTabComplete(CommandSender sender, Command cmd, String label, String[] args) {
		List<String> completionList = new ArrayList<>();
		String partOfCommand = "";

		if (cmd.getName().equals("ragemode") || cmd.getName().equals("rm")) {
			List<String> cmds = new ArrayList<>();
			if (args.length < 2) {
				getDefaultCmds(sender).forEach(com -> cmds.add(com));

				getAdminCmds(sender).forEach(aCom -> cmds.add(aCom));

				getSomeCmds(sender).forEach(sCom -> cmds.add(sCom));

				partOfCommand = args[0];
			} else if (args.length < 3) {
				if (args[0].equalsIgnoreCase("holostats")) {
					Arrays.asList("add", "remove", "tp").forEach(hcmd -> cmds.add(hcmd));

					partOfCommand = args[1];
				} else {
					for (int i = 0; i < getGameListCmds().size(); i++) {
						if (args[0].equalsIgnoreCase(getGameListCmds().get(i))) {
							for (String scmd : GetGames.getGameNames()) {
								cmds.add(scmd);
							}

							partOfCommand = args[1];
						}
					}
				}
			} else if (args.length < 4) {
				for (int i = 0; i < getValueListCmds().size(); i++) {
					if (args[0].equalsIgnoreCase(getValueListCmds().get(i))) {
						Arrays.asList("true", "false").forEach(tf -> cmds.add(tf));

						partOfCommand = args[2];
					}
				}
			}
			StringUtil.copyPartialMatches(partOfCommand, cmds, completionList);
		}
		Collections.sort(completionList);
		return completionList;
	}

	private List<String> getDefaultCmds(CommandSender sender) {
		List<String> c = new ArrayList<>();
		for (String cmds : new String[] { "join", "leave", "listgames", "stats", "spectate", "listplayers" }) {
			if (!hasPerm(sender, "ragemode.help.playercommands") || !hasPerm(sender, "ragemode." + cmds))
				continue;

			c.add(cmds);
		}
		return c;
	}

	private List<String> getAdminCmds(CommandSender sender) {
		List<String> c = new ArrayList<>();
		for (String cmds : new String[] { "addgame", "addspawn", "setlobby", "reload", "holostats", "removegame", "resetstats", "forcestart",
				"kick", "stopgame", "signupdate", "togglegame", "points", "givesaveditems", "removespawn", "latestart" }) {
			if (!hasPerm(sender, "ragemode.admin." + cmds))
				continue;

			c.add(cmds);
		}
		return c;
	}

	private List<String> getSomeCmds(CommandSender sender) {
		List<String> c = new ArrayList<>();
		for (String cmds : new String[] { "actionbar", "bossbar", "globalmessages", "gametime", "lobbydelay" }) {
			if (!hasPerm(sender, "ragemode.admin.set" + cmds))
				continue;

			c.add(cmds);
		}
		return c;
	}

	private List<String> getGameListCmds() {
		return Arrays.asList("actionbar", "bossbar", "globalmessages", "gametime", "lobbydelay", "removegame", "forcestart", "setlobby",
				"addspawn", "join", "leave", "signupdate", "stop", "stopgame", "togglegame", "spectate", "removespawn", "listplayers");
	}

	private List<String> getValueListCmds() {
		return Arrays.asList("actionbar", "bossbar", "globalmessages");
	}

	boolean hasPerm(CommandSender sender, String perm) {
		if (sender.hasPermission(perm))
			return true;
		return false;
	}

	void sendMessage(CommandSender sender, String msg) {
		if (sender != null && msg != null && !msg.equals(""))
			sender.sendMessage(msg);
	}

	boolean run(CommandSender sender) {
		return false;
	}

	boolean run(RageMode plugin, CommandSender sender) {
		return false;
	}

	boolean run(CommandSender sender, String[] args) {
		return false;
	}

	boolean run(RageMode plugin, CommandSender sender, String[] args) {
		return false;
	}

	boolean run(RageMode plugin, CommandSender sender, Command cmd, String[] args) {
		return false;
	}
}
