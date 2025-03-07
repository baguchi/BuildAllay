package baguchi.build_allay.registry;

import baguchi.build_allay.BuildAllayCore;
import baguchi.build_allay.entity.BuildAllay;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.animal.allay.Allay;
import net.minecraft.world.level.levelgen.Heightmap;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.EntityAttributeCreationEvent;
import net.neoforged.neoforge.event.entity.RegisterSpawnPlacementsEvent;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

@EventBusSubscriber(modid = BuildAllayCore.MODID, bus = EventBusSubscriber.Bus.MOD)
public class ModEntities {
    public static final DeferredRegister<EntityType<?>> ENTITIES_REGISTRY = DeferredRegister.create(BuiltInRegistries.ENTITY_TYPE, BuildAllayCore.MODID);

    public static final DeferredHolder<EntityType<?>, EntityType<BuildAllay>> BUILDER_ALLAY = ENTITIES_REGISTRY.register("build_allay", () -> EntityType.Builder.of(BuildAllay::new, MobCategory.CREATURE).sized(0.6F, 0.6F).build(prefix("build_allay")));

    private static String prefix(String path) {
        return BuildAllayCore.MODID + "." + path;
    }

    @SubscribeEvent
    public static void registerEntity(EntityAttributeCreationEvent event) {
        event.put(BUILDER_ALLAY.get(), Allay.createAttributes().build());
    }

    @SubscribeEvent
    public static void registerSpawnPlacement(RegisterSpawnPlacementsEvent event) {
        event.register(BUILDER_ALLAY.get(), SpawnPlacementTypes.NO_RESTRICTIONS, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, Mob::checkMobSpawnRules, RegisterSpawnPlacementsEvent.Operation.OR);
    }
}