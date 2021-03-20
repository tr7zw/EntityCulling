package dev.tr7zw.entityculling.occlusionculling;

import java.util.Arrays;

public class ArrayCache implements Cache {

	private final int reachX2;
	private final byte[] cache;
	private int keyPos;
	private int entry;
	private int offset;

	public ArrayCache(int reach) {
		this.reachX2 = reach*2;
		this.cache = new byte[(reachX2 * reachX2 * reachX2) / 4];
	}
	
	@Override
	public void resetCache() {
		Arrays.fill(cache, (byte) 0);
	}
	
	@Override
	public void setVisible(int x, int y, int z) {
		keyPos = x + y * reachX2 + z * reachX2 * reachX2;
		entry = keyPos / 4;
		offset = (keyPos % 4) * 2;
		cache[entry] |= 1 << offset;
	}
	
	@Override
	public void setHidden(int x, int y, int z) {
		keyPos = x + y * reachX2 + z * reachX2 * reachX2;
		entry = keyPos / 4;
		offset = (keyPos % 4) * 2;
		cache[entry] |= 1 << offset + 1;
	}
	
	@Override
	public int getState(int x, int y, int z) {
		keyPos = x + y * reachX2 + z * reachX2 * reachX2;
		entry = keyPos / 4;
		offset = (keyPos % 4) * 2;
		return cache[entry] >> offset & 3;
	}
	
	@Override
	public void setLastVisible() {
		cache[entry] |= 1 << offset;
	}
	
	@Override
	public void setLastHidden() {
		cache[entry] |= 1 << offset + 1;
	}
	
}
