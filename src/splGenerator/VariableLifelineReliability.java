package splGenerator;

import java.math.BigDecimal;
import java.util.Formatter;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;

public class VariableLifelineReliability extends VariableBehavioralParameters {

	protected VariableLifelineReliability() {
		// TODO Auto-generated constructor stub
	}

	protected LinkedList<SPL> employTransformation(SPL spl)
			throws CloneNotSupportedException {
		LinkedList<SPL> answer = new LinkedList<SPL>();

		SPL seed = spl;
		while (currentValue <= maxValue) {
			SPL temp = createSplDeepCopy(seed);

			// 1st step: recover all lifelines of all sequence diagrams used by
			// the SPL's behavioral model.
			HashSet<Lifeline> lifelines = new HashSet<Lifeline>();
			List<Activity> activities = temp.getActivityDiagram()
					.getSetOfActivities();
			for (Activity a : activities) {
				lifelines.addAll(a.getTransitiveLifelines());
			}

			// 2nd step: change the reliability values of all lifelines used by
			// the SPL's behavioral model.
			for (Lifeline l : lifelines) {
				double d = BigDecimal.valueOf(l.getReliability())
						.setScale(currentValue, BigDecimal.ROUND_FLOOR)
						.doubleValue();
				l.setReliability(d);
			}

			// 3rd step: change the reliability values of messages used by all
			// the sequence diagrams.
			// TODO we should refactor the code in order to allow the reuse of
			// lifelines' reliability values when setting the message's value
			// (instead of copying it).

			LinkedList<SequenceDiagram> sequenceDiagrams = new LinkedList<SequenceDiagram>();
			for (Activity a : temp.getActivityDiagram().getSetOfActivities()) {
				for (SequenceDiagram s : a.getSequenceDiagrams()) {
					sequenceDiagrams.addAll(getTransitiveSequenceDiagrams(s));
				}
				for (SequenceDiagram s : sequenceDiagrams) {
					transformMessageReliability(s);
				}
			}
			answer.add(temp);
			seed = temp;
			currentValue += variationStep;
		}
		return answer;
	}

	/**
	 * Auxiliary method for transforming the message's reliability according to
	 * the number of decimal places used for its representation
	 * 
	 * @param s
	 *            sequence diagram whose message's reliabilities will be
	 *            transformed.
	 */
	private void transformMessageReliability(SequenceDiagram s) {
		LinkedList<SequenceDiagramElement> elements = s.getElements();
		for (SequenceDiagramElement e : elements) {
			if (e instanceof Message) {
				Message m = (Message) e;
				String probability = new Formatter().format(Locale.ENGLISH,
						"%." + currentValue + "f", (float) m.getProbability())
						.toString();
				m.setProbability(Double.parseDouble(probability));
			}
		}
	}

	/**
	 * Auxiliary method for getting all the sequence diagrams (by considering
	 * its transitive closure) given a sequence diagram as input.
	 * 
	 * @param s
	 *            sequence diagram from which all transitive sequence diagram
	 *            are going to be recovered.
	 * @return linked list of SequenceDiagram objects recovered from a sequence
	 *         diagram passed by parameter.
	 */
	private LinkedList<SequenceDiagram> getTransitiveSequenceDiagrams(
			SequenceDiagram s) {
		LinkedList<SequenceDiagram> answer = new LinkedList<SequenceDiagram>();

		HashSet<Fragment> frags = s.getFragments();
		for (Fragment f : frags) {
			for (SequenceDiagram seq : f.getSequenceDiagrams()) {
				answer.addAll(getTransitiveSequenceDiagrams(seq));
			}
		}
		answer.add(s);
		return answer;
	}
}
