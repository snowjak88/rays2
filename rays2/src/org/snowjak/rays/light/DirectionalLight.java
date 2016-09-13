package org.snowjak.rays.light;

import java.util.function.Function;

import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;
import org.snowjak.rays.Ray;
import org.snowjak.rays.World;
import org.snowjak.rays.color.RawColor;
import org.snowjak.rays.function.Functions;
import org.snowjak.rays.transform.Transformer;

/**
 * Implements a simple directional light -- a {@link Light} which shines its
 * light-rays into the world at the same angle. A directional light is a simple
 * method of simulating a light-source that is extremely far away -- so far that
 * the received light-rays are practically parallel.
 * <p>
 * One consequence of this is that a DirectionalLight's position is immaterial:
 * it gives the same light to the entire world regardless of any
 * {@link Transformer}s applied to it.
 * </p>
 * 
 * @author snowjak88
 *
 */
public final class DirectionalLight extends Light {

	private Vector3D direction;

	/**
	 * Create a new {@link DirectionalLight}, with its light-rays pointing in
	 * the given direction (expressed as a vector in global coordinates).
	 * 
	 * @param direction
	 * @param ambientIntensityFunction
	 * @param diffuseIntensityFunction
	 * @param specularIntensityFunction
	 * @param intensityFunction
	 */
	public DirectionalLight(Vector3D direction, Function<Ray, RawColor> ambientIntensityFunction,
			Function<Ray, RawColor> diffuseIntensityFunction, Function<Ray, RawColor> specularIntensityFunction) {
		super(ambientIntensityFunction, diffuseIntensityFunction, specularIntensityFunction,
				(l, i) -> l.getLocation().subtract(i.getPoint()).normalize().dotProduct(i.getNormal()),
				Functions.constant(1d));
		this.direction = direction.normalize();
	}

	/**
	 * Create a new {@link DirectionalLight}, with its light-rays pointing in
	 * the given direction (expressed as a vector in global coordinates).
	 * 
	 * @param direction
	 * @param ambientIntensity
	 * @param diffuseIntensity
	 * @param specularIntensity
	 * @param intensity
	 */
	public DirectionalLight(Vector3D direction, RawColor ambientIntensity, RawColor diffuseIntensity,
			RawColor specularIntensity, double intensity) {
		this(direction, CONSTANT_COLOR(ambientIntensity), CONSTANT_COLOR(diffuseIntensity),
				CONSTANT_COLOR(specularIntensity));
	}

	@Override
	public Vector3D getLocation() {

		return direction.negate().normalize().scalarMultiply(World.WORLD_BOUND);
	}

}
