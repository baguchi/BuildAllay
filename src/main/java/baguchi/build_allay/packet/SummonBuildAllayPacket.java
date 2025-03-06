package baguchi.build_allay.packet;

import baguchi.build_allay.BuildAllayUtils;
import baguchi.champaign.Champaign;
import baguchi.champaign.attachment.ChampaignAttachment;
import com.mojang.datafixers.types.Type;
import com.simibubi.create.AllItems;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class SummonBuildAllayPacket {


    public SummonBuildAllayPacket() {
    }

    public void serialize(FriendlyByteBuf buffer) {
    }

    public static SummonBuildAllayPacket deserialize(FriendlyByteBuf buffer) {
        return new SummonBuildAllayPacket(
        );
    }

    public static void handle(SummonBuildAllayPacket message, Supplier<NetworkEvent.Context> contextSupplier) {
        contextSupplier.get().enqueueWork(() -> {
            Player player = contextSupplier.get().getSender();
            if (player instanceof ServerPlayer serverPlayer) {
                   ChampaignAttachment attachment = player.getCapability(Champaign.CHAMPAIGN_CAPABILITY).orElse(new ChampaignAttachment());
                    if(BuildAllayUtils.summonAllay(attachment.getAllayCount(), serverPlayer)){
                        attachment.setAllayCount(attachment.getAllayCount()- 1, serverPlayer);
                    }
            }
        });
    }
}