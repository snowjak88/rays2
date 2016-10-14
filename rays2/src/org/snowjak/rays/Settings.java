package org.snowjak.rays;

import java.util.Optional;
import java.util.Properties;

import org.snowjak.rays.ui.AntialiasingScreenDecorator;
import org.snowjak.rays.ui.AntialiasingScreenDecorator.AA;
import org.snowjak.rays.ui.MultithreadedScreenDecorator.RenderSplitType;

/**
 * A central repository for raytracer settings
 * <p>
 * There are several "preset" methods available to quickly configure the
 * Settings to alternative configurations. See {@link #presetFast()},
 * {@link #presetDetailed()}.
 * </p>
 * <p>
 * By default, the Settings singleton will use the "fast" preset.
 * </p>
 * 
 * @author snowjak88
 *
 */
public class Settings {

	/**
	 * Specifies the allowed depth of ray recursion. Ray recursion is used to
	 * model, e.g., reflection.
	 */
	public static final int DEFAULT_MAX_RAY_RECURSION = 4;

	private int maxRayRecursion = DEFAULT_MAX_RAY_RECURSION;

	private int imageWidth, imageHeight;

	private AntialiasingScreenDecorator.AA antialiasing;

	private RenderSplitType renderSplitType;

	/**
	 * Defines the property name to associate with {@link #getImageWidth()}
	 */

	public static final String PROPERTY_IMAGE_WIDTH = "rays2.render.imageWidth";

	/**
	 * Defines the property name to associate with {@link #getImageHeight()}
	 */
	public static final String PROPERTY_IMAGE_HEIGHT = "rays2.render.imageHeight";

	/**
	 * Defines the property name to associate with {@link #getAntialiasing()}
	 */
	public static final String PROPERTY_ANTIALIASING = "rays2.render.antialias";

	/**
	 * Defines the property name to associate with {@link #getRenderSplitType()}
	 */
	public static final String PROPERTY_RENDER_SPLIT_TYPE = "rays2.render.renderSplitType";

	/**
	 * Defines the property name to associate with {@link #getMaxRayRecursion()}
	 */
	public static final String PROPERTY_MAX_RAY_RECURSION = "rays2.render.maxRayRecursion";

	/**
	 * Create a new {@link Settings} instance.
	 * 
	 * @param imageWidth
	 * @param imageHeight
	 * @param antialiasing
	 * @param renderSplitType
	 * @param maxRayRecursion
	 */
	public Settings(int imageWidth, int imageHeight, AntialiasingScreenDecorator.AA antialiasing,
			RenderSplitType renderSplitType, int maxRayRecursion) {
		this.imageWidth = imageWidth;
		this.imageHeight = imageHeight;
		this.antialiasing = antialiasing;
		this.renderSplitType = renderSplitType;
		this.maxRayRecursion = maxRayRecursion;
	}

	/**
	 * Copy an existing Settings instance into a new instance.
	 * 
	 * @param toCopy
	 */
	public Settings(Settings toCopy) {
		this.imageWidth = toCopy.imageWidth;
		this.imageHeight = toCopy.imageHeight;
		this.antialiasing = toCopy.antialiasing;
		this.renderSplitType = toCopy.renderSplitType;
		this.maxRayRecursion = toCopy.maxRayRecursion;
	}

	/**
	 * Configures the Settings singleton to use a "fast-render" preset:
	 * <ul>
	 * <li>Image width/height: 400x250</li>
	 * <li>Antialiasing: OFF</li>
	 * <li>Render-thread count: {@link #getDefaultRenderThreadCount()}</li>
	 * </ul>
	 * 
	 * @return the reconfigured Settings instance
	 */
	public static Settings presetFast() {

		return new Settings(400, 250, AA.OFF, RenderSplitType.REGION, Settings.DEFAULT_MAX_RAY_RECURSION);
	}

	/**
	 * Configures the Settings singleton to use a "detailed-render" preset:
	 * <ul>
	 * <li>Image width/height: 800x500</li>
	 * <li>Antialiasing: 8x</li>
	 * <li>Render-thread count: {@link #getDefaultRenderThreadCount()}</li>
	 * </ul>
	 * 
	 * @return the reconfigured Settings instance
	 */
	public static Settings presetDetailed() {

		return new Settings(800, 500, AA.x8, RenderSplitType.REGION, Settings.DEFAULT_MAX_RAY_RECURSION);
	}

	/**
	 * @return the width of the rendered image
	 */
	public int getImageWidth() {

		return imageWidth;
	}

	/**
	 * Set the width of the to-be-rendered image
	 * 
	 * @param imageWidth
	 */
	public void setImageWidth(int imageWidth) {

		this.imageWidth = imageWidth;
	}

	/**
	 * @return the height of the rendered image
	 */
	public int getImageHeight() {

		return imageHeight;
	}

	/**
	 * Set the height of the to-be-rendered image
	 * 
	 * @param imageHeight
	 */
	public void setImageHeight(int imageHeight) {

		this.imageHeight = imageHeight;
	}

	/**
	 * @return the selected antialiasing strength
	 */
	public AntialiasingScreenDecorator.AA getAntialiasing() {

		return antialiasing;
	}

	/**
	 * Set the desired antialiasing strength to use when rendering
	 * 
	 * @param antialiasing
	 */
	public void setAntialiasing(AntialiasingScreenDecorator.AA antialiasing) {

		this.antialiasing = antialiasing;
	}

	/**
	 * @return the selected {@link RenderSplitType} to be used when rendering
	 */
	public RenderSplitType getRenderSplitType() {

		return renderSplitType;
	}

	/**
	 * Set the desired {@link RenderSplitType} to be used when rendering
	 * 
	 * @param renderSplitType
	 */
	public void setRenderSplitType(RenderSplitType renderSplitType) {

		this.renderSplitType = renderSplitType;
	}

	/**
	 * Specifies the allowed depth of ray recursion. Ray recursion is used to
	 * model, e.g., reflection.
	 * 
	 * @return allowed depth of ray recursion
	 */
	public int getMaxRayRecursion() {

		return maxRayRecursion;
	}

	/**
	 * Specifies the allowed depth of ray recursion. Ray recursion is used to
	 * model, e.g., reflection.
	 * 
	 * @param maxRayRecursion
	 */
	public void setMaxRayRecursion(int maxRayRecursion) {

		this.maxRayRecursion = maxRayRecursion;
	}

	/**
	 * @return a {@link Properties} instance containing this {@link Settings}'
	 *         encoded values
	 */
	public Properties saveToProperties() {

		Properties prop = new Properties();

		prop.setProperty(PROPERTY_IMAGE_WIDTH, Integer.toString(getImageWidth()));
		prop.setProperty(PROPERTY_IMAGE_HEIGHT, Integer.toString(getImageHeight()));
		prop.setProperty(PROPERTY_ANTIALIASING, AA.toString(getAntialiasing()));
		prop.setProperty(PROPERTY_RENDER_SPLIT_TYPE, RenderSplitType.toString(getRenderSplitType()));
		prop.setProperty(PROPERTY_MAX_RAY_RECURSION, Integer.toString(getMaxRayRecursion()));

		return prop;
	}

	/**
	 * Create a new {@link Settings} instance using the given {@link Properties}
	 * and another Settings (representing the default values to use).
	 * 
	 * @param properties
	 * @param defaults
	 * @return a new Settings instance
	 */
	public static Settings fromProperties(Properties properties, Settings defaults) {

		Settings newSettings = new Settings(defaults);

		newSettings.setImageWidth(
				parsePropertyAsInt(properties, PROPERTY_IMAGE_WIDTH).orElse(newSettings.getImageWidth()));

		newSettings.setImageHeight(
				parsePropertyAsInt(properties, PROPERTY_IMAGE_HEIGHT).orElse(newSettings.getImageHeight()));

		newSettings.setMaxRayRecursion(
				parsePropertyAsInt(properties, PROPERTY_MAX_RAY_RECURSION).orElse(newSettings.getMaxRayRecursion()));

		String antialias = properties.getProperty(PROPERTY_ANTIALIASING);
		if (antialias != null)
			newSettings.setAntialiasing(AA.fromString(antialias));

		String renderSplitType = properties.getProperty(PROPERTY_RENDER_SPLIT_TYPE);
		if (renderSplitType != null)
			newSettings.setRenderSplitType(RenderSplitType.fromString(renderSplitType));

		return newSettings;
	}

	private static Optional<Integer> parsePropertyAsInt(Properties properties, String propertyName) {

		try {
			if (properties.getProperty(propertyName) != null)
				return Optional.of(Integer.parseInt(properties.getProperty(propertyName)));
		} catch (NumberFormatException e) {
			System.err.println("Could not parse " + propertyName + ": " + e.getMessage());
		}

		return Optional.empty();
	}

}