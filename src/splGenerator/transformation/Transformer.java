package splGenerator.transformation;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Stack;

import fdtmc.FDTMC;
import fdtmc.State;
import splGenerator.Activity;
import splGenerator.ActivityDiagram;
import splGenerator.ActivityDiagramElement;
import splGenerator.Fragment;
import splGenerator.Lifeline;
import splGenerator.Message;
import splGenerator.SPL;
import splGenerator.SequenceDiagram;
import splGenerator.SequenceDiagramElement;
import splGenerator.Transition;
import splGenerator.Util.SPLFilePersistence;
import tool.RDGNode;

public class Transformer {

	private HashMap<String, State> fdtmcStateById = new HashMap<String, State>();
	private RDGNode root;

	private int idxActivity = 0;
	private int idxTrans = 0;
	private int idxDecision = 0;
	private int idxActivityDiagram = 0;

	private HashMap<ActivityDiagramElement, Integer> incomingEdgesByState = new HashMap<ActivityDiagramElement, Integer>();
	private int idxLifeline = 0;
	private int idxMessage = 0;
	private Lifeline mockLifeline = new Lifeline("Mock lifeline");
	private static HashMap<String, Activity> actByName = new HashMap<String, Activity>();
	private static HashMap<String, Fragment> fragmentByName = new HashMap<String, Fragment>();
	private static HashMap<String, SequenceDiagram> seqDiagByName = new HashMap<String, SequenceDiagram>();
	private static HashMap<Double, Lifeline> lifelineByReliability = new HashMap<Double, Lifeline>();
	private static Stack<fdtmc.Transition> transitionStack = new Stack<fdtmc.Transition>();

	/**
	 * This method's role is to link behavioral elements at behavioral models
	 * generated from the RDG nodes and their respective FDTMCs. As each
	 * activity in activity diagram is associated with a sequence diagram and
	 * each fragment in sequence diagram contains, at least, one sequence
	 * diagram this method is responsible for identifying such dependencies
	 * between distinct RDG nodes (i.e. between their FDTMCs) and create such
	 * links at the behavioral models level. It must be called after all
	 * activity and sequence diagrams are created from the FDTMCs.
	 * 
	 * @param spl
	 *            the software product line object containing all the behavioral
	 *            models
	 * @return the SPL object with all behavioral models linked.
	 */
	public SPL linkBehavioralElements(SPL spl) {
		// 1st step: link fragments to their respective sequence diagrams
		for (String fragName : fragmentByName.keySet()) {
			Fragment fr = fragmentByName.get(fragName);
			SequenceDiagram sd = seqDiagByName.get(fragName);
			fr.addSequenceDiagram(sd);
		}

		// 2nd step: link activities to sequence diagram
		for (Activity act : spl.getActivityDiagram().getSetOfActivities()) {
			SequenceDiagram sd = seqDiagByName.get(act.getElementName());
			if (sd == null) {
				System.out.println("WARNING!!! Sequence diagram not found!");
				System.out.println("    -> " + act.getElementName());
			}
			act.addSequenceDiagram(sd);
		}

		return spl;
	}

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

	private State transformAdElement(ActivityDiagramElement adElem,
			FDTMC f) {
		State answer = null;

		State source = null;
		State isModeled;
		String adClass = adElem.getClass().getSimpleName();
		switch (adClass) {
		case "StartNode":
			source = f.createInitialState();
			State error = f.createErrorState();

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
				State target = transformAdElement(a, f);
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
					State target = transformAdElement(e, f);
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
					State target = transformAdElement(t.getTarget(), f);
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
					State target = transformAdElement(t.getTarget(), f);
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

		for (Activity a : answer.getSetOfActivities()) {
			actByName.put(a.getElementName(), a);
		}

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
			State currentState, FDTMC fdtmc, ActivityDiagram ad) {
		ActivityDiagramElement answer = null;

		// remove error transitions leaving the current state
		List<fdtmc.Transition> outgoingTransitions = fdtmc
				.getTransitionsBySource(currentState);
		outgoingTransitions = pruneErrorTransitions(fdtmc, outgoingTransitions);

		// 1st Step.: identify the activity diagram element based on the fdtmc
		// structure. Such identification must ensure the number and type of
		// elements are correct.
		if (currentState.getLabel() == null) { 
			//states that don't have labels are "internal" fdtmc states (ie. 
			//they can't be part of the fdtmc's interface
			ActivityDiagramElement adSource;
			if (outgoingTransitions.size() > 1) {
				//in case the number of "regular" transitions is bigger than 1,
				//the element must be a decision node.
				adSource = ActivityDiagramElement.createElement(
						ActivityDiagramElement.DECISION_NODE, "DecisionNode_"
								+ idxDecision++);
				incomingEdgesByState.put(adSource, 0);
				for (fdtmc.Transition t : outgoingTransitions) {
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
			} else if (outgoingTransitions.size() == 1) {
				/* In case the state does not have an associated label and it
				 * has only one outgoing transition, it can only be an activity
				 */
				adSource = ActivityDiagramElement.createElement(
						ActivityDiagramElement.ACTIVITY, "Activity_"
								+ idxActivity++);
				incomingEdgesByState.put(adSource, 0);

				//Identifying the transition's probability of the singleton 
				//transition and creating the AD target element
				fdtmc.Transition t = outgoingTransitions.get(0);
				ActivityDiagramElement adTarget = extractADElementFromFDTMC(
						t.getTarget(), fdtmc, ad);
				String adProbability;
				if (t.getProbability().matches("[0-9]*")) {
					adProbability = "1.0";
				} else {
					adProbability = t.getProbability();
					adSource.setElementName(adProbability);
				}
				
				//Creating the singleton transition to the AD element
				Transition trans = adSource.createTransition(adTarget,
						t.getActionName(), 1.0);
				ad.addElement(trans);
				updateIncomingEdges(adTarget);
				
				ad.addElement(adSource);
				answer = adSource;
			}
			/* In case the FDTMC label has an associated label, it will guide
			 * which activity diagram is related with the fdtmc states and 
			 * transitions under analysis.
			 */
		} else if (currentState.getLabel().equalsIgnoreCase("initial")) {
			ActivityDiagramElement adStartNode = ad.getStartNode();
			incomingEdgesByState.put(adStartNode, 0);
			for (fdtmc.Transition t : outgoingTransitions) {
				State target = t.getTarget();
				ActivityDiagramElement adTarget;
				String adProbability;
				
				/* In case the first fdtmc transition does not represent the 
				 * reliability of an activity (ie. its probability equals one)
				 * the extraction of AD element begins from the next state.
				 */
				if (t.getProbability().matches("[0-1].[0-9]+")) {
					adTarget = extractADElementFromFDTMC(target, fdtmc, ad);
					adProbability = "1.0";
					Transition trans = adStartNode.createTransition(adTarget,
							"", 1.0);
					ad.addElement(adTarget);
					ad.addElement(trans);
					answer = adTarget;
				} else {
					/* otherwise the first transition represents the reliability
					 * of an Activity, that must be created immediately.  
					 */
					adProbability = t.getProbability();
					adTarget = ActivityDiagramElement.createElement(
							ActivityDiagramElement.ACTIVITY, adProbability);
					ad.addElement(adTarget);
					Transition trans = adStartNode.createTransition(adTarget,
							t.getProbability(), 1.0);
					ad.addElement(trans);
					incomingEdgesByState.put(adTarget, 0);

					ActivityDiagramElement adSource = adTarget;
					State source = target;
					outgoingTransitions = fdtmc.getTransitionsBySource(source);
					outgoingTransitions = pruneErrorTransitions(fdtmc, outgoingTransitions);

					fdtmc.Transition tr = outgoingTransitions.get(0);
					adTarget = extractADElementFromFDTMC(source, fdtmc, ad);
					if (tr.getProbability().matches("[0-9]*")) {
						adProbability = "1.0";
					} else {
						adProbability = tr.getProbability();
					}
					trans = adSource.createTransition(adTarget,
							tr.getActionName(), 1.0);
					ad.addElement(trans);
					answer = adStartNode;
				}
			}

		} else if (currentState.getLabel().equalsIgnoreCase("success")) {
			/* In case the state's label is "success", the end node for the 
			 * activity diagram will be created.
			 */
			ActivityDiagramElement adEndNode = ActivityDiagramElement
					.createElement(ActivityDiagramElement.END_NODE, "End node");
			incomingEdgesByState.put(adEndNode, 0);
			ad.addElement(adEndNode);
			answer = adEndNode;
		} else if (currentState.getLabel().equalsIgnoreCase("error")) {
			/*In case error state is found, do nothing.
			 */
			answer = null;
		}

		return answer;
	}

	/**
	 * This method is useful for updating the counter for incoming edges for
	 * every activity diagram element created. Such counter is useful when 
	 * identifying whether an element is a merge node (in this case, the number
	 * of incoming nodes, must be greater than 1).
	 * @param adTarget 
	 * 				The element whose incoming transitions counter will be 
	 * increased
	 */
	private void updateIncomingEdges(ActivityDiagramElement adTarget) {
		int oldValue = incomingEdgesByState.get(adTarget);
		int newValue = oldValue + 1;
		incomingEdgesByState.put(adTarget, newValue);
	}

	/**
	 * Given an RDG node as input, this method returns its Sequence Diagram, by
	 * the analysis of its FDTMC. Based on this FDTMC it is able to identify all
	 * the SD elements which were employed for its creation and the name of the
	 * sequence diagram.
	 * 
	 * @param node
	 *            the RDG node subject to analysis from which a sequence diagram
	 *            will be extracted.
	 * @return the sequence diagram object associated to the input node.
	 */
	public HashSet<SequenceDiagram> getSequenceDiagramFromFDTMC(RDGNode node) {
		HashSet<SequenceDiagram> answer = new HashSet<SequenceDiagram>();

		// defining the name of the sequence diagram, based on its variable name
		String sdName = node.getFDTMC().getVariableName();
		if (sdName.endsWith("_s")) {
			sdName = sdName.replace("_s", "");
		}
		if (sdName.matches("[s][a-zA-Z0-9]+")) {
			sdName = sdName.substring(1, sdName.length());
		}

		SequenceDiagram sd = SequenceDiagram.createSequenceDiagram(sdName,
				node.getPresenceCondition());

		FDTMC fdtmc = node.getFDTMC();
		State initialState = fdtmc.getInitialState();

		// building the FDTMC recursively
		extractSDElementFromFDTMC(initialState, fdtmc, sd);

		answer.add(sd);
		seqDiagByName.put(sdName, sd); // adding the sequence diagram in this
										// mapping allows its recovery in the
										// future by its name.
		return answer;
	}

	/**
	 * This method is responsible for building an entire sequence diagram by
	 * employing a recursive analysis of an FDTMC (given as input). The sequence
	 * diagram is also given as input, as each recursion step it is incremented
	 * with the elements gathered from the FDTMC fragment under analysis
	 * 
	 * @param currentState
	 *            the state from which the FDTMC analysis will start.
	 * @param fdtmc
	 *            the FDTMC subjected to the analysis for the creation of a
	 *            sequence diagram.
	 * @param sd
	 *            the sequence diagram that will be build incrementally as the
	 *            recursion steps are executed.
	 * @return the Sequence Diagram object represented by the sd input parameter
	 */
	private SequenceDiagramElement extractSDElementFromFDTMC(
			State currentState, FDTMC fdtmc, SequenceDiagram sd) {
		SequenceDiagramElement answer = null;

		// get all transitions leaving the current fdtmc state
		List<fdtmc.Transition> outgoingTransitions = fdtmc
				.getTransitionsBySource(currentState);

		// "cleaning" all the irrelevant transitions (to error state or
		// absorbing transition)
		outgoingTransitions = pruneErrorTransitions(fdtmc, outgoingTransitions);

		// for the remaining transitions we must identify which kind of SD
		// structure they represent and build it again.
		for (fdtmc.Transition t : outgoingTransitions) {
			/*
			 * FDTMC transitions abstracting sequence diagram fragments have the
			 * nXX form, where X is a digit. The set of FDTMC states and
			 * transitions generated by the parser for MagicDraw models has the 
			 * form for each fragment:
			 * 
			 * 	   nXX/1.0            ""/nXX            ""/1.0
			 * (s) -------> (  s  ) ---------> (  s  ) --------> (  s  )
			 *              initFr  \           endFr
			 *                       \-------> (  s  ) 
			 *                        ""/1-nXX  errorFr
			 */
			if (t.getActionName().matches("[n][a-zA-Z0-9]+")
					&& t.getProbability().equals("1.0")) {
				State initFr = t.getTarget(), endFr, errorFr;
				List<fdtmc.Transition> outTransInitFrag = fdtmc
						.getTransitionsBySource(initFr);
				for (fdtmc.Transition tr : outTransInitFrag) {
					if (tr.getProbability().matches("[n][0-9]+")) {
						endFr = tr.getTarget();

						Fragment fr = new Fragment(tr.getProbability());
						// REVIEW --> how to discover other kinds of fragments?
						fr.setType(Fragment.OPTIONAL);
						if (!fragmentByName.containsKey(tr.getProbability())) {
							fragmentByName.put(fr.getName(), fr);
						} else {
							fr = fragmentByName.get(tr.getProbability());
						}
						
						//add the fragment at the sequence diagram
						sd.addFragment(fr);

						//proceed building the sequence diagram from the next state
						State next = fdtmc
								.getTransitionsBySource(tr.getTarget()).get(0)
								.getTarget();
						extractSDElementFromFDTMC(next, fdtmc, sd);

					} else if (tr.getProbability().matches(
							"[1][\\s]*[-][\\s]*[a-zA-Z0-9]+")) {
						errorFr = tr.getTarget();
					}
				}
				
			/*
			 * In case a transition has a real value associated with its
			 * probability it represents a lifeline's reliability and thus
			 * it must be considered as a message (synchronous, asynchronous
			 * or reply) in the sequence diagram  
			 */
			} else if (t.getProbability().matches("^[0-1]\\.[0-9]+")) {
				Lifeline target = identifyLifelines(t);
				//REVIEW: how can we substitute the mocklifeline? 
				Lifeline source = mockLifeline;
				int mType = identifyMessageType(t);
				sd.createMessage(source, target, mType, t.getActionName(),
						target.getReliability());
				extractSDElementFromFDTMC(t.getTarget(), fdtmc, sd);
			}
		}

		return answer;
	}

	/**
	 * Auxiliary method for pruning the transitions from a "regular" state to
	 * the FDTMC's error state. Considering only the "regular" transitions
	 * simplifies the analysis of the FDTMC.
	 * 
	 * @param fdtmc
	 *            the FDTMC subject to the analysis
	 * @param outgoingTransitions
	 *            the set of outgoing transitions from a given state
	 * @return the set of "regular" transitions leaving a given FDTMC state
	 */
	private List<fdtmc.Transition> pruneErrorTransitions(FDTMC fdtmc,
			List<fdtmc.Transition> outgoingTransitions) {
		for (fdtmc.Transition t : outgoingTransitions) {
			if (t.getTarget() == fdtmc.getErrorState()
					|| (t.getSource() == fdtmc.getSuccessState() && t
							.getTarget() == fdtmc.getSuccessState())) {
				outgoingTransitions.remove(t);
			}
		}
		return outgoingTransitions;
	}

	private State nextState(State st, FDTMC fdtmc) {
		State answer = null;
		List<fdtmc.Transition> transitions = fdtmc.getTransitionsBySource(st);
		for (fdtmc.Transition t : transitions) {
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

		List<fdtmc.Transition> transitions = fdtmc.getTransitionsBySource(t
				.getTarget());
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

	/**
	 * This auxiliary method has the role of identifying which 
	 * lifeline is being used as target in a given transition. 
	 * All lifelines are associated with a reliability value 
	 * such that, for a given reliability value, its lifeline 
	 * is returned.
	 * @param trans 
	 * 			the transition from where the target lifeline 
	 * 			will be identified.
	 * @return the lifeline associated with the transition's 
	 * 			reliability value.
	 */
	private Lifeline identifyLifelines(fdtmc.Transition trans) {
		Lifeline answer = null;
		String toConfirm = trans.getProbability();
		if (toConfirm.matches("[0-9.]+")) {
			double probability = Double.parseDouble(trans.getProbability());

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

	/**
	 * Given an FDTMC transition, this auxiliary method returns 
	 * the message type that produced the fdtmc transition.
	 * @param trans 
	 * 			the input FDTMC transition
	 * @return the message type as defined at the Message class.
	 */
	private int identifyMessageType(fdtmc.Transition trans) {
		int answer = -1;
		if (trans.getActionName() == null || trans.getActionName().equals("")) { 
			// asynchronous messages do not have an associated action name
			answer = Message.ASYNCHRONOUS;
		} else { 
			// otherwise it can be a synchronous or reply message
			answer = Message.SYNCHRONOUS;
		}
		return answer;
	}
}
