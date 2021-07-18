package hu.montlikadani.ragemode;

import org.apache.commons.lang.StringUtils;
import org.bukkit.entity.Player;

import hu.montlikadani.ragemode.area.GameAreaManager;
import hu.montlikadani.ragemode.gameLogic.base.BaseGame;
import hu.montlikadani.ragemode.gameUtils.GameUtils;
import hu.montlikadani.ragemode.managers.PlayerManager;
import hu.montlikadani.ragemode.runtimePP.RuntimePPManager;
import hu.montlikadani.ragemode.scores.PlayerPoints;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;

final class Placeholder extends PlaceholderExpansion {

	Placeholder() {
	}

	public static final String IDENTIFIER = "ragemode";
	public static final String PREFIX = IDENTIFIER + '_';

	public enum RMPlaceholders {
		KILLS, AXE_KILLS, DIRECT_ARROW_KILLS, EXPLOSION_KILLS, KNIFE_KILLS, ZOMBIE_KILLS, DEATHS, AXE_DEATHS,
		DIRECT_ARROW_DEATHS, EXPLOSION_DEATHS, KNIFE_DEATHS, CURRENT_STREAK, LONGEST_STREAK, POINTS, GAMES, WINS, KD,

		PLAYER_LIVES, IS_SPECTATOR,

		STATE("game"), PLAYERS("game"), SPECTATOR_PLAYERS("game"), MAXPLAYERS("game"), ZOMBIES_ALIVE("game"),
		TYPE("game"),;

		private String requirements = "";
		private BaseGame game;

		private boolean complexed = false;

		private RMPlaceholders() {
		}

		private RMPlaceholders(String requirements) {
			this.requirements = requirements;
			complexed = !requirements.isEmpty();
		}

		public boolean isComplexed() {
			return complexed;
		}

		public String getRequirements() {
			return requirements;
		}

		public String getFullName() {
			return PREFIX + name();
		}

		public BaseGame getGame() {
			return game;
		}

		public static RMPlaceholders getByIdentifier(final String id) {
			for (RMPlaceholders holder : values()) {
				if (id.equalsIgnoreCase(holder.getFullName())) {
					return holder;
				}
			}

			for (RMPlaceholders holder : values()) {
				if (id.equalsIgnoreCase(holder.toString())) {
					return holder;
				}
			}

			String original = id;
			String contextName = id;

			for (int i = contextName.length() - 1; i > 0; i--) {
				if (contextName.charAt(i) == '_') {
					contextName = contextName.substring(i + 1);
					original = original.substring(0, i);
					break;
				}
			}

			for (RMPlaceholders holder : values()) {
				if (holder.isComplexed() && StringUtils.startsWithIgnoreCase(original, holder.toString())) {
					holder.game = GameUtils.getGame(contextName);
					return holder;
				}
			}

			return null;
		}
	}

	private final org.bukkit.plugin.Plugin rm = org.bukkit.plugin.java.JavaPlugin.getProvidingPlugin(RageMode.class);

	@Override
	public String getAuthor() {
		return String.join(", ", rm.getDescription().getAuthors());
	}

	@Override
	public String getIdentifier() {
		return IDENTIFIER;
	}

	@Override
	public String getVersion() {
		return rm.getDescription().getVersion();
	}

	@Override
	public String onPlaceholderRequest(Player p, String v) {
		if (p == null)
			return "";

		RMPlaceholders placeholder = RMPlaceholders.getByIdentifier(v);
		if (placeholder == null) {
			return "";
		}

		if (placeholder == RMPlaceholders.IS_SPECTATOR) {
			PlayerManager pm = GameUtils.getPlayerManager(p);

			return String.valueOf(pm != null && pm.isSpectator());
		}

		if (placeholder == RMPlaceholders.PLAYER_LIVES) {
			PlayerManager pm = GameUtils.getPlayerManager(p);

			return pm == null ? "0" : Integer.toString(pm.getPlayerLives());
		}

		if (placeholder.game != null) {
			switch (placeholder) {
			case STATE:
				return placeholder.game.getStatus().toString().toLowerCase();
			case PLAYERS:
				return Integer.toString(placeholder.game.getPlayers().size());
			case SPECTATOR_PLAYERS:
				return Integer.toString(placeholder.game.getSpectatorPlayers().size());
			case MAXPLAYERS:
				return Integer.toString(placeholder.game.maxPlayers);
			case ZOMBIES_ALIVE:
				hu.montlikadani.ragemode.area.GameArea area = GameAreaManager.getAreaByGame(placeholder.game);

				return area == null ? "0"
						: Integer.toString(area.getEntities(org.bukkit.entity.EntityType.ZOMBIE).size());
			case TYPE:
				return placeholder.game.getGameType().toString().toLowerCase();
			default:
				break;
			}
		}

		PlayerPoints rpp = RuntimePPManager.getPPForPlayer(p.getUniqueId());
		if (rpp != null) {
			switch (placeholder) {
			case KILLS:
				return Integer.toString(rpp.getKills());
			case AXE_KILLS:
				return Integer.toString(rpp.getAxeKills());
			case DIRECT_ARROW_KILLS:
				return Integer.toString(rpp.getDirectArrowKills());
			case EXPLOSION_KILLS:
				return Integer.toString(rpp.getExplosionKills());
			case KNIFE_KILLS:
				return Integer.toString(rpp.getKnifeKills());
			case ZOMBIE_KILLS:
				return Integer.toString(rpp.getZombieKills());
			case DEATHS:
				return Integer.toString(rpp.getDeaths());
			case AXE_DEATHS:
				return Integer.toString(rpp.getAxeDeaths());
			case DIRECT_ARROW_DEATHS:
				return Integer.toString(rpp.getDirectArrowDeaths());
			case EXPLOSION_DEATHS:
				return Integer.toString(rpp.getExplosionDeaths());
			case KNIFE_DEATHS:
				return Integer.toString(rpp.getKnifeDeaths());
			case CURRENT_STREAK:
				return Integer.toString(rpp.getCurrentStreak());
			case LONGEST_STREAK:
				return Integer.toString(rpp.getLongestStreak());
			case POINTS:
				return Integer.toString(rpp.getPoints());
			case GAMES:
				return Integer.toString(rpp.getGames());
			case WINS:
				return Integer.toString(rpp.getWins());
			case KD:
				return Double.toString(rpp.getKD());
			default:
				break;
			}
		}

		return "";
	}
}
