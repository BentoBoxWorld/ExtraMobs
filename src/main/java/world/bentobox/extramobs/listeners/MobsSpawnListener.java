package world.bentobox.extramobs.listeners;


import java.util.Optional;
import java.util.Random;
import java.util.Set;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Biome;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Fish;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.eclipse.jdt.annotation.NonNull;

import world.bentobox.bentobox.api.addons.GameModeAddon;
import world.bentobox.extramobs.ExtraMobsAddon;


/**
 * This listener checks for Zombie Pigmen and Enderman spawning so it could be replaced
 * with blaze, wither or shulker
 */
public class MobsSpawnListener implements Listener
{
	private static final Set<Biome> DEEP_OCEAN_BIOMES = Set.of(
		Biome.DEEP_OCEAN,
		Biome.DEEP_COLD_OCEAN,
		Biome.DEEP_FROZEN_OCEAN,
		Biome.DEEP_LUKEWARM_OCEAN);

	private static final Set<Material> NETHER_BRICKS = Set.of(
		Material.NETHER_BRICKS,
		Material.NETHER_BRICK_SLAB,
		Material.NETHER_BRICK_STAIRS);

	private static final Set<Material> PURPUR_BLOCKS = Set.of(
		Material.PURPUR_BLOCK,
		Material.PURPUR_SLAB,
		Material.PURPUR_STAIRS);

	private static final Set<Material> PRISMARINE_BLOCKS = Set.of(
		Material.PRISMARINE,
		Material.PRISMARINE_SLAB,
		Material.PRISMARINE_STAIRS,
		Material.PRISMARINE_BRICKS,
		Material.PRISMARINE_BRICK_SLAB,
		Material.PRISMARINE_BRICK_STAIRS,
		Material.DARK_PRISMARINE,
		Material.DARK_PRISMARINE_SLAB,
		Material.DARK_PRISMARINE_STAIRS);


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
			return;
		}

		World world = event.getLocation().getWorld();
		String gameModeName = this.resolveActiveGameMode(world);

		if (gameModeName == null)
		{
			return;
		}

		EntityType entityType = event.getEntityType();

		if (this.isPiglin(entityType) && this.addon.getPlugin().getIWM().isIslandNether(world))
		{
			this.handleNetherSpawn(event, gameModeName);
		}
		else if (entityType == EntityType.ENDERMAN && this.addon.getPlugin().getIWM().isIslandEnd(world))
		{
			this.handleEndSpawn(event, gameModeName);
		}
		else if (world.getEnvironment() == World.Environment.NORMAL && event.getEntity() instanceof Fish)
		{
			this.handleOverworldSpawn(event, world, gameModeName);
		}
	}


	/**
	 * Resolves the GameMode addon that owns the given world, or {@code null} if no
	 * GameMode applies or the GameMode is in the {@code disabled-gamemodes} list.
	 */
	private String resolveActiveGameMode(World world)
	{
		Optional<GameModeAddon> optionalAddon = this.addon.getPlugin().getIWM().getAddon(world);

		if (optionalAddon.isEmpty())
		{
			return null;
		}

		String name = optionalAddon.get().getDescription().getName();

		if (this.addon.getSettings().getDisabledGameModes().contains(name))
		{
			return null;
		}

		return name;
	}


	private boolean isPiglin(EntityType type)
	{
		return type == EntityType.ZOMBIFIED_PIGLIN || type == EntityType.PIGLIN;
	}


	private void handleNetherSpawn(CreatureSpawnEvent event, String gameModeName)
	{
		if (!this.isSuitableNetherLocation(event.getLocation()))
		{
			return;
		}

		if (this.applyGameModeReplacements(event, gameModeName, "nether"))
		{
			return;
		}

		if (this.spawningRandom.nextDouble() < this.addon.getSettings().getWitherSkeletonChance())
		{
			this.summonEntity(event.getLocation(), EntityType.WITHER_SKELETON);
			event.setCancelled(true);
		}
		else if (this.spawningRandom.nextDouble() < this.addon.getSettings().getBlazeChance())
		{
			this.summonEntity(event.getLocation(), EntityType.BLAZE);
			event.setCancelled(true);
		}
	}


	private void handleEndSpawn(CreatureSpawnEvent event, String gameModeName)
	{
		if (!this.isSuitableEndLocation(event.getLocation()))
		{
			return;
		}

		if (this.applyGameModeReplacements(event, gameModeName, "end"))
		{
			return;
		}

		if (this.spawningRandom.nextDouble() < this.addon.getSettings().getShulkerChance())
		{
			this.summonEntity(event.getLocation(), EntityType.SHULKER);
			event.setCancelled(true);
		}
	}


	private void handleOverworldSpawn(CreatureSpawnEvent event, World world, String gameModeName)
	{
		Biome biome = world.getBiome(
			event.getLocation().getBlockX(),
			event.getLocation().getBlockY(),
			event.getLocation().getBlockZ());

		// Monuments are located only in Deep Ocean. So guardians will spawn there.
		if (!DEEP_OCEAN_BIOMES.contains(biome) || !this.isSuitableGuardianLocation(event.getLocation()))
		{
			return;
		}

		if (this.applyGameModeReplacements(event, gameModeName, "world"))
		{
			return;
		}

		if (this.spawningRandom.nextDouble() < this.addon.getSettings().getGuardianChance())
		{
			this.summonEntity(event.getLocation(), EntityType.GUARDIAN);
			event.setCancelled(true);
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
		return NETHER_BRICKS.contains(location.getBlock().getRelative(BlockFace.DOWN).getType());
	}


	/**
	 * This method checks if given location is available for spawning shulker.
	 * @param location Location where mob is standing.
	 * @return {@code true} if mob is spawned on purpur block, slab or stair, {@code false} otherwise.
	 */
	private boolean isSuitableEndLocation(Location location)
	{
		return PURPUR_BLOCKS.contains(location.getBlock().getRelative(BlockFace.DOWN).getType());
	}


	/**
	 * This method checks if given location is available for spawning guardian.
	 * @param location Location where mob is standing.
	 * @return {@code true} if mob is spawned below prismarine, {@code false} otherwise.
	 */
	private boolean isSuitableGuardianLocation(Location location)
	{
		Block block = location.getBlock();

		while (block.getType() == Material.WATER)
		{
			block = block.getRelative(BlockFace.UP);
		}

		return PRISMARINE_BLOCKS.contains(block.getType());
	}


	/**
	 * Attempts to apply per-gamemode replacement rules for the given environment.
	 *
	 * <p>Iterates through each configured {@link world.bentobox.extramobs.config.MobSpawnReplacement}
	 * rule for {@code gameModeName}/{@code environment}.  For the first rule whose
	 * {@code old} mob matches the spawning entity type and whose random roll succeeds,
	 * the event is cancelled and the replacement entity is summoned.
	 *
	 * @param event        the spawn event (will be cancelled on a successful match).
	 * @param gameModeName GameMode addon name resolved from the world.
	 * @param environment  {@code "nether"}, {@code "end"}, or {@code "world"}.
	 * @return {@code true} if a per-gamemode rule was applied (callers should skip
	 *         further processing); {@code false} if no matching rule was found.
	 */
	private boolean applyGameModeReplacements(
		CreatureSpawnEvent event,
		String gameModeName,
		String environment)
	{
		var rules = this.addon.getSettings().getReplacements(gameModeName, environment);

		if (rules.isEmpty())
		{
			return false;
		}

		for (var rule : rules)
		{
			EntityType oldType = rule.resolveOldEntityType();
			EntityType newType = rule.resolveNewEntityType();

			if (oldType == null || newType == null)
			{
				continue;
			}

			if (event.getEntityType() == oldType
				&& this.spawningRandom.nextDouble() < rule.getChance())
			{
				this.summonEntity(event.getLocation(), newType);
				event.setCancelled(true);
				return true;
			}
		}

		return false;
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
