package dev.tr7zw.entityculling;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Function;

import org.jetbrains.annotations.Nullable;

import com.google.common.collect.Lists;

import net.minecraft.client.font.EmptyGlyphRenderer;
import net.minecraft.client.font.FontStorage;
import net.minecraft.client.font.Glyph;
import net.minecraft.client.font.GlyphRenderer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.text.CharacterVisitor;
import net.minecraft.text.Style;
import net.minecraft.text.TextColor;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Matrix4f;

public class Drawer implements CharacterVisitor {
	private final boolean shadow;
	private final float brightnessMultiplier;
	private final float red;
	private final float green;
	private final float blue;
	private final float alpha;
	private final boolean seeThrough;
	private final int light;
	private float x;
	private float y;
	@Nullable
	private List<GlyphRenderer.Rectangle> rectangles;
	private List<BiConsumer<VertexConsumerProvider, Matrix4f>> glyphes = new ArrayList<>();
	private Function<Identifier, FontStorage> fontStorageAccessor;

	private void addRectangle(GlyphRenderer.Rectangle rectangle) {
		if (this.rectangles == null) {
			this.rectangles = Lists.newArrayList();
		}
		this.rectangles.add(rectangle);
	}

	public Drawer(Function<Identifier, FontStorage> fontStorageAccessor, float x, float y, int color, boolean shadow,
			boolean seeThrough, int light) {
		this.fontStorageAccessor = fontStorageAccessor;
		this.x = x;
		this.y = y;
		this.shadow = shadow;
		this.brightnessMultiplier = shadow ? 0.25f : 1.0f;
		this.red = (float) (color >> 16 & 255) / 255.0f * this.brightnessMultiplier;
		this.green = (float) (color >> 8 & 255) / 255.0f * this.brightnessMultiplier;
		this.blue = (float) (color & 255) / 255.0f * this.brightnessMultiplier;
		this.alpha = (float) (color >> 24 & 255) / 255.0f;
		this.seeThrough = seeThrough;
		this.light = light;
	}

	public boolean accept(int i, Style style, int j) {
		float o;
		float s;
		float m;
		float n;
		FontStorage fontStorage = fontStorageAccessor.apply(style.getFont());
		Glyph glyph = fontStorage.getGlyph(j);
		GlyphRenderer glyphRenderer = style.isObfuscated() && j != 32
				? fontStorage.getObfuscatedGlyphRenderer(glyph)
				: fontStorage.getGlyphRenderer(j);
		boolean bl = style.isBold();
		float f = this.alpha;
		TextColor textColor = style.getColor();
		if (textColor != null) {
			int k = textColor.getRgb();
			m = (float) (k >> 16 & 255) / 255.0f * this.brightnessMultiplier;
			n = (float) (k >> 8 & 255) / 255.0f * this.brightnessMultiplier;
			o = (float) (k & 255) / 255.0f * this.brightnessMultiplier;
		} else {
			m = this.red;
			n = this.green;
			o = this.blue;
		}
		if (!(glyphRenderer instanceof EmptyGlyphRenderer)) {
			float p = bl ? glyph.getBoldOffset() : 0.0f;
			float q = this.shadow ? glyph.getShadowOffset() : 0.0f;
			final float fx = x;
			glyphes.add((consumer, matrix) -> {
				VertexConsumer vertexConsumer = consumer.getBuffer(glyphRenderer.getLayer(this.seeThrough));
				drawGlyph(glyphRenderer, bl, style.isItalic(), p, fx + q, this.y + q, matrix,
					vertexConsumer, m, n, o, 1, this.light);
			});
			
		}
		float r = glyph.getAdvance(bl);
		s = this.shadow ? 1.0f : 0.0f;
		if (style.isStrikethrough()) {
			this.addRectangle(new GlyphRenderer.Rectangle(this.x + s - 1.0f, this.y + s + 4.5f, this.x + s + r,
					this.y + s + 4.5f - 1.0f, 0.01f, m, n, o, f));
		}
		if (style.isUnderlined()) {
			this.addRectangle(new GlyphRenderer.Rectangle(this.x + s - 1.0f, this.y + s + 9.0f, this.x + s + r,
					this.y + s + 9.0f - 1.0f, 0.01f, m, n, o, f));
		}
		this.x += r;
		return true;
	}

	public float drawLayer(int underlineColor, float x) {
		if (underlineColor != 0) {
			float f = (float) (underlineColor >> 24 & 255) / 255.0f;
			float g = (float) (underlineColor >> 16 & 255) / 255.0f;
			float h = (float) (underlineColor >> 8 & 255) / 255.0f;
			float i = (float) (underlineColor & 255) / 255.0f;
			this.addRectangle(new GlyphRenderer.Rectangle(x - 1.0f, this.y + 9.0f, this.x + 1.0f, this.y - 1.0f,
					0.01f, g, h, i, f));
		}
		return this.x;
	}
	
	public void drawAll(VertexConsumerProvider provider, Matrix4f matrix) {
		for(BiConsumer<VertexConsumerProvider, Matrix4f> run : glyphes) {
			run.accept(provider, matrix);
		}
		if (this.rectangles != null) {
			GlyphRenderer glyphRenderer = fontStorageAccessor.apply(Style.DEFAULT_FONT_ID)
					.getRectangleRenderer();
			VertexConsumer vertexConsumer = provider.getBuffer(glyphRenderer.getLayer(this.seeThrough));
			for (GlyphRenderer.Rectangle rectangle : this.rectangles) {
				glyphRenderer.drawRectangle(rectangle, matrix, vertexConsumer, this.light);
			}
		}
	}
	
	private void drawGlyph(GlyphRenderer glyphRenderer, boolean bold, boolean italic, float weight, float x, float y,
			Matrix4f matrix, VertexConsumer vertexConsumer, float red, float green, float blue, float alpha,
			int light) {
		glyphRenderer.draw(italic, x, y, matrix, vertexConsumer, red, green, blue, alpha, light);
		if (bold) {
			glyphRenderer.draw(italic, x + weight, y, matrix, vertexConsumer, red, green, blue, alpha, light);
		}
	}
	
}