package baguchi.build_allay.entity.ai;

import baguchi.build_allay.entity.BuildAllay;
import baguchi.champaign.registry.ModMemorys;
import com.google.common.collect.ImmutableMap;
import com.simibubi.create.foundation.utility.BlockHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.core.GlobalPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.behavior.Behavior;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.minecraft.world.entity.ai.memory.WalkTarget;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

import javax.annotation.Nullable;
import java.util.Optional;

public class Building extends Behavior<BuildAllay> {
    private final float speedMultiplier;
    private boolean workOver = false;
    private BlockPos currentBlockPos;

    public static final int RANGE = 2;

    public Building(float p_275357_) {
        super(ImmutableMap.of(baguchi.champaign.registry.ModMemorys.WORK_POS.get(), MemoryStatus.VALUE_PRESENT), 2400);
        this.speedMultiplier = p_275357_;
    }


    protected boolean canStillUse(ServerLevel p_147391_, BuildAllay p_147392_, long p_147393_) {
        return !workOver;
    }

    protected void start(ServerLevel p_147399_, BuildAllay p_147400_, long p_147401_) {
        this.workOver = false;
        Brain<?> brain = p_147400_.getBrain();
        GlobalPos globalPos = brain.getMemory(baguchi.champaign.registry.ModMemorys.WORK_POS.get()).get();
    }

    protected void stop(ServerLevel p_217118_, BuildAllay p_217119_, long p_217120_) {
        Brain<?> brain = p_217119_.getBrain();
        brain.eraseMemory(baguchi.champaign.registry.ModMemorys.WORK_POS.get());
    }

    protected void tick(ServerLevel level, BuildAllay mob, long p_147405_) {
        Brain<?> brain = mob.getBrain();
        Optional<GlobalPos> globalPos = brain.getMemory(ModMemorys.WORK_POS.get());



        if (globalPos.isPresent()) {
            if(mob.missingItem != null){
            return;
        }

            if (currentBlockPos == null) {
                BlockPos closest = mob.printer.getCurrentTarget();
                if (closest != null) {
                    currentBlockPos = closest;
                } else {
                    workOver = true;
                }
            } else {
                if (currentBlockPos != null) {
                    if (mob.getNavigation().isDone()) {
                        brain.setMemory(MemoryModuleType.WALK_TARGET, new WalkTarget(currentBlockPos, this.speedMultiplier, 1));

                    }
                    if (currentBlockPos.distSqr(mob.blockPosition()) < 2.5F) {
                        mob.printer.handleCurrentTarget((target, blockState, blockEntity) -> {
                            CompoundTag data = BlockHelper.prepareBlockEntityData(blockState, blockEntity);

                            // Launch block
                            mob.statusMsg = blockState.getBlock() != Blocks.AIR ? "placing" : "clearing";
                            launchBlock(level, target, mob.getMainHandItem(), blockState, data);
                            Block.pushEntitiesUp(level.getBlockState(target), blockState, level, target);
                        mob.playSound(blockState.getSoundType().getPlaceSound());
                            }, (target, entity) -> {
                            // Launch entity
                            entity.setPos(target.getCenter());
                            level.addFreshEntity(entity);
                            mob.statusMsg = "placing";
                        });
                        mob.setItemInHand(InteractionHand.MAIN_HAND, ItemStack.EMPTY);
                        // Update Target
                        if (mob.missingItem == null) {
                            if (!mob.printer.advanceCurrentPos()) {
                                mob.finishedPrinting();
                                workOver = true;
                                return;
                            }
                            currentBlockPos = null;
                            mob.sendUpdate = true;
                        }
                    }
                }
            }
        }

    }

    protected void launchBlock(Level level, BlockPos target, ItemStack stack, BlockState state, @Nullable CompoundTag data) {

        BlockHelper.placeSchematicBlock(level, state, target, stack, data);
    }


    private boolean isGatherable(BlockState blockState, ServerLevel level, BlockPos pos, BuildAllay mob) {
        return !blockState.isAir() && blockState.getDestroySpeed(level, pos) >= 0.0 && blockState.getDestroySpeed(level, pos) <= 10F;
    }
}