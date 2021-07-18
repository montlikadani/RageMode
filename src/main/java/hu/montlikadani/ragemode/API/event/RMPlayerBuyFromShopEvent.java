package hu.montlikadani.ragemode.API.event;

import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import hu.montlikadani.ragemode.gameLogic.base.BaseGame;
import hu.montlikadani.ragemode.items.shop.BoughtElements;
import hu.montlikadani.ragemode.items.shop.ShopCategory;

/**
 * Called when a player bought some item from ragemode shop.
 */
public class RMPlayerBuyFromShopEvent extends GameEvent {

	private Player player;
	private BoughtElements elements;
	private ShopCategory shopCategory;

	public RMPlayerBuyFromShopEvent(BaseGame game, Player player, BoughtElements elements, ShopCategory shopCategory) {
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
	@NotNull
	public Player getPlayer() {
		return player;
	}

	/**
	 * Gets the elements which the player bought before.
	 * 
	 * @return {@link BoughtElements}
	 */
	@NotNull
	public BoughtElements getElements() {
		return elements;
	}

	/**
	 * Gets the current category where the player bought the item(s).
	 * 
	 * @return {@link ShopCategory}
	 */
	@NotNull
	public ShopCategory getShopCategory() {
		return shopCategory;
	}
}
