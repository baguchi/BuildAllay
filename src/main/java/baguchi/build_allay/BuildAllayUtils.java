package baguchi.build_allay;

import baguchi.build_allay.entity.BuildAllay;
import baguchi.build_allay.registry.ModEntities;
import baguchi.champaign.entity.GatherAllay;
import baguchi.champaign.registry.ModMemorys;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.GlobalPos;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.ForgeMod;

public class BuildAllayUtils {
    public static boolean summonAllay(int allay, ServerPlayer player) {
        Vec3 vec3 = player.getEyePosition();
        Vec3 vec31 = player.getViewVector(1.0F);
        double d0 = player.getAttributeValue((Attribute) ForgeMod.ENTITY_REACH.get());
        vec3.add(vec31.x * d0, vec31.y * d0, vec31.z * d0);
        ServerLevel serverLevel = player.serverLevel();
        HitResult hitResult = player.pick((double)20.0F, 0.0F, false);
        Vec3 pos = hitResult.getLocation();
        if (hitResult.getType() != HitResult.Type.MISS && allay > 0) {
            HitResult var13 = BlockHitResult.miss(pos, Direction.getNearest(vec3.x, vec3.y, vec3.z), BlockPos.containing(pos));
            if (var13 instanceof BlockHitResult blockHitResult) {
                BlockPos blockpos = blockHitResult.getBlockPos();

                    BuildAllay buildAllay = ModEntities.BUILDER_ALLAY.get().create(serverLevel);

                ItemStack stack = player.getMainHandItem().copyAndClear();
                stack.getOrCreateTag().putBoolean("Deployed", true);
                stack.getOrCreateTag().put("Anchor", NbtUtils.writeBlockPos(blockpos.above()));
                    buildAllay.initializePrinter(stack);
                    //buildAllay.setItemInHand(InteractionHand.MAIN_HAND, player.getMainHandItem().copyAndClear());
                    buildAllay.getBrain().setMemory(MemoryModuleType.LIKED_PLAYER, player.getUUID());
                    buildAllay.setPos(player.position());
                    buildAllay.getBrain().setMemory(ModMemorys.WORK_POS.get(), GlobalPos.of(serverLevel.dimension(), blockpos));

                    buildAllay.checklist.required.forEach(
                            (item ,integer) -> {
                                int i = player.getInventory().findSlotMatchingItem(item.getDefaultInstance());
                                buildAllay.getInventory().addItem(player.getInventory().getItem(i).copyAndClear());
                            }
                    );
                    serverLevel.addFreshEntity(buildAllay);


                player.playSound(SoundEvents.ALLAY_ITEM_GIVEN);
                return true;
            }
        }
        return false;
    }
}
