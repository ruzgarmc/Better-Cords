package dev.ruzgar.bettercoords;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.world.entity.Entity;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;

import java.util.Locale;

/**
 * Draws the "XYZ: x y z" line, and optionally the vanilla-style "Facing:" line under it, in the
 * top-left corner. Styled like the vanilla F3 text: same background box colour, text colour,
 * position and line height.
 */
public final class CoordsHudRenderer {
	private static final int X = 2;
	private static final int Y = 2;
	private static final int LINE_HEIGHT = 9;
	private static final int BACKGROUND = 0x90505050;
	private static final int TEXT_COLOR = 0xFFE0E0E0;

	// One per line, so the two lines churn independently.
	private static final Line COORDS = new Line();
	private static final Line FACING = new Line();

	private CoordsHudRenderer() {
	}

	public static void render(GuiGraphicsExtractor context, Font textRenderer, Entity camera) {
		BetterCoordsConfig cfg = BetterCoords.config();
		Vec3 pos = DisplayedCoords.of(camera);

		String coords = String.format(Locale.ROOT, "%.3f  %.5f  %.3f", pos.x, pos.y, pos.z);
		COORDS.draw(context, textRenderer, "XYZ: ", coords, 0, cfg.glitchEnabled);

		if (cfg.showFacing) {
			FACING.draw(context, textRenderer, "Facing: ", facingText(camera), 1, cfg.glitchEnabled);
		}
	}

	/** Same wording and rounding as the vanilla facing line. */
	private static String facingText(Entity camera) {
		Direction direction = camera.getDirection();
		String axis = switch (direction) {
			case NORTH -> "Towards negative Z";
			case SOUTH -> "Towards positive Z";
			case WEST -> "Towards negative X";
			case EAST -> "Towards positive X";
			default -> "Invalid";
		};
		return String.format(Locale.ROOT, "%s (%s) (%.1f / %.1f)",
				direction.getSerializedName(),
				axis,
				Mth.wrapDegrees(camera.getYRot()),
				Mth.wrapDegrees(camera.getXRot()));
	}

	/** One rendered line: its glitch state plus the width its background box is currently drawn at. */
	private static final class Line {
		/** How long the box holds a width before it is allowed to shrink again. */
		private static final long BOX_HOLD_MS = 600L;

		private final GlitchText glitch = new GlitchText();
		private int boxWidth;
		private int windowMax;
		private long windowStartedAt;

		void draw(GuiGraphicsExtractor context, Font textRenderer, String prefix, String value, int index, boolean glitched) {
			String line = prefix + (glitched ? this.glitch.scramble(value) : value);
			int textWidth = textRenderer.width(line);

			// Glitch glyphs are not all the same width, so the raw text width changes every time a
			// character is re-rolled. Letting the box follow that makes its right edge flicker, so
			// while glitching it is drawn at the widest the text has been recently instead.
			int width = glitched ? this.widestRecently(textWidth) : textWidth;

			int y = Y + index * LINE_HEIGHT;
			context.fill(X - 1, y - 1, X + width + 1, y + LINE_HEIGHT - 1, BACKGROUND);
			context.text(textRenderer, line, X, y, TEXT_COLOR, false);
		}

		private int widestRecently(int target) {
			long now = System.currentTimeMillis();
			this.windowMax = Math.max(this.windowMax, target);
			if (now - this.windowStartedAt > BOX_HOLD_MS) {
				this.boxWidth = this.windowMax;
				this.windowMax = target;
				this.windowStartedAt = now;
			}
			return Math.max(this.boxWidth, target);
		}
	}
}
