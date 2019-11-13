package hu.montlikadani.ragemode.gameUtils;

import org.bukkit.entity.Player;

public interface IObjectives {

	boolean addToList(String gameName, boolean forceReplace);

	void remove(Player p);

	void remove();
}
