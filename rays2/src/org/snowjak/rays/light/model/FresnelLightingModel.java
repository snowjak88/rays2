package org.snowjak.rays.light.model;

import static org.apache.commons.math3.util.FastMath.cos;
import static org.apache.commons.math3.util.FastMath.pow;
import static org.apache.commons.math3.util.FastMath.sqrt;

import java.time.Instant;
import java.util.Optional;

import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;
import org.snowjak.rays.Ray;
import org.snowjak.rays.RaytracerContext;
import org.snowjak.rays.Renderer;
import org.snowjak.rays.color.RawColor;
import org.snowjak.rays.function.Functions;
import org.snowjak.rays.intersect.Intersection;
import org.snowjak.rays.shape.Shape;
import org.snowjak.rays.util.ExecutionTimeTracker;
import org.snowjak.rays.world.World;

/**
 * A {@link LightingModel} that implements the Schlick approximation to the
 * Fresnel equations for reflection and refraction for the interface between two
 * materials of differing indices of refraction.
 * <p>
 * The Fresnel equations model the relative proportion of reflected vs.
 * refracted light between two materials of differing indices of refraction. The
 * Schlick approximation simplifies these equations by ignoring the change in
 * phase-angle that reflected and refracted light undergoes.
 * </p>
 * <p>
 * This LightingModel does not model sub-surface scattering, nor does it do an
 * especially rigorous job of handling surface colors. It ignores the effect of
 * intervening materials (i.e., the Material sitting between the ray's origin
 * and the first intersection), and relies on a
 * {@link PhongSpecularLightingModel} to derive the lighting at a point on an
 * object's surface.
 * </p>
 * 
 * @author snowjak88
 *
 */
public class FresnelLightingModel implements LightingModel {

	private LightingModel surfaceLightingModel = null;

	/**
	 * Construct a new {@link FresnelLightingModel}, using the specified
	 * {@link LightingModel} to compute the radiance of encountered surfaces.
	 * 
	 * @param surfaceLightingModel
	 */
	public FresnelLightingModel(LightingModel surfaceLightingModel) {
		this.surfaceLightingModel = surfaceLightingModel;
	}

	@Override
	public Optional<RawColor> determineRayColor(Ray ray, Optional<Intersection<Shape>> intersection) {

		Instant start = Instant.now();

		if (!intersection.isPresent())
			return Optional.empty();

		World world = RaytracerContext.getSingleton().getCurrentWorld();
		Renderer renderer = RaytracerContext.getSingleton().getCurrentRenderer();

		if (ray.getRecursiveLevel() > RaytracerContext.getSingleton().getSettings().getMaxRayRecursion())
			return surfaceLightingModel.determineRayColor(ray, intersection);

		//
		//
		//
		Intersection<Shape> intersect = intersection.get();
		FresnelResult fresnel = new FresnelResult(intersect);
		double reflectance = fresnel.getReflectance();
		double transmittance = fresnel.getTransmittance();
		ExecutionTimeTracker.logExecutionRecord("FresnelLightingModel - compute Fresnel terms", start, Instant.now(), null);
		start = Instant.now();

		//
		//
		// Now shoot some rays!
		RawColor finalColor = new RawColor();

		//
		//
		//
		RawColor surfaceColor = surfaceLightingModel.determineRayColor(ray, intersection).orElse(new RawColor());
		
		ExecutionTimeTracker.logExecutionRecord("FresnelLightingModel - get child LightingModel color", start, Instant.now(), null);
		start = Instant.now();

		//
		//
		//
		RawColor reflectedColor = new RawColor(), refractedColor = new RawColor();
		double surfaceTransparency = intersect.getEnteringMaterial().getSurfaceTransparency(intersect.getPoint());

		if (reflectance > 0d) {
			Optional<Intersection<Shape>> reflectedIntersection = world
					.getClosestShapeIntersection(fresnel.getReflectedRay());
			reflectedColor = renderer.getLightingModel()
					.determineRayColor(fresnel.getReflectedRay(), reflectedIntersection)
					.orElse(new RawColor());
		}
		if (transmittance > 0d) {
			//
			// Get the color of the refracted ray.
			Optional<Intersection<Shape>> refractedIntersection = world
					.getClosestShapeIntersection(fresnel.getRefractedRay());
			refractedColor = renderer.getLightingModel()
					.determineRayColor(fresnel.getRefractedRay(), refractedIntersection)
					.orElse(new RawColor());

			//
			//
			// The refracted color is to be mixed with the surface
			// color, insofar as the surface is transparent

			RawColor finalRefractedColor = Functions.lerp(surfaceColor, refractedColor, surfaceTransparency);
			refractedColor = finalRefractedColor;
		}

		finalColor = Functions.lerp(reflectedColor, refractedColor, transmittance);

		return Optional.of(finalColor);
	}

	/**
	 * Calculates the results of a Fresnel interaction (using the Schlick
	 * approximation): the results for Fresnel reflectance and transmittance,
	 * plus a reflected and a refracted Ray
	 * 
	 * @author snowjak88
	 *
	 */
	@SuppressWarnings("javadoc")
	public static class FresnelResult {

		public FresnelResult(Intersection<Shape> intersection) {
			Ray ray = intersection.getRay();
			Vector3D point = intersection.getPoint();
			Vector3D i = intersection.getRay().getVector();
			Vector3D n = intersection.getNormal();
			double theta_i = Vector3D.angle(i.negate(), n);

			//
			//
			//
			double n1 = intersection.getLeavingMaterial().getRefractiveIndex(point),
					n2 = intersection.getEnteringMaterial().getRefractiveIndex(point);

			//
			//
			// Determine reflected ray
			Vector3D reflectedVector = getTangentPart(i, n).subtract(getNormalPart(i, n));
			reflectedRay = new Ray(point, reflectedVector, ray.getRecursiveLevel() + 1);

			//
			//
			// Determine refracted ray
			double sin2_theta_t = pow(n1 / n2, 2d) * (1d - pow(cos(theta_i), 2d));
			Vector3D refractedVector = i.scalarMultiply(n1 / n2)
					.add(n.scalarMultiply((n1 / n2) * cos(theta_i) - sqrt(1d - sin2_theta_t)));
			refractedRay = new Ray(point, refractedVector, ray.getRecursiveLevel() + 1);

			//
			//
			// Calculate reflectance and transmittance fractions
			reflectance = 1d;
			//
			//
			// Is this *not* a case of Total-Internal Reflection?
			if (sin2_theta_t <= 1d) {
				//
				double cos_theta_t = sqrt(1d - sin2_theta_t);
				double r_normal = pow((n1 * cos(theta_i) - n2 * cos_theta_t) / (n1 * cos(theta_i) + n2 * cos_theta_t),
						2d);
				double r_tangent = pow((n2 * cos(theta_i) - n1 * cos_theta_t) / (n2 * cos(theta_i) + n1 * cos_theta_t),
						2d);

				reflectance = (r_normal + r_tangent) / 2d;
			}

			this.transmittance = 1d - reflectance;
		}

		private double reflectance, transmittance;

		private Ray reflectedRay, refractedRay;

		public double getReflectance() {

			return reflectance;
		}

		public double getTransmittance() {

			return transmittance;
		}

		public Ray getReflectedRay() {

			return reflectedRay;
		}

		public Ray getRefractedRay() {

			return refractedRay;
		}
	}

	private static Vector3D getNormalPart(Vector3D v, Vector3D normal) {

		return normal.scalarMultiply(normal.dotProduct(v));
	}

	private static Vector3D getTangentPart(Vector3D v, Vector3D normal) {

		return v.subtract(getNormalPart(v, normal));
	}

	/**
	 * @return the LightingModel this {@link FresnelLightingModel} uses to
	 *         illuminate object-surfaces
	 */
	public LightingModel getSurfaceLightingModel() {

		return surfaceLightingModel;
	}

}
