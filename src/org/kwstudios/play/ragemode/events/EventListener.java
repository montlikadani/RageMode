package org.kwstudios.play.ragemode.events;

import java.util.List;
import java.util.Random;

import net.minecraft.server.v1_8_R3.EntityItem;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Player;
import org.bukkit.entity.Snowball;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.kwstudios.play.ragemode.gameLogic.GameSpawnGetter;
import org.kwstudios.play.ragemode.gameLogic.PlayerList;
import org.kwstudios.play.ragemode.items.CombatAxe;
import org.kwstudios.play.ragemode.loader.PluginLoader;
import org.kwstudios.play.ragemode.toolbox.GameBroadcast;



public class EventListener implements Listener {

	public FileConfiguration fileConfiguration = null;
	
	public EventListener(PluginLoader plugin, FileConfiguration fileconfiguration) {
		plugin.getServer().getPluginManager().registerEvents(this, plugin);
		fileConfiguration = fileconfiguration;
	}

	@EventHandler
	public void onPlayerQuit(PlayerQuitEvent event) {
		Player player = event.getPlayer();

		// Bukkit.broadcastMessage("Ragemode noticed that " + player.getName() +
		// " disconnected.");

		if (PlayerList.isPlayerPlaying(event.getPlayer().getUniqueId().toString())) {
			if (PlayerList.removePlayer(player)) {
				// Bukkit.broadcastMessage(player.getName() + " was removed
				// successfully.");
			}

		}
	}

	@EventHandler(priority = EventPriority.LOWEST)
	public void onProjectileHit(ProjectileHitEvent event) {    //RageArrow explosion event
		if (event.getEntity() instanceof Arrow) {
			Arrow arrow = (Arrow) event.getEntity();
			if (arrow.getShooter() instanceof Player) {
				Player shooter = (Player) arrow.getShooter();
				if (PlayerList.isPlayerPlaying(shooter.getUniqueId().toString())) {
					Location location = arrow.getLocation();
					World world = arrow.getWorld();
					double x = location.getX();
					double y = location.getY();
					double z = location.getZ();
					
					world.createExplosion(x, y, z, 2f, false, false); //original 4f
					arrow.remove();
					//TODO check if 4f is too strong (4f is TNT strength)
				}
			}
		}
	}
	
	@EventHandler
	public void onRageKnifeHit(EntityDamageByEntityEvent event){    //RageKnife hit event
		if(event.getDamager() instanceof Player && event.getEntity() instanceof Player){
			Player killer = (Player) event.getDamager();
			Player victim = (Player) event.getEntity();
			if(PlayerList.isPlayerPlaying(killer.getUniqueId().toString()) && PlayerList.isPlayerPlaying(victim.getUniqueId().toString())){
				if(killer.getItemInHand() != null && killer.getItemInHand().getItemMeta() != null && killer.getItemInHand().getItemMeta().getDisplayName() != null) {
					if(killer.getItemInHand().getItemMeta().getDisplayName().equals(ChatColor.GOLD + "RageKnife")){
						//TODO check if "killer.getItemInHand() instanceof MATERIAL.SHEARS" also works (maybe more stable)
						event.setDamage(25);
//						TODO give the killer +15 points
					}					
				}
			}
			if(PlayerList.isPlayerPlaying(victim.getUniqueId().toString())) {
				if(!PlayerList.isGameRunning(PlayerList.getPlayersGame(victim))) {
					event.setDamage(0);
				}
			}
		}
		//TODO add Constant for "RageKnife" for unexpected error preventing
	}
	
	/* NOT WORKING
	@EventHandler(priority = EventPriority.LOWEST)
	public void onPlayerTeleport(PlayerTeleportEvent event){
		Player player = event.getPlayer();
		if(PlayerList.isPlayerPlaying(player.getUniqueId().toString())){
			player.getInventory().clear();
			player.getInventory().setItem(0, RageBow.getRageBow());		//
			player.getInventory().setItem(1, RageKnife.getRageKnife());	//	give him a new set of items
			player.getInventory().setItem(9, RageArrow.getRageArrow());	//
		}
	}*/
	
	@EventHandler(priority = EventPriority.HIGHEST)
	public void onItemSpawn(PlayerDropItemEvent event){
		Player player = event.getPlayer();
		if(PlayerList.isPlayerPlaying(player.getUniqueId().toString())){
			event.setCancelled(true);
		}
	}
	
	@EventHandler
	public void onArrowHitPlayer(EntityDamageEvent event){    //Arrow hit player event
		if(event.getEntity() instanceof Player) {
			if(event.getCause().equals(DamageCause.PROJECTILE)) {
				Player victim = (Player) event.getEntity();
				if(PlayerList.isPlayerPlaying(victim.getUniqueId().toString())){
					if(event.getDamage() == 0.0d) {
						event.setDamage(28.34d);
					}
					else {
						event.setDamage(27.114);
					}
				}
			}
			if(event.getCause().equals(DamageCause.FALL)) {
				event.setCancelled(true);
			}
		}
	}
	
	@EventHandler
	public void onPlayerDeath(PlayerDeathEvent event) {		//Player autorespawn
		if(PlayerList.isPlayerPlaying(event.getEntity().getUniqueId().toString())) {
			Player deceased = (Player) event.getEntity();
			
			if(deceased.getLastDamage() == 0.0f) {
				Bukkit.broadcastMessage(deceased.getName() + " was killed by a CombatAxe.");
//				TODO - points for vctim, + points for killer
			}
			else if(deceased.getLastDamageCause().equals(DamageCause.PROJECTILE)) {
				Bukkit.broadcastMessage(deceased.getName() + " was killed by a direct arrow hit.");	
//				TODO add points for killer
			}
			else if(deceased.getLastDamageCause().equals(DamageCause.ENTITY_ATTACK)) {
				Bukkit.broadcastMessage(deceased.getName() + " was killed by a RageKnife.");	
//				TODO add points for killer
			}
			else if(deceased.getLastDamageCause().equals(DamageCause.ENTITY_EXPLOSION)) {
				Bukkit.broadcastMessage(deceased.getName() + " was killed by a explosion.");	
//				TODO add points for killer
			}
			else {
				Bukkit.broadcastMessage(deceased.getName() + " was killed by something unexpected.");	
			}
			
			event.setKeepInventory(true);
			GameSpawnGetter gameSpawnGetter = new GameSpawnGetter(PlayerList.getPlayersGame(deceased), fileConfiguration);
			
			List<Location> spawns = gameSpawnGetter.getSpawnLocations();
			//Location[] aSpawns = (Location[]) spawns.toArray();    ----> performance optimization
			
			Random rand = new Random();
			int x = rand.nextInt(spawns.size() - 1);    //----> performance optimization

			deceased.setHealth(20);
			
			deceased.teleport(spawns.get(x));    //----> performance optimization
			
			//deceased.getInventory().clear();
			//deceased.getInventory().setItem(0, RageBow.getRageBow());		//
			//deceased.getInventory().setItem(1, RageKnife.getRageKnife());	//	give him a new set of items
			//deceased.getInventory().setItem(9, RageArrow.getRageArrow());	//
			deceased.getInventory().setItem(2, CombatAxe.getCombatAxe());

		}
	}
	
	@EventHandler
	public void onHungerGain(FoodLevelChangeEvent event) {
		if(event.getEntity() instanceof Player) {
			Player player = (Player) event.getEntity();
			if(PlayerList.isPlayerPlaying(player.getUniqueId().toString())) {
				event.setCancelled(true);
			}
		}
	}
	
	@EventHandler
	public void onBlockBreak(BlockBreakEvent event) {
		if(PlayerList.isPlayerPlaying(event.getPlayer().getUniqueId().toString())) {
			event.setCancelled(true);
		}
	}

	@EventHandler
	public void onCommand(PlayerCommandPreprocessEvent event) {
		if(PlayerList.isPlayerPlaying(event.getPlayer().getUniqueId().toString()) && !event.getPlayer().hasPermission("rm.inGameCommands")) {
			event.setCancelled(true);
		}

	}
	
	@EventHandler
	public void onCombatAxeThrow(PlayerInteractEvent event) {
		if(PlayerList.isPlayerPlaying(event.getPlayer().getUniqueId().toString())) {
			Player thrower = event.getPlayer();
			if(thrower.getItemInHand() != null && thrower.getItemInHand().getItemMeta() != null && thrower.getItemInHand().getItemMeta().getDisplayName() != null) {
				if(thrower.getItemInHand().getItemMeta().getDisplayName().equals(ChatColor.GOLD + "CombatAxe")) {
					thrower.launchProjectile(Snowball.class);
					thrower.getInventory().setItemInHand(null);
				}
			}
		}
	}
	
	

	// @EventHandler
	// public void onPlayerDeath(PlayerDeathEvent event) {
	// event.getEntity().getLastDamageCause().
	//
	// }
}
