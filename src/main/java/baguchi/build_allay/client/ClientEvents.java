package baguchi.build_allay.client;

import baguchi.build_allay.BuildAllayCore;
import baguchi.build_allay.packet.SummonBuildAllayPacket;
import baguchi.build_allay.registry.ModKeyMappings;
import com.simibubi.create.AllItems;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.ItemTooltipEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import net.neoforged.neoforge.network.PacketDistributor;

@OnlyIn(Dist.CLIENT)
@EventBusSubscriber(modid = BuildAllayCore.MODID, value = Dist.CLIENT)
public class ClientEvents {
    public static int pressSummonTick;
    @SubscribeEvent
    public static void onPlayerPostTick(PlayerTickEvent.Post event) {
            if (Minecraft.getInstance().player != null && Minecraft.getInstance().player.getMainHandItem().is(AllItems.SCHEMATIC.get())) {

                if (ModKeyMappings.KEY_SUMMON_BUILDER_ALLAY.isDown()) {
                    if (pressSummonTick <= 0) {
                        pressSummonTick = 20;
                        PacketDistributor.sendToServer(new SummonBuildAllayPacket());
                    }
                }
            }

            if (pressSummonTick > 0) {
                --pressSummonTick;
            }

    }

    @SubscribeEvent
    public static void onPlayerPostTick(ItemTooltipEvent event) {
        if(event.getItemStack().is(AllItems.SCHEMATIC.get())){
            Component useKey = Minecraft.getInstance().options.keyUse.getTranslatedKeyMessage();
            Component summonKey = ModKeyMappings.KEY_SUMMON_BUILDER_ALLAY.getTranslatedKeyMessage();
            event.getToolTip().add(Component.literal("[").append(summonKey).append("] :").withStyle(ChatFormatting.DARK_AQUA));
            event.getToolTip().add(Component.translatable("item.build_allay.tooltip.description").withStyle(ChatFormatting.DARK_AQUA));

        }
    }

}
