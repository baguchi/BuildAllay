package baguchi.build_allay.client;


import baguchi.build_allay.BuildAllayCore;
import baguchi.build_allay.client.render.BuildAllayRender;
import baguchi.build_allay.registry.ModEntities;
import baguchi.build_allay.registry.ModKeyMappings;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;

@OnlyIn(Dist.CLIENT)
@EventBusSubscriber(modid = BuildAllayCore.MODID, value = Dist.CLIENT, bus = EventBusSubscriber.Bus.MOD)
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
