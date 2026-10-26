package dev.ruzgar.bettercoords.mixin;

import dev.ruzgar.bettercoords.Keybinds;
import net.minecraft.client.Keyboard;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.input.KeyInput;
import net.minecraft.client.util.InputUtil;
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
@Mixin(Keyboard.class)
public abstract class KeyboardMixin {
	@Shadow
	@Final
	private MinecraftClient client;

	@Inject(method = "onKey", at = @At("HEAD"))
	private void bettercoords$handleToggleKeys(long window, int action, KeyInput input, CallbackInfo ci) {
		if (action != InputUtil.GLFW_PRESS) {
			return;
		}
		// Not while a screen is open, so binding a key or typing in chat never toggles anything.
		if (this.client.currentScreen != null || this.client.world == null) {
			return;
		}
		Keybinds.onKeyPressed(this.client, input.key());
	}
}
