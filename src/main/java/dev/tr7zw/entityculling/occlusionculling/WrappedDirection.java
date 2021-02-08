package dev.tr7zw.entityculling.occlusionculling;

import net.minecraft.util.math.Vec3i;

public enum WrappedDirection {

	EAST(1, 0, 0),
	WEST(-1, 0, 0),
	SOUTH(0, 0, 1),
	NORTH(0, 0, -1),
	UP(0, 1, 0),
	DOWN(0, -1, 0);

	private Vec3i v;

	WrappedDirection(int x, int y, int z) {
		this.v = new Vec3i(x, y, z);
	}


	public Vec3i getVec3i() {
		return v;
	}

	public int getX() {
		return v.getX();
	}

	public int getY() {
		return v.getY();
	}

	public int getZ() {
		return v.getZ();
	}

	//TODO
	public WrappedDirection getOpposite() {
		Vec3i v2 = new Vec3i(v.getX()*-1f, v.getY()*-1f, v.getZ()*-1f);
		for (WrappedDirection dir : WrappedDirection.values()) {
			if (v2.equals(dir.v)) {
				return dir;
			}
		}
		return null;
	}

	public int getId() {
		return this.ordinal();
	}

	public boolean isSide() {
		return this == NORTH || this == EAST || this == SOUTH || this == WEST;
	}

	public static WrappedDirection fromID(int id) {
		for (WrappedDirection pd : WrappedDirection.values()) {
			if (pd.getId() == id) {
				return pd;
			}
		}
		return null;
	}

	public WrappedDirection getNextDirection() {
		if (getId() == WrappedDirection.values().length - 1) {
			return fromID(0);
		}
		return fromID(getId() + 1);
	}

}
