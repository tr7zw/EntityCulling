# EntityCulling

[![Discord](https://tr7zw.dev/curse/Discord-long.png)](https://discord.gg/fWtMAkFaZF)[![ko-fi](https://ko-fi.com/img/githubbutton_sm.svg)](https://ko-fi.com/O5O7ACGRH)[Website](https://tr7zw.github.io/project/entityculling/)

Using async path-tracing to skip rendering Block/Entities that are not visible.

Minecraft skips rendering things that are behind you, so why is it rendering everything that you still can't see because of a wall in the way? This mod utilizes your other CPU cores/threads to do really quick path-tracing from your camera to all block/-entities to determine rather they are visible or not. During the rendering, the not visible ones will be skipped the same way entities behind you are.

## Dependencies

- none

## Incompatible

- none (With some mods you might need to add the blocks to the config whitelist to fix visual issues)

## Tested with

- Sodium
- Iris
- Optifine

## Download

Make sure you pick the correct File depending on Forge/Fabric!

- 1.18.x [⬇️](https://www.curseforge.com/minecraft/mc-mods/entityculling/files/all?filter-status=1&filter-game-version=1738749986%3A73250) 
- 1.17.x [⬇️](https://www.curseforge.com/minecraft/mc-mods/entityculling/files/all?filter-status=1&filter-game-version=1738749986%3A73242) 
- 1.16.x [⬇️](https://www.curseforge.com/minecraft/mc-mods/entityculling/files/all?filter-status=1&filter-game-version=1738749986%3A70886) 
- 1.8.9 [⬇️](https://www.curseforge.com/minecraft/mc-mods/entityculling/files/all?filter-status=1&filter-game-version=1738749986%3A4)
 
## Note to why it works even with Sodium/Optifine

This has been tested with other mods, Optifine(Optifabric), Iris, and Sodium, in all cases resulting in fps gains in places like Game Server lobbies.

You might wonder why it does increase the FPS with Sodium since Sodium has "Use Entity Culling" in its Advanced settings and enabled by default. The difference is that Sodium does a really quick pass based on the visible chunks, being way less aggressive and thereby still rendering entities that just happen to be in visible chunks, but not visible themselves.

## FAQ

### Does this have to be installed on the Server?

No, this is fully Client-side and can't be installed on servers.

### Will this influence farms/mobs?

No. Since this mod just skips the rendering, mobs will still spawn/move/drop items.

## Credits

RoboTricker created the original server-side async raytracing occlusion culling implementation for Transport-Pipes.

Support via [![Discord](https://tr7zw.dev/curse/Discord.png)](https://discord.gg/fWtMAkFaZF) or [Github](https://github.com/tr7zw/EntityCulling-Fabric)! The comments are not there to report bugs/crashes/get help.