package dev.tr7zw.entityculling.versionless.util;

/**
 * Contains MathHelper methods
 */
public final class MathUtilities {

    private MathUtilities() {
    }

    public static int floor(double d) {
        int i = (int) d;
        return d < (double) i ? i - 1 : i;
    }

    public static int fastFloor(double d) {
        return (int) (d + 1024.0) - 1024;
    }

    public static int ceil(double d) {
        int i = (int) d;
        return d > (double) i ? i + 1 : i;
    }

    public static boolean rayIntersection(int[] b, Vec3d rayOrigin, Vec3d rayDir) {
        Vec3d rInv = new Vec3d(1, 1, 1).div(rayDir);

        double t1 = (b[0] - rayOrigin.x) * rInv.x;
        double t2 = (b[0] + 1 - rayOrigin.x) * rInv.x;
        double t3 = (b[1] - rayOrigin.y) * rInv.y;
        double t4 = (b[1] + 1 - rayOrigin.y) * rInv.y;
        double t5 = (b[2] - rayOrigin.z) * rInv.z;
        double t6 = (b[2] + 1 - rayOrigin.z) * rInv.z;

        double tmin = Math.max(Math.max(Math.min(t1, t2), Math.min(t3, t4)), Math.min(t5, t6));
        double tmax = Math.min(Math.min(Math.max(t1, t2), Math.max(t3, t4)), Math.max(t5, t6));

        // if tmax > 0, ray (line) is intersecting AABB, but the whole AABB is behind us
        if (tmax > 0) {
            return false;
        }

        // if tmin > tmax, ray doesn't intersect AABB
        if (tmin > tmax) {
            return false;
        }

        return true;
    }

}
