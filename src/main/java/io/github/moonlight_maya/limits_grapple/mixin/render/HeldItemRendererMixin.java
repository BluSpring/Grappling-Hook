package io.github.moonlight_maya.limits_grapple.mixin.render;

import io.github.moonlight_maya.limits_grapple.GrappleMod;
import io.github.moonlight_maya.limits_grapple.GrappleModClient;
import io.github.moonlight_maya.limits_grapple.RenderingUtils;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.item.HeldItemRenderer;
import net.minecraft.client.render.model.json.ModelTransformation;
import net.minecraft.client.render.model.json.ModelTransformationMode;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Arm;
import net.minecraft.util.Hand;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RotationAxis;
import net.minecraft.util.math.Vec3d;
import org.joml.Vector3f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(HeldItemRenderer.class)
public abstract class HeldItemRendererMixin {

	@Shadow
	public abstract void renderItem(LivingEntity entity, ItemStack stack, ModelTransformationMode renderMode, boolean leftHanded, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light);

	@Inject(method = "renderItem(Lnet/minecraft/entity/LivingEntity;Lnet/minecraft/item/ItemStack;Lnet/minecraft/client/render/model/json/ModelTransformationMode;ZLnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;I)V", at=@At("HEAD"))
	public void limits_grapple$storeCurrentRenderedPlayer(LivingEntity entity, ItemStack stack, ModelTransformationMode renderMode, boolean leftHanded, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, CallbackInfo ci) {
		if (entity instanceof AbstractClientPlayerEntity acpe)
			if (acpe.getActiveItem().isOf(GrappleMod.GRAPPLE_ITEM))
				GrappleModClient.currentRenderedPlayerEntity = acpe;
	}

	@Inject(method = "renderItem(Lnet/minecraft/entity/LivingEntity;Lnet/minecraft/item/ItemStack;Lnet/minecraft/client/render/model/json/ModelTransformationMode;ZLnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;I)V", at=@At("RETURN"))
	public void limits_grapple$restoreCurrentRenderedPlayer(LivingEntity entity, ItemStack stack, ModelTransformationMode renderMode, boolean leftHanded, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, CallbackInfo ci) {
		GrappleModClient.currentRenderedPlayerEntity = null;
	}

	@Inject(method = "renderItem(FLnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider$Immediate;Lnet/minecraft/client/network/ClientPlayerEntity;I)V", at=@At("HEAD"))
	public void limits_grapple$negateFirstPersonItemSway(float tickDelta, MatrixStack matrices, VertexConsumerProvider.Immediate vertexConsumers, ClientPlayerEntity player, int light, CallbackInfo ci) {
		float h = MathHelper.lerp(tickDelta, player.lastRenderPitch, player.renderPitch);
		float i = MathHelper.lerp(tickDelta, player.lastRenderYaw, player.renderYaw);
		matrices.multiply(RotationAxis.NEGATIVE_Y.rotationDegrees((player.getYaw(tickDelta) - i) * 0.1F));
		matrices.multiply(RotationAxis.NEGATIVE_X.rotationDegrees((player.getPitch(tickDelta) - h) * 0.1F));
	}


	@Inject(method = "renderFirstPersonItem", at = @At("HEAD"), cancellable = true)
	public void limits_grapple$setupFirstPersonArmRotation(AbstractClientPlayerEntity player, float tickDelta, float pitch, Hand hand, float swingProgress, ItemStack item, float equipProgress, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, CallbackInfo ci) {
		if (!item.isOf(GrappleMod.GRAPPLE_ITEM))
			return;
		if (!item.contains(GrappleMod.ACTIVE) || !item.contains(GrappleMod.ANCHOR))
			return;

		matrices.push();
		boolean leftHand = (player.getMainArm() == Arm.LEFT) == (hand == Hand.MAIN_HAND);

		GrappleModClient.setupGrappleRotation(matrices, item, player, tickDelta, hand, swingProgress, equipProgress, leftHand);
		renderItem(player, item, leftHand ? ModelTransformationMode.FIRST_PERSON_LEFT_HAND : ModelTransformationMode.FIRST_PERSON_RIGHT_HAND, leftHand, matrices, vertexConsumers, light);

		matrices.pop();

		ci.cancel();

	}

}
