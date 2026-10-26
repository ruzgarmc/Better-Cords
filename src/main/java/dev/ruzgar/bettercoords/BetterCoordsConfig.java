package dev.ruzgar.bettercoords;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.fabricmc.loader.api.FabricLoader;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Persisted at {@code config/bettercoords.json}.
 */
public class BetterCoordsConfig {
	private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
	private static final Path FILE = FabricLoader.getInstance().getConfigDir().resolve(BetterCoords.MOD_ID + ".json");

	/** Mod on: F3 shows only XYZ. Mod off: vanilla F3. */
	public boolean enabled = true;

	/** When on, every character of the coordinate numbers is replaced with constantly changing junk. */
	public boolean glitchEnabled = false;

	/** When on, a vanilla-style "Facing:" line is drawn under the coordinates. */
	public boolean showFacing = false;

	/** When on, the position you were at when it was enabled is displayed as {@code fakeX/Y/Z}. */
	public boolean fakeCoordsEnabled = false;
	public double fakeX = 0.0;
	public double fakeY = 64.0;
	public double fakeZ = 0.0;

	/**
	 * Offset added to the real position to get the displayed one. Computed as
	 * {@code fake - real} the first time a position is available after {@link #needsCalibration} is set.
	 */
	public double offsetX = 0.0;
	public double offsetY = 0.0;
	public double offsetZ = 0.0;
	public boolean needsCalibration = true;

	public static BetterCoordsConfig load() {
		if (Files.exists(FILE)) {
			try {
				String json = Files.readString(FILE, StandardCharsets.UTF_8);
				BetterCoordsConfig loaded = GSON.fromJson(json, BetterCoordsConfig.class);
				if (loaded != null) {
					return loaded;
				}
			} catch (Exception e) {
				BetterCoords.LOGGER.warn("Could not read {}, using defaults", FILE, e);
			}
		}
		BetterCoordsConfig fresh = new BetterCoordsConfig();
		fresh.save();
		return fresh;
	}

	public void save() {
		try {
			Files.createDirectories(FILE.getParent());
			Files.writeString(FILE, GSON.toJson(this), StandardCharsets.UTF_8);
		} catch (IOException e) {
			BetterCoords.LOGGER.error("Could not save {}", FILE, e);
		}
	}
}
