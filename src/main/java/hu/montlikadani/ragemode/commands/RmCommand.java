package hu.montlikadani.ragemode.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import hu.montlikadani.ragemode.RageMode;
import hu.montlikadani.ragemode.commands.annotations.CommandProcessor;
import hu.montlikadani.ragemode.utils.reflection.Reflections;

import static hu.montlikadani.ragemode.utils.Misc.sendMessage;

import java.net.URL;
import java.net.URLClassLoader;
import java.util.HashSet;
import java.util.Set;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

public class RmCommand implements CommandExecutor {

	private final Set<ICommand> cmds = new HashSet<>();

	public RmCommand() {
		Set<Class<?>> subCmds = new HashSet<Class<?>>() {

			private static final long serialVersionUID = 1L;

			{
				// Avoiding adding class names one by one
				for (URL jarURL : ((URLClassLoader) RageMode.class.getClassLoader()).getURLs()) {
					try (JarFile file = new JarFile(jarURL.toURI().getPath())) {
						for (java.util.Enumeration<JarEntry> entry = file.entries(); entry.hasMoreElements();) {
							String name = entry.nextElement().getName().replace('/', '.');

							if (name.contains("ragemode.commands.list") && name.endsWith(".class")
									&& name.matches("^[a-zA-Z|.]+$")) {
								try {
									add(Class.forName(name.substring(0, name.length() - 6)));
								} catch (ClassNotFoundException e) {
								}
							}
						}
					} catch (java.io.IOException | java.net.URISyntaxException e) {
						e.printStackTrace();
					}
				}
			}
		};

		for (Class<?> s : subCmds) {
			try {
				if (!s.isAnnotationPresent(CommandProcessor.class)) {
					continue;
				}

				if (Reflections.getCurrentJavaVersion() >= 9) {
					cmds.add((ICommand) s.getDeclaredConstructor().newInstance());
				} else {
					cmds.add((ICommand) s.newInstance());
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		boolean found = false;
		boolean isPlayer = sender instanceof Player;

		for (ICommand command : cmds) {
			CommandProcessor proc = ICommand.PROC;

			if (proc == null) {
				continue;
			}

			if (args.length == 0) {
				if (isPlayer) {
					boolean haveAnyPermission = false;

					for (String permission : proc.permission()) {
						if (sender.hasPermission(permission)) {
							haveAnyPermission = true;
							break;
						}
					}

					if (!haveAnyPermission) {
						continue;
					}
				} else if (proc.playerOnly()) {
					continue;
				}

				String params = proc.params().isEmpty() ? "" : " " + proc.params();
				sendMessage(sender, "&7- /rm " + proc.name() + params + " -&6 " + proc.desc());
				found = true; // We marks as found to make sure it is not a wrong command
				continue;
			}

			if (!proc.name().equalsIgnoreCase(args[0])) {
				continue;
			}

			found = true;

			if (proc.playerOnly() && !isPlayer) {
				sendMessage(sender, RageMode.getLang().get("in-game-only"));
				return true;
			}

			if (isPlayer) {
				boolean haveAnyPermission = false;

				for (String permission : proc.permission()) {
					if (sender.hasPermission(permission)) {
						haveAnyPermission = true;
						break;
					}
				}

				if (!haveAnyPermission) {
					sendMessage(sender, RageMode.getLang().get("no-permission"));
					return true;
				}
			}

			command.run(org.bukkit.plugin.java.JavaPlugin.getPlugin(RageMode.class), sender, args);
			break;
		}

		if (!found) {
			sendMessage(sender, RageMode.getLang().get("wrong-command"));
		}

		return true;
	}
}
