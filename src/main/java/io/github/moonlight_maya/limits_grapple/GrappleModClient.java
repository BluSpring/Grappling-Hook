package io.github.moonlight_maya.limits_grapple;

import net.fabricmc.api.ClientModInitializer;
import net.minecraft.client.item.ModelPredicateProviderRegistry;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Arm;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.RotationAxis;
import net.minecraft.util.math.Vec3d;
import org.joml.Vector3f;

public class GrappleModClient implements ClientModInitializer {

	public static AbstractClientPlayerEntity currentRenderedPlayerEntity;
    public static boolean DISABLE_DEPTH_CLEAR_FLAG = false;

    @Override
	public void onInitializeClient() {
		ModelPredicateProviderRegistry.register(GrappleMod.GRAPPLE_ITEM, Identifier.of(GrappleMod.MODID, "shot"), (itemStack, clientWorld, livingEntity, i) -> {
			if (livingEntity == null) return 0.0f;
			if (!itemStack.isOf(GrappleMod.GRAPPLE_ITEM)) return 0.0f;
			return itemStack.contains(GrappleMod.ACTIVE) ? 1.0f : 0.0f;
		});
	}

	public static void setupGrappleRotation(MatrixStack matrices, ItemStack item, AbstractClientPlayerEntity player, float tickDelta, Hand hand, float swingProgress, float equipProgress, boolean leftHand) {
		Vec3d anchor = item.get(GrappleMod.ANCHOR);
		Vector3f transformedAnchor = RenderingUtils.transformWorldToView(anchor);


		//Stolen from applyEquipmentOffset

		int i = leftHand ? -1 : 1;
		Vector3f handOffset = new Vector3f(i * 0.56F, -0.52F + equipProgress * -0.6F, -0.72f);
		Vector3f scaledHandOffset = new Vector3f(handOffset);
		float scaleFactor = RenderingUtils.getSizeMultiplier(player, tickDelta);
		scaledHandOffset.mul(scaleFactor, -scaleFactor, scaleFactor);
		transformedAnchor.add(scaledHandOffset);

		transformedAnchor.normalize();
		float pitchOffset = (float) (Math.asin(transformedAnchor.y()));
		float yawOffset = (float) (Math.atan2(transformedAnchor.x(), transformedAnchor.z()));

		matrices.scale(scaleFactor, scaleFactor, scaleFactor);
		//Following translation is to move the item from the center of the screen
		//to the left or right side, depending on the hand holding it. This part
		//does not need to take the scale into account.
		matrices.translate(handOffset.x(), handOffset.y(), handOffset.z());
		matrices.translate(i*-1.0/16, 3.0/16, 0);
		matrices.multiply(RotationAxis.POSITIVE_Y.rotation(yawOffset));
		matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(180f));
		matrices.multiply(RotationAxis.POSITIVE_X.rotation(pitchOffset));
	}
}
