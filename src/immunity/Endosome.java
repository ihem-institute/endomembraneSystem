package immunity;

//import immunity.EndosomeStyle.MemCont;
//import immunity.EndosomeStyle.RabCont;
//import immunity.EndosomeStyle.SolCont;
import java.util.Random;
import java.awt.Graphics;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.opengis.filter.identity.ObjectId;

import repast.simphony.context.Context;
import repast.simphony.context.Context;
import repast.simphony.context.DefaultContext;
import repast.simphony.context.space.continuous.ContinuousSpaceFactory;
import repast.simphony.context.space.continuous.ContinuousSpaceFactoryFinder;
import repast.simphony.context.space.graph.NetworkBuilder;
import repast.simphony.context.space.grid.GridFactory;
import repast.simphony.context.space.grid.GridFactoryFinder;
import repast.simphony.engine.environment.RunEnvironment;
import repast.simphony.engine.schedule.ScheduledMethod;
import repast.simphony.parameter.Parameters;
import repast.simphony.query.space.continuous.ContinuousWithin;
import repast.simphony.query.space.grid.GridCell;
import repast.simphony.query.space.grid.GridCellNgh;
import repast.simphony.random.RandomHelper;
import repast.simphony.space.SpatialMath;
import repast.simphony.space.continuous.ContinuousSpace;
import repast.simphony.space.continuous.NdPoint;
import repast.simphony.space.grid.Grid;
import repast.simphony.space.grid.GridPoint;
import repast.simphony.util.ContextUtils;

import java.util.Random;

import repast.simphony.valueLayer.GridValueLayer;

/**
 * @author lmayorga
 *
 */
public class Endosome {
	// space

	private ContinuousSpace<Object> space;
	private Grid<Object> grid;
	// Endosomal
	CellProperties cellProperties = CellProperties.getInstance();
	HashMap<String, Double> cellK = cellProperties.getCellK();

	double area = 4d * Math.PI * 30d * 30d; // initial value, but should change
	double volume = 4d / 3d * Math.PI * 30d * 30d * 30d; // initial value, but
															// should change
	double a = 0; // width of the ellipsoid representing the endosome
	double c = 0; // length;
	double size;// = Math.pow(volume * 3d / 4d / Math.PI, (1d / 3d));
	double speed;// = 5d / size; // initial value, but should change
	double heading = 0;// = Math.random() * 360d; // initial value, but should
						// change
	double cellLimit = 3 * Cell.orgScale;
	double mvb;// = 0; // number of internal vesices
	double cellMembrane;// = 0;
	Set<String> membraneMet = cellProperties.getMembraneMet();
	Set<String> solubleMet = cellProperties.getSolubleMet();
	Set<String> rabSet = cellProperties.getRabSet();
	HashMap<String, Double> rabCell = new HashMap<String, Double>();
	private List<MT> mts;
	// HashMap<String, Double> membraneMet = cellProperties.membraneMet();
	HashMap<String, Double> rabCompatibility = cellProperties
			.getRabCompatibility();
	HashMap<String, Double> tubuleTropism = cellProperties.getTubuleTropism();
	HashMap<String, Set<String>> rabTropism = cellProperties.getRabTropism();
	HashMap<String, Double> mtTropism = cellProperties.getMtTropism();
	HashMap<String, Double> rabContent = new HashMap<String, Double>();
	HashMap<String, Double> membraneContent = new HashMap<String, Double>();
	HashMap<String, Double> solubleContent = new HashMap<String, Double>();
	HashMap<String, Double> initOrgProp = new HashMap<String, Double>();

	// constructor of endosomes with grid, space and a set of Rabs, membrane
	// contents,
	// and volume contents.
	public Endosome(ContinuousSpace<Object> sp, Grid<Object> gr,
			HashMap<String, Double> rabContent,
			HashMap<String, Double> membraneContent,
			HashMap<String, Double> solubleContent,
			HashMap<String, Double> initOrgProp) {
		this.space = sp;
		this.grid = gr;
		this.rabContent = rabContent;
		this.membraneContent = membraneContent;
		this.solubleContent = solubleContent;
		this.initOrgProp = initOrgProp;
		area = initOrgProp.get("area");// 4d * Math.PI * 30d * 30d; // initial
										// value, but should change
		volume = initOrgProp.get("volume");// 4d / 3d * Math.PI * 30d * 30d *
											// 30d; // initial value, but
		size = Math.pow(volume * 3d / 4d / Math.PI, (1d / 3d));
		speed = Cell.orgScale / size; // initial value, but should change
		heading = Math.random() * 360d; // initial value, but should change
		double mvb = 0; // number of internal vesicles
	}

	public ContinuousSpace<Object> getSpace() {
		return space;
	}

	@ScheduledMethod(start = 1, interval = 1)
	public void step() {
		endosomeShape(this);
		EndosomeRecycleStep.recycle(this);
		EndosomeUptakeStep.uptake(this);
		EndosomeNewFromERStep.newFromEr(this);
		EndosomeMove.changeDirection(this);
		EndosomeMove.moveTowards(this);//moveTowards(); // leave it in this class?
		EndosomeTetherStep.tether(this);
		EndosomeInternalVesicleStep.internalVesicle(this);
		EndosomeFusionStep.fusion(this);
		EndosomeSplitStep.split(this);
		EndosomeLysosomalDigestionStep.lysosomalDigestion(this);

		if (Math.random() < 0.001)
			EndosomeRabConversionStep.rabConversion(this);
		// rabConversionN();
		if (Math.random() < 0.001)
		EndosomeAntigenPresentationStep.antigenPresentation(this);
	}



	public static void endosomeShape(Endosome end) {
		double s = end.area;
		double v = end.volume;
		double rsphere = Math.pow((v * 3) / (4 * Math.PI), (1 / 3d));
		double svratio = s / v; // ratio surface volume
		double aa = rsphere; // initial a from the radius of a sphere of volume
								// v
		double cc = aa;// initially, c=a
		// calculation from s/v for a cylinder that it is the same than for an
		// ellipsoid
		// s= 2PIa^2+2PIa*2c and v = PIa^2*2c hence s/v =(1/c)+(2/a)
		for (int i = 1; i < 5; i++) {// just two iterations yield an acceptable
										// a-c ratio for plotting
			aa = 2 / (svratio - 1 / cc);// from s/v ratio
			cc = v * 3 / (4 * Math.PI * aa * aa);// from v ellipsoid
		}
		end.a = aa;
		end.c = cc;
	}


	public double getArea() {
		return area;
	}

	public double getVolume() {
		if (volume < 1.0) {
			return volume;
		}
		return volume;
	}

	public double getSpeed() {
		return speed;
	}

	public double getHeading() {
		return heading;
	}

	public HashMap<String, Double> getRabContent() {
		return rabContent;
	}

	public HashMap<String, Double> getMembraneContent() {
		return membraneContent;
	}

	public HashMap<String, Double> getSolubleContent() {
		return solubleContent;
	}

	public Endosome getEndosome() {
		return this;
	}

	/*
	 * public static Cell getInstance() { return instance; }
	 * 
	 * 
	 * public HashMap<String, Double> getRabContent() { return rabContent; }
	 * /*public HashMap<String, Double> getMembraneContent() { return
	 * membraneContent; } public HashMap<String, Double> getSolubleContent() {
	 * return solubleContent; }
	 */
	public String getMvb() {
		if (solubleContent.containsKey("mvb")) {
			if (solubleContent.get("mvb") > 0.9) {
				int i = solubleContent.get("mvb").intValue();
				return String.valueOf(i);
			} else
				return null;
		} else
			return null;

	}

	public double getRed() {
		// double red = 0.0;
		String contentPlot = CellProperties.getInstance().getColorContent()
				.get("red");

		if (membraneContent.containsKey(contentPlot)) {
			double red = membraneContent.get(contentPlot) / area;
			if (red > 1)
				System.out.println("RED FUERA ESCALA " + " " + red + " "
						+ membraneContent.get(contentPlot) + "  " + area);
			if (red > 1)
				System.out.println("RED FUERA ESCALA " + " " + contentPlot);
			// System.out.println("mHCI content" + red);
			return red;
		}
		if (solubleContent.containsKey(contentPlot)) {
			double red = solubleContent.get(contentPlot) / volume;
			// System.out.println("mHCI content" + red);
			return red;
		} else
			return 0;
	}

	public double getGreen() {
		// double red = 0.0;
		String contentPlot = CellProperties.getInstance().getColorContent()
				.get("green");

		if (membraneContent.containsKey(contentPlot)) {
			double green = membraneContent.get(contentPlot) / area;
			// System.out.println("mHCI content" + red);
			return green;
		}
		if (solubleContent.containsKey(contentPlot)) {
			double green = solubleContent.get(contentPlot) / volume;
			// System.out.println("mHCI content" + red);
			return green;
		} else
			return 0;
	}

	public double getBlue() {
		// double red = 0.0;
		String contentPlot = CellProperties.getInstance().getColorContent()
				.get("blue");

		if (membraneContent.containsKey(contentPlot)) {
			double blue = membraneContent.get(contentPlot) / area;
			if (blue > 1.1)
				System.out.println("BLUE FUERA ESCALA " + " " + blue + " "
						+ membraneContent.get(contentPlot) + "  " + area);
			if (blue > 1.1)
				System.out.println("BLUE FUERA ESCALA " + " " + contentPlot);

			return blue;
		}
		if (solubleContent.containsKey(contentPlot)) {
			double blue = solubleContent.get(contentPlot) / volume;
			if (blue > 1.1)
				System.out.println("BLUE FUERA ESCALA " + " " + blue + " "
						+ solubleContent.get(contentPlot) + "  " + area);
			if (blue > 1)
				System.out.println("BLUE FUERA ESCALA " + " " + contentPlot);
			// System.out.println("mHCI content" + red);
			return blue;
		} else
			return 0;
	}

	// Edge color coded by Rabs
	// RabA (5) Green (0,255,0)
	// RabB (22) Red (255,0,0)
	// RabC (7) Olive (128,128,0)
	// RabD (11) Blue (0,0,255)
	// RabE (5) Purple (128,0,128)
	//
	public double getEdgeRed() {
		// double red = 0.0;
		String edgePlot = CellProperties.getInstance().getColorRab().get("red");

		if (rabContent.containsKey(edgePlot)) {
			double red = rabContent.get(edgePlot) / area;
			// System.out.println("mHCI content" + red);
			return red;
		} else
			return 0;
	}

	public double getEdgeGreen() {
		// double red = 0.0;
		String edgePlot = CellProperties.getInstance().getColorRab()
				.get("green");

		if (rabContent.containsKey(edgePlot)) {
			double green = rabContent.get(edgePlot) / area;
			// System.out.println("mHCI content" + red);
			return green;
		} else
			return 0;
	}

	public double getEdgeBlue() {
		// double red = 0.0;
		String edgePlot = CellProperties.getInstance().getColorRab()
				.get("blue");

		if (rabContent.containsKey(edgePlot)) {
			double blue = rabContent.get(edgePlot) / area;
			// System.out.println("mHCI content" + red);
			return blue;
		} else
			return 0;
	}

	/*
	 * enum RabCont { RABA, RABB, RABC, RABD, RABE } public RabCont rabCont;
	 * 
	 * enum MemCont { TF, EGF, MHCI, PROT1, PROT2 } public MemCont memCont;
	 * 
	 * enum SolCont { OVA, DEXTRAN } public SolCont solCont;
	 */
	public double getSolContRab() { // (String solCont, String rab){
		Parameters params = RunEnvironment.getInstance().getParameters();
		String rab = (String) params.getValue("Rab");
		String solCont = (String) params.getValue("soluble");
		Double sc = null;
		Double rc = null;
		if (solCont != null && rab != null) {
			if (solubleContent.containsKey(solCont)) {
				sc = solubleContent.get(solCont);
			} else
				return 0;
			if (rabContent.containsKey(rab)) {
				rc = rabContent.get(rab);
			} else
				return 0;
			if (sc != null && rc != null) {
				double solContRab = sc * rc / this.area;
				return solContRab;
			}
		}
		return 0;
	}

	public double getSolContRab2() { // (String solCont, String rab){
		Parameters params = RunEnvironment.getInstance().getParameters();
		String rab = (String) params.getValue("Rab2");
		String solCont = (String) params.getValue("soluble");
		Double sc = null;
		Double rc = null;
		if (solCont != null && rab != null) {
			if (solubleContent.containsKey(solCont)) {
				sc = solubleContent.get(solCont);
			} else
				return 0;
			if (rabContent.containsKey(rab)) {
				rc = rabContent.get(rab);
			} else
				return 0;
			if (sc != null && rc != null) {
				double solContRab = sc * rc / this.area;
				return solContRab;
			}
		}
		return 0;
	}

	public double getMemContRab(String memCont, String rab) {
		double memContRab = membraneContent.get(memCont) * rabContent.get(rab)
				/ this.area;
		return memContRab;
	}

	public double getA() {
		return a;
	}

	public double getC() {
		return c;
	}

	public Grid<Object> getGrid() {
		return grid;
	}

}
