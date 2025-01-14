package immunity;

import java.util.Collections;
import java.util.Map;

public class EndosomeAssessCompatibility {
	
	public static boolean compatibles(Endosome endosome1, Endosome endosome2) {
		double sum = 0;
		for (String key1 : endosome1.rabContent.keySet()) {
			for (String key2 : endosome2.rabContent.keySet()) {
				double comp = getCompatibility(key1, key2)
						* endosome1.rabContent.get(key1) / endosome1.area
						* endosome2.rabContent.get(key2) / endosome2.area;
				sum = sum + comp;
			}
		}
		// compatibility is a value between 0 and 1. Fusion
		// occurs with a probability proportional to the compatibility
		String maxRab = Collections.max(endosome1.rabContent.entrySet(), Map.Entry.comparingByValue()).getKey();
		if (maxRab.equals("RabD")) {
//			System.out.println( "compatibilidad RabD " + sum);
//			sum = 1;
		}
		return Math.random() < sum;
	}

	private static double getCompatibility(String rabX, String rabY) {
		ModelProperties modelProperties = ModelProperties.getInstance();
		if (!modelProperties.rabCompatibility.containsKey(rabX + rabY)
				&& !modelProperties.rabCompatibility.containsKey(rabY + rabX))
			return 0;
		if (modelProperties.rabCompatibility.containsKey(rabX + rabY)) {
			// System.out.println("COMPATIB");
			// System.out.println(rabCompatibility.get(rabX+rabY));
			return modelProperties.rabCompatibility.get(rabX + rabY);

		} else {

			return modelProperties.rabCompatibility.get(rabY + rabX);
		}
	}

}
