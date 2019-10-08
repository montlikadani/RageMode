package hu.montlikadani.ragemode.utils;

import java.util.HashSet;
import java.util.Set;

import org.bukkit.Material;

public class MaterialUtil {

	private static Set<Material> WALL_SIGNS = new HashSet<>();
	private static Set<Material> WOODEN_PRESSURE_PLATES = new HashSet<>();
	private static Set<Material> TRAPWOODEN_DOORS = new HashSet<>();
	private static Set<Material> BUTTONS = new HashSet<>();
	private static Set<Material> WOODEN_DOORS = new HashSet<>();

	static {
		WALL_SIGNS.clear();
		WOODEN_PRESSURE_PLATES.clear();
		TRAPWOODEN_DOORS.clear();
		BUTTONS.clear();
		WOODEN_DOORS.clear();

		WALL_SIGNS.add(getMat("WALL_SIGN"));
		WOODEN_PRESSURE_PLATES.add(getMat("WOODEN_PRESSURE_PLATE"));
		TRAPWOODEN_DOORS.add(getMat("TRAP_DOOR"));
		BUTTONS.add(getMat("WOODEN_BUTTON"));
		WOODEN_DOORS.add(getMat("WOODEN_DOOR"));

		BUTTONS.add(getMat("STONE_BUTTON"));
		TRAPWOODEN_DOORS.add(getMat("IRON_TRAPDOOR"));
		WOODEN_DOORS.add(getMat("ACACIA_DOOR"));
		WOODEN_DOORS.add(getMat("BIRCH_DOOR"));
		WOODEN_DOORS.add(getMat("DARK_OAK_DOOR"));
		WOODEN_DOORS.add(getMat("JUNGLE_DOOR"));
		WOODEN_DOORS.add(getMat("SPRUCE_DOOR"));

		WALL_SIGNS.add(getMat("ACACIA_WALL_SIGN"));
		WALL_SIGNS.add(getMat("BIRCH_WALL_SIGN"));
		WALL_SIGNS.add(getMat("DARK_OAK_WALL_SIGN"));
		WALL_SIGNS.add(getMat("JUNGLE_WALL_SIGN"));
		WALL_SIGNS.add(getMat("OAK_WALL_SIGN"));
		WALL_SIGNS.add(getMat("SPRUCE_WALL_SIGN"));
		WOODEN_PRESSURE_PLATES.add(getMat("OAK_PRESSURE_PLATE"));
		WOODEN_PRESSURE_PLATES.add(getMat("ACACIA_PRESSURE_PLATE"));
		WOODEN_PRESSURE_PLATES.add(getMat("BIRCH_PRESSURE_PLATE"));
		WOODEN_PRESSURE_PLATES.add(getMat("DARK_OAK_PRESSURE_PLATE"));
		WOODEN_PRESSURE_PLATES.add(getMat("JUNGLE_PRESSURE_PLATE"));
		WOODEN_PRESSURE_PLATES.add(getMat("SPRUCE_PRESSURE_PLATE"));
		TRAPWOODEN_DOORS.add(getMat("OAK_TRAPDOOR"));
		TRAPWOODEN_DOORS.add(getMat("ACACIA_TRAPDOOR"));
		TRAPWOODEN_DOORS.add(getMat("BIRCH_TRAPDOOR"));
		TRAPWOODEN_DOORS.add(getMat("DARK_OAK_TRAPDOOR"));
		TRAPWOODEN_DOORS.add(getMat("JUNGLE_TRAPDOOR"));
		TRAPWOODEN_DOORS.add(getMat("SPRUCE_TRAPDOOR"));
		BUTTONS.add(getMat("OAK_BUTTON"));
		BUTTONS.add(getMat("ACACIA_BUTTON"));
		BUTTONS.add(getMat("BIRCH_BUTTON"));
		BUTTONS.add(getMat("DARK_OAK_BUTTON"));
		BUTTONS.add(getMat("JUNGLE_BUTTON"));
		BUTTONS.add(getMat("SPRUCE_BUTTON"));
		WOODEN_DOORS.add(getMat("OAK_DOOR"));
	}

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
		return Material.getMaterial(name);
	}
}
