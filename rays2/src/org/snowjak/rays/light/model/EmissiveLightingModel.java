package org.snowjak.rays.light.model;

import java.util.Optional;

import org.snowjak.rays.Ray;
import org.snowjak.rays.color.RawColor;
import org.snowjak.rays.intersect.Intersection;
import org.snowjak.rays.shape.Shape;

/**
 * A LightingModel that simply returns the intersected {@link Shape}'s emissive
 * radiance, if it has any.
 * 
 * @author snowjak88
 *
 */
public class EmissiveLightingModel implements LightingModel {

	@Override
	public Optional<RawColor> determineRayColor(Ray ray, Optional<Intersection<Shape>> intersection) {

		if (!intersection.isPresent())
			return Optional.empty();

		Shape intersected = intersection.get().getIntersected();
		if (!intersected.isEmissive())
			return Optional.empty();

		RawColor visibleRadiance = intersection.get().getEmissive(intersection.get().getPoint()).orElse(new RawColor());

		return Optional.of(visibleRadiance);
	}

}
