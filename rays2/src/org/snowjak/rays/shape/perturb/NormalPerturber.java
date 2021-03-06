package org.snowjak.rays.shape.perturb;

import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.stream.Collectors;

import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;
import org.apache.commons.math3.util.FastMath;
import org.snowjak.rays.Ray;
import org.snowjak.rays.color.ColorScheme;
import org.snowjak.rays.intersect.Intersection;
import org.snowjak.rays.material.Material;
import org.snowjak.rays.shape.Shape;
import org.snowjak.rays.world.World;

/**
 * A decorator Shape that enhances another Shape with normal-perturbation --
 * i.e., bump-mapping. This is an uncomplicated way to add "bumpiness" to
 * objects.
 * <p>
 * This Shape encapsulates a child Shape. When intersections are detected the
 * child-Shape, this NormalPerturber updates the each intersection's
 * normal-vector with the results of its <strong>normal perturbation
 * function</strong>.
 * </p>
 * <p>
 * This normal perturbation function is of the form:
 * 
 * <pre>
 *   original-normal, original-intersection --> updated-normal
 * </pre>
 * 
 * where {@code original-normal} and {@code original-intersection} are expressed
 * in global coordinates (relative to the NormalPerturber instance).
 * 
 * See {@link #DEFAULT_PERTURBATION_FUNCTION}
 * </p>
 * 
 * @author snowjak88
 *
 */
public class NormalPerturber extends Shape {

	/**
	 * The default perturbation function. Implements {@code (v, i) -> v} --
	 * i.e., leaves the original normal unchanged.
	 */
	public static final BiFunction<Vector3D, Intersection<Shape>, Vector3D> DEFAULT_PERTURBATION_FUNCTION = (v, i) -> v;

	private BiFunction<Vector3D, Intersection<Shape>, Vector3D> normalPerturbationFunction = DEFAULT_PERTURBATION_FUNCTION;

	private Shape child;

	/**
	 * Create a new {@link NormalPerturber} with the specified perturbation
	 * function.
	 * 
	 * @param normalPerturbationFunction
	 * @param child
	 */
	public NormalPerturber(BiFunction<Vector3D, Intersection<Shape>, Vector3D> normalPerturbationFunction,
			Shape child) {
		super();
		this.normalPerturbationFunction = normalPerturbationFunction;
		this.child = child;
	}

	/**
	 * @return the current normal-perturbation {@link BiFunction}
	 */
	public BiFunction<Vector3D, Intersection<Shape>, Vector3D> getNormalPerturbationFunction() {

		return normalPerturbationFunction;
	}

	/**
	 * Set the normal-perturbation {@link BiFunction} to use
	 * 
	 * @param normalPerturbationFunction
	 */
	public void setNormalPerturbationFunction(
			BiFunction<Vector3D, Intersection<Shape>, Vector3D> normalPerturbationFunction) {

		this.normalPerturbationFunction = normalPerturbationFunction;
	}

	/**
	 * @return the {@link Shape} whose normals should be perturbed
	 */
	public Shape getChild() {

		return child;
	}

	/**
	 * Set the {@link Shape} whose normals should be perturbed
	 * 
	 * @param child
	 */
	public void setChild(Shape child) {

		this.child = child;
	}

	@Override
	public Vector3D getNormalRelativeTo(Vector3D localPoint) {

		return child.getNormalRelativeTo(localPoint);
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<Intersection<Shape>> getIntersections(Ray ray, boolean includeBehindRayOrigin,
			boolean onlyIncludeClosest) {

		List<Intersection<Shape>> childResults = child.getIntersections(worldToLocal(ray), includeBehindRayOrigin,
				onlyIncludeClosest);
		return childResults.parallelStream()
				.limit(onlyIncludeClosest ? 1 : childResults.size())
				.filter(i -> Double.compare(FastMath.abs(i.getDistanceFromRayOrigin()), World.NEARLY_ZERO) >= 0)
				.peek(i -> i.setNormal(normalPerturbationFunction.apply(i.getNormal(), i)))
				.map(i -> localToWorld(i))
				.collect(Collectors.toCollection(LinkedList::new));
	}

	@Override
	public ColorScheme getDiffuseColorScheme() {

		return child.getDiffuseColorScheme();
	}

	@Override
	public void setDiffuseColorScheme(ColorScheme diffuseColorScheme) {

		child.setDiffuseColorScheme(diffuseColorScheme);
	}

	@Override
	public ColorScheme getSpecularColorScheme() {

		return child.getSpecularColorScheme();
	}

	@Override
	public void setSpecularColorScheme(ColorScheme specularColorScheme) {

		child.setSpecularColorScheme(specularColorScheme);
	}

	@Override
	public Optional<ColorScheme> getEmissiveColorScheme() {

		return child.getEmissiveColorScheme();
	}

	@Override
	public void setEmissiveColorScheme(ColorScheme emissiveColorScheme) {

		child.setEmissiveColorScheme(emissiveColorScheme);
	}

	@Override
	public Material getMaterial() {

		return child.getMaterial();
	}

	@Override
	public void setMaterial(Material material) {

		child.setMaterial(material);
	}

	@Override
	public NormalPerturber copy() {

		NormalPerturber perturber = new NormalPerturber(normalPerturbationFunction, child.copy());
		return configureCopy(perturber);
	}

	@Override
	public Vector3D selectPointWithin(boolean selectSurfaceOnly) {

		return child.selectPointWithin(selectSurfaceOnly);
	}

}
