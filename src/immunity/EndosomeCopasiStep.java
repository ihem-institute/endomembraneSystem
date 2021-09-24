package immunity;
import java.util.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.Collections;
import org.COPASI.CModel;
import org.COPASI.CTimeSeries;
import org.apache.commons.lang3.StringUtils;

import repast.simphony.engine.environment.RunEnvironment;

public class EndosomeCopasiStep {
	
	public static void antPresTimeSeriesLoad(Endosome endosome){
		int tick = (int) RunEnvironment.getInstance().getCurrentSchedule().getTickCount();

		if (endosome.getEndosomeTimeSeries().isEmpty()){			
			callLipidPresentation(endosome);
			timeSeriesLoadintoEndosome(endosome);

			return;
		} 
		if (tick >= Collections.max(endosome.getEndosomeTimeSeries().keySet())) {
			timeSeriesLoadintoEndosome(endosome);
			endosome.getEndosomeTimeSeries().clear();
			callLipidPresentation(endosome);

			return;
			}
		if (!endosome.endosomeTimeSeries.containsKey(tick)) {
//			System.out.println("Return without UPDATED");
			return;
		}else {

			timeSeriesLoadintoEndosome(endosome);
			return;

		}
	}
	public static void timeSeriesLoadintoEndosome(Endosome endosome){
//		values in endosomeTimeSeries are in mM.  Transform back in area and volume units multiplying
//		by area the membrane metabolites and by volume the soluble metabolites
		int tick = (int) RunEnvironment.getInstance().getCurrentSchedule().getTickCount();
//		TreeMap< Integer, HashMap<String, Double>> orderedValues = new TreeMap<Integer ,HashMap<String, Double>>(endosome.endosomeTimeSeries);
		HashMap<String, Double> presentValues = new HashMap<String, Double>(endosome.endosomeTimeSeries.get(tick));
		HashMap<String, Double> pastValues = new HashMap<String, Double>();
//		The release of metabolites into cytosol from a time series must consider that it is a "delta".
//		Supose a metabolite is not modified by the copasi, if the value in the time series is added, the
//		concentration will artificially build up. At time zero of the series, the delta is zero for all the 
//		metabolites.
		int pastTick = 0;
		if (tick == endosome.endosomeTimeSeries.firstKey()){ ;// first tick in the time series
		pastValues = presentValues;
		pastTick = tick;
		} else {
			pastTick = endosome.endosomeTimeSeries.lowerKey(tick);
			pastValues = endosome.endosomeTimeSeries.get(pastTick);
		}
		
		for (String met :presentValues.keySet()){
			// if the content is cytosolic, increase the cell pull proportinal to the volume.  The content is
			//			eliminated from endosome
			String met1 = met;//.substring(0, met.length()-2);
			if (met.endsWith("En") && ModelProperties.getInstance().solubleMet.contains(met1)) {
				double metValue = presentValues.get(met)* endosome.volume;
				endosome.solubleContent.put(met1, metValue);
			}
			else if (met.endsWith("En") && ModelProperties.getInstance().membraneMet.contains(met1)) {
				double metValue = presentValues.get(met)* endosome.area;
				endosome.membraneContent.put(met1, metValue);
			}
//			metabolites in the Cell are expressed in concentration. I am using the area ratio between PM and Cell 
//			for dilution of the metabilite that is released into the cell.  I may use volume ratio?
//			Only a fraction of the metabolite in the cell participates
//			in copasi, hence the concentration are added to the existing values.
//			Since the endosome is releasing Cyto metabolites at each tick, what must be incorporated is the delta with respect to the previous tick.
//			At tick = 0, nothing is released (pastValues = presentValues)
			else if (met.endsWith("Cy")){
				 if (!Cell.getInstance().getSolubleCell().containsKey(met1)){Cell.getInstance().getSolubleCell().put(met1, 0.0);}
	//			 System.out.println("TICK " + met+tick + "\n " + pastTick + "\n " + presentValues.get(met) + "\n " + pastValues.get(met) + "\n" + endosome
	//			 );
				double delta =  presentValues.get(met) - pastValues.get(met);
				double metValue = Cell.getInstance().getSolubleCell().get(met1)
						+ delta * endosome.area/Cell.getInstance().getCellArea();
				Cell.getInstance().getSolubleCell().put(met1, metValue);
//			 System.out.println("SOLUBLE CELL " + Cell.getInstance().getSolubleCell()+ " MET " + met1);
				//				endosome.solubleContent.remove(met1);
			}
//			Only a fraction of the metabolite in the plasma membrane participates
//			in copasi, hence the concentration are added to the existing values.
//			Ask if the metabolite is soluble or membrane associated.  If is not in PM set to zero the metabolite
//			Since the endosome is releasing PM metabolites at each tick, what must be incorporated is the delta with respect to the previous tick.
//			At tick = 0, nothing is released (pastValues = presentValues)
			else if (met.endsWith("Pm") && ModelProperties.getInstance().getSolubleMet().contains(met1)) {
				 if (!PlasmaMembrane.getInstance().getSolubleRecycle().containsKey(met1))PlasmaMembrane.getInstance().getSolubleRecycle().put(met1, 0.0);
				 double delta =  presentValues.get(met) - pastValues.get(met);
				 double metValue = PlasmaMembrane.getInstance().getSolubleRecycle().get(met1)
						+ delta * endosome.volume;
				PlasmaMembrane.getInstance().getSolubleRecycle().put(met1, metValue);
			}
			else if (met.endsWith("Pm") && ModelProperties.getInstance().getMembraneMet().contains(met1)) {
				 if (!PlasmaMembrane.getInstance().getMembraneRecycle().containsKey(met1))PlasmaMembrane.getInstance().getMembraneRecycle().put(met1, 0.0);
				 double delta =  presentValues.get(met) - pastValues.get(met);
				 double metValue = PlasmaMembrane.getInstance().getMembraneRecycle().get(met1)
						+ delta * endosome.area;
				PlasmaMembrane.getInstance().getMembraneRecycle().put(met1, metValue);
			}
		}
		
	}
	
	public static void callLipidPresentation(Endosome endosome) {
// Membrane and soluble metabolites are transformed from the area an volume units to mM.
// From my calculations (see Calculos), dividing these units by the area or the volume of the endosome, transform the 
//the values in mM.  Back from copasi, I recalculate the values to area and volume
//
		EndosomeCopasi lipidMetabolism = EndosomeCopasi
				.getInstance();

		Set<String> metabolites = lipidMetabolism.getMetabolites();
		HashMap<String, Double> localM = new HashMap<String, Double>();
		for (String met : metabolites) {
			String met1 = met;//met.substring(0, met.length()-2);
//			for endosomes and other organelles, all the metabolites participate in the reaction
			if (met.endsWith("En") && endosome.membraneContent.containsKey(met1)) {
				double metValue = endosome.membraneContent.get(met1)/endosome.area;
//				double a = sigFigs(metValue, 5);
//				System.out.println(metValue +" REDONDEO "+ a);
				lipidMetabolism.setInitialConcentration(met, sigFigs(metValue,6));
				localM.put(met, metValue);
			} else if (met.endsWith("En") && endosome.solubleContent.containsKey(met1)) {
				double metValue = Math.abs(endosome.solubleContent.get(met1))/endosome.volume;
				lipidMetabolism.setInitialConcentration(met, sigFigs(metValue,6));
				localM.put(met, metValue);
//				for metabolites in the plasma membrane, only a fraction participate in the reaction and it is consumed 
//				for soluble metabolites, proportional to the volume and for membrane metabolites proportional to the area
			} else if (met.endsWith("Pm") && PlasmaMembrane.getInstance().getMembraneRecycle().containsKey(met1)) {
				double metValue = PlasmaMembrane.getInstance().getMembraneRecycle().get(met1)/PlasmaMembrane.getInstance().getPlasmaMembraneArea();
				double metLeft = metValue* (PlasmaMembrane.getInstance().getPlasmaMembraneArea() - endosome.area);
				PlasmaMembrane.getInstance().getMembraneRecycle().put(met1, metLeft);
				lipidMetabolism.setInitialConcentration(met, sigFigs(metValue,6));
				localM.put(met, metValue);
			} else if (met.endsWith("Pm") && PlasmaMembrane.getInstance().getSolubleRecycle().containsKey(met1)) {
				double metValue = Math.abs(PlasmaMembrane.getInstance().getSolubleRecycle().get(met1))/PlasmaMembrane.getInstance().getPlasmaMembraneVolume();
				double metLeft = metValue* (PlasmaMembrane.getInstance().getPlasmaMembraneVolume() - endosome.volume);
				PlasmaMembrane.getInstance().getSolubleRecycle().put(met1, metLeft);
				lipidMetabolism.setInitialConcentration(met, sigFigs(metValue,6));
				localM.put(met, metValue);
//				Rabs are included only for reactions that occurs in a specific compartment. 
			} else if (met.startsWith("Rab") && endosome.rabContent.containsKey(met)) {
				double metValue = Math.abs(endosome.rabContent.get(met))/endosome.area;
				lipidMetabolism.setInitialConcentration(met, sigFigs(metValue,6));
				localM.put(met, metValue);
//				for metabolites in the cell, only a fraction participate in the reaction and it is consumed 
//				metabolites in the cell are in concentration units and only soluble metabolites are considered
			} else if (met.endsWith("Cy") && Cell.getInstance().getSolubleCell().containsKey(met1)) {
				double metValue = Cell.getInstance().getSolubleCell().get(met1);
//				System.out.println(Cell.area + "volume cell "+Cell.volume);
				double metLeft = metValue*(Cell.getInstance().getCellVolume() - endosome.volume)/(Cell.getInstance().getCellVolume());
				Cell.getInstance().getSolubleCell().put(met1, metLeft);
				lipidMetabolism.setInitialConcentration(met, sigFigs(metValue,6));
				localM.put(met, metValue);
			} else {
				lipidMetabolism.setInitialConcentration(met, 0.0);
				localM.put(met, 0.0);
			}
		}
//		lipidMetabolism.setInitialConcentration("protonCy", 1e-04);
//		localM.put("protonCy", 1e-04);
//		System.out.println("METABOLITES IN "+ localM);

//		if (localM.get("proton")==null||localM.get("proton") < 1e-05){
//			lipidMetabolism.setInitialConcentration("proton", 1e-04);
//			localM.put("proton", 1e-04);
//		}


		lipidMetabolism.runTimeCourse();
		

		CTimeSeries timeSeries = lipidMetabolism.getTrajectoryTask().getTimeSeries();
		int stepNro = (int) timeSeries.getRecordedSteps();
		int metNro = metabolites.size();
		double initpepMHC = 0d;
		double finalpepMHC = 0d;
		int tick = (int) RunEnvironment.getInstance().getCurrentSchedule().getTickCount();
		for (int time = 0; time < stepNro; time = time + 1){
			HashMap<String, Double> value = new HashMap<String, Double>();
			for (int met = 1; met < metNro +1; met = met +1){
				if (timeSeries.getTitle(met).equals("pepMHCIEn")){
						if (time == 1 ) initpepMHC = timeSeries.getConcentrationData(time, met);
						else if (time == stepNro - 1 ) finalpepMHC = timeSeries.getConcentrationData(time, met);
				}
				value.put(timeSeries.getTitle(met), sigFigs(timeSeries.getConcentrationData(time, met),6));
				endosome.getEndosomeTimeSeries().put((int) (tick+time*Cell.timeScale/0.03),value);
			}
		}
		if(initpepMHC > 0 || finalpepMHC > 0) {
//		System.out.println(initpepMHC +" ENDOSOME WITH pepMHC "+ finalpepMHC + endosome.toString());
		endosome.complexMHC = finalpepMHC;
		endosome.assembleMHC = finalpepMHC - initpepMHC;
		}		
		}
	public static double sigFigs(double n, int sig) {
//		if (Math.abs(n) < 1E-20) return 0d;
//		else 
//		{
		double mult = Math.pow(10, sig - Math.floor(Math.log(n) / Math.log(10) + 1));
	    return Math.round(n * mult) / mult;
//	    }
	}
	
	
}

