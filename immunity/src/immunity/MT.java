package immunity;

import java.io.FileWriter;
import java.io.IOException;
import java.util.Random;

//import com.thoughtworks.xstream.XStream;

import repast.simphony.engine.schedule.ScheduledMethod;
import repast.simphony.random.RandomHelper;
import repast.simphony.space.continuous.ContinuousSpace;
import repast.simphony.space.grid.Grid;

public class MT {
	// globals
	private ContinuousSpace<Object> space;
	private Grid<Object> grid;
	double xorigin = 40d;
	double xend = 40d;
	double yorigin = 0d;
	double yend = 50d;
	double zorigin = 0d;
	double zend = 50d;
	double mth = Math.atan((yend - yorigin) / (xend - xorigin));
	public double mtheading = -mth * 180 / Math.PI;
	public double length = 0.0;

	// constructor
	public MT(ContinuousSpace<Object> sp, Grid<Object> gr) {
		this.space = sp;
		this.grid = gr;
	}

	@ScheduledMethod(start = 1, interval = 100)
	public void step() {
		if (Math.random() <0.01)
			changePosition(this);
//		mtheading = -180;
		
	}

	public void changePosition(MT mt) {
		//if (Math.random() < 0.1) return;
		// move the origin and the end of the MT
//		xorigin = RandomHelper.nextDoubleFromTo(15, 35);
//		if (xorigin <= 25) {xend = xorigin -RandomHelper.nextDoubleFromTo(0, xorigin);}
//		else {xend = xorigin + RandomHelper.nextDoubleFromTo(0, 50-xorigin);}
//		double mth = Math.atan((50) / (xend - xorigin));
//		System.out.println("a-tang");
//		System.out.println(mth * 180 / Math.PI);
//		if (mth < 0) {
//			mth = 180 + (mth * 180 / Math.PI);
//		} else
//		{	mth = mth * 180 / Math.PI;}
////		xorigin = 25;
////		xend = RandomHelper.nextDoubleFromTo(0, 50);

		xorigin= CellBuilder.xWorld/2;
		yorigin = CellBuilder.yWorld/2;
		zorigin = CellBuilder.zWorld/2;
		
		double[] randomMTend = getRandomPointOnCircle(CellBuilder.xWorld/2);
		xend = randomMTend[0]+ xorigin;
		yend = randomMTend[1] + yorigin;
		zend = zorigin;
		mtheading = Math.atan2(xend-25, yend-25)*180/Math.PI; //-mth;
		double x = (xend + xorigin)/2 ;//25 * Math.cos(mtheading*Math.PI / 180);
		double y = (yend + yorigin)/2 ;//25 * Math.sin(mtheading*Math.PI / 180);
		double z = CellBuilder.zWorld/2;
		space.moveTo(mt, x, y, z);
		grid.moveTo(mt, (int) x, (int) y, (int) z);
		length = Math.sqrt((xend-xorigin)*(xend-xorigin)+(yend-yorigin)*(yend-yorigin));
//System.out.println(mtheading + "  "+xend +" XY al azar del cuadrado  "+ yend);		
//writing to a xml file.  It works, but I will not be able to use to strart a simulation
//		XStream xstream = new XStream();
//		String file = "C:/Users/lmayo/Desktop/pruebaXML.xml";
//		try {
//			xstream.toXML(mt, new FileWriter(file));
//		} catch (IOException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
		
	}
    public static double[] getRandomPointOnCircle(double r) {
        Random random = new Random();

        // Generate a random angle in radians between 0 and 2PI
        double theta = 2 * Math.PI * random.nextDouble();

        // Calculate x and y coordinates
        double x = r * Math.cos(theta);
        double y = r * Math.sin(theta);

        return new double[] { x, y };
    }
	
	// GETTERS AND SETTERS
	public double getXorigin() {
		return xorigin;
	}

	public double getXend() {
		return xend;
	}

	public double getYorigin() {
		return yorigin;
	}

	public double getYend() {
		return yend;
	}
	public double getZorigin() {
		return zorigin;
	}

	public double getZend() {
		return zend;
	}

	public double getMtheading() {
		return mtheading;
	}

	public double getLength() {
		return length;
	}

}
