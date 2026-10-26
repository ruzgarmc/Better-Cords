package dev.ruzgar.bettercoords;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.input.KeyInput;
import net.minecraft.client.util.InputUtil;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

/**
 * The two in-game toggles. Keys are stored as GLFW codes in the config and bound from the mod's own
 * settings screen, so the mod stays independent of Fabric API.
 */
public final class Keybinds {
	public static final int UNBOUND = -1;

	private Keybinds() {
	}

	/** Called for every key press that reaches the game with no screen open. */
	public static void onKeyPressed(MinecraftClient client, int keyCode) {
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

	private static void announce(MinecraftClient client, String translationKey, boolean on) {
		Text state = Text.translatable(on ? "bettercoords.message.on" : "bettercoords.message.off")
				.formatted(on ? Formatting.GREEN : Formatting.RED);
		client.inGameHud.setOverlayMessage(Text.translatable(translationKey, state), false);
	}

	/** The key's name as the game writes it, or "None" when nothing is bound. */
	public static Text displayName(int keyCode) {
		if (keyCode == UNBOUND) {
			return Text.translatable("bettercoords.config.keys.unbound");
		}
		return InputUtil.fromKeyCode(new KeyInput(keyCode, 0, 0)).getLocalizedText();
	}
}
