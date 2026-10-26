package dev.ruzgar.bettercoords.mixin;

import dev.ruzgar.bettercoords.BetterCoords;
import dev.ruzgar.bettercoords.CoordsHudRenderer;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.hud.DebugHud;
import net.minecraft.entity.Entity;
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
@Mixin(DebugHud.class)
public abstract class DebugHudMixin {
	@Shadow
	@Final
	private MinecraftClient client;

	@Shadow
	@Final
	private TextRenderer textRenderer;

	@Inject(method = "render", at = @At("HEAD"), cancellable = true)
	private void bettercoords$renderCoordsOnly(DrawContext context, CallbackInfo ci) {
		if (!BetterCoords.config().enabled) {
			return;
		}
		ci.cancel();

		// Same early-outs as vanilla: not loaded yet, or HUD hidden with F1 and no screen open.
		if (!this.client.isFinishedLoading()) {
			return;
		}
		if (this.client.options.hudHidden && this.client.currentScreen == null) {
			return;
		}
		if (!this.client.debugHudEntryList.isF3Enabled()) {
			return;
		}

		Entity camera = this.client.getCameraEntity();
		if (camera == null || this.client.world == null) {
			return;
		}

		context.createNewRootLayer();
		CoordsHudRenderer.render(context, this.textRenderer, camera);
	}
}
