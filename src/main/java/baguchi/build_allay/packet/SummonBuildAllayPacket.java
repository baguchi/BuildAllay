package baguchi.build_allay.packet;

import baguchi.build_allay.BuildAllayCore;
import baguchi.build_allay.BuildAllayUtils;
import baguchi.champaign.attachment.ChampaignAttachment;
import baguchi.champaign.registry.ModAttachments;
import com.mojang.datafixers.types.Type;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.neoforged.neoforge.network.handling.IPayloadHandler;

public class SummonBuildAllayPacket implements CustomPacketPayload, IPayloadHandler<SummonBuildAllayPacket> {

    public static final StreamCodec<FriendlyByteBuf, SummonBuildAllayPacket> STREAM_CODEC = CustomPacketPayload.codec(
            SummonBuildAllayPacket::write, SummonBuildAllayPacket::new
    );
    public static final Type<SummonBuildAllayPacket> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(BuildAllayCore.MODID, "summon_build_allay"));


    public SummonBuildAllayPacket() {
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public void write(FriendlyByteBuf buffer) {
    }

    public SummonBuildAllayPacket(FriendlyByteBuf buffer) {
        this(
        );
    }

    public void handle(SummonBuildAllayPacket message, IPayloadContext context) {
        context.enqueueWork(() -> {
            Player player = context.player();
            if (player instanceof ServerPlayer serverPlayer) {
                ChampaignAttachment attachment = player.getData(ModAttachments.CHAMPAIGN);
                    if(BuildAllayUtils.summonAllay(attachment.getAllayCount(), serverPlayer)){
                        attachment.setAllayCount(attachment.getAllayCount()- 1, serverPlayer);
                    }
            }
        });
    }
}