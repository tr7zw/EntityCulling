package dev.tr7zw.entityculling.versionless.occlusionculling.provider;

public interface BrickProvider extends DataProvider {

    long[] getVoxelBitmask();
    boolean isBrickEmpty(int lx, int ly, int lz);
}
