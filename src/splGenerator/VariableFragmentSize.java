package splGenerator;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.Random;

public class VariableFragmentSize extends VariableBehavioralParameters {

	private static int messageId = 0;

	@Override
	protected LinkedList<SPL> employTransformation(SPL spl)
			throws CloneNotSupportedException {
		LinkedList<SPL> answer = new LinkedList<SPL>();

		SPL next = spl;
		// The transformation will be applied while the maximum number of
		// messages is not reached
		while (currentValue <= maxValue) {
			SPL temp = createSplDeepCopy(next);

			// 1st step: check if all sequence diagrams have the number of
			// messages equals to the number of minimum messages defined by the attribute
			// minValue of this class's superclass.
			LinkedList<SequenceDiagram> seqDiags = new LinkedList<SequenceDiagram>();
			for (Activity a : temp.getActivityDiagram().getSetOfActivities()) {
				for (SequenceDiagram s : a.getSequenceDiagrams()) {
					try {
						seqDiags.addAll(getTransitiveSequenceDiagrams(s));
					} catch (InsuficientNumberOfMessagesException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}

			// 2nd step: in case it is possible to create random messages, for
			// each sequence diagram a set of messages (given by the step value) is
			// created considering the set of lifelines initially defined for
			// the "seed" sequence diagram
			for (SequenceDiagram s : seqDiags) {
				int size = s.getElements().size();
				int position = new Random().nextInt(size);
				LinkedList<SequenceDiagramElement> randomMessages = createRandomMessages(
						variationStep, s.getLifelines());
				s.getElements().addAll(position, randomMessages);
			}

			// 3rd step: for each transformation applied, we must create a SPL
			// object, add it to the set of SPLs created (represented by a
			// LinkedList) and then return the set of SPLs.
			answer.add(temp);
			next = temp;
			currentValue += variationStep; // increase the current value by the
											// step value
		}

		return answer;
	}

	/**
	 * This method is responsible for creating a set of messages (whose size is
	 * given by the variationStep parameter), comprising the lifelines passed by
	 * the parameter lifelines. It returns a sequence of messages represented by
	 * a linked list.
	 * 
	 * @param variationStep
	 *            The number of messages to be created
	 * @param lifelines
	 *            The set of lifelines which will be used for creating the
	 *            random messages.
	 * @return the messages sequence represented by a linked list.
	 */
	private LinkedList<SequenceDiagramElement> createRandomMessages(
			int variationStep, HashSet<Lifeline> lifelines) {
		LinkedList<SequenceDiagramElement> answer = null;
		Message m = null;
		boolean consistentSetOfMessages = false;
		while (!consistentSetOfMessages) {
			answer = new LinkedList<SequenceDiagramElement>();
			int i = 0;
			while (i < variationStep) {
				SequenceDiagramElement e = SequenceDiagramElement
						.createElement(SequenceDiagramElement.MESSAGE,
								"added_message_" + messageId++);
				m = (Message) e;
				Lifeline source = randomLifeline(lifelines);
				Lifeline target = randomLifeline(lifelines);
				m.setSource(source);
				m.setTarget(target);
				m.setProbability(target.getReliability());
				m.setType(randomMessageType());
				answer.add(m);
				i++;
			}
			consistentSetOfMessages = isSetOfMessagesConsistent(answer);
		}

		return answer;
	}

	/**
	 * This message is responsible for choosing randomly if a message is
	 * synchronous, asynchronous or reply
	 * 
	 * @return an integer related to the constants Message.SYNCHRONOUS,
	 *         Message.ASYNCHRONOUS or Message.REPLY
	 */
	private int randomMessageType() {
		int answer = -1;
		LinkedList<Integer> values = new LinkedList<Integer>();
		values.add(Message.SYNCHRONOUS);
		values.add(Message.ASYNCHRONOUS);
		values.add(Message.REPLY);
		int index = new Random().nextInt(values.size());
		answer = values.get(index);
		return answer;
	}

	/**
	 * This method is responsible for randomly choosing a lifeline from a given
	 * set of lifelines
	 * 
	 * @param lifelines
	 *            the lifelines which will be subject to random choose.
	 * @return a lifeline randomly choosed.
	 */
	private Lifeline randomLifeline(HashSet<Lifeline> lifelines) {
		Lifeline answer = null;
		int i = new Random().nextInt(lifelines.size());
		Object[] vector = lifelines.toArray();
		answer = (Lifeline) vector[i];
		return answer;
	}

	/**
	 * This method returns all the sequence diagrams reachable from a given
	 * sequence diagram. It searches for sequence diagrams, in a recursive
	 * fashion, considering all the fragments represented at the input sequence
	 * diagram. As this method is used when the behavioral models of a given SPL
	 * are being evolved, in case a sequence diagram containing less messages
	 * than the minimum number of messages defined by the minValue attribute, an
	 * InsuficientNumberOfMessagesException will be thrown.
	 * 
	 * @param seqDiag
	 *            the input sequence diagram from the others sequence diagrams
	 *            will be searched.
	 * @return a linked list of sequence diagram objects.
	 * @throws InsuficientNumberOfMessagesException
	 *             in case
	 */
	private LinkedList<SequenceDiagram> getTransitiveSequenceDiagrams(
			SequenceDiagram seqDiag)
			throws InsuficientNumberOfMessagesException {
		LinkedList<SequenceDiagram> answer = new LinkedList<SequenceDiagram>();

		int numMessages = countMessages(seqDiag);
		if (numMessages < minValue) {
			throw new InsuficientNumberOfMessagesException("");
		} else {
			answer.add(seqDiag);
			for (Fragment f : seqDiag.getFragments()) {
				for (SequenceDiagram s : f.getSequenceDiagrams()) {
					answer.addAll(getTransitiveSequenceDiagrams(s));
				}
			}
		}
		return answer;
	}

	/**
	 * Auxiliary method for counting the number of messages of a given Sequence
	 * Diagram.
	 * 
	 * @param seqDiag
	 *            the given sequence diagram for which the messages will be
	 *            counted
	 * @return an integer representing the number of messages contained into the
	 *         sequence diagram
	 */
	private int countMessages(SequenceDiagram seqDiag) {
		int numMessages = 0;
		for (SequenceDiagramElement e : seqDiag.getElements()) {
			if (e instanceof splGenerator.Message) {
				numMessages++;
			}
		}
		return numMessages;
	}

}
