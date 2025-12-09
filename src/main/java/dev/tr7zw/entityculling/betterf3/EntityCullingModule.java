package dev.tr7zw.entityculling.betterf3;

import dev.tr7zw.entityculling.Config;
import dev.tr7zw.entityculling.EntityCullingMod;
import net.minecraft.client.Minecraft;
import ralf2oo2.betterf3.modules.BaseModule;
import ralf2oo2.betterf3.utils.DebugLine;

public class EntityCullingModule extends BaseModule {

    public EntityCullingModule() {
        this.defaultNameColor = 0xFF5555;
        this.defaultValueColor = 0xFFFF55;

        this.nameColor = defaultNameColor;
        this.valueColor = defaultValueColor;

        lines.add(new DebugLine("cullinglastpass"));
        lines.add(new DebugLine("cullingrenderedblockentities"));
        lines.add(new DebugLine("cullingrenderedentities"));
    }

    @Override
    public void update(Minecraft minecraft) {
        if (  (0 != EntityCullingMod.instance.tickedEntities)
           || (0 != EntityCullingMod.instance.skippedEntityTicks)
        ) {
            EntityCullingMod.instance.tickedEntities = 0;
            EntityCullingMod.instance.skippedEntityTicks = 0;
        }

        lines.get(0).setValue( EntityCullingMod.instance.cullTask.lastTime + "ms" );

        if (!Config.FIELDS.disableBlockEntityCulling) {
            lines.get(1).setValue( EntityCullingMod.instance.renderedBlockEntities
                                 + " Skipped: "
                                 + EntityCullingMod.instance.skippedBlockEntities );
        } else {
            lines.get(1).setValue("Culling Disabled");
        }

        if (!Config.FIELDS.disableEntityCulling) {
            lines.get(2).setValue( EntityCullingMod.instance.renderedEntities
                                 + " Skipped: "
                                 + EntityCullingMod.instance.skippedEntities);
        } else {
            lines.get(2).setValue("Culling Disabled");
        }

        EntityCullingMod.instance.renderedBlockEntities = 0;
        EntityCullingMod.instance.skippedBlockEntities = 0;
        EntityCullingMod.instance.renderedEntities = 0;
        EntityCullingMod.instance.skippedEntities = 0;
    }
}
