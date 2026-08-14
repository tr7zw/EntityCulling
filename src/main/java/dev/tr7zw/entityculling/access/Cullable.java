package dev.tr7zw.entityculling.access;

import net.minecraft.world.phys.*;

public interface Cullable {

    void setTimeout();

    boolean isForcedVisible();

    void setCulled(boolean value);

    boolean isCulled();

    void setOutOfCamera(boolean value);

    boolean isOutOfCamera();

    boolean isShouldEntityAppearGlowing();

    void setShouldEntityAppearGlowing(boolean value);

    AABB getEc$BoundingBox();

    void setEc$BoundingBox(AABB box);

    Vec3 getEc$Position();

    void setEc$Position(Vec3 pos);

}
