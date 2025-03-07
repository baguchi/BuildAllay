package baguchi.build_allay;


import baguchi.build_allay.packet.SummonBuildAllayPacket;
import baguchi.build_allay.registry.ModEntities;
import baguchi.build_allay.registry.ModMemorys;
import net.minecraft.client.Minecraft;
import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.server.ServerStartingEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.util.thread.EffectiveSide;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.simple.SimpleChannel;
import net.minecraftforge.server.ServerLifecycleHooks;

import java.util.Locale;

// The value here should match an entry in the META-INF/mods.toml file
@Mod(BuildAllayCore.MODID)
public class BuildAllayCore
{
    // Define mod id in a common place for everything to reference
    public static final String MODID = "build_allay";
    public static final String NETWORK_PROTOCOL = "2";
    public static final SimpleChannel CHANNEL = NetworkRegistry.ChannelBuilder.named(new ResourceLocation(MODID, "net"))
            .networkProtocolVersion(() -> NETWORK_PROTOCOL)
            .clientAcceptedVersions(NETWORK_PROTOCOL::equals)
            .serverAcceptedVersions(NETWORK_PROTOCOL::equals)
            .simpleChannel();

    // The constructor for the mod class is the first code that is run when your mod is loaded.
    // FML will recognize some parameter types like IEventBus or ModContainer and pass them in automatically.
    public BuildAllayCore()
    {
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();

        MinecraftForge.EVENT_BUS.register(this);
        ModEntities.ENTITIES_REGISTRY.register(modEventBus);
        ModMemorys.MEMORY_REGISTRY.register(modEventBus);

        modEventBus.addListener(this::commonSetup);
        this.setupMessages();
    }

    private void commonSetup(final FMLCommonSetupEvent event)
    {
    }

    public static RegistryAccess registryAccess() {
        if (EffectiveSide.get().isServer()) {
            return ServerLifecycleHooks.getCurrentServer().registryAccess();
        }
        return Minecraft.getInstance().getConnection().registryAccess();
    }


    private void setupMessages() {
        CHANNEL.messageBuilder(SummonBuildAllayPacket.class, 0)
                .encoder(SummonBuildAllayPacket::serialize).decoder(SummonBuildAllayPacket::deserialize)
                .consumerMainThread(SummonBuildAllayPacket::handle)
                .add();
    }
    // You can use SubscribeEvent and let the Event Bus discover methods to call
    @SubscribeEvent
    public void onServerStarting(ServerStartingEvent event)
    {

    }

    public static ResourceLocation prefix(String name) {
        return new ResourceLocation(BuildAllayCore.MODID, name.toLowerCase(Locale.ROOT));
    }
}
