package org.snowjak.rays.ui;

import java.util.Optional;

import org.snowjak.rays.World;
import org.snowjak.rays.camera.Camera;
import org.snowjak.rays.color.RawColor;

import javafx.scene.paint.Color;

/**
 * Represents the raytracer's output at the UI-level. Interfaces with a
 * {@link Camera} to render the current {@link World}.
 * 
 * @author rr247200
 *
 */
public abstract class Screen implements DrawsEntireScreen, DrawsScreenPixel {

	private int screenMinX, screenMinY, screenMaxX, screenMaxY;

	/**
	 * Create a new Screen with the given extent.
	 * 
	 * @param screenMaxX
	 * @param screenMaxY
	 */
	public Screen(int screenMaxX, int screenMaxY) {
		this(0, 0, screenMaxX, screenMaxY);
	}

	/**
	 * Create a new Screen with the given extent.
	 * 
	 * @param screenMinX
	 * @param screenMinY
	 * @param screenMaxX
	 * @param screemMaxY
	 * @param screenMaxY
	 */
	public Screen(int screenMinX, int screenMinY, int screenMaxX, int screemMaxY) {
		this.screenMinX = screenMinX;
		this.screenMinY = screenMinY;
		this.screenMaxX = screenMaxX;
		this.screenMaxY = screemMaxY;
	}

	/**
	 * Draw (or redraw) the entire screen. Every pixel in (screenMinX,
	 * screenMinY) - (screenMaxX, screenMaxY) is iterated across; the Camera is
	 * queried for each, and {@link #drawPixel(int, int, Color)} executed.
	 */
	@Override
	public void draw(Camera camera) {

		if (camera != null)
			for (int x = screenMinX; x <= screenMaxX; x++)
				for (int y = screenMinY; y <= screenMaxY; y++) {
					Optional<RawColor> color = getRayColor(x, y, camera);
					if (color.isPresent())
						drawPixel(x, screenMaxY - y + screenMinY, color.get());
				}

	}

	/**
	 * @param screenX
	 * @param screenY
	 * @return the computed RawColor, if present, for the corresponding screen
	 *         location
	 */
	@Override
	public Optional<RawColor> getRayColor(int screenX, int screenY, Camera camera) {

		return camera.shootRay(getCameraX(screenX, camera), getCameraY(screenY, camera));
	}

	/**
	 * Draw the given Color to the Screen at the given location.
	 * 
	 * @param x
	 * @param y
	 * @param color
	 */
	public abstract void drawPixel(int x, int y, RawColor color);

	/**
	 * A method-stub, to be called when the application should shut down. Useful
	 * as a hook to shut down any rendering-related tasks -- e.g., killing
	 * render-worker threads.
	 */
	@Override
	public void shutdown() {

	}

	/**
	 * @return the screen's minimum X-coordinate
	 */
	@Override
	public int getScreenMinX() {

		return screenMinX;
	}

	/**
	 * @return the screen's minimum Y-coordinate
	 */
	@Override
	public int getScreenMinY() {

		return screenMinY;
	}

	/**
	 * @return the screen's maximum X-coordinate
	 */
	@Override
	public int getScreenMaxX() {

		return screenMaxX;
	}

	/**
	 * @return the screen's maximum Y-coordinate
	 */
	@Override
	public int getScreenMaxY() {

		return screenMaxY;
	}

}
