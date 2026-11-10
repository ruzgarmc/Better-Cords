package dev.ruzgar.bettercoords.mixin;

import com.mojang.blaze3d.platform.InputConstants;
import dev.ruzgar.bettercoords.Keybinds;
import net.minecraft.client.KeyboardHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.client.input.KeyEvent;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Watches raw key presses for the mod's own toggles. The press is never consumed, so a key that is
 * also bound to something in vanilla still does its normal job.
 */
@Mixin(KeyboardHandler.class)
public abstract class KeyboardHandlerMixin {
	@Shadow
	@Final
	private Minecraft minecraft;

	@Inject(method = "keyPress", at = @At("HEAD"))
	private void bettercoords$handleToggleKeys(long window, int action, KeyEvent event, CallbackInfo ci) {
		if (action != InputConstants.PRESS) {
			return;
		}
		// Not while a screen is open, so binding a key or typing in chat never toggles anything.
		if (this.minecraft.gui.screen() != null || this.minecraft.level == null) {
			return;
		}
		Keybinds.onKeyPressed(this.minecraft, event.key());
	}
}
