package immunity;

import java.awt.Color;
import java.awt.Font;

import org.jogamp.java3d.Shape3D;
import org.jogamp.java3d.Transform3D;
import org.jogamp.java3d.TransformGroup;
import org.jogamp.java3d.Appearance;
import org.jogamp.java3d.ColoringAttributes;
import org.jogamp.java3d.Material;
import org.jogamp.java3d.Shape3D;
import org.jogamp.java3d.Texture;
import org.jogamp.java3d.Transform3D;
import org.jogamp.java3d.TransformGroup;
import org.jogamp.java3d.utils.geometry.Primitive;
import org.jogamp.java3d.utils.geometry.Sphere;
import org.jogamp.vecmath.Color3f;
import org.jogamp.vecmath.Vector3d;

import repast.simphony.visualization.visualization3D.AppearanceFactory;
import repast.simphony.visualization.visualization3D.ShapeFactory;
import repast.simphony.visualization.visualization3D.style.Style3D;
import repast.simphony.visualization.visualization3D.style.TaggedAppearance;
import repast.simphony.visualization.visualization3D.style.TaggedBranchGroup;

/**
 * Represents a spheroid for visualization in Repast3D.
 * 
 * @author Modified
 */
public class SpheroidNodeStyle implements Style3D<Endosome> {
    
	 public TaggedBranchGroup getBranchGroup(Endosome agent, 
		        TaggedBranchGroup taggedGroup) {
		    System.out.println("Rendering agent: " + agent);
		 
	        if (taggedGroup == null) {
	            taggedGroup = new TaggedBranchGroup("Spheroid");

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
	            transform.setScale(new Vector3d(1.0, 10, 1.0)); // Example: squish along the Y-axis

	            // Apply the transformation
	            tg.setTransform(transform);

	            // Add the sphere to the TransformGroup
	            tg.addChild(shape);

	            // Add the TransformGroup to the branch group
	            taggedGroup.getBranchGroup().addChild(tg);
	        }
	        return taggedGroup;
	    }
    
	    public float[] getRotation(Endosome o) {
	    	return new float[4];
	    }
    
    public String getLabel(Endosome o, String currentLabel) {
        return null; 
    }
    
    public Color getLabelColor(Endosome t, Color currentColor) {
        return Color.YELLOW;
    }
    
    public Font getLabelFont(Endosome t, Font currentFont) {
        return null;
    }
    
    public LabelPosition getLabelPosition(Endosome o, 
            LabelPosition currentPosition) {
        return LabelPosition.NORTH;
    }
    
    public float getLabelOffset(Endosome t) {
        return .035f;
    }
    
    public TaggedAppearance getAppearance(Endosome t, TaggedAppearance taggedAppearance, Object shapeID) {
        if (taggedAppearance == null || taggedAppearance.getTag() == null) {
          taggedAppearance = new TaggedAppearance("DEFAULT");
          AppearanceFactory.setMaterialAppearance(taggedAppearance.getAppearance(), Color.RED);
        }
        return taggedAppearance;
        
      }
    
    
    public float[] getScale(Endosome agent) {
    	return new float[]{1,0.02f,1};//; // Assume agent provides scale as [scaleX, scaleY, scaleZ]
    }
}
