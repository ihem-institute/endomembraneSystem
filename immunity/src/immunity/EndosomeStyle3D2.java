package immunity;
import java.awt.Color;
import java.awt.Font;

import org.jogamp.java3d.Shape3D;

import repast.simphony.visualization.visualization3D.AppearanceFactory;
import repast.simphony.visualization.visualization3D.ShapeFactory;
import repast.simphony.visualization.visualization3D.style.Style3D;
import repast.simphony.visualization.visualization3D.style.TaggedAppearance;
import repast.simphony.visualization.visualization3D.style.TaggedBranchGroup;

public class EndosomeStyle3D2<Endosome> implements Style3D<Endosome>{



/**
 * Style for Predator agents.  Styled as a red sphere.
 * 
 * @author Eric Tatara
 *
 */


  public TaggedBranchGroup getBranchGroup(Endosome o, TaggedBranchGroup taggedGroup) {
    if (taggedGroup == null || taggedGroup.getTag() == null) {
      taggedGroup = new TaggedBranchGroup("DEFAULT");
      Shape3D sphere = ShapeFactory.createSphere(.2f, "DEFAULT");
      taggedGroup.getBranchGroup().addChild(sphere);
      return taggedGroup;
    }
    
    return null;
  }

  public float[] getRotation(Endosome o) {
    return null;
  }
  
  public String getLabel(Endosome o, String currentLabel) {
    return null;
  }

  public Color getLabelColor(Endosome t, Color currentColor) {
    return null; 
  }

  public Font getLabelFont(Endosome t, Font currentFont) {
    return null; 
  }

  public LabelPosition getLabelPosition(Endosome o, Style3D.LabelPosition curentPosition) {
    return Style3D.LabelPosition.NORTH;
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

  public float[] getScale(Endosome o) {
    return null;
  }

}
