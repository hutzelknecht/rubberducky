package de.immerdieses.rubberducky.client.model;

// Model geometry ported from salvadormg15/Rubber-Duck (MIT License)
// https://github.com/salvadormg15/Rubber-Duck

import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;

public class RubberDuckModel extends EntityModel<RubberDuckRenderState> {

    public static final ModelLayerLocation LAYER_LOCATION =
            new ModelLayerLocation(ResourceLocation.fromNamespaceAndPath("rubberducky", "rubber_duck"), "main");

    private final ModelPart root;
    private final ModelPart body;
    private final ModelPart head;
    private final ModelPart beak;
    private final ModelPart leftWing;
    private final ModelPart rightWing;
    private final ModelPart tail;

    public RubberDuckModel(ModelPart root) {
        super(root);
        this.root = root;
        this.body      = root.getChild("body");
        this.head      = root.getChild("head");
        this.beak      = root.getChild("beak");
        this.leftWing  = root.getChild("left_wing");
        this.rightWing = root.getChild("right_wing");
        this.tail      = root.getChild("tail");
    }

    /**
     * Geometry ported 1:1 from salvadormg15's block model JSON.
     * Original coordinates were in block-space [0-16] centred at x=8, z=8.
     * Shifted by (-8, 0, -8) so the duck is centred on the entity origin.
     */
    public static LayerDefinition createBodyLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition parts = mesh.getRoot();

        // Body: [5,0,5]→[11,4,11] → shifted: [-3,0,-3]→[3,4,3]
        parts.addOrReplaceChild("body",
                CubeListBuilder.create().texOffs(0, 0).addBox(-3f, -4f, -3f, 6, 4, 6),
                PartPose.offset(0f, 24f, 0f));

        // Head: [6,4,3]→[10,8,7] → shifted: [-2,0,-5]→[2,4,-1], placed above body
        parts.addOrReplaceChild("head",
                CubeListBuilder.create().texOffs(24, 0).addBox(-2f, -4f, -2f, 4, 4, 4),
                PartPose.offset(0f, 20f, -2f));

        // Beak: centred on front face of head, vertically mid-head (~y=17-18)
        parts.addOrReplaceChild("beak",
                CubeListBuilder.create().texOffs(36, 0).addBox(-1.5f, -1f, -2f, 3, 1, 2),
                PartPose.offset(0f, 18f, -4f));

        // Left wing: thin slab on left side
        parts.addOrReplaceChild("left_wing",
                CubeListBuilder.create().texOffs(0, 10).addBox(-0.4f, -2.6f, 0f, 1, 3, 4),
                PartPose.offset(-3.6f, 22f, -1f));

        // Right wing: mirror of left
        parts.addOrReplaceChild("right_wing",
                CubeListBuilder.create().texOffs(0, 10).addBox(-0.6f, -2.6f, 0f, 1, 3, 4),
                PartPose.offset(3.6f, 22f, -1f));

        // Tail: small bump at the back
        parts.addOrReplaceChild("tail",
                CubeListBuilder.create().texOffs(0, 14).addBox(-2f, -1f, 0f, 4, 1, 1),
                PartPose.offset(0f, 21f, 3f));

        return LayerDefinition.create(mesh, 64, 32);
    }

    @Override
    public void setupAnim(RubberDuckRenderState state) {
        super.setupAnim(state);

        // Gentle bob using ageInTicks equivalent from render state
        float bob = Mth.sin(state.ageInTicks * 0.15f) * 0.04f;
        body.y = 24f + bob;
        head.y = 20f + bob;
        beak.y = 18f + bob;
        tail.y = 21f + bob;

        // Head turns with body yaw delta
        head.yRot = state.bodyRot * Mth.DEG_TO_RAD * 0.5f;
        beak.yRot = head.yRot;

        // Wing flutter proportional to walk speed
        float speed = state.walkAnimationSpeed;
        float wingFlap = Mth.sin(state.walkAnimationPos * 0.8f) * 0.3f * speed;
        leftWing.zRot  = -wingFlap;
        rightWing.zRot =  wingFlap;
    }
}

