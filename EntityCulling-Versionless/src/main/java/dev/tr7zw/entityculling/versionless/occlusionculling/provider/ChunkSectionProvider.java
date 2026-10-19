package dev.tr7zw.entityculling.versionless.occlusionculling.provider;

public interface ChunkSectionProvider {

    void ec$setProviderData(Object data);

    <T> T ec$getProviderData(Class<T> clazz);

    boolean ec$isDirty();

    void ec$setDirty(boolean dirty);

}
