package immunity;

import java.awt.Color;
import java.awt.Font;
import java.util.Collections;

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
import java.util.Map;

import repast.simphony.context.Context;
import repast.simphony.space.continuous.NdPoint;
import repast.simphony.util.ContextUtils;
import repast.simphony.visualization.visualization3D.AppearanceFactory;
import repast.simphony.visualization.visualization3D.ShapeFactory;
import repast.simphony.visualization.visualization3D.style.Style3D;
import repast.simphony.visualization.visualization3D.style.TaggedAppearance;
import repast.simphony.visualization.visualization3D.style.TaggedBranchGroup;
import repast.simphony.space.continuous.ContinuousSpace;
import repast.simphony.space.grid.Grid;
/**
 * Represents a spheroid for visualization in Repast3D.
 * 
 * @author Modified
 */
public class SpheroidNodeStyle implements Style3D<Endosome> {

	 public TaggedBranchGroup getBranchGroup(Endosome agent, 
		        TaggedBranchGroup taggedGroup) {

		 if (agent.getSpace().getLocation(agent) == null) {
			    System.out.println("Skipping agent with null location: " + agent);
			    return null;
			}
		 
	        if (taggedGroup == null) {
	            taggedGroup = new TaggedBranchGroup("Spheroid");
//			    System.out.println("Rendering agent: " + agent + " " + agent.getXcoor());
	            // Define appearance.  The color is modified  with public TaggedAppearance getAppearance
	            Appearance appearance = new Appearance();
	            ColoringAttributes colorAttr = new ColoringAttributes(new Color3f(1f,1f,1f), ColoringAttributes.SHADE_GOURAUD);
	            appearance.setColoringAttributes(colorAttr);

	            // Create a sphere proportional to the volume.
	            double size = Math.pow((agent.volume*3d/4d/Math.PI),1d/3d)*2E-4d;
//	            System.out.println("SIZE  "+size);
	            Sphere sphere = new Sphere((float) size, Primitive.GENERATE_NORMALS, 64, appearance);
	            Shape3D shape = new Shape3D(sphere.getShape().getGeometry(),appearance);
				shape.setCapability(Shape3D.ALLOW_APPEARANCE_READ);
				shape.setCapability(Shape3D.ALLOW_APPEARANCE_WRITE);
				shape.setCapability(Shape3D.ALLOW_GEOMETRY_READ);

	            // Create a TransformGroup for scaling
	            TransformGroup tg = new TransformGroup();

	            // Create a scale transformation for a spheroid
//	            Transform3D transform = new Transform3D();
//	            transform.setScale(new Vector3d(1.0, 10.0 , 1.0)); // Example: squish along the Y-axis

	            // Apply the transformation
	//            tg.setTransform(transform);

	            // Add the sphere to the TransformGroup
	            tg.addChild(shape);

	            // Add the TransformGroup to the branch group
	            taggedGroup.getBranchGroup().addChild(tg);
	        }
	        return taggedGroup;
	    }
    
	 public float[] getRotation(Endosome agent) {
		    // Convert angles to radians
		    double theta = Math.toRadians(agent.getHeadingP()); // Polar angle (degrees to radians)
		    double phi = Math.toRadians(agent.getHeadingA()); // Azimuthal angle (degrees to radians)
//		    theta = 0;
//		    phi= 0;
		    // Calculate quaternion components
		    float x = (float) (Math.sin(theta / 2) * Math.cos(phi / 2));
		    float y = (float) (Math.sin(theta / 2) * Math.sin(phi / 2));
		    float z = (float) (Math.cos(theta / 2) * Math.sin(phi / 2));
		    float w = (float) (Math.cos(theta / 2) * Math.cos(phi / 2));
//		    System.out.println(agent.headingP + agent.headingA + x + y + z + w);
		    
		    // Return the quaternion rotation
		    return new float[]{x, y, z, w};
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
    
    @Override
    public TaggedAppearance getAppearance(Endosome t, TaggedAppearance taggedAppearance, Object shapeID) {
        if (taggedAppearance == null || taggedAppearance.getTag() == null) {
            taggedAppearance = new TaggedAppearance("DEFAULT");

            // Determine the dominant Rab content
            String rabColor = Collections.max(t.rabContent.entrySet(), Map.Entry.comparingByValue()).getKey();

            // Set the appearance color based on Rab content
            switch (rabColor) {
                case "RabA":
                    AppearanceFactory.setMaterialAppearance(taggedAppearance.getAppearance(), Color.BLUE); // Early endosome
                    break;
                case "RabB":
                    AppearanceFactory.setMaterialAppearance(taggedAppearance.getAppearance(), Color.CYAN); // Sorting endosome
                    break;
                case "RabC":
                    AppearanceFactory.setMaterialAppearance(taggedAppearance.getAppearance(), Color.GREEN); // Recycling endosome
                    break;
                case "RabD":
                    AppearanceFactory.setMaterialAppearance(taggedAppearance.getAppearance(), Color.RED); // Late endosome
                    break;
                case "RabE":
                    AppearanceFactory.setMaterialAppearance(taggedAppearance.getAppearance(), Color.YELLOW); // TGN
                    break;
                case "RabF":
                    AppearanceFactory.setMaterialAppearance(taggedAppearance.getAppearance(), new Color(225, 128, 0)); // trans-Golgi
                    break;
                case "RabG":
                    AppearanceFactory.setMaterialAppearance(taggedAppearance.getAppearance(), new Color(191, 96, 0)); // medial-Golgi
                    break;
                case "RabH":
                    AppearanceFactory.setMaterialAppearance(taggedAppearance.getAppearance(), new Color(128, 64, 0)); // cis-Golgi
                    break;
                case "RabI":
                    AppearanceFactory.setMaterialAppearance(taggedAppearance.getAppearance(), Color.MAGENTA); // ERGIC
                    break;
                default:
                    AppearanceFactory.setMaterialAppearance(taggedAppearance.getAppearance(), Color.BLACK); // Default
                    break;
            }
        }
        return taggedAppearance;
    }

    
    
    public float[] getScale(Endosome agent) {
        Endosome.endosomeShape(agent);
        double a=agent.getA();
        double c=agent.getC();
    	double ratio = Math.pow(c/a, 0.5);
 //   	System.out.println("a/c   "+ratio);
    	return new float[]{1,(float) ratio ,1};//; // Assume agent provides scale as [scaleX, scaleY, scaleZ]
    }
    


    // Method to determine the color based on Rab content
    private Color3f getRabColor(Endosome agent) {
        String rabColor = Collections.max(agent.rabContent.entrySet(), Map.Entry.comparingByValue()).getKey();

        if (rabColor.equals("RabA")) return new Color3f(0f, 0f, 1f); // EE (blue)
        else if (rabColor.equals("RabB")) return new Color3f(0f, 1f, 1f); // SE (cyan)
        else if (rabColor.equals("RabC")) return new Color3f(0f, 1f, 0f); // RE (green)
        else if (rabColor.equals("RabD")) return new Color3f(1f, 0f, 0f); // LE (red)
        else if (rabColor.equals("RabE")) return new Color3f(1f, 1f, 0f); // TGN (yellow)
        else if (rabColor.equals("RabF")) return new Color3f(0.88f, 0.5f, 0f); // transG (orange)
        else if (rabColor.equals("RabG")) return new Color3f(0.75f, 0.38f, 0f); // medialG (brownish-orange)
        else if (rabColor.equals("RabH")) return new Color3f(0.5f, 0.25f, 0f); // cisG (dark brown)
        else if (rabColor.equals("RabI")) return new Color3f(1f, 0f, 1f); // ERGIC (magenta)
        else return new Color3f(0f, 0f, 0f); // Default (black)
    }

    
}
