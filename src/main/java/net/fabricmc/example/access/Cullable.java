package net.fabricmc.example.access;

public interface Cullable {

	public void setTimeout();
	public boolean isForcedVisible();
	
	public void setCulled(boolean value);
	public boolean isCulled();
	
}
