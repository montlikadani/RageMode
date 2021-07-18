package hu.montlikadani.ragemode.utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.concurrent.CompletableFuture;

import org.bukkit.entity.Player;

import hu.montlikadani.ragemode.RageMode;
import hu.montlikadani.ragemode.config.configconstants.ConfigValues;

public final class UpdateDownloader {

	private static final RageMode PLUGIN = org.bukkit.plugin.java.JavaPlugin.getPlugin(RageMode.class);

	private static File releasesFolder;

	public static void checkFromGithub(org.bukkit.command.CommandSender sender) {
		releasesFolder = new File(PLUGIN.getFolder(), "releases");

		if (!ConfigValues.isCheckForUpdates()) {
			deleteDirectory();
			return;
		}

		CompletableFuture.supplyAsync(() -> {
			try {
				URL githubUrl = new URL(
						"https://raw.githubusercontent.com/montlikadani/RageMode/master/src/main/resources/plugin.yml");
				String lineWithVersion = "";

				try (BufferedReader br = new BufferedReader(new InputStreamReader(githubUrl.openStream()))) {
					String s;

					while ((s = br.readLine()) != null) {
						if (s.toLowerCase().contains("version")) {
							lineWithVersion = s;
							break;
						}
					}
				}

				String versionString = lineWithVersion.split(": ", 2)[1],
						nVersion = versionString.replaceAll("[^0-9]", ""),
						cVersion = PLUGIN.getDescription().getVersion().replaceAll("[^0-9]", "");

				int newVersion = Integer.parseInt(nVersion);
				int currentVersion = Integer.parseInt(cVersion);

				if (newVersion <= currentVersion || currentVersion >= newVersion) {
					deleteDirectory();
					return false;
				}

				if (sender instanceof Player) {
					sender.sendMessage(Utils.colors("&aA new update is available for RageMode!&4 Version:&7 "
							+ versionString + (ConfigValues.isDownloadUpdates() ? ""
									: "\n&6Download:&c &nhttps://www.spigotmc.org/resources/69169/")));
				} else {
					sender.sendMessage("New version (" + versionString
							+ ") is available at https://www.spigotmc.org/resources/69169/");
				}

				if (!ConfigValues.isDownloadUpdates()) {
					deleteDirectory();
					return false;
				}

				final String name = "RageMode-" + versionString;

				releasesFolder.mkdirs();

				// Do not attempt to download the file again, when it is already downloaded
				final File jar = new File(releasesFolder, name + ".jar");
				if (jar.exists()) {
					return false;
				}

				Debug.logConsole("Downloading new version of RageMode...");

				final URL download = new URL(
						"https://github.com/montlikadani/RageMode/releases/latest/download/" + name + ".jar");

				try (InputStream in = download.openStream()) {
					Files.copy(in, jar.toPath(), StandardCopyOption.REPLACE_EXISTING);
				}

				return true;
			} catch (FileNotFoundException f) {
			} catch (Exception e) {
				e.printStackTrace();
			}

			return true;
		}).thenAccept(b -> {
			if (b) {
				Debug.logConsole("The new RageMode has been downloaded to releases folder.");
			}
		});
	}

	private static void deleteDirectory() {
		if (!releasesFolder.exists()) {
			return;
		}

		for (File file : releasesFolder.listFiles()) {
			try {
				file.delete();
			} catch (SecurityException e) {
			}
		}

		try {
			releasesFolder.delete();
		} catch (SecurityException e) {
		}
	}
}
