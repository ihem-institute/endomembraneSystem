package immunity.config;

import java.util.LinkedHashMap;
import java.util.Map;

public class RabDomainDefinition {

	private final String rabId;
	private String displayName;
	private double initCellAmount = 1d;
	private double tubuleTropism = 1d;
	private double mtTropismTubule = 0d;
	private double mtTropismRest = 0d;
	private double rabRecyProb = 0d;
	private final Map<String, Double> maturationTo = new LinkedHashMap<String, Double>();

	public RabDomainDefinition(String rabId) {
		this.rabId = rabId.trim();
	}

	public String getRabId() {
		return rabId;
	}

	public String getDisplayName() {
		return displayName;
	}

	public void setDisplayName(String displayName) {
		this.displayName = displayName == null ? "" : displayName.trim();
	}

	public double getInitCellAmount() {
		return initCellAmount;
	}

	public void setInitCellAmount(double initCellAmount) {
		this.initCellAmount = initCellAmount;
	}

	public double getTubuleTropism() {
		return tubuleTropism;
	}

	public void setTubuleTropism(double tubuleTropism) {
		this.tubuleTropism = tubuleTropism;
	}

	public double getMtTropismTubule() {
		return mtTropismTubule;
	}

	public void setMtTropismTubule(double mtTropismTubule) {
		this.mtTropismTubule = mtTropismTubule;
	}

	public double getMtTropismRest() {
		return mtTropismRest;
	}

	public void setMtTropismRest(double mtTropismRest) {
		this.mtTropismRest = mtTropismRest;
	}

	public double getRabRecyProb() {
		return rabRecyProb;
	}

	public void setRabRecyProb(double rabRecyProb) {
		this.rabRecyProb = rabRecyProb;
	}

	public Map<String, Double> getMaturationTo() {
		return maturationTo;
	}

	public static String compatibilityKey(String rabA, String rabB) {
		if (rabA.compareTo(rabB) <= 0) {
			return rabA + rabB;
		}
		return rabB + rabA;
	}
}
