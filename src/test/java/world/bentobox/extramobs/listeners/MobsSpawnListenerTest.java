package world.bentobox.extramobs.listeners;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Biome;
import org.bukkit.block.Block;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Fish;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.eclipse.jdt.annotation.NonNull;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.powermock.modules.junit4.PowerMockRunner;

import world.bentobox.bentobox.BentoBox;
import world.bentobox.bentobox.api.addons.AddonDescription;
import world.bentobox.bentobox.api.addons.GameModeAddon;
import world.bentobox.bentobox.managers.IslandWorldManager;
import world.bentobox.extramobs.ExtraMobsAddon;
import world.bentobox.extramobs.config.Settings;

@RunWith(PowerMockRunner.class)
public class MobsSpawnListenerTest {

    @Mock
    private ExtraMobsAddon addon;

    @Mock
    private CreatureSpawnEvent event;

    @Mock
    private World world;

    private MobsSpawnListener listener;

    private Settings settings;

    @Mock
    private BentoBox plugin;

    @Mock
    private IslandWorldManager iwm;

    @Mock
    private GameModeAddon gma;

    @Mock
    private Location location;

    @Mock
    private Block block;

    @Before
    public void setUp() {
        settings = new Settings();
        when(addon.getSettings()).thenReturn(settings);

        when(addon.getPlugin()).thenReturn(plugin);

        when(plugin.getIWM()).thenReturn(iwm);

        when(iwm.getAddon(world)).thenReturn(Optional.of(gma));

        when(iwm.isIslandEnd(world)).thenReturn(true);
        when(iwm.isIslandNether(world)).thenReturn(true);

        @NonNull
        AddonDescription desc = new AddonDescription.Builder("main", "bskyblock", "1.0.0").build();

        when(gma.getDescription()).thenReturn(desc);

        // Location
        when(location.getBlock()).thenReturn(block);
        when(location.getWorld()).thenReturn(world);

        when(block.getRelative(any())).thenReturn(block);

        when(block.getType()).thenReturn(Material.STONE);

        // Initialize mocks and the class to test
        listener = new MobsSpawnListener(addon);
    }

    // Test case for natural spawning of Zombified Piglin in the Nether
    @Test
    public void testNaturalSpawnZombifiedPiglinNether() {
        when(event.getEntityType()).thenReturn(EntityType.ZOMBIFIED_PIGLIN);
        when(event.getSpawnReason()).thenReturn(CreatureSpawnEvent.SpawnReason.NATURAL);
        when(event.getLocation()).thenReturn(location);
        when(world.getEnvironment()).thenReturn(World.Environment.NETHER);
        when(addon.getPlugin().getIWM().isIslandNether(world)).thenReturn(true);
        settings.setWitherSkeletonChance(1.1); // Set so that it will always spawn
        when(block.getType()).thenReturn(Material.NETHER_BRICKS);

        listener.onEntitySpawn(event);

        verify(event).setCancelled(true);
        // Additional verifications can be added to check if the correct entity was spawned
    }

    // Test case for natural spawning of Enderman in the End
    @Test
    public void testNaturalSpawnEndermanEnd() {
        when(event.getEntityType()).thenReturn(EntityType.ENDERMAN);
        when(event.getSpawnReason()).thenReturn(CreatureSpawnEvent.SpawnReason.NATURAL);
        when(event.getLocation()).thenReturn(location);
        when(world.getEnvironment()).thenReturn(World.Environment.THE_END);
        when(addon.getPlugin().getIWM().isIslandEnd(world)).thenReturn(true);
        settings.setShulkerChance(1.1); // Set so that it will always spawn
        when(block.getType()).thenReturn(Material.PURPUR_BLOCK);

        listener.onEntitySpawn(event);

        verify(event).setCancelled(true);
        // Additional verifications can be added to check if the correct entity was spawned
    }

    // Test case for spawning of Fish in Deep Ocean biome
    @Test
    public void testFishSpawnDeepOcean() {
        Fish fish = mock(Fish.class);
        when(event.getEntity()).thenReturn(fish);
        when(event.getEntityType()).thenReturn(EntityType.TROPICAL_FISH);
        when(event.getSpawnReason()).thenReturn(CreatureSpawnEvent.SpawnReason.NATURAL);
        when(event.getLocation()).thenReturn(location);
        when(world.getEnvironment()).thenReturn(World.Environment.NORMAL);
        when(world.getBiome(anyInt(), anyInt(), anyInt())).thenReturn(Biome.DEEP_OCEAN);
        settings.setGuardianChance(1.1); // Set so that it will always spawn
        when(block.getType()).thenReturn(Material.WATER, Material.WATER, Material.WATER, Material.PRISMARINE);

        listener.onEntitySpawn(event);

        verify(event).setCancelled(true);
        // Additional verifications for guardian spawning
    }

    // Test case for non-natural spawning
    @Test
    public void testNonNaturalSpawn() {
        when(event.getSpawnReason()).thenReturn(CreatureSpawnEvent.SpawnReason.SPAWNER);

        listener.onEntitySpawn(event);

        verify(event, never()).isCancelled();
    }

}
