package hu.montlikadani.ragemode.items.threads;

import org.bukkit.entity.Entity;
import org.bukkit.entity.Item;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.metadata.FixedMetadataValue;

import hu.montlikadani.ragemode.RageMode;

public class CombatAxeThread {

	private Player player;
	private Item item;

	private boolean running;

	public CombatAxeThread(Player player, Item item) {
		this.player = player;
		this.item = item;
	}

	public void start() {
		running = true;

		if (running) {
			new Thread(() -> {
				while (running) {
					if (item.isDead()) {
						stop();
						return;
					}

					// To prevent async catch
					RageMode.getInstance().getServer().getScheduler().runTask(RageMode.getInstance(), () -> {
						for (Entity entity : item.getNearbyEntities(0.4D, 0.5D, 0.4D)) {
							if (!running) {
								break;
							}

							if (entity instanceof Player) {
								final Player victim = (Player) entity;
								if (victim == player) {
									continue;
								}

								victim.damage(25D, player);
								victim.removeMetadata("killedWith", RageMode.getInstance());
								victim.setMetadata("killedWith",
										new FixedMetadataValue(RageMode.getInstance(), "combataxe"));
							} else if (entity instanceof LivingEntity) {
								((LivingEntity) entity).damage(25D, player);
							}

							item.remove();
							stop();
						}
					});

					if (item.isOnGround()) {
						item.remove();
						stop();
						return;
					}

					try {
						Thread.sleep(10L);
					} catch (InterruptedException e) {
						item.remove();
						stop();
					}
				}
			}).start();
		}
	}

	public void stop() {
		running = false;
	}
}
