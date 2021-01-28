package hu.montlikadani.ragemode.gameUtils;

import java.util.Collection;

import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.scoreboard.Scoreboard;

public class StorePlayerStuffs {

	public Double oldHealth = 0d;
	public Collection<PotionEffect> oldEffects;
	public String oldDisplayName;
	public String oldListName;
	public Integer oldFire = Integer.valueOf(0),
			oldHunger = Integer.valueOf(0),
			oldExpLevel = Integer.valueOf(0);
	public Float oldExp = 0f;
	public Entity oldVehicle;
	public Scoreboard currentBoard;

	// Spectator
	public boolean fly = false;
	public boolean allowFly = false;

	// Both
	public Location oldLocation;
	public ItemStack[] oldInventories;
	public ItemStack[] oldArmor;
	public GameMode oldGameMode = GameMode.SURVIVAL;
}
