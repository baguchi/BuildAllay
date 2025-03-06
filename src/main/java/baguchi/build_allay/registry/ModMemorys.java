package baguchi.build_allay.registry;

import baguchi.build_allay.BuildAllayCore;
import net.minecraft.core.GlobalPos;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

import java.util.Optional;

public class ModMemorys {
    public static final DeferredRegister<MemoryModuleType<?>> MEMORY_REGISTRY = DeferredRegister.create(ForgeRegistries.MEMORY_MODULE_TYPES, BuildAllayCore.MODID);
}