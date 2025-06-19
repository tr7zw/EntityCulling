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
//$$	
//$$}
//#elseif NEOFORGE
//$$package dev.tr7zw.entityculling;
//$$
//$$import net.neoforged.fml.common.Mod;
//$$import net.neoforged.fml.loading.FMLEnvironment;
//$$
//$$@Mod("entityculling")
//$$public class EntityCullingBootstrap {
//$$
//$$    public EntityCullingBootstrap() {
//$$            if (FMLEnvironment.dist.isClient()) new EntityCullingMod().onInitialize();
//$$    }
//$$	
//$$}
//#endif
