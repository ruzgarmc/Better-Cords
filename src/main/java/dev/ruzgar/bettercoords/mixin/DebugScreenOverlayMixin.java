package dev.ruzgar.bettercoords.mixin;

import dev.ruzgar.bettercoords.BetterCoords;
import dev.ruzgar.bettercoords.CoordsHudRenderer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.DebugScreenOverlay;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * While the mod is on, replaces the whole F3 overlay (all text entries, charts, pie chart)
 * with a single XYZ line. While off, vanilla renders untouched.
 */
@Mixin(DebugScreenOverlay.class)
public abstract class DebugScreenOverlayMixin {
	@Shadow
	@Final
	private Minecraft minecraft;

	@Shadow
	@Final
	private Font font;

	@Inject(method = "extractRenderState", at = @At("HEAD"), cancellable = true)
	private void bettercoords$renderCoordsOnly(GuiGraphicsExtractor graphics, CallbackInfo ci) {
		if (!BetterCoords.config().enabled) {
			return;
		}
		ci.cancel();

		// Same early-outs as vanilla: not loaded yet, or HUD hidden with F1 and no screen open.
		if (!this.minecraft.isGameLoadFinished()) {
			return;
		}
		if (this.minecraft.gui.hud.isHidden() && this.minecraft.gui.screen() == null) {
			return;
		}
		if (!this.minecraft.debugEntries.isOverlayVisible()) {
			return;
		}

		Entity camera = this.minecraft.getCameraEntity();
		if (camera == null || this.minecraft.level == null) {
			return;
		}

		graphics.nextStratum();
		CoordsHudRenderer.render(graphics, this.font, camera);
	}
}
