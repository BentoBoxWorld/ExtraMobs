# ExtraMobs Addon
[![Discord](https://img.shields.io/discord/272499714048524288.svg?logo=discord)](https://discord.bentobox.world)
[![Build Status](https://ci.codemc.org/buildStatus/icon?job=BentoBoxWorld/ExtraMobs)](https://ci.codemc.org/job/BentoBoxWorld/job/ExtraMobs/)

Add-on for BentoBox that adjusts some mob spawning rules to get Blazes, Wither Skeleton, and Shulkers.

## Where to find

Currently ExtraMobs Addon is in **Alpha stage**, so it may or may not contain bugs... a lot of bugs. Also it means, that some features are not working or implemented. 
You can download it from [Release tab](https://github.com/BentoBoxWorld/ExtraMobs/releases)

Or you can try **nightly builds** where you can check and test new features that will be implemented in next release from [Jenkins Server](https://ci.codemc.org/job/BentoBoxWorld/job/ExtraMobs/lastStableBuild/).

If you like this addon but something is missing or is not working as you want, you can always submit an [Issue request](https://github.com/BentoBoxWorld/ExtraMobs/issues) or get a support in Discord [BentoBox ![icon](https://avatars2.githubusercontent.com/u/41555324?s=15&v=4)](https://discord.bentobox.world)

## How to use

1. Place the addon jar in the addons folder of the BentoBox plugin
2. Restart the server
3. In game you can change flags that allows to use current addon.

## Information

This addon does not change Minecraft spawning rules. Instead it uses other mobs that are naturally generated and change their type with new entity, if all conditions are met.

##### For Wither Skeleton and Blaze:

Addon will replace Zombie Pigmen with Blaze or Wither Skeleton by chance from config, if:
 - given world is generated by GameMode Addon.
 - given world is Nether
 - Zombie Pigmen is standing on nether brick, nether brick slab or nether brick stairs.

##### For Shulkers:

Addon will replace Enderman with Shulker by chance from config if:
 - given world is generated by GameMode Addon.
 - given world is the End
 - Enderman is standing on purpur block, purpur stair or purpur slab. 

##### For Guardians:

Addon will replace Cod, Salmon or Tropical fish with Guardian by chance from config if:
 - given world is generated by GameMode Addon.
 - given world is the Overworld
 - biome in given location is deep ocean or any its variants
 - first block above water where fish is spawned is prismarine, prismarine brick or dark prismarine (blocks, slabs and stairs).     


## Compatibility

- [x] BentoBox - 1.11.0 version

Addon is build on Minecraft 1.15.2 and BentoBox 1.11.0 version, however, it should even work on Minecraft 1.13.2 and BentoBox 1.0 Release.

Addon supports all Game mode addons.


## Information

More information can be found in [Wiki Pages](https://github.com/BentoBoxWorld/ExtraMobs/wiki).
