package baguchi.build_allay.client;


import baguchi.build_allay.BuildAllayCore;
import baguchi.build_allay.client.render.BuildAllayRender;
import baguchi.build_allay.registry.ModEntities;
import baguchi.build_allay.registry.ModKeyMappings;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@OnlyIn(Dist.CLIENT)
@Mod.EventBusSubscriber(modid = BuildAllayCore.MODID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.MOD)
public class ClientRegistrar {


    @SubscribeEvent
    public static void registerEntityRenders(EntityRenderersEvent.RegisterRenderers event) {
        event.registerEntityRenderer(ModEntities.BUILDER_ALLAY.get(), BuildAllayRender::new);
    }

    @SubscribeEvent
    public static void registerKeyMapping(RegisterKeyMappingsEvent event) {
        event.register(ModKeyMappings.KEY_SUMMON_BUILDER_ALLAY);
    }
}
