package hu.montlikadani.ragemode.events;

import hu.montlikadani.ragemode.RageMode;
import hu.montlikadani.ragemode.gameLogic.GameSpawnGetter;
import hu.montlikadani.ragemode.gameLogic.PlayerList;
import hu.montlikadani.ragemode.holo.HoloHolder;
import hu.montlikadani.ragemode.items.CombatAxe;
import hu.montlikadani.ragemode.items.RageArrow;
import hu.montlikadani.ragemode.items.RageBow;
import hu.montlikadani.ragemode.items.RageKnife;
import hu.montlikadani.ragemode.scores.RageScores;
import hu.montlikadani.ragemode.signs.SignCreator;
import hu.montlikadani.ragemode.toolbox.GameBroadcast;
import hu.montlikadani.ragemode.toolbox.GetGames;
import hu.montlikadani.ragemode.toolbox.MapChecker;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Sign;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerPickupItemEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.UUID;
import java.util.logging.Level;

public class EventListener implements Listener {

    public static HashMap<String, Boolean> waitingGames = new HashMap<>();
    private RageMode plugin;
    private Map<UUID, UUID> explosionVictims = new HashMap<>();

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

        if(PlayerList.isPlayerPlaying(player.getUniqueId().toString())) {
            if(PlayerList.removePlayer(player)) {
                Bukkit.getLogger().log(Level.INFO, "Player " + player.getName() + " left the server while playing.");

                if(plugin.getConfiguration().getCfg().getBoolean("game.global.run-commands-for-player-left-while-playing.enable")) {
                    for(String cmds : plugin.getConfiguration().getCfg().getStringList("game.global.run-commands-for-player-left-while-playing.commands")) {
                        cmds = cmds.replace("%player%", player.getName());
                        cmds = cmds.replace("%player-ip%", player.getAddress().getAddress().getHostAddress());
                        Bukkit.dispatchCommand(Bukkit.getServer().getConsoleSender(), cmds);
                    }
                }
            }
        }
        HoloHolder.deleteHoloObjectsOfPlayer(player);
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onProjectileHit(ProjectileHitEvent event) {
        // RageArrow explosion event
        if(event.getEntity() instanceof Arrow) {
            Arrow arrow = (Arrow) event.getEntity();
            if(arrow.getShooter() instanceof Player) {
                Player shooter = (Player) arrow.getShooter();
                if(PlayerList.isPlayerPlaying(shooter.getUniqueId().toString())) {
                    if(waitingGames.containsKey(PlayerList.getPlayersGame(shooter))) {
                        if(waitingGames.get(PlayerList.getPlayersGame(shooter)))
                            return;
                    }

                    Location location = arrow.getLocation();
                    World world = arrow.getWorld();
                    double x = location.getX();
                    double y = location.getY();
                    double z = location.getZ();

                    List<Entity> nears = arrow.getNearbyEntities(10, 10, 10);

                    world.createExplosion(x, y, z, 2f, false, false); // original 4f
                    arrow.remove();

                    int i = 0;
                    int imax = nears.size();
                    while(i < imax) {
                        if(nears.get(i) instanceof Player /*
                         * && !nears.get(i).
                         * getUniqueId().
                         * toString().equals(
                         * shooter.getUniqueId()
                         * .toString())
                         */) {
                            Player near = (Player) nears.get(i);
                            if(explosionVictims != null) {
                                if(explosionVictims.containsKey(near.getUniqueId())) {
                                    explosionVictims.remove(near.getUniqueId());
                                    explosionVictims.put(near.getUniqueId(), shooter.getUniqueId());
                                }
                            }
                            explosionVictims.put(near.getUniqueId(), shooter.getUniqueId());
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
        if(event.getDamager() instanceof Player && event.getEntity() instanceof Player) {
            Player killer = (Player) event.getDamager();
            Player victim = (Player) event.getEntity();
            if(PlayerList.isPlayerPlaying(killer.getUniqueId().toString())
                    && PlayerList.isPlayerPlaying(victim.getUniqueId().toString())) {
                if(waitingGames.containsKey(PlayerList.getPlayersGame(killer))) {
                    if(waitingGames.get(PlayerList.getPlayersGame(killer))) {
                        event.setCancelled(true);
                        return;
                    }
                }
                ItemMeta meta = killer.getItemInHand().getItemMeta();
                if(killer.getItemInHand() != null && meta != null && meta.getDisplayName() != null) {
                    if(meta.getDisplayName().equals(ChatColor.GOLD + "RageKnife")) {
                        // TODO check if "killer.getItemInHand() instanceof
                        // MATERIAL.SHEARS" also works (maybe more stable)
                        event.setDamage(25);
                    }
                }
            }
            if(PlayerList.isPlayerPlaying(victim.getUniqueId().toString())) {
                if(!PlayerList.isGameRunning(PlayerList.getPlayersGame(victim)))
                    event.setDamage(0);
            }
        }
        // TODO add Constant for "RageKnife" for unexpected error preventing
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onItemSpawn(PlayerDropItemEvent event) {
        if(PlayerList.isPlayerPlaying(event.getPlayer().getUniqueId().toString()))
            event.setCancelled(true);
    }

    @EventHandler
    public void onArrowHitPlayer(EntityDamageEvent event) {
        // Arrow hit player event
        if(event.getEntity() instanceof Player) {
            if(PlayerList.isPlayerPlaying(event.getEntity().getUniqueId().toString())) {
                if(event.getCause().equals(DamageCause.PROJECTILE)) {
                    Player victim = (Player) event.getEntity();
                    if(PlayerList.isPlayerPlaying(victim.getUniqueId().toString())) {
                        if(waitingGames.containsKey(PlayerList.getPlayersGame(victim))) {
                            if(waitingGames.get(PlayerList.getPlayersGame(victim))) {
                                event.setDamage(0);
                                event.setCancelled(true);
                                return;
                            }
                        }
                        if(event.getDamage() == 0.0d)
                            event.setDamage(28.34d);
                        else
                            event.setDamage(27.114);
                    }
                }
                if(event.getCause().equals(DamageCause.FALL))
                    event.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {
        // Player autorespawn
        Player deceased;
        if(event.getEntity() instanceof Player && event.getEntity() != null)
            deceased = event.getEntity();
        else {
            deceased = null;
            return;
        }
        if(PlayerList.isPlayerPlaying(deceased.getUniqueId().toString())) {
            if((deceased.getKiller() != null && PlayerList.isPlayerPlaying(deceased.getKiller().getUniqueId().toString()))
                    || deceased.getKiller() == null) {
                String game = PlayerList.getPlayersGame(deceased);

                boolean doDeathBroadcast = plugin.getConfiguration().getCfg().getBoolean("game.global.death-messages");

                if(plugin.getConfiguration().getArenasCfg().isSet("arenas." + PlayerList.getPlayersGame(deceased) + ".death-messages")) {
                    String gameBroadcast = plugin.getConfiguration().getArenasCfg().getString("arenas." + PlayerList.getPlayersGame(deceased)
                            + ".death-messages");
                    if(gameBroadcast != null && !gameBroadcast.equals("")) {
                        if(gameBroadcast.equals("true") || gameBroadcast.equals("false"))
                            doDeathBroadcast = Boolean.parseBoolean(gameBroadcast);
                    }
                }


                if(deceased.getLastDamage() == 0.0f) {
                    if(deceased.getKiller() == null) {
                        if(doDeathBroadcast) {
                            GameBroadcast.broadcastToGame(game, RageMode.getLang().get("broadcast-axe-kill", "%victim%", deceased.getName(),
                                    "%killer%", deceased.getName()));
                        }
                        RageScores.addPointsToPlayer(deceased, deceased, "combataxe");
                    } else {
                        if(doDeathBroadcast) {
                            GameBroadcast.broadcastToGame(game, RageMode.getLang().get("broadcast-axe-kill", "%victim%", deceased.getName(),
                                    "%killer%", deceased.getKiller().getName()));
                        }
                        RageScores.addPointsToPlayer(deceased.getKiller(), deceased, "combataxe");
                    }
                } else if(deceased.getLastDamageCause().getCause().equals(DamageCause.PROJECTILE)) {
                    if(deceased.getKiller() == null) {
                        if(doDeathBroadcast) {
                            GameBroadcast.broadcastToGame(game, RageMode.getLang().get("broadcast-arrow-kill", "%victim%", deceased.getName(),
                                    "%killer%", deceased.getName()));
                        }
                        RageScores.addPointsToPlayer(deceased, deceased, "ragebow");
                    } else {
                        if(doDeathBroadcast) {
                            GameBroadcast.broadcastToGame(game, RageMode.getLang().get("broadcast-arrow-kill", "%victim%", deceased.getName(),
                                    "%killer%", deceased.getKiller().getName()));
                        }
                        RageScores.addPointsToPlayer(deceased.getKiller(), deceased, "ragebow");
                    }
                } else if(deceased.getLastDamageCause().getCause().equals(DamageCause.ENTITY_ATTACK)) {
                    if(deceased.getKiller() == null) {
                        if(doDeathBroadcast) {
                            GameBroadcast.broadcastToGame(game, RageMode.getLang().get("broadcast-knife-kill", "%victim%", deceased.getName(),
                                    "%killer%", deceased.getName()));
                        }
                        RageScores.addPointsToPlayer(deceased, deceased, "rageknife");
                    } else {
                        if(doDeathBroadcast) {
                            GameBroadcast.broadcastToGame(game, RageMode.getLang().get("broadcast-knife-kill", "%victim%", deceased.getName(),
                                    "%killer%", deceased.getKiller().getName()));
                        }
                        RageScores.addPointsToPlayer(deceased.getKiller(), deceased, "rageknife");
                    }
                } else if(deceased.getLastDamageCause().getCause().equals(DamageCause.BLOCK_EXPLOSION)) {
                    if(explosionVictims.containsKey(deceased.getUniqueId())) {
                        if(doDeathBroadcast) {
                            GameBroadcast.broadcastToGame(game, RageMode.getLang().get("broadcast-explosion-kill", "%victim%", deceased.getName(),
                                    "%killer%", Bukkit.getPlayer(explosionVictims.get(deceased.getUniqueId())).getName()));
                        }
                        RageScores.addPointsToPlayer(Bukkit.getPlayer(explosionVictims.get(deceased.getUniqueId())),
                                deceased, "explosion");
                    } else {
                        if(doDeathBroadcast)
                            GameBroadcast.broadcastToGame(game, RageMode.getLang().get("game.broadcast-error-kill"));
                        deceased.sendMessage(RageMode.getLang().get("game.unknown-killer"));
                    }
                } else {
                    if(doDeathBroadcast)
                        GameBroadcast.broadcastToGame(game, RageMode.getLang().get("unknown-weapon", "%victim%", deceased.getName()));
                }
                event.setDeathMessage("");
            }

            event.setKeepInventory(true);
            GameSpawnGetter gameSpawnGetter = new GameSpawnGetter(PlayerList.getPlayersGame(deceased));
            List<Location> spawns = gameSpawnGetter.getSpawnLocations();
            // Location[] aSpawns = (Location[]) spawns.toArray(); ----> performance optimization

            Random rand = new Random();
            int x = rand.nextInt(spawns.size() - 1); // ----> performance optimization

            deceased.setHealth(20);

            deceased.teleport(spawns.get(x)); // ----> performance optimization

            // give him a new set of items
            // deceased.getInventory().clear();
            deceased.getInventory().setItem(0, RageBow.getRageBow());
            deceased.getInventory().setItem(1, RageKnife.getRageKnife());
            deceased.getInventory().setItem(9, RageArrow.getRageArrow());
            deceased.getInventory().setItem(2, CombatAxe.getCombatAxe());

        }
    }

    @EventHandler
    public void onHungerGain(FoodLevelChangeEvent event) {
        if(event.getEntity() instanceof Player) {
            Player player = (Player) event.getEntity();
            if(PlayerList.isPlayerPlaying(player.getUniqueId().toString()))
                event.setCancelled(true);
        }
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        if(PlayerList.isPlayerPlaying(event.getPlayer().getUniqueId().toString()))
            event.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onSignBreak(BlockBreakEvent event) {
        if(event.getBlock().getState() instanceof Sign && !event.isCancelled())
            SignCreator.removeSign((Sign) event.getBlock().getState());
    }

    @EventHandler
    public void onCommand(PlayerCommandPreprocessEvent event) {
        Player p = event.getPlayer();
        if(PlayerList.isPlayerPlaying(p.getUniqueId().toString()) && !p.hasPermission("ragemode.admin.cmd")) {
            if(event.getMessage() != null) {
                String cmd = event.getMessage().trim().toLowerCase();
                if(plugin.getConfiguration().getCfg().getStringList("game.global.commands").contains(cmd)) {
                    if(waitingGames.containsKey(PlayerList.getPlayersGame(p))) {
                        if(waitingGames.get(PlayerList.getPlayersGame(p)))
                            event.setCancelled(true);
                    }
                } else {
                    p.sendMessage(RageMode.getLang().get("game.this-command-is-disabled-in-game"));
                    event.setCancelled(true);
                }
            }
        }
    }

    @EventHandler
    public void onCombatAxeThrow(PlayerInteractEvent event) {
        if(PlayerList.isPlayerPlaying(event.getPlayer().getUniqueId().toString())) {
            Player thrower = event.getPlayer();
            if(waitingGames.containsKey(PlayerList.getPlayersGame(thrower))) {
                if(waitingGames.get(PlayerList.getPlayersGame(thrower)))
                    return;
            }
            ItemMeta meta = thrower.getItemInHand().getItemMeta();
            if(thrower.getItemInHand() != null && meta != null && meta.getDisplayName() != null) {
                if(meta.getDisplayName().equals(ChatColor.GOLD + "CombatAxe")) {
                    //Snowball sb = thrower.launchProjectile(Snowball.class);
                    thrower.launchProjectile(org.bukkit.entity.Snowball.class);
                    //thrower.getInventory().setItemInHand(null);
                }
            }
        }

        if(event.getClickedBlock() != null && event.getClickedBlock().getState() != null) {
            if(event.getClickedBlock().getState() instanceof Sign) {
                if(event.getAction() == Action.RIGHT_CLICK_BLOCK) {
                    Sign sign = (Sign) event.getClickedBlock().getState();
                    if(SignCreator.isJoinSign(sign)) {
                        Player player = event.getPlayer();
                        if(player.hasPermission("ragemode.join")) {
                            String arena = SignCreator.getGameFromSign(sign);
                            MapChecker mapChecker = new MapChecker(arena);
                            if(mapChecker.isValid()) {
                                String path = "arenas." + arena + ".lobby";
                                String world = RageMode.getInstance().getConfiguration().getArenasCfg().getString(path + ".world");
                                double lobbyX = RageMode.getInstance().getConfiguration().getArenasCfg().getDouble(path + ".lobby.x");
                                double lobbyY = RageMode.getInstance().getConfiguration().getArenasCfg().getDouble(path + ".lobby.y");
                                double lobbyZ = RageMode.getInstance().getConfiguration().getArenasCfg().getDouble(path + ".lobby.z");
                                double lobbyYaw = RageMode.getInstance().getConfiguration().getArenasCfg().getDouble(path + ".lobby.yaw");
                                double lobbyPitch = RageMode.getInstance().getConfiguration().getArenasCfg().getDouble(path + ".lobby.pitch");

                                Location lobbyLocation = new Location(Bukkit.getWorld(world), lobbyX, lobbyY, lobbyZ);
                                lobbyLocation.setYaw((float) lobbyYaw);
                                lobbyLocation.setPitch((float) lobbyPitch);

                                org.bukkit.inventory.PlayerInventory inv = player.getInventory();
                                if(PlayerList.addPlayer(player, arena)) {
                                    PlayerList.oldLocations.addToBoth(player, player.getLocation());
                                    PlayerList.oldInventories.addToBoth(player, inv.getContents());
                                    PlayerList.oldArmor.addToBoth(player, inv.getArmorContents());
                                    PlayerList.oldHealth.addToBoth(player, player.getHealth());
                                    PlayerList.oldHunger.addToBoth(player, player.getFoodLevel());
                                    PlayerList.oldGameMode.addToBoth(player, player.getGameMode());

                                    inv.clear();
                                    player.teleport(lobbyLocation);
                                    player.setGameMode(GameMode.SURVIVAL);
                                    player.setHealth(20);
                                    player.setFoodLevel(20);
                                    inv.setHelmet(null);
                                    inv.setChestplate(null);
                                    inv.setLeggings(null);
                                    inv.setBoots(null);
                                    GameBroadcast.broadcastToGame(arena, RageMode.getLang().get("game.player-joined", "%player%", player.getName()));
                                    SignCreator.updateAllSigns(arena);
                                } else
                                    Bukkit.getLogger().log(Level.INFO, RageMode.getLang().get("game.player-could-not-join", "%player%", player.getName(), "%game%", arena));
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
    public void onInventoryInteractEvent(InventoryClickEvent event) {
        if(PlayerList.isPlayerPlaying(event.getWhoClicked().getUniqueId().toString()))
            event.setCancelled(true);
    }

    @EventHandler
    public void onItemPickedUpEvent(PlayerPickupItemEvent event) {
        if(PlayerList.isPlayerPlaying(event.getPlayer().getUniqueId().toString()))
            event.setCancelled(true);
    }

    @EventHandler
    public void onSignChange(SignChangeEvent event) {
        Sign sign = (Sign) event.getBlock().getState();

        if(event.getLine(0).equalsIgnoreCase("[rm]") || event.getLine(0).equalsIgnoreCase("[ragemode]")) {
            if(!event.getPlayer().hasPermission("ragemode.admin.signs")) {
                event.setCancelled(true);
                return;
            }

            String[] allGames = GetGames.getGameNames();
            for(String game : allGames) {
                if(event.getLine(1).equalsIgnoreCase(game)) {
                    SignCreator.createNewSign(sign, game);
                    SignCreator.updateSign(event);
                }
            }
        }
    }

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        if(PlayerList.isPlayerPlaying(event.getPlayer().getUniqueId().toString())) {
            Player p = event.getPlayer();
            if(EventListener.waitingGames != null) {
                if(EventListener.waitingGames.containsKey(PlayerList.getPlayersGame(p))) {
                    if(EventListener.waitingGames.get(PlayerList.getPlayersGame(p))) {
                        Location from = event.getFrom();
                        Location to = event.getTo();
                        double x = Math.floor(from.getX());
                        double z = Math.floor(from.getZ());
                        if(Math.floor(to.getX()) != x || Math.floor(to.getZ()) != z) {
                            x += .5;
                            z += .5;
                            p.teleport(new Location(from.getWorld(), x, from.getY(), z, from.getYaw(), from.getPitch()));
                        }
                    }
                }
            }
        }
    }

    @EventHandler
    public void onWorldChangedEvent(PlayerTeleportEvent event) {
        if(PlayerList.isPlayerPlaying(event.getPlayer().getUniqueId().toString())) {
            if(!MapChecker.isGameWorld(PlayerList.getPlayersGame(event.getPlayer()), event.getTo().getWorld())) {
                if(!event.getPlayer().hasMetadata("Leaving"))
                    event.getPlayer().performCommand("rm leave");
                else
                    event.getPlayer().removeMetadata("Leaving", RageMode.getInstance());
            }
        }
    }
}
