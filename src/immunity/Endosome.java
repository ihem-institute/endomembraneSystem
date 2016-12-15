package immunity;

//import immunity.EndosomeStyle.MemCont;
//import immunity.EndosomeStyle.RabCont;
//import immunity.EndosomeStyle.SolCont;

import java.awt.Graphics;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

import repast.simphony.context.Context;
import repast.simphony.context.Context;
import repast.simphony.context.DefaultContext;
import repast.simphony.context.space.continuous.ContinuousSpaceFactory;
import repast.simphony.context.space.continuous.ContinuousSpaceFactoryFinder;
import repast.simphony.context.space.graph.NetworkBuilder;
import repast.simphony.context.space.grid.GridFactory;
import repast.simphony.context.space.grid.GridFactoryFinder;
import repast.simphony.engine.schedule.ScheduledMethod;
import repast.simphony.query.space.continuous.ContinuousWithin;
import repast.simphony.random.RandomHelper;
import repast.simphony.space.SpatialMath;
import repast.simphony.space.continuous.ContinuousSpace;
import repast.simphony.space.continuous.NdPoint;
import repast.simphony.space.grid.Grid;
import repast.simphony.space.grid.GridPoint;
import repast.simphony.util.ContextUtils;
import repast.simphony.valueLayer.GridValueLayer;


public class Endosome {
	// space
	private ContinuousSpace<Object> space;
	private Grid<Object> grid;
	// Endosomal 
	double area = 4d * Math.PI * 30d * 30d; // initial value, but should change
	double volume = 4d / 3d * Math.PI * 30d * 30d * 30d ; // initial value, but should change
	double size = Math.pow(volume * 3d / 4d / Math.PI, ( 1d / 3d ));
	double speed = 5d/size; // initial value, but should change
	double heading = Math.random() * 360d; // initial value, but should change
	double mvb = 0; //number of internal vesices
	double cellMembrane = 0;
	HashMap<String, Double> cellRab = new HashMap<String, Double>();
	//ArrayList<Element> areaElement = new ArrayList<Element>();
	//ArrayList<Element> volumeElement = new ArrayList<Element>();
	private List<MT> mts;
	HashMap<String, Double> rabCompatibility = new HashMap<String, Double>();
	HashMap<String, Double> tubuleTropism = new HashMap<String, Double>();
	HashMap<String, Double> rabContent = new HashMap<String, Double>();
	HashMap<String, Double> membraneContent = new HashMap<String, Double>();
	HashMap<String, Double> solubleContent = new HashMap<String, Double>();
	// constructor of endosomes with grid, space and a set of area elements (contents)
	// I need to add a set of volume contents.
	public	Endosome (ContinuousSpace<Object> sp, Grid<Object> gr, HashMap<String, Double> rabContent, 
			HashMap<String, Double> membraneContent, HashMap<String, Double> solubleContent) {
		this.space = sp;
		this.grid = gr;
		this.rabContent = rabContent;
		this.membraneContent = membraneContent;
		this.solubleContent = solubleContent;
		rabCompatibility.put("RabARabA", 1.0d);
		rabCompatibility.put("RabARabB", 0.1d);
		//rabCompatibility.put("RabBRabA", 0.1d);
		rabCompatibility.put("RabBRabB", 1.0d);
		tubuleTropism.put("RabA",0.5d);
		tubuleTropism.put("RabB",0.1d);
		tubuleTropism.put("Tf", 1.0d);
		tubuleTropism.put("dextran",0.0d);
		tubuleTropism.put("mvb", 0.0d);
		/* TODO: agregar todas las combinaciones.  
		No s� por qu� tienen que estar
		aqu�. Me da error si las saco fuera del constructur y en realidad ser�an
		propiedades de la cellula y no de los endosomas*/
		this.heading = Math.random() * 360d;

		cellMembrane = 0;
		cellRab.put("RabA", 0d);
	}
	private double distance(Endosome endosome, MT obj) {
			
			// If the line passes through two points P1=(x1,y1) and P2=(x2,y2) then the distance of (x0,y0) from the line is:
			NdPoint pt = space.getLocation(this);
			double xpt = pt.getX();
			double ypt = pt.getY();
			double ymax = (double)((MT) obj).getYend();
			double ymin = (double)((MT) obj).getYorigin();
			double xmax = (double)((MT) obj).getXend();
			double xmin = (double)((MT) obj).getXorigin();
	//		double a = Math.abs((ymax-ymin) * xpt - (xmax-xmin)*ypt + xmax*ymin - ymax*xmin);
			double a = Math.abs((xmax-xmin) * (ymin - ypt) - (ymax -ymin) * (xmin - xpt));
			double b = Math.sqrt((ymax-ymin)*(ymax-ymin) + (xmax-xmin)*(xmax-xmin));
			double distance = a/b;
			//System.out.println("distance");
			//System.out.println(distance);
			return distance;
		}
	@ScheduledMethod(start = 1, interval = 1)
	public void step() {
		Recycle();
		size();
		changeDirection();
		moveTowards();
		fusion();
		//printEndosomes();
		split();
		internalVesicle();
		//rabConversion();

	}
	
	public void printEndosomes(){
			List<Endosome> endosomes = new ArrayList<Endosome>();
			for (Object obj : grid.getObjects()) {
				if (obj instanceof Endosome) {
					endosomes.add((Endosome) obj);
					}
				}
			for (Endosome endosome1: endosomes){
				 //System.out.println(endosome1.rabContent+" " + endosome1.membraneContent+" " + endosome1.solubleContent);		
			}
	}
	
	private void rabConversion() {
		RabConversion rabConversion = RabConversion.getInstance();
		
		rabConversion.setInitialConcentration("RabA", 1);
    	
    	// run time course
    	rabConversion.runTimeCourse();
    	
    	double rabA = rabConversion.getConcentration("RabA");
    	double rabB = rabConversion.getConcentration("RabB");
	}

	public List<MT> associateMt(){
		List<MT> mts = new ArrayList<MT>();
		for (Object obj : grid.getObjects()) {
			if (obj instanceof MT) {
				mts.add((MT) obj);
				}
			}
		 // System.out.println(mts);
		return mts;
		
		}

	public double changeDirection(){
		if (Math.random()< 0.60) {
		return this.heading;
		}
		if (Math.random()< 0.60) {
			this.heading = (this.heading + (0.5d - Math.random()) * 90d *30d / size())% 360;
		return this.heading;
		}
		if (mts == null) {
			mts = associateMt();
		}
		for (MT mt : mts){
			double dist = distance(this, mt);
			if(dist < this.size / 30d){
				this.heading =  -mt.getMtheading()+ 180f;
				NdPoint myPoint = space.getLocation(this);
				double x = myPoint.getX() + Math.cos((heading - 90)* 2d * Math.PI / 360d) * dist;
				double y = myPoint.getY() + Math.sin((heading - 90) * 2d * Math.PI / 360d) * dist;
				if (y >50 || y < 0){
					heading = - heading;
					y = 0;
					x = 0;
							
				}
			space.moveTo(this, x, y);
			grid.moveTo(this, (int)x, (int)y);
				
			//this.heading =  -mt.getMtheading()+ 180f;
			
			//System.out.println("headingMT");  
			//System.out.println(this.heading);
			return this.heading;
		}
		}
		return this.heading;
	}
	
	public double size(){
		 double rsphere = Math.pow(this.volume * 3d / 4d / Math.PI, ( 1d / 3d ));
		 double size = rsphere; // cellscale ;calculate size proportional to volume (radius of sphere with this volume)
	return size;
	}
	//TRAIDO DE RABS.  NO SE LLAMA. NO FUNCIONA
	private double getCompatibility(String rabX, String rabY) {
		if(!rabCompatibility.containsKey(rabX+rabY)&& !rabCompatibility.containsKey(rabY+rabX))return 0;
			if(rabCompatibility.containsKey(rabX+rabY)) {
				//System.out.println("COMPATIB");
				//System.out.println(rabCompatibility.get(rabX+rabY));
				return rabCompatibility.get(rabX+rabY);
				
			} else {

				return rabCompatibility.get(rabY+rabX);
			}
	}
	
	private boolean compatibles(Endosome endosome1, Endosome endosome2) {
		double sum = 0;
		for (String key1 : endosome1.rabContent.keySet()) {
			for (String key2 : endosome2.rabContent.keySet()) {
				double comp = getCompatibility(key1, key2) * endosome1.rabContent.get(key1)/endosome1.area * endosome2.rabContent.get(key2)/endosome2.area;
				sum = sum + comp;
			}
		}
		//System.out.println("probabFusion");
		//System.out.println(sum + " "+ endosome1.rabContent + "  " + endosome2.rabContent);
		return Math.random() < sum;
	}
	// HASTA AC�

	public void fusion() {
		GridPoint pt = grid.getLocation(this);
		List<Endosome> endosomes_to_delete = new ArrayList<Endosome>();
		Iterable<Object> objectsAt = grid.getObjectsAt(pt.getX(), pt.getY());
		for (Object obj : objectsAt) {
			if (obj instanceof Endosome && obj != this) {	
				if( compatibles(this, (Endosome) obj) ) { //COMO SE HACE, NO ME FUNCIONA
					endosomes_to_delete.add((Endosome) obj);
				}
				  //System.out.println(endosomes_to_delete);
			}
		}
		for (Endosome endosome : endosomes_to_delete) {
			this.volume = this.volume + endosome.volume;
			this.area = this.area + endosome.area;
			this.rabContent = sumRabContent(this, endosome);
			this.membraneContent = sumMembraneContent(this, endosome);
			this.solubleContent = sumSolubleContent(this, endosome);
			Context<Object> context = ContextUtils.getContext(endosome);
			context.remove(endosome);
		}
		  double size = size();
		  this.speed = 5/size;
		
	}
	
	private HashMap<String, Double> sumRabContent(Endosome endosome1, Endosome endosome2) {
		//HashMap<String, Double> mapSum1 = new HashMap<String, Double>();
		for (String key1 : endosome1.rabContent.keySet()) {
				if(endosome2.rabContent.containsKey(key1)) {
				double sum = endosome1.rabContent.get(key1) + endosome2.rabContent.get(key1);
				endosome1.rabContent.put(key1, sum);
				}
		}
		for (String key2 : endosome2.rabContent.keySet()) {
				if(!endosome1.rabContent.containsKey(key2)) {
					endosome1.rabContent.put(key2, endosome2.rabContent.get(key2));
					}
			}
		  //endosome1.rabContent = mapSum1;
		  //System.out.println("rabContentSum" + endosome1.rabContent);
		  return endosome1.rabContent;
	}
	

	private HashMap<String, Double> sumMembraneContent(Endosome endosome1, Endosome endosome2) {
	//	HashMap<String, Double> mapSum2 = new HashMap<String, Double>();
		for (String key1 : endosome1.membraneContent.keySet()) {
				if(endosome2.membraneContent.containsKey(key1)) {
				double sum = endosome1.membraneContent.get(key1) + endosome2.membraneContent.get(key1);
				endosome1.membraneContent.put(key1, sum);
				}
		}
		for (String key2 : endosome2.membraneContent.keySet()) {
				if(!endosome1.membraneContent.containsKey(key2)) {
					double sum = endosome2.membraneContent.get(key2);
					endosome1.membraneContent.put(key2, sum);
					}
			}
		  //endosome1.membraneContent = mapSum2;
		  //System.out.println("rabMembraneSum" + endosome1.membraneContent);
		  return endosome1.membraneContent;
	}
	

	private HashMap<String, Double> sumSolubleContent(Endosome endosome1, Endosome endosome2) {
			//HashMap<String, Double> mapSum3 = new HashMap<String, Double>();
			for (String key1 : endosome1.solubleContent.keySet()) {
					if(endosome2.solubleContent.containsKey(key1)) {
					double sum = endosome1.solubleContent.get(key1) + endosome2.solubleContent.get(key1);
					endosome1.solubleContent.put(key1, sum);
					}
			}
			for (String key2 : endosome2.solubleContent.keySet()) {
					if(!endosome1.solubleContent.containsKey(key2)) {
						double sum = endosome2.solubleContent.get(key2);
						endosome1.solubleContent.put(key2, sum);
						}
				}
			  //endosome1.solubleContent = mapSum3;
			 // System.out.println("solubleContentSum" + endosome1.solubleContent);
			  return endosome1.solubleContent;
		}
	

			
	public void moveTowards() {
		// only move if we are not already in this grid location

			NdPoint myPoint = space.getLocation(this);
			double x = myPoint.getX() + Math.cos(heading * 2d * Math.PI / 360d) * this.speed;
			double y = myPoint.getY() + Math.sin(heading * 2d * Math.PI / 360d) * this.speed;
			if (y >50 || y < 0){
				heading = - heading;
				return;
			}
		space.moveTo(this, x, y);
		grid.moveTo(this, (int)x, (int)y);
	}
	
	// Split the endosome in two
	public void split(){
		String rabInTube = null;
		double vo = this.volume;
		double so = this.area;
		if (vo < 3 * 2 * Math.PI * Cell.rcyl * Cell.rcyl * Cell.rcyl)return; //if too small do not split. Volume of a cylinder of 2 cylinder radius long (almost a sphere)
		if (so < 2 * Cell.mincyl) return; // if the surface is less than two minimus tubules, abort splitting
		if (so * so * so / (vo * vo) <= 36.001 * Math.PI) return; // organelle is an sphere
		 //if s^3 / v^2 is equal to 36*PI then it is an sphere and cannot form a tubule		
		if (vo /(so - 2 * Math.PI * Cell.rcyl * Cell.rcyl)<=Cell.rcyl/2)return;
		//too small volume for the surface for a tubule.  Cannot form a tubule
		if (vo / (so - 2 * Math.PI * Cell.rcyl * Cell.rcyl) == Cell.rcyl / 2){
			System.out.println("tubule");
			//return;	// NEED TO GENERATE A SUBRUTINE TO SPLIT TUBULE-TUBULE
			//SUBRUTINE SHOULD SEND BACK VCYLINDER AND SCYLINDER-DONE.  The same procedures is used for both
		}

		// TRANSFORM IN A SUBRUTINE TO SPLIT VESICLE-TUBULE
		// procedure to split an organelle which is between a sphere (that cannot be splitted) and a cylinder, that follow other rules.
	  //calculate surface of smallest sphere

	  double rsphere = Math.pow((vo * 3) / (4 * Math.PI) , (1/3d)) ;// calculate the radius of the sphere with a given volume
	  //System.out.println("vsphere");
	  //System.out.println(vsphere);
	  //System.out.println("rsphere");
	  //System.out.println(rsphere);
	  double ssphere = (4 * Math.PI * rsphere * rsphere) ;//area of a sphere containing the volume
	  if (so - ssphere < Cell.mincyl) return; // if not enough surface to contain the volume plus a mininal tubuel, no split
	  rabInTube = rabInTube(); //select a rab for the tubule
	  if (rabInTube == null) return; // if non is selected, no fission
	  //System.out.println("rabInTube" + rabInTube);

	  /*initial minimum tubule to be formed*/
	  double scylinder = Cell.mincyl; // surface minimum cylinder 2*radius cylinder high
	  double vcylinder = 2 * Math.PI* Math.pow(Cell.rcyl,  3); // volume minimum cylinder

	  while ((so - ssphere - scylinder > 4 * Math.PI* Math.pow(Cell.rcyl,  2)) //organelle area should be enough to cover the volume (ssphere
			  								// to cover the cylinder already formed (scylinder) and to elongate a two r cylinder (without caps)
			&& (rabContent.get(rabInTube) - scylinder > 4 * Math.PI* Math.pow(Cell.rcyl,  2))// the Rab area should b enough to cover
											// the minimum cylinder and to elongate a two r cylider
			&& (scylinder < 0.5 * so) // the area of the cylinder must not be larger than 50% of the total area
			&& ((vo-vcylinder-2 * Math.PI* Math.pow(Cell.rcyl,  3))/((so - scylinder-4 * Math.PI* Math.pow(Cell.rcyl,  2)) - 2 * Math.PI * Cell.rcyl * Cell.rcyl)> Cell.rcyl/2)){ //volume left cannot be smaller than the volume of the mincyl	
		/*while there is enough membrane and enough rab surface, the tubule grows*/
		//System.out.println("in while" + (vo-vcylinder-2 * Math.PI* Math.pow(Cell.rcyl,  3))/((so - scylinder-4 * Math.PI* Math.pow(Cell.rcyl,  2)) - 2 * Math.PI * Cell.rcyl * Cell.rcyl));
		  scylinder = scylinder + 4 * Math.PI* Math.pow(Cell.rcyl,  2);// add a cylinder without caps (the caps were considered in the mincyl
		  vcylinder = vcylinder + 2 * Math.PI* Math.pow(Cell.rcyl,  3);// add a volume
		  //System.out.println(scylinder +"surface and volume"+ vcylinder);
	  }
	  //System.out.println("after while" + (vo-vcylinder)/((so - scylinder) - 2 * Math.PI * Cell.rcyl * Cell.rcyl));
		
	  /*the volume of the vesicle is formed subtracting the volume of the formed cylinder from the total volume
	   * idem for the area */
	  //SUBRUTINE SHOULD SEND BACK VCYLINDER AND SCYLINDER
	  
	  /*From the information of vcylinder and scylinder, the organelle is splitted in two, 
	   * a sphere and tubule (case 2) or in two almost tubules 
	   * (a pice of the lateral surface must be used to close the
	   * tubules*/
	  double vVesicle = vo - vcylinder;
	  double sVesicle = so - scylinder;
	  if (vVesicle/(sVesicle - 2*Math.PI*Cell.rcyl*Cell.rcyl) < 0.9999*Cell.rcyl/2) {
			 System.out.println("voluem 0000000000000000000000000000000000000000000000"+(vVesicle/(sVesicle - 2*Math.PI*Cell.rcyl*Cell.rcyl)));
				  }

	
	  /*the final volume and surface of the vesicle are tested.  If too small, the split aborts*/
//	  if ((vVesicle < 2 * Math.PI * Cell.rcyl * Cell.rcyl * Cell.rcyl)||(sVesicle < Cell.mincyl)) return; 
	 
	  
	  /*FORMATION 1ST ORGANELLE (referred as sphere)
	   * the rab-in-tubule of the tubule is substracted from the original rab-in-tube content of the organelle
	   * the final proportion of the rab-in-tubule in the vesicular organelle is obtained dividein by the total 
	   * surface of the vesicle*/
	  this.area = sVesicle;
	  this.volume = vVesicle;
	  /*CONTENT DISTRIBUTION
	   * Rab in the tubule is sustracted  */
	  double rabLeft = this.rabContent.get(rabInTube) - scylinder;
	  if (rabLeft < 0) {
		  System.out.println(rabInTube + this.rabContent.get(rabInTube)+ "surfaceCy"+ scylinder);
		  System.out.println(rabContent);
	  }
	  this.rabContent.put(rabInTube, rabLeft);
	  // MEMBRANE CONTENT IS DISTRIBUTED according to three possibilities
	  // 0.5-even distribution, 1-tubule tropism, 0-sphere tropism 
	  HashMap<String, Double> copyMembrane = new HashMap<String, Double>();
	  copyMembrane.putAll(this.membraneContent);
	  for (String content : copyMembrane.keySet()){
		  if (tubuleTropism.get(content)== 0.5){
			  this.membraneContent.put(content, copyMembrane.get(content)* (sVesicle)/so);
		  }
		  if (tubuleTropism.get(content)== 1){
			  if (copyMembrane.get(content)> scylinder){
				  this.membraneContent.put(content, copyMembrane.get(content)-scylinder);				  
			  }
			  else this.membraneContent.put(content, 0.0d);
			  //if (this.membraneContent.get(content)>this.area){
				//  System.out.println("this-redddddddddddddddddddddddddddd"+this.membraneContent.get(content)/this.area);
			  //}
		  }
		  if (tubuleTropism.get(content)== 0){
			  if (copyMembrane.get(content)> sVesicle){			  	
				  this.membraneContent.put(content, sVesicle);				  
			  }
			  else this.membraneContent.put(content, copyMembrane.get(content));  
		  }
		  
	  }
	// SOLUBLE CONTENT IS DISTRIBUTED according to three possibilities
		  // 0.5-even distribution, 1-tubule tropism, 0-sphere tropism 
		  HashMap<String, Double> copySoluble = new HashMap<String, Double>();
		  copySoluble.putAll(this.solubleContent);
		  for (String content : copySoluble.keySet()){
			  if (tubuleTropism.get(content)== 0.5){
				  this.solubleContent.put(content, copySoluble.get(content)* (vVesicle)/vo);
			  }
			  if (tubuleTropism.get(content)== 1){
				  if (copySoluble.get(content)> vcylinder){
					  this.solubleContent.put(content, copySoluble.get(content)-vcylinder);				  
				  }
				  else this.solubleContent.put(content, 0.0d);
			  }
			  if (tubuleTropism.get(content)== 0){
				  if (copySoluble.get(content)> vVesicle){
					  this.solubleContent.put(content, vVesicle);				  
				  }
				  else this.solubleContent.put(content, copySoluble.get(content));  
			  }
			  
		  }

	 // System.out.println("vesicula1");
	 // System.out.println(this.area+ "ddd"+ this.rabContent);
	  this.speed = 5/size();
		moveTowards();
	//	moveTowards();

	  /*the tubule is created as an independent endosome*/
	  Endosome b = new Endosome(this.space, this.grid, null, null, null);
	  Context<Object> context = ContextUtils.getContext(this);
	  context.add(b) ;
	  b.area = scylinder;
	  b.volume = vcylinder;
	  //if (vcylinder < 1){
		//	 System.out.println("voluem 0000000000000000000000000000000000000000000000");
	  //}
	  HashMap<String, Double> map = new HashMap<String, Double>();
	  b.rabContent = map;
	  //b.membraneContent = map;
	  //b.solubleContent = map;
	  b.rabContent.put(rabInTube, scylinder);
	  HashMap<String, Double> map1 = new HashMap<String, Double>();
	  b.membraneContent = map1;
	  for (String content : copyMembrane.keySet()){
		  b.membraneContent.put(content, copyMembrane.get(content) - this.membraneContent.get(content));
	  if (b.membraneContent.get(content)>b.area){
	//	  System.out.println("redddddddddddddddddddddddddddd"+b.membraneContent.get(content)/b.area);
  
	  }
	  }
	  HashMap<String, Double> map2 = new HashMap<String, Double>();
	  b.solubleContent = map2;
	  for (String content : copySoluble.keySet()){
		  b.solubleContent.put(content, copySoluble.get(content)-this.solubleContent.get(content));
	  }
	  b.size = Math.pow(b.volume * 3d / 4d / Math.PI, ( 1d / 3d ));// radius of a sphere with the volume of the cylinder 
	  b.speed = 5/b.size;
	  b.heading = this.heading + 180;// contrary to the vesicle heading
	 // System.out.println("VESICLE B");
	  //System.out.println(b.area +" "+ b.rabContent +" "+ b.membraneContent +" "+ b.solubleContent);	  
	  NdPoint myPoint = space.getLocation(this);
		double x = myPoint.getX();
		double y = myPoint.getY();
	space.moveTo(b, x, y);
	grid.moveTo(b, (int)x, (int)y);
	moveTowards();
//	moveTowards();
	
}

	public String rabInTube(){
		HashMap<String, Double> copyMap = new HashMap<String, Double>();
		copyMap.putAll(this.rabContent);
		String rab = null;
		//System.out.println("CopyMap "+copyMap);
		for (String rab1 : this.rabContent.keySet()){
			if (copyMap.get(rab1) < Cell.mincyl ){
				copyMap.remove(rab1);
			}
		}
			if (copyMap.size()== 0){
				 System.out.println("NINGUN RAB"+copyMap);
						return null;
								}

			if (copyMap.size() < 2) {

			for (String rab1 : copyMap.keySet()){
				// System.out.println("UNICO RAB "+copyMap);
				return rab1;
				}
			}
		
			 else {
			 while (rab == null){
				 for (String rab1 : copyMap.keySet()){
					 if (Math.random() < tubuleTropism.get(rab1)){
						// System.out.println(copyMap + "RabInTubeSelected" + rab1);	
						 return rab1;
					 }
				 }
			 }
			 }
			
			return null;
	}
	public void internalVesicle() {
		
		//System.out.println("original endosome"+area+ " " + volume + "original endosome rabs" + this.rabContent);	
		//System.out.println("original Rab Cellular Content" + cellRab);
		double vo = this.volume;
		double so = this.area;
		//if (vo < 2 * Math.PI * Cell.rcyl * Cell.rcyl * Cell.rcyl)return; //if too small do not split. Volume of a cylinder of 2 cylinder radius long (almost a sphere)
		//if (so < 2 * Cell.mincyl) return; // if the surface is less than two minimus tubules, abort splitting
		if (so * so * so / (vo * vo) <= 36.001 * Math.PI) return;
			 //System.out.println("ESFERA" + so * so * so / (vo * vo));	
		 //if s^3 / v^2 is equal to 36*PI then it is an sphere and cannot form a tubule		
		
		double rIV = 15; //Internal vesicle radius 
		double vIV  = 4 / 3 * Math.PI * Math.pow(rIV, 3); //volume  14137.16
		double sIV = 4 * Math.PI * Math.pow(rIV, 2);// surface 2827.43

		if (vo < 2 * vIV) return;
		if (so < 2 * sIV) return;
		double vp = vo + vIV;
		double sp = so - sIV;
		if (sp * sp * sp / (vp * vp) <= 36 * Math.PI) return;// if the resulting surface cannot embrance the resulting volume
		double rsphere = Math.pow((0.75 * vp / Math.PI), (1 / 3));
		double ssphere = 4 * Math.PI * Math.pow(rsphere, 2);
		if (ssphere >= sp ) return;
		//double scylinder = so - ssphere;
		//if (scylinder <  sIV * 1.10) return;//if the available membrane is less than the surface of an internal vesicle, stop.
		                                    //the factor 1.01 is to account for an increase in the surface required because the increase in volume
		this.area = this.area - sIV;
		this.volume = this.volume + vIV;
		if (solubleContent.containsKey("mvb")){
			double content = this.solubleContent.get("mvb") + 1;
			this.solubleContent.put("mvb", content);}
		else {
			this.solubleContent.put("mvb", 1d);	
		}
		cellRab =Cell.getInstance().getRabCell();
		for (String key1 : this.rabContent.keySet()) {
			if(cellRab.containsKey(key1)) {
				double sum = this.rabContent.get(key1)*sIV/so + cellRab.get(key1);
					cellRab.put(key1, sum);
					}
					else {
						double sum = this.rabContent.get(key1)*sIV/so;
						cellRab.put(key1, sum);
					}
			}
		
		Cell.getInstance().setRabCell(cellRab);
		System.out.println("Rab Cellular Content" + Cell.getInstance().getRabCell());
		for (String rab : this.rabContent.keySet()){
			double content1 = this.rabContent.get(rab)* (so - sIV) / so;
			this.rabContent.put(rab, content1);
			}
		for (String content : this.membraneContent.keySet()){
			double mem = this.membraneContent.get(content)* (so - sIV) / so;
			this.membraneContent.put(content, mem);
			}
		 //System.out.println("Left in endosome" +area+ "  " +volume+ "Rab left in endosome" + rabContent);
		cellMembrane = 	Cell.getInstance().gettMembrane() + sIV;
		Cell.getInstance().settMembrane(cellMembrane);
		System.out.println("total cell membrane" + Cell.getInstance().gettMembrane());

	}
	public void Recycle(){
		  NdPoint myPoint = space.getLocation(this);
			//double x = myPoint.getX();
			double y = myPoint.getY();
			if (y < 49 || !rabContent.containsKey("RabA")) return;
			double RabA = rabContent.get("RabA")/area;
			if (RabA < 0.99) return; // if not near the PM
												// or without a recycling Rab
												// return
			else {
				//RECYCLE
				//Recycle membrane content
				HashMap<String, Double> membraneRecycle =Cell.getInstance().getMembraneRecycle();
				for (String key1 : this.membraneContent.keySet()) {
					if(membraneRecycle.containsKey(key1)) {
						double sum = membraneRecycle.get(key1) + membraneContent.get(key1);
							membraneRecycle.put(key1, sum);
							}
							else {
								membraneRecycle.put(key1, membraneContent.get(key1));
							}
					}
				
				Cell.getInstance().setMembraneRecycle(membraneRecycle);
				this.membraneContent.clear();
				System.out.println("membrane Recycled" + Cell.getInstance().getMembraneRecycle());

				
				
				//Recycle soluble content
				HashMap<String, Double> solubleRecycle =Cell.getInstance().getSolubleRecycle();
				for (String key1 : this.solubleContent.keySet()) {
					if(solubleRecycle.containsKey(key1)) {
						double sum = solubleRecycle.get(key1) + solubleContent.get(key1);
						solubleRecycle.put(key1, sum);
							}
							else {
								solubleRecycle.put(key1, solubleContent.get(key1));
							}
					}
				Cell.getInstance().setSolubleRecycle(solubleRecycle);
				this.solubleContent.clear();
				System.out.println("soluble Recycled" + Cell.getInstance().getSolubleRecycle());
			}
	}
		
	public double getArea() {
		return area;
	}

	public double getVolume() {
		if (volume < 1.0){
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
	public String getRabContent() {
		return rabContent.toString();
	}

	/*
	public HashMap<String, Double> getRabContent() {
		return rabContent;
	}
	/*public HashMap<String, Double> getMembraneContent() {
		return membraneContent;
	}
	public HashMap<String, Double> getSolubleContent() {
		return solubleContent;
	}
	*/
	public String getMvb() {
		if (solubleContent.containsKey("mvb")){
				if (solubleContent.get("mvb")>0 ){
			int i = solubleContent.get("mvb").intValue();
			return String.valueOf(i);
				}
				else return "";
		}
		else return "";

	}
	public int getRed(){
		int red = 0;
		String memPlot = "Tf";
		if (membraneContent.containsKey(memPlot)){
		double rd = membraneContent.get(memPlot)/area;
		red = (int)(255 * Math.pow(rd, (1/1)));
		//System.out.println("red " + red);
		return red;
		}
		else return 0;
	}
	public int getGreen(){
		int green = 0;
		String rabPlot = "RabA";
		if (rabContent.containsKey(rabPlot)){
		double gr = rabContent.get(rabPlot)/area;
		green = (int)(255 * Math.pow(gr, (1/1)));
		//System.out.println("green " + green);
		return green;
		}
		else return green;
	}

	public int getBlue(){
		int blue = 0;
		String solPlot = "dextran";
		if (solubleContent.containsKey(solPlot)){
		double bl = solubleContent.get(solPlot)/volume;
		blue = (int)(255 * Math.pow(bl, (1/1)));
		//System.out.println("blue " + blue);
		return blue;
		}
		else return blue;
	}
	/*
	 enum RabCont {
		RABA, RABB, RABC, RABD, RABE
	}
	public RabCont rabCont;
	
		 enum MemCont {
		TF, EGF, MHCI, PROT1, PROT2
	}
	public MemCont memCont;
	
		 enum SolCont {
		OVA, DEXTRAN
	}
	public SolCont solCont;
	*/
	
}


