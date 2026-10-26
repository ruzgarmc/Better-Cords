package dev.ruzgar.bettercoords;

import net.minecraft.entity.Entity;
import net.minecraft.util.math.Vec3d;

/**
 * Turns the real camera position into the position that should be shown on screen,
 * applying the fake-coordinate offset when that feature is on.
 */
public final class DisplayedCoords {
	private DisplayedCoords() {
	}

	public static Vec3d of(Entity camera) {
		BetterCoordsConfig cfg = BetterCoords.config();
		double x = camera.getX();
		double y = camera.getY();
		double z = camera.getZ();

		if (!cfg.fakeCoordsEnabled) {
			return new Vec3d(x, y, z);
		}

		// First position we see after fake coords were enabled / edited becomes the fake point.
		if (cfg.needsCalibration) {
			cfg.offsetX = cfg.fakeX - x;
			cfg.offsetY = cfg.fakeY - y;
			cfg.offsetZ = cfg.fakeZ - z;
			cfg.needsCalibration = false;
			cfg.save();
		}

		return new Vec3d(x + cfg.offsetX, y + cfg.offsetY, z + cfg.offsetZ);
	}
}
