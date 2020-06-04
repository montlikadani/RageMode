package hu.montlikadani.ragemode.items.threads;

import org.bukkit.entity.Entity;
import org.bukkit.entity.Item;
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
						for (Entity entity : item.getNearbyEntities(0.3D, 0.3D, 0.3D)) {
							if (!(entity instanceof Player)) {
								continue;
							}

							final Player victim = (Player) entity;
							if (victim == player) {
								continue;
							}

							victim.removeMetadata("killedWith", RageMode.getInstance());
							victim.setMetadata("killedWith",
									new FixedMetadataValue(RageMode.getInstance(), "combataxe"));
							victim.damage(25D, player);
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
