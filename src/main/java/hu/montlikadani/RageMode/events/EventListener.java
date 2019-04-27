package hu.montlikadani.ragemode.events;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.block.Sign;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.entity.EntityShootBowEvent;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerBedEnterEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerEggThrowEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.event.player.PlayerToggleFlightEvent;
import org.bukkit.event.vehicle.VehicleEnterEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.scheduler.BukkitRunnable;

import hu.montlikadani.ragemode.NMS;
import hu.montlikadani.ragemode.RageMode;
import hu.montlikadani.ragemode.Utils;
import hu.montlikadani.ragemode.config.Configuration;
import hu.montlikadani.ragemode.gameLogic.GameLoader;
import hu.montlikadani.ragemode.gameLogic.GameSpawnGetter;
import hu.montlikadani.ragemode.gameLogic.GameStatus;
import hu.montlikadani.ragemode.gameLogic.LobbyTimer;
import hu.montlikadani.ragemode.gameLogic.PlayerList;
import hu.montlikadani.ragemode.gameUtils.GameUtils;
import hu.montlikadani.ragemode.gameUtils.GetGames;
import hu.montlikadani.ragemode.gameUtils.MapChecker;
import hu.montlikadani.ragemode.gameUtils.Titles;
import hu.montlikadani.ragemode.holo.HoloHolder;
import hu.montlikadani.ragemode.items.CombatAxe;
import hu.montlikadani.ragemode.items.ForceStarter;
import hu.montlikadani.ragemode.items.Grenade;
import hu.montlikadani.ragemode.items.LeaveGame;
import hu.montlikadani.ragemode.items.RageArrow;
import hu.montlikadani.ragemode.items.RageKnife;
import hu.montlikadani.ragemode.libs.Sounds;
import hu.montlikadani.ragemode.scores.RageScores;
import hu.montlikadani.ragemode.signs.SignCreator;
import hu.montlikadani.ragemode.signs.SignData;

public class EventListener implements Listener {

	private RageMode plugin;
	private Map<UUID, UUID> explosionVictims = new HashMap<>();
	private Map<UUID, UUID> grenadeExplosionVictims = new HashMap<>();

	public static HashMap<String, Boolean> waitingGames = new HashMap<>();

	public EventListener(RageMode plugin) {
		this.plugin = plugin;
	}

	@EventHandler
	public void onPlayerJoin(PlayerJoinEvent event) {
		HoloHolder.showAllHolosToPlayer(event.getPlayer());
	}

	@EventHandler
	public void onPlayerQuit(PlayerQuitEvent event) {
		Player player = event.getPlayer();

		if (PlayerList.removeSpectatorPlayer(player))
			// Just removes the spec player

		if (PlayerList.getStatus() == GameStatus.RUNNING && PlayerList.isPlayerPlaying(player.getUniqueId().toString())) {
			if (PlayerList.removePlayer(player)) {
				RageMode.logConsole("Player " + player.getName() + " left the server while playing.");

				List<String> list = plugin.getConfiguration().getCfg().getStringList("game.global.run-commands-for-player-left-while-playing");
				if (list != null && !list.isEmpty()) {
					for (String cmds : list) {
						cmds = cmds.replace("%player%", player.getName());
						cmds = cmds.replace("%player-ip%", player.getAddress().getAddress().getHostAddress());
						Bukkit.dispatchCommand(Bukkit.getServer().getConsoleSender(), RageMode.getLang().colors(cmds));
					}
				}
			}
		}
		HoloHolder.deleteHoloObjectsOfPlayer(player);
	}

	@EventHandler(priority = EventPriority.HIGHEST)
	public void onPlayerChatFormat(AsyncPlayerChatEvent event) {
		Player p = event.getPlayer();

		// Cancels the spectator player chat
		if (PlayerList.specPlayer.containsKey(p.getUniqueId()))
			event.setCancelled(true);

		if (PlayerList.isPlayerPlaying(p.getUniqueId().toString())) {
			if (PlayerList.getStatus() == GameStatus.WAITING) {
				if (!plugin.getConfiguration().getCfg().getBoolean("game.global.lobby.enable-chat-in-lobby")
						&& !p.hasPermission("ragemode.bypass.lobby.lockchat")) {
					event.setCancelled(true);
					p.sendMessage(RageMode.getLang().get("game.lobby.chat-is-disabled"));
				} else event.setCancelled(false);
			}

			if (PlayerList.getStatus() == GameStatus.RUNNING) {
				if (!plugin.getConfiguration().getCfg().getBoolean("game.global.enable-chat-in-game")
						&& !p.hasPermission("ragemode.bypass.game.lockchat")) {
					event.setCancelled(true);
					p.sendMessage(RageMode.getLang().get("game.chat-is-disabled"));
					return;
				}

				if (plugin.getConfiguration().getCfg().getBoolean("game.global.chat-format.enable")) {
					String game = PlayerList.getPlayersGame(p);
					String[] players = PlayerList.getPlayersInGame(game);
					String format = plugin.getConfiguration().getCfg().getString("game.global.chat-format.format");
					format = format.replace("%player%", p.getName());
					format = format.replace("%player-displayname%", p.getDisplayName());
					format = format.replace("%game%", game);
					format = format.replace("%online-ingame-players%", Integer.toString(players.length));
					format = format.replace("%message%", event.getMessage());
					format = Utils.setPlaceholders(format, p);

					event.setFormat(RageMode.getLang().colors(format));
				}
			}

			if (PlayerList.getStatus() == GameStatus.GAMEFREEZE) {
				if (!plugin.getConfiguration().getCfg().getBoolean("game.global.enable-chat-after-end")) {
					event.setCancelled(true);
					p.sendMessage(RageMode.getLang().get("game.game-freeze.chat-is-disabled"));
				} else event.setCancelled(false);
			}
		}
	}

	@EventHandler(priority = EventPriority.LOWEST)
	public void onProjectileHit(ProjectileHitEvent event) {
		// RageArrow explosion event
		if (PlayerList.getStatus() == GameStatus.RUNNING && event.getEntity() instanceof Arrow) {
			Arrow arrow = (Arrow) event.getEntity();
			if (arrow.getShooter() != null && arrow.getShooter() instanceof Player) {
				Player shooter = (Player) arrow.getShooter();
				if (PlayerList.isPlayerPlaying(shooter.getUniqueId().toString())) {
					if (waitingGames.containsKey(PlayerList.getPlayersGame(shooter))) {
						if (waitingGames.get(PlayerList.getPlayersGame(shooter)))
							return;
					}

					Location location = arrow.getLocation();
					double x = location.getX();
					double y = location.getY();
					double z = location.getZ();

					List<Entity> nears = arrow.getNearbyEntities(10, 10, 10);

					arrow.getWorld().createExplosion(x, y, z, 2f, false, false);
					arrow.remove();

					int i = 0;
					int imax = nears.size();
					while (i < imax) {
						if (nears.get(i) instanceof Player /*
														 * && !nears.get(i).
														 * getUniqueId().
														 * toString().equals(
														 * shooter.getUniqueId()
														 * .toString())
														 */) {
							Player near = (Player) nears.get(i);
							if (explosionVictims != null) {
								if (explosionVictims.containsKey(near.getUniqueId())) {
									explosionVictims.remove(near.getUniqueId());
									explosionVictims.put(near.getUniqueId(), shooter.getUniqueId());
								}
							}
							explosionVictims.put(near.getUniqueId(), shooter.getUniqueId());

							// Remove arrows from player body
							if (plugin.getConfiguration().getCfg().getBoolean("game.global.remove-arrows-from-player-body")) {
								for (Entity e : near.getNearbyEntities(1, 1, 1)) {
									if (e instanceof Arrow)
										e.remove();
								}
							}
						}
						i++;
					}
				}
			}
		}
	}

	@EventHandler
	public void onHit(EntityDamageByEntityEvent event) {
		if (event.getDamager() instanceof Player) {
			Player killer = (Player) event.getDamager();
			// RageKnife hit event
			if (PlayerList.getStatus() == GameStatus.RUNNING && event.getEntity() instanceof Player) {
				Player victim = (Player) event.getEntity();
				if (PlayerList.isPlayerPlaying(killer.getUniqueId().toString())
						&& PlayerList.isPlayerPlaying(victim.getUniqueId().toString())) {
					if (waitingGames.containsKey(PlayerList.getPlayersGame(killer))) {
						if (waitingGames.get(PlayerList.getPlayersGame(killer))) {
							event.setCancelled(true);
							return;
						}
					}
					ItemStack hand = NMS.getItemInHand(killer);
					ItemMeta meta = hand.getItemMeta();
					if (meta != null && meta.getDisplayName() != null) {
						if (meta.getDisplayName().equals(RageKnife.getName()))
							event.setDamage(25);
					}
				}
				if (!PlayerList.isGameRunning(PlayerList.getPlayersGame(victim)))
					event.setDamage(0);
			}

			// Prevent player damage in lobby
			if (PlayerList.getStatus() == GameStatus.WAITING && PlayerList.isPlayerPlaying(killer.getUniqueId().toString()))
				event.setCancelled(true);
		}
	}

	@EventHandler(priority = EventPriority.HIGHEST)
	public void onItemDrop(PlayerDropItemEvent event) {
		if (PlayerList.isPlayerPlaying(event.getPlayer().getUniqueId().toString()))
			event.setCancelled(true);
	}

	@EventHandler
	public void onHitPlayer(EntityDamageEvent event) {
		// Hit player event
		if (PlayerList.getStatus() == GameStatus.RUNNING && event.getEntity() instanceof Player) {
			if (PlayerList.isPlayerPlaying(event.getEntity().getUniqueId().toString())) {
				if (event.getCause().equals(DamageCause.PROJECTILE)) {
					Player victim = (Player) event.getEntity();
					if (PlayerList.isPlayerPlaying(victim.getUniqueId().toString())) {
						if (waitingGames.containsKey(PlayerList.getPlayersGame(victim))) {
							if (waitingGames.get(PlayerList.getPlayersGame(victim))) {
								event.setDamage(0);
								event.setCancelled(true);
								return;
							}
						}
						if (event.getEntity() instanceof Projectile && event.getEntity() instanceof org.bukkit.entity.Egg
								&& event.getEntity().getCustomName().equals(Grenade.getName()))
							event.setDamage(0.22d);

						if (event.getEntity() instanceof Projectile && event.getEntity() instanceof Arrow
								&& event.getEntity().getCustomName().equals(RageArrow.getName())) {
							if (event.getDamage() == 0.0d)
								event.setDamage(2.12d);
							else
								event.setDamage(2.35d);
						}
					}
				}
				if (event.getCause().equals(DamageCause.FALL) && !plugin.getConfiguration().getCfg().getBoolean("game.global.damage-player-fall"))
					event.setDamage(0);
			}
		}
	}

	@EventHandler
	public void onEntityShootBow(EntityShootBowEvent event) {
		if (!event.isCancelled() && event.getEntity() != null && event.getEntity() instanceof Player
				&& plugin.getConfiguration().getCfg().getBoolean("game.global.arrow-trail.enable")) {
			if (PlayerList.isPlayerPlaying(((Player) event.getEntity()).getUniqueId().toString())
					&& waitingGames.containsKey(PlayerList.getPlayersGame((Player) event.getEntity()))) {
				if (waitingGames.get(PlayerList.getPlayersGame((Player) event.getEntity())))
					return;
			}

			if (PlayerList.getStatus() == GameStatus.RUNNING) {
				final Arrow arrow = (Arrow) event.getProjectile();

				if (PlayerList.isPlayerPlaying(((Player) event.getEntity()).getUniqueId().toString())) {
					new BukkitRunnable() {
						@Override
						public void run() {
							try {
								if (!arrow.isOnGround() && !arrow.isDead()) {
									arrow.getWorld().spawnParticle(Particle.valueOf(plugin.getConfiguration().getCfg().getString("game.global.arrow-trail.effect-type")),
											arrow.getLocation(), 15, 0.0F, 0.0F, 0.0F);
								} else
									cancel();
							} catch (IllegalArgumentException e) {
								RageMode.logConsole(java.util.logging.Level.WARNING, "[RageMode] Unknown particle effect type: "
							+ plugin.getConfiguration().getCfg().getString("game.global.arrow-trail.effect-type"));
								cancel();
							}
						}
					}.runTaskTimer(plugin, 0, 2);
				}
			}
		}
	}

	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void onPlayerDeath(PlayerDeathEvent event) {
		// Player autorespawn
		Player deceased;
		if (event.getEntity() != null && event.getEntity() instanceof Player)
			deceased = (Player) event.getEntity();
		else {
			deceased = null;
			return;
		}
		if (deceased != null && PlayerList.isPlayerPlaying(deceased.getUniqueId().toString()) && PlayerList.getStatus() == GameStatus.RUNNING) {
			if ((deceased.getKiller() != null && PlayerList.isPlayerPlaying(deceased.getKiller().getUniqueId().toString()))
					|| deceased.getKiller() == null) {
				String game = PlayerList.getPlayersGame(deceased);

				boolean doDeathBroadcast = plugin.getConfiguration().getCfg().getBoolean("game.global.death-messages");

				if (plugin.getConfiguration().getArenasCfg().isSet("arenas." + game + ".death-messages")) {
					String gameBroadcast = plugin.getConfiguration().getArenasCfg().getString("arenas." + game + ".death-messages");
					if (gameBroadcast != null && gameBroadcast != "") {
						if (gameBroadcast.equals("true") || gameBroadcast.equals("false"))
							doDeathBroadcast = Boolean.parseBoolean(gameBroadcast);
					}
				}

				if (deceased.getLastDamage() == 0.0f) {
					if (deceased.getKiller() == null) {
						if (doDeathBroadcast)
							GameUtils.broadcastToGame(game, RageMode.getLang().get("game.broadcast.axe-kill", "%victim%", deceased.getName(),
									"%killer%", deceased.getName()));

						RageScores.addPointsToPlayer(deceased, deceased, "combataxe");
					} else {
						if (doDeathBroadcast)
							GameUtils.broadcastToGame(game, RageMode.getLang().get("game.broadcast.axe-kill", "%victim%", deceased.getName(),
									"%killer%", deceased.getKiller().getName()));

						RageScores.addPointsToPlayer(deceased.getKiller(), deceased, "combataxe");
					}
				} else if (deceased.getLastDamageCause().getCause().equals(DamageCause.PROJECTILE)) {
					if (deceased.getKiller() == null) {
						if (doDeathBroadcast)
							GameUtils.broadcastToGame(game, RageMode.getLang().get("game.broadcast.arrow-kill", "%victim%", deceased.getName(),
									"%killer%", deceased.getName()));

						RageScores.addPointsToPlayer(deceased, deceased, "ragebow");
					} else {
						if (doDeathBroadcast)
							GameUtils.broadcastToGame(game, RageMode.getLang().get("game.broadcast.arrow-kill", "%victim%", deceased.getName(),
									"%killer%", deceased.getKiller().getName()));

						RageScores.addPointsToPlayer(deceased.getKiller(), deceased, "ragebow");
					}
				} else if (deceased.getLastDamageCause().getCause().equals(DamageCause.ENTITY_ATTACK)) {
					if (deceased.getKiller() == null) {
						if (doDeathBroadcast)
							GameUtils.broadcastToGame(game, RageMode.getLang().get("game.broadcast.knife-kill", "%victim%", deceased.getName(),
									"%killer%", deceased.getName()));

						RageScores.addPointsToPlayer(deceased, deceased, "rageknife");
					} else {
						if (doDeathBroadcast)
							GameUtils.broadcastToGame(game, RageMode.getLang().get("game.broadcast.knife-kill", "%victim%", deceased.getName(),
									"%killer%", deceased.getKiller().getName()));

						RageScores.addPointsToPlayer(deceased.getKiller(), deceased, "rageknife");
					}
				} else if (deceased.getLastDamageCause().getCause().equals(DamageCause.BLOCK_EXPLOSION)) {
					if (explosionVictims.containsKey(deceased.getUniqueId())) {
						if (doDeathBroadcast)
							GameUtils.broadcastToGame(game, RageMode.getLang().get("game.broadcast.explosion-kill", "%victim%", deceased.getName(),
									"%killer%", Bukkit.getPlayer(explosionVictims.get(deceased.getUniqueId())).getName()));

						RageScores.addPointsToPlayer(Bukkit.getPlayer(explosionVictims.get(deceased.getUniqueId())),
								deceased, "explosion");
					} else if (grenadeExplosionVictims.containsKey(deceased.getUniqueId())) {
						if (doDeathBroadcast)
							GameUtils.broadcastToGame(game, RageMode.getLang().get("game.broadcast.explosion-kill", "%victim%",
									deceased.getName(), "%killer%", Bukkit.getPlayer(grenadeExplosionVictims.get(deceased.getUniqueId())).getName()));

						RageScores.addPointsToPlayer(
								Bukkit.getPlayer(grenadeExplosionVictims.get(deceased.getUniqueId())), deceased, "explosion");
					} else {
						if (doDeathBroadcast)
							GameUtils.broadcastToGame(game, RageMode.getLang().get("game.broadcast.error-kill"));

						deceased.sendMessage(RageMode.getLang().get("game.unknown-killer"));
					}
				} else {
					if (doDeathBroadcast)
						GameUtils.broadcastToGame(game, RageMode.getLang().get("game.unknown-weapon", "%victim%", deceased.getName()));
				}
				event.setDroppedExp(0);
				event.setDeathMessage("");
				event.setKeepInventory(true);
			}

			GameSpawnGetter gameSpawnGetter = new GameSpawnGetter(PlayerList.getPlayersGame(deceased));
			gameSpawnGetter.randomSpawn(deceased);

			// give him a new set of items that losing durability
			//deceased.getInventory().clear();

			YamlConfiguration conf = plugin.getConfiguration().getCfg();
			deceased.getInventory().setItem(conf.getInt("items.rageBow.slot"), hu.montlikadani.ragemode.items.RageBow.getItem());
			deceased.getInventory().setItem(conf.getInt("items.rageKnife.slot"), RageKnife.getItem());
			deceased.getInventory().setItem(conf.getInt("items.combatAxe.slot"), CombatAxe.getItem());
			deceased.getInventory().setItem(conf.getInt("items.grenade.slot"), Grenade.getItem());
		}
	}

	@EventHandler
	public void onHungerGain(FoodLevelChangeEvent event) {
		if (event.getEntity() instanceof Player) {
			Player player = (Player) event.getEntity();
			if (PlayerList.isPlayerPlaying(player.getUniqueId().toString()))
				event.setCancelled(true);
		}
	}

	@EventHandler
	public void onBlockPlace(BlockPlaceEvent ev) {
		if (PlayerList.isPlayerPlaying(ev.getPlayer().getUniqueId().toString()))
			ev.setCancelled(true);
	}

	@EventHandler(priority = EventPriority.NORMAL)
	public void onBlockBreak(BlockBreakEvent event) {
		if (PlayerList.isPlayerPlaying(event.getPlayer().getUniqueId().toString()))
			event.setCancelled(true);
	}

	@EventHandler
	public void onSignBreak(BlockBreakEvent event) {
		if (event.isCancelled() || !plugin.getConfiguration().getCfg().getBoolean("signs.enable")) return;

		org.bukkit.block.BlockState blockState = event.getBlock().getState();
		if (blockState instanceof Sign) {
			for (SignData data : SignCreator.getSignData()) {
				if (blockState.getLocation().equals(data.getLocation())) {
					if (!event.getPlayer().hasPermission("ragemode.admin.signs")) {
						event.getPlayer().sendMessage(RageMode.getLang().get("no-permission-to-interact-sign"));
						event.setCancelled(true);
						return;
					}

					SignCreator.removeSign((Sign) blockState);
				}
			}
		}
	}

	@EventHandler
	public void onDisabledCommand(PlayerCommandPreprocessEvent event) {
		Player p = event.getPlayer();

		if (event.getMessage() == null) return;

		List<String> cmds = plugin.getConfiguration().getCfg().getStringList("game.global.commands");
		String arg = event.getMessage().trim().toLowerCase();
		if (cmds != null && !cmds.isEmpty()) {
			if (PlayerList.specPlayer.containsKey(p.getUniqueId())) {
				if (!cmds.contains(arg)) {
					p.sendMessage(RageMode.getLang().get("game.this-command-is-disabled-in-game"));
					event.setCancelled(true);
				}
			}

			if (PlayerList.isPlayerPlaying(p.getUniqueId().toString())) {
				if (waitingGames.containsKey(PlayerList.getPlayersGame(p))) {
					if (waitingGames.get(PlayerList.getPlayersGame(p))) {
						p.sendMessage(RageMode.getLang().get("game.command-disabled-in-end-game"));
						event.setCancelled(true);
					}
				}
			}
		}
	}

	@EventHandler
	public void eggThrow(PlayerEggThrowEvent event) {
		if (event.getPlayer() == null || !event.getPlayer().isOnline()) return;

		final Player p = event.getPlayer();
		if (PlayerList.isPlayerPlaying(p.getUniqueId().toString())) {
			if (waitingGames.containsKey(PlayerList.getPlayersGame(p))) {
				if (waitingGames.get(PlayerList.getPlayersGame(p)))
					return;
			}

			if (PlayerList.getStatus() == GameStatus.RUNNING) {
				event.setHatching(false); // no baby chickens

				// Removes the egg from player inventory and prevent
				// other item remove when moved the slot to another
				if (p.getInventory().contains(Material.EGG)) {
					ItemStack item = p.getInventory().getItem(plugin.getConfiguration().getCfg().getInt("items.grenade.slot"));
					item.setAmount(item.getAmount() - 1);
				}

				final Item grenade = p.getWorld().dropItem(event.getEgg().getLocation(), new ItemStack(Material.EGG));

				// Cancel egg pick up
				grenade.setPickupDelay(41);

				grenade.setVelocity(p.getEyeLocation().getDirection());
				// move egg from land location to simulate bounce
				grenade.getLocation().add(event.getEgg().getVelocity());

				final List<Entity> nears = grenade.getNearbyEntities(13, 13, 13);

				if (Utils.getVersion().contains("1.8"))
					Sounds.CREEPER_HISS.playSound(grenade.getLocation(), 1, 1);
				else
					Sounds.ENTITY_CREEPER_PRIMED.playSound(grenade.getLocation(), 1, 1);

				new BukkitRunnable() {
					@Override
					public void run() {
						Location loc = grenade.getLocation();
						double gX = loc.getX();
						double gY = loc.getY();
						double gZ = loc.getZ();

						grenade.getWorld().createExplosion(gX, gY, gZ, 2f, false, false);
						grenade.remove(); // get rid of egg so it can't be picked up

						int i = 0;
						int imax = nears.size();
						while (i < imax) {
							if (nears.get(i) instanceof Player) {
								Player near = (Player) nears.get(i);
								if (grenadeExplosionVictims != null) {
									if (grenadeExplosionVictims.containsKey(near.getUniqueId())) {
										grenadeExplosionVictims.remove(near.getUniqueId());
										grenadeExplosionVictims.put(near.getUniqueId(), p.getUniqueId());
									}
								}
								grenadeExplosionVictims.put(near.getUniqueId(), p.getUniqueId());
							}
							i++;
						}
					}
				}.runTaskLater(plugin, 40);
			}
		}
	}

	@EventHandler
	public void onInteract(PlayerInteractEvent event) {
		Player p = event.getPlayer();

		if (PlayerList.isPlayerPlaying(p.getUniqueId().toString())) {
			if (PlayerList.getStatus() == GameStatus.RUNNING) {
				Player thrower = event.getPlayer();
				if (waitingGames.containsKey(PlayerList.getPlayersGame(thrower))) {
					if (waitingGames.get(PlayerList.getPlayersGame(thrower)))
						return;
				}

				ItemStack hand = NMS.getItemInHand(thrower);
				ItemMeta meta = hand.getItemMeta();
				if (meta != null && meta.getDisplayName() != null) {
					if (meta.getDisplayName().equals(CombatAxe.getName())) {
						thrower.launchProjectile(org.bukkit.entity.Snowball.class);
						NMS.setItemInHand(thrower, null);
					}
				}
			}

			// Don't use left click block action to prevent block randomly hide
			//
			// Left/Right click air bug: when the player joins the game with a left/right click
			// through the sign and the slot is on one of the lobby items, the item is executed
			// SO THESE ARE BUGGY!!
			if (event.getAction() == Action.RIGHT_CLICK_BLOCK || event.getAction() == Action.RIGHT_CLICK_AIR
					|| event.getAction() == Action.LEFT_CLICK_AIR) {
				if (PlayerList.getStatus() == GameStatus.WAITING) {
					ItemStack hand = NMS.getItemInHand(p);
					ItemMeta meta = hand.getItemMeta();
					if (meta != null && meta.getDisplayName() != null) {
						if (p.hasPermission("ragemode.admin.item.forcestart")
								&& meta.getDisplayName().equals(ForceStarter.getName())) {
							String game = PlayerList.getPlayersGame(p);
							if (LobbyTimer.map.containsKey(game)) {
								LobbyTimer.map.get(game).cancel();
								LobbyTimer.map.remove(game);

								// Set level back to 0
								p.setLevel(0);
							}

							new GameLoader(game);
							p.sendMessage(RageMode.getLang().get("commands.forcestart.game-start", "%game%", game));
						}

						if (meta.getDisplayName().equals(LeaveGame.getName())) {
							PlayerList.removePlayer(p);
							PlayerList.removeSpectatorPlayer(p);
						}
					}
				}
			}
		}

		Configuration conf = plugin.getConfiguration();

		if (conf.getCfg().getBoolean("signs.enable") && event.getClickedBlock() != null
				&& event.getClickedBlock().getState() != null) {
			if (event.getClickedBlock().getState() instanceof Sign && event.getAction() == Action.RIGHT_CLICK_BLOCK) {
				Sign sign = (Sign) event.getClickedBlock().getState();

				if (sign.getLine(0).contains(RageMode.getLang().colors(conf.getCfg()
						.getStringList("signs.list").get(0))) && SignCreator.isJoinSign()) {
					if (p.hasPermission("ragemode.join.sign")) {
						String arena = SignCreator.getGameFromString();
						org.bukkit.inventory.PlayerInventory inv = p.getInventory();
						if (PlayerList.getStatus() == GameStatus.RUNNING) {
							if (!PlayerList.isPlayerPlaying(p.getUniqueId().toString())) {
								if (PlayerList.addSpectatorPlayer(p)) {
									GameSpawnGetter gameSpawnGetter = new GameSpawnGetter(arena);
									gameSpawnGetter.randomSpawn(p);
									p.setAllowFlight(true);
									p.setFlying(true);
									p.setGameMode(GameMode.SPECTATOR);
									if (conf.getCfg().contains("items.leavegameitem"))
										inv.setItem(conf.getCfg().getInt("items.leavegameitem.slot"), LeaveGame.getItem());
								}
							} else
								p.sendMessage(RageMode.getLang().get("game.player-not-switch-spectate"));
						} else {
							MapChecker mapChecker = new MapChecker(arena);
							if (mapChecker.isValid()) {
								String path = "arenas." + arena + ".lobby.";
								String world = conf.getArenasCfg().getString(path + "world");
								double lobbyX = conf.getArenasCfg().getDouble(path + "x");
								double lobbyY = conf.getArenasCfg().getDouble(path + "y");
								double lobbyZ = conf.getArenasCfg().getDouble(path + "z");
								double lobbyYaw = conf.getArenasCfg().getDouble(path + "yaw");
								double lobbyPitch = conf.getArenasCfg().getDouble(path + "pitch");

								Location lobbyLocation = new Location(Bukkit.getWorld(world), lobbyX, lobbyY, lobbyZ);
								lobbyLocation.setYaw((float) lobbyYaw);
								lobbyLocation.setPitch((float) lobbyPitch);

								if (PlayerList.addPlayer(p, arena)) {
									GameUtils.savePlayerData(p);

									p.teleport(lobbyLocation);

									if (conf.getCfg().contains("items.leavegameitem"))
										inv.setItem(conf.getCfg().getInt("items.leavegameitem.slot"), LeaveGame.getItem());

									if (conf.getCfg().contains("items.force-start") && p.hasPermission("ragemode.admin.item.forcestart"))
										inv.setItem(conf.getCfg().getInt("items.force-start.slot"), ForceStarter.getItem());

									GameUtils.broadcastToGame(arena, RageMode.getLang().get("game.player-joined", "%player%", p.getName()));

									String title = conf.getCfg().getString("titles.join-game.title");
									String subtitle = conf.getCfg().getString("titles.join-game.subtitle");
									if (title != null && subtitle != null) {
										title = title.replace("%game%", arena);
										subtitle = subtitle.replace("%game%", arena);
										Titles.sendTitle(p, conf.getCfg().getInt("titles.join-game.fade-in"), conf.getCfg().getInt("titles.join-game.stay"),
												conf.getCfg().getInt("titles.join-game.fade-out"), title, subtitle);
									}
									SignCreator.updateAllSigns(arena);
								} else
									RageMode.logConsole(RageMode.getLang().get("game.player-could-not-join", "%player%", p.getName(), "%game%", arena));
							} else
								p.sendMessage(mapChecker.getMessage());
						}
					} else
						p.sendMessage(RageMode.getLang().get("no-permission"));
				}
			}
		}
	}

	@EventHandler
	public void onInventoryOpen(InventoryOpenEvent e) {
		if (PlayerList.isPlayerPlaying(e.getPlayer().getUniqueId().toString())) {
			InventoryType type = e.getInventory().getType();

			if (type == InventoryType.ENCHANTING
					|| type == InventoryType.CRAFTING
					|| type == InventoryType.ANVIL
					|| type == InventoryType.BREWING
					|| type == InventoryType.FURNACE
					|| type == InventoryType.WORKBENCH
					|| type == InventoryType.CHEST
					|| type == InventoryType.DROPPER
					|| type == InventoryType.ENDER_CHEST
					|| type == InventoryType.HOPPER)
				e.setCancelled(true);
		}
	}

	@EventHandler
	public void onCropsTrampled(PlayerInteractEvent ev) {
		if (PlayerList.isPlayerPlaying(ev.getPlayer().getUniqueId().toString()) && ev.getAction() == Action.PHYSICAL) {
			org.bukkit.block.Block block = ev.getClickedBlock();
			if (block == null)
				return;

			if (block.getType() == Material.FARMLAND) {
				ev.setUseInteractedBlock(org.bukkit.event.Event.Result.DENY);
				ev.setCancelled(true);
			}
		}
	}

	@EventHandler
	public void onVehicleEnter(VehicleEnterEvent e) {
		if (PlayerList.isPlayerPlaying(e.getEntered().getUniqueId().toString()))
			e.setCancelled(true);
	}

	@EventHandler
	public void onInventoryInteract(InventoryClickEvent event) {
		if (event.getWhoClicked() instanceof Player && PlayerList.isPlayerPlaying(event.getWhoClicked().getUniqueId().toString()))
			event.setCancelled(true);
	}

	@EventHandler(priority = EventPriority.HIGHEST)
	public void onFly(PlayerToggleFlightEvent event) {
		if (PlayerList.isPlayerPlaying(event.getPlayer().getUniqueId().toString()))
			event.setCancelled(true);
	}

	@EventHandler
	private void PlayerBedEnter(PlayerBedEnterEvent event) {
		if (PlayerList.isPlayerPlaying(event.getPlayer().getUniqueId().toString()))
			event.setCancelled(true);
	}

	@EventHandler
	public void onSignChange(SignChangeEvent event) {
		if (event.isCancelled() || event.getPlayer() == null || event.getBlock() == null) return;

		if (plugin.getConfiguration().getCfg().getBoolean("signs.enable")
				&& event.getLine(0).contains("[rm]") || event.getLine(0).contains("[ragemode]")) {
			if (!event.getPlayer().hasPermission("ragemode.admin.signs")) {
				event.setCancelled(true);
				event.getPlayer().sendMessage(RageMode.getLang().get("no-permission-to-interact-sign"));
				return;
			}

			for (String game : GetGames.getGameNames()) {
				if (event.getLine(1).contains(game)) {
					SignCreator.createNewSign((Sign) event.getBlock().getState(), game);
					SignCreator.updateSign(((Sign) event.getBlock().getState()).getLocation());
				}
			}
		}
	}

	@EventHandler
	public void onPlayerMove(PlayerMoveEvent event) {
		Player p = event.getPlayer();

		if (PlayerList.isPlayerPlaying(p.getUniqueId().toString())) {
			if (PlayerList.getStatus() == GameStatus.RUNNING) {
				if (waitingGames != null && waitingGames.containsKey(PlayerList.getPlayersGame(p))) {
					if (waitingGames.get(PlayerList.getPlayersGame(p))) {
						Location from = event.getFrom();
						Location to = event.getTo();
						double x = Math.floor(from.getX());
						double z = Math.floor(from.getZ());
						if (Math.floor(to.getX()) != x || Math.floor(to.getZ()) != z) {
							x += .5;
							z += .5;
							p.teleport(new Location(from.getWorld(), x, from.getY(), z, from.getYaw(), from.getPitch()));
						}
					}
				}
			}

			if (p.getLocation().getY() < 0) {
				GameSpawnGetter gameSpawnGetter = new GameSpawnGetter(PlayerList.getPlayersGame(p));
				gameSpawnGetter.randomSpawn(p);

				// Prevent damaging player when respawned
				p.setFallDistance(0);

				GameUtils.broadcastToGame(PlayerList.getPlayersGame(p), RageMode.getLang().get("game.void-fall", "%player%", p.getName()));
			}
		}
	}

	@EventHandler
	public void onWorldChangedEvent(PlayerTeleportEvent event) {
		if (PlayerList.isPlayerPlaying(event.getPlayer().getUniqueId().toString())) {
			if (!MapChecker.isGameWorld(PlayerList.getPlayersGame(event.getPlayer()), event.getTo().getWorld())) {
				if (!event.getPlayer().hasMetadata("Leaving"))
					event.getPlayer().performCommand("rm leave");
				else
					event.getPlayer().removeMetadata("Leaving", RageMode.getInstance());
			}
		}
	}
}
