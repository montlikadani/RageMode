package hu.montlikadani.ragemode.commands;

import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.HashMap;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import hu.montlikadani.ragemode.Debug;
import hu.montlikadani.ragemode.RageMode;
import hu.montlikadani.ragemode.Utils;

import static hu.montlikadani.ragemode.utils.Message.hasPerm;
import static hu.montlikadani.ragemode.utils.Message.sendMessage;

public class RmCommand implements CommandExecutor {

	public static Map<String, String> arg = new HashMap<>();

	public RmCommand() {
		arg.clear();

		String path = "hu.montlikadani.ragemode.commands.list";
		List<Class<?>> classes = Utils.getClasses(path);
		for (int i = 0; i < classes.size(); i++) {
			String className = classes.get(i).getName().toLowerCase();
			arg.put(className.replace(path + ".", ""), className);
		}
	}

	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		if (cmd.getName().equalsIgnoreCase("ragemode")) {
			if (args.length == 0) {
				String msg = "";

				msg += "&7=====&6[&3RageMode&c commands list&6]&7=====\n";

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

					if (hasPerm(sender, "ragemode.listplayers"))
						msg += "&7-&6 /rm listplayers [game]&a - Lists all currently playing players.";
				} else {
					msg += "&7-&6 /rm stats <player>&a - Showing statistic of a target player.\n";

					msg += "&7-&6 /rm resetstats <player>&a - Reset the player's stat.\n";

					msg += "&7-&6 /rm listplayers <game>&a - Lists all currently playing players.\n";
				}

				if (hasPerm(sender, "ragemode.help.playercommands") || hasPerm(sender, "ragemode.listgames"))
					msg += "&7-&6 /rm listgames&a - Listing available games.\n \n"; // Style

				if (hasPerm(sender, "ragemode.admin.help")) {
					msg += "&7-&6 /rm admin&a - Lists all admin commands.\n";

					if (sender instanceof Player && hasPerm(sender, "ragemode.admin.setup"))
						msg += "&7-&6 /rm setup&a - Lists all admin setup commands.\n";
				}

				msg += "&7==========";

				if (!msg.isEmpty())
					sendMessage(sender, Utils.colors(msg));

				return true;
			}

			if (args.length >= 1) {
				if (args[0].equalsIgnoreCase("setup")) {
					if (!(sender instanceof Player)) {
						sendMessage(sender, RageMode.getLang().get("in-game-only"));
						return true;
					}

					if (!hasPerm(sender, "ragemode.admin.setup")) {
						sendMessage(sender, RageMode.getLang().get("no-permission"));
						return true;
					}

					String msg = "";

					// Setup
					if (hasPerm(sender, "ragemode.admin.addgame"))
						msg += "&7-&6 /rm addgame <gameName>&a - Adds a new game.\n";

					if (hasPerm(sender, "ragemode.admin.maxplayers"))
						msg += "&7-&6 /rm maxplayers <gameName> <maxPlayers>&a - Modifies the maxplayers value for the game.\n";

					if (hasPerm(sender, "ragemode.admin.setlobby"))
						msg += "&7-&6 /rm setlobby <gameName>&a - Adds a lobby spawn for the new game.\n";

					if (hasPerm(sender, "ragemode.admin.addspawn"))
						msg += "&7-&6 /rm addspawn <gameName>&a - Adds a new spawn location.\n";

					if (hasPerm(sender, "ragemode.admin.removespawn"))
						msg += "&7-&6 /rm removespawn <gameName> <id/all>&a - Removes the game spawn from id, or removes all.\n";

					if (hasPerm(sender, "ragemode.admin.holostats"))
						msg += "&7-&6 /rm holostats <add/remove/tp>&a - Adds/remove/teleports a new hologram.\n";

					if (hasPerm(sender, "ragemode.admin.setactionbar"))
						msg += "&7-&6 /rm actionbar <gameName> <true/false>&a - Actionbar on/off which display in the game.\n";

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

					if (!msg.isEmpty()) {
						sendMessage(sender, Utils.colors(msg));
					}
				} else if (args[0].equalsIgnoreCase("admin")) {
					if (!hasPerm(sender, "ragemode.admin.help")) {
						sendMessage(sender, RageMode.getLang().get("no-permission"));
						return true;
					}

					String msg = "";

					if (hasPerm(sender, "ragemode.admin.reload"))
						msg += "&7-&6 /rm reload&a - Reloads the plugin and configuration.\n";

					if (hasPerm(sender, "ragemode.admin.forcestart"))
						msg += "&7-&6 /rm forcestart <gameName>&a - Forces the specified game to start.\n";

					if (hasPerm(sender, "ragemode.admin.givesaveditems"))
						msg += "&7-&6 /rm givesaveditems <player> [true]&a - Returns the saved inventory to the player.\n";

					if (hasPerm(sender, "ragemode.admin.latestart"))
						msg += "&7-&6 /rm latestart <timeInSeconds>&a - Increases the current lobby waiting time.\n";

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

					if (hasPerm(sender, "ragemode.admin.kick"))
						msg += "&7-&6 /rm kick <gameName> <player>&a - Kick a player from the game.";

					if (!msg.isEmpty()) {
						sendMessage(sender, Utils.colors(msg));
					}
				}

				for (Entry<String, String> a : arg.entrySet()) {
					if (args[0].equalsIgnoreCase(a.getKey())) {
						try {
							String className = a.getValue();
							Class<?> fclass = Class.forName(className);
							Object run = fclass.newInstance();
							Class<?>[] paramTypes = { RageMode.class, CommandSender.class, Command.class, String[].class };
							Method printMethod = null;
							try {
								printMethod = run.getClass().getDeclaredMethod("run", paramTypes);
							} catch (NoSuchMethodException e2) {
								paramTypes = new Class<?>[] { RageMode.class, CommandSender.class, String[].class };
							}

							if (printMethod == null) {
								try {
									printMethod = run.getClass().getDeclaredMethod("run", paramTypes);
								} catch (NoSuchMethodException e3) {
									paramTypes = new Class<?>[] { CommandSender.class, String[].class };
								}
							}

							if (printMethod == null) {
								try {
									printMethod = run.getClass().getDeclaredMethod("run", paramTypes);
								} catch (NoSuchMethodException e4) {
									paramTypes = new Class<?>[] { RageMode.class, CommandSender.class };
								}
							}

							if (printMethod == null) {
								try {
									printMethod = run.getClass().getDeclaredMethod("run", paramTypes);
								} catch (NoSuchMethodException e5) {
									paramTypes = new Class<?>[] { CommandSender.class };
								}
							}

							if (printMethod == null)
								printMethod = run.getClass().getDeclaredMethod("run", paramTypes);

							Object[] arguments = { RageMode.getInstance(), sender, cmd, args };
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
							Debug.throwMsg();
						}

						break;
					}
				}
			}
		}
		return true;
	}
}
