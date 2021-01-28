package hu.montlikadani.ragemode.utils;

import java.util.HashSet;
import java.util.Set;

import org.bukkit.Material;

@SuppressWarnings("serial")
public class MaterialUtil {

	private static final Set<Material> WALL_SIGNS = new HashSet<Material>() {
		{
			add(getMat("WALL_SIGN"));
			add(getMat("ACACIA_WALL_SIGN"));
			add(getMat("BIRCH_WALL_SIGN"));
			add(getMat("DARK_OAK_WALL_SIGN"));
			add(getMat("JUNGLE_WALL_SIGN"));
			add(getMat("OAK_WALL_SIGN"));
			add(getMat("SPRUCE_WALL_SIGN"));
			add(getMat("WARPED_WALL_SIGN"));
			add(getMat("CRIMSON_WALL_SIGN"));
		}
	};
	private static final Set<Material> PRESSURE_PLATES = new HashSet<Material>() {
		{
			add(getMat("WOODEN_PRESSURE_PLATE"));
			add(getMat("OAK_PRESSURE_PLATE"));
			add(getMat("ACACIA_PRESSURE_PLATE"));
			add(getMat("BIRCH_PRESSURE_PLATE"));
			add(getMat("DARK_OAK_PRESSURE_PLATE"));
			add(getMat("JUNGLE_PRESSURE_PLATE"));
			add(getMat("SPRUCE_PRESSURE_PLATE"));
			add(getMat("WARPED_PRESSURE_PLATE"));
			add(getMat("POLISHED_BLACKSTONE_PRESSURE_PLATE"));
			add(getMat("CRIMSON_PRESSURE_PLATE"));
			add(getMat("STONE_PRESSURE_PLATE"));
			add(getMat("HEAVY_WEIGHTED_PRESSURE_PLATE"));
			add(getMat("LIGHT_WEIGHTED_PRESSURE_PLATE"));
		}
	};
	private static final Set<Material> TRAPDOORS = new HashSet<Material>() {
		{
			add(getMat("TRAP_DOOR"));
			add(getMat("IRON_TRAPDOOR"));
			add(getMat("OAK_TRAPDOOR"));
			add(getMat("ACACIA_TRAPDOOR"));
			add(getMat("BIRCH_TRAPDOOR"));
			add(getMat("DARK_OAK_TRAPDOOR"));
			add(getMat("JUNGLE_TRAPDOOR"));
			add(getMat("SPRUCE_TRAPDOOR"));
			add(getMat("WARPED_TRAPDOOR"));
			add(getMat("CRIMSON_TRAPDOOR"));
		}
	};
	private static final Set<Material> BUTTONS = new HashSet<Material>() {
		{
			add(getMat("WOODEN_BUTTON"));
			add(getMat("STONE_BUTTON"));
			add(getMat("OAK_BUTTON"));
			add(getMat("ACACIA_BUTTON"));
			add(getMat("BIRCH_BUTTON"));
			add(getMat("DARK_OAK_BUTTON"));
			add(getMat("JUNGLE_BUTTON"));
			add(getMat("SPRUCE_BUTTON"));
			add(getMat("WARPED_BUTTON"));
			add(getMat("POLISHED_BLACKSTONE_BUTTON"));
			add(getMat("CRIMSON_BUTTON"));
		}
	};
	private static final Set<Material> DOORS = new HashSet<Material>() {
		{
			add(getMat("WOODEN_DOOR"));
			add(getMat("ACACIA_DOOR"));
			add(getMat("BIRCH_DOOR"));
			add(getMat("DARK_OAK_DOOR"));
			add(getMat("JUNGLE_DOOR"));
			add(getMat("SPRUCE_DOOR"));
			add(getMat("WOOD_DOOR"));
			add(getMat("OAK_DOOR"));
			add(getMat("WARPED_DOOR"));
			add(getMat("CRIMSON_DOOR"));
		}
	};
	private static final Set<Material> COMPARATORS = new HashSet<Material>() {
		{
			add(getMat("REDSTONE_COMPARATOR"));
			add(getMat("REDSTONE_COMPARATOR_ON"));
			add(getMat("REDSTONE_COMPARATOR_OFF"));
			add(getMat("COMPARATOR"));
		}
	};

	public static boolean isWallSign(Material mat) {
		return WALL_SIGNS.contains(mat);
	}

	public static boolean isPressurePlate(Material mat) {
		return PRESSURE_PLATES.contains(mat);
	}

	public static boolean isTrapdoor(Material mat) {
		return TRAPDOORS.contains(mat);
	}

	public static boolean isButton(Material mat) {
		return BUTTONS.contains(mat);
	}

	public static boolean isDoor(Material mat) {
		return DOORS.contains(mat);
	}

	public static boolean isComparator(Material mat) {
		return COMPARATORS.contains(mat);
	}

	private static Material getMat(String name) {
		return Material.matchMaterial(name);
	}
}
