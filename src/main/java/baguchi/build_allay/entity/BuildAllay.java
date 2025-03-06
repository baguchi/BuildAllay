package baguchi.build_allay.entity;

import baguchi.champaign.entity.AbstractWorkerAllay;
import baguchi.champaign.registry.ModMemorys;
import com.google.common.collect.ImmutableList;
import com.mojang.serialization.Dynamic;
import com.simibubi.create.AllBlocks;
import com.simibubi.create.AllItems;
import com.simibubi.create.AllSoundEvents;
import com.simibubi.create.content.kinetics.belt.BeltBlock;
import com.simibubi.create.content.kinetics.belt.BeltPart;
import com.simibubi.create.content.schematics.SchematicPrinter;
import com.simibubi.create.content.schematics.cannon.MaterialChecklist;
import com.simibubi.create.content.schematics.cannon.SchematicannonBlockEntity;
import com.simibubi.create.content.schematics.requirement.ItemRequirement;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.sensing.Sensor;
import net.minecraft.world.entity.ai.sensing.SensorType;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.npc.InventoryCarrier;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.piston.PistonHeadBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BedPart;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;

import java.util.List;
import java.util.UUID;

import static com.simibubi.create.content.schematics.cannon.SchematicannonBlockEntity.MAX_ANCHOR_DISTANCE;
import static net.minecraft.commands.arguments.coordinates.BlockPosArgument.getBlockPos;

public class BuildAllay extends AbstractWorkerAllay {
    public static final ImmutableList<SensorType<? extends Sensor<? super BuildAllay>>> SENSOR_TYPES = ImmutableList.of(SensorType.NEAREST_LIVING_ENTITIES, SensorType.NEAREST_PLAYERS, SensorType.HURT_BY, SensorType.NEAREST_ITEMS);
    public static final ImmutableList<MemoryModuleType<?>> MEMORY_TYPES = ImmutableList.of(MemoryModuleType.PATH, MemoryModuleType.LOOK_TARGET, MemoryModuleType.NEAREST_VISIBLE_LIVING_ENTITIES, MemoryModuleType.WALK_TARGET, MemoryModuleType.CANT_REACH_WALK_TARGET_SINCE, MemoryModuleType.HURT_BY, MemoryModuleType.NEAREST_VISIBLE_WANTED_ITEM, MemoryModuleType.LIKED_PLAYER, MemoryModuleType.IS_PANICKING, ModMemorys.WORK_POS.get());

    private final SimpleContainer inventory = new SimpleContainer(12);

    public ItemStack missingItem;
    public boolean sendUpdate;
    public SchematicannonBlockEntity.State state;
    public String statusMsg;
    public SchematicPrinter printer = new SchematicPrinter();
    public int blocksPlaced;
    public int blocksToPlace;
    public MaterialChecklist checklist = new MaterialChecklist();

    // Settings
    public int replaceMode;
    public boolean skipMissing;
    public boolean replaceBlockEntities;
    private boolean blockSkipped;

    public BuildAllay(EntityType<? extends BuildAllay> p_218310_, Level p_218311_) {
        super(p_218310_, p_218311_);
        statusMsg = "idle";
    }

    public SimpleContainer getInventory() {
        return this.inventory;
    }

    public boolean wantsToPickUp(ItemStack p_218387_) {
        return this.inventory.canAddItem(p_218387_);
    }

    protected void dropEquipment() {
        super.dropEquipment();
        this.inventory.removeAllItems().forEach((p_375836_) -> this.spawnAtLocation(p_375836_));
    }

    public void giveResource() {
        if (!this.level().isClientSide() && !this.inventory.isEmpty() && this.getBrain().getMemory(MemoryModuleType.LIKED_PLAYER).isPresent()) {
            Player player = this.level().getPlayerByUUID((UUID)this.getBrain().getMemory(MemoryModuleType.LIKED_PLAYER).get());

            for(int j = 0; j < this.inventory.getContainerSize(); ++j) {
                ItemStack itemstack = this.inventory.getItem(j);
                if (!itemstack.isEmpty() && !player.getInventory().add(itemstack)) {
                    player.drop(itemstack, false);
                }
            }

            player.take(this, 1);
        }

    }

    protected Brain.Provider<BuildAllay> brainAllayProvider() {
        return Brain.provider(MEMORY_TYPES, SENSOR_TYPES);
    }

    @Override
    public void addAdditionalSaveData(CompoundTag compound) {
        super.addAdditionalSaveData(compound);

        CompoundTag printerData = new CompoundTag();
        printer.write(printerData);
        statusMsg = compound.getString("Status");
        compound.putString("State", state.name());
        compound.putInt("AmountPlaced", blocksPlaced);
        compound.putInt("AmountToPlace", blocksToPlace);
        // Settings
        CompoundTag options = new CompoundTag();
        options.putInt("ReplaceMode", replaceMode);
        options.putBoolean("SkipMissing", skipMissing);
        options.putBoolean("ReplaceTileEntities", replaceBlockEntities);
        compound.put("Options", options);

    }

    @Override
    public void readAdditionalSaveData(CompoundTag compound) {
        super.readAdditionalSaveData(compound);
        if (compound.contains("Printer"))
            printer.fromTag(compound.getCompound("Printer"), false);
        String stateString = compound.getString("State");
        state = stateString.isEmpty() ? SchematicannonBlockEntity.State.STOPPED : SchematicannonBlockEntity.State.valueOf(compound.getString("State"));
        blocksPlaced = compound.getInt("AmountPlaced");
        blocksToPlace = compound.getInt("AmountToPlace");
        // Settings
        CompoundTag options = compound.getCompound("Options");
        replaceMode = options.getInt("ReplaceMode");
        skipMissing = options.getBoolean("SkipMissing");
        replaceBlockEntities = options.getBoolean("ReplaceTileEntities");

    }

    public void initializePrinter(ItemStack blueprint) {
        if (!blueprint.hasTag()) {
            returnToPlayer();
            state = SchematicannonBlockEntity.State.STOPPED;
            statusMsg = "schematicInvalid";
            sendUpdate = true;
            return;
        }

        if (!blueprint.getTag()
                .getBoolean("Deployed")) {
            returnToPlayer();
            state = SchematicannonBlockEntity.State.STOPPED;
            statusMsg = "schematicNotPlaced";
            sendUpdate = true;
            return;
        }

        // Load blocks into reader
        printer.loadSchematic(blueprint, this.level(), true);

        if (printer.isErrored()) {
            returnToPlayer();
            state = SchematicannonBlockEntity.State.STOPPED;
            statusMsg = "schematicErrored";
            printer.resetSchematic();
            sendUpdate = true;
            return;
        }

        if (printer.isWorldEmpty()) {
            returnToPlayer();
            state = SchematicannonBlockEntity.State.STOPPED;
            printer.resetSchematic();
            sendUpdate = true;
            return;
        }

        if (!printer.getAnchor()
                .closerThan(blockPosition(), MAX_ANCHOR_DISTANCE)) {
            returnToPlayer();
            state = SchematicannonBlockEntity.State.STOPPED;
            statusMsg = "targetOutsideRange";
            printer.resetSchematic();
            sendUpdate = true;
            return;
        }

        state = SchematicannonBlockEntity.State.PAUSED;
        statusMsg = "ready";
        updateChecklist();
        sendUpdate = true;
        blocksToPlace += blocksPlaced;
    }

    public void updateChecklist() {
        checklist.required.clear();
        checklist.damageRequired.clear();
        checklist.blocksNotLoaded = false;

        if (printer.isLoaded() && !printer.isErrored()) {
            blocksToPlace = blocksPlaced;
            blocksToPlace += printer.markAllBlockRequirements(checklist, this.level(), this::shouldPlace);
            printer.markAllEntityRequirements(checklist);
        }

        checklist.gathered.clear();

        for (int slot = 0; slot < inventory.getContainerSize(); slot++) {
            ItemStack stackInSlot = inventory.getItem(slot);
            if (inventory.getItem(slot)
                    .isEmpty())
                continue;
            checklist.collect(stackInSlot);
        }
        sendUpdate = true;
    }

    public void finishedPrinting() {
        this.inventory.addItem(new ItemStack(AllItems.EMPTY_SCHEMATIC, 1));
        state = SchematicannonBlockEntity.State.STOPPED;
        statusMsg = "finished";
        resetPrinter();
        sendUpdate = true;
    }

    public void resetPrinter() {
        printer.resetSchematic();
        missingItem = null;
        sendUpdate = true;
        blocksPlaced = 0;
        blocksToPlace = 0;
    }

    protected boolean shouldPlace(BlockPos pos, BlockState state, BlockEntity be, BlockState toReplace,
                                  BlockState toReplaceOther, boolean isNormalCube) {
        if (pos.closerThan(this.blockPosition(), 2f))
            return false;
        if (!replaceBlockEntities
                && (toReplace.hasBlockEntity() || (toReplaceOther != null && toReplaceOther.hasBlockEntity())))
            return false;

        if (shouldIgnoreBlockState(state, be))
            return false;

        boolean placingAir = state.isAir();

        if (replaceMode == 3)
            return true;
        if (replaceMode == 2 && !placingAir)
            return true;
        if (replaceMode == 1 && (isNormalCube || (!toReplace.isRedstoneConductor(this.level(), pos)
                && (toReplaceOther == null || !toReplaceOther.isRedstoneConductor(this.level(), pos)))) && !placingAir)
            return true;
        if (replaceMode == 0 && !toReplace.isRedstoneConductor(this.level(), pos)
                && (toReplaceOther == null || !toReplaceOther.isRedstoneConductor(this.level(), pos)) && !placingAir)
            return true;

        return false;
    }

    protected boolean shouldIgnoreBlockState(BlockState state, BlockEntity be) {
        // Block doesn't have a mapping (Water, lava, etc)
        if (state.getBlock() == Blocks.STRUCTURE_VOID)
            return true;

        ItemRequirement requirement = ItemRequirement.of(state, be);
        if (requirement.isEmpty())
            return false;
        if (requirement.isInvalid())
            return false;

        // Block doesn't need to be placed twice (Doors, beds, double plants)
        if (state.hasProperty(BlockStateProperties.DOUBLE_BLOCK_HALF)
                && state.getValue(BlockStateProperties.DOUBLE_BLOCK_HALF) == DoubleBlockHalf.UPPER)
            return true;
        if (state.hasProperty(BlockStateProperties.BED_PART)
                && state.getValue(BlockStateProperties.BED_PART) == BedPart.HEAD)
            return true;
        if (state.getBlock() instanceof PistonHeadBlock)
            return true;
        if (AllBlocks.BELT.has(state))
            return state.getValue(BeltBlock.PART) == BeltPart.MIDDLE;
        return false;
    }

    @Override
    protected Brain<?> makeBrain(Dynamic<?> p_218344_) {
        return BuildAllayAi.makeBrain(this.brainAllayProvider().makeBrain(p_218344_));
    }

    @Override
    public Brain<BuildAllay> getBrain() {
        return (Brain<BuildAllay>) super.getBrain();
    }

    @Override
    protected void customServerAiStep() {
        this.level().getProfiler().push("allayBuildBrain");
        this.getBrain().tick((ServerLevel) this.level(), this);
        this.level().getProfiler().pop();
        this.level().getProfiler().push("allayBuildActivityUpdate");
        BuildAllayAi.updateActivity(this);
        this.level().getProfiler().pop();
        super.customServerAiStep();
    }

    @Override
    public void tick() {
        super.tick();
        // Get item requirement
        if(printer.isLoaded() && this.getMainHandItem().isEmpty()) {
            ItemRequirement requirement = printer.getCurrentRequirement();
            if (requirement.isInvalid() || !printer.shouldPlaceCurrent(this.level(), this::shouldPlace)) {
                sendUpdate = !statusMsg.equals("searching");
                statusMsg = "searching";
                blockSkipped = true;
                return;
            }

            // Find item
            List<ItemRequirement.StackRequirement> requiredItems = requirement.getRequiredItems();
            if (!requirement.isEmpty()) {
                for (ItemRequirement.StackRequirement required : requiredItems) {
                    if (!grabItemsFromAttachedInventories(required, true)) {
                        if (skipMissing) {
                            statusMsg = "skipping";
                            blockSkipped = true;
                            if (missingItem != null) {
                                missingItem = null;
                                state = SchematicannonBlockEntity.State.RUNNING;
                            }
                            return;
                        }

                        missingItem = required.stack;
                        state = SchematicannonBlockEntity.State.PAUSED;
                        statusMsg = "missingBlock";
                        return;
                    }
                }

                for (ItemRequirement.StackRequirement required : requiredItems)
                    grabItemsFromAttachedInventories(required, false);
            }

            // Success
            state = SchematicannonBlockEntity.State.RUNNING;

            sendUpdate = true;
            missingItem = null;
        }
    }

    protected boolean grabItemsFromAttachedInventories(ItemRequirement.StackRequirement required, boolean simulate) {
        // Find and remove
        boolean success = false;
        int amountFound = 0;

        if(simulate) {
            if(required.stack.getCount() <= this.inventory.countItem(required.stack.getItem())) {
                success = true;
            }
        }

        if(!simulate) {
            this.inventory.removeItemType(required.stack.getItem(), required.stack.getCount());
this.setItemInHand(InteractionHand.MAIN_HAND, required.stack);

            success = true;
        }


        return success;
    }
}
