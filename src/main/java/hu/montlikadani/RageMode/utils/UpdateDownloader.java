package hu.montlikadani.ragemode.utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;

import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import hu.montlikadani.ragemode.Debug;
import hu.montlikadani.ragemode.RageMode;
import hu.montlikadani.ragemode.Utils;
import hu.montlikadani.ragemode.config.ConfigValues;

/**
 * @author montlikadani
 *
 */
public class UpdateDownloader {

	public static void checkFromGithub(org.bukkit.command.CommandSender sender) {
		if (!ConfigValues.isCheckForUpdates()) {
			return;
		}

		new BukkitRunnable() {
			@Override
			public void run() {
				String versionString = "";
				String lineWithVersion = "";

				int newVersion = 0;
				int currentVersion = 0;

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

					versionString = lineWithVersion.split(": ")[1];
					String nVersion = versionString.replaceAll("[^0-9]", "");
					newVersion = Integer.parseInt(nVersion);

					String cVersion = RageMode.getInstance().getDescription().getVersion().replaceAll("[^0-9]", "");
					currentVersion = Integer.parseInt(cVersion);

					String msg = "";
					if (newVersion > currentVersion) {
						if (sender instanceof Player) {
							msg = Utils.colors("&8&m&l--------------------------------------------------\n"
									+ "&aA new update is available for RageMode!&4 Version:&7 " + versionString
									+ (ConfigValues.isDownloadUpdates() ? ""
											: "\n&6Download:&c &nhttps://www.spigotmc.org/resources/69169/")
									+ "\n&8&m&l--------------------------------------------------");
						} else {
							msg = "New version (" + versionString
									+ ") is available at https://www.spigotmc.org/resources/69169/";
						}
					} else if (!(sender instanceof Player)) {
						msg = "You're running the latest version.";
					}

					sender.sendMessage(msg);

					if (newVersion <= currentVersion) {
						return;
					}

					if (!ConfigValues.isDownloadUpdates()) {
						return;
					}

					final String name = "RageMode-" + newVersion;

					String updatesFolder = RageMode.getInstance().getFolder() + File.separator + "releases";
					File temp = new File(updatesFolder);
					if (!temp.exists()) {
						temp.mkdir();
					}

					// Do not attempt to download the file again, when it is already downloaded
					final File jar = new File(updatesFolder + File.separator + name + ".jar");
					if (jar.exists()) {
						return;
					}

					Debug.logConsole("Downloading new version of RageMode...");

					final URL download = new URL(
							"https://github.com/montlikadani/RageMode/releases/latest/download/RageMode.jar");

					InputStream in = download.openStream();
					Files.copy(in, jar.toPath(), StandardCopyOption.REPLACE_EXISTING);

					in.close();

					Debug.logConsole("The new RageMode has been downloaded to releases folder.");
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}.runTaskLaterAsynchronously(RageMode.getInstance(), 0);
	}
}
