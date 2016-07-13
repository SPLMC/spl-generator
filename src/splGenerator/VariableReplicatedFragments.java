package splGenerator;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Random;

import com.sun.org.apache.xerces.internal.impl.xpath.XPath.Step;

public class VariableReplicatedFragments extends VariableBehavioralParameters {

	HashMap<Integer, LinkedList<Fragment>> fragmentsBySize;

	@Override
	protected LinkedList<SPL> employTransformation(SPL spl)
			throws CloneNotSupportedException {
		LinkedList<SPL> answer = new LinkedList<SPL>();

		LinkedList<Fragment> usedFragments = new LinkedList<Fragment>();
		SPL seed = createSplDeepCopy(spl);
		do {
			// 1st step: create a mapping of fragment's lists, whose keys are
			// the size of inner fragments for a given fragment
			SPL temp = seed;
			createFragmentsMapping(temp);
			printFragmentsMapping();
			// 2nd step: reuse how many fragments are necessary to achieve the
			// number of fragments to be replicated given by the current value,
			// respecting the value of step variation
//			int pendingFragments = currentValue;
			int pendingFragments = variationStep;
			LinkedList<Fragment> choosedFragments = new LinkedList<Fragment>();
			do {
				// Filter those fragments containing less or equal fragments
				// than the current pending fragments value
				LinkedList<Fragment> candidateFragments = new LinkedList<Fragment>();
				for (Integer i : fragmentsBySize.keySet()) {
					if (i + 1 <= pendingFragments) {
						candidateFragments.addAll(fragmentsBySize.get(i));
					}
				}
				candidateFragments.removeAll(usedFragments);

				// Select a fragment from candidates and decrease the number of
				// fragments replicated from the number of pending fragments
				int position = new Random().nextInt(candidateFragments.size());
				Fragment choosed = candidateFragments.remove(position);
				int choosedSize = countFragments(choosed);
				choosedFragments.add(choosed);
				usedFragments.add(choosed);
				System.out.println("Choosed " + choosed.getName() + ": " + choosedSize);
				pendingFragments -= choosedSize + 1;
			} while (pendingFragments > 0);

			// 3rd step: choose other fragments than the choosed ones, so their
			// sequence diagrams can receive the choosed fragments for
			// replication

			HashSet<Fragment> receiversFragments = getAllSplFragments(temp);
			System.out.println("|receiversFragments| = " + receiversFragments.size());
			receiversFragments.removeAll(choosedFragments);
			System.out.println("|receiversFragments - choosed| = " + receiversFragments.size());
			receiversFragments.removeAll(usedFragments);
			System.out.println("|receiversFragments - choosed - used| = " + receiversFragments.size());
			while (choosedFragments.size() > 0) {
				Object[] recvs = receiversFragments.toArray();
				int position = new Random().nextInt(recvs.length);
				Fragment receiver = (Fragment) recvs[position];
				receiversFragments.remove(receiver);

				Object[] repls = choosedFragments.toArray();
				position = new Random().nextInt(repls.length);
				Fragment replicated = (Fragment) repls[position];
				choosedFragments.remove(replicated);

				SequenceDiagram s = receiver.getSequenceDiagrams().get(0);
				position = new Random().nextInt(s.getElements().size());
				s.getElements().add(position, replicated);
			}
			answer.add(temp);
			seed = createSplDeepCopy(temp);
			currentValue += variationStep;
		} while (currentValue <= maxValue);

		return answer;
	}

	private HashSet<Fragment> getAllSplFragments(SPL spl) {
		HashSet<Fragment> answer = new HashSet<Fragment>();

		for (Activity a : spl.getActivityDiagram().getSetOfActivities()) {
			for (SequenceDiagram s : a.getSequenceDiagrams()) {
				answer.addAll(s.getTransitiveFragments());
			}
		}
		return answer;
	}

	private int countFragments(Fragment choosed) {
		int answer = 0;
		for (SequenceDiagram s : choosed.getSequenceDiagrams()) {
			for (Fragment f : s.getFragments()) {
				answer += countFragments(f);
				answer += 1;
			}
		}
		return answer;
	}

	private void createFragmentsMapping(SPL temp) {
		fragmentsBySize = new HashMap<Integer, LinkedList<Fragment>>();

		for (Activity a : temp.getActivityDiagram().getSetOfActivities()) {
			for (SequenceDiagram sd : a.getSequenceDiagrams()) {
				for (Fragment fr : sd.getFragments()) {
					buildMappingEntry(fr);
				}
			}
		}
	}

	private Integer buildMappingEntry(Fragment fr) {
		int answer = 0;
		for (SequenceDiagram sd : fr.getSequenceDiagrams()) {
			for (Fragment f : sd.getFragments()) {
				answer++;
				answer += buildMappingEntry(f);
			}
		}
		if (!fragmentsBySize.containsKey(answer)) {
			LinkedList<Fragment> list = new LinkedList<Fragment>();
			list.add(fr);
			fragmentsBySize.put(answer, list);
		} else {
			fragmentsBySize.get(answer).add(fr);
		}
		return answer;
	}

	private void printFragmentsMapping() {
		for (Integer i : fragmentsBySize.keySet()) {
			System.out.print(i + ": ");
			for (Fragment f : fragmentsBySize.get(i)) {
				StringBuilder str = new StringBuilder();
				str.append(f.getName());
				str.append("(");
				for (SequenceDiagram s : f.getSequenceDiagrams()) {
					str.append(s.getName());
					str.append(" ");
				}
				str.append(")");
				System.out.print(str.toString() + " -> ");
			}
			System.out.println();
		}
	}
}
