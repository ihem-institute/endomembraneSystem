package immunity;

import java.awt.Color;
import java.awt.Font;

import org.jogamp.java3d.Appearance;
import org.jogamp.java3d.ColoringAttributes;
import org.jogamp.java3d.Material;
import org.jogamp.java3d.Shape3D;
import org.jogamp.java3d.Transform3D;
import org.jogamp.java3d.TransformGroup;
import org.jogamp.java3d.utils.geometry.Primitive;
import org.jogamp.java3d.utils.geometry.Sphere;
import org.jogamp.vecmath.Color3f;
import org.jogamp.vecmath.Vector3d;

import repast.simphony.visualization.visualization3D.style.Style3D;
import repast.simphony.visualization.visualization3D.style.TaggedAppearance;
import repast.simphony.visualization.visualization3D.style.TaggedBranchGroup;
 class EndosomeStyle3D implements Style3D<Object> {

    @Override
    public TaggedBranchGroup getBranchGroup(Object obj, TaggedBranchGroup group) {
        if (group == null) {
            group = new TaggedBranchGroup("Spheroid");

            // Define appearance
            Appearance appearance = new Appearance();
            ColoringAttributes colorAttr = new ColoringAttributes(new Color3f(0.5f, 0.5f, 1.0f), ColoringAttributes.SHADE_GOURAUD);
            appearance.setColoringAttributes(colorAttr);

            // Create a sphere
            Sphere sphere = new Sphere(1.0f, Primitive.GENERATE_NORMALS, 64, appearance);
            Shape3D shape = new Shape3D(sphere.getShape().getGeometry(),appearance);
			shape.setCapability(Shape3D.ALLOW_APPEARANCE_READ);
			shape.setCapability(Shape3D.ALLOW_APPEARANCE_WRITE);
			shape.setCapability(Shape3D.ALLOW_GEOMETRY_READ);

            // Create a TransformGroup for scaling
            TransformGroup tg = new TransformGroup();

            // Create a scale transformation for a spheroid
            Transform3D transform = new Transform3D();
            transform.setScale(new Vector3d(1.0, 0.5, 1.0)); // Example: squish along the Y-axis

            // Apply the transformation
            tg.setTransform(transform);

            // Add the sphere to the TransformGroup
            tg.addChild(shape);

            // Add the TransformGroup to the branch group
            group.getBranchGroup().addChild(tg);
        }
        return group;
    }


    private Appearance createAppearance() {
        Appearance appearance = new Appearance();
        Material material = new Material();
        material.setAmbientColor(0.0f, 1.0f, 0.0f); // Green
        appearance.setMaterial(material);
        return appearance;
    }

    @Override
    public float[] getScale(Object obj) {
        return new float[] { 1.0f, 1.0f, 1.0f };
    }

    @Override
    public String getLabel(Object obj, String currentLabel) {
        return "Object Label";
    }

    @Override
    public Color getLabelColor(Object obj, Color currentColor) {
        return Color.BLUE;
    }

    @Override
    public Font getLabelFont(Object obj, Font currentFont) {
        return currentFont;
    }

    @Override
    public float getLabelOffset(Object obj) {
        return 1.0f;
    }

	@Override
	public float[] getRotation(Object obj) {
		// TODO Auto-generated method stub
		return null;
	}


	@Override
	public LabelPosition getLabelPosition(Object obj, LabelPosition curentPosition) {
		return LabelPosition.NORTH;
	}

	@Override
	public TaggedAppearance getAppearance(Object obj, TaggedAppearance appearance, Object shapeID) {
		// TODO Auto-generated method stub
		return appearance;
	}
}
