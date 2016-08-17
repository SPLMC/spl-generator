package splGenerator;

public class ExperimentConfigurationSet extends FeatureModelParameters {

	public ExperimentConfigurationSet() {
		mandatoryPercentage = 0;
		optionalPercentage = 100; 
		inclusiveOrPercentage = 0; 
		exclusiveOrPercentage = 0; 

		minimumBranchingFactor = 1;
		maximumBranchingFactor = Integer.MAX_VALUE;
		maximumGroupSize = 2;	

		numCrossTreeConstraints = 0;
		clauseDensity = 0.0f;
	}
}
