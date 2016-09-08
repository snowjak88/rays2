package org.snowjak.rays.ui;

import java.util.Optional;

import org.apache.commons.math3.distribution.NormalDistribution;
import org.apache.commons.math3.distribution.RealDistribution;
import org.apache.commons.math3.util.FastMath;
import org.snowjak.rays.Renderer.Settings;
import org.snowjak.rays.camera.Camera;
import org.snowjak.rays.color.RawColor;

/**
 * Implements a simple antialiasing filter on top of an existing
 * {@link PixelDrawer} implementation.
 * <p>
 * This Decorator implements a simple box filter. It samples a variety of Rays
 * +/- the central Ray, and combines all the resulting colors into a single
 * color.
 * </p>
 * <p>
 * These colors are combined using the {@code sinc} function:
 * 
 * <pre>
 *    factor = [sin(x) / (x)] * [sin(y) / (y)]
 * </pre>
 * </p>
 * 
 * @author snowjak88
 *
 */
public class AntialiasingScreenDecorator implements PixelDrawer {

	private AA aaSetting;

	private RealDistribution distribution;

	private PixelDrawer child;

	private double coordinateDelta;

	private double filterSpan;

	/**
	 * Create a new AntialiasingScreenDecorator on top of an existing
	 * {@link PixelDrawer} instance.
	 * 
	 * @param decoratedScreen
	 *            the existing screen to decorate
	 */
	public AntialiasingScreenDecorator(PixelDrawer decoratedScreen) {

		this.child = decoratedScreen;
		this.filterSpan = 1;
		this.aaSetting = Settings.getSingleton().getAntialiasing();
		if (aaSetting != AA.OFF)
			this.coordinateDelta = filterSpan / ((double) (aaSetting.sampleCount / 2));

		this.distribution = new NormalDistribution(0d, 0.5);
	}

	@Override
	public Optional<RawColor> getRayColor(int screenX, int screenY, Camera camera) {

		if (aaSetting == AA.OFF)
			return child.getRayColor(screenX, screenY, camera);

		RawColor totalColor = new RawColor();
		double totalScale = 0d;

		for (double dx = -(filterSpan / 2d); dx <= (filterSpan / 2d); dx += coordinateDelta) {
			for (double dy = -(filterSpan / 2d); dy <= (filterSpan / 2d); dy += coordinateDelta) {

				double x = getCameraX(screenX + dx, camera), y = getCameraY(screenY + dy, camera);
				double scale = distribution.density(FastMath.sqrt(FastMath.pow(dx, 2d) + FastMath.pow(dy, 2d)));

				totalScale += scale;
				Optional<RawColor> color = camera.shootRay(x, y);
				if (color.isPresent())
					totalColor = totalColor.add(color.get().multiplyScalar(scale));
			}
		}

		totalColor = totalColor.multiplyScalar(1d / totalScale);
		return Optional.of(totalColor);
	}

	@Override
	public void drawPixel(int x, int y, RawColor color) {

		child.drawPixel(x, y, color);
	}

	@Override
	public void shutdown() {

		child.shutdown();
	}

	@Override
	public int getScreenMinX() {

		return child.getScreenMinX();
	}

	@Override
	public int getScreenMinY() {

		return child.getScreenMinY();
	}

	@Override
	public int getScreenMaxX() {

		return child.getScreenMaxX();
	}

	@Override
	public int getScreenMaxY() {

		return child.getScreenMaxY();
	}

	/**
	 * Defines the number of samples to use when anti-aliasing.
	 * 
	 * @author snowjak88
	 *
	 */
	@SuppressWarnings("javadoc")
	public static enum AA {
		/**
		 * Turn off antialiasing completely.
		 */
		OFF(-1), x2(3), x4(5), x8(9), x16(17), x32(33);

		private int sampleCount;

		AA(int sampleCount) {
			this.sampleCount = sampleCount;
		}

	}

}
