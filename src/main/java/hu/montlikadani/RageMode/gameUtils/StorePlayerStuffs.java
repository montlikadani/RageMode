package hu.montlikadani.ragemode.gameUtils;

import java.util.Collection;

import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;

public class StorePlayerStuffs {

	public Double oldHealth = 0d;
	public Integer oldHunger = Integer.valueOf(0);
	public Collection<PotionEffect> oldEffects;
	public String oldDisplayName;
	public String oldListName;
	public Integer oldFire = Integer.valueOf(0);
	public Float oldExp = 0f;
	public Integer oldExpLevel = Integer.valueOf(0);
	public Entity oldVehicle;

	// Spectator datas
	public boolean fly = false;
	public boolean allowFly = false;

	// Both
	public Location oldLocation;
	public ItemStack[] oldInventories;
	public ItemStack[] oldArmor;
	public GameMode oldGameMode = GameMode.SURVIVAL;
}
