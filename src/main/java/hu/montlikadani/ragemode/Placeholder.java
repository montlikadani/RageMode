package hu.montlikadani.ragemode;

import org.bukkit.entity.Player;

import hu.montlikadani.ragemode.runtimePP.RuntimePPManager;
import hu.montlikadani.ragemode.scores.PlayerPoints;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;

public class Placeholder extends PlaceholderExpansion {

	Placeholder() {
	}

	@Override
	public String getAuthor() {
		return RageMode.getInstance().getDescription().getAuthors().toString();
	}

	@Override
	public String getIdentifier() {
		return "ragemode";
	}

	@Override
	public String getVersion() {
		return "1.0";
	}

	@Override
	public String onPlaceholderRequest(Player p, String var) {
		if (p == null)
			return "";

		PlayerPoints rpp = RuntimePPManager.getPPForPlayer(p.getUniqueId());
		if (rpp == null)
			return "";

		switch (var.toLowerCase()) {
		case "kills":
			return Integer.toString(rpp.getKills());
		case "axe_kills":
			return Integer.toString(rpp.getAxeKills());
		case "direct_arrow_kills":
			return Integer.toString(rpp.getDirectArrowKills());
		case "explosion_kills":
			return Integer.toString(rpp.getExplosionKills());
		case "knife_kills":
			return Integer.toString(rpp.getKnifeKills());
		case "zombie_kills":
			return Integer.toString(rpp.getZombieKills());
		case "deaths":
			return Integer.toString(rpp.getDeaths());
		case "axe_deaths":
			return Integer.toString(rpp.getAxeDeaths());
		case "direct_arrow_deaths":
			return Integer.toString(rpp.getDirectArrowDeaths());
		case "explosion_deaths":
			return Integer.toString(rpp.getExplosionDeaths());
		case "knife_deaths":
			return Integer.toString(rpp.getKnifeDeaths());
		case "current_streak":
			return Integer.toString(rpp.getCurrentStreak());
		case "longest_streak":
			return Integer.toString(rpp.getLongestStreak());
		case "points":
			return Integer.toString(rpp.getPoints());
		case "games":
			return Integer.toString(rpp.getGames());
		case "wins":
			return Integer.toString(rpp.getWins());
		case "kd":
			return Double.toString(rpp.getKD());
		default:
			break;
		}

		return "";
	}
}
