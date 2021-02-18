package dev.tr7zw.entityculling.mixin;

import java.util.function.Function;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import dev.tr7zw.entityculling.Drawer;
import dev.tr7zw.entityculling.access.CachedText;
import net.minecraft.client.font.FontStorage;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.util.math.Vector3f;
import net.minecraft.text.OrderedText;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Matrix4f;

@Mixin(TextRenderer.class)
public abstract class TextRendererMixin {

	@Shadow
	private Function<Identifier, FontStorage> fontStorageAccessor;
	private static final Vector3f FORWARD_SHIFT = new Vector3f(0.0f, 0.0f, 0.03f);

	@Inject(method = "Lnet/minecraft/client/font/TextRenderer;draw(Lnet/minecraft/text/Text;FFIZLnet/minecraft/util/math/Matrix4f;Lnet/minecraft/client/render/VertexConsumerProvider;ZII)I", at = @At("HEAD"), cancellable = true)
	public int draw(Text text, float x, float y, int color, boolean shadow, Matrix4f matrix,
			VertexConsumerProvider vertexConsumers, boolean seeThrough, int backgroundColor, int light,
			CallbackInfoReturnable<Integer> info) {
		info.setReturnValue(this.drawInternal(text, x, y, color, shadow, matrix, vertexConsumers, seeThrough,
				backgroundColor, light));
		info.cancel();
		return 0;
	}

	private int drawInternal(Text text, float x, float y, int color, boolean shadow, Matrix4f matrix,
			VertexConsumerProvider vertexConsumerProvider, boolean seeThrough, int backgroundColor, int light) {
		color = tweakTransparency(color);
		Matrix4f matrix4f = matrix.copy();
		if (shadow) {
			this.drawLayer(text, x, y, color, true, matrix, vertexConsumerProvider, seeThrough, backgroundColor, light);
			matrix4f.addToLastColumn(FORWARD_SHIFT);
		}
		x = this.drawLayer(text, x, y, color, false, matrix4f, vertexConsumerProvider, seeThrough, backgroundColor,
				light);
		return (int) x + (shadow ? 1 : 0);
	}

	private float drawLayer(Text text, float x, float y, int color, boolean shadow, Matrix4f matrix,
			VertexConsumerProvider vertexConsumerProvider, boolean seeThrough, int underlineColor, int light) {
		if (text instanceof CachedText) {
			CachedText cached = (CachedText) text;
			if (cached.getDrawer() != null) {
				cached.getDrawer().drawAll(vertexConsumerProvider, matrix);
				return x;
			}
			Drawer drawer = new Drawer(fontStorageAccessor, x, y, underlineColor, shadow, seeThrough, light);
			text.asOrderedText().accept(drawer);
			drawer.drawLayer(underlineColor, x);
			System.out.println("uncached rendering " + text.hashCode());
			cached.setDrawer(drawer);
			cached.getDrawer().drawAll(vertexConsumerProvider, matrix);
			return x;
		}
		return drawLayer(text.asOrderedText(), x, y, color, shadow, matrix, vertexConsumerProvider, seeThrough,
				underlineColor, light);
	}

	@Shadow
	protected abstract float drawLayer(OrderedText text, float x, float y, int color, boolean shadow, Matrix4f matrix,
			VertexConsumerProvider vertexConsumerProvider, boolean seeThrough, int underlineColor, int light);

	private static int tweakTransparency(int argb) {
		if ((argb & -67108864) == 0) {
			return argb | -16777216;
		}
		return argb;
	}

}
