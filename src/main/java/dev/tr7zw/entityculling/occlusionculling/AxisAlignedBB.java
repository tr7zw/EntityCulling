package dev.tr7zw.entityculling.occlusionculling;

import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

public class AxisAlignedBB {

	public double minx;
	public double miny;
	public double minz;
	public double maxx;
	public double maxy;
	public double maxz;

	public AxisAlignedBB(double minx, double miny, double minz, double maxx, double maxy, double maxz) {
		this.minx = minx;
		this.miny = miny;
		this.minz = minz;
		this.maxx = maxx;
		this.maxy = maxy;
		this.maxz = maxz;
	}

	public Vec3d getAABBMiddle(Vec3d blockLoc) {
		return new Vec3d(minx + (maxx - minx) / 2d, miny + (maxy - miny) / 2d, minz + (maxz - minz) / 2d).add(blockLoc);
	}

	public Vec3d getMinVec3d() {
		return new Vec3d(minx, miny, minz);
	}

	public Vec3d getMaxVec3d(World world) {
		return new Vec3d(maxx, maxy, maxz);
	}
	
	public double getWidth() {
		return maxx - minx;
	}
	
	public double getHeight() {
		return maxy - miny;
	}
	
	public double getDepth() {
		return maxz - minz;
	}

	public WrappedDirection rayIntersection(Vec3d ray, Vec3d rayOrigin, Vec3d pipeBlockLoc) {

		//optimization to decrease division operations
		Vec3d dirFrac = new Vec3d(1d / ray.getX(), 1d / ray.getY(), 1d / ray.getZ());

		double t1 = (minx + pipeBlockLoc.getX() - rayOrigin.getX()) * dirFrac.getX();
		double t2 = (maxx + pipeBlockLoc.getX() - rayOrigin.getX()) * dirFrac.getX();
		double t3 = (miny + pipeBlockLoc.getY() - rayOrigin.getY()) * dirFrac.getY();
		double t4 = (maxy + pipeBlockLoc.getY() - rayOrigin.getY()) * dirFrac.getY();
		double t5 = (minz + pipeBlockLoc.getZ() - rayOrigin.getZ()) * dirFrac.getZ();
		double t6 = (maxz + pipeBlockLoc.getZ() - rayOrigin.getZ()) * dirFrac.getZ();

		double tMin = Math.max(Math.max(Math.min(t1, t2), Math.min(t3, t4)), Math.min(t5, t6));
		double tMax = Math.min(Math.min(Math.max(t1, t2), Math.max(t3, t4)), Math.max(t5, t6));

		//AABB is behind player
		if (tMax < 0) {
			return null;
		}

		//don't intersect
		if (tMin > tMax) {
			return null;
		}

		Vec3d intersectionPoint = new Vec3d(rayOrigin.x * tMin, rayOrigin.y * tMin, rayOrigin.z * tMin);

		Vec3d aabbMiddle = getAABBMiddle(pipeBlockLoc);
		double tx = 0;
		double ty = 0;
		double tz = 0;

		for (WrappedDirection pd : WrappedDirection.values()) {
			tx = (aabbMiddle.getX() + pd.getX() * (maxx - minx) / 2d);
			ty = (aabbMiddle.getY() + pd.getY() * (maxy - miny) / 2d);
			tz = (aabbMiddle.getZ() + pd.getZ() * (maxz - minz) / 2d);
			double v = 1d;
			if (pd.getX() != 0) {
				v = Math.abs(intersectionPoint.getX() - tx);
			}
			if (pd.getY() != 0) {
				v = Math.abs(intersectionPoint.getY() - ty);
			}
			if (pd.getZ() != 0) {
				v = Math.abs(intersectionPoint.getZ() - tz);
			}
			if (v <= 0.001d) {
				return pd;
			}
		}

		return null;
	}

	@Override
	public String toString() {
		return minx + ":" + miny + ":" + minz + "_" + maxx + ":" + maxy + ":" + maxz;
	}

}
