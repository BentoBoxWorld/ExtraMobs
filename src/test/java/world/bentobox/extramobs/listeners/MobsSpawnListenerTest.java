package world.bentobox.extramobs.listeners;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.Optional;
import java.util.Set;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Biome;
import org.bukkit.block.Block;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Fish;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;

import world.bentobox.bentobox.api.addons.AddonDescription;
import world.bentobox.bentobox.api.addons.GameModeAddon;
import world.bentobox.extramobs.CommonTestSetup;
import world.bentobox.extramobs.ExtraMobsAddon;
import world.bentobox.extramobs.config.Settings;

/**
 * Tests for {@link MobsSpawnListener}.
 */
class MobsSpawnListenerTest extends CommonTestSetup {

    @Mock
    private ExtraMobsAddon addon;
    @Mock
    private Settings settings;
    @Mock
    private CreatureSpawnEvent event;
    @Mock
    private Block standingBlock;
    @Mock
    private Block blockBelow;
    @Mock
    private GameModeAddon gameModeAddon;

    private MobsSpawnListener listener;

    @Override
    @BeforeEach
    public void setUp() throws Exception {
        super.setUp();

        when(addon.getPlugin()).thenReturn(plugin);
        when(addon.getSettings()).thenReturn(settings);
        when(settings.getDisabledGameModes()).thenReturn(Collections.emptySet());
        when(settings.getWitherSkeletonChance()).thenReturn(0.0);
        when(settings.getBlazeChance()).thenReturn(0.0);
        when(settings.getShulkerChance()).thenReturn(0.0);
        when(settings.getGuardianChance()).thenReturn(0.0);

        // GameMode resolved by default
        AddonDescription desc = new AddonDescription.Builder("main.Class", "BSkyBlock", "1.0").build();
        when(gameModeAddon.getDescription()).thenReturn(desc);
        when(iwm.getAddon(world)).thenReturn(Optional.of(gameModeAddon));

        // Event basics
        when(event.getLocation()).thenReturn(location);
        when(event.getSpawnReason()).thenReturn(CreatureSpawnEvent.SpawnReason.NATURAL);

        // Location resolves to mocked world + the standing block
        when(location.getBlock()).thenReturn(standingBlock);
        when(standingBlock.getRelative(org.bukkit.block.BlockFace.DOWN)).thenReturn(blockBelow);
        when(blockBelow.getType()).thenReturn(Material.AIR);

        listener = new MobsSpawnListener(addon);
    }

    // ── Guard clauses ──────────────────────────────────────────────────────

    @Test
    void testNonNaturalSpawnIgnored() {
        when(event.getSpawnReason()).thenReturn(CreatureSpawnEvent.SpawnReason.SPAWNER);
        when(event.getEntityType()).thenReturn(EntityType.ZOMBIFIED_PIGLIN);
        when(iwm.isIslandNether(world)).thenReturn(true);
        when(settings.getWitherSkeletonChance()).thenReturn(1.0);

        listener.onEntitySpawn(event);

        verify(world, never()).spawnEntity(any(Location.class), any(EntityType.class));
        verify(event, never()).setCancelled(true);
    }

    @Test
    void testNoGameModeIgnored() {
        when(iwm.getAddon(world)).thenReturn(Optional.empty());
        when(event.getEntityType()).thenReturn(EntityType.ZOMBIFIED_PIGLIN);
        when(iwm.isIslandNether(world)).thenReturn(true);
        when(settings.getWitherSkeletonChance()).thenReturn(1.0);

        listener.onEntitySpawn(event);

        verify(world, never()).spawnEntity(any(Location.class), any(EntityType.class));
    }

    @Test
    void testDisabledGameModeIgnored() {
        when(settings.getDisabledGameModes()).thenReturn(Set.of("BSkyBlock"));
        when(event.getEntityType()).thenReturn(EntityType.ZOMBIFIED_PIGLIN);
        when(iwm.isIslandNether(world)).thenReturn(true);
        when(settings.getWitherSkeletonChance()).thenReturn(1.0);

        listener.onEntitySpawn(event);

        verify(world, never()).spawnEntity(any(Location.class), any(EntityType.class));
    }

    // ── Nether: piglin → wither skeleton / blaze ───────────────────────────

    @Test
    void testZombifiedPiglinOnNetherBrickReplacedWithWitherSkeleton() {
        when(event.getEntityType()).thenReturn(EntityType.ZOMBIFIED_PIGLIN);
        when(iwm.isIslandNether(world)).thenReturn(true);
        when(blockBelow.getType()).thenReturn(Material.NETHER_BRICKS);
        when(settings.getWitherSkeletonChance()).thenReturn(1.0);

        listener.onEntitySpawn(event);

        verify(world).spawnEntity(location, EntityType.WITHER_SKELETON);
        verify(event).setCancelled(true);
    }

    @Test
    void testPiglinOnNetherBrickReplacedWithWitherSkeleton() {
        when(event.getEntityType()).thenReturn(EntityType.PIGLIN);
        when(iwm.isIslandNether(world)).thenReturn(true);
        when(blockBelow.getType()).thenReturn(Material.NETHER_BRICKS);
        when(settings.getWitherSkeletonChance()).thenReturn(1.0);

        listener.onEntitySpawn(event);

        verify(world).spawnEntity(location, EntityType.WITHER_SKELETON);
        verify(event).setCancelled(true);
    }

    @Test
    void testPiglinOnNetherBrickSlabReplacedWithBlazeWhenWitherFails() {
        when(event.getEntityType()).thenReturn(EntityType.ZOMBIFIED_PIGLIN);
        when(iwm.isIslandNether(world)).thenReturn(true);
        when(blockBelow.getType()).thenReturn(Material.NETHER_BRICK_SLAB);
        when(settings.getWitherSkeletonChance()).thenReturn(0.0);
        when(settings.getBlazeChance()).thenReturn(1.0);

        listener.onEntitySpawn(event);

        verify(world).spawnEntity(location, EntityType.BLAZE);
        verify(event).setCancelled(true);
    }

    @Test
    void testPiglinOnNetherBrickStairsAcceptedForReplacement() {
        when(event.getEntityType()).thenReturn(EntityType.ZOMBIFIED_PIGLIN);
        when(iwm.isIslandNether(world)).thenReturn(true);
        when(blockBelow.getType()).thenReturn(Material.NETHER_BRICK_STAIRS);
        when(settings.getWitherSkeletonChance()).thenReturn(1.0);

        listener.onEntitySpawn(event);

        verify(world).spawnEntity(location, EntityType.WITHER_SKELETON);
    }

    @Test
    void testPiglinOnNonNetherBrickNotReplaced() {
        when(event.getEntityType()).thenReturn(EntityType.ZOMBIFIED_PIGLIN);
        when(iwm.isIslandNether(world)).thenReturn(true);
        when(blockBelow.getType()).thenReturn(Material.STONE);
        when(settings.getWitherSkeletonChance()).thenReturn(1.0);
        when(settings.getBlazeChance()).thenReturn(1.0);

        listener.onEntitySpawn(event);

        verify(world, never()).spawnEntity(any(Location.class), any(EntityType.class));
        verify(event, never()).setCancelled(true);
    }

    @Test
    void testPiglinNotInNetherSkipsBranch() {
        when(event.getEntityType()).thenReturn(EntityType.ZOMBIFIED_PIGLIN);
        when(iwm.isIslandNether(world)).thenReturn(false);
        when(blockBelow.getType()).thenReturn(Material.NETHER_BRICKS);
        when(settings.getWitherSkeletonChance()).thenReturn(1.0);

        listener.onEntitySpawn(event);

        verify(world, never()).spawnEntity(any(Location.class), any(EntityType.class));
    }

    @Test
    void testPiglinChanceZeroNoReplacement() {
        when(event.getEntityType()).thenReturn(EntityType.ZOMBIFIED_PIGLIN);
        when(iwm.isIslandNether(world)).thenReturn(true);
        when(blockBelow.getType()).thenReturn(Material.NETHER_BRICKS);
        // both chances are 0.0 from setUp

        listener.onEntitySpawn(event);

        verify(world, never()).spawnEntity(any(Location.class), any(EntityType.class));
        verify(event, never()).setCancelled(true);
    }

    // ── End: enderman → shulker ────────────────────────────────────────────

    @Test
    void testEndermanOnPurpurReplacedWithShulker() {
        when(event.getEntityType()).thenReturn(EntityType.ENDERMAN);
        when(iwm.isIslandEnd(world)).thenReturn(true);
        when(blockBelow.getType()).thenReturn(Material.PURPUR_BLOCK);
        when(settings.getShulkerChance()).thenReturn(1.0);

        listener.onEntitySpawn(event);

        verify(world).spawnEntity(location, EntityType.SHULKER);
        verify(event).setCancelled(true);
    }

    @Test
    void testEndermanOnPurpurSlabAccepted() {
        when(event.getEntityType()).thenReturn(EntityType.ENDERMAN);
        when(iwm.isIslandEnd(world)).thenReturn(true);
        when(blockBelow.getType()).thenReturn(Material.PURPUR_SLAB);
        when(settings.getShulkerChance()).thenReturn(1.0);

        listener.onEntitySpawn(event);

        verify(world).spawnEntity(location, EntityType.SHULKER);
    }

    @Test
    void testEndermanOnPurpurStairsAccepted() {
        when(event.getEntityType()).thenReturn(EntityType.ENDERMAN);
        when(iwm.isIslandEnd(world)).thenReturn(true);
        when(blockBelow.getType()).thenReturn(Material.PURPUR_STAIRS);
        when(settings.getShulkerChance()).thenReturn(1.0);

        listener.onEntitySpawn(event);

        verify(world).spawnEntity(location, EntityType.SHULKER);
    }

    @Test
    void testEndermanOnNonPurpurNotReplaced() {
        when(event.getEntityType()).thenReturn(EntityType.ENDERMAN);
        when(iwm.isIslandEnd(world)).thenReturn(true);
        when(blockBelow.getType()).thenReturn(Material.END_STONE);
        when(settings.getShulkerChance()).thenReturn(1.0);

        listener.onEntitySpawn(event);

        verify(world, never()).spawnEntity(any(Location.class), any(EntityType.class));
    }

    @Test
    void testEndermanNotInEndSkipsBranch() {
        when(event.getEntityType()).thenReturn(EntityType.ENDERMAN);
        when(iwm.isIslandEnd(world)).thenReturn(false);
        when(blockBelow.getType()).thenReturn(Material.PURPUR_BLOCK);
        when(settings.getShulkerChance()).thenReturn(1.0);

        listener.onEntitySpawn(event);

        verify(world, never()).spawnEntity(any(Location.class), any(EntityType.class));
    }

    @Test
    void testEndermanChanceZeroNoReplacement() {
        when(event.getEntityType()).thenReturn(EntityType.ENDERMAN);
        when(iwm.isIslandEnd(world)).thenReturn(true);
        when(blockBelow.getType()).thenReturn(Material.PURPUR_BLOCK);

        listener.onEntitySpawn(event);

        verify(world, never()).spawnEntity(any(Location.class), any(EntityType.class));
    }

    // ── Overworld: fish → guardian ─────────────────────────────────────────

    private Block prepareWaterColumnTopped(Material topMaterial) {
        Block water = mock(Block.class);
        when(water.getType()).thenReturn(Material.WATER);
        Block top = mock(Block.class);
        when(top.getType()).thenReturn(topMaterial);
        when(water.getRelative(org.bukkit.block.BlockFace.UP)).thenReturn(top);
        when(location.getBlock()).thenReturn(water);
        return top;
    }

    private void prepareFishEvent(Biome biome) {
        Fish fish = mock(Fish.class);
        when(event.getEntity()).thenReturn(fish);
        when(event.getEntityType()).thenReturn(EntityType.COD);
        when(world.getEnvironment()).thenReturn(World.Environment.NORMAL);
        when(world.getBiome(0, 0, 0)).thenReturn(biome);
    }

    @Test
    void testFishInDeepOceanOverPrismarineReplacedWithGuardian() {
        prepareFishEvent(Biome.DEEP_OCEAN);
        prepareWaterColumnTopped(Material.PRISMARINE);
        when(settings.getGuardianChance()).thenReturn(1.0);

        listener.onEntitySpawn(event);

        verify(world).spawnEntity(location, EntityType.GUARDIAN);
        verify(event).setCancelled(true);
    }

    @Test
    void testFishInDeepColdOceanOverDarkPrismarineReplacedWithGuardian() {
        prepareFishEvent(Biome.DEEP_COLD_OCEAN);
        prepareWaterColumnTopped(Material.DARK_PRISMARINE);
        when(settings.getGuardianChance()).thenReturn(1.0);

        listener.onEntitySpawn(event);

        verify(world).spawnEntity(location, EntityType.GUARDIAN);
    }

    @Test
    void testFishInDeepFrozenOceanOverPrismarineBricksReplacedWithGuardian() {
        prepareFishEvent(Biome.DEEP_FROZEN_OCEAN);
        prepareWaterColumnTopped(Material.PRISMARINE_BRICKS);
        when(settings.getGuardianChance()).thenReturn(1.0);

        listener.onEntitySpawn(event);

        verify(world).spawnEntity(location, EntityType.GUARDIAN);
    }

    @Test
    void testFishInDeepLukewarmOceanOverPrismarineSlabReplacedWithGuardian() {
        prepareFishEvent(Biome.DEEP_LUKEWARM_OCEAN);
        prepareWaterColumnTopped(Material.PRISMARINE_SLAB);
        when(settings.getGuardianChance()).thenReturn(1.0);

        listener.onEntitySpawn(event);

        verify(world).spawnEntity(location, EntityType.GUARDIAN);
    }

    @Test
    void testFishInShallowOceanNotReplaced() {
        prepareFishEvent(Biome.OCEAN);
        prepareWaterColumnTopped(Material.PRISMARINE);
        when(settings.getGuardianChance()).thenReturn(1.0);

        listener.onEntitySpawn(event);

        verify(world, never()).spawnEntity(any(Location.class), eq(EntityType.GUARDIAN));
    }

    @Test
    void testFishOverNonPrismarineNotReplaced() {
        prepareFishEvent(Biome.DEEP_OCEAN);
        prepareWaterColumnTopped(Material.SAND);
        when(settings.getGuardianChance()).thenReturn(1.0);

        listener.onEntitySpawn(event);

        verify(world, never()).spawnEntity(any(Location.class), any(EntityType.class));
    }

    @Test
    void testFishChanceZeroNoReplacement() {
        prepareFishEvent(Biome.DEEP_OCEAN);
        prepareWaterColumnTopped(Material.PRISMARINE);

        listener.onEntitySpawn(event);

        verify(world, never()).spawnEntity(any(Location.class), any(EntityType.class));
    }

    @Test
    void testNonFishInNormalWorldIgnored() {
        LivingEntity zombie = mock(LivingEntity.class);
        when(event.getEntity()).thenReturn(zombie);
        when(event.getEntityType()).thenReturn(EntityType.ZOMBIE);
        when(world.getEnvironment()).thenReturn(World.Environment.NORMAL);
        when(settings.getGuardianChance()).thenReturn(1.0);

        listener.onEntitySpawn(event);

        verify(world, never()).spawnEntity(any(Location.class), any(EntityType.class));
    }
}
