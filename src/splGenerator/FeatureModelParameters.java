package splGenerator;

public abstract class FeatureModelParameters {

	/**
	 * Set of constants for describing configuration set previously created
	 */
	public static final int STANDARD_FEATURE_MODEL = 0;
	public static final int GHEZZI_FEATURE_MODEL = 1;

	/**
	 * Variable Feature Model's parameters, according to SPLAR generator
	 */
	protected int mandatoryPercentage;
	protected int optionalPercentage; 
	protected int inclusiveOrPercentage; 
	protected int exclusiveOrPercentage; 

	protected int minimumBranchingFactor;
	protected int maximumBranchingFactor;
	protected int maximumGroupSize;	

	protected int numCrossTreeConstraints;
	protected float clauseDensity;
	protected final int modelConsistency = 1; 


	
	/**
	 * This method is responsible for returning objects
	 * 
	 * @param configurationSet
	 * @return
	 */
	public static FeatureModelParameters getConfiguration(int configurationSet) {
		FeatureModelParameters answer = null;
		switch (configurationSet) {
		case GHEZZI_FEATURE_MODEL:
			answer = new GhezziConfigurationSet();
			break;

		case STANDARD_FEATURE_MODEL: 
			answer = new StandardConfigurationSet(); 
			break; 
			
		default:
			break;
		}
		return answer;
	}



	public int getMandatoryFeatures() {
		return mandatoryPercentage;
	}



	public int getOptionalFeatures() {
		return optionalPercentage;
	}



	public int getInclusiveOrFeatures() {
		return inclusiveOrPercentage;
	}



	public int getExclusiveOrFeatures() {
		return exclusiveOrPercentage;
	}



	public int getMininumBranchingFeatures() {
		return minimumBranchingFactor;
	}



	public int getMaximumBranchingFeatures() {
		return maximumBranchingFactor;
	}



	public int getMaximumGroupSize() {
		return maximumGroupSize;
	}



	public int getNumCrossTreeConstraints() {
		return numCrossTreeConstraints;
	}



	public float getClauseDensity() {
		return clauseDensity;
	}



	public int getModelConsistency() {
		return modelConsistency;
	}




}
