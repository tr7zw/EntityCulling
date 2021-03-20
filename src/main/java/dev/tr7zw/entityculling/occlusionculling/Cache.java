package dev.tr7zw.entityculling.occlusionculling;

public interface Cache {

	void resetCache();

	void setVisible(int x, int y, int z);

	void setHidden(int x, int y, int z);

	int getState(int x, int y, int z);

	void setLastHidden();

	void setLastVisible();

}