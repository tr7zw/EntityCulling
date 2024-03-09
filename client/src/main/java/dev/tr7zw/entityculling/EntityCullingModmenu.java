package dev.tr7zw.entityculling;

import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;
import io.github.axolotlclient.AxolotlClientConfig.api.manager.ConfigManager;
import io.github.axolotlclient.AxolotlClientConfig.api.ui.ConfigUI;
import io.github.axolotlclient.AxolotlClientConfig.api.AxolotlClientConfig;

public class EntityCullingModmenu implements ModMenuApi {

	@Override
	public ConfigScreenFactory<?> getModConfigScreenFactory() {
		return screen -> {
			ConfigManager manager = AxolotlClientConfig.getInstance().getConfigManager("entityculling");
		 	return ConfigUI.getInstance().getScreen(this.getClass().getClassLoader(),
						manager.getRoot(), screen);
		};
	}
}
