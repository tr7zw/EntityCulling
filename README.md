![Entity Culling Banner](https://tr7zw.github.io/uikit/banner/header_entity_culling.png)

<p align="center" style="text-align: center;">
  <a href="https://discord.gg/caVV5eXekm"><img src="https://tr7zw.github.io/uikit/social_buttons_icon/Discord-Button-64.png" alt="Discord" style="margin: 5px 10px;"></a>
  <a href="https://github.com/tr7zw/EntityCulling"><img src="https://tr7zw.github.io/uikit/social_buttons_icon/Github-Button-64.png" alt="GitHub" style="margin: 5px 10px;"></a>
  <a href="https://modrinth.com/mod/entityculling"><img src="https://tr7zw.github.io/uikit/social_buttons_icon/Modrinth-Button-64.png" alt="GitHub" style="margin: 5px 10px;"></a>
  <a href="https://www.curseforge.com/minecraft/mc-mods/entityculling"><img src="https://tr7zw.github.io/uikit/social_buttons_icon/Curseforge-Button-64.png" alt="GitHub" style="margin: 5px 10px;"></a>
  <a href="https://ko-fi.com/tr7zw"><img src="https://tr7zw.github.io/uikit/social_buttons_icon/Kofi-Button-64.png" alt="Ko-fi" style="margin: 5px 10px;"></a>
</p>

<br>![Divider](https://tr7zw.github.io/uikit/divider_faded/Divider_01.png)

<img src="https://tr7zw.github.io/uikit/headlines/large/About.png" alt="About" style="margin: 5px 10px;">

Modern Minecraft rendering is fast—but not always smart. Why render block entities and mobs that are hidden behind walls or ceilings and are completely out of sight, when you could just skip them entirely?

This mod introduces **asynchronous path-tracing** to efficiently determine what's actually visible to the player. By leveraging multiple CPU cores, it calculates line-of-sight visibility in real time and eliminates unnecessary draw calls for hidden block entities and entities.

The result?  
Smarter rendering. Less overhead. More performance.

<br>![Divider](https://tr7zw.github.io/uikit/divider_faded/Divider_01.png)

<br>![Features](https://tr7zw.github.io/uikit/headlines/large/Features.png)

Unlock untapped performance by only rendering entities that truly matter. This mod goes beyond conventional optimization to bring next-level visibility optimization to deliver advanced visibility culling for Minecraft, giving you smoother gameplay.

### Multithreaded Path-Tracing

- Uses spare CPU threads to rapidly calculate visibility
- Runs alongside the main game thread without blocking
- Updates visibility data in real time

### Smart Occlusion Culling

- Skips the rendering of block entities and mobs hidden behind terrain or structures
- Works like Minecraft's back-face culling, but smarter
- Reduces GPU load without sacrificing visual fidelity

### Entity Tick Optimization

- Reduces client impact from entities that are not visible
- Only updated the essentials
- Fully configurable and compatible with most mods

<br>

[![Essential](https://tr7zw.github.io/uikit/banner/essential_1.png)](http://essential.gg)<br><br>
![Need a 24/7 Server? Check this out!](https://tr7zw.github.io/uikit/banner/shockbyte_divider.png)
[![Shockbyte](https://tr7zw.github.io/uikit/banner/shockbyte_small.png)](http://bit.ly/4bczSJY)

<br>![Divider](https://tr7zw.github.io/uikit/divider_faded/Divider_01.png)

<br>![Compatibility & Dependencies](https://tr7zw.github.io/uikit/headlines/medium/Compatibility%20&%20Dependancys.png)

|   Minecraft   |        Loader         |      Status       | Version  |                 Note                  |
|---------------|-----------------------|-------------------|----------|---------------------------------------|
| 1.19.4+       | Fabric/Forge/NeoForge | ✅ Supported       | Latest   |                                       |
| 1.16.5-1.19.2 | Fabric/Forge          | ❌ Not supported | Outdated | Might get new updates at some point |
| 1.12.2/1.8.9  | Forge                 | ❌ Not supported   | Outdated | Might get new updates at some point   |
| 1.7.10        | Forge                 | ❌ Not supported   | Outdated | No updates planned                    |
| b1.7.3        | Babric                | ❌ Not supported   | Outdated | No updates planned                    |

<br>![Divider](https://tr7zw.github.io/uikit/divider_faded/Divider_01.png)

<br>![Screenshots / Media](https://tr7zw.github.io/uikit/headlines/medium/Screenshots%20Media.png)

![Stage View](https://tr7zw.github.io/uikit/screens/entityculling_compare.png)  
*A direct comparison with and without EntityCulling active. Testing was conducted in Scarland (Hermitcraft Season 9) with render distance set to 16 chunks. The mods Sodium, Iris and ImmediatelyFast were used alongside EntityCulling, running on Minecraft 1.21.5 with the Fabric mod loader.*

<br>![Divider](https://tr7zw.github.io/uikit/divider_faded/Divider_01.png)

<br>![Known Issues](https://tr7zw.github.io/uikit/headlines/medium/Known%20Issues.png)

Client-side entities, commonly used by magic mods for animations, may not behave as expected. Whitelist the relevant entities via the config screen for tick culling and/or entity culling.

You’ll also need to whitelist block entities that render well beyond their normal bounds. Examples include the vanilla beacon, pulleys from Create, and certain Botania blocks.

<br>![Divider](https://tr7zw.github.io/uikit/divider_faded/Divider_01.png)

<br>![FAQ](https://tr7zw.github.io/uikit/headlines/medium/FAQ.png)

### Does this need to be installed on the server?

No. This is a fully client-side mod and does **not** need to be installed on the server.

### Will this affect mob behavior or farms?

No. The mod only skips rendering—not simulation. Mobs will continue to spawn, move, and drop items as expected. Your farms and other gameplay mechanics will remain unaffected.

### I have "Use Entity Culling" enabled in Sodium - does this still help?

Yes! While Sodium performs basic visibility checks based on loaded chunks, this mod goes further. It analyzes the actual line-of-sight visibility, skipping entities that are within visible chunks but not actually visible to the player. It’s a much more aggressive and accurate approach.

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
