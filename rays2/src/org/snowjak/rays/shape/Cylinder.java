package org.snowjak.rays.shape;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;
import org.apache.commons.math3.geometry.euclidean.twod.Vector2D;
import org.apache.commons.math3.util.FastMath;
import org.snowjak.rays.Ray;
import org.snowjak.rays.intersect.Intersection;
import org.snowjak.rays.material.Material;
import org.snowjak.rays.transform.Translation;
import org.snowjak.rays.world.World;

/**
 * Represents an open-ended cylinder aligned with the Y-axis, centered on
 * (0,0,0), with a radius of 1, and an extension along the Y-axis from [-1] to
 * [1].
 * 
 * @author snowjak88
 *
 */
public class Cylinder extends Shape {

	private static final Random RND = new Random();

	private Plane minusYCap, plusYCap;

	/**
	 * Create a new Cylinder.
	 */
	public Cylinder() {

		this.minusYCap = new Plane();
		minusYCap.getTransformers().add(new Translation(0d, -1d, 0d));

		this.plusYCap = new Plane();
		plusYCap.getTransformers().add(new Translation(0d, 1d, 0d));
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<Intersection<Shape>> getIntersections(Ray ray, boolean includeBehindRayOrigin,
			boolean onlyIncludeClosest) {

		Ray localRay = worldToLocal(ray);

		if (!isIntersectWithBoundingSphere(localRay, 3d))
			return Collections.emptyList();

		Vector3D localLocation = worldToLocal(getLocation());
		List<Intersection<Shape>> results = new LinkedList<>();
		//
		//
		// Here we take the sphere-intersection detection routine from our
		// Sphere primitive, and adapt it for 2 dimensions (X,Z).
		//
		// For explanation of this routine, see the comments in
		// org.snowjak.rays.shape.Sphere

		Vector2D rayOrigin = new Vector2D(localRay.getOrigin().getX(), localRay.getOrigin().getZ());
		Vector2D rayVector = new Vector2D(localRay.getVector().getX(), localRay.getVector().getZ()).normalize();
		Vector2D circleOrigin = new Vector2D(localLocation.getX(), localLocation.getZ());

		Vector2D L = circleOrigin.subtract(rayOrigin);
		double t_ca = rayVector.dotProduct(L);
		double d2 = L.getNormSq() - FastMath.pow(t_ca, 2d);

		double r2 = 1d;
		if (Double.compare(d2, r2) > 0)
			return Collections.emptyList();

		double t_hc = FastMath.sqrt(r2 - d2);

		double t1_2d = t_ca - t_hc;
		double t2_2d = t_ca + t_hc;

		boolean useIntersection1 = (includeBehindRayOrigin || Double.compare(t1_2d, World.NEARLY_ZERO) >= 0),
				useIntersection2 = (includeBehindRayOrigin || Double.compare(t2_2d, World.NEARLY_ZERO) >= 0);
		//
		// Now we can determine the intersection-point(s) in 2D space.
		Vector2D intersectionPoint1_2D = rayOrigin.add(rayVector.scalarMultiply(t1_2d));
		Vector2D intersectionPoint2_2D = rayOrigin.add(rayVector.scalarMultiply(t2_2d));
		//
		// And translate those 2D intersection-points into 3D equivalents.
		double t1 = (intersectionPoint1_2D.getX() - localRay.getOrigin().getX()) / localRay.getVector().getX();
		double t2 = (intersectionPoint2_2D.getX() - localRay.getOrigin().getX()) / localRay.getVector().getX();

		Vector3D intersectionPoint1 = localRay.getOrigin().add(localRay.getVector().scalarMultiply(t1));
		Vector3D intersectionPoint2 = localRay.getOrigin().add(localRay.getVector().scalarMultiply(t2));

		//
		//
		// Now we've determined the intersection(s) to the cylinder in 3D-space.
		// Time to see if those intersections are within the bounds of this
		// cylinder.
		//
		if (useIntersection1 && Double.compare(FastMath.abs(t1), World.NEARLY_ZERO) >= 0
				&& Double.compare(intersectionPoint1.getY(), -1d) >= 0
				&& Double.compare(intersectionPoint1.getY(), 1d) <= 0) {

			// We need to ensure that the reported surface normal is facing
			// toward the intersecting ray.
			// After all, it is possible to see on both sides of the
			// cylinder's surface.
			Vector3D normal = new Vector3D(intersectionPoint1.getX(), 0d, intersectionPoint1.getZ()).normalize();
			double normalSign = FastMath.signum(localRay.getVector().negate().dotProduct(normal));
			normal = normal.scalarMultiply(Double.compare(normalSign, 0d) != 0 ? normalSign : 1d);

			results.add(new Intersection<Shape>(intersectionPoint1, normal, localRay, this, getDiffuseColorScheme(),
					getSpecularColorScheme(), getEmissiveColorScheme(), getMaterial(), getMaterial()));
		}

		if (!(onlyIncludeClosest && results.size() > 0))
			if (useIntersection2 && Double.compare(FastMath.abs(t2), World.NEARLY_ZERO) >= 0
					&& Double.compare(intersectionPoint2.getY(), -1d) >= 0
					&& Double.compare(intersectionPoint2.getY(), 1d) <= 0) {

				Vector3D normal = new Vector3D(intersectionPoint2.getX(), 0d, intersectionPoint2.getZ()).normalize();
				double normalSign = FastMath.signum(localRay.getVector().negate().dotProduct(normal));
				normal = normal.scalarMultiply(Double.compare(normalSign, 0d) != 0 ? normalSign : 1d);

				results.add(new Intersection<Shape>(intersectionPoint2, normal, localRay, this, getDiffuseColorScheme(),
						getSpecularColorScheme(), getEmissiveColorScheme(), getMaterial(), getMaterial()));
			}

		//
		//
		// Now -- are the ends of this cylinder capped?
		//
		// Test each of the capped ends for an intersection.
		// If any intersection exists, test if it lies within the circle of this
		// cylinder.
		//

		if (!results.isEmpty()) {

			//
			// Remember that a circle is x^2 + y^2 = r^2
			// or, for points inside the circle:
			// x^2 + y^2 <= r^2
			results.addAll(minusYCap.getIntersections(localRay, includeBehindRayOrigin, onlyIncludeClosest)
					.parallelStream()
					.filter(i -> Double.compare(
							FastMath.pow(i.getPoint().getX(), 2d) + FastMath.pow(i.getPoint().getZ(), 2d), 1d) <= 0)
					.peek(i -> {
						i.setIntersected(this);
						i.setDiffuseColorScheme(getDiffuseColorScheme());
						i.setSpecularColorScheme(getSpecularColorScheme());
						i.setEmissiveColorScheme(getEmissiveColorScheme());
						i.setLeavingMaterial(getMaterial());
						i.setEnteringMaterial(getMaterial());
					})
					.collect(Collectors.toCollection(LinkedList::new)));
		}

		if (!results.isEmpty()) {
			results.addAll(plusYCap.getIntersections(localRay, includeBehindRayOrigin, onlyIncludeClosest)
					.parallelStream()
					.filter(i -> Double.compare(
							FastMath.pow(i.getPoint().getX(), 2d) + FastMath.pow(i.getPoint().getZ(), 2d), 1d) <= 0)
					.peek(i -> {
						i.setDiffuseColorScheme(getDiffuseColorScheme());
						i.setSpecularColorScheme(getSpecularColorScheme());
						i.setEmissiveColorScheme(getEmissiveColorScheme());
						i.setLeavingMaterial(getMaterial());
						i.setEnteringMaterial(getMaterial());
						i.setIntersected(this);
					})
					.collect(Collectors.toCollection(LinkedList::new)));
		}

		results = results.parallelStream()
				.map(i -> localToWorld(i))
				.filter(i -> Double.compare(FastMath.abs(i.getDistanceFromRayOrigin()), World.NEARLY_ZERO) >= 0)
				.sorted((i1, i2) -> Double.compare(i1.getDistanceFromRayOrigin(), i2.getDistanceFromRayOrigin()))
				.limit(onlyIncludeClosest ? 1 : 2)
				.peek(i -> i.setIntersected(this))
				.collect(Collectors.toCollection(LinkedList::new));
		if (results.size() > 0)
			results.get(0).setLeavingMaterial(Material.AIR);
		if (results.size() > 1)
			results.get(1).setEnteringMaterial(Material.AIR);

		return results;
	}

	@Override
	public Cylinder copy() {

		Cylinder newCylinder = new Cylinder();
		newCylinder = configureCopy(newCylinder);

		return newCylinder;
	}

	@Override
	public Vector3D getNormalRelativeTo(Vector3D localPoint) {

		Vector3D normal = localPoint.normalize();
		return new Vector3D(normal.getX(), 0d, normal.getZ());
	}

	@Override
	public Vector3D selectPointWithin(boolean selectSurfaceOnly) {

		double theta = 2d * FastMath.PI * RND.nextDouble();
		double r = (selectSurfaceOnly ? 1d : 1d * RND.nextDouble());
		double h = (2d * RND.nextDouble()) - 1d;
		return localToWorld(new Vector3D(r * FastMath.cos(theta), h, r * FastMath.sin(theta)));
	}

}
