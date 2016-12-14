package immunity;

import java.awt.Color;
import java.awt.Font;

import javax.media.opengl.GL2;

import repast.simphony.visualizationOGL2D.StyleOGL2D;
import saf.v3d.ShapeFactory2D;
import saf.v3d.render.RenderState;
import saf.v3d.scene.Position;
import saf.v3d.scene.VShape;
import saf.v3d.scene.VSpatial;

public class CytosolStyle implements StyleOGL2D<Cytosol> {

	ShapeFactory2D factory;
	private double String;
	
	@Override
	public void init(ShapeFactory2D factory) {
		this.factory = factory;

	}

	@Override
	public VSpatial getVSpatial(Cytosol object, VSpatial spatial) {
		////double s = object.getArea();
		//double v = object.getVolume();
		//int svr =(int) ((s * s * s) / (v * v)/ (113d));
		//double h =  object.getMtheading();
		//double hh = h * Math.PI /180;
		  //System.out.println(vsphere);
		//int length = (int)(- 750 / (Math.sin(hh)));
		//System.out.println(length)
		VSpatial createRectangle = this.factory.createRectangle(5, 5);
		return createRectangle;

	}

	@Override
	public Color getColor(Cytosol object) {
		//int f = Math.abs( (int)object.getArea() % 256 );
		return new Color(0, 0, 0);
	}

	@Override
	public int getBorderSize(Cytosol object) {
		return 0;
	}

	@Override
	public Color getBorderColor(Cytosol object) {
		return new Color(100);
	}

	@Override
	public float getRotation(Cytosol object) {
		//float h = (float) object.getMtheading()+ 90f;
		return 0;
	}

	@Override
	public float getScale(Cytosol object) {
		
		return (float) 1; //object.size() / 10f;
	}

	@Override
	public String getLabel(Cytosol object) {
		return ""; 
	}
	@Override
	public Font getLabelFont(Cytosol object) {
		return new Font("sansserif", Font.PLAIN, 14);
	}

	@Override
	public float getLabelXOffset(Cytosol object) {
		return 0;
	}

	@Override
	public float getLabelYOffset(Cytosol object) {
		return 0;
	}

	@Override
	public Position getLabelPosition(Cytosol object) {
		return Position.CENTER;
	}

	@Override
	public Color getLabelColor(Cytosol object) {
		return new Color(100);
	}

}
