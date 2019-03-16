package hu.montlikadani.ragemode.events;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.UUID;
import java.util.logging.Level;

import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.block.Sign;
import org.bukkit.configuration.file.FileConfiguration;
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
import org.bukkit.event.entity.EntityPickupItemEvent;
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
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.scheduler.BukkitRunnable;

import hu.montlikadani.ragemode.NMS;
import hu.montlikadani.ragemode.RageMode;
import hu.montlikadani.ragemode.Utils;
import hu.montlikadani.ragemode.config.Configuration;
import hu.montlikadani.ragemode.gameLogic.GameSpawnGetter;
import hu.montlikadani.ragemode.gameLogic.GameStatus;
import hu.montlikadani.ragemode.gameLogic.PlayerList;
import hu.montlikadani.ragemode.gameUtils.GameBroadcast;
import hu.montlikadani.ragemode.gameUtils.GetGames;
import hu.montlikadani.ragemode.gameUtils.MapChecker;
import hu.montlikadani.ragemode.gameUtils.Titles;
import hu.montlikadani.ragemode.holo.HoloHolder;
import hu.montlikadani.ragemode.items.CombatAxe;
import hu.montlikadani.ragemode.items.Grenade;
import hu.montlikadani.ragemode.items.LeaveGame;
import hu.montlikadani.ragemode.items.RageArrow;
import hu.montlikadani.ragemode.items.RageKnife;
import hu.montlikadani.ragemode.libs.Sounds;
import hu.montlikadani.ragemode.scores.RageScores;
import hu.montlikadani.ragemode.signs.SignCreator;

public class EventListener implements Listener {

	private RageMode plugin;
	public static HashMap<String, Boolean> waitingGames = new HashMap<>();
	private Map<UUID, UUID> explosionVictims = new HashMap<>();
	private Map<UUID, UUID> grenadeExplosionVictims = new HashMap<>();

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

		if (PlayerList.getStatus() == GameStatus.RUNNING && PlayerList.isPlayerPlaying(player.getUniqueId().toString())) {
			if (PlayerList.removePlayer(player)) {
				RageMode.logConsole(Level.INFO, "Player " + player.getName() + " left the server while playing.");

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
		if (event.isCancelled() || event.getPlayer() == null || !event.getPlayer().isOnline()) return;

		Player p = event.getPlayer();

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
	public void onRageKnifeHit(EntityDamageByEntityEvent event) {
		// RageKnife hit event
		if (PlayerList.getStatus() == GameStatus.RUNNING && event.getDamager() instanceof Player && event.getEntity() instanceof Player) {
			Player killer = (Player) event.getDamager();
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
					if (meta.getDisplayName().contains(RageKnife.getRageKnifeName()))
						event.setDamage(25);
				}
			}
			if (!PlayerList.isGameRunning(PlayerList.getPlayersGame(victim)))
				event.setDamage(0);
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
								&& event.getEntity().getCustomName().contains(Grenade.getGrenadeName()))
							event.setDamage(0.22d);

						if (event.getEntity() instanceof Projectile && event.getEntity() instanceof Arrow
								&& event.getEntity().getCustomName().contains(RageArrow.getRageArrowName())) {
							if (event.getDamage() == 0.0d)
								event.setDamage(2.12d);
							else
								event.setDamage(2.35d);
						}
					}
				}
				if (event.getCause().equals(DamageCause.FALL) && !plugin.getConfiguration().getCfg().getBoolean("game.global.damage-player-fall"))
					event.setDamage(0);

				if (event.getCause().equals(DamageCause.VOID)) {
					Player p = (Player) event.getEntity();

					GameBroadcast.broadcastToGame(PlayerList.getPlayersGame(p), RageMode.getLang().get("game.void-fall", "%player%", p.getName()));
					playerVars(p);
				}
			}
		}
	}

	@EventHandler
	public void onEntityShootBow(EntityShootBowEvent event) {
		if (!event.isCancelled() && PlayerList.getStatus() == GameStatus.RUNNING && event.getEntity() != null && event.getEntity() instanceof Player
				&& plugin.getConfiguration().getCfg().getBoolean("game.global.arrow-trail.enable")) {
			final Arrow arrow = (Arrow) event.getProjectile();

			if (PlayerList.isPlayerPlaying(((Player) event.getEntity()).getUniqueId().toString())) {
				new BukkitRunnable() {
					@Override
					public void run() {
						try {
							if (!arrow.isOnGround() && !arrow.isDead()) {
								for (int i = 0; i < 3; i++) {
									arrow.getWorld().spawnParticle(Particle.valueOf(plugin.getConfiguration().getCfg().getString("game.global.arrow-trail.effect-type")),
											arrow.getLocation(), 4 * i / 2, 0.0, 0.0, 0.0);
								}
							} else
								cancel();
						} catch (IllegalArgumentException e) {
							RageMode.logConsole(Level.WARNING, "[RageMode] Unknown particle effect type: "
						+ plugin.getConfiguration().getCfg().getString("game.global.arrow-trail.effect-type"));
							cancel();
						}
					}
				}.runTaskTimer(plugin, 0, 2);
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
		if (deceased != null && PlayerList.getStatus() == GameStatus.RUNNING && PlayerList.isPlayerPlaying(deceased.getUniqueId().toString())) {
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
							GameBroadcast.broadcastToGame(game, RageMode.getLang().get("game.broadcast.axe-kill", "%victim%", deceased.getName(),
									"%killer%", deceased.getName()));

						RageScores.addPointsToPlayer(deceased, deceased, "combataxe");
					} else {
						if (doDeathBroadcast)
							GameBroadcast.broadcastToGame(game, RageMode.getLang().get("game.broadcast.axe-kill", "%victim%", deceased.getName(),
									"%killer%", deceased.getKiller().getName()));

						RageScores.addPointsToPlayer(deceased.getKiller(), deceased, "combataxe");
					}
				} else if (deceased.getLastDamageCause().getCause().equals(DamageCause.PROJECTILE)) {
					if (deceased.getKiller() == null) {
						if (doDeathBroadcast)
							GameBroadcast.broadcastToGame(game, RageMode.getLang().get("game.broadcast.arrow-kill", "%victim%", deceased.getName(),
									"%killer%", deceased.getName()));

						RageScores.addPointsToPlayer(deceased, deceased, "ragebow");
					} else {
						if (doDeathBroadcast)
							GameBroadcast.broadcastToGame(game, RageMode.getLang().get("game.broadcast.arrow-kill", "%victim%", deceased.getName(),
									"%killer%", deceased.getKiller().getName()));

						RageScores.addPointsToPlayer(deceased.getKiller(), deceased, "ragebow");
					}
				} else if (deceased.getLastDamageCause().getCause().equals(DamageCause.ENTITY_ATTACK)) {
					if (deceased.getKiller() == null) {
						if (doDeathBroadcast)
							GameBroadcast.broadcastToGame(game, RageMode.getLang().get("game.broadcast.knife-kill", "%victim%", deceased.getName(),
									"%killer%", deceased.getName()));

						RageScores.addPointsToPlayer(deceased, deceased, "rageknife");
					} else {
						if (doDeathBroadcast)
							GameBroadcast.broadcastToGame(game, RageMode.getLang().get("game.broadcast.knife-kill", "%victim%", deceased.getName(),
									"%killer%", deceased.getKiller().getName()));

						RageScores.addPointsToPlayer(deceased.getKiller(), deceased, "rageknife");
					}
				} else if (deceased.getLastDamageCause().getCause().equals(DamageCause.BLOCK_EXPLOSION)) {
					if (explosionVictims.containsKey(deceased.getUniqueId())) {
						if (doDeathBroadcast)
							GameBroadcast.broadcastToGame(game, RageMode.getLang().get("game.broadcast.explosion-kill", "%victim%", deceased.getName(),
									"%killer%", Bukkit.getPlayer(explosionVictims.get(deceased.getUniqueId())).getName()));

						RageScores.addPointsToPlayer(Bukkit.getPlayer(explosionVictims.get(deceased.getUniqueId())),
								deceased, "explosion");
					} else if (grenadeExplosionVictims.containsKey(deceased.getUniqueId())) {
						if (doDeathBroadcast)
							GameBroadcast.broadcastToGame(game, RageMode.getLang().get("game.broadcast.explosion-kill", "%victim%", deceased.getName(),
									"%killer%", Bukkit.getPlayer(grenadeExplosionVictims.get(deceased.getUniqueId())).getName()));

					RageScores.addPointsToPlayer(Bukkit.getPlayer(grenadeExplosionVictims.get(deceased.getUniqueId())),
							deceased, "explosion");
					} else {
						if (doDeathBroadcast)
							GameBroadcast.broadcastToGame(game, RageMode.getLang().get("game.broadcast.error-kill"));

						deceased.sendMessage(RageMode.getLang().get("game.unknown-killer"));
					}
				} else {
					if (doDeathBroadcast)
						GameBroadcast.broadcastToGame(game, RageMode.getLang().get("game.unknown-weapon", "%victim%", deceased.getName()));
				}
				event.setDroppedExp(0);
				event.setDeathMessage("");
				event.setKeepInventory(true);
			}

			playerVars(deceased);
		}
	}

	private void playerVars(Player deceased) {
		GameSpawnGetter gameSpawnGetter = new GameSpawnGetter(PlayerList.getPlayersGame(deceased));
		List<Location> spawns = gameSpawnGetter.getSpawnLocations();

		Random rand = new Random();
		int x = rand.nextInt(spawns.size() - 1);

		deceased.setHealth(20);

		deceased.teleport(spawns.get(x));

		// give him a new set of items
		//deceased.getInventory().clear();

		FileConfiguration conf = plugin.getConfiguration().getCfg();
		deceased.getInventory().setItem(conf.getInt("items.rageBow.slot"), hu.montlikadani.ragemode.items.RageBow.getRageBow());
		deceased.getInventory().setItem(conf.getInt("items.rageKnife.slot"), RageKnife.getRageKnife());
		deceased.getInventory().setItem(conf.getInt("items.combatAxe.slot"), CombatAxe.getCombatAxe());
		//deceased.getInventory().setItem(conf.getInt("items.rageArrow.slot"), RageArrow.getRageArrow());
		deceased.getInventory().setItem(conf.getInt("items.grenade.slot"), Grenade.getGrenade());
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
		Player p = ev.getPlayer();
		if (PlayerList.isPlayerPlaying(p.getUniqueId().toString())) {
			ItemStack hand = NMS.getItemInHand(p);
			ItemMeta meta = hand.getItemMeta();
			if (meta != null && meta.getDisplayName() != null) {
				if (meta.getDisplayName().equals(LeaveGame.getLeaveGameItemName())) {
					ev.setCancelled(true);
				}
			}
		}
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
			if (((Sign) blockState).getLine(0).contains(RageMode.getLang().colors(plugin.getConfiguration()
					.getCfg().getStringList("signs.list").get(0)))) {
				if (!event.getPlayer().hasPermission("ragemode.admin.signs")) {
					event.getPlayer().sendMessage(RageMode.getLang().get("no-permission-to-interact-sign"));
					event.setCancelled(true);
					return;
				} else SignCreator.removeSign((Sign) blockState);
			}
		}
	}

	@EventHandler
	public void onCommand(PlayerCommandPreprocessEvent event) {
		Player p = event.getPlayer();
		if (PlayerList.isPlayerPlaying(p.getUniqueId().toString()) && !p.hasPermission("ragemode.admin.cmd")) {
			if (event.getMessage() != null) {
				List<String> cmds = plugin.getConfiguration().getCfg().getStringList("game.global.commands");
				String arg = event.getMessage().trim().toLowerCase();

				if (cmds != null && !cmds.isEmpty()) {
					if (cmds.contains(arg)) {
						if (waitingGames.containsKey(PlayerList.getPlayersGame(p))) {
							if (waitingGames.get(PlayerList.getPlayersGame(p)))
								event.setCancelled(true);
						}
					} else {
						p.sendMessage(RageMode.getLang().get("game.this-command-is-disabled-in-game"));
						event.setCancelled(true);
					}
				}
			}
		}
	}

	@EventHandler
	public void eggThrow(PlayerEggThrowEvent event) {
		if (event.getPlayer() == null || !event.getPlayer().isOnline()) return;

		Player p = event.getPlayer();
		if (PlayerList.getStatus() == GameStatus.RUNNING && PlayerList.isPlayerPlaying(p.getUniqueId().toString())) {
			event.setHatching(false); // no baby chickens

			ItemStack hand = NMS.getItemInHand(p);
			hand.setAmount(hand.getAmount() - 1);

			final Item grenade = p.getWorld().dropItem(event.getEgg().getLocation(), new ItemStack(Material.EGG));

			// Cancel egg pick up
			grenade.setPickupDelay(41);

			grenade.setVelocity(p.getEyeLocation().getDirection());
			// move egg from land location to simulate bounce
			grenade.getLocation().add(event.getEgg().getVelocity());

			List<Entity> nears = grenade.getNearbyEntities(13, 13, 13);

			if (RageMode.getVersion().contains("1.8"))
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

	@EventHandler
	public void onMaterialEvents(PlayerInteractEvent event) {
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
					if (meta.getDisplayName().equals(CombatAxe.getCombatAxeName())) {
						thrower.launchProjectile(org.bukkit.entity.Snowball.class);
						NMS.setItemInHand(thrower, null);
					}
				}
			}

			ItemStack hand = NMS.getItemInHand(p);
			ItemMeta meta = hand.getItemMeta();
			if (meta != null && meta.getDisplayName() != null) {
				if (meta.getDisplayName().equals(LeaveGame.getLeaveGameItemName()))
					p.performCommand("rm leave");
			}
		}

		if (plugin.getConfiguration().getCfg().getBoolean("signs.enable") && event.getClickedBlock() != null
				&& event.getClickedBlock().getState() != null && event.getAction() != Action.LEFT_CLICK_BLOCK) {
			if (event.getClickedBlock().getState() instanceof Sign) {
				if (event.getAction() == Action.RIGHT_CLICK_BLOCK) {
					Sign sign = (Sign) event.getClickedBlock().getState();
					if (sign.getLine(0).contains(RageMode.getLang().colors(plugin.getConfiguration()
							.getCfg().getStringList("signs.list").get(0))) && SignCreator.isJoinSign()) {
						Player player = event.getPlayer();
						if (player.hasPermission("ragemode.join.sign")) {
							String arena = SignCreator.getGameFromString();
							MapChecker mapChecker = new MapChecker(arena);
							if (mapChecker.isValid()) {
								String path = "arenas." + arena + ".lobby";
								Configuration conf = plugin.getConfiguration();
								String world = conf.getArenasCfg().getString(path + ".world");
								double lobbyX = conf.getArenasCfg().getDouble(path + ".x");
								double lobbyY = conf.getArenasCfg().getDouble(path + ".y");
								double lobbyZ = conf.getArenasCfg().getDouble(path + ".z");
								double lobbyYaw = conf.getArenasCfg().getDouble(path + ".yaw");
								double lobbyPitch = conf.getArenasCfg().getDouble(path + ".pitch");

								Location lobbyLocation = new Location(Bukkit.getWorld(world), lobbyX, lobbyY, lobbyZ);
								lobbyLocation.setYaw((float) lobbyYaw);
								lobbyLocation.setPitch((float) lobbyPitch);

								org.bukkit.inventory.PlayerInventory inv = player.getInventory();
								if (PlayerList.addPlayer(player, arena)) {
									PlayerList.oldLocations.addToBoth(player, player.getLocation());
									PlayerList.oldInventories.addToBoth(player, inv.getContents());
									PlayerList.oldArmor.addToBoth(player, inv.getArmorContents());
									PlayerList.oldHealth.addToBoth(player, player.getHealth());
									PlayerList.oldHunger.addToBoth(player, player.getFoodLevel());
									PlayerList.oldEffects.addToBoth(player, player.getActivePotionEffects());
									PlayerList.oldGameMode.addToBoth(player, player.getGameMode());
									PlayerList.oldDisplayName.addToBoth(player, player.getDisplayName());
									PlayerList.oldListName.addToBoth(player, player.getPlayerListName());
									PlayerList.oldFire.addToBoth(p, p.getFireTicks());

									inv.clear();
									player.teleport(lobbyLocation);
									player.setGameMode(GameMode.SURVIVAL);
									player.setHealth(20);
									player.setFoodLevel(20);
									player.setFireTicks(0);
									for (PotionEffect e : player.getActivePotionEffects()) {
										player.removePotionEffect(e.getType());
									}
									inv.setHelmet(null);
									inv.setChestplate(null);
									inv.setLeggings(null);
									inv.setBoots(null);
									player.setDisplayName(player.getName());
									player.setPlayerListName(player.getName());

									if (conf.getCfg().contains("items.leavegameitem"))
										inv.setItem(conf.getCfg().getInt("items.leavegameitem.slot"), LeaveGame.getLeaveGameItem());

									if (conf.getCfg().getBoolean("game.global.tablist.player-format.enable"))
										PlayerList.setPlayerGroup(p, conf.getCfg().getString("game.global.tablist.player-format.prefix"),
												conf.getCfg().getString("game.global.tablist.player-format.suffix"), true);

									GameBroadcast.broadcastToGame(arena, RageMode.getLang().get("game.player-joined", "%player%", player.getName()));
									String title = conf.getCfg().getString("titles.join-game.title");
									String subtitle = conf.getCfg().getString("titles.join-game.subtitle");
									if (title != null && !title.equals("") && subtitle != null && !subtitle.equals("")) {
										title = title.replace("%game%", arena);
										subtitle = subtitle.replace("%game%", arena);
										Titles.sendTitle(player, conf.getCfg().getInt("titles.join-game.fade-in"), conf.getCfg().getInt("titles.join-game.stay"),
												conf.getCfg().getInt("titles.join-game.fade-out"), title, subtitle);
									}
									SignCreator.updateAllSigns(arena);
								} else
									RageMode.logConsole(Level.INFO, RageMode.getLang().get("game.player-could-not-join", "%player%", player.getName(), "%game%", arena));
							} else
								player.sendMessage(mapChecker.getMessage());
						} else
							player.sendMessage(RageMode.getLang().get("no-permission"));
					}
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
	public void onInventoryInteract(InventoryClickEvent event) {
		if (PlayerList.isPlayerPlaying(event.getWhoClicked().getUniqueId().toString()))
			event.setCancelled(true);
	}

	@EventHandler(priority = EventPriority.HIGHEST)
	public void onFly(PlayerToggleFlightEvent event) {
		if (!event.isCancelled() && PlayerList.isPlayerPlaying(event.getPlayer().getUniqueId().toString()))
			event.setCancelled(true);
	}

	@EventHandler
	private void PlayerBedEnter(PlayerBedEnterEvent event) {
		if (PlayerList.isPlayerPlaying(event.getPlayer().getUniqueId().toString()))
			event.setCancelled(true);
	}

	// For 1.8
	@SuppressWarnings("deprecation")
	@EventHandler
	public void onItemPickedUpEvent(org.bukkit.event.player.PlayerPickupItemEvent event) {
		if (PlayerList.isPlayerPlaying(event.getPlayer().getUniqueId().toString()))
			event.setCancelled(true);
	}

	// For 1.12
	@EventHandler
	public void onItemPickUpEvent(EntityPickupItemEvent e) {
		if (e.isCancelled() || !(e.getEntity() instanceof Player))
			return;

		if (PlayerList.isPlayerPlaying(((Player) e.getEntity()).getUniqueId().toString()))
			e.setCancelled(true);
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
					SignCreator.updateSign((Sign) event.getBlock().getState());
				}
			}
		}
	}

	@EventHandler
	public void onPlayerMove(PlayerMoveEvent event) {
		if (PlayerList.getStatus() == GameStatus.RUNNING && PlayerList.isPlayerPlaying(event.getPlayer().getUniqueId().toString())) {
			Player p = event.getPlayer();
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

			if (p.getLocation().getY() < 0) {
				GameSpawnGetter gameSpawnGetter = new GameSpawnGetter(PlayerList.getPlayersGame(p));
				List<Location> spawns = gameSpawnGetter.getSpawnLocations();

				Random rand = new Random();
				int x = rand.nextInt(spawns.size() - 1);

				p.teleport(spawns.get(x));
				GameBroadcast.broadcastToGame(PlayerList.getPlayersGame(p), RageMode.getLang().get("game.void-fall", "%player%", p.getName()));
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
