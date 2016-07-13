package splGenerator;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;

import splGenerator.Util.SPLFilePersistence;
import splGenerator.Util.ValuesGenerator;
import splar.apps.generator.FMGeneratorEngine;
import splar.core.constraints.CNFFormula;
import splar.core.fm.FeatureModel;
import splar.core.fm.FeatureTreeNode;

public class SplGenerator {

	/**
	 * Constants used by the SPL models generator
	 */
	// Constants for choosing which FM generator will be employed
	public static final int GHEZZIGENERATOR = 0;
	public static final int SPLOT = 1;

	// Constants for choosing which topology resemblance between FM and UML
	// behavioral models will be used
	public static final int SYMMETRIC = 0;
	// add constants for new methods here...

	private static SplGenerator instance = null;

	private ReanaFeatureModel reanaFm;
	private splar.core.fm.FeatureModel splarFm;
	private ConfigurationKnowledge ck;

	/**
	 * GENERATORS GENERAL PARAMETERS The following attributes represent the
	 * general attributes of the SPL generator. Here we set the folders and
	 * files names used by the generator.
	 */
	private String modelsPath = "/home/andlanna/workspace2/reana/src/splGenerator/generatedModels/";
	private String fmFilePrefix = "fm_";
	private String umlFilePrefix = "uml_";

	/**
	 * FEATURE MODEL GENERAL PARAMETERS The following object contains all the
	 * parameters defined for Feature Models that will be created. Such object
	 * is created by the FeatureModelParameters class such that a specific
	 * feature model parameters setting is created according to the input value
	 * passed by parameter.
	 */
	private FeatureModelParameters fmParameters = null;

	/**
	 * The attributes below regard to the Model's parameters, i.e., they
	 * represent the values of each parameter for a especific model.
	 */
	private int numberOfFeatures;
	private int fragmentSize = 10; // =numberOfMessages/fragments
	private int numberOfActivities;
	private int numberOfDecisionNodes;
	private int numberOfMergeNodes;
	private int numberOfLifelines;
	private int numberOfReliabilityValues;
	private int numberOfAltFragments;
	private int numberOfLoopFragments;

	/**
	 * Indexes used by the behavioral models generator
	 */
	private int idxModel;
	private int idxFeature;
	// Indexes for activity diagram elements
	private int idxActTransition;
	private int idxDecisionNode;
	private int idxMergeNode;
	// Indexes for sequence diagram elements
	private int idxLifeline;
	private int idxMessage;

	private static int idxActivity = 0;
	private static int idxSequenceDiagram = 0;
	private static int idxFragment = 0;

	/**
	 * Lists of elements created during the behavioral models generation
	 */
	private HashSet<Activity> setOfActivities;
	private List<Fragment> listOfPendingFragments;
	private List<Feature> listOfPendingFeatures;

	private SplGenerator() {
		ck = new ConfigurationKnowledge();

		idxModel = 0;
		idxFeature = 0;
//		idxActivity = 0;
		idxActTransition = 0;
		idxDecisionNode = 0;
		idxMergeNode = 0;
//		idxSequenceDiagram = 0;
		idxLifeline = 0;
//		idxFragment = 0;
		idxMessage = 0;

		listOfPendingFragments = new LinkedList<Fragment>();
		listOfPendingFeatures = new LinkedList<Feature>();
	}

	/**
	 * This method is responsible for generating the artifacts of a software
	 * product line in an automated fashion. When it is called, its results is
	 * an SPL object containing references for a FeatureModel object
	 * representing the SPL's feature model, an ActivityDiagram object
	 * containing representing the coarse-grained behavior of a software product
	 * line which activities are refined into their respective sequence diagrams
	 * and ConfigurationKnowledge object representing the configuration
	 * Knowledge of the software product line.
	 * 
	 * @param fmGenerationMethod
	 *            The parameter value represents the FM generation method which
	 *            will be employed by the generator. The values it may assume
	 *            are defined by the constants GHEZZIGENERATOR and
	 *            SPLOTGENERATOR.
	 * 
	 * @param modelsCorrespondence
	 *            The parameter value represents the resemblance between feature
	 *            model and behavioral models. The SYMMETRIC value means the
	 *            FM's topology is used as basis for creating the SPL's
	 *            behavioral models.
	 * 
	 * @return the SPL object representing the whole software product line, and
	 *         its artifacts.
	 */
	public SPL generateSPL(int fmGenerationMethod, int modelsCorrespondence) {
		// 1st step: generate the feature model of the whole Software Product
		// Line, as the generation method choosed.
		FeatureModel fm = (FeatureModel) generateFeatureModel(fmGenerationMethod);
		FeatureTreeNode root = fm.getRoot();
		SPLFilePersistence.FM2JavaCNF(fm);

		// 2nd step: create the UML behavioral elements according to the
		// parameters defined by the user.
		// creating the sequence diagrams elements before creating the sequence
		// diagrams
		ValuesGenerator.generateRandomReliabilityValues(numberOfLifelines);
		for (int i = 0; i < numberOfLifelines; i++) {
			SequenceDiagramElement e;
			e = SequenceDiagramElement
					.createElement(SequenceDiagramElement.LIFELINE, "Lifeline"
							+ idxLifeline++);
			((Lifeline) e)
					.setReliability(ValuesGenerator.getReliabilityValue());
		}

		// creating the fragments of the sequence diagram, one fragment by
		// feature
//		System.out.println("|listOfPendingFragments| = " + listOfPendingFragments.size());
		for (int i = 0; i < fm
				.countFeatures(FeatureModel.SOLITAIRE_AND_GROUPED); i++) {
			Fragment f = (Fragment) Fragment.createElement(
					SequenceDiagramElement.FRAGMENT, "Fragment_"
							+ idxFragment++);
			f.setType(Fragment.OPTIONAL);
			listOfPendingFragments.add(f);
		}
//		System.out.println("|listOfPendingFragments| = " + listOfPendingFragments.size());

		// 3rd step: create the activity diagram describing the coarse-grained
		// behavior of the software product line.
		ActivityDiagram ad = generateActivityDiagramStructure();

		// 4th step: assign the initial elements to the SPL object
		SPL spl = SPL.createSPL("SPL_model_" + idxModel++);
		spl.setFeatureModel(fm);
		spl.setActivityDiagram(ad);

		// 5th step: generate the behavioral models.
		SequenceDiagram sdRoot = null;
		if (modelsCorrespondence == SplGenerator.SYMMETRIC) {
			if (ad.getSetOfActivities().size() == 1) {
				// in case there's only one activity in activity diagram, its
				// associated sequence
				// diagram respects fully the FM's structure
				Fragment frRoot = symmetricModelsCreation(root);
				sdRoot = frRoot.getSequenceDiagrams().getFirst();
				Activity a = ad.getSetOfActivities().get(0);
				a.addSequenceDiagram(sdRoot);
			} else {
				// in case the number of activities is different of the number
				// of first-level features,
				// the first-level features will be assigned randomly for one
				// activity, such as the
				// resulting sequence diagram follows Feature Model's topology.
				List<FeatureTreeNode> remainingFeatures = fm.getNodesAtLevel(1);
				int actDiag = 0;
				while (remainingFeatures.size() > 0) {
					// associating features to "additional" activities
					Random r = new Random();
					int j = r.nextInt(remainingFeatures.size());
					FeatureTreeNode f = remainingFeatures.remove(j);
					Fragment frFeature = symmetricModelsCreation(f);
					Activity a = ad.getActivityByName("Activity_" + actDiag);
					a.getSequenceDiagrams().getFirst().addFragment(frFeature);
					actDiag = (actDiag + 1) % ad.getSetOfActivities().size();
				}
				sdRoot = randomSequenceDiagram("SD_" + idxSequenceDiagram++,
						"true");
			}
		}
		spl.setSplGenerator(this);
		return spl;
	}

	/**
	 * This method creates a sequence diagram whose structure formed by the
	 * which compose them, resembles to the sub-tree of the feature model for
	 * the feature passed by parameter. Therefore, if a leaf node is passed by
	 * argument, a single and basic sequence diagram inside a fragment is
	 * returned. If an intermediate feature of the feature model is passed by
	 * parameter, a sequence diagram representing the structure of the feature's
	 * subtree is returned, such that each feature has an associated fragment
	 * that contains its sequence diagram (basic in case of leaf features,
	 * variable in case of other intermediate features). If the root node is
	 * passed by the parameter, the returned sequence diagram's structure
	 * resembles the topology of the whole feature model.
	 * 
	 * @param feature
	 *            the feature diagram passed by parameter.
	 * @return the sequence diagram structure randomly generated.
	 */
	private Fragment symmetricModelsCreation(FeatureTreeNode feature) {
		Fragment fr = randomFragment();
		SequenceDiagram sd = randomSequenceDiagram(
				"SD_" + idxSequenceDiagram++, feature.getName());
		fr.addSequenceDiagram(sd);
		ck.associateArtifact(feature, sd);

		for (int i = 0; i < feature.getChildCount(); i++) {
			FeatureTreeNode fc = (FeatureTreeNode) feature.getChildAt(i);
			Fragment frc = symmetricModelsCreation(fc);
			// insert it on a random position of root's sequence diagram
			int position = randomPosition(sd);
			sd.getElements().add(position, frc);
		}
		return fr;
	}

	/**
	 * This method is a factory method that returns an instance of SplGenerator
	 * class.
	 * 
	 * @return a new instance of SplGenerator class
	 */
	public static SplGenerator newInstance() {
		instance = new SplGenerator();
		return instance;
	}

	/**
	 * Method for setting the number of feature that will be present at the
	 * generated Feature Model
	 * 
	 * @param numFeatures
	 *            - number of features to be created
	 */
	public void setNumberOfFeatures(int numFeatures) {
		this.numberOfFeatures = numFeatures;
	}

	public void setFragmentSize(int fragSize) {
		this.fragmentSize = fragSize;
	}

	/**
	 * Method for setting the number of activities that will be present at the
	 * activity diagram describing the coarse-grained behavior of the software
	 * product line.
	 * 
	 * @param numActivities
	 *            - number of activities to be created.
	 */
	public void setNumberOfActivities(int numActivities) {
		this.numberOfActivities = numActivities;
	}

	/**
	 * Method for setting the number of decision nodes that will be part of the
	 * activity diagram describing the coarse-grained behavior of the software
	 * product line.
	 * 
	 * @param numDecisionNodes
	 *            - number of decision nodes.
	 */
	public void setNumberOfDecisionNodes(int numDecisionNodes) {
		this.numberOfDecisionNodes = numDecisionNodes;
	}

	/**
	 * Method for setting the number of lifelines that will be used by all
	 * sequence diagrams that will be generated for the software product line.
	 * 
	 * @param numLifelines
	 *            - number of lifeline that will be created.
	 */
	public void setNumberOfLifelines(int numLifelines) {
		this.numberOfLifelines = numLifelines;
	}

	/**
	 * This method is responsible for creating all the elements of the
	 * behavioral models and assembling them in a correct manner.
	 * 
	 * @param fm
	 *            - The feature model associated with the Software Product Line
	 * @return spl - the SPL object containing the Feature and Behavioral models
	 *         of the software product line.
	 */
	public SPL generateBehavioralModel(ReanaFeatureModel fm) {

		SPL spl = SPL.createSPL("SPL_model_" + idxModel++);
		ActivityDiagram ad = generateActivityDiagramStructure();
		spl.setActivityDiagram(ad);

		// creating the sequence diagrams elements before creating the sequence
		// diagrams
		for (int i = 0; i < numberOfLifelines; i++) {
			SequenceDiagramElement
					.createElement(SequenceDiagramElement.LIFELINE, "Lifeline"
							+ idxLifeline++);
		}

		// generate the Sequence Diagram related to the Root feature
		Feature root = fm.getRoot();
		SequenceDiagram sdRoot = randomSequenceDiagram("root", "true");
		System.out.println(sdRoot.toString());
		ck.associateArtifact(root, sdRoot);

		// creating the fragments of the sequence diagram
		for (int i = 0; i < numberOfFeatures; i++) {
			Fragment f = (Fragment) Fragment.createElement(
					SequenceDiagramElement.FRAGMENT, "Fragment_"
							+ idxFragment++);
			f.setType(Fragment.OPTIONAL);
			listOfPendingFragments.add(f);
		}

		for (int i = 0; i < listOfPendingFragments.size(); i++) {
			Fragment fr = randomFragment();
			Feature f = randomFeature();
			SequenceDiagram sd = randomSequenceDiagram("SD_"
					+ idxSequenceDiagram++, f.getName());
			fr.addSequenceDiagram(sd);
			ck.associateArtifact(f, sd);

			// insert it on a random position of root's sequence diagram
			int position = randomPosition(sdRoot);
			sdRoot.getElements().add(position, fr);
		}

		Activity a = ad.getActivityByName("Activity_0");
		System.out.println(a);
		a.addSequenceDiagram(sdRoot);

		return spl;
	}

	private int randomPosition(SequenceDiagram seqDiag) {
		int tam = seqDiag.getElements().size();
		Random ran = new Random();
		int position = ran.nextInt(tam);
		return position;
	}

	private Fragment randomFragment() {
		Random ran = new Random();
		// System.out.println("|listOfPendingFragments|="
		// + listOfPendingFragments.size());
		int i = ran.nextInt(listOfPendingFragments.size());
		Fragment f = listOfPendingFragments.remove(i);
		return f;
	}

	private Feature randomFeature() {
		Feature answer = null;
		if (listOfPendingFeatures.size() > 0) {
			Random ran = new Random();
			int i = ran.nextInt(listOfPendingFeatures.size());
			answer = listOfPendingFeatures.remove(i - 1);
		} else {
			// TODO Alterar depois!!!!
			reanaFm.getRoot();
		}
		return answer;
	}

	/**
	 * This method creates a Sequence Diagram randomly according to the
	 * parameters defined for the SPLGenerator object.
	 * 
	 * @param name
	 *            - the name of the sequence diagram
	 * @param guard
	 *            - the guard condition expressed by propositional logical
	 *            formula, described in terms of feature's names.
	 * @return Sequence Diagram object randomly generated.
	 */
	private SequenceDiagram randomSequenceDiagram(String name, String guard) {
		int idxAltFragments = 0;
		int idxLoopFragments = 0;

		SequenceDiagram sd = SequenceDiagram.createSequenceDiagram(name, guard);
		Lifeline source = randomLifeline();
		// System.out.println("Fragment size = " + fragmentSize);
		for (int i = 0; i < fragmentSize; i++) {
			Lifeline target = randomLifeline();
			sd.createMessage(source, target, Message.SYNCHRONOUS, "T"
					+ idxActTransition++, target.getReliability());
			source = target;
		}

		// include alt fragments into a random position
		while (idxAltFragments < numberOfAltFragments) {
			Random r = new Random();
			int position = r.nextInt(fragmentSize);
			Fragment f = createAlternativeFragment(name + "_alt_"
					+ idxAltFragments);
			sd.getElements().add(position, f);
			idxAltFragments++;
		}

		// include loop fragments into a random position
		while (idxLoopFragments < numberOfLoopFragments) {
			Random r = new Random();
			int position = r.nextInt(fragmentSize);
			Fragment f = createLoopFragment(name + "_loop_" + idxAltFragments);
			sd.getElements().add(position, f);
			idxLoopFragments++;
		}

		return sd;
	}

	private Fragment createLoopFragment(String name) {
		Fragment f = (Fragment) SequenceDiagramElement.createElement(
				SequenceDiagramElement.FRAGMENT, "");
		f.setType(Fragment.LOOP);

		SequenceDiagram sd = SequenceDiagram
				.createSequenceDiagram(name, "true");
		Lifeline source = randomLifeline();
		for (int i = 0; i < fragmentSize; i++) {
			Lifeline target = randomLifeline();
			sd.createMessage(source, target, Message.SYNCHRONOUS, "T"
					+ idxActTransition++, target.getReliability());
			source = target;
		}
		f.addSequenceDiagram(sd);
		return f;
	}

	private Fragment createAlternativeFragment(String name) {
		Fragment f = (Fragment) SequenceDiagramElement.createElement(
				SequenceDiagramElement.FRAGMENT, "");
		f.setType(Fragment.ALTERNATIVE);

		SequenceDiagram sd = SequenceDiagram
				.createSequenceDiagram(name, "true");
		Lifeline source = randomLifeline();
		for (int i = 0; i < fragmentSize; i++) {
			Lifeline target = randomLifeline();
			sd.createMessage(source, target, Message.SYNCHRONOUS, "T"
					+ idxActTransition++, target.getReliability());
			source = target;
		}
		f.addSequenceDiagram(sd);
		return f;
	}

	private Lifeline randomLifeline() {
		Random ran = new Random();
		int i = ran.nextInt(numberOfLifelines);
		Lifeline l = (Lifeline) Lifeline.getElementByName("Lifeline" + i);
		return l;
	}

	/**
	 * This method is responsible for creating the structure of the activity
	 * diagram representing the coarse-grained behavior of the software product
	 * line. Currently it is only creating sequential activity diagrams, but
	 * soon it will be changed for creating random and more complex structures.
	 * If no number of activities is initially informed by the user, the number
	 * of activities will be equal to the number of features present at 1st
	 * level of the feature model.
	 * 
	 * @return The ActivityDiagram object representing the SPL's activity
	 *         diagram.
	 */
	private ActivityDiagram generateActivityDiagramStructure() {
		ActivityDiagram ad = new ActivityDiagram();
		ad.setName("AD_SPL_" + idxModel);
		// 1st step: create the number of activities according to the user's
		// choice
		// number of activities will be equal to the number of features present
		// at first level of the feature model.
		if (numberOfActivities == 0) {
			int num1stLevelFeatures = splarFm.getNodesAtLevel(1).size();
			for (int i = 0; i < num1stLevelFeatures; i++) {
				ActivityDiagramElement e = ActivityDiagramElement
						.createElement(ActivityDiagramElement.ACTIVITY,
								"Activity_" + idxActivity++);
				ad.addElement(e);
			}
		} else { // otherwise the number of generated activities will be equal
					// to
					// the number informed by the user.
			for (int i = 0; i < numberOfActivities; i++) {
				ActivityDiagramElement e = ActivityDiagramElement
						.createElement(ActivityDiagramElement.ACTIVITY,
								"Activity_" + idxActivity++);
				ad.addElement(e);
			}
		}

		// 2nd step: create a linear activity diagram structure.
		ActivityDiagramElement source = ad.getStartNode(), target = null;
		for (Activity a: ad.getSetOfActivities()) {
			target = a;
			Transition t = source.createTransition(target, "Trans_"
					+ idxActTransition++, 1);
			ad.addElement(t);
			source = target;
		}
		target = ActivityDiagramElement.createElement(
				ActivityDiagramElement.END_NODE, "EndNode");
		Transition t = source.createTransition(target, "Trans_"
				+ idxActTransition++, 1);
		ad.addElement(target);
		ad.addElement(t);

		// 3rd step: ensure each activity has an empty sequence diagram
		// associated with it
		for (Activity a : ad.getSetOfActivities()) {
			// SequenceDiagram s = SequenceDiagram.createSequenceDiagram(
			// "SD_" + idxSequenceDiagram++,
			// "true");
			SequenceDiagram s = randomSequenceDiagram("SD_"
					+ idxSequenceDiagram++, "true");
			a.addSequenceDiagram(s);
		}

		return ad;
	}

	/**
	 * This method is used for creating Feature Models according to the choosen
	 * algorithm. Initially it supports two algorithms, that are chooosen
	 * according to the generatorAlgorithm parameter: GHEZZIGENERATOR that
	 * results into a FM that all features besides root are optional, and
	 * SPLOTGENERATOR that uses the SPLOT FM generator for producing valid
	 * feature models.
	 * 
	 * @param generatorAlgorithm
	 * @return fm - a Feature Model object representing the whole Feature Model,
	 *         including its crosstree constraints.
	 */
	public Object generateFeatureModel(int generatorAlgorithm) {
		Object answer = null;

		switch (generatorAlgorithm) {
		case GHEZZIGENERATOR:
			// TODO change for using the SPLAR engine
			// reanaFm = generateGhezziFeatureModel();
			reanaFm = null;
			answer = reanaFm;
			break;

		default:
			splarFm = generateSplotFeatureModel();
			answer = splarFm;
			break;
		}
		return answer;
	}

	/**
	 * This method sets the parameters of the SPLAR feature models generator,
	 * and starts the automatic generation of the feature model.
	 * 
	 * @return - the random feature model created by the SPLAR generator engine
	 */
	private splar.core.fm.FeatureModel generateSplotFeatureModel() {
		FMGeneratorEngine engine = FMGeneratorEngine.getInstance();

		// setting SPLAR Collection's parameters
		engine.setCollectionName(fmFilePrefix + "myCollection");
		engine.setCollectionSize(1);
		engine.setCollectionPath(modelsPath);

		// setting SPLAR Feature Tree Information's parameters
		if (fmParameters == null) {
			fmParameters = FeatureModelParameters
					.getConfiguration(FeatureModelParameters.STANDARD_FEATURE_MODEL);
		}

		engine.setFeatureModelSize(numberOfFeatures);
		engine.setMandatoryPercentage(fmParameters.getMandatoryFeatures());
		engine.setOptionalPercentage(fmParameters.getOptionalFeatures());
		engine.setInclusiveORPercentage(fmParameters.getInclusiveOrFeatures());
		engine.setExclusiveORPercentage(fmParameters.getExclusiveOrFeatures());
		engine.setMinimumBranchingFactor(fmParameters
				.getMininumBranchingFeatures());
		engine.setMaximumBranchingFactor(fmParameters
				.getMaximumBranchingFeatures());
		engine.setMaximumGroupSize(fmParameters.getMaximumGroupSize());

		// setting SPLAR Cross-tree's constraints parameters
		engine.setCTCR(fmParameters.getNumCrossTreeConstraints());
		engine.setClauseDensity(fmParameters.getClauseDensity());
		engine.setModelConsistency(fmParameters.getModelConsistency());

		List<FeatureModel> featureModels = engine
				.run2(FMGeneratorEngine.BOTH_FORMAT);
		FeatureModel fm = featureModels.get(0);
		return fm;
	}

	private ReanaFeatureModel generateGhezziFeatureModel() {
		ReanaFeatureModel tmp = ReanaFeatureModel
				.createFeatureModel("FeatureModel_" + idxModel++);
		Feature root = tmp.getRoot();
		root.setType(Feature.OR);
		root.setAbstract(Feature.ABSTRACT);
		for (int i = 0; i < numberOfFeatures; i++) {
			Feature f = root.addChild("Feature_" + idxFeature++,
					Feature.ALTERNATIVE, Feature.MANDATORY, Feature.ABSTRACT,
					!Feature.HIDDEN);
			listOfPendingFeatures.add(f);
		}
		return tmp;
	}

	public splar.core.fm.FeatureModel getSplotFM() {
		return this.splarFm;
	}

	/**
	 * This method is responsible for generating a set of reliabilities values
	 * for the lifelines within the range specified by minValue and maxValue
	 * parameters. A number of values to be created is also necessary to be
	 * informed by the parameter numOfValues. The precision of the generated
	 * values is represented in number of decimal places, informed by the
	 * precision parameter.
	 * 
	 * @param minValue
	 *            reliability's initial value range
	 * @param maxValue
	 *            reliability's final value range
	 * @param numOfValues
	 *            quantity of reliabilities values to be generated
	 * @param precision
	 *            number of decimal places for representing each reliability
	 *            value randomly generated.
	 */
	public void setNumberOfReliabiliatiesValues(double minValue,
			double maxValue, int precision) {
		ValuesGenerator.setMinReliabilityValue(minValue);
		ValuesGenerator.setMaxReliabilityValue(maxValue);
		// ValuesGenerator.setNumOfReliabilitiesValues(numOfValues);
		ValuesGenerator.setReliabilityPrecision(precision);
	}

	public void setFeatureModelParameters(FeatureModelParameters fmParameters) {
		this.fmParameters = fmParameters;
	}

	public void setNumberOfAltFragments(int numberOfAltFragments) {
		this.numberOfAltFragments = numberOfAltFragments;
	}

	public void setNumberOfLoopsFragments(int numberOfLoopFragments) {
		this.numberOfLoopFragments = numberOfLoopFragments;
	}

	public FeatureModelParameters getFeatureModelParameters() {
		return fmParameters;
	}

	public int getFragmentSize() {
		return this.fragmentSize;
	}

	public int getNumberOfAltFragments() {
		return this.numberOfAltFragments;
	}

	public int getNumberOfLoopFragments() {
		return this.numberOfLoopFragments;
	}

}
