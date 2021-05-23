package hu.montlikadani.ragemode.holder.holograms;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.bukkit.Location;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.EntityType;

public class ArmorStands {

	public static TextBuilder holoTextBuilder() {
		return new TextBuilder();
	}

	private Location location;
	private StandHologram[] holograms;

	protected ArmorStands() {
	}

	public StandHologram[] getHolograms() {
		return holograms;
	}

	public void setLocation(Location location) {
		this.location = location;
	}

	public Location getLocation() {
		return location;
	}

	public void delete() {
		if (holograms != null) {
			for (StandHologram hologram : holograms) {
				if (hologram.getArmorStand() != null) {
					hologram.getArmorStand().remove();
				}
			}
		}
	}

	public void append() {
		delete();

		if (location == null || holograms == null) {
			return;
		}

		Location cloned = location.clone();
		double y = cloned.getY();

		for (int i = 0; i < holograms.length; i++) {
			holograms[i].armorStand = getNewEntityArmorStand(cloned, y += -0.27);
		}
	}

	protected ArmorStand getNewEntityArmorStand(Location loc, double y) {
		loc.setY(y);

		ArmorStand stand = (ArmorStand) loc.getWorld().spawnEntity(loc, EntityType.ARMOR_STAND);

		stand.setVisible(false);
		stand.setGravity(false);
		stand.setCustomNameVisible(true);
		return stand;
	}

	public static final class StandHologram {

		private String textLine = "";
		private ArmorStand armorStand;

		public StandHologram(String textLine) {
			this.textLine = textLine;
		}

		public void setArmorStand(ArmorStand armorStand) {
			this.armorStand = armorStand;
		}

		public ArmorStand getArmorStand() {
			return armorStand;
		}

		public String getTextLine() {
			return textLine;
		}
	}

	public static final class TextBuilder {

		private final List<String> texts = new ArrayList<>();

		private TextBuilder() {
		}

		public TextBuilder addLine(String text) {
			if (!StringUtils.isEmpty(text)) {
				texts.add(text);
			}

			return this;
		}

		public TextBuilder addLines(String... lines) {
			if (lines != null) {
				for (String s : lines) {
					texts.add(s);
				}
			}

			return this;
		}

		public TextBuilder addLines(List<String> list) {
			if (list != null) {
				texts.addAll(list);
			}

			return this;
		}

		/**
		 * Sets the text at the given index. If the text is empty or null, it will be
		 * removed from the given index.
		 * 
		 * @param index the line of text (index >= 0 && index < size)
		 * @param text  the text that should be added
		 * @return {@link TextBuilder}
		 */
		public TextBuilder setLine(int index, String text) {
			if (index < 0 || index >= texts.size()) {
				return this;
			}

			if (StringUtils.isEmpty(text)) {
				texts.remove(index);
				return this;
			}

			texts.set(index, text);
			return this;
		}

		public ArmorStands build() {
			ArmorStands built = new ArmorStands();

			built.holograms = new StandHologram[texts.size()];

			for (int a = 0; a < built.holograms.length; a++) {
				built.holograms[a] = new StandHologram(texts.get(a));
			}

			return built;
		}
	}
}
