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

		// Player kill stats
		if (var.equals("kills"))
			return Integer.toString(rpp.getKills());

		if (var.equals("axe_kills"))
			return Integer.toString(rpp.getAxeKills());

		if (var.equals("direct_arrow_kills"))
			return Integer.toString(rpp.getDirectArrowKills());

		if (var.equals("explosion_kills"))
			return Integer.toString(rpp.getExplosionKills());

		if (var.equals("knife_kills"))
			return Integer.toString(rpp.getKnifeKills());

		// Player death stats
		if (var.equals("deaths"))
			return Integer.toString(rpp.getDeaths());

		if (var.equals("axe_deaths"))
			return Integer.toString(rpp.getAxeDeaths());

		if (var.equals("direct_arrow_deaths"))
			return Integer.toString(rpp.getDirectArrowDeaths());

		if (var.equals("explosion_deaths"))
			return Integer.toString(rpp.getExplosionDeaths());

		if (var.equals("knife_deaths"))
			return Integer.toString(rpp.getKnifeDeaths());

		// Other stats
		if (var.equals("current_streak"))
			return Integer.toString(rpp.getCurrentStreak());

		if (var.equals("longest_streak"))
			return Integer.toString(rpp.getLongestStreak());

		if (var.equals("points"))
			return Integer.toString(rpp.getPoints());

		if (var.equals("games"))
			return Integer.toString(rpp.getGames());

		if (var.equals("wins"))
			return Integer.toString(rpp.getWins());

		if (var.equals("kd"))
			return Double.toString(rpp.getKD());

		if (var.equals("rank"))
			return Integer.toString(rpp.getRank());

		return "";
	}
}
