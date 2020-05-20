package hu.montlikadani.ragemode.utils;

import java.util.HashSet;
import java.util.Set;

import org.bukkit.Material;

@SuppressWarnings("serial")
public class MaterialUtil {

	private static Set<Material> WALL_SIGNS = new HashSet<Material>() {
		{
			add(getMat("WALL_SIGN"));
			add(getMat("ACACIA_WALL_SIGN"));
			add(getMat("BIRCH_WALL_SIGN"));
			add(getMat("DARK_OAK_WALL_SIGN"));
			add(getMat("JUNGLE_WALL_SIGN"));
			add(getMat("OAK_WALL_SIGN"));
			add(getMat("SPRUCE_WALL_SIGN"));
		}
	};
	private static Set<Material> WOODEN_PRESSURE_PLATES = new HashSet<Material>() {
		{
			add(getMat("WOODEN_PRESSURE_PLATE"));
			add(getMat("OAK_PRESSURE_PLATE"));
			add(getMat("ACACIA_PRESSURE_PLATE"));
			add(getMat("BIRCH_PRESSURE_PLATE"));
			add(getMat("DARK_OAK_PRESSURE_PLATE"));
			add(getMat("JUNGLE_PRESSURE_PLATE"));
			add(getMat("SPRUCE_PRESSURE_PLATE"));
		}
	};
	private static Set<Material> TRAPWOODEN_DOORS = new HashSet<Material>() {
		{
			add(getMat("TRAP_DOOR"));
			add(getMat("IRON_TRAPDOOR"));
			add(getMat("OAK_TRAPDOOR"));
			add(getMat("ACACIA_TRAPDOOR"));
			add(getMat("BIRCH_TRAPDOOR"));
			add(getMat("DARK_OAK_TRAPDOOR"));
			add(getMat("JUNGLE_TRAPDOOR"));
			add(getMat("SPRUCE_TRAPDOOR"));
		}
	};
	private static Set<Material> BUTTONS = new HashSet<Material>() {
		{
			add(getMat("WOODEN_BUTTON"));
			add(getMat("STONE_BUTTON"));
			add(getMat("OAK_BUTTON"));
			add(getMat("ACACIA_BUTTON"));
			add(getMat("BIRCH_BUTTON"));
			add(getMat("DARK_OAK_BUTTON"));
			add(getMat("JUNGLE_BUTTON"));
			add(getMat("SPRUCE_BUTTON"));
		}
	};
	private static Set<Material> WOODEN_DOORS = new HashSet<Material>() {
		{
			add(getMat("WOODEN_DOOR"));
			add(getMat("ACACIA_DOOR"));
			add(getMat("BIRCH_DOOR"));
			add(getMat("DARK_OAK_DOOR"));
			add(getMat("JUNGLE_DOOR"));
			add(getMat("SPRUCE_DOOR"));
			add(getMat("OAK_DOOR"));
		}
	};

	public static boolean isWallSign(Material mat) {
		return WALL_SIGNS.contains(mat);
	}

	public static boolean isWoodenPressurePlate(Material mat) {
		return WOODEN_PRESSURE_PLATES.contains(mat);
	}

	public static boolean isTrapdoor(Material mat) {
		return TRAPWOODEN_DOORS.contains(mat);
	}

	public static boolean isButton(Material mat) {
		return BUTTONS.contains(mat);
	}

	public static boolean isWoodenDoor(Material mat) {
		return WOODEN_DOORS.contains(mat);
	}

	private static Material getMat(String name) {
		return Material.getMaterial(name.toUpperCase());
	}
}
