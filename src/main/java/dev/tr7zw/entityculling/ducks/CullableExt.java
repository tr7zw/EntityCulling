package dev.tr7zw.entityculling.ducks;

public interface CullableExt {

    void entityCulling$setTimeout();
    boolean entityCulling$isForcedVisible();

    void entityCulling$setCulled(boolean value);
    boolean entityCulling$isCulled();

    void entityCulling$setOutOfCamera(boolean value);
    boolean entityCulling$isOutOfCamera();

}
