# EntityCulling

__Using async path-tracing to skip rendering Tiles/Entities that are not visible.__

Minecraft skips rendering things that are behind you, so why is it rendering everything that you still can't see because of a wall in the way? This mod utilizes your other CPU cores/threads to do really quick path-tracing from your camera to all tile/-entities to determine rather they are visible or not. During the rendering, the not visible ones will be skipped the same way entities behind you are.

This mod calculates the visibility of tile/-entities 64 blocks in each direction of the player(so a 128x128x128 cube in total), everything outside of that is considered too far away and is invisible(should somewhat line up with the vanilla "Entity Distance" setting, but future changes to this size are possible).

## Compatibility

This has been tested with other mods, Optifine(Optifabric), Canvas, and Sodium, in all cases resulting in massive fps gains in places like Game Server lobbies.

You might wonder why it does increase the FPS with Sodium(and Canvas) since Sodium has "Use Entity Culling" in its Advanced settings and enabled by default. The difference is that Sodium does a really quick pass based on the visible chunks, being way less aggressive and thereby still rendering entities that just happen to be in visible chunks, but not visible themselves.

## License

This project is licensed under [``tr7zw Protective License``](LICENSE-EntityCulling).
This license does not allow others to distribute the software/derivative works(in source or binary form).
You have to contact the author to get permission for redistribution. (For example: Modpacks(that are not hosted on CurseForge), "Clients", mod hosting sites).
Keep in mind that [Githubs TOS](https://docs.github.com/en/github/site-policy/github-terms-of-service#d-user-generated-content) and [Overwolfs TOS](https://www.overwolf.com/legal/terms/) apply at their respective places. This (among other things) means you don't need to ask to include the mod in a CurseForge Modpack and that by contributing code it explicitly gets the same license as the repository.
