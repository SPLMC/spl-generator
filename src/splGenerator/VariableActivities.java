package splGenerator;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;

public class VariableActivities extends VariableBehavioralParameters {

	/**
	 * Parameters to be define for the behavioral models' evolution provided by
	 * this class.
	 */
	// TODO we must change this class to define such values based on the given
	// SPL's models
	private int fragmentSize = 10; // =numberOfMessages/fragments
	private int numberOfAltFragments = 0;
	private int numberOfLoopFragments = 0;
	private static int idxSequenceDiagram = 0;
	private static int idxActivity = 0;
	private static int idxTransition = 0;
	private HashSet<Lifeline> setOfLifelines;
	private int idxMessage = 0;

	protected LinkedList<SPL> employTransformation(SPL spl)
			throws CloneNotSupportedException {
		LinkedList<SPL> answer = new LinkedList<SPL>();

		// 1st step: retrieve all lifelines already used by the SPL.
		setOfLifelines = new HashSet<Lifeline>();
		List<Activity> activities = spl.getActivityDiagram()
				.getSetOfActivities();
		for (Activity act : activities) {
			LinkedList<SequenceDiagram> sequenceDiagrams = act
					.getSequenceDiagrams();
			for (SequenceDiagram sd : sequenceDiagrams) {
				setOfLifelines.addAll(sd.getTransitiveLifeline());
			}
		}

		SPL seed = spl;

		while (currentValue <= maxValue) {
			SPL temp = createSplDeepCopy(seed);
			ActivityDiagram activityDiagram = temp.getActivityDiagram();

			// 2nd step: create activities according to the step value defined
			// by the user, each one with its related sequence diagram
			int nActCreated = 0;

			LinkedList<Activity> createdActivities = new LinkedList<Activity>();
			do {
				Activity act = (Activity) ActivityDiagramElement.createElement(
						ActivityDiagramElement.ACTIVITY, "createdActivity_"
								+ ++idxActivity);
				SequenceDiagram sd = createRandomSequenceDiagram();
				act.addSequenceDiagram(sd);
				createdActivities.add(act);
				nActCreated++;
			} while ((nActCreated % variationStep) != 0);

			// 3rd step: allocate the created activities randomly in the current
			// activity diagram
			for (Activity act : createdActivities) {
				allocateActivityRandomly2(act, activityDiagram);
			}
			
			addCharacteristic(temp, currentValue);

			// 4th step: add the variation to the set of generated SPLs.
			answer.add(temp);
			seed = temp;
			currentValue += variationStep;
		}

		return answer;
	}
	
	
	@Override
	public void addCharacteristic(SPL temp, int value) {
		temp.addCharacteristic("activities", value);
	}

	private void allocateActivityRandomly2(Activity act,
			ActivityDiagram activityDiagram) {

		// 1st step: create a list of activities
		List<Activity> setOfActivities = activityDiagram.getSetOfActivities();

		// 2nd step: choose an activity that one transition will be modified for
		// accommodating the new activity
		int indexActivity = new Random().nextInt(setOfActivities.size());
		Activity source = setOfActivities.get(indexActivity);

		// 3rd step: choose one transition of this activity to change in order
		// to accommodate the new activity
		Object[] setOfTransitions = source.getTransitions().toArray();
		System.out.println("source.transitions=" +source.getTransitions().size());
		int indexTransitions = new Random().nextInt(source.getTransitions()
				.size());
		Transition trans = (Transition) setOfTransitions[indexTransitions];

		// 4th step: recover all other important informations about the
		// transition
		ActivityDiagramElement oldTarget = trans.getTarget();
		String transName = trans.getElementName();

		// 5th step: create the new activity diagram structure, in order to
		// accommodate the new activity
		activityDiagram.addElement(act);
		boolean removeTrans = source.getTransitions().remove(trans);
		ActivityDiagramElement found = activityDiagram.getElementByName(trans
				.getElementName());
		activityDiagram.removeElement(found);

		Transition t = source.createTransition(act, "createdTransition_"
				+ idxTransition++, 1.0);
		activityDiagram.addElement(t);
		t = act.createTransition(oldTarget, "createdTransition_"
				+ idxTransition++, 1.0);
		activityDiagram.addElement(t);
	}

	private SequenceDiagram createRandomSequenceDiagram() {
		SequenceDiagram answer;
		Lifeline source, target;

		SequenceDiagram temp;
		do {
			temp = SequenceDiagram.createSequenceDiagram("createdSD_"
					+ ++idxSequenceDiagram, "true");
			source = randomLifeline();
			for (int i = 0; i < fragmentSize; i++) {
				target = randomLifeline();
				int type = randomMessageType();
				temp.createMessage(source, target, type, "createdMessage"
						+ ++idxMessage, target.getReliability());
				source = target;
			}
		} while (!isSetOfMessagesConsistent(temp.getElements()));
		answer = temp;
		idxSequenceDiagram++;

		return answer;
	}

	private int randomMessageType() {
		LinkedList<Integer> types = new LinkedList<Integer>();
		// types.add(Message.SYNCHRONOUS);
		types.add(Message.ASYNCHRONOUS);
		types.add(Message.REPLY);
		int position = new Random().nextInt(types.size());
		int answer = types.get(position);
		return answer;
	}

	private Lifeline randomLifeline() {
		Object[] lifelines = setOfLifelines.toArray();
		int position = new Random().nextInt(lifelines.length);

		Lifeline answer = (Lifeline) lifelines[position];
		return answer;
	}

}
