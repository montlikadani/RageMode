package hu.montlikadani.ragemode.holder.holograms;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.bukkit.Location;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.EntityType;

public class ArmorStands {

	private String[] textLines;
	private Location location;

	private final List<ArmorStand> armorStands = new ArrayList<>();

	protected ArmorStands() {
	}

	public static TextBuilder holoTextBuilder() {
		return new TextBuilder();
	}

	public String[] getTextLines() {
		return textLines;
	}

	public List<ArmorStand> getArmorStands() {
		return armorStands;
	}

	public void setLocation(Location location) {
		this.location = location;
	}

	public Location getLocation() {
		return location;
	}

	public void delete() {
		for (ArmorStand armor : armorStands) {
			armor.remove();
		}

		armorStands.clear();
	}

	public void append() {
		delete();

		if (location == null || textLines == null) {
			return;
		}

		double y = location.getY();

		for (int i = 0; i <= textLines.length - 1; i++) {
			y += -0.27;

			ArmorStand eas = getNewEntityArmorStand(location, y);
			eas.setCustomName(textLines[i]);
			armorStands.add(eas);
		}
	}

	private ArmorStand getNewEntityArmorStand(Location loc, double y) {
		loc.setY(y);

		ArmorStand stand = (ArmorStand) loc.getWorld().spawnEntity(loc, EntityType.ARMOR_STAND);
		stand.setVisible(false);
		stand.setGravity(false);
		stand.setCustomNameVisible(true);
		return stand;
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
		 * Set the text to the given index line. If the text is empty or null, from the
		 * given index the text should be removed.
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
			ArmorStands builtText = new ArmorStands();
			builtText.textLines = texts.toArray(new String[texts.size()]);
			return builtText;
		}
	}
}
