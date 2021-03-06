package org.snowjak.rays.light.model;

import java.util.Optional;

import org.apache.commons.math3.util.FastMath;
import org.snowjak.rays.Ray;
import org.snowjak.rays.color.RawColor;
import org.snowjak.rays.intersect.Intersection;
import org.snowjak.rays.shape.Shape;
import org.snowjak.rays.world.World;

/**
 * A Decorator-type LightingModel that enhances another LightingModel with
 * rudimentary fog.
 * <p>
 * This effect is implemented as a fog-color that is mixed in with the decorated
 * LightingModel's color according to the following:
 * 
 * <pre>
 *   fog-fraction = 0.5 ^ (distance / half-distance)
 * </pre>
 * 
 * where {@code fog-fraction} = fraction of the final color that is the
 * fog-color, {@code distance} = the distance from the eye to the decorated
 * LightingModel's color, and {@code half-distance} = the distance at which the
 * fog is at half-strength.
 * </p>
 * 
 * @author snowjak88
 *
 */
public class FogDecoratingLightingModel implements LightingModel {

	private double halfFogDistance;

	private RawColor fogColor;

	private LightingModel decoratedModel;

	/**
	 * Construct a new FogDecoratingLightingModel.
	 * <p>
	 * For details on the algorithm used and the meaning of these paramters, see
	 * {@link FogDecoratingLightingModel}.
	 * </p>
	 * 
	 * @param halfFogDistance
	 * @param fogColor
	 * @param modelToDecorate
	 */
	public FogDecoratingLightingModel(double halfFogDistance, RawColor fogColor, LightingModel modelToDecorate) {
		this.halfFogDistance = halfFogDistance;
		this.fogColor = fogColor;
		this.decoratedModel = modelToDecorate;
	}

	@Override
	public Optional<RawColor> determineRayColor(Ray ray, Optional<Intersection<Shape>> intersection) {

		Optional<RawColor> decoratedLightingResult = decoratedModel.determineRayColor(ray, intersection);

		RawColor unfoggedColor = decoratedLightingResult.orElse(new RawColor());

		double colorDistance = 0d;
		if (!intersection.isPresent())
			colorDistance = World.FAR_AWAY;
		else
			colorDistance = intersection.get().getDistanceFromRayOrigin();

		double fogStrength = FastMath.pow(0.5, (colorDistance / halfFogDistance));
		RawColor foggedColor = unfoggedColor.multiplyScalar(fogStrength).add(fogColor.multiplyScalar(1d - fogStrength));

		return Optional.of(foggedColor);
	}

}
