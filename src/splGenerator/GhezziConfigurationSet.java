package splGenerator;

public class GhezziConfigurationSet extends FeatureModelParameters {

	public GhezziConfigurationSet() {
		mandatoryPercentage = 0;
		optionalPercentage = 35; 
		inclusiveOrPercentage = 65; 
		exclusiveOrPercentage = 0; 

		minimumBranchingFactor = 0;
		maximumBranchingFactor = Integer.MAX_VALUE;
		maximumGroupSize = 2;	

		numCrossTreeConstraints = 0;
		clauseDensity = 0.0f;
	}
}
