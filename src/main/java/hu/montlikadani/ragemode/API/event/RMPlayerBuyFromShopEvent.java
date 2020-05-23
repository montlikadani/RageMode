package hu.montlikadani.ragemode.API.event;

import org.bukkit.entity.Player;

import hu.montlikadani.ragemode.gameLogic.Game;
import hu.montlikadani.ragemode.items.shop.BoughtElements;
import hu.montlikadani.ragemode.items.shop.ShopCategory;

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

	public Player getPlayer() {
		return player;
	}

	public BoughtElements getElements() {
		return elements;
	}

	public ShopCategory getShopCategory() {
		return shopCategory;
	}
}
