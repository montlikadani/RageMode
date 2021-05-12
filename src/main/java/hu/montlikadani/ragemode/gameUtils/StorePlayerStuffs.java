package hu.montlikadani.ragemode.gameUtils;

import org.bukkit.GameMode;

public final class StorePlayerStuffs {

	public java.util.Collection<org.bukkit.potion.PotionEffect> oldEffects;

	public String oldDisplayName, oldListName;

	public double oldHealth = 0d;
	public int oldFire = 0, oldHunger = 0, oldExpLevel = 0;
	public float oldExp = 0f;

	public org.bukkit.entity.Entity oldVehicle;
	public org.bukkit.scoreboard.Scoreboard currentBoard;

	// Spectator
	public boolean fly = false, allowFly = false;

	// Both
	public org.bukkit.Location oldLocation;
	public org.bukkit.inventory.ItemStack[] oldInventories, oldArmor;
	public GameMode oldGameMode = GameMode.SURVIVAL;
}
