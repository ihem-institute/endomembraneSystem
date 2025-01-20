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

		if ( endosome.area >= Cell.minCistern/20//era 20  minimal cistern Golgi absolute Scale hacer constante
				&& isGolgi(endosome))
		{ // test if it is Golgi.  If it is and is large, behaves as a cistern and has fixed position 
//			in the Golgi area
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
//		if it is not a Golgi cistern, move as a regular organelle
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
		return areaGolgi/endosome.area >= 0.5;
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
		1- Near the borders, the movement is: speed random between 0 and a value that depends on the endosome size.
		Heading, the original heading plus a random number that depends on the momentum
		2- Away of microtubules is the same than near borders
		3- Near MT, the speed is fixed and the heading is in the direction of the Mt or 180 that of the Mt
		 */

		NdPoint myPoint = space.getLocation(endosome);
//		NdPoint myPoint = endosome.getEndosomeLocation(endosome);
		
		double x = myPoint.getX();
		double y = myPoint.getY();
		double z = myPoint.getZ();

//		If near the border, change heading randomly (100%) and stop move with 10% probability
		if (!isPointInEllipsoid(x, y, z))
		// cellSize- 5 cellLimit)) 
		{ // near the cell border  LARGECELL
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
		 if (isOccupied((int)xx, (int)yy, (int)zz, endosome)) return;   

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
	
	public static boolean isOccupied(int x, int y, int z, Endosome endosome) {
	    for (Object obj : grid.getObjectsAt(x, y, z)) {
	        if (!obj.equals(endosome)) { // Exclude the moving object
//	          System.out.println(obj + "  OCCUPIED ");       	
	            return true; // Found another object, so the cell is occupied
	        }
	    }

	    return false; // No other objects in the cell
	}
	
//	public static int isOccupied(int x, int y, int z) {
//	    Iterable<Object> objects = grid.getObjectsAt(x, y, z);
//	    int count = 0;
//
////	    System.out.println("Objects at (" + x + ", " + y + "):");
//	    for (Object obj : objects) {
//
//	        count++; // Increment the count for each object
//	    }
//        System.out.println(objects + "   "+ count); // Print each object found
//	    return count; // Return the total count of objects
//	}
	
	public static void changeDirectionRnd(Endosome endosome) {
//		1% of the time, the speed is 0 and the heading change at random
		if (Math.random()<0.01) {
			endosome.speed = 0;
			endosome.headingP = Math.random()*360;
			endosome.headingA = Math.random()*360;
			return;
		}
// The speed is random between 0 and a value inversely proportional to the endosome size
			endosome.speed = 20d/endosome.size*Math.random()* Cell.orgScale/Cell.timeScale;
			return;
		}
	
	public static void changeDirectionMt(Endosome endosome){
		if (mts == null) {
			mts = associateMt();
		}
//		Check for the nearest MT.  Each MT defines a segment, then the distance of the point to the segment
//		is calculated
		double mtDir = 0;
		String rabDir = "";
		double dist = 1000;
		MT mt = null;
//RULE 19-7-2021.  The organelle will sense the MT around it and select the closest one (minimal absolute distance)
		for (MT mmt : mts) {
			double ndist = distance(endosome, mmt);
//			The distance is in space units from 0 to 50. At scale 1, the space is 1500 nm.  At 
//			scale 0.5 it is 3000 nm.
//			Hence to convert to nm, I must multiply by 45 (2250/50) and divide by scale. An organelle will sense MT
//			at a distance less than its size.
			if (ndist <= dist) {
				dist = ndist; 
				mt = mmt;
			}
		}
// Check if near MT.  If it is, then move on MT according to organelle domains and if it is or not a tubule

		//		If far from  Mts, then random
		if (dist*30d/Cell.orgScale > 2*endosome.size) {
			changeDirectionRnd(endosome);
			return;
			
		}

//			if (isGolgi(endosome)) 
//			{
//				moveGolgiVesicles(endosome);
//				return;
//			}
//			else
			{boolean isTubule = (endosome.volume/(endosome.area - 2*Math.PI*Cell.rcyl*Cell.rcyl) <=Cell.rcyl/2); // should be /2
			// select a mtDir according with the domains present in the endosome.  Larger probability for the more aboundant domain
			// 0 means to plus endo of MT (to PM); +1 means to the minus end of MT (to nucleus)
			rabDir = mtDirection(endosome);
			//				System.out.println (dist +" distancia " + rabDir);
			if (isTubule)
			{
//									System.out.println(mtDir + "IS TUBULE "+ rabDir);
//				If near a MT, move on the MT according to the sign and to the value of the mtTropism of the tubule domain
//				the value is the probability of moving on the MT, the sign is if it will to to the + end or the -end of the MT
				mtDir = ModelProperties.getInstance().mtTropismTubule.get(rabDir);
				if (Math.random()<Math.abs(mtDir)) {
					//+1 means to plus end of MT (to PM); -1 means to the minus end of MT (to nucleus)
					if (Math.signum(mtDir)>=0) {mtDir = 0;} else {mtDir = 1;}
//					mtDir = Math.signum(mtDir);
				}
				else {
					changeDirectionRnd(endosome);
					return;
				}
//			System.out.println(mtDir + " IS TUBULE "+ rabDir);
			} // if no a tubule
			else
			{
//				If near a MT, move on the MT according to the sign and to the value of the mtTropism of the non tubule domain
//				the value is the probability of moving on the MT, the sign is if it will to to the + end or the -end of the MT

				mtDir = ModelProperties.getInstance().mtTropismRest.get(rabDir);
	//								System.out.println("IS NOT TUBULE"+ mtDir);
				if (Math.random()< Math.abs(mtDir)) {
					//+1 means to plus end of MT (to PM); -1 means to the minus end of MT (to nucleus)
					if (Math.signum(mtDir)>=0) {mtDir = 0;} else {mtDir = 1;}
//					mtDir = Math.signum(mtDir);
				}
				else {
					changeDirectionRnd(endosome);
					return;
				}
//			System.out.println(mtDir + " IS NO TUBULE "+ rabDir);
			}
			}
			//				Changes the heading to the heading of the MT
			//				Moves the endosome to the MT position
			double mth = mt.getMtheading();
			NdPoint myPoint = space.getLocation(endosome);
//			NdPoint myPoint = endosome.getEndosomeLocation(endosome);		
			double x = myPoint.getX();
			double y = myPoint.getY();
			double z = myPoint.getZ();
			
			double[] point = closestMTpoint(
					x,y,z,
					mt.getXorigin(), mt.getYorigin(), mt.getZorigin(),
					mt.getXend(), mt.getYend(),mt.getZend());

			space.moveTo(endosome, point[0], point[1], point[2]);
			grid.moveTo(endosome, (int) point[0], (int) point[1], (int) point[2]);
//			myPoint = space.getLocation(endosome);
//			NdPoint myPoint = endosome.getEndosomeLocation(endosome);		
//			x = myPoint.getX();
//			y = myPoint.getY();
//			z = myPoint.getZ();
			
//			System.out.println(x+ " final position " + y + " final position " + z);
			//				dist = distance(endosome, mt);
			//				Changes the speed to a standard speed in MT independet of size
			endosome.speed = 1d*Cell.orgScale/Cell.timeScale;
			endosome.headingP = -(mtDir * 180f + mt.getMtheading()+270f);
			endosome.headingA = 0;
//				System.out.println(endosome.rabContent +" endosome heading "+ endosome.headingP+" MTheading " + mt.getMtheading());
			return;
		}


public static double[] closestMTpoint(double x, double y, double z, 
            double xmin, double ymin, double zmin, 
            double xmax, double ymax, double zmax) {
// Line segment vector
double dx = xmax - xmin;
double dy = ymax - ymin;
double dz = zmax - zmin;

// Vector from segment start to the given point
double px = x - xmin;
double py = y - ymin;
double pz = z - zmin;

// Calculate the length squared of the segment
double lineLengthSquared = dx * dx + dy * dy + dz * dz;

// Handle edge case: if the segment is a single point
if (lineLengthSquared == 0.0) {
return new double[] { xmin, ymin, zmin };
}

// Projection of point onto the line (parameter t)
double dotProduct = px * dx + py * dy + pz * dz;
double t = dotProduct / lineLengthSquared;

// Clamp t to the range [0, 1] to stay within the segment
t = Math.max(0, Math.min(1, t));

// Compute the closest point on the segment
double closestX = xmin + t * dx;
double closestY = ymin + t * dy;
double closestZ = zmin + t * dz;

// Debugging output (optional)
//System.out.println("Closest point: (" + closestX + ", " + closestY + ", " + closestZ + ")");

return new double[] { closestX, closestY, closestZ };
}

	private static void moveGolgiVesicles(Endosome endosome) {
		double deltaX = Math.random()*6-3;//when near MT rnd en zona Golgi
		double deltaY = Math.random()*6-3;//when near MT rnd en zona Golgi
		double deltaZ = 4; //Math.random()*6-3;//when near MT rnd en zona Golgi

			space.moveTo(endosome, 25 + deltaX, 47 + deltaY, deltaZ);
			grid.moveTo(endosome, (int) (25 + deltaX), (int)(47 + deltaY), (int) deltaZ); 

			
		NdPoint myPoint = space.getLocation(endosome);
		double x = myPoint.getX();
		endosome.setXcoor(x);
		double y = myPoint.getY();
		endosome.setYcoor(y);
		double z = myPoint.getZ();
		endosome.setZcoor(z);
		System.out.println(x+ " Golgi Vesicle " + y);
		
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
	private static double distance(Endosome endosome, MT mt) {
	    // Get the coordinates of the endosome and MT segment endpoints
	    NdPoint pt = space.getLocation(endosome);
	    double xP = pt.getX();
	    double yP = pt.getY();
	    double zP = CellBuilder.zWorld/2;//pt.getZ(); No concidera la distancia en z.  El MT abarca todo el plano Z
	    double xMin = mt.getXorigin();
	    double yMin = mt.getYorigin();
	    double zMin = mt.getZorigin();
	    double xMax = mt.getXend();
	    double yMax = mt.getYend();
	    double zMax = mt.getZend();

	    // Vector AB (segment direction vector)
	    double ABx = xMax - xMin;
	    double ABy = yMax - yMin;
	    double ABz = zMax - zMin;

	    // Vector AP (vector from segment start to point)
	    double APx = xP - xMin;
	    double APy = yP - yMin;
	    double APz = zP - zMin;

	    // Dot products
	    double ABdotAB = ABx * ABx + ABy * ABy + ABz * ABz; // Segment length squared
	    double ABdotAP = ABx * APx + ABy * APy + ABz * APz; // Projection scalar

	    // Calculate projection parameter t
	    double t = ABdotAP / ABdotAB;

	    // Clamp t to the range [0, 1] to ensure it's within the segment
	    t = Math.max(0, Math.min(1, t));

	    // Closest point on the segment
	    double closestX = xMin + t * ABx;
	    double closestY = yMin + t * ABy;
	    double closestZ = zMin + t * ABz;

	    // Distance from the point to the closest point on the segment
	    double dx = xP - closestX;
	    double dy = yP - closestY;
	    double dz = zP - closestZ;
	    double distance = Math.sqrt(dx * dx + dy * dy + dz * dz);

//	    System.out.println("Distance to MT: " + distance);
	    return distance;
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
        return value <= 0.99;

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
//System.out.println(x + " xMovePoint Toward " + newX);
//System.out.println(y + " yMovePoint Toward " + newY);
//System.out.println(z + " zMovePoint Toward " + newZ);
//System.out.println(distance + " MovePoint Toward " + ratio);
        return new double[] { newX, newY, newZ };
    }


}

