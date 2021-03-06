package org.snowjak.rays.shape;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;

import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;
import org.apache.commons.math3.util.FastMath;
import org.snowjak.rays.Ray;
import org.snowjak.rays.intersect.Intersection;
import org.snowjak.rays.material.Material;
import org.snowjak.rays.world.World;

/**
 * Represents a plane, by default oriented normal to the Y-axis.
 * 
 * @author snowjak88
 *
 */
public class Plane extends Shape {

	private static final Random RND = new Random();

	private Material plusMaterial, minusMaterial;

	/**
	 * Construct a new Plane, passing through (0,0,0) and normal to the Y-axis.
	 * <p>
	 * Note that, at present, this Plane is invisible, as its plus- and
	 * minus-Materials are both set to the default Material
	 * ({@link Material#AIR}). If this Plane should be visible, you should
	 * configure it further with {@link #setPlusMaterial(Material)} and
	 * {@link #setMinusMaterial(Material)}.
	 * </p>
	 * 
	 * @see #Plane(Material, Material)
	 */
	public Plane() {
		this(Material.AIR, Material.AIR);
	}

	/**
	 * Construct a new Plane, passing through (0,0,0) and normal to the Y-axis.
	 * This plane delineates a boundary between two Materials -- one on the +Y
	 * side of the plane ({@code plusMaterial}) and one on the -Y side
	 * ({@code minusMaterial}).
	 * 
	 * @param plusMaterial
	 * @param minusMaterial
	 */
	public Plane(Material plusMaterial, Material minusMaterial) {
		this.plusMaterial = plusMaterial;
		this.minusMaterial = minusMaterial;
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<Intersection<Shape>> getIntersections(Ray ray, boolean includeBehindRayOrigin,
			boolean onlyIncludeClosest) {

		Ray transformedRay = worldToLocal(ray);

		LinkedList<Intersection<Shape>> results = new LinkedList<>();

		//
		//
		// Because this plane will always be oriented normal to the Y-axis,
		// and its displacement will always = 0,
		// a number of simplifications apply to the intersection calculations.
		//
		// Remember that a ray at any given extent t is defined by P + V*t
		//
		// We want to find t such that P + V*t intersects the plane -- i.e.,
		// so that P.y + V.y*t = 0
		//
		//
		// First: check for the trivial case: where the vector is parallel to
		// the plane.
		if (Double.compare(FastMath.abs(transformedRay.getVector().getY()), World.NEARLY_ZERO) < 0) {
			//
			// This ray is entirely parallel to the plane.
			// In the past, we tried giving an intersection at 0 distance from
			// the ray origin if
			// the ray was entirely within the plane -- but that led to all
			// kinds of problems with trying to normalize zero-length vectors.
			// It's easier to say that, if a ray is entirely parallel to the
			// plane, then it's treated across the board as non-intersecting.
			return Collections.emptyList();

		} else {
			//
			// Not parallel.
			//
			// OK -- find t such that P.y + V.y*t = 0
			//
			// P.y + V.y*t = 0
			// V.y*t = 0 - P.y
			// t = -P.y/V.y
			//
			//
			double t = -transformedRay.getOrigin().getY() / transformedRay.getVector().getY();

			if (!includeBehindRayOrigin && Double.compare(t, World.NEARLY_ZERO) <= 0)
				return Collections.emptyList();

			if (Double.compare(FastMath.abs(t), World.NEARLY_ZERO) < 0)
				return Collections.emptyList();

			Vector3D intersectionPoint = transformedRay.getOrigin().add(transformedRay.getVector().scalarMultiply(t));
			double normalSign = FastMath.signum(transformedRay.getVector().negate().dotProduct(Vector3D.PLUS_J));
			Vector3D normal = Vector3D.PLUS_J.scalarMultiply(Double.compare(normalSign, 0d) != 0 ? normalSign : 1d)
					.normalize();

			Material leavingMaterial, enteringMaterial;
			if (Double.compare(normalSign, 0d) < 0) {
				leavingMaterial = minusMaterial;
				enteringMaterial = plusMaterial;
			} else {
				leavingMaterial = plusMaterial;
				enteringMaterial = minusMaterial;
			}

			results.add(localToWorld(new Intersection<Shape>(intersectionPoint, normal, transformedRay, this,
					this.getDiffuseColorScheme(), this.getSpecularColorScheme(), this.getEmissiveColorScheme(),
					leavingMaterial, enteringMaterial)));

		}

		return results;
	}

	@Override
	@Deprecated
	public Material getMaterial() {

		return super.getMaterial();
	}

	@Override
	@Deprecated
	public void setMaterial(Material material) {

		super.setMaterial(material);
	}

	/**
	 * @return the Material assigned to the (local) Y+ side of this plane
	 */
	public Material getPlusMaterial() {

		return plusMaterial;
	}

	/**
	 * Set the Material assigned to the (local) Y+ side of this plane.
	 * 
	 * @param plusMaterial
	 */
	public void setPlusMaterial(Material plusMaterial) {

		this.plusMaterial = plusMaterial;
	}

	/**
	 * @return the Material assigned to the (local) Y- side of this plane
	 */
	public Material getMinusMaterial() {

		return minusMaterial;
	}

	/**
	 * Set the Material assigned to the (local) Y- side of this plane
	 * 
	 * @param minusMaterial
	 */
	public void setMinusMaterial(Material minusMaterial) {

		this.minusMaterial = minusMaterial;
	}

	@Override
	public boolean isInside(Vector3D point) {

		Vector3D localPoint = worldToLocal(point);

		if (Double.compare(FastMath.abs(localPoint.getY()), World.NEARLY_ZERO) <= 0)
			return true;

		return false;
	}

	/**
	 * @return this Plane's normal vector, in global coordinates
	 */
	public Vector3D getNormal() {

		return localToWorld(new Ray(Vector3D.ZERO, Vector3D.PLUS_J)).getVector();
	}

	@Override
	public Plane copy() {

		Plane newPlane = new Plane(this.getPlusMaterial(), this.getMinusMaterial());
		newPlane.setDiffuseColorScheme(this.getDiffuseColorScheme().copy());
		newPlane.setSpecularColorScheme(this.getSpecularColorScheme().copy());
		if (this.isEmissive())
			newPlane.setEmissiveColorScheme(this.getEmissiveColorScheme().get().copy());
		newPlane.getTransformers().addAll(this.getTransformers());
		return newPlane;
	}

	@Override
	public Vector3D getNormalRelativeTo(Vector3D localPoint) {

		double normalSign = FastMath.signum(localPoint.normalize().dotProduct(Vector3D.PLUS_J));
		return Vector3D.PLUS_J.scalarMultiply(normalSign).normalize();
	}

	@Override
	public Vector3D selectPointWithin(boolean selectSurfaceOnly) {

		return localToWorld(new Vector3D((RND.nextDouble() * 2d - 1d) * World.FAR_AWAY, 0d,
				(RND.nextDouble() * 2d - 1d) * World.FAR_AWAY));
	}

}
