package immunity.config;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class CargoDefinition {

	private final String name;
	private final CargoType type;
	private double uptakeRate;
	private double secretionRate;
	private final List<String> rabTropismTags;

	public CargoDefinition(String name, CargoType type) {
		this.name = name.trim();
		this.type = type;
		this.uptakeRate = 0d;
		this.secretionRate = 0d;
		this.rabTropismTags = new ArrayList<String>();
	}

	public CargoDefinition(String name, CargoType type, double uptakeRate, double secretionRate,
			List<String> rabTropismTags) {
		this.name = name.trim();
		this.type = type;
		this.uptakeRate = uptakeRate;
		this.secretionRate = secretionRate;
		this.rabTropismTags = new ArrayList<String>(rabTropismTags);
	}

	public String getName() {
		return name;
	}

	public CargoType getType() {
		return type;
	}

	public double getUptakeRate() {
		return uptakeRate;
	}

	public void setUptakeRate(double uptakeRate) {
		this.uptakeRate = uptakeRate;
	}

	public double getSecretionRate() {
		return secretionRate;
	}

	public void setSecretionRate(double secretionRate) {
		this.secretionRate = secretionRate;
	}

	public List<String> getRabTropismTags() {
		return Collections.unmodifiableList(rabTropismTags);
	}

	public void setRabTropismTags(List<String> rabTropismTags) {
		this.rabTropismTags.clear();
		this.rabTropismTags.addAll(rabTropismTags);
	}
}
