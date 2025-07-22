package io.github.moonlight_maya.limits_grapple.mixin.render;

import com.mojang.blaze3d.systems.RenderSystem;
import io.github.moonlight_maya.limits_grapple.GrappleMod;
import io.github.moonlight_maya.limits_grapple.item.GrappleItem;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.hud.InGameHud;
import net.minecraft.client.option.Perspective;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Arm;
import net.minecraft.util.Identifier;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import org.joml.Vector4f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(InGameHud.class)
public class InGameHudMixin {
	@Unique private static final Identifier limits_grapple$CROSSHAIR_TEXTURE = Identifier.of(GrappleMod.MODID, "textures/crosshair_indicator.png");
	@Unique private static final int limits_grapple$CROSSHAIR_TEXTURE_WIDTH = 15;
	@Unique private static final int limits_grapple$CROSSHAIR_TEXTURE_HEIGHT = 15;

	@Unique private static final Vector4f limits_grapple$HIT_COLOR = new Vector4f(0.2f, 0.2f, 1.0f, 1.0f);
	@Unique private static final Vector4f limits_grapple$MISS_COLOR = new Vector4f(0.6f, 0.6f, 0.3f, 1.0f);

	@Inject(method = "renderCrosshair", at = @At("HEAD"))
	public void limits_grapple$renderCrosshairIndicator(DrawContext context, RenderTickCounter tickCounter, CallbackInfo ci) {

		MinecraftClient client = MinecraftClient.getInstance();
		if (client.options.getPerspective() != Perspective.FIRST_PERSON)
			return;

		Entity entity = client.getCameraEntity();
		if (entity instanceof PlayerEntity player) {
			Arm mainArm = player.getMainArm();
			ItemStack rightStack = mainArm == Arm.RIGHT ? player.getMainHandStack() : player.getOffHandStack();
			ItemStack leftStack = mainArm == Arm.LEFT ? player.getMainHandStack() : player.getOffHandStack();

			boolean rightGrapple = rightStack.isOf(GrappleMod.GRAPPLE_ITEM);
			boolean leftGrapple = leftStack.isOf(GrappleMod.GRAPPLE_ITEM);

			if (rightGrapple || leftGrapple) {
				//Set render state
				RenderSystem.disableBlend();

				if (rightGrapple && leftGrapple) {
					limits_grapple$drawHitResult(context, GrappleItem.raycast(player, rightStack), false);
					limits_grapple$drawHitResult(context, GrappleItem.raycast(player, leftStack), true);
				} else {
					ItemStack grappleStack = rightGrapple ? rightStack : leftStack;
					BlockHitResult hitResult = GrappleItem.raycast(player, grappleStack);
					limits_grapple$drawHitResult(context, hitResult, false);
					limits_grapple$drawHitResult(context, hitResult, true);
				}

				//Restore state
				context.setShaderColor(1, 1, 1, 1);
				RenderSystem.enableBlend();
			}
		}

	}

	@Unique
	private void limits_grapple$drawHitResult(DrawContext context, BlockHitResult hitResult, boolean left) {

		boolean wouldHit = hitResult.getType() != HitResult.Type.MISS &&
				MinecraftClient.getInstance().world != null &&
				!MinecraftClient.getInstance().world.getBlockState(hitResult.getBlockPos()).isIn(GrappleMod.NO_GRAPPLE_BLOCKS);
		//Set color
		if (wouldHit)
			context.setShaderColor(limits_grapple$HIT_COLOR.x(), limits_grapple$HIT_COLOR.y(), limits_grapple$HIT_COLOR.z(), limits_grapple$HIT_COLOR.w());
		else
			context.setShaderColor(limits_grapple$MISS_COLOR.x(), limits_grapple$MISS_COLOR.y(), limits_grapple$MISS_COLOR.z(), limits_grapple$MISS_COLOR.w());

		//Get vars
		int x = (context.getScaledWindowWidth() - limits_grapple$CROSSHAIR_TEXTURE_WIDTH) / 2;
		int y = (context.getScaledWindowHeight() - limits_grapple$CROSSHAIR_TEXTURE_HEIGHT) / 2;
		int v = left ? limits_grapple$CROSSHAIR_TEXTURE_HEIGHT : 0;

		context.drawTexture(limits_grapple$CROSSHAIR_TEXTURE, x, y, 0, v, limits_grapple$CROSSHAIR_TEXTURE_WIDTH, limits_grapple$CROSSHAIR_TEXTURE_HEIGHT, limits_grapple$CROSSHAIR_TEXTURE_WIDTH, limits_grapple$CROSSHAIR_TEXTURE_HEIGHT * 2);
	}





}
