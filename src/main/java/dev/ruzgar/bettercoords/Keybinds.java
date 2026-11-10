package dev.ruzgar.bettercoords;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;

/**
 * The two in-game toggles. Keys are stored as {@link InputConstants} key codes in the config and bound
 * from the mod's own settings screen, so the mod stays independent of Fabric API.
 */
public final class Keybinds {
	public static final int UNBOUND = -1;

	private Keybinds() {
	}

	/** Called for every key press that reaches the game with no screen open. */
	public static void onKeyPressed(Minecraft client, int keyCode) {
		if (keyCode == UNBOUND) {
			return;
		}
		BetterCoordsConfig cfg = BetterCoords.config();

		if (keyCode == cfg.toggleModKey) {
			cfg.enabled = !cfg.enabled;
			cfg.save();
			announce(client, "bettercoords.message.mod", cfg.enabled);
		}

		if (keyCode == cfg.toggleFakeCoordsKey) {
			cfg.fakeCoordsEnabled = !cfg.fakeCoordsEnabled;
			// Turning it on here anchors the fake point to wherever the player is standing.
			if (cfg.fakeCoordsEnabled) {
				cfg.needsCalibration = true;
			}
			cfg.save();
			announce(client, "bettercoords.message.fake", cfg.fakeCoordsEnabled);
		}
	}

	private static void announce(Minecraft client, String translationKey, boolean on) {
		Component state = Component.translatable(on ? "bettercoords.message.on" : "bettercoords.message.off")
				.withStyle(on ? ChatFormatting.GREEN : ChatFormatting.RED);
		client.gui.hud.setOverlayMessage(Component.translatable(translationKey, state), false);
	}

	/** The key's name as the game writes it, or "None" when nothing is bound. */
	public static Component displayName(int keyCode) {
		if (keyCode == UNBOUND) {
			return Component.translatable("bettercoords.config.keys.unbound");
		}
		return InputConstants.Type.KEYBOARD.getOrCreate(keyCode).getDisplayName();
	}

	/**
	 * Configs written by 1.x hold GLFW key codes; 26.3 uses SDL scancodes. Letters, digits and F1-F12
	 * are translated, anything else is unbound so it can be set again from the settings screen.
	 */
	static int fromGlfw(int glfw) {
		if (glfw == UNBOUND) {
			return UNBOUND;
		}
		if (glfw >= 65 && glfw <= 90) {
			return InputConstants.KEY_A + (glfw - 65);
		}
		if (glfw >= 49 && glfw <= 57) {
			return InputConstants.KEY_1 + (glfw - 49);
		}
		if (glfw == 48) {
			return InputConstants.KEY_0;
		}
		if (glfw >= 290 && glfw <= 301) {
			return InputConstants.KEY_F1 + (glfw - 290);
		}
		return UNBOUND;
	}
}
