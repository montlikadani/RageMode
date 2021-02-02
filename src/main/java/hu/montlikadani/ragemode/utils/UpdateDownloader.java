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
import hu.montlikadani.ragemode.config.ConfigValues;

public final class UpdateDownloader {

	private static final File RELEASESFOLDER = new File(RageMode.getInstance().getFolder(), "releases");

	public static void checkFromGithub(org.bukkit.command.CommandSender sender) {
		if (!ConfigValues.isCheckForUpdates()) {
			deleteDirectory();
			return;
		}

		CompletableFuture.supplyAsync(() -> {
			try {
				URL githubUrl = new URL(
						"https://raw.githubusercontent.com/montlikadani/RageMode/master/src/main/resources/plugin.yml");
				BufferedReader br = new BufferedReader(new InputStreamReader(githubUrl.openStream()));
				String s;
				String lineWithVersion = "";
				while ((s = br.readLine()) != null) {
					String line = s;
					if (line.toLowerCase().contains("version")) {
						lineWithVersion = line;
						break;
					}
				}

				String versionString = lineWithVersion.split(": ")[1],
						nVersion = versionString.replaceAll("[^0-9]", ""),
						cVersion = RageMode.getInstance().getDescription().getVersion().replaceAll("[^0-9]", "");

				int newVersion = Integer.parseInt(nVersion);
				int currentVersion = Integer.parseInt(cVersion);

				if (newVersion <= currentVersion || currentVersion >= newVersion) {
					deleteDirectory();
					return false;
				}

				String msg = "";
				if (sender instanceof Player) {
					msg = Utils.colors("&aA new update is available for RageMode!&4 Version:&7 " + versionString
							+ (ConfigValues.isDownloadUpdates() ? ""
									: "\n&6Download:&c &nhttps://www.spigotmc.org/resources/69169/"));
				} else {
					msg = "New version (" + versionString
							+ ") is available at https://www.spigotmc.org/resources/69169/";
				}

				sender.sendMessage(msg);

				if (!ConfigValues.isDownloadUpdates()) {
					deleteDirectory();
					return false;
				}

				final String name = "RageMode-" + versionString;

				if (!RELEASESFOLDER.exists()) {
					RELEASESFOLDER.mkdir();
				}

				// Do not attempt to download the file again, when it is already downloaded
				final File jar = new File(RELEASESFOLDER, name + ".jar");
				if (jar.exists()) {
					return false;
				}

				Debug.logConsole("Downloading new version of RageMode...");

				final URL download = new URL(
						"https://github.com/montlikadani/RageMode/releases/latest/download/" + name + ".jar");

				InputStream in = download.openStream();
				try {
					Files.copy(in, jar.toPath(), StandardCopyOption.REPLACE_EXISTING);
				} finally {
					in.close();
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
		if (!RELEASESFOLDER.exists()) {
			return;
		}

		for (File file : RELEASESFOLDER.listFiles()) {
			try {
				file.delete();
			} catch (SecurityException e) {
			}
		}

		try {
			RELEASESFOLDER.delete();
		} catch (SecurityException e) {
		}
	}
}
