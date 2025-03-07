package baguchi.build_allay.client.render;

import baguchi.build_allay.BuildAllayCore;
import baguchi.build_allay.client.WorkerAllayModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.entity.layers.ItemInHandLayer;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;

public class BuildAllayRender<T extends baguchi.champaign.entity.AbstractWorkerAllay> extends MobRenderer<T, WorkerAllayModel<T>> {
    private static final ResourceLocation ALLAY_TEXTURE = ResourceLocation.fromNamespaceAndPath(BuildAllayCore.MODID, "textures/entity/allay.png");

    public BuildAllayRender(EntityRendererProvider.Context p_234551_) {
        super(p_234551_, new WorkerAllayModel<>(p_234551_.bakeLayer(ModelLayers.ALLAY)), 0.4F);
        this.addLayer(new ItemInHandLayer<>(this, p_234551_.getItemInHandRenderer()));
    }

    @Override
    protected int getBlockLightLevel(T p_234560_, BlockPos p_234561_) {
        return 15;
    }

    @Override
    public ResourceLocation getTextureLocation(T p_368654_) {
        return ALLAY_TEXTURE;
    }
}