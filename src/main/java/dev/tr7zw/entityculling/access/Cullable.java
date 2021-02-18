package dev.tr7zw.entityculling.access;

public interface Cullable {

	public void setTimeout();
	public boolean isForcedVisible();
	
	public void setCulled(boolean value);
	public boolean isCulled();
	
}
