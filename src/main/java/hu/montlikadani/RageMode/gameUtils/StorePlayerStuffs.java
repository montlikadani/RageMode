package hu.montlikadani.ragemode.gameUtils;

import java.util.Collection;

import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;

public class StorePlayerStuffs {

	public Location oldLocation;
	public ItemStack[] oldInventories;
	public ItemStack[] oldArmor;
	public Double oldHealth = 0d;
	public Integer oldHunger = Integer.valueOf(0);
	public Collection<PotionEffect> oldEffects;
	public GameMode oldGameMode = GameMode.SURVIVAL;
	public String oldDisplayName;
	public String oldListName;
	public Integer oldFire = Integer.valueOf(0);
	public Float oldExp = 0f;
	public Integer oldExpLevel = Integer.valueOf(0);
	public Entity oldVehicle;

	// Spectator datas
	public ItemStack[] inventory;
	public ItemStack[] armor;
	public Location loc;
	public GameMode gMode = GameMode.SURVIVAL;
	public boolean fly = false;
	public boolean allowFly = false;
}
