//#if FORGE
//$$package dev.tr7zw.entityculling;
//$$
//$$import net.minecraftforge.api.distmarker.Dist;
//$$import net.minecraftforge.fml.DistExecutor;
//$$import net.minecraftforge.fml.common.Mod;
//$$import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
//$$import dev.tr7zw.transition.loader.ModLoaderUtil;
//$$
//$$@Mod("entityculling")
//$$public class EntityCullingBootstrap {
//$$
//$$	public EntityCullingBootstrap(FMLJavaModLoadingContext context) {
//$$        ModLoaderUtil.setModLoadingContext(context);
//$$		DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> { 
//$$         new EntityCullingMod().onInitialize();
//$$        });
//$$	}
//$$    public EntityCullingBootstrap() {
//$$        this(FMLJavaModLoadingContext.get());
//$$    }
//$$	
//$$}
//#elseif NEOFORGE
//$$package dev.tr7zw.entityculling;
//$$
//$$import net.neoforged.fml.common.Mod;
//$$import net.neoforged.fml.loading.FMLEnvironment;
//$$import dev.tr7zw.transition.loader.ModLoaderEventUtil;
//$$
//$$@Mod("entityculling")
//$$public class EntityCullingBootstrap {
//$$
//$$    public EntityCullingBootstrap() {
//#if MC < 12109
//$$        if(FMLEnvironment.dist == Dist.CLIENT) {
//#else
//$$        if(FMLEnvironment.getDist() == Dist.CLIENT) {
//#endif
//$$                    ModLoaderEventUtil.registerClientSetupListener(() -> new EntityCullingMod().onInitialize());
//$$            }
//$$    }
//$$	
//$$}
//#endif
