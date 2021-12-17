package world.bentobox.extramobs.listeners;


import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Biome;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.EntityType;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.eclipse.jdt.annotation.NonNull;

import java.util.Optional;
import java.util.Random;

import world.bentobox.bentobox.api.addons.GameModeAddon;
import world.bentobox.extramobs.ExtraMobsAddon;


/**
 * This listener checks for Zombie Pigmen and Enderman spawning so it could be replaced
 * with blaze, wither or shulker
 */
public class MobsSpawnListener implements Listener
{
	/**
	 * Constructor MobsSpawnListener creates a new MobsSpawnListener instance.
	 *
	 * @param addon of type ExtraMobsAddon
	 */
	public MobsSpawnListener(ExtraMobsAddon addon)
	{
		this.addon = addon;
		this.spawningRandom = new Random();
	}


	/**
	 * This method replaces zombie pigments or endermans with blaze or wither skeleton or
	 * shulker based on random chance.
	 * @param event Creature spawining event
	 */
	@EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
	public void onEntitySpawn(CreatureSpawnEvent event)
	{
		if (event.getSpawnReason() != CreatureSpawnEvent.SpawnReason.NATURAL)
		{
			// Effect only natural mob spawning.
			return;
		}

		World world = event.getLocation().getWorld();

		Optional<GameModeAddon> optionalAddon =
			this.addon.getPlugin().getIWM().getAddon(world);

		if (!optionalAddon.isPresent() ||
			!this.addon.getSettings().getDisabledGameModes().isEmpty() &&
				this.addon.getSettings().getDisabledGameModes().contains(
					optionalAddon.get().getDescription().getName()))
		{
			// GameMode addon is not in enable list.
			return;
		}


		if ((event.getEntityType().name().equals("PIG_ZOMBIE") ||
			event.getEntityType().name().equals("ZOMBIFIED_PIGLIN")) &&
			this.addon.getPlugin().getIWM().isIslandNether(world))
		{
			// replace pigmen with blaze or wither

			if (this.isSuitableNetherLocation(event.getLocation()))
			{
				if (this.spawningRandom.nextDouble() < this.addon.getSettings().getWitherSkeletonChance())
				{
					// oOo wither skeleton got lucky.
					this.summonEntity(event.getLocation(), EntityType.WITHER_SKELETON);
					event.setCancelled(true);
				}
				else if (this.spawningRandom.nextDouble() < this.addon.getSettings().getBlazeChance())
				{
					// oOo blaze got lucky.
					this.summonEntity(event.getLocation(), EntityType.BLAZE);
					event.setCancelled(true);
				}
			}
		}
		else if (event.getEntityType() == EntityType.ENDERMAN &&
			this.addon.getPlugin().getIWM().isIslandEnd(world))
		{
			// replace enderman with shulker
			if (this.isSuitableEndLocation(event.getLocation()))
			{
				if (this.spawningRandom.nextDouble() < this.addon.getSettings().getShulkerChance())
				{
					// oOo shulker got lucky.
					this.summonEntity(event.getLocation(), EntityType.SHULKER);
					event.setCancelled(true);
				}
			}
		}
		else if (world.getEnvironment() == World.Environment.NORMAL &&
			(event.getEntityType() == EntityType.COD ||
				event.getEntityType() == EntityType.SALMON ||
				event.getEntityType() == EntityType.TROPICAL_FISH))
		{
			// Check biome
			Biome biome = world.getBiome(
				event.getLocation().getBlockX(),
				event.getLocation().getBlockY(),
				event.getLocation().getBlockZ());

			if (biome == Biome.DEEP_OCEAN ||
				biome == Biome.DEEP_COLD_OCEAN ||
				biome == Biome.DEEP_FROZEN_OCEAN ||
				biome == Biome.DEEP_LUKEWARM_OCEAN)
			{
				// Monuments are located only in Deep Ocean. So guardians will spawn there.

				if (this.isSuitableGuardianLocation(event.getLocation()))
				{
					if (this.spawningRandom.nextDouble() < this.addon.getSettings().getGuardianChance())
					{
						// oOo guardian got lucky.
						this.summonEntity(event.getLocation(), EntityType.GUARDIAN);
						event.setCancelled(true);
					}
				}
			}
		}
	}


	/**
	 * This method checks if given location is available for spawning blaze or wither
	 * skeleton.
	 * @param location Location where mob is standing.
	 * @return {@code true} if mob is spawned on nether brick, slab or stair, {@code false} otherwise.
	 */
	private boolean isSuitableNetherLocation(Location location)
	{
		Material material = location.getBlock().getRelative(BlockFace.DOWN).getType();

		return material == Material.NETHER_BRICKS ||
			material == Material.NETHER_BRICK_SLAB ||
			material == Material.NETHER_BRICK_STAIRS;
	}


	/**
	 * This method checks if given location is available for spawning shulker.
	 * @param location Location where mob is standing.
	 * @return {@code true} if mob is spawned on purpur block, slab or stair, {@code false} otherwise.
	 */
	private boolean isSuitableEndLocation(Location location)
	{
		Material material = location.getBlock().getRelative(BlockFace.DOWN).getType();

		return material == Material.PURPUR_BLOCK ||
			material == Material.PURPUR_SLAB ||
			material == Material.PURPUR_STAIRS;
	}


	/**
	 * This method checks if given location is available for spawning guardian.
	 * @param location Location where mob is standing.
	 * @return {@code true} if mob is spawned below prismarine, {@code false} otherwise.
	 */
	private boolean isSuitableGuardianLocation(Location location)
	{
		// Current block
		Block block = location.getBlock();

		while (block != null && block.getType() == Material.WATER)
		{
			// Find first top block that is not a water.
			block = block.getRelative(BlockFace.UP);
		}

		Material material = block.getType();

		return material == Material.PRISMARINE ||
			material == Material.PRISMARINE_SLAB ||
			material == Material.PRISMARINE_STAIRS ||
			material == Material.PRISMARINE_BRICKS ||
			material == Material.PRISMARINE_BRICK_SLAB ||
			material == Material.PRISMARINE_BRICK_STAIRS ||
			material == Material.DARK_PRISMARINE ||
			material == Material.DARK_PRISMARINE_SLAB ||
			material == Material.DARK_PRISMARINE_STAIRS;
	}


	/**
	 * This method spawns entity in given location.
	 * @param location Location where entity must be summoned.
	 * @param type Type of entity that must be summoned.
	 */
	private void summonEntity(@NonNull Location location, @NonNull EntityType type)
	{
		location.getWorld().spawnEntity(location, type);
	}


// ---------------------------------------------------------------------
// Section: Variables
// ---------------------------------------------------------------------

	/**
	 * Instance of ExtraMobs Addon.
	 */
	private ExtraMobsAddon addon;

	/**
	 * Single random instance.
	 */
	private Random spawningRandom;
}
