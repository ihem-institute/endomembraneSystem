package immunity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Random;

import repast.simphony.query.space.continuous.ContinuousWithin;
import repast.simphony.query.space.grid.GridCell;
import repast.simphony.query.space.grid.GridCellNgh;
import repast.simphony.space.continuous.ContinuousSpace;
import repast.simphony.space.grid.Grid;
import repast.simphony.space.grid.GridPoint;
import repast.simphony.util.ContextUtils;

public class EndosomeTetherStep {

		private static ContinuousSpace<Object> space;
		private static Grid<Object> grid;
		
		public static void tether (Endosome endosome) {
//			HashMap<String, Double> rabContent = new HashMap<String, Double>(endosome.getRabContent());
//			HashMap<String, Double> membraneContent = new HashMap<String, Double>(endosome.getMembraneContent());
//			HashMap<String, Double> solubleContent = new HashMap<String, Double>(endosome.getSolubleContent());
			space = endosome.getSpace();
			grid = endosome.getGrid();
//			double cellLimit = 3 * Cell.orgScale;

		GridPoint pt = grid.getLocation(endosome);
		// I calculated that the 50 x 50 grid is equivalent to a 750 x 750 nm
		// square
		// Hence, size/15 is in grid units
		int gridSize = (int) Math.round(endosome.size*Cell.orgScale / 15d);
//		double gridSize = endosome.size*Cell.orgScale / 15d;
//		List<Endosome> endosomes_to_delete = new ArrayList<Endosome>();
//		ContinuousWithin<Endosome> endosomeList = new ContinuousWithin<Endosome>(ContextUtils.getContext(endosome), endosome, gridSize);
////		Iterable<Endosome> endosomes = endosomeList.query();
//		List<Endosome> endosomes = new ArrayList<>();
//		for (Object obj : endosomeList.query()) {
//		    if (obj instanceof Endosome) {
//		        endosomes.add((Endosome) obj);
//		    }
//		}
//		
//		for (Endosome end : endosomes) {
////			System.out.println(end + "  ENDOSOMAS EN EL RADIO DE ACCION " + endosome);
//
//			if ( (end.volume <= endosome.volume)
//					&& (EndosomeAssessCompatibility.compatibles(endosome, end))) {
//				endosomes_to_delete.add(end);
//			}
//		}
		
//		List<Endosome> endosomesToTether = new ArrayList<Endosome>();	
//		for (Endosome end : endosomes) {
//			if (EndosomeAssessCompatibility.compatibles(endosome, (Endosome) end)) 
//				endosomesToTether.add(end);
//			}
//			
		
		
		GridCellNgh<Endosome> nghCreator = new GridCellNgh<Endosome>(grid, pt,
				Endosome.class, gridSize, gridSize, gridSize);
		// System.out.println("SIZE           "+gridSize);

		List<GridCell<Endosome>> cellList = nghCreator.getNeighborhood(true);
		if (cellList.size()<2)return;//if only one return
		List<Endosome> endosomesToTether = new ArrayList<Endosome>();
		for (GridCell<Endosome> gr : cellList) {
			// include all endosomes
			for (Endosome end : gr.items()) {
				if (EndosomeAssessCompatibility.compatibles(endosome, (Endosome) end)) {
					endosomesToTether.add(end);
				}
			}
		}
		
		// new list with just the compatible endosomes (same or compatible rabs)
		if (endosomesToTether.size()<2)return; //if only one, return
		// select the largest endosome
		Endosome largest = endosome;
		for (Endosome end : endosomesToTether) {
//			if (endosome.getRabContent().containsKey("RabA")) System.out.println(endosome.size+" "+end.size+ " " + endosome.getRabContent() + " " + end.getRabContent());
			if (end.size > largest.size) {
				largest = end;
			}
		}
		
//		System.out.println(endosome.size+" "+largest.size);
		
		// assign the speed and heading of the largest endosome to the gropu

		for (Endosome end : endosomesToTether) {

//			Random r = new Random();		
//			double rr = r.nextGaussian();
			end.headingP = largest.headingP; //rr * 3d + 
			end.headingA = largest.headingA;//rr * 3d + 
	//		OrganelleMove.moveTowards(end);
		}
	}
	
}
