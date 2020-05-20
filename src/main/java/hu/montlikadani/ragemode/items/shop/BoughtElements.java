package hu.montlikadani.ragemode.items.shop;

import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;

public class BoughtElements {

	private ItemStack item;

	private double cost;
	private int points;

	private PotionEffect potion;

	public BoughtElements(double cost, int points) {
		this.cost = cost;
		this.points = points;
	}

	public BoughtElements(ItemStack item, double cost, int points) {
		this(cost, points);

		this.item = item;
	}

	public BoughtElements(PotionEffect potion, double cost, int points) {
		this(cost, points);

		this.potion = potion;
	}

	public double getCost() {
		return cost;
	}

	public void setCost(double cost) {
		this.cost = cost;
	}

	public int getPoints() {
		return points;
	}

	public void setPoints(int points) {
		this.points = points;
	}

	public ItemStack getItem() {
		return item;
	}

	public void setItem(ItemStack item) {
		this.item = item;
	}

	public PotionEffect getPotion() {
		return potion;
	}

	public void setPotion(PotionEffect potion) {
		this.potion = potion;
	}
}
