package splGenerator;

public class StandardConfigurationSet extends FeatureModelParameters {

	public StandardConfigurationSet() {
		mandatoryPercentage = 25;
		optionalPercentage = 25; 
		inclusiveOrPercentage = 25; 
		exclusiveOrPercentage = 25; 

		minimumBranchingFactor = 1;
		maximumBranchingFactor = 6;
		maximumGroupSize = 6;	

		numCrossTreeConstraints = 20;
		clauseDensity = 1.0f;
	}
}
