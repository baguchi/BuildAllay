package baguchi.build_allay.entity;

import baguchi.champaign.entity.ai.BackToPlayer;
import baguchi.build_allay.entity.ai.Building;
import baguchi.champaign.registry.ModMemorys;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.mojang.datafixers.util.Pair;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.valueproviders.UniformInt;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.behavior.*;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.schedule.Activity;
import net.minecraft.world.level.Level;

import java.util.Optional;
import java.util.UUID;

public class BuildAllayAi {
    private static final float SPEED_MULTIPLIER_WHEN_IDLING = 1.0F;
    private static final float SPEED_MULTIPLIER_WHEN_FOLLOWING_DEPOSIT_TARGET = 2.25F;
    private static final float SPEED_MULTIPLIER_WHEN_RETRIEVING_ITEM = 1.75F;
    private static final float SPEED_MULTIPLIER_WHEN_PANICKING = 2.5F;
    private static final int CLOSE_ENOUGH_TO_TARGET = 4;
    private static final int TOO_FAR_FROM_TARGET = 16;
    private static final int MAX_LOOK_DISTANCE = 6;
    private static final int MIN_WAIT_DURATION = 30;
    private static final int MAX_WAIT_DURATION = 60;
    private static final int TIME_TO_FORGET_NOTEBLOCK = 600;
    private static final int DISTANCE_TO_WANTED_ITEM = 32;
    private static final int GIVE_ITEM_TIMEOUT_DURATION = 20;

    protected static Brain<?> makeBrain(Brain<BuildAllay> p_218420_) {
        initCoreActivity(p_218420_);
        initIdleActivity(p_218420_);
        initWorkActivity(p_218420_);
        p_218420_.setCoreActivities(ImmutableSet.of(Activity.CORE));
        p_218420_.setDefaultActivity(Activity.IDLE);
        p_218420_.useDefaultActivity();
        return p_218420_;
    }

    private static void initCoreActivity(Brain<BuildAllay> p_218426_) {
        p_218426_.addActivity(Activity.CORE, 0, ImmutableList.of(
                new Swim(0.8F),
                new AnimalPanic(2.5F),
                new LookAtTargetSink(45, 90),
                new MoveToTargetSink(),
                new CountDownCooldownTicks(MemoryModuleType.LIKED_NOTEBLOCK_COOLDOWN_TICKS),
                new CountDownCooldownTicks(MemoryModuleType.ITEM_PICKUP_COOLDOWN_TICKS)
        ));
    }

    private static void initWorkActivity(Brain<BuildAllay> p_35126_) {
        p_35126_.addActivityAndRemoveMemoryWhenStopped(Activity.WORK, 10, ImmutableList.of(new Building(1.2F)), ModMemorys.WORK_POS.get());
    }

    private static void initIdleActivity(Brain<BuildAllay> p_218432_) {
        p_218432_.addActivityWithConditions(Activity.IDLE, ImmutableList.of(Pair.of(0, GoToWantedItem.create((p_218428_) -> {
            return true;
        }, 1.75F, true, 32)), Pair.of(2, new BackToPlayer(1.5F)), Pair.of(3, SetEntityLookTargetSometimes.create(6.0F, UniformInt.of(30, 60))), Pair.of(4, new RunOne<>(ImmutableList.of(Pair.of(RandomStroll.fly(1.0F), 2), Pair.of(SetWalkTargetFromLookTarget.create(1.0F, 3), 2), Pair.of(new DoNothing(30, 60), 1))))), ImmutableSet.of());
    }

    public static void updateActivity(BuildAllay p_218422_) {
        p_218422_.getBrain().setActiveActivityToFirstValid(ImmutableList.of(Activity.WORK, Activity.IDLE));
    }

    private static boolean hasWantedItem(LivingEntity p_273346_) {
        Brain<?> brain = p_273346_.getBrain();
        return brain.hasMemoryValue(MemoryModuleType.NEAREST_VISIBLE_WANTED_ITEM);
    }

    public static Optional<PositionTracker> getLikedPlayerPositionTracker(LivingEntity p_218430_) {
        return getLikedPlayer(p_218430_).map((p_218409_) -> {
            return new EntityTracker(p_218409_, true);
        });
    }

    public static Optional<ServerPlayer> getLikedPlayer(LivingEntity p_218411_) {
        Level level = p_218411_.level();
        if (!level.isClientSide() && level instanceof ServerLevel serverlevel) {
            Optional<UUID> optional = p_218411_.getBrain().getMemory(MemoryModuleType.LIKED_PLAYER);
            if (optional.isPresent()) {
                Entity entity = serverlevel.getEntity(optional.get());
                if (entity instanceof ServerPlayer) {
                    ServerPlayer serverplayer = (ServerPlayer) entity;
                    if ((serverplayer.gameMode.isSurvival() || serverplayer.gameMode.isCreative()) && serverplayer.closerThan(p_218411_, 64.0D)) {
                        return Optional.of(serverplayer);
                    }
                }

                return Optional.empty();
            }
        }

        return Optional.empty();
    }
}