package dev.ruzgar.bettercoords.mixin;

import dev.ruzgar.bettercoords.BetterCoords;
import net.minecraft.client.gui.hud.debug.DebugHudEntries;
import net.minecraft.client.gui.hud.debug.DebugHudEntryVisibility;
import net.minecraft.client.gui.hud.debug.DebugHudProfile;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * F3 normally swaps the crosshair for the 3D axis gizmo. With the mod on, keep the normal crosshair
 * (unless the player explicitly pinned the gizmo to "always on" in the debug options).
 */
@Mixin(DebugHudProfile.class)
public abstract class DebugHudProfileMixin {
	@Shadow
	public abstract DebugHudEntryVisibility getVisibility(Identifier id);

	@Inject(method = "isEntryVisible", at = @At("HEAD"), cancellable = true)
	private void bettercoords$hideDebugCrosshair(Identifier id, CallbackInfoReturnable<Boolean> cir) {
		if (!BetterCoords.config().enabled) {
			return;
		}
		if (DebugHudEntries.THREE_DIMENSIONAL_CROSSHAIR.equals(id)
				&& this.getVisibility(id) != DebugHudEntryVisibility.ALWAYS_ON) {
			cir.setReturnValue(false);
		}
	}
}
