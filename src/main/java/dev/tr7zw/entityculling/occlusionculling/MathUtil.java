package dev.tr7zw.entityculling.occlusionculling;

/**
 * Contains MathHelper methods
 *
 */
public class MathUtil {

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
	
}
