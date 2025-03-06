package baguchi.build_allay.client;

import baguchi.champaign.entity.AbstractWorkerAllay;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.model.ArmedModel;
import net.minecraft.client.model.HierarchicalModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.HumanoidArm;

public class WorkerAllayModel<T extends AbstractWorkerAllay> extends HierarchicalModel<T> implements ArmedModel {
    private final ModelPart root;
    private final ModelPart head;
    private final ModelPart body;
    private final ModelPart right_arm;
    private final ModelPart left_arm;
    private final ModelPart right_wing;
    private final ModelPart left_wing;
    private static final float FLYING_ANIMATION_X_ROT = ((float) Math.PI / 4F);
    private static final float MAX_HAND_HOLDING_ITEM_X_ROT_RAD = -1.134464F;
    private static final float MIN_HAND_HOLDING_ITEM_X_ROT_RAD = (-(float) Math.PI / 3F);

    public WorkerAllayModel(ModelPart p_233312_) {
        super(RenderType::entityTranslucent);
        this.root = p_233312_.getChild("root");
        this.head = this.root.getChild("head");
        this.body = this.root.getChild("body");
        this.right_arm = this.body.getChild("right_arm");
        this.left_arm = this.body.getChild("left_arm");
        this.right_wing = this.body.getChild("right_wing");
        this.left_wing = this.body.getChild("left_wing");
    }

    @Override
    public ModelPart root() {
        return this.root;
    }

    @Override
    public void setupAnim(T p_233325_, float p_233326_, float p_233327_, float p_233328_, float p_233329_, float p_233330_) {
        this.root().getAllParts().forEach(ModelPart::resetPose);
        float f = p_233328_ * 20.0F * (float) (Math.PI / 180.0) + p_233326_;
        float f1 = Mth.cos(f) * (float) Math.PI * 0.15F + p_233327_;
        float f2 = p_233328_ - (float) p_233325_.tickCount;
        float f3 = p_233328_ * 9.0F * (float) (Math.PI / 180.0);
        float f4 = Math.min(p_233327_ / 0.3F, 1.0F);
        float f5 = 1.0F - f4;

        this.head.xRot = p_233330_ * (float) (Math.PI / 180.0);
        this.head.yRot = p_233329_ * (float) (Math.PI / 180.0);


        this.right_wing.xRot = 0.43633232F * (1.0F - f4);
        this.right_wing.yRot = (float) (-Math.PI / 4) + f1;
        this.left_wing.xRot = 0.43633232F * (1.0F - f4);
        this.left_wing.yRot = (float) (Math.PI / 4) - f1;
        this.body.xRot = f4 * (float) (Math.PI / 4);
        this.root.y = this.root.y + (float) Math.cos((double) f3) * 0.25F * f5;

        float f13 = f5 * (1.0F);
        float f14 = 0.43633232F - Mth.cos(f3 + (float) (Math.PI * 3.0 / 2.0)) * (float) Math.PI * 0.075F * f13;
        this.left_arm.zRot = -f14;
        this.right_arm.zRot = f14;
    }

    @Override
    public void translateToHand(HumanoidArm p_233322_, PoseStack p_233323_) {
        float f = 1.0F;
        float f1 = 3.0F;
        this.root.translateAndRotate(p_233323_);
        this.body.translateAndRotate(p_233323_);
        p_233323_.translate(0.0F, 0.0625F, 0.1875F);
        p_233323_.mulPose(Axis.XP.rotation(this.right_arm.xRot));
        p_233323_.scale(0.7F, 0.7F, 0.7F);
        p_233323_.translate(0.0625F, 0.0F, 0.0F);
    }
}
