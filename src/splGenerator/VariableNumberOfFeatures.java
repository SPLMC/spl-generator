package splGenerator;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Parameter;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;
import java.util.function.IntPredicate;
import java.util.stream.IntStream;

import splGenerator.Util.SPLFilePersistence;
import splar.core.fm.FeatureModel;
import splar.core.fm.FeatureTreeNode;

public class VariableNumberOfFeatures extends VariableBehavioralParameters {

	FeatureModelParameters fmParameters = FeatureModelParameters
			.getConfiguration(FeatureModelParameters.EXPERIMENT_EVOLUTION);
	private int fragmentSize;
	private HashSet<Lifeline> lifelines;
	private int numberOfAltFragments;
	private int numberOfLoopFragments;

	private HashMap<String, String> renamedFeatures;

	@Override
	protected LinkedList<SPL> employTransformation(SPL spl)
			throws CloneNotSupportedException {
		// 1st step: obtain all SPL's characteristics for creating similar SPLs
		// get the feature model's characteristics
		defineFMParameters(spl);
		// get the behavioral model's characteristics
		defineBehavioralModelParameters(spl);

		// 2nd step: create the first seed SPL.
		LinkedList<SPL> answer = new LinkedList<SPL>();
		SPL currentVersion = createSplDeepCopy(spl);
		renameFeatures(currentVersion.getFeatureModel().getRoot(),
				currentVersion, 0);

		while (currentValue <= maxValue) {
			renamedFeatures = new HashMap<String, String>();
			int lastFeatureIndex = lastFeatureIndex(currentVersion
					.getFeatureModel().getRoot());

			// 3rd step: from each seed SPL, we will create a new SPL having the
			// same feature and behavioral models' characteristics of its seed
			SplGenerator generator = SplGenerator.newInstance();
			generator.setFeatureModelParameters(fmParameters);
			generator.setNumberOfFeatures(variationStep + 1);
			generator.setFragmentSize(this.fragmentSize);
			generator.setNumberOfActivities(1);
			generator.setNumberOfDecisionNodes(0);
			generator.setNumberOfLifelines(6);
			generator.setNumberOfReliabiliatiesValues(0.990, 0.9999, 4);
			generator.setNumberOfAltFragments(this.numberOfAltFragments);
			generator.setNumberOfLoopsFragments(this.numberOfLoopFragments);

			SPL temp = generator.generateSPL(SplGenerator.SPLOT,
					SplGenerator.SYMMETRIC);

			int nextIndex = lastFeatureIndex;

			renameFeatures(temp.getFeatureModel().getRoot(), temp, nextIndex);
			createFeatureIDEFile(temp, "");

			temp = appendSPL(temp, currentVersion);

			answer.add(temp);

			currentVersion = createSplDeepCopy(temp);
			currentValue += variationStep;
		}
		return answer;
	}

	private String printFeatureModel(FeatureTreeNode feature) {
		StringBuilder answer = new StringBuilder();
		for (int i = 0; i < feature.getLevel(); i++) {
			answer.append("    ");
		}
		answer.append(feature.getName() + " (" + feature.getChildCount() + ")"
				+ '\n');

		int childrenCount = feature.getChildCount();
		Enumeration<FeatureTreeNode> children = feature.children();
		while (childrenCount > 0) {
			FeatureTreeNode f = (FeatureTreeNode) children.nextElement();
			answer.append(printFeatureModel(f));
			childrenCount--;
		}

		return answer.toString();
	}

	private void createFeatureIDEFile(SPL temp, String obs) {
		String x = temp.getFeatureModel().dumpFeatureIdeXML();
		try {
			File f = new File(
					"/home/andlanna/workspace/spl-generator/src/generatedModels/"
					// "/home/andlanna/workspace2/reana/src/splGenerator/generatedModels/"
							+ currentValue + "_" + obs + ".xml");
			PrintStream p = new PrintStream(f);
			PrintStream oldOut = java.lang.System.out;
			java.lang.System.setOut(p);
			System.out.print(x);
			p.flush();
			p.close();
			java.lang.System.setOut(oldOut);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private SPL appendSPL(SPL temp, SPL currentVersion) {
		SPL answer = null;
		// 1st step: create a deep copy of the current version of the spl to be
		// changed.
		answer = createSplDeepCopy(currentVersion);

		createFeatureIDEFile(answer, "beforeAppend");

		// 2nd step: obtain all features to be added in the current version of
		// the spl.
		Enumeration<?> children = temp.getFeatureModel().getRoot().children();
		LinkedList<FeatureTreeNode> childrenToAdd = new LinkedList<FeatureTreeNode>();

		while (children.hasMoreElements()) {
			FeatureTreeNode a = (FeatureTreeNode) children.nextElement();
			childrenToAdd.add(a);
		}

		LinkedList<Fragment> fragmentsToAdd = new LinkedList<Fragment>();
		int countChildrenToAdd = childrenToAdd.size();
		System.out.println("#children to add --> " + countChildrenToAdd);

		for (FeatureTreeNode node : childrenToAdd) {
			fragmentsToAdd.addAll(getFragmentsByGuardCondition(node.getName(),
					temp));
			answer.getFeatureModel().getRoot().add(node);
			// answer.getFeatureModel().getRoot()
			// .attachData(new Integer(countChildrenToAdd - 1));
		}

		createFeatureIDEFile(answer, "afterAppend");

		// 3rd step: get the fragments associated to features which will be add
		// at new feature model
		SequenceDiagram sdRoot = chooseRootSequenceDiagram(answer);

		System.out.println(" SD Root that will receive the fragment: "
				+ sdRoot.getName());
		for (Fragment fr : fragmentsToAdd) {
			System.out.println(" Fragment that will be added: " + fr.getName());
			int pos = new Random().nextInt(sdRoot.getElements().size());
			sdRoot.getElements().add(pos, fr);
		}

		return answer;
	}

	private SequenceDiagram chooseRootSequenceDiagram(SPL answer) {
		SequenceDiagram sdRoot = getSequenceDiagramByGuardCondition("R", answer);
		if (sdRoot == null) {
			sdRoot = getSequenceDiagramByGuardCondition("Root", answer);
		}
		if (sdRoot == null) {
			sdRoot = getSequenceDiagramByGuardCondition("root", answer);
		}
		if (sdRoot == null) {
			sdRoot = getSequenceDiagramByGuardCondition("true", answer);
		}
		return sdRoot;
	}

	private SequenceDiagram getSequenceDiagramByGuardCondition(String string,
			SPL spl) {
		SequenceDiagram answer = null;
		for (Activity a : spl.getActivityDiagram().getSetOfActivities()) {
			for (SequenceDiagram sd : a.getTransitiveSequenceDiagram()) {
				if (sd.getGuardCondition().equals(string)) {
					answer = sd;
				}
			}
		}
		return answer;
	}

	private LinkedList<Fragment> getFragmentsByGuardCondition(String name,
			SPL temp) {
		LinkedList<Fragment> answer = new LinkedList<Fragment>();
		for (Activity a : temp.getActivityDiagram().getSetOfActivities()) {
			for (SequenceDiagram sd : a.getSequenceDiagrams()) {
				for (Fragment fr : sd.getFragments()) {
					for (SequenceDiagram s : fr.getSequenceDiagrams()) {
						if (s.getGuardCondition().equals(name)) {
							answer.add(fr);
						} else {
						}
					}
				}
			}
		}

		return answer;
	}

	private LinkedList<Fragment> getFragmentsByFeatureName(String name, SPL temp) {
		LinkedList<Fragment> answer = new LinkedList<Fragment>();
		List<Activity> activities = temp.getActivityDiagram()
				.getSetOfActivities();
		for (Activity act : activities) {
			HashSet<Fragment> fragments = act.getTransitiveFragments();
			for (Fragment fr : fragments) {
				for (SequenceDiagram sd : fr.getSequenceDiagrams()) {
					if (sd.getGuardCondition().equals(name)) {
						answer.add(fr);
					}
				}
			}
		}
		return answer;
	}

	private void renameFeatures(FeatureTreeNode node, SPL spl, int nextIndex) {
		if (node.isRoot()) {
			Enumeration<?> children = node.children();
			while (children.hasMoreElements()) {
				FeatureTreeNode f = (FeatureTreeNode) children.nextElement();
				nextIndex++;
				renameFeatures(f, spl, nextIndex);
			}
		} else {
			StringBuilder strNewName = new StringBuilder();
			if (node.getName().startsWith("o_")
					|| node.getName().startsWith("m_")
					|| node.getName().startsWith("g_")) {

				String[] strs = node.getName().split("_");
				strs[1] = Integer.toString(nextIndex);

				for (int i = 0; i < strs.length; i++) {
					strNewName.append(strs[i]);
					if (i != (strs.length - 1)) {
						strNewName.append("_");
					}
				}
				renameSequenceDiagram(node.getName(), strNewName.toString(),
						spl);
				node.setName(strNewName.toString());
				for (int i = 0; i < node.getChildCount(); i++) {
					FeatureTreeNode f = (FeatureTreeNode) node.getChildAt(i);
					nextIndex++;
					renameFeatures(f, spl, nextIndex);
				}
			}

			if (node.getName().startsWith("_Ge")
					|| node.getName().startsWith("_Gi")) {
				String[] strs = node.getName().split("_");

				// for (int i = 0; i < strs.length; i++) {
				// System.out.println("strs[" + i + "]: " + strs[i]);
				// }
				strNewName.append("_");
				for (int i = 1; i < strs.length; i++) {
					if (i == 3) {
						strNewName.append(nextIndex);
					} else {
						strNewName.append(strs[i]);
					}
					if (i != (strs.length - 1)) {
						strNewName.append("_");
					}
				}
				strNewName.deleteCharAt(0);
				// System.out.println("OLD: " + node.getName() + " ---> NEW: "
				// + strNewName.toString());
				renameSequenceDiagram(node.getName(), strNewName.toString(),
						spl);
				node.setName(strNewName.toString());
				for (int i = 0; i < node.getChildCount(); i++) {
					FeatureTreeNode f = (FeatureTreeNode) node.getChildAt(i);
					renameFeatures(f, spl, nextIndex);
				}
			}
		}
	}

	private void renameSequenceDiagram(String oldName, String newName, SPL spl) {
		for (Activity a : spl.getActivityDiagram().getSetOfActivities()) {
			for (SequenceDiagram sd : a.getSequenceDiagrams()) {
				if (sd.getGuardCondition().equals(oldName)) {
					sd.setGuard(newName);
				}
				LinkedList<SequenceDiagram> otherSDs = sd
						.getTransitiveSequenceDiagram();
				for (SequenceDiagram s : otherSDs) {
					if (s.getGuardCondition().equals(oldName)) {
						s.setGuard(newName);
					}
				}
			}
		}
	}

	private int lastFeatureIndex(FeatureTreeNode feature) {
		String reliability = feature.getName();
		int answer; 
		if (reliability.matches("[0-1]\\.[0-9]*")) {
			answer = parseIndex(feature.getName());
			Enumeration<?> children = feature.children();
			while (children.hasMoreElements()) {
				Object obj = children.nextElement();
				FeatureTreeNode c = (FeatureTreeNode) obj;
				answer = Integer.max(answer, lastFeatureIndex(c));
			}
		} else {
			answer = 0;
		}
		return answer;
	}

	private int parseIndex(String name) {
		int answer;
		String[] strs = name.split("_");
		if (strs.length > 1) {
			int pos = 0;
			while (strs[pos].equals("") || strs[pos].equals("o")
					|| strs[pos].equals("m") || strs[pos].equals("g")
					|| strs[pos].equals("Gi") || strs[pos].equals("Ge")) {
				pos++;
			}
			answer = Integer.parseInt(strs[pos]);
		} else {
			answer = 0;
		}
		return answer;
	}

	private void defineBehavioralModelParameters(SPL spl) {
		SplGenerator g = spl.getSplGenerator();
		this.fragmentSize = g.getFragmentSize();
		this.lifelines = getLifelines(spl);
		this.numberOfAltFragments = g.getNumberOfAltFragments();
		this.numberOfLoopFragments = g.getNumberOfLoopFragments();
	}

	private HashSet<Lifeline> getLifelines(SPL spl) {
		HashSet<Lifeline> answer = new HashSet<Lifeline>();
		for (Activity a : spl.getActivityDiagram().getSetOfActivities()) {
			answer.addAll(a.getTransitiveLifelines());
		}
		return answer;
	}

	private void defineFMParameters(SPL spl) {
		String className = spl.getSplGenerator().getFeatureModelParameters()
				.getClass().getSimpleName();
		switch (className) {
		case "GhezziConfigurationSet":
			fmParameters = FeatureModelParameters
					.getConfiguration(FeatureModelParameters.GHEZZI_FEATURE_MODEL);
			break;

		case "ExperimentConfigurationSet":
			System.out.println(" ---> EXPERIMENT!!!");
			fmParameters = FeatureModelParameters
					.getConfiguration(FeatureModelParameters.EXPERIMENT_EVOLUTION);
		default:
			fmParameters = null;
			break;
		}
	}

	private void printObject(Object o) {
		// Object o = fmParameters2;
		StringBuilder str = new StringBuilder();

		str.append("Class name: " + o.getClass().getName());
		str.append("\n");
		str.append("---");
		str.append("\n\n");

		str.append("Constructors");
		str.append("\n");
		for (Constructor c : o.getClass().getConstructors()) {
			str.append(Modifier.toString(c.getModifiers()) + " ");
			str.append(c.getName());
			str.append("\n");
		}
		str.append("\n\n");

		str.append("Field");
		str.append("\n");
		for (Field f : o.getClass().getDeclaredFields()) {
			str.append(Modifier.toString(f.getModifiers()) + " ");
			str.append(f.getName());
			str.append("\n");
		}
		str.append("\n\n");

		str.append("Method");
		str.append("\n");
		for (Method m : o.getClass().getMethods()) {
			str.append(Modifier.toString(m.getModifiers()) + " ");
			str.append(m.getName());
			str.append("(");
			for (Parameter p : m.getParameters()) {
				str.append(p.getType().getClass().getSimpleName() + " ");
				str.append(p.getName());
			}
			str.append(")");
			str.append("\n");
		}
		str.append("\n\n");

		String answer = str.toString();
		System.out.println(answer);
	}

}
