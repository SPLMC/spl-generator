package splGenerator;

public class Parameters {
	
	/**
	 * Model's size parameters
	 */
	public static int numberActivities;
	public static int numberOptionalFeatures; 
	public static int numberAlternativeFeatures;
	public static int messagesByFragment = 10;
	public static int numberAlternativeFragments;

	
	
	/**
	 * Models's complexity parameters
	 */
	public static int numberDecisionNodes; 
	public static int numScatteredFragments;
	public static int numReplicatedFragments;
	public static int numProbabilitiesValues;
	public static double messagesProbabilities = 0.999;
	
	
	
	/**
	 * SPL's parameters
	 */
	public static int numProducts;
}
