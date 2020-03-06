package hu.montlikadani.ragemode.utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;

import org.bukkit.scheduler.BukkitRunnable;

import hu.montlikadani.ragemode.Debug;
import hu.montlikadani.ragemode.RageMode;
import hu.montlikadani.ragemode.config.ConfigValues;

/**
 * @author montlikadani
 *
 */
public class UpdateDownloader {

	public static String checkFromGithub(String sender) {
		if (!ConfigValues.isCheckForUpdates()) {
			return "";
		}

		String msg = "";
		String lineWithVersion = "";

		double newVersion = 0d;
		double currentVersion = 0d;

		try {
			URL githubUrl = new URL(
					"https://raw.githubusercontent.com/montlikadani/RageMode/master/src/main/resources/plugin.yml");
			BufferedReader br = new BufferedReader(new InputStreamReader(githubUrl.openStream()));
			String s;
			while ((s = br.readLine()) != null) {
				String line = s;
				if (line.toLowerCase().contains("version")) {
					lineWithVersion = line;
					break;
				}
			}

			String[] nVersion;
			String[] cVersion;

			String versionString = lineWithVersion.split(": ")[1];
			nVersion = versionString.replaceAll("[^0-9.]", "").split("\\.");
			newVersion = Double.parseDouble(nVersion[0] + "." + nVersion[1]);

			cVersion = RageMode.getInstance().getDescription().getVersion().replaceAll("[^0-9.]", "").split("\\.");
			currentVersion = Double.parseDouble(cVersion[0] + "." + cVersion[1]);

			if (newVersion > currentVersion) {
				if (sender.equals("player")) {
					msg = "&8&m&l--------------------------------------------------\n"
							+ "&aA new update is available for RageMode!&4 Version:&7 " + versionString
							+ (ConfigValues.isDownloadUpdates() ? ""
									: "\n&6Download:&c &nhttps://www.spigotmc.org/resources/69169/")
							+ "\n&8&m&l--------------------------------------------------";
				} else if (sender.equals("console")) {
					msg = "New version (" + versionString
							+ ") is available at https://www.spigotmc.org/resources/69169/";
				}
			} else if (sender.equals("console")) {
				return "You're running the latest version.";
			}

			if (newVersion < currentVersion) {
				return msg;
			}

			if (!ConfigValues.isDownloadUpdates()) {
				return msg;
			}

			Debug.logConsole("Downloading new version of RageMode...");

			final String name = "RageMode-" + newVersion;
			final URL download = new URL(
					"https://github.com/montlikadani/RageMode/releases/latest/download/RageMode.jar");

			new BukkitRunnable() {
				@Override
				public void run() {
					try {
						InputStream in = download.openStream();
						String per = File.separator;
						String updatesFolder = RageMode.getInstance().getFolder() + per + "releases";
						File temp = new File(updatesFolder);
						if (!temp.exists()) {
							temp.mkdir();
						}

						File jar = new File(updatesFolder + per + name + ".jar");
						Files.copy(in, jar.toPath(), StandardCopyOption.REPLACE_EXISTING);

						in.close();

						Debug.logConsole("The RageMode successfully downloaded to releases folder.");
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			}.runTaskLaterAsynchronously(RageMode.getInstance(), 0);
		} catch (Exception e) {
			e.printStackTrace();
		}

		return msg;
	}
}
