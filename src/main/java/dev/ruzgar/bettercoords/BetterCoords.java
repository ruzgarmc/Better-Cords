package dev.ruzgar.bettercoords;

import net.fabricmc.api.ClientModInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BetterCoords implements ClientModInitializer {
	public static final String MOD_ID = "bettercoords";
	public static final Logger LOGGER = LoggerFactory.getLogger("Better Coords");

	private static BetterCoordsConfig config = new BetterCoordsConfig();

	@Override
	public void onInitializeClient() {
		config = BetterCoordsConfig.load();
		LOGGER.info("Better Coords loaded (mod {}, fake coords {})",
				config.enabled ? "on" : "off",
				config.fakeCoordsEnabled ? "on" : "off");
	}

	public static BetterCoordsConfig config() {
		return config;
	}

	public static void saveConfig() {
		config.save();
	}
}
