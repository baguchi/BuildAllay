package baguchi.build_allay;


import baguchi.build_allay.packet.SummonBuildAllayPacket;
import baguchi.build_allay.registry.ModEntities;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.server.ServerStartingEvent;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

import java.util.Locale;

// The value here should match an entry in the META-INF/neoforge.mods.toml file
@Mod(BuildAllayCore.MODID)
public class BuildAllayCore
{
    // Define mod id in a common place for everything to reference
    public static final String MODID = "build_allay";
    public static final String NETWORK_PROTOCOL = "2";


    // The constructor for the mod class is the first code that is run when your mod is loaded.
    // FML will recognize some parameter types like IEventBus or ModContainer and pass them in automatically.
    public BuildAllayCore(IEventBus modEventBus, Dist dist, ModContainer modContainer)
    {

        NeoForge.EVENT_BUS.register(this);
        ModEntities.ENTITIES_REGISTRY.register(modEventBus);

        modEventBus.addListener(this::commonSetup);
        modEventBus.addListener(this::setupPackets);
    }

    private void commonSetup(final FMLCommonSetupEvent event)
    {
    }

    public void setupPackets(RegisterPayloadHandlersEvent event) {
        PayloadRegistrar registrar = event.registrar(MODID).versioned("1.0.0").optional();
        registrar.playBidirectional(SummonBuildAllayPacket.TYPE, SummonBuildAllayPacket.STREAM_CODEC, (handler, payload) -> handler.handle(handler, payload));
    }

    // You can use SubscribeEvent and let the Event Bus discover methods to call
    @SubscribeEvent
    public void onServerStarting(ServerStartingEvent event)
    {

    }

    public static ResourceLocation prefix(String name) {
        return ResourceLocation.fromNamespaceAndPath(BuildAllayCore.MODID, name.toLowerCase(Locale.ROOT));
    }
}
