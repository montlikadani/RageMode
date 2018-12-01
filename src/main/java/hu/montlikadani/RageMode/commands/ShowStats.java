package hu.montlikadani.RageMode.commands;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import hu.montlikadani.RageMode.RageMode;
import hu.montlikadani.RageMode.runtimeRPP.RuntimeRPPManager;
import hu.montlikadani.RageMode.scores.RetPlayerPoints;

public class ShowStats extends RmCommand {

	public ShowStats(CommandSender sender, Command cmd, String label, String[] args) {
		if (!sender.hasPermission("ragemode.stats")) {
			sender.sendMessage(RageMode.getLang().get("no-permission"));
			return;
		}
		Player p = (Player) sender;
		if (args.length < 2)
			constructMessage(p, Bukkit.getPlayer(args[1]).getName());
		else
			constructMessage(p, p.getName());
		return;
	}

	private void constructMessage(Player player, String playerName) {
		Thread thread = new Thread(new uuiderThread(player, playerName));
		thread.start();
	}

	private class UUIDStrings {
		public String id;
		public String name;
	}

	private class uuiderThread implements Runnable {
		private String name;
		private Player player;
		private String sUUID;

		public uuiderThread(Player player, String name) {
			this.player = player;
			this.name = name;
		}

		@Override
		public void run() {
			// http("https://api.mojang.com/users/profiles/minecraft/" + name,
			// "");
			https();

			if (sUUID == null) {
				player.sendMessage(RageMode.getLang().get("player-non-existent"));
				return;
			}

			CharSequence sUUID_SEQ_1 = sUUID.subSequence(0, 8);
			CharSequence sUUID_SEQ_2 = sUUID.subSequence(8, 12);
			CharSequence sUUID_SEQ_3 = sUUID.subSequence(12, 16);
			CharSequence sUUID_SEQ_4 = sUUID.subSequence(16, 20);
			CharSequence sUUID_SEQ_5 = sUUID.subSequence(20, 32);
			sUUID = new String(sUUID_SEQ_1 + "-" + sUUID_SEQ_2 + "-" + sUUID_SEQ_3 + "-" + sUUID_SEQ_4 + "-" + sUUID_SEQ_5);

			//Player statsPlayer = Bukkit.getPlayer(UUID.fromString(sUUID));
			RetPlayerPoints rpp = null;

//			rpp = YAMLStats.getPlayerStatistics(sUUID);
			rpp = RuntimeRPPManager.getRPPForPlayer(sUUID);

			if (rpp != null) {
				player.sendMessage("Showing the stats of " + name + ":");

				for (String list : RageMode.getLang().getList("statistic-list")) {
					list = list.replace("%knife-kills%", rpp.getKnifeKills() + "");
					list = list.replace("%knife-deaths%", rpp.getKnifeDeaths() + "");

					list = list.replace("%explosion-kills%", rpp.getExplosionKills() + "");
					list = list.replace("%explosion-deaths%", rpp.getExplosionDeaths() + "");

					list = list.replace("%axe-kills%", rpp.getAxeKills() + "");
					list = list.replace("%axe-deaths%", rpp.getAxeDeaths() + "");

					list = list.replace("%direct-arrow-kills%", rpp.getDirectArrowKills() + "");
					list = list.replace("%direct-arrow-deaths%", rpp.getDirectArrowDeaths() + "");

					list = list.replace("%kills%", rpp.getKills() + "");
					list = list.replace("%deaths%", rpp.getDeaths() + "");
					list = list.replace("%kd%", rpp.getKD() + "");
					list = list.replace("%games%", rpp.getGames() + "");
					list = list.replace("%wins%", rpp.getWins() + "");
					list = list.replace("%points%", rpp.getPoints() + "");
					list = list.replace("%rank%", Integer.toString(rpp.getRank()) + "");
					player.sendMessage(list);
				}

				/*player.sendMessage(PluginLoader.gets().KNIFE_KILLS + rpp.getKnifeKills());
				player.sendMessage(PluginLoader.gets().EXPLOSION_KILLS + rpp.getExplosionKills());
				player.sendMessage(PluginLoader.gets().ARROW_KILLS + rpp.getDirectArrowKills());
				player.sendMessage(PluginLoader.gets().AXE_KILLS + rpp.getAxeKills());
				player.sendMessage("---------------");
				player.sendMessage(PluginLoader.gets().KNIFE_DEATHS + rpp.getKnifeDeaths());
				player.sendMessage(PluginLoader.gets().EXPLOSION_DEATHS + rpp.getExplosionDeaths());
				player.sendMessage(PluginLoader.gets().ARROW_DEATHS + rpp.getDirectArrowDeaths());
				player.sendMessage(PluginLoader.gets().AXE_DEATHS + rpp.getAxeDeaths());
				player.sendMessage("---------------");
				player.sendMessage(PluginLoader.gets().KILLS + rpp.getKills());
				player.sendMessage(PluginLoader.gets().DEATHS + rpp.getDeaths());
				player.sendMessage(PluginLoader.gets().KD + rpp.getKD());
				player.sendMessage("---------------");
				player.sendMessage(PluginLoader.gets().GAMES + rpp.getGames());
				player.sendMessage(PluginLoader.gets().WINS + rpp.getWins());
				player.sendMessage("---------------");
				player.sendMessage(PluginLoader.gets().SCORE + rpp.getPoints());
				player.sendMessage(PluginLoader.gets().RANK + Integer.toString(rpp.getRank()));*/
			} else
				player.sendMessage(RageMode.getLang().get("not-played-yet"));
		}

		/*
		 * public HttpResponse http(String url, String body) {
		 * 
		 * try (CloseableHttpClient httpClient =
		 * HttpClientBuilder.create().build()) { HttpPost request = new
		 * HttpPost(url); StringEntity params = new StringEntity(body);
		 * request.addHeader("content-type", "application/json");
		 * request.setEntity(params); HttpResponse result =
		 * httpClient.execute(request); String json =
		 * EntityUtils.toString(result.getEntity(), "UTF-8");
		 * 
		 * com.google.gson.Gson gson = new com.google.gson.Gson(); UUIDStrings
		 * data = gson.fromJson(json, UUIDStrings.class);
		 * Bukkit.broadcastMessage(data.id + data.name + data.legacy);
		 * this.sUUID = data.id;
		 * 
		 * } catch (IOException ex) { } return null; }
		 */
		public void https() {
			try {
				URL url = new URL("https://api.mojang.com/users/profiles/minecraft/" + name);
				URLConnection con = url.openConnection();
				InputStream in = con.getInputStream();
				BufferedReader reader = new BufferedReader(new InputStreamReader(in, "UTF8"));
				try {
					com.google.gson.Gson gson = new com.google.gson.Gson();
					UUIDStrings data = gson.fromJson(reader, UUIDStrings.class);
					if ((data.id != null) && (data.name != null)) {
						// Bukkit.broadcastMessage(data.id + data.name);
						this.sUUID = data.id;
					} else
						return;
				} catch (NullPointerException i) {
					this.sUUID = null;
				}
			} catch (Exception i) {
				i.printStackTrace();
			}
		}
	}
}
