package splGenerator.transformation;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Set;

import com.sun.corba.se.spi.orbutil.fsm.State;

import fdtmc.FDTMC;
import splGenerator.Activity;
import splGenerator.ActivityDiagram;
import splGenerator.ActivityDiagramElement;
import splGenerator.SequenceDiagram;
import splGenerator.SplGenerator;
import splGenerator.Transition;
import splGenerator.Util.SPLFilePersistence;
import tool.RDGNode;

public class Transformer {

	private HashMap<String, fdtmc.State> fdtmcStateById = new HashMap<String, fdtmc.State>();
	private RDGNode root;

	/**
	 * This method is responsible for creating an RDG structure for a whole SPL
	 * given an activity diagram as input.
	 * 
	 * @param ad
	 *            the activity diagram describing the coarse-grained behavior of
	 *            the SPL.
	 * @return the root node of the RDG structure built for the SPL.
	 */
	public RDGNode transformAD(ActivityDiagram ad) {
		FDTMC f = new FDTMC();
		f.setVariableName(ad.getName() + "_s");
		RDGNode answer = new RDGNode(ad.getName(), "true", f);
		root = answer;

		// Takes the first element (init) and transform it into its FDTMC
		// representation
		ActivityDiagramElement init = ad.getStartNode();
		transformAdElement(init, f);

		SPLFilePersistence.fdtmc2Dot(f, ad.getName());

		return answer;
	}

	private fdtmc.State transformAdElement(ActivityDiagramElement adElem,
			FDTMC f) {
		fdtmc.State answer = null;

		fdtmc.State source = null;
		fdtmc.State isModeled;
		String adClass = adElem.getClass().getSimpleName();
		switch (adClass) {
		case "StartNode":
			source = f.createInitialState();
			fdtmc.State error = f.createErrorState();

			HashSet<Activity> nextActivities = new HashSet<Activity>();
			for (Transition t : adElem.getTransitions()) {
				ActivityDiagramElement e = t.getTarget();
				Activity a;
				if (e instanceof Activity) {
					a = (Activity) e;
					nextActivities.add(a);
				}
			}

			for (Activity a : nextActivities) {
				fdtmc.State target = transformAdElement(a, f);
				f.createTransition(source, target, "", Double.toString(1.0));
				// source = transformAdElement(a, f);
			}
			source.setLabel(FDTMC.INITIAL_LABEL);
			answer = source;

			break;

		case "Activity":
			// 1st.: check if the activity is already modeled and its FDTMC is
			// available
			isModeled = fdtmcStateById.get(adElem.getElementName());
			if (isModeled == null) {
				// In case the activity was not modeled yet, we should model its
				// associated sequence diagrams
				Activity a = (Activity) adElem;
				for (SequenceDiagram s : a.getSequenceDiagrams()) {
					SequenceDiagramTransformer sdt = new SequenceDiagramTransformer();
					this.root.addDependency(sdt.transformSD(s));
				}

				source = f.createState();
				HashSet<ActivityDiagramElement> nextElement = new HashSet<ActivityDiagramElement>();
				for (Transition t : adElem.getTransitions()) {
					ActivityDiagramElement e = t.getTarget();
					nextElement.add(e);
				}

				for (ActivityDiagramElement e : nextElement) {
					fdtmc.State target = transformAdElement(e, f);
					f.createTransition(source, target, a.getElementName(), a
							.getSequenceDiagrams().getFirst().getName());
					f.createTransition(source, f.getErrorState(),
							a.getElementName(), "1-"
									+ a.getSequenceDiagrams().getFirst()
											.getName());
					// f.createTransition(source, target, a.getElementName(),
					// "r" + a.getElementName());
					// f.createTransition(source, f.getErrorState(),
					// a.getElementName(), "1-r" + a.getElementName());
				}
				// for (Transition t : adElem.getTransitions()) {
				// fdtmc.State target = transformAdElement(t.getTarget(), f);
				// f.createTransition(source, target, t.getElementName(),
				// Double.toString(t.getProbability()));
				// f.createTransition(source, f.getErrorState(),
				// t.getElementName(),
				// Double.toString(1 - t.getProbability()));
				// }
				fdtmcStateById.put(adElem.getElementName(), source);
				answer = source;
			} else
				answer = isModeled;
			break;

		case "EndNode":
			// 1st.: check if the end node is already modeled and its FDTMC is
			// available
			isModeled = fdtmcStateById.get(adElem.getElementName());
			if (isModeled == null) {
				source = f.createState();
				source.setLabel("success");
				f.createTransition(source, source, "", Double.toString(1.0));
				answer = source;
			} else
				answer = isModeled;
			break;

		case "DecisionNode":
			// 1st.: check if the decision node is already modeled and its FDTMC
			// is already available
			isModeled = fdtmcStateById.get(adElem.getElementName());
			if (isModeled == null) {
				source = f.createState();
				for (Transition t : adElem.getTransitions()) {
					fdtmc.State target = transformAdElement(t.getTarget(), f);
					f.createTransition(source, target, t.getElementName(),
							Double.toString(t.getProbability()));
					f.createTransition(source, f.getErrorState(),
							t.getElementName(),
							Double.toString(1 - t.getProbability()));
				}
				fdtmcStateById.put(adElem.getElementName(), source);
				answer = source;
			} else
				answer = isModeled;
			break;

		case "MergeNode":
			// 1st.: Check of the merge node is already modeled and its FDTMC is
			// already available
			isModeled = fdtmcStateById.get(adElem.getElementName());
			if (isModeled == null) {
				source = f.createState();
				for (Transition t : adElem.getTransitions()) {
					fdtmc.State target = transformAdElement(t.getTarget(), f);
					f.createTransition(source, target, "", Double.toString(1.0));
				}
				fdtmcStateById.put(adElem.getElementName(), source);
				answer = source;
			} else
				answer = isModeled;
			break;

		default:
			break;
		}

		return answer;
	}
}
