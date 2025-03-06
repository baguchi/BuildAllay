package baguchi.build_allay;


import baguchi.build_allay.packet.SummonBuildAllayPacket;
import baguchi.champaign.attachment.ChampaignAttachment;
import baguchi.champaign.attachment.OwnerAttachment;
import baguchi.champaign.music.MusicSummon;
import baguchi.champaign.packet.*;
import baguchi.build_allay.registry.ModEntities;
import baguchi.champaign.registry.ModItems;
import baguchi.build_allay.registry.ModMemorys;
import baguchi.champaign.registry.ModMusicSummons;
import net.minecraft.client.Minecraft;
import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.capabilities.CapabilityToken;
import net.minecraftforge.event.server.ServerStartingEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.util.thread.EffectiveSide;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.simple.SimpleChannel;
import net.minecraftforge.registries.DataPackRegistryEvent;
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

    public static final Capability<ChampaignAttachment> CHAMPAIGN_CAPABILITY = CapabilityManager.get(new CapabilityToken<>() {
    });

    public static final Capability<OwnerAttachment> OWNER_CAPABILITY = CapabilityManager.get(new CapabilityToken<>() {
    });
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
