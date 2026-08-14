![Entity Culling Banner](https://tr7zw.github.io/uikit/banner/header_entity_culling.png)

<p align="center" style="text-align: center;">
  <a href="https://discord.gg/caVV5eXekm"><img src="https://tr7zw.github.io/uikit/social_buttons_icon/Discord-Button-64.png" alt="Discord" style="margin: 5px 10px;"></a>
  <a href="https://github.com/tr7zw/EntityCulling"><img src="https://tr7zw.github.io/uikit/social_buttons_icon/Github-Button-64.png" alt="GitHub" style="margin: 5px 10px;"></a>
  <a href="https://modrinth.com/mod/entityculling"><img src="https://tr7zw.github.io/uikit/social_buttons_icon/Modrinth-Button-64.png" alt="GitHub" style="margin: 5px 10px;"></a>
  <a href="https://www.curseforge.com/minecraft/mc-mods/entityculling"><img src="https://tr7zw.github.io/uikit/social_buttons_icon/Curseforge-Button-64.png" alt="GitHub" style="margin: 5px 10px;"></a>
</p>

<br>![Divider](https://tr7zw.github.io/uikit/divider_faded/Divider_01.png)

<img src="https://tr7zw.github.io/uikit/headlines/large/About.png" alt="About" style="margin: 5px 10px;">

Minecraft skips rendering things that are behind you, so why is it rendering everything that you still can't see because of walls or ceilings in the way?

This mod introduces **asynchronous path-tracing** to efficiently determine what's actually visible to the player. Using other available CPU cores, it calculates line-of-sight visibility in real time and eliminates unnecessary draw calls/processing for hidden block entities and entities.

This can drastically improve the frame rate, depending on the number of entities and the position in the world.

<br>![Divider](https://tr7zw.github.io/uikit/divider_faded/Divider_01.png)

<br>![Features](https://tr7zw.github.io/uikit/headlines/large/Features.png)

### Asynchronous Path-Tracing

- Uses spare CPU threads to calculate visibility
- Runs alongside the main game thread without blocking (every few ticks, required data will be collected on the main thread)
- Updates visibility data in real time, keeping pop-ins to a minimum

### Entity Tick Optimization

- Reduces client impact from entities that are not visible
- Fully configurable and compatible with most mods
- No impact on server-side simulation, farms, or mob behavior

### Fully Configurable

- All features of the mod can be toggled on/off in the config screen
- Whitelist entities and block entities that should not be culled or tick-culled
- When playing with fast graphics or custom leaves, there is an option to treat leaves as solid blocks for Entity Culling. This can help to increase the performance in forested areas

<br>

[![Essential](https://tr7zw.github.io/uikit/banner/essential_1.png)](http://essential.gg)<br>

<br>![Divider](https://tr7zw.github.io/uikit/divider_faded/Divider_01.png)

<br>![Compatibility & Dependencies](https://tr7zw.github.io/uikit/headlines/medium/Compatibility%20&%20Dependancys.png)

|   Minecraft   |        Loader         |     Status      | Version  |                Note                 |
|---------------|-----------------------|-----------------|----------|-------------------------------------|
| 1.19.4+       | Fabric/Forge/NeoForge | ✅ Supported     | Latest   |                                     |
| 1.16.5-1.19.2 | Fabric/Forge          | ❌ Not supported | Outdated | Might get new updates at some point |
| 1.12.2/1.8.9  | Forge                 | ❌ Not supported | Outdated | Might get new updates at some point |
| 1.7.10        | Forge                 | ❌ Not supported | Outdated | No updates planned                  |
| b1.7.3        | Babric                | ❌ Not supported | Outdated | No updates planned                  |

<br>![Divider](https://tr7zw.github.io/uikit/divider_faded/Divider_01.png)

<br>![Screenshots / Media](https://tr7zw.github.io/uikit/headlines/medium/Screenshots%20Media.png)

![Stage View](https://tr7zw.github.io/uikit/screens/entityculling_compare.png)  
*A direct comparison with and without EntityCulling active. Testing was conducted in Scarland (Hermitcraft Season 9) with render distance set to 16 chunks. The mods Sodium, Iris and ImmediatelyFast were used alongside EntityCulling, running on Minecraft 1.21.5 with the Fabric mod loader.*

<br>![Divider](https://tr7zw.github.io/uikit/divider_faded/Divider_01.png)

<br>![Known Issues](https://tr7zw.github.io/uikit/headlines/medium/Known%20Issues.png)

Client-side entities, commonly used by magic mods for animations, may not behave as expected. Whitelist the relevant entities via the config screen for tick culling and/or entity culling. Consider reporting the ids of any entities that are not behaving correctly on the Github for inclusion in future updates.

You’ll also need to whitelist block entities that render well beyond their normal bounds. Examples include the vanilla beacon, pulleys from Create, and certain Botania blocks.

<br>![Divider](https://tr7zw.github.io/uikit/divider_faded/Divider_01.png)

<br>![FAQ](https://tr7zw.github.io/uikit/headlines/medium/FAQ.png)

### Does this need to be installed on the server?

No. This is a fully client-side mod and does **not** need to be installed on the server.

### Will this affect mob behavior or farms?

No. This mod only skips the rendering of entities and has no impact on their logic. Mobs will continue to spawn, move, and drop items as expected. Your farms and other gameplay mechanics will remain unaffected. The tick culling feature is fully limited to the client side world, so it will not affect the server-side simulation.

### I have "Use Entity Culling" enabled in Sodium - does this still help?

Yes! While Sodium performs basic visibility checks based on loaded chunks, this mod goes further. It analyzes the actual line-of-sight visibility, skipping entities that are within visible chunks but not actually visible to the player. It’s a much more aggressive and accurate approach.

### How can I test the performance impact of this mod?

In the controls menu, you can bind a debug key to toggle the mod on and off on the fly. This allows you to compare performance with and without the mod active, without needing to restart the game. This can also be used to check if visibility issues are caused by this mod or another mod.

Consider doing this, when playing on a really old computer, that is already struggling to run Minecraft. When the CPU is already maxed out, there is no spare CPU time to run the visibility calculations. Outside of this case, the mod should never cause a negative performance impact on the game, and at worst have no effect at all, when there is just nothing to cull.

### The F3 numbers do not seem right, what is going on?

The F3 debug screen numbers (in later versions hidden by default, can be turned on via F3+F6) are internally collected and may not necessarily reflect the actual render counts. Having active shaders for instance will cause higher numbers due to the shadow rendering.
Also keep in mind that the mod tries to error on the side of caution, so entities too close to walls or ceilings may be rendering through them, due to the possibility of clipping.

### Why is this mod running on the CPU, not the GPU?

The goal is to reduce the GPU load and have less unnecessary data transfers between the GPU and CPU. Doing the calculations would probably increase the GPU load, and main (render) thread load of the CPU. But feel free to prove me wrong and create a PR with a GPU-based implementation. Honestly, I lack the knowhow and time to implement this myself.

<br>![Divider](https://tr7zw.github.io/uikit/divider_faded/Divider_01.png)

<br>![Credits & license](https://tr7zw.github.io/uikit/headlines/medium/Credits%20&%20License.png)

👤 Thanks to RoboTricker for his Transport-Pipes plugin, which created the foundation for this mod. <br><br>
👤 Thanks to vicisacat for the Babric Beta 1.7.3 backport. <br><br>
👤 Thanks to Pelotrio for the Forge 1.7.10 backport. <br><br>
👤 Thanks to the awesome translators and contributors on GitHub!
<a href="https://github.com/tr7zw/EntityCulling/graphs/contributors">
<img src="https://tr7zw.github.io/uikit/links/underlined/more_details.png" style="vertical-align: middle;" alt="Link">
</a> <br><br>
📄 License: tr7zw Protective License <br>
Feel free to use this mod in your Modrinth and CurseForge-hosted modpacks or YouTube videos without asking for permission. Do not redistribute the JAR files anywhere else!
