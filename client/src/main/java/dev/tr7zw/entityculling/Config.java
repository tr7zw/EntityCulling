package dev.tr7zw.entityculling;

import io.github.axolotlclient.AxolotlClientConfig.annotation.annotations.SerializedName;

@io.github.axolotlclient.AxolotlClientConfig.annotation.annotations.Config(name = "entityculling")
public class Config {

	@SerializedName("disable_entity_culling")
	public boolean disableEntityCulling = false;


	@SerializedName("disable_block_entity_culling")
	public boolean disableBlockEntityCulling = false;


	@SerializedName("show_f3_info")
	public boolean showF3Info = true;


	@SerializedName("glass_culls")
	public boolean glassCulls = false;

}
