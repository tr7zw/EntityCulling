package dev.tr7zw.entityculling.betterf3;

import net.mine_diver.unsafeevents.listener.EventListener;
import net.modificationstation.stationapi.api.mod.entrypoint.Entrypoint;
import net.modificationstation.stationapi.api.util.Namespace;
import ralf2oo2.betterf3.registry.ModuleRegisterEvent;
import ralf2oo2.betterf3.utils.ModulePosition;

public class ModuleListener {
    @Entrypoint.Namespace
    public static Namespace NAMESPACE;

    @EventListener
    public void registerModules(ModuleRegisterEvent event){
        event.register(NAMESPACE.id("culling_module"), EntityCullingModule::new, ModulePosition.LEFT, "culling");
    }
}
