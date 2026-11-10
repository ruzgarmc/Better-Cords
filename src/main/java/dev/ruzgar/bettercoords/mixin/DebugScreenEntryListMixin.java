package dev.ruzgar.bettercoords.mixin;

import dev.ruzgar.bettercoords.BetterCoords;
import net.minecraft.client.gui.components.debug.DebugScreenEntries;
import net.minecraft.client.gui.components.debug.DebugScreenEntryList;
import net.minecraft.client.gui.components.debug.DebugScreenEntryStatus;
import net.minecraft.resources.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * F3 normally swaps the crosshair for the 3D axis gizmo. With the mod on, keep the normal crosshair
 * (unless the player explicitly pinned the gizmo to "always on" in the debug options).
 */
@Mixin(DebugScreenEntryList.class)
public abstract class DebugScreenEntryListMixin {
	@Shadow
	public abstract DebugScreenEntryStatus getStatus(Identifier id);

	@Inject(method = "isCurrentlyEnabled", at = @At("HEAD"), cancellable = true)
	private void bettercoords$hideDebugCrosshair(Identifier id, CallbackInfoReturnable<Boolean> cir) {
		if (!BetterCoords.config().enabled) {
			return;
		}
		if (DebugScreenEntries.THREE_DIMENSIONAL_CROSSHAIR.equals(id)
				&& this.getStatus(id) != DebugScreenEntryStatus.ALWAYS_ON) {
			cir.setReturnValue(false);
		}
	}
}
