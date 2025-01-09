package immunity;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Random;

import repast.simphony.space.continuous.ContinuousSpace;
import repast.simphony.space.continuous.NdPoint;
import repast.simphony.space.grid.Grid;

public class OrganelleMove {

	private static ContinuousSpace<Object> space;
	private static Grid<Object> grid;
	private static List<MT> mts;
	public static double cellLimit = 3 * Cell.orgScale;
	
	public static void moveTowards(Endosome endosome) {

		if ( endosome.area >= Cell.minCistern/20// minimal cistern Golgi absolute Scale hacer constante
				&& isGolgi(endosome))
		{ // test if it is Golgi
//			System.out.println(endosome.heading + " INITIAL HEADING");
//			endosome.heading = -90;
			moveCistern(endosome);		
			NdPoint myPoint = space.getLocation(endosome);
			double x = myPoint.getX();
			endosome.setXcoor(x);
			double y = myPoint.getY();
			endosome.setYcoor(y);
			double z = myPoint.getZ();
			endosome.setZcoor(z);
		}
		else {
			moveNormal(endosome);
			NdPoint myPoint = space.getLocation(endosome);
			double x = myPoint.getX();
			endosome.setXcoor(x);
			double y = myPoint.getY();
			endosome.setYcoor(y);
			double z = myPoint.getZ();
			endosome.setZcoor(z);
		}
	}
	
	
	private static boolean isGolgi(Endosome endosome) {
		double areaGolgi = 0d;
		for (String rab : endosome.rabContent.keySet()){
			String name = ModelProperties.getInstance().rabOrganelle.get(rab);
			if (name.contains("Golgi")) {areaGolgi = areaGolgi + endosome.rabContent.get(rab);} 
		}
		boolean isGolgi = false;
		if (areaGolgi/endosome.area >= 0.5) {
			isGolgi = true;
		}
		return isGolgi;
	}


	private static void moveCistern(Endosome endosome) {
		double scale = Cell.orgScale;
		space = endosome.getSpace();
		grid = endosome.getGrid();		
		String maxRab = Collections.max(endosome.rabContent.entrySet(), Map.Entry.comparingByValue()).getKey();
		String organelleName = ModelProperties.getInstance().rabOrganelle.get(maxRab);
		double between = 4*scale;//distance between cisterna Math.random();
		double high = 29;//distance from the bottom
//		endosome.setHeading(-90d);// = -90d;			
//		System.out.println(endosome.heading + " final HEADING");
		double zcoor = CellBuilder.zWorld/2;
		if (organelleName.contains("cisGolgi")) {
			space.moveTo(endosome, 25, between*1+high, zcoor);
			grid.moveTo(endosome, 25, (int)(between*1+high), (int) zcoor);
		}
		if (organelleName.contains("medialGolgi")) {
			space.moveTo(endosome, 25, between*2+high, zcoor);
			grid.moveTo(endosome, 25, (int)(between*2+high), (int) zcoor);
		}
		if (organelleName.contains("transGolgi")) {
			space.moveTo(endosome, 25, between*3+high, zcoor);
			grid.moveTo(endosome, 25, (int)(between*3+high),(int) zcoor);
		}
		
	}


	public static void moveNormal(Endosome endosome) {
//		if (endosome.area > 200000) return;
		space = endosome.getSpace();
		grid = endosome.getGrid();
		/*
		 * Direction in Repast 0 to the right 180 to the left -90 down +90 up
		 * Move with random speed inversely proportional to the radius of an sphere with the endosome
		 * volume.  The speed of a small organelle of radius 20 nm is taken as unit.  
		To move, three situations are considered
		1- Near the borders, the movement is: speed random between 0 and a value that depends on the endosome size
		heading, the original heading plus a random number that depends on the momentum
		2- Away of microtubules is the same than near borders
		3- Near MT, the speed is fixed and the heading is in the direction of the Mt or 180 that of the Mt
		 */

		NdPoint myPoint = space.getLocation(endosome);
//		NdPoint myPoint = endosome.getEndosomeLocation(endosome);
		
		double x = myPoint.getX();
		double y = myPoint.getY();
		double z = myPoint.getZ();
//	If near the borders, move only with 10% probability MT independent
		/*
		 * double cellSize = CellBuilder.xWorld; double cellCenterX = cellSize/2; double
		 * cellCenterY = cellSize/2; double cellCenterZ = CellBuilder.zWorld/2; double
		 * nucleusSize = 3.5; double nucleusCenterX = cellSize/2; double nucleusCenterY
		 * = cellSize/2*0.8; double nucleusCenterZ = cellCenterZ;
		 */
//		If near the border, change heading randomly (100%) and stop move with 10% probability
		if (!isPointInEllipsoid(x, y, z))
		// cellSize- 5 cellLimit)) 
		{ // near the cell border  LARGECELL
//			endosome.headingP = Math.random()*360;
//			endosome.headingA = Math.random()*360;
//	    	System.out.println(" en el borde  " + x+"  " + y);
			changeDirectionRnd(endosome);
//		return;	
		}
//		If near the nucleus, change heading randomly (5%) and stop move with 10% probability

	
		else if (isPointInCircle(x, y, z)) { // near the nucleus
//				if (Math.random() < 0.05) {
//					endosome.headingP = Math.random()*360;
//					endosome.headingA = Math.random()*360;
//				}
				changeDirectionRnd(endosome);
				
			}
		else
//			if not near the borders
		{
//			boolean onMt = false;
			changeDirectionMt(endosome);

		}
		
//		Having the heading and speed, make the movement.  If out of the space, limit
//		the movement
			if (endosome.speed == 0) return;// random movement 90% of the time return speed=0
		    double xx = x + Math.cos(endosome.headingP * Math.PI / 180d)
			* endosome.speed*Cell.orgScale/Cell.timeScale;
		    double yy = y + Math.sin(endosome.headingP * Math.PI / 180d)
			* endosome.speed * Cell.orgScale/Cell.timeScale;
		    double zz = z + Math.sin(endosome.headingA * Math.PI / 180d)
			* endosome.speed * Cell.orgScale/Cell.timeScale;
//	    	System.out.println("coordenadas  " + xx+"  " + yy+ "  "+ zz);

//		    if move out the cell, goes to the center of the cell and change heading randomly
		    if (!isPointInEllipsoid(xx, yy, zz)) {
//		    	System.out.println("FUERA DE CELULA ANTES " + xx+"  " + yy+"  " + zz);
				double x0 = CellBuilder.xWorld/2;
				double y0 = CellBuilder.yWorld/2;
				double z0 = CellBuilder.zWorld/2;
		    	double[] newPoint = movePointToward(xx, yy, zz, x0, y0, z0, 2*cellLimit);
			    xx = newPoint[0];
			    yy = newPoint[1];
			    zz = newPoint[2];		    
			    endosome.headingP = Math.random()*360;
			    endosome.headingA = Math.random()*360;

		    	}
//	    	System.out.println("FUERA DE CELULA DESPUES " + xx+"  " + yy+"  " + zz);
		space.moveTo(endosome, xx, yy, zz);
		grid.moveTo(endosome, (int) xx, (int) yy, (int) zz);
	}
	
	public static void changeDirectionRnd(Endosome endosome) {
//		90% of the time, the speed is 0 and the endosome does not move
		if (Math.random()<0.5) {
			endosome.speed = 0;
			endosome.headingP = Math.random()*360;
			endosome.headingA = Math.random()*360;
			return;
		}
//		double initialh = endosome.heading;
//		Endosome.endosomeShape(endosome);

// when near the borders or no MT is nearby, the organelle rotates randomly
// according with i) its present heading, ii) a gaussian random number (0+- 30degree/momentum) 
//	As unit momentum I take that of a sphere of radius 20.
//	Momentum of a ellipsoid = volume*(large radius^2 + small radius^2)/5.  For the sphere or radius 20
//	4/3*PI*r^3*(20^2+20^2)/5 = 26808257/5 = 5.361.651.
//		To prevent the tubules to move, I did not consider the volume in the calculation
//		then a 20 nm sphere has a "pseudo" momentum of 800
//NEW RULE FOR RANDOM CHANGE OF HEADING
//A free rnd movement 360.  The probability decrease with size
//An inertial movement.  Gaussian arround 0 with an angle that decreases with size
//An inertial movement depending on the momentum.  Gaussian around 0 or 180

//			double momentum = (endosome.a * endosome.a + endosome.c * endosome.c)/800;
//			Random fRandom = new Random();
//			double finalh = 0;
//			finalh = finalh + fRandom.nextGaussian() * 45d/endosome.size;// inertial depending size
////			finalh = finalh + fRandom.nextGaussian() * 1d * 800d/momentum;// inertial depending momentum
//			finalh = initialh + finalh;

// The speed is random between 0 and a value inversely proportional to the endosome size
			endosome.speed = 20d/endosome.size*Math.random()* Cell.orgScale/Cell.timeScale;
			return;
		}
	
	public static void changeDirectionMt(Endosome endosome){
		if (mts == null) {
			mts = associateMt();
		}
		double mtDir = 0;
		String rabDir = "";
/*
 * mtDirection decides if the endosome is going to move to the (-) end
 * of the MT (dyneine like or to the plus end (kinesine like). -1 goes
 * to the nucleus, 1 to the PM
 * 
 */

//		Collections.shuffle(mts); 19-7-21 No need to shuffle because the closest MT will be selected
		double dist = 1000;
		MT mt = null;
//NEW		RULE 19-7-2021.  The organelle will sense the MT around it and select the closest one (minimal absolute distance)
		for (MT mmt : mts) {
			double ndist = distance(endosome, mmt);
//			The distance is in space units from 0 to 50. At scale 1, the space is 1500 nm.  At 
//			scale 0.5 it is 3000 nm.
//			Hence to convert to nm, I must multiply by 45 (2250/50) and divide by scale. An organelle will sense MT
//			at a distance less than its size.
			if (Math.abs(ndist) <= Math.abs(dist)) {
				dist = ndist; 
				mt = mmt;
			}
		}
// Check if near MT.  If it is, then move on MT according to organelle domains and if it is or not a tubule
		if (Math.abs(dist*30d/Cell.orgScale) < endosome.size) {

				if (endosome.a >endosome.c) {moveGolgiVesicles(endosome);}
				boolean isTubule = (endosome.volume/(endosome.area - 2*Math.PI*Cell.rcyl*Cell.rcyl) <=Cell.rcyl/2); // should be /2
// select a mtDir according with the domains present in the endosome.  Larger probability for the more aboundant domain
// 0 means to plus endo of MT (to PM); +1 means to the minus end of MT (to nucleus)
				rabDir = mtDirection(endosome);
//				System.out.println (dist +" distancia " + rabDir);
				if (isTubule)
					{
//					System.out.println("IS TUBULE"+ rabDir);
					mtDir = ModelProperties.getInstance().mtTropismTubule.get(rabDir);
					if (Math.random()<Math.abs(mtDir)) {
//+1 means to plus end of MT (to PM); -1 means to the minus end of MT (to nucleus)
						if (Math.signum(mtDir)>=0) {mtDir = 0;} else {mtDir = 1;}
						}
						else {
						changeDirectionRnd(endosome);
						return;
						}
					} // if no a tubule
				else
					{
					mtDir = ModelProperties.getInstance().mtTropismRest.get(rabDir);
//					System.out.println("IS NOT TUBULE"+ mtDir);
					if (Math.random()< Math.abs(mtDir)) {
//+1 means to plus end of MT (to PM); -1 means to the minus end of MT (to nucleus)
						if (Math.signum(mtDir)>=0) {mtDir = 0;} else {mtDir = 1;}
						}
						else {
						changeDirectionRnd(endosome);
						return;
						}
					}
//				Changes the heading to the heading of the MT
//				Moves the endosome to the MT position
				double mth = mt.getMtheading();
				double[] point = closestMTpoint(
						endosome.getXcoor(),endosome.getYcoor(),endosome.getZcoor(),
						mt.getXorigin(), mt.getYorigin(), mt.getZorigin(),
						mt.getXend(), mt.getYend(),mt.getZend());

				space.moveTo(endosome, point[0], point[1], point[2]);
				grid.moveTo(endosome, (int) point[0], (int) point[1], (int) point[2]);
//				dist = distance(endosome, mt);
//				Changes the speed to a standard speed in MT independet of size
				endosome.speed = 1d*Cell.orgScale/Cell.timeScale;
				endosome.headingP = -(mtDir * 180f + mt.getMtheading()+270f);
				endosome.headingA = 0;
				
//				System.out.println(endosome.speed +" speed heading "+ endosome.heading+" MTheading" + mt.getMtheading());
				return;
			}
		
//		If no Mts, then random
		else 
		{changeDirectionRnd(endosome);
		return;
		}
	}
	public static double[] closestMTpoint(double x, double y, double z, 
			double xmin, double ymin, double zmin, 
			double xmax, double ymax, double zmax) {
		// Line segment vector
		double dx = xmax - xmin;
		double dy = ymax - ymin;
		double dz = zmax - zmin;

		// Vector from segment start to point
		double px = x - xmin;
		double py = y - ymin;
		double pz = z - zmin;

		// Dot products
		double lineLengthSquared = dx * dx + dy * dy + dz * dz;
		double dotProduct = px * dx + py * dy + pz * dz;

		// Calculate projection parameter t
		double t = dotProduct / lineLengthSquared;

		// Clamp t to the range [0, 1]
		t = Math.max(0, Math.min(1, t));

		// Closest point coordinates
		double closestX = xmin + t * dx;
		double closestY = ymin + t * dy;
		double closestZ = zmin + t * dz;

		return new double[] { closestX, closestY, closestZ };
	}
	private static void moveGolgiVesicles(Endosome endosome) {
		space = endosome.getSpace();
		grid = endosome.getGrid();		
		double deltaX = Math.random()*10-5;//when near MT rnd en zona Golgi
		double deltaY = Math.random()*6-3;//when near MT rnd en zona Golgi
		double deltaZ = 4; //Math.random()*6-3;//when near MT rnd en zona Golgi

			space.moveTo(endosome, 25 + deltaX, 14 + deltaY, deltaZ);
			grid.moveTo(endosome, (int) (25 + deltaX), (int)(14 + deltaY), (int) deltaZ);
			
		NdPoint myPoint = space.getLocation(endosome);
		double x = myPoint.getX();
		endosome.setXcoor(x);
		double y = myPoint.getY();
		endosome.setYcoor(y);
		double z = myPoint.getZ();
		endosome.setZcoor(z);
		
	}


	public static String mtDirection(Endosome endosome) {
//		Picks a Rab domain according to the relative area of the domains in the organelle
//		More abundant Rabs have more probability of being selected
//		Returns the moving properties on MT of this domain 
		double rnd = Math.random();// select a random number
		double mtd = 0d;
//		Start adding the rabs domains present in the organelle until the value is larger than the random number selected
		for (String rab : endosome.rabContent.keySet()) {
			mtd = mtd + endosome.rabContent.get(rab) / endosome.area;
			if (rnd <= mtd) {
				return rab;
			}
		}
		String rab = Collections.max(endosome.rabContent.entrySet(), Map.Entry.comparingByValue()).getKey();
		return rab;// never used
	}
	public static List<MT> associateMt() {
		List<MT> mts = new ArrayList<MT>();
		for (Object obj : grid.getObjects()) {
			if (obj instanceof MT) {
				mts.add((MT) obj);
			}
		}
		return mts;
	}

	private static double distance(Endosome endosome, MT obj) {
		
		NdPoint pt = space.getLocation(endosome);
		double xP = pt.getX();
		double yP = pt.getY();
		double zP = pt.getZ();
		double xMax = (double) ((MT) obj).getXend();
		double xMin = (double) ((MT) obj).getXorigin();
		double yMax = (double) ((MT) obj).getYend();
		double yMin = (double) ((MT) obj).getYorigin();
		double zMax = (double) ((MT) obj).getZend();
		double zMin = (double) ((MT) obj).getZorigin();
    // Vector AB (line direction vector)
    double ABx = xMax - xMin;
    double ABy = yMax - yMin;
    double ABz = zMax - zMin;

    // Vector AP (vector from line point to target point)
    double APx = xP - xMin;
    double APy = yP - yMin;
    double APz = zP - zMin;

    // Cross product of AP and AB
    double crossX = APy * ABz - APz * ABy;
    double crossY = APz * ABx - APx * ABz;
    double crossZ = APx * ABy - APy * ABx;

    // Magnitudes of vectors
    double crossMagnitude = Math.sqrt(crossX * crossX + crossY * crossY + crossZ * crossZ);
    double ABmagnitude = Math.sqrt(ABx * ABx + ABy * ABy + ABz * ABz);

    // Distance formula
    return crossMagnitude / ABmagnitude;
	}
	
    public static boolean isPointInCircle(double x, double y, double z) {
		double r = CellBuilder.zWorld/2;
		double x0 = CellBuilder.xWorld/2;
		double y0 = CellBuilder.yWorld/2;
		double z0 = CellBuilder.zWorld/2;
        double distanceSquared = Math.pow(x - x0, 2) + Math.pow(y - y0, 2) + Math.pow(z - z0, 2);
        double radiusSquared = Math.pow(r, 2);
        return distanceSquared <= radiusSquared;
    }
    
    public static boolean isPointInEllipsoid(double x, double y, double z) {
		double x0 = CellBuilder.xWorld/2;
		double y0 = CellBuilder.yWorld/2;
		double z0 = CellBuilder.zWorld/2;
		
		double dx = x-x0;
		double dy = y-y0;
		double dz = z-z0;
 //       double halfSide = ll / 2.0;
        double value = (dx * dx) / (x0 * x0) + (dy * dy) / (y0 * y0) + (dz * dz) / (z0 * z0);
        // Check if the point is within the boundaries
        return value <= 1;

    }
    public static double[] movePointToward(double x, double y, double z, double x0, double y0, double z0, double d) {
        // Calculate the differences in each coordinate
        double dx = x0 - x;
        double dy = y0 - y;
        double dz = z0 - z;

        // Calculate the distance from the point to the center
        double distance = Math.sqrt(dx * dx + dy * dy + dz * dz);

        if (distance == 0) {
            // The starting point and target point are the same
            return new double[] { x, y, z };
        }

        // Calculate the ratio to scale the direction vector
        double ratio = d / distance;

        // Calculate the new coordinates
        double newX = x + ratio * dx;
        double newY = y + ratio * dy;
        double newZ = z + ratio * dz;

        return new double[] { newX, newY, newZ };
    }


}

