package baguchi.build_allay;

import baguchi.build_allay.entity.BuildAllay;
import baguchi.build_allay.registry.ModEntities;
import baguchi.champaign.registry.ModMemorys;
import com.google.common.collect.Lists;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.GlobalPos;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.ForgeMod;

import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

public class BuildAllayUtils {
    public static boolean summonAllay(int allay, ServerPlayer player) {
        Vec3 vec3 = player.getEyePosition();
        Vec3 vec31 = player.getViewVector(1.0F);
        double d0 = player.getAttributeValue((Attribute) ForgeMod.ENTITY_REACH.get());
        vec3.add(vec31.x * d0, vec31.y * d0, vec31.z * d0);
        ServerLevel serverLevel = player.serverLevel();
        HitResult hitResult = player.pick((double) 20.0F, 0.0F, false);
        Vec3 pos = hitResult.getLocation();
        if (hitResult.getType() != HitResult.Type.MISS && allay > 0) {
            HitResult var13 = BlockHitResult.miss(pos, Direction.getNearest(vec3.x, vec3.y, vec3.z), BlockPos.containing(pos));
            if (var13 instanceof BlockHitResult blockHitResult) {
                BlockPos blockpos = blockHitResult.getBlockPos();

                BuildAllay buildAllay = ModEntities.BUILDER_ALLAY.get().create(serverLevel);

                AtomicBoolean flag = new AtomicBoolean(true);
                ItemStack stack = player.getMainHandItem().copy();
                stack.getOrCreateTag().putBoolean("Deployed", true);
                stack.getOrCreateTag().put("Anchor", NbtUtils.writeBlockPos(blockpos));
                buildAllay.initializePrinter(stack);
                buildAllay.getBrain().setMemory(MemoryModuleType.LIKED_PLAYER, player.getUUID());
                buildAllay.setPos(player.position());
                buildAllay.getBrain().setMemory(ModMemorys.WORK_POS.get(), GlobalPos.of(serverLevel.dimension(), blockpos));

                //失敗したときに戻ってくるItemを保持
                List<ItemStack> needItemStack = Lists.newArrayList();
                //必要なItemを数える
                AtomicReference<Item> needItem = new AtomicReference<>();
                AtomicReference<Integer> needCount = new AtomicReference<>();
                buildAllay.checklist.required.forEach(
                        (item, integer) -> {
                            int count = player.getInventory().countItem(item);
                            if (count >= integer) {
                                int actualCount = integer;
                                player.getInventory().clearOrCountMatchingItems(predicate -> {
                                    return predicate.is(item);
                                }, integer, player.getInventory());
                                for (int i = actualCount; i > 64; i -= 64) {
                                    buildAllay.getInventory().addItem(new ItemStack(item, 64));
                                    needItemStack.add(new ItemStack(item, 64));
                                    actualCount -= i;
                                }
                                needItemStack.add(new ItemStack(item, actualCount));
                                buildAllay.getInventory().addItem(new ItemStack(item, actualCount));
                            } else {
                                needItem.set(item);
                                needCount.set(integer - count);
                                flag.set(false);
                            }
                        }
                );
                if (flag.get()) {
                    serverLevel.addFreshEntity(buildAllay);
                    player.getMainHandItem().copyAndClear();
                } else {
                    needItemStack.forEach(itemStack -> {
                        player.getInventory().add(itemStack.copy());
                    });
                    player.displayClientMessage(Component.translatable("build_allay.missing_item", needItem.get().getDescription(), needCount), true);
                    player.playSound(SoundEvents.THORNS_HIT);
                    return false;
                }

                player.playSound(SoundEvents.ALLAY_ITEM_GIVEN);
                return true;
            }
        }
        return false;
    }

}
