package splGenerator.transformation;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.Stack;

import javax.swing.plaf.synth.SynthSeparatorUI;

import com.sun.corba.se.spi.orbutil.fsm.State;

import fdtmc.FDTMC;
import splGenerator.Activity;
import splGenerator.ActivityDiagram;
import splGenerator.ActivityDiagramElement;
import splGenerator.Fragment;
import splGenerator.Lifeline;
import splGenerator.Message;
import splGenerator.SequenceDiagram;
import splGenerator.SequenceDiagramElement;
import splGenerator.SplGenerator;
import splGenerator.Transition;
import splGenerator.Util.SPLFilePersistence;
import tool.RDGNode;

public class Transformer {

	private HashMap<String, fdtmc.State> fdtmcStateById = new HashMap<String, fdtmc.State>();
	private RDGNode root;

	private int idxActivity = 0;
	private int idxTrans = 0;
	private int idxDecision = 0;
	private int idxActivityDiagram = 0;

	private HashMap<ActivityDiagramElement, Integer> incomingEdgesByState = new HashMap<ActivityDiagramElement, Integer>();
	private int idxLifeline = 0;
	private int idxMessage = 0;
	private Lifeline mockLifeline = new Lifeline("Mock lifeline");
	private static HashMap<Double, Lifeline> lifelineByReliability = new HashMap<Double, Lifeline>();
	private static Stack<fdtmc.Transition> transitionStack = new Stack<fdtmc.Transition>();

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
				}
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

	/**
	 * This method is responsible for creating the Activity Diagram of a given
	 * software product line. The FDTMC of the root node represent how the
	 * activities (describing the coarse-grained behavior of the software
	 * product line) interact among them.
	 * 
	 * @param root
	 *            the root node of the RDG structure
	 * @return an ActivityDiagram object representing the coarse-grained
	 *         behavior of the software product line.
	 */
	public ActivityDiagram getActivityDiagramFromFDTMC(RDGNode root) {
		ActivityDiagram answer = new ActivityDiagram();
		answer.setName("AD_SPL_" + idxActivityDiagram++);
		FDTMC fdtmc = root.getFDTMC();

		extractADElementFromFDTMC(fdtmc.getInitialState(), fdtmc, answer);

		return answer;
	}

	/**
	 * This method's role is to infer an activity diagram structure from a given
	 * FDTMC state and its relation with other FDTMC states. Such method
	 * implements the transformation templates in its reverse order.
	 * 
	 * @param currentState
	 *            the FDTMC state from which the activity diagram structure will
	 *            be extracted
	 * @param fdtmc
	 *            the FDTMC under analysis.
	 * @param ad
	 *            the resulting activity diagram. Its construction will occur
	 *            step-by-step, as iterations go on.
	 * @return
	 */
	private ActivityDiagramElement extractADElementFromFDTMC(
			fdtmc.State currentState, FDTMC fdtmc, ActivityDiagram ad) {
		ActivityDiagramElement answer = null;

		// remove error transitions leaving from the current state
		List<fdtmc.Transition> transitions = fdtmc
				.getTransitionsBySource(currentState);
		for (fdtmc.Transition t : transitions) {
			if (t.getTarget() == fdtmc.getErrorState()) {
				transitions.remove(t);
			}
		}

		// 1st Step.: identify the activity diagram element based on the fdtmc
		// structure. Such identification must ensure the number and type of
		// elements are correct.
		if (currentState.getLabel() == null) {
			ActivityDiagramElement adSource;
			if (transitions.size() > 1) {
				adSource = ActivityDiagramElement.createElement(
						ActivityDiagramElement.DECISION_NODE, "DecisionNode_"
								+ idxDecision++);
				incomingEdgesByState.put(adSource, 0);
				for (fdtmc.Transition t : transitions) {
					ActivityDiagramElement adTarget = extractADElementFromFDTMC(
							t.getTarget(), fdtmc, ad);
					Transition trans = adSource.createTransition(adTarget,
							t.getActionName(),
							Double.parseDouble(t.getProbability()));
					ad.addElement(trans);
					updateIncomingEdges(adTarget);
				}
				ad.addElement(adSource);
				answer = adSource;
			} else if (transitions.size() == 1) {
				adSource = ActivityDiagramElement.createElement(
						ActivityDiagramElement.ACTIVITY, "Activity_"
								+ idxActivity++);
				incomingEdgesByState.put(adSource, 0);
				for (fdtmc.Transition t : transitions) {
					ActivityDiagramElement adTarget = extractADElementFromFDTMC(
							t.getTarget(), fdtmc, ad);
					String adProbability;
					if (t.getProbability().matches("[0-9]*")) {
						adProbability = "1.0";
					} else {
						adProbability = t.getProbability();
						adSource.setElementName(adProbability);
					}

					System.out.println("Probability:" + t.getProbability());
					Transition trans = adSource.createTransition(adTarget,
							t.getActionName(), 1.0);
					ad.addElement(trans);
					updateIncomingEdges(adTarget);
				}
				ad.addElement(adSource);
				answer = adSource;
			}
		} else if (currentState.getLabel().equalsIgnoreCase("initial")) {
			ActivityDiagramElement adStartNode = ad.getStartNode();
			incomingEdgesByState.put(adStartNode, 0);
			for (fdtmc.Transition t : transitions) {
				fdtmc.State target = t.getTarget();
				System.out.println(t.getSource().getIndex() + " --- "
						+ t.getProbability() + " ---> "
						+ t.getTarget().getIndex());

				ActivityDiagramElement adTarget = extractADElementFromFDTMC(
						target, fdtmc, ad);
				// ActivityDiagramElement adTarget =
				// extractADElementFromFDTMC(target,
				// fdtmc, ad);

				String adProbability;
				if (t.getProbability().matches("[0-9]*")) {
					adProbability = "1.0";
				} else {
					adProbability = t.getProbability();
					adTarget.setElementName(adProbability);
				}

				System.out.println("t.getActionName() = " + t.getActionName());
				System.out.println("adTarget.name= "
						+ adTarget.getElementName());
				Transition trans = adStartNode.createTransition(adTarget,
						t.getActionName(), 1.0);
				System.out.println("*** Probability:" + t.getProbability());
				ad.addElement(trans);
				updateIncomingEdges(adTarget);
			}
			answer = adStartNode;

		} else if (currentState.getLabel().equalsIgnoreCase("success")) {
			ActivityDiagramElement adEndNode = ActivityDiagramElement
					.createElement(ActivityDiagramElement.END_NODE, "End node");
			incomingEdgesByState.put(adEndNode, 0);
			ad.addElement(adEndNode);
			answer = adEndNode;
		} else if (currentState.getLabel().equalsIgnoreCase("error")) {
			System.out.println("---> Error Found");
			System.out.println("   |--> nothing to do, I will return null");
			answer = null;
		}

		return answer;
	}

	private void updateIncomingEdges(ActivityDiagramElement adTarget) {
		int oldValue = incomingEdgesByState.get(adTarget);
		int newValue = oldValue + 1;
		incomingEdgesByState.put(adTarget, newValue);
	}

	public List<SequenceDiagram> getSequenceDiagramFromFDTMC(RDGNode n) {
		LinkedList<SequenceDiagram> answer = new LinkedList<SequenceDiagram>();

		// defining the name of the sequence diagram, based on its variable name
		String sdName = n.getFDTMC().getVariableName();
		if (sdName.endsWith("_s")) {
			sdName = sdName.replace("_s", "");
		}

		System.out.println("---> I will create a Sequence diagram right now:\n"
				+ "     Name: " + sdName + '\n' + "     Guard: "
				+ n.getPresenceCondition());
		SequenceDiagram sd = SequenceDiagram.createSequenceDiagram(sdName,
				n.getPresenceCondition());

		FDTMC fdtmc = n.getFDTMC();
		fdtmc.State initialState = fdtmc.getInitialState();

		extractSDElementFromFDTMC(initialState, fdtmc, sd);

		System.out.println(sd);

		answer.add(sd);

		return answer;
	}

	private SequenceDiagramElement extractSDElementFromFDTMC(
			fdtmc.State currentState, FDTMC fdtmc, SequenceDiagram sd) {
		SequenceDiagramElement answer = null;

		// get all transitions leaving the current fdtmc state
		List<fdtmc.Transition> outgoingTransitions = fdtmc
				.getTransitionsBySource(currentState);

		// "cleaning" all the irrelevant transitions (to error state or
		// absorbing transition)
		for (fdtmc.Transition t : outgoingTransitions) {
			if (t.getTarget() == fdtmc.getErrorState()
					|| (t.getSource() == fdtmc.getSuccessState() && t
							.getTarget() == fdtmc.getSuccessState())) {
				outgoingTransitions.remove(t);
			}
		}

		// for the remaining transitions we must identify which kind of SD
		// structure they represent and build it again.
		for (fdtmc.Transition t : outgoingTransitions) {
//			System.out.println("|outgoing|= " + outgoingTransitions.size());
//			System.out.println("--- " + t.getActionName() + " / "  + t.getProbability() + " --->");
			if (t.getActionName().matches("[n][a-zA-Z0-9]+") && 
			    t.getProbability().equals("1.0")) {
				System.out.println("A fragment was found!");
				fdtmc.State initialFragment = t.getTarget(),
							endFragment, 
							errorFragment;
				List<fdtmc.Transition> outTransInitFrag = fdtmc.getTransitionsBySource(initialFragment); 
				for (fdtmc.Transition tr : outTransInitFrag) {
					if (tr.getProbability().matches("[n][0-9]+")) {
						System.out.println("END frag found");
						endFragment = tr.getTarget();
						System.out.println("Create fragment here");
						
						Fragment fr = new Fragment(tr.getProbability());
						sd.addFragment(fr);
						
						System.out.println("Proceed to next state from here!");
						System.out.println(fdtmc.getTransitionsBySource(tr.getTarget()).size());
						System.out.println(fdtmc.getTransitionsBySource(tr.getTarget()).get(0).getActionName());
						System.out.println(fdtmc.getTransitionsBySource(tr.getTarget()).get(0).getProbability());
						fdtmc.State next = fdtmc.getTransitionsBySource(tr.getTarget()).get(0).getTarget();
						
						extractSDElementFromFDTMC(next, fdtmc, sd);
						
						
					} else if (tr.getProbability().matches("[1][\\s]*[-][\\s]*[a-zA-Z0-9]+")) {
						System.out.println("ERROR frag found");
						errorFragment = tr.getTarget();
					}
				}
				
				
			} else if (t.getProbability().matches("^[0-1]\\.[0-9]+"))/* &&
				!nextTransitionProbability(t, fdtmc).matches("[a-z]+[a-zA-Z0-9]*") ) */{
				System.out.println("A transition with probability equals to a real number was found");
//				System.out.println("Value: " + t.getProbability() + "\n");
				Lifeline target = identifyLifelines(t);
				Lifeline source = mockLifeline;
				int mType = identifyMessageType(t);
				sd.createMessage(source, target, mType, t.getActionName(), target.getReliability());
				extractSDElementFromFDTMC(t.getTarget(), fdtmc, sd);
			} 
		}
		
		
		return answer;
	}

	


	private fdtmc.State nextState(fdtmc.State st, FDTMC fdtmc) {
		fdtmc.State answer = null;
		List<fdtmc.Transition> transitions = fdtmc.getTransitionsBySource(st);
		for ( fdtmc.Transition t : transitions) {
			if (t.getTarget() == fdtmc.getErrorState()) {
				transitions.remove(t);
			}
		}
		
		if (transitions.size() == 1) {
			answer = transitions.get(0).getTarget();
			System.out.println(answer.getIndex());
		}
		return answer;
	}

	private String nextTransitionProbability(fdtmc.Transition t, FDTMC fdtmc) {
		String answer = null;
		
		List<fdtmc.Transition> transitions = fdtmc.getTransitionsBySource(t.getTarget()); 
		for (fdtmc.Transition tr : transitions) {
			if (tr.getTarget() == fdtmc.getErrorState()) {
				transitions.remove(tr);
			} 
			if (tr.getProbability().matches("[a-z]+[a-zA-Z0-9]*")) {
				answer = tr.getProbability(); 
			}
		}
		
		if (transitions.size() == 1) {
			String probability = transitions.get(0).getProbability();
			answer = probability;
		}
		
		return answer;
	}


	private Lifeline identifyLifelines(fdtmc.Transition t) {
		Lifeline answer = null;
		String toConfirm = t.getProbability();
		if (toConfirm.matches("[0-9.]+")) {
			double probability = Double.parseDouble(t.getProbability());

			if (!lifelineByReliability.containsKey(probability)) {
				answer = new Lifeline("Lifeline_" + idxLifeline++);
				answer.setReliability(probability);
				lifelineByReliability.put(probability, answer);
			} else {
				answer = lifelineByReliability.get(probability);
			}
		}
		return answer;
	}

	private int identifyMessageType(fdtmc.Transition t) {
		int answer = -1;
		if (t.getActionName() == null || t.getActionName().equals("")) { // asynchronous
			// message do not have an
			// associated action name
			answer = Message.ASYNCHRONOUS;
		} else { // otherwise it can be a synchronous or reply message
			// get the first transition from the stack
			answer = Message.SYNCHRONOUS;
		}

		return answer;
	}
}
