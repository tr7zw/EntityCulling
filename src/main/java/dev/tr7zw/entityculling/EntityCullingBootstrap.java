//#if FORGE
//$$package dev.tr7zw.entityculling;
//$$
//$$import net.minecraftforge.api.distmarker.Dist;
//$$import net.minecraftforge.fml.DistExecutor;
//$$import net.minecraftforge.fml.common.Mod;
//$$
//$$@Mod("entityculling")
//$$public class EntityCullingBootstrap {
//$$
//$$	public EntityCullingBootstrap() {
//$$		DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> { 
//$$         new EntityCullingMod();
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
//$$            if (FMLEnvironment.dist.isClient()) new EntityCullingMod();
//$$    }
//$$	
//$$}
//#endif
