package de.immerdieses.rubberducky.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import de.immerdieses.rubberducky.client.model.RubberDuckModel;
import de.immerdieses.rubberducky.client.model.RubberDuckRenderState;
import de.immerdieses.rubberducky.entity.RubberDuckEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;

public class RubberDuckRenderer extends MobRenderer<RubberDuckEntity, RubberDuckRenderState, RubberDuckModel> {

    private static final ResourceLocation TEXTURE =
            ResourceLocation.fromNamespaceAndPath("rubberducky", "textures/entity/rubber_duck.png");

    public RubberDuckRenderer(EntityRendererProvider.Context context) {
        super(context, new RubberDuckModel(context.bakeLayer(RubberDuckModel.LAYER_LOCATION)), 0.4f);
    }

    @Override
    public RubberDuckRenderState createRenderState() {
        return new RubberDuckRenderState();
    }

    @Override
    public ResourceLocation getTextureLocation(RubberDuckRenderState state) {
        return TEXTURE;
    }

    @Override
    protected void scale(RubberDuckRenderState state, PoseStack poseStack) {
        poseStack.scale(1.4f, 1.4f, 1.4f);
    }
}

