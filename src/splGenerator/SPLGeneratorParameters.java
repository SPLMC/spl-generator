package splGenerator;

public class SPLGeneratorParameters {

	private int numberOfFeatures;
	private int fragmentSize;
	private int numberOfActivities;
	private int numberOfDecisionNodes;
	private int numberOfMergeNodes;
	private int numberOfLifelines;
	private int numberOfReliabilityValues;
	private int numberOfAltFragments;
	private int numberOfLoopFragments;
	
	public int getNumberOfFeatures() {
		return numberOfFeatures;
	}

	public int getFragmentSize() {
		return fragmentSize;
	}

	public int getNumberOfActivities() {
		return numberOfActivities;
	}

	public int getNumberOfDecisionNodes() {
		return numberOfDecisionNodes;
	}

	public int getNumberOfMergeNodes() {
		return numberOfMergeNodes;
	}

	public int getNumberOfLifelines() {
		return numberOfLifelines;
	}

	public int getNumberOfReliabilityValues() {
		return numberOfReliabilityValues;
	}

	public int getNumberOfAltFragments() {
		return numberOfAltFragments;
	}

	public int getNumberOfLoopFragments() {
		return numberOfLoopFragments;
	}

	public void setNumberOfFeatures(int numberOfFeatures) {
		this.numberOfFeatures = numberOfFeatures;
	}

	public void setFragmentSize(int fragmentSize) {
		this.fragmentSize = fragmentSize;
	}

	public void setNumberOfActivities(int numberOfActivities) {
		this.numberOfActivities = numberOfActivities;
	}

	public void setNumberOfDecisionNodes(int numberOfDecisionNodes) {
		this.numberOfDecisionNodes = numberOfDecisionNodes; 
	}

	public void setNumberOfMergeNodes(int numberOfMergeNodes) {
		this.numberOfMergeNodes = numberOfMergeNodes;
	}

	public void setNumberOfLifelines(int numberOfLifelines) {
		this.numberOfLifelines = numberOfLifelines;
	}

	public void setNumberOfReliabilityValues(int numberOfReliabilityValues) {
		this.numberOfReliabilityValues = numberOfReliabilityValues;
	}

	public void setNumberOfAltFragments(int numberOfAltFragments) {
		this.numberOfAltFragments = numberOfAltFragments;
	}

	public void setNumberOfLoopFragments(int numberOfLoopFragments) {
		this.numberOfLoopFragments = numberOfLoopFragments; 
	}
}
