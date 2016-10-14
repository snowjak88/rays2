package org.snowjak.rays.material;

import java.util.LinkedList;
import java.util.List;
import java.util.function.Function;

import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;
import org.snowjak.rays.builder.Builder;
import org.snowjak.rays.color.RawColor;
import org.snowjak.rays.function.Functions;
import org.snowjak.rays.transform.TransformableBuilder;
import org.snowjak.rays.transform.Transformer;

import javafx.scene.paint.Color;

/**
 * A convenient interface for building {@link Material}s.
 * 
 * @author snowjak88
 *
 */
public class MaterialBuilder implements Builder<Material>, TransformableBuilder<Material> {

	private Function<Vector3D, RawColor> color = Material.AIR.getSurfaceColor();

	private Function<Vector3D, Double> surfaceTransparency = Material.AIR.getSurfaceTransparency(),
			refractiveIndex = Material.AIR.getRefractiveIndex();

	private List<Transformer> transformers = new LinkedList<>();

	/**
	 * @return a new MaterialBuilder instance
	 */
	public static MaterialBuilder builder() {

		return new MaterialBuilder();
	}

	protected MaterialBuilder() {

	}

	/**
	 * Configures this in-progress Material to use a specified constant color.
	 * 
	 * @param constantColor
	 * @return this Builder, for method-chaining
	 */
	public MaterialBuilder color(Color constantColor) {

		return color(new RawColor(constantColor));
	}

	/**
	 * Configures this in-progress Material to use a specified constant color.
	 * 
	 * @param constantColor
	 * @return this Builder, for method-chaining
	 */
	public MaterialBuilder color(RawColor constantColor) {

		return color(Functions.constant(constantColor));
	}

	/**
	 * Configures this in-progress Material to use the specified
	 * {@link Function} when computing coloring.
	 * 
	 * @param colorFunction
	 * @return this Builder, for method-chaining
	 */
	public MaterialBuilder color(Function<Vector3D, RawColor> colorFunction) {

		this.color = colorFunction;
		return this;
	}

	/**
	 * Configures this in-progress Material to use a constant value for
	 * surface-transparency.
	 * 
	 * @param transparency
	 * @return this Builder, for method-chaining
	 */
	public MaterialBuilder surfaceTransparency(double transparency) {

		return surfaceTransparency(Functions.constant(transparency));
	}

	/**
	 * Configures this in-progress Material to use the specified
	 * {@link Function} when computing surface transparency.
	 * 
	 * @param transparencyFunction
	 * @return this Builder, for method-chaining
	 */
	public MaterialBuilder surfaceTransparency(Function<Vector3D, Double> transparencyFunction) {

		this.surfaceTransparency = transparencyFunction;
		return this;
	}

	/**
	 * Configures this in-progress Material to use a constant value for the
	 * Material's refractive index.
	 * 
	 * @param refractiveIndex
	 * @return this Builder, for method-chaining
	 */
	public MaterialBuilder refractiveIndex(double refractiveIndex) {

		return refractiveIndex(Functions.constant(refractiveIndex));
	}

	/**
	 * Configures this in-progress Material to use the specified
	 * {@link Function} when computing the refractive index.
	 * 
	 * @param refractiveIndexFunction
	 * @return this Builder, for method-chaining
	 */
	public MaterialBuilder refractiveIndex(Function<Vector3D, Double> refractiveIndexFunction) {

		this.refractiveIndex = refractiveIndexFunction;
		return this;
	}

	@Override
	public MaterialBuilder transform(Transformer transformer) {

		this.transformers.add(transformer);
		return this;
	}

	@Override
	public Material build() {

		Material newMaterial = new Material(color, surfaceTransparency, refractiveIndex);
		newMaterial.getTransformers().addAll(transformers);

		return newMaterial;
	}

}
