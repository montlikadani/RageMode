package hu.montlikadani.ragemode.API.event;

import org.bukkit.entity.Player;

import hu.montlikadani.ragemode.gameLogic.Game;
import hu.montlikadani.ragemode.items.shop.BoughtElements;
import hu.montlikadani.ragemode.items.shop.ShopCategory;

/**
 * Called when a player bought some item from ragemode shop.
 */
public class RMPlayerBuyFromShopEvent extends GameEvent {

	private Player player;
	private BoughtElements elements;
	private ShopCategory shopCategory;

	public RMPlayerBuyFromShopEvent(Game game, Player player, BoughtElements elements, ShopCategory shopCategory) {
		super(game);
		this.player = player;
		this.elements = elements;
		this.shopCategory = shopCategory;
	}

	/**
	 * Gets the player who bought an item.
	 * 
	 * @return {@link Player}
	 */
	public Player getPlayer() {
		return player;
	}

	/**
	 * Gets the elements which the player bought before.
	 * 
	 * @return {@link BoughtElements}
	 */
	public BoughtElements getElements() {
		return elements;
	}

	/**
	 * Gets the current category where the player bought the item(s).
	 * 
	 * @return {@link ShopCategory}
	 */
	public ShopCategory getShopCategory() {
		return shopCategory;
	}
}
