package hu.montlikadani.ragemode.events;

import static hu.montlikadani.ragemode.utils.Misc.hasPerm;
import static hu.montlikadani.ragemode.utils.Misc.sendMessage;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

import org.bukkit.Bukkit;
import org.bukkit.Effect;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Egg;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.entity.Snowball;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockGrowEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.entity.EntityInteractEvent;
import org.bukkit.event.entity.EntityShootBowEvent;
import org.bukkit.event.entity.ExplosionPrimeEvent;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.entity.ProjectileLaunchEvent;
import org.bukkit.event.hanging.HangingBreakByEntityEvent;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerBedEnterEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerEggThrowEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.event.player.PlayerToggleFlightEvent;
import org.bukkit.event.vehicle.VehicleEnterEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.metadata.MetadataValue;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import hu.montlikadani.ragemode.NMS;
import hu.montlikadani.ragemode.RageMode;
import hu.montlikadani.ragemode.ServerSoftwareType;
import hu.montlikadani.ragemode.ServerVersion.Version;
import hu.montlikadani.ragemode.area.GameAreaManager;
import hu.montlikadani.ragemode.Utils;
import hu.montlikadani.ragemode.API.event.RMGameLeaveAttemptEvent;
import hu.montlikadani.ragemode.API.event.RMGameStopEvent;
import hu.montlikadani.ragemode.API.event.RMPlayerKilledEvent;
import hu.montlikadani.ragemode.API.event.RMPlayerPreRespawnEvent;
import hu.montlikadani.ragemode.API.event.RMPlayerRespawnedEvent;
import hu.montlikadani.ragemode.config.ConfigValues;
import hu.montlikadani.ragemode.gameLogic.Game;
import hu.montlikadani.ragemode.gameLogic.GameStatus;
import hu.montlikadani.ragemode.gameLogic.IGameSpawn;
import hu.montlikadani.ragemode.gameUtils.AntiCheatChecks;
import hu.montlikadani.ragemode.gameUtils.GameType;
import hu.montlikadani.ragemode.gameUtils.GameUtils;
import hu.montlikadani.ragemode.items.ItemHandler;
import hu.montlikadani.ragemode.items.Items;
import hu.montlikadani.ragemode.items.handler.PressureMine;
import hu.montlikadani.ragemode.items.shop.IShop;
import hu.montlikadani.ragemode.items.shop.LobbyShop;
import hu.montlikadani.ragemode.items.threads.CombatAxeThread;
import hu.montlikadani.ragemode.libs.Sounds;
import hu.montlikadani.ragemode.scores.KilledWith;
import hu.montlikadani.ragemode.scores.RageScores;
import hu.montlikadani.ragemode.utils.MaterialUtil;

public class GameListener implements Listener {

	private RageMode plugin;

	private final LobbyShop lobbyShop = new LobbyShop();

	private final Map<UUID, UUID> explosionVictims = new HashMap<>(), grenadeExplosionVictims = new HashMap<>();

	public static final Set<PressureMine> PRESSUREMINES = new HashSet<>();

	public GameListener(RageMode plugin) {
		this.plugin = plugin;
	}

	@EventHandler
	public void onGameLeave(RMGameLeaveAttemptEvent e) {
		Player player = e.getPlayer();

		if (LobbyShop.USERPARTICLES.containsKey(player.getUniqueId())) {
			LobbyShop.USERPARTICLES.remove(player.getUniqueId());
		}

		for (java.util.Iterator<PressureMine> it = PRESSUREMINES.iterator(); it.hasNext();) {
			PressureMine mine = it.next();
			if (mine.getPlayerUUID().equals(player.getUniqueId())) {
				mine.removeMines();
				it.remove();
				break;
			}
		}
	}

	@EventHandler
	public void onGameStop(RMGameStopEvent event) {
		PRESSUREMINES.forEach(PressureMine::removeMines);
		PRESSUREMINES.clear();

		Game game = event.getGame();
		if (game.getGameType() == GameType.APOCALYPSE && !game.getPlayersFromList().isEmpty()) {
			game.getPlayersFromList().get(0).getPlayer().getWorld().setTime(game.worldTime);
		}
	}

	@EventHandler(priority = EventPriority.MONITOR)
	public void onPlayerChatFormat(AsyncPlayerChatEvent event) {
		Player p = event.getPlayer();

		// Cancels the spectator player chat
		if (GameUtils.isSpectatorPlaying(p)) {
			event.setCancelled(true);
			return;
		}

		if (!GameUtils.isPlayerPlaying(p)) {
			return;
		}

		Game game = GameUtils.getGameByPlayer(p);
		GameStatus status = game.getStatus();

		if (status == GameStatus.WAITING && !ConfigValues.isChatEnabledinLobby()
				&& !hasPerm(p, "ragemode.bypass.lobby.lockchat")) {
			event.setCancelled(true);
			sendMessage(p, RageMode.getLang().get("game.lobby.chat-is-disabled"));
			return;
		}

		if (status == GameStatus.RUNNING) {
			if (!ConfigValues.isEnableChatInGame() && !hasPerm(p, "ragemode.bypass.game.lockchat")) {
				sendMessage(p, RageMode.getLang().get("game.chat-is-disabled"));
				event.setCancelled(true);
				return;
			}

			if (ConfigValues.isChatFormatEnabled()) {
				String format = ConfigValues.getChatFormat();
				format = format.replace("%player%", p.getName());
				format = format.replace("%player-displayname%", p.getDisplayName());
				format = format.replace("%game%", game.getName());
				format = format.replace("%online-ingame-players%", Integer.toString(game.getPlayers().size()));
				format = format.replace("%message%", event.getMessage());
				format = Utils.setPlaceholders(format, p);

				event.setFormat(Utils.colors(format));
			}
		}

		if (!ConfigValues.isEnableChatAfterEnd() && status == GameStatus.GAMEFREEZE) {
			sendMessage(p, RageMode.getLang().get("game.game-freeze.chat-is-disabled"));
			event.setCancelled(true);
		}
	}

	@EventHandler(priority = EventPriority.LOWEST)
	public void onProjectileHit(ProjectileHitEvent event) {
		Projectile proj = event.getEntity();
		if (!(proj.getShooter() instanceof Player) || !GameUtils.isPlayerPlaying((Player) proj.getShooter())) {
			return;
		}

		Player shooter = (Player) proj.getShooter();
		if (shooter == null) {
			return;
		}

		// flash event
		if (proj instanceof Snowball && (!GameUtils.isPlayerInFreezeRoom(shooter)
				|| GameUtils.getGameByPlayer(shooter).getGameType() != GameType.APOCALYPSE)) {
			final Item flash = proj.getWorld().dropItem(proj.getLocation(), new ItemStack(
					Version.isCurrentLower(Version.v1_9_R1) ? Material.getMaterial("SNOW_BALL") : Material.SNOWBALL));
			flash.setPickupDelay(41);
			flash.setVelocity(shooter.getEyeLocation().getDirection());
			flash.getLocation().add(proj.getVelocity());

			Bukkit.getScheduler().runTaskLater(plugin, cons -> {
				if (flash.isDead()) {
					return;
				}

				if (!GameUtils.isPlayerPlaying(shooter) || GameUtils.isPlayerInFreezeRoom(shooter)) {
					flash.remove();
					return;
				}

				for (Entity entity : Utils.getNearbyEntities(flash, 6)) {
					if (entity instanceof Player) {
						((Player) entity).addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, 60, 2));
					}
				}

				if (Version.isCurrentEqualOrHigher(Version.v1_9_R1)) {
					proj.getWorld().spawnParticle(org.bukkit.Particle.EXPLOSION_LARGE, proj.getLocation(), 0);
				}

				flash.remove();
			}, 40);
		} else if (proj instanceof Arrow) { // RageArrow explosion event
			Arrow arrow = (Arrow) proj;
			if (arrow.getShooter() == null) {
				return;
			}

			Game game = GameUtils.getGameByPlayer((Player) arrow.getShooter());
			GameStatus status = game.getStatus();

			if (status == GameStatus.GAMEFREEZE && GameUtils.isGameInFreezeRoom(game)) {
				arrow.remove();
				return;
			}

			if (status == GameStatus.RUNNING) {
				Location location = arrow.getLocation();
				if (!GameAreaManager.inArea(location)) {
					arrow.remove();
					return;
				}

				double x = location.getX(), y = location.getY(), z = location.getZ();

				List<Entity> nears = Utils.getNearbyEntities(arrow, 10);

				arrow.getWorld().createExplosion(x, y, z, 2f, false, false);
				arrow.remove();

				int i = 0;
				int imax = nears.size();
				while (i < imax) {
					Entity near = nears.get(i);

					if (explosionVictims.containsKey(near.getUniqueId())) {
						explosionVictims.remove(near.getUniqueId());
					}
					explosionVictims.put(near.getUniqueId(), ((Player) arrow.getShooter()).getUniqueId());

					if (nears.get(i) instanceof Player) {
						near.removeMetadata("killedWith", plugin);
						near.setMetadata("killedWith", new FixedMetadataValue(plugin, "explosion"));
					}

					i++;
				}
			}
		}
	}

	@EventHandler(priority = EventPriority.HIGHEST)
	public void onHit(EntityDamageByEntityEvent event) {
		if (GameUtils.getGame(event.getEntity().getLocation()) != null
				&& GameUtils.getGame(event.getEntity().getLocation()).getGameType() == GameType.APOCALYPSE) {
			// Players do not hurt players, because friendship
			if (event.getEntity() instanceof Player && event.getDamager() instanceof Player) {
				event.setCancelled(true);
				return;
			}

			double finalDamage = 0d;

			if (event.getDamager() instanceof Player) {
				Player damager = (Player) event.getDamager();

				if (GameUtils.isPlayerPlaying(damager)) {
					ItemMeta meta = NMS.getItemInHand(damager).getItemMeta();
					ItemHandler knife = Items.getRageKnife();
					if (knife != null && meta != null && meta.hasDisplayName()
							&& meta.getDisplayName().equals(knife.getDisplayName())) {
						finalDamage = knife.getDamage();
					}
				}
			} else if (event.getDamager() instanceof org.bukkit.entity.Egg && Items.getGrenade() != null) {
				finalDamage = Items.getGrenade().getDamage();
			} else if (event.getDamager() instanceof Arrow && Items.getRageArrow() != null) {
				finalDamage = Items.getRageArrow().getDamage();
			}

			if (finalDamage > 0d) {
				event.setDamage(finalDamage);
			}
		} else {
			if (!(event.getEntity() instanceof Player)) {
				return;
			}

			Player victim = (Player) event.getEntity();
			if (!GameUtils.isPlayerPlaying(victim)) {
				return;
			}

			Game victimGame = GameUtils.getGameByPlayer(victim);
			// Prevent player damage in lobby
			if (victimGame.getStatus() == GameStatus.WAITING) {
				event.setCancelled(true);
				return;
			}

			if (event.getDamager() instanceof Player) {
				Player damager = (Player) event.getDamager();
				if (!GameUtils.isPlayerPlaying(damager)) {
					return; // Preventing non-playing players damaging to playing players
				}

				if (GameUtils.getGameByPlayer(damager).getStatus() == GameStatus.GAMEFREEZE
						&& GameUtils.isPlayerInFreezeRoom(damager)) {
					event.setCancelled(true);
					return;
				}
			}

			if (victimGame.getStatus() != GameStatus.RUNNING) {
				return;
			}

			double finalDamage = 0d;
			String tool = "";

			if (event.getDamager() instanceof Player) {
				Player damager = (Player) event.getDamager();

				if (GameUtils.isPlayerPlaying(damager)) {
					ItemMeta meta = NMS.getItemInHand(damager).getItemMeta();
					ItemHandler knife = Items.getRageKnife();
					if (knife != null && meta != null && meta.getDisplayName().equals(knife.getDisplayName())) {
						finalDamage = knife.getDamage();
						tool = "knife";
					}
				}
			} else if (event.getDamager() instanceof org.bukkit.entity.Egg) {
				finalDamage = Items.getGrenade() != null ? Items.getGrenade().getDamage() : 2.20;
				tool = "grenade";
			} else if (event.getDamager() instanceof Arrow) {
				finalDamage = Items.getRageArrow() != null ? Items.getRageArrow().getDamage() : 3.35;
				tool = "arrow";
			}

			if (finalDamage <= 0d) {
				return;
			}

			event.setDamage(finalDamage);

			if (!tool.isEmpty()) {
				victim.removeMetadata("killedWith", plugin);
				victim.setMetadata("killedWith", new FixedMetadataValue(plugin, tool));
			}
		}
	}

	@EventHandler
	public void onHitPlayer(EntityDamageEvent event) {
		Entity e = event.getEntity();
		if (!(e instanceof Player)) {
			return;
		}

		Player victim = (Player) e;
		if (!GameUtils.isPlayerPlaying(victim)) {
			return;
		}

		Game game = GameUtils.getGameByPlayer(victim);

		// Hit player event
		if (game.getStatus() == GameStatus.RUNNING) {
			if (event.getCause() == DamageCause.FALL && !ConfigValues.isDamagePlayerFall()) {
				event.setCancelled(true);
			}
		} else if ((game.getStatus() == GameStatus.GAMEFREEZE && GameUtils.isGameInFreezeRoom(game))
				|| game.getStatus() == GameStatus.WAITING) {
			event.setCancelled(true); // Prevent damage in lobby or game freeze
		}
	}

	@EventHandler
	public void onBowShoot(EntityShootBowEvent e) {
		if (!(e.getEntity() instanceof Player)) {
			return;
		}

		Player p = (Player) e.getEntity();
		if (GameUtils.isPlayerInFreezeRoom(p)) {
			e.setCancelled(true);
		}
	}

	@EventHandler(priority = EventPriority.HIGH)
	public void onPlayerDeath(PlayerDeathEvent event) {
		Player deceased = event.getEntity();
		if (!GameUtils.isPlayerPlaying(deceased)) {
			return;
		}

		Game deceasedGame = GameUtils.getGameByPlayer(deceased);
		if (deceasedGame.getStatus() != GameStatus.RUNNING) {
			return;
		}

		boolean killerExists = deceased.getKiller() != null;

		if ((killerExists && GameUtils.isPlayerPlaying(deceased.getKiller())) || !killerExists) {
			if (deceasedGame.getGameType() != GameType.APOCALYPSE) {
				boolean doDeathBroadcast = ConfigValues.isDefaultDeathMessageEnabled();

				if (plugin.getConfiguration().getArenasCfg()
						.isSet("arenas." + deceasedGame.getName() + ".death-messages")) {
					String gameBroadcast = plugin.getConfiguration().getArenasCfg()
							.getString("arenas." + deceasedGame.getName() + ".death-messages", "");
					if (gameBroadcast.equals("true") || gameBroadcast.equals("false")) {
						doDeathBroadcast = Boolean.parseBoolean(gameBroadcast);
					}
				}

				String message = "";
				String deceaseName = deceased.getName();
				List<MetadataValue> data = deceased.getMetadata("killedWith");
				RMPlayerKilledEvent killed = null;

				if (!data.isEmpty()) {
					switch (data.get(0).asString()) {
					case "arrow":
						if (!killerExists) {
							message = RageMode.getLang().get("game.broadcast.arrow-kill", "%victim%", deceaseName,
									"%killer%", deceaseName);

							RageScores.addPointsToPlayer(deceased, deceased, KilledWith.RAGEBOW);
						} else {
							message = RageMode.getLang().get("game.broadcast.arrow-kill", "%victim%", deceaseName,
									"%killer%", deceased.getKiller().getName());

							RageScores.addPointsToPlayer(deceased.getKiller(), deceased, KilledWith.RAGEBOW);
						}

						killed = new RMPlayerKilledEvent(deceasedGame, deceased, deceased.getKiller(),
								KilledWith.RAGEBOW);
						break;
					case "combataxe":
						if (!killerExists) {
							message = RageMode.getLang().get("game.broadcast.axe-kill", "%victim%", deceaseName,
									"%killer%", deceaseName);

							RageScores.addPointsToPlayer(deceased, deceased, KilledWith.COMBATAXE);
						} else {
							message = RageMode.getLang().get("game.broadcast.axe-kill", "%victim%", deceaseName,
									"%killer%", deceased.getKiller().getName());

							RageScores.addPointsToPlayer(deceased.getKiller(), deceased, KilledWith.COMBATAXE);
						}

						killed = new RMPlayerKilledEvent(deceasedGame, deceased, deceased.getKiller(),
								KilledWith.COMBATAXE);
						break;
					case "explosion":
						if (explosionVictims.containsKey(deceased.getUniqueId())) {
							Player v = Bukkit.getPlayer(explosionVictims.get(deceased.getUniqueId()));
							if (v != null) {
								message = RageMode.getLang().get("game.broadcast.explosion-kill", "%victim%",
										deceaseName, "%killer%", v.getName());

								RageScores.addPointsToPlayer(v, deceased, KilledWith.EXPLOSION);
							}
						} else if (grenadeExplosionVictims.containsKey(deceased.getUniqueId())) {
							Player vic = Bukkit.getPlayer(grenadeExplosionVictims.get(deceased.getUniqueId()));
							if (vic != null) {
								message = RageMode.getLang().get("game.broadcast.explosion-kill", "%victim%",
										deceaseName, "%killer%", vic.getName());

								RageScores.addPointsToPlayer(vic, deceased, KilledWith.EXPLOSION);
							}
						} else {
							message = RageMode.getLang().get("game.broadcast.error-kill");

							sendMessage(deceased, RageMode.getLang().get("game.unknown-killer"));
						}

						killed = new RMPlayerKilledEvent(deceasedGame, deceased, deceased.getKiller(),
								KilledWith.EXPLOSION);
						break;
					case "grenade":
						if (!killerExists) {
							message = RageMode.getLang().get("game.broadcast.grenade-kill", "%victim%", deceaseName,
									"%killer%", deceaseName);

							RageScores.addPointsToPlayer(deceased, deceased, KilledWith.GRENADE);
						} else {
							message = RageMode.getLang().get("game.broadcast.grenade-kill", "%victim%", deceaseName,
									"%killer%", deceased.getKiller().getName());

							RageScores.addPointsToPlayer(deceased.getKiller(), deceased, KilledWith.GRENADE);
						}

						killed = new RMPlayerKilledEvent(deceasedGame, deceased, deceased.getKiller(),
								KilledWith.GRENADE);
						break;
					case "knife":
						if (!killerExists) {
							message = RageMode.getLang().get("game.broadcast.knife-kill", "%victim%", deceaseName,
									"%killer%", deceaseName);

							RageScores.addPointsToPlayer(deceased, deceased, KilledWith.RAGEKNIFE);
						} else {
							message = RageMode.getLang().get("game.broadcast.knife-kill", "%victim%", deceaseName,
									"%killer%", deceased.getKiller().getName());

							RageScores.addPointsToPlayer(deceased.getKiller(), deceased, KilledWith.RAGEKNIFE);
						}

						killed = new RMPlayerKilledEvent(deceasedGame, deceased, deceased.getKiller(),
								KilledWith.RAGEKNIFE);
						break;
					default:
						message = RageMode.getLang().get("game.unknown-weapon", "%victim%", deceaseName);
						break;
					}

					if (doDeathBroadcast && !message.isEmpty()) {
						GameUtils.broadcastToGame(deceasedGame, message);
					}

					deceased.removeMetadata("killedWith", plugin);
					if (killed != null) {
						Utils.callEvent(killed);
					}
				}
			} else {
				deceasedGame.getPlayerManager(deceased).ifPresent(pl -> {
					if (pl.canRespawn()) {
						pl.setPlayerLives(pl.getPlayerLives() + 1);
					}
				});
			}

			event.setDroppedExp(0);
			event.setDeathMessage("");
			event.setKeepInventory(true);
			event.getDrops().clear();

			GameUtils.runCommands(deceased, deceasedGame.getName(), "death");
			if (killerExists) {
				hu.montlikadani.ragemode.gameLogic.Bonus.addKillBonus(deceased.getKiller());
			}
		}

		// respawn player instantly
		if (RageMode.getSoftwareType() != ServerSoftwareType.UNKNOWN) {
			Bukkit.getScheduler().runTaskLater(plugin, deceased.spigot()::respawn, 1L);
		}

		GameUtils.addGameItems(deceased, true);
	}

	@EventHandler
	public void onMobDeath(EntityDeathEvent event) {
		org.bukkit.entity.LivingEntity entity = event.getEntity();
		if (entity instanceof Player) {
			return;
		}

		Game game = GameUtils.getGame(entity.getLocation());
		if (game != null && game.getGameType() == GameType.APOCALYPSE && game.getStatus() == GameStatus.RUNNING) {
			event.setDroppedExp(0);
			event.getDrops().clear();

			Player killer = entity.getKiller();
			UUID entityUUID = entity.getUniqueId();
			if ((killer != null || explosionVictims.containsKey(entityUUID)
					|| grenadeExplosionVictims.containsKey(entityUUID)) && entity instanceof org.bukkit.entity.Zombie) {
				if (killer == null) {
					killer = Bukkit.getPlayer(explosionVictims.get(entityUUID));
				}

				if (killer == null) {
					killer = Bukkit.getPlayer(grenadeExplosionVictims.get(entityUUID));
				}

				if (killer != null && killer instanceof Player) {
					RageScores.addPointsToPlayer(killer, entity);

					RMPlayerKilledEvent killed = new RMPlayerKilledEvent(game, entity, killer);
					Utils.callEvent(killed);
				}
			}
		}
	}

	@EventHandler
	public void onEntityExplode(EntityExplodeEvent ev) {
		if (GameAreaManager.inArea(ev.getLocation())) {
			ev.setCancelled(true);
		}
	}

	@EventHandler
	public void onEntitySpawn(CreatureSpawnEvent e) {
		if (e.getSpawnReason() != CreatureSpawnEvent.SpawnReason.CUSTOM && GameAreaManager.inArea(e.getLocation())) {
			e.setCancelled(true);
		}
	}

	// The event priority should be HIGHEST to prevent overwriting
	// spawns over plugins
	@EventHandler(priority = EventPriority.HIGHEST)
	public void onRespawn(PlayerRespawnEvent e) {
		final Player p = e.getPlayer();

		if (!GameUtils.isPlayerPlaying(p)) {
			return;
		}

		Game game = GameUtils.getGameByPlayer(p);

		// If player still is in game with respawn screen then remove player
		// 1.14 issue
		if (!game.isGameRunning()) {
			game.removePlayer(p);
			return;
		}

		RMPlayerPreRespawnEvent preRespawn = new RMPlayerPreRespawnEvent(game, p);
		Utils.callEvent(preRespawn);
		if (preRespawn.isCancelled()) {
			return;
		}

		IGameSpawn gsg = GameUtils.getGameSpawn(game.getName());
		if (!gsg.haveAnySpawn()) {
			return;
		}

		// Checks if the player can respawn again in apocalypse
		if (game.getGameType() == GameType.APOCALYPSE && game.getPlayerManager(p).isPresent()
				&& !game.getPlayerManager(p).get().canRespawn()) {
			sendMessage(p,
					RageMode.getLang().get("game.killed-by-zombies", "%amount%", ConfigValues.getPlayerLives() + 1));

			// Switching to spectate
			if (ConfigValues.isSpectatorEnabled() && game.removePlayer(p, true)) {
				e.setRespawnLocation(gsg.getRandomSpawn());

				if (Items.getLeaveGameItem() != null) {
					p.getInventory().setItem(Items.getLeaveGameItem().getSlot(), Items.getLeaveGameItem().build());
				}

				// delay to avoid async catch
				Bukkit.getScheduler().scheduleSyncDelayedTask(plugin,
						() -> p.setGameMode(org.bukkit.GameMode.SPECTATOR), 5L);
			} else {
				game.removePlayer(p);
			}

			return;
		}

		Location randomSpawn = gsg.getRandomSpawn();
		/*if (game.getGameType() == GameType.APOCALYPSE) {
			int amountEntities = 0;

			// simulate the respawn from monsters
			for (Entity nearby : Utils.getNearbyEntities(randomSpawn.getWorld(), randomSpawn, 15)) {
				if (!(nearby instanceof Player)) {
					amountEntities++;

					// farther from the monsters to respawn the player
					if (amountEntities > 4) {
						randomSpawn.setX(randomSpawn.getX() + 8);
						randomSpawn.setZ(randomSpawn.getZ() + 8);
						break; // we don't need anymore to check for nearby entities
					}
				}
			}
		}*/

		e.setRespawnLocation(randomSpawn);

		int time = ConfigValues.getRespawnProtectTime();
		if (time > 0) {
			p.setNoDamageTicks(time * 20);
		}

		Utils.callEvent(new RMPlayerRespawnedEvent(game, p, gsg));
	}

	@EventHandler
	public void onHungerGain(FoodLevelChangeEvent event) {
		if (event.getEntity() instanceof Player && GameUtils.isPlayerPlaying((Player) event.getEntity()))
			event.setCancelled(true);
	}

	@EventHandler
	public void onBlockPlace(BlockPlaceEvent ev) {
		Player p = ev.getPlayer();

		if (!GameUtils.isPlayerPlaying(p)) {
			return;
		}

		if (GameUtils.isPlayerInFreezeRoom(p)) {
			ev.setCancelled(true);
			return;
		}

		PressureMine pressureMine = null;
		for (PressureMine mine : PRESSUREMINES) {
			if (mine.getPlayerUUID().equals(p.getUniqueId())) {
				pressureMine = mine;
				break;
			}
		}

		if (pressureMine == null) {
			pressureMine = new PressureMine(p.getUniqueId());
		}

		if (pressureMine.addMine(ev.getBlock(), ev.getBlock().getLocation())) {
			if (!PRESSUREMINES.contains(pressureMine)) {
				PRESSUREMINES.add(pressureMine);
			}

			return;
		}

		ev.setCancelled(true);
	}

	@EventHandler(priority = EventPriority.HIGHEST)
	public void onBlockBreak(BlockBreakEvent event) {
		if (GameUtils.isPlayerPlaying(event.getPlayer())) {
			if (GameUtils.isPlayerInFreezeRoom(event.getPlayer())) {
				event.setCancelled(true);
				return;
			}

			if (explodeMine(event.getPlayer(), event.getBlock().getLocation())) {
				return;
			}

			event.setCancelled(true);
		}
	}

	@EventHandler(priority = EventPriority.HIGHEST)
	public void onItemDrop(PlayerDropItemEvent event) {
		if (GameUtils.isPlayerPlaying(event.getPlayer()))
			event.setCancelled(true);
	}

	@EventHandler
	public void disableCommand(PlayerCommandPreprocessEvent event) {
		Player p = event.getPlayer();
		String arg = event.getMessage().trim().toLowerCase();

		if (ConfigValues.isSpectatorEnabled() && GameUtils.isSpectatorPlaying(p)) {
			if (!ConfigValues.getAllowedSpectatorCommands().contains(arg)
					&& !hasPerm(p, "ragemode.bypass.spectatorcommands")) {
				sendMessage(p, RageMode.getLang().get("game.this-command-is-disabled-in-game"));
				event.setCancelled(true);
				return;
			}
		}

		if (GameUtils.isPlayerPlaying(p)) {
			if (ConfigValues.isCommandsDisabledInEndGame() && GameUtils.isPlayerInFreezeRoom(p)) {
				sendMessage(p, RageMode.getLang().get("game.command-disabled-in-end-game"));
				event.setCancelled(true);
				return;
			}

			if (!ConfigValues.getAllowedInGameCommands().contains(arg)
					&& !hasPerm(p, "ragemode.bypass.disabledcommands")) {
				sendMessage(p, RageMode.getLang().get("game.this-command-is-disabled-in-game"));
				event.setCancelled(true);
			}
		}
	}

	@EventHandler
	public void onProjectileLaunch(ProjectileLaunchEvent ev) {
		Projectile proj = ev.getEntity();
		if (!(proj.getShooter() instanceof Player)) {
			return;
		}

		Player shooter = (Player) proj.getShooter();
		if (!GameUtils.isPlayerPlaying(shooter)) {
			return;
		}

		if (GameUtils.isPlayerInFreezeRoom(shooter)) {
			ev.setCancelled(true);
			return;
		}

		if (proj instanceof Arrow && ConfigValues.isPreventFastBowEvent() && AntiCheatChecks.checkBowShoot(shooter)) {
			ev.setCancelled(true);
			return;
		}

		setTrails(proj);
	}

	@EventHandler
	public void eggThrow(PlayerEggThrowEvent event) {
		final Player p = event.getPlayer();
		if (!p.isOnline() || !GameUtils.isPlayerPlaying(p)) {
			return;
		}

		final Game game = GameUtils.getGameByPlayer(p);
		if (game.getStatus() != GameStatus.RUNNING && game.getStatus() != GameStatus.GAMEFREEZE) {
			return;
		}

		// no baby chickens
		event.setHatching(false);

		// check if player is in waiting game (game freeze)
		if (GameUtils.isPlayerInFreezeRoom(p)) {
			return;
		}

		if (!GameAreaManager.inArea(p.getLocation())) {
			return;
		}

		final Item grenade = p.getWorld().dropItem(event.getEgg().getLocation(), new ItemStack(Material.EGG));

		// Cancel egg pick up
		grenade.setPickupDelay(41);

		// make custom name
		if (Items.getGrenade() != null && !Items.getGrenade().getCustomName().isEmpty()) {
			grenade.setCustomName(Items.getGrenade().getCustomName());
			grenade.setCustomNameVisible(true);
		}

		grenade.setVelocity(p.getEyeLocation().getDirection());
		// move egg from land location to simulate bounce
		grenade.getLocation().add(event.getEgg().getVelocity());

		final List<Entity> nears = Utils.getNearbyEntities(grenade, 13);
		final Location loc = grenade.getLocation();

		Sounds.playSound(loc,
				Version.isCurrentEqualOrLower(Version.v1_8_R3) ? Sounds.CREEPER_HISS : Sounds.ENTITY_CREEPER_PRIMED, 1,
				1);

		Bukkit.getScheduler().runTaskLater(plugin, cons -> {
			// Remove egg when the player is not in-game or is in freeze room.
			if (!GameUtils.isPlayerPlaying(p) || GameUtils.isPlayerInFreezeRoom(p)) {
				grenade.remove();
				return;
			}

			double gX = loc.getX(),
					gY = loc.getY(),
					gZ = loc.getZ();

			grenade.getWorld().createExplosion(gX, gY, gZ, 3f, false, false);
			grenade.remove(); // get rid of egg so it can't be picked up

			int i = 0;
			int imax = nears.size();
			while (i < imax) {
				Entity near = nears.get(i);

				if (grenadeExplosionVictims.containsKey(near.getUniqueId())) {
					grenadeExplosionVictims.remove(near.getUniqueId());
				}
				grenadeExplosionVictims.put(near.getUniqueId(), p.getUniqueId());

				if (near instanceof Player) {
					near.removeMetadata("killedWith", plugin);
					near.setMetadata("killedWith", new FixedMetadataValue(plugin, "explosion"));
				}

				i++;
			}
		}, 40);
	}

	@EventHandler
	public void onInteract(PlayerInteractEvent event) {
		final Player p = event.getPlayer();

		if (!GameUtils.isPlayerPlaying(p) || GameUtils.isPlayerInFreezeRoom(p)) {
			return;
		}

		Action action = event.getAction();
		ItemStack hand = NMS.getItemInHand(p);

		/**
		 * Cancels the rage knife usage, for example with collecting beehives
		 * 
		 * For this we don't need to check all game items, because there is no
		 * possibility to change the item types from config.
		 */
		if (action == Action.RIGHT_CLICK_BLOCK && Items.getRageKnife() != null
				&& hand.getType() == Items.getRageKnife().getItem()) {
			event.setCancelled(true);
		}

		if (action == Action.PHYSICAL) {
			return;
		}

		// Combat axe throw
		if (hand.getType() == Items.getCombatAxe().getItem()
				&& GameUtils.getGameByPlayer(p).getStatus() == GameStatus.RUNNING) {
			final Item item = p.getWorld().dropItem(p.getEyeLocation(), NMS.getItemInHand(p));

			p.getInventory().remove(Items.getCombatAxe().getItem());

			double velocity = plugin.getConfiguration().getItemsCfg().getDouble("gameitems.combatAxe.velocity", 2D);
			if (velocity > 0D) {
				item.setVelocity(p.getLocation().getDirection().multiply(velocity));
			}

			final CombatAxeThread combatAxeThread = new CombatAxeThread(p, item);
			combatAxeThread.start();

			CompletableFuture.supplyAsync(() -> {
				while (!item.isDead()) {
					try {
						Thread.sleep(3000);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}

					if (item.isOnGround()) {
						item.remove();
						combatAxeThread.stop();
						break; // To make sure we're broke the thread
					}
				}

				return true;
			});

			return;
		}

		if (GameUtils.getGameByPlayer(p).getStatus() == GameStatus.WAITING) {
			ItemMeta meta = hand.getItemMeta();
			if (meta != null && meta.hasDisplayName()) {
				Game game = GameUtils.getGameByPlayer(p);
				if (hasPerm(p, "ragemode.admin.item.forcestart") && Items.getForceStarter() != null
						&& Items.getForceStarter().getDisplayName().equals(meta.getDisplayName())) {
					if (GameUtils.forceStart(game)) {
						sendMessage(p,
								RageMode.getLang().get("commands.forcestart.game-start", "%game%", game.getName()));
					} else {
						sendMessage(p, RageMode.getLang().get("not-enough-players"));
					}
				}

				if (Items.getLeaveGameItem() != null
						&& Items.getLeaveGameItem().getDisplayName().equals(meta.getDisplayName())) {
					GameUtils.leavePlayer(p, game);
				}

				if (Items.getShopItem() != null && Items.getShopItem().getDisplayName().equals(meta.getDisplayName())) {
					lobbyShop.openMainPage(p);
				}
			}

			event.setCancelled(true);
		}
	}

	@EventHandler
	public void onInventoryOpen(InventoryOpenEvent e) {
		Player p = (Player) e.getPlayer();

		if (GameUtils.isPlayerPlaying(p) && GameUtils.getGameByPlayer(p).getStatus() == GameStatus.RUNNING
				&& e.getInventory().getType() != InventoryType.PLAYER) {
			e.setCancelled(true);
		}
	}

	@EventHandler
	public void onEntityInteract(PlayerInteractEntityEvent e) {
		if (GameUtils.isPlayerPlaying(e.getPlayer())) {
			e.setCancelled(true);
		}
	}

	// To prevent removing paintings
	@EventHandler
	public void onHanging(HangingBreakByEntityEvent ev) {
		if (ev.getRemover() instanceof Player && GameUtils.isPlayerPlaying((Player) ev.getRemover())) {
			ev.setCancelled(true);
		}
	}

	@EventHandler
	public void onInteractRedstone(PlayerInteractEvent ev) {
		if (ev.getClickedBlock() == null) {
			return;
		}

		Player p = ev.getPlayer();
		if (!GameUtils.isPlayerPlaying(p)) {
			return;
		}

		Action action = ev.getAction();
		Material t = ev.getClickedBlock().getType();
		GameStatus status = GameUtils.getGameByPlayer(p).getStatus();

		if (status != GameStatus.RUNNING && status != GameStatus.GAMEFREEZE) {
			return;
		}

		if (action == Action.PHYSICAL) {
			if (t == Material.FARMLAND) {
				ev.setUseInteractedBlock(Event.Result.DENY);
				ev.setCancelled(true);
			} else if (ConfigValues.isCancelRedstoneActivate() && MaterialUtil.isPressurePlate(t)) {
				ev.setUseInteractedBlock(Event.Result.DENY);
				ev.setCancelled(true);
			}
		} else if (ConfigValues.isCancelRedstoneActivate() && action == Action.RIGHT_CLICK_BLOCK) {
			if (MaterialUtil.isTrapdoor(t) || MaterialUtil.isButton(t) || MaterialUtil.isComparator(t)
					|| (Version.isCurrentEqualOrHigher(Version.v1_12_R1) && t == Material.REPEATER)
					|| t == Material.LEVER || t == Material.DAYLIGHT_DETECTOR) {
				ev.setUseInteractedBlock(Event.Result.DENY);
				ev.setCancelled(true);
			}
		}

		if (ConfigValues.isCancelDoorUse() && action == Action.RIGHT_CLICK_BLOCK && MaterialUtil.isDoor(t)) {
			ev.setUseInteractedBlock(Event.Result.DENY);
			ev.setCancelled(true);
		}

		// Just prevent usage of bush
		if (Version.isCurrentEqualOrHigher(Version.v1_14_R1) && (action == Action.RIGHT_CLICK_BLOCK
				&& (t == Material.SWEET_BERRY_BUSH || t == Material.COMPOSTER))) {
			ev.setUseInteractedBlock(Event.Result.DENY);
			ev.setCancelled(true);
		}
	}

	@EventHandler
	public void onVehicleEnter(VehicleEnterEvent e) {
		if (e.getEntered() instanceof Player && GameUtils.isPlayerPlaying((Player) e.getEntered()))
			e.setCancelled(true);
	}

	@EventHandler
	public void onInventoryInteract(InventoryClickEvent event) {
		if (!(event.getWhoClicked() instanceof Player)) {
			return;
		}

		Player p = (Player) event.getWhoClicked();

		if (GameUtils.isSpectatorPlaying(p) && event.getClickedInventory() != null
				&& event.getClickedInventory().getType() == InventoryType.PLAYER) {
			if ((event.getClick() == ClickType.LEFT || event.getClick() == ClickType.MIDDLE
					|| event.getClick() == ClickType.RIGHT)
					&& Items.getLeaveGameItem().build().isSimilar(event.getCurrentItem())) {
				event.setCancelled(true);
				GameUtils.leavePlayer(p, GameUtils.getGameBySpectator(p));
			}

			return;
		}

		if (GameUtils.isPlayerPlaying(p) && GameUtils.getGameByPlayer(p).getStatus() == GameStatus.WAITING
				&& !(event.getInventory().getHolder() instanceof IShop)) {
			event.setCancelled(true);
		}
	}

	@EventHandler
	public void onFly(PlayerToggleFlightEvent event) {
		if (GameUtils.isPlayerPlaying(event.getPlayer()))
			event.setCancelled(true);
	}

	@EventHandler
	public void playerBedEnter(PlayerBedEnterEvent event) {
		if (GameUtils.isPlayerPlaying(event.getPlayer()))
			event.setCancelled(true);
	}

	// This will fired in GameTimer to simulate pressure mine
	@EventHandler
	public void onMonsterInteract(EntityInteractEvent e) {
		if (GameAreaManager.inArea(e.getEntity().getLocation())) {
			explodeMine(null, e.getEntity().getLocation());
		}
	}

	@EventHandler
	public void onBlockGrow(BlockGrowEvent e) {
		GameAreaManager.getAreaByLocation(e.getBlock().getLocation()).ifPresent(area -> {
			if (area.getGame().isGameRunning()) {
				e.setCancelled(true);
			}
		});
	}

	@EventHandler
	public void onPlayerTeleport(PlayerTeleportEvent ev) {
		Player p = ev.getPlayer();

		if (!GameUtils.isPlayerPlaying(p) || GameUtils.getGameByPlayer(p).getStatus() != GameStatus.RUNNING) {
			return;
		}

		// when the player is not in the area
		if (ev.getTo() != null && !GameAreaManager.inArea(ev.getTo())) {
			ev.setCancelled(true);
		}
	}

	@EventHandler
	public void onPlayerMove(PlayerMoveEvent event) {
		Player p = event.getPlayer();

		if (!GameUtils.isPlayerPlaying(p)) {
			return;
		}

		Game game = GameUtils.getGameByPlayer(p);

		if (p.getLocation().getY() < 0) {
			Utils.teleport(p, GameUtils.getGameSpawn(game).getRandomSpawn());

			// Prevent damaging player when respawned
			p.setFallDistance(0);

			GameUtils.broadcastToGame(game, RageMode.getLang().get("game.void-fall", "%player%", p.getName()));
			return;
		}

		Location from = event.getFrom(), to = event.getTo();
		if (to == null) {
			return;
		}

		if (game.getStatus() == GameStatus.RUNNING) {
			if (to.getX() == from.getX() && to.getY() == from.getY() && to.getZ() == from.getZ()) {
				return;
			}

			// prevent player moving outside of game area
			Location loc = p.getLocation();

			// TODO if area lowest location is in air, then somehow teleport player to a block within area
			if (!GameAreaManager.inArea(loc)) {
				event.setTo(from.subtract(loc.clone().getDirection().multiply(1)));
				return;
			}

			// pressure mine
			GameAreaManager.getAreaByLocation(loc).ifPresent(area -> explodeMine(p, p.getLocation()));
		}

		if (game.getStatus() == GameStatus.GAMEFREEZE && GameUtils.isGameInFreezeRoom(game)) {
			if (!ConfigValues.isFreezePlayers()) {
				return;
			}

			double x = Math.floor(from.getX()), y = Math.floor(from.getY()), z = Math.floor(from.getZ());

			if (Math.floor(to.getX()) != x || Math.floor(to.getY()) != y || Math.floor(to.getZ()) != z) {
				x += .5;
				y += .5;
				z += .5;

				Utils.teleport(p, new Location(from.getWorld(), x, y, z, from.getYaw(), from.getPitch()));
			}
		}
	}

	@EventHandler
	public void onExplode(ExplosionPrimeEvent e) {
		explodeMine(null, e.getEntity().getLocation());
	}

	private boolean explodeMine(Player p, Location currentLoc) {
		org.bukkit.World w = currentLoc.getWorld();
		if (w == null) {
			return false;
		}

		Block b = currentLoc.getBlock();

		if (b.getRelative(BlockFace.DOWN).getType() != Material.TRIPWIRE && b.getType() != Material.TRIPWIRE) {
			return false;
		}

		double x = currentLoc.getX(),
				y = currentLoc.getY(),
				z = currentLoc.getZ();

		Collection<Entity> nears = Utils.getNearbyEntities(w, currentLoc, 10);

		Location explodeLoc = new Location(w, x, y + 1, z);
		explodeLoc.getWorld().createExplosion(x, y + 1, z, 4f, false, false);
		b.setType(Material.AIR);

		if (GameUtils.getGame(currentLoc) != null) {
			for (final Entity near : nears) {
				final UUID uuid = near.getUniqueId();

				if (explosionVictims.containsKey(uuid)) {
					explosionVictims.remove(uuid);
				}

				if (GameUtils.getGame(currentLoc).getGameType() == GameType.APOCALYPSE) {
					for (PressureMine mines : PRESSUREMINES) {
						if (mines.getMines().contains(currentLoc)) {
							explosionVictims.put(uuid, mines.getPlayerUUID());
							break;
						}
					}

					continue;
				}

				if (p != null) {
					explosionVictims.put(uuid, p.getUniqueId());
				} else if (near instanceof Player) {
					explosionVictims.put(uuid, uuid);
				}

				if (near instanceof Player) {
					near.removeMetadata("killedWith", plugin);
					near.setMetadata("killedWith", new FixedMetadataValue(plugin, "explosion"));
				}
			}
		}

		for (PressureMine mines : PRESSUREMINES) {
			mines.removeMine(currentLoc);
		}

		return true;
	}

	private void setTrails(final Projectile proj) {
		final boolean isArrow = proj instanceof Arrow;
		final boolean isEgg = proj instanceof Egg;

		double velocity = plugin.getConfiguration().getItemsCfg().getDouble("gameitems.grenade.velocity", 2D);
		if (velocity > 0D && isEgg) {
			proj.setVelocity(((Player) proj.getShooter()).getLocation().getDirection().multiply(velocity));
		}

		if ((!ConfigValues.isUseArrowTrails() || !isArrow) && (!ConfigValues.isUseGrenadeTrails() || !isEgg)) {
			return;
		}

		final UUID uuid = ((Player) proj.getShooter()).getUniqueId();
		if (ConfigValues.isUseArrowTrails() && isArrow && !LobbyShop.USERPARTICLES.containsKey(uuid)) {
			return;
		}

		CompletableFuture.supplyAsync(() -> {
			do {
				if (isEgg) {
					try {
						Thread.sleep(155L);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}

					proj.getWorld().playEffect(proj.getLocation(), org.bukkit.Effect.SMOKE, 0);
				} else if (ConfigValues.isUseArrowTrails() && isArrow && LobbyShop.USERPARTICLES.containsKey(uuid)) {
					try {
						Thread.sleep(40L);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}

					if (Version.isCurrentLower(Version.v1_9_R1)) {
						proj.getWorld().playEffect(proj.getLocation(), (Effect) LobbyShop.USERPARTICLES.get(uuid), 0);
					} else {
						proj.getWorld().spawnParticle((Particle) LobbyShop.USERPARTICLES.get(uuid), proj.getLocation(),
								0);
					}
				} else {
					return false;
				}
			} while (!proj.isDead() && !proj.isOnGround());

			return true;
		});
	}
}
