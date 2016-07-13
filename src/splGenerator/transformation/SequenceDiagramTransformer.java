package splGenerator.transformation;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;

import splGenerator.Fragment;
import splGenerator.Lifeline;
import splGenerator.Message;
import splGenerator.SequenceDiagram;
import splGenerator.SequenceDiagramElement;
import splGenerator.Util.SPLFilePersistence;
import splGenerator.Util.ValuesGenerator;
import tool.RDGNode;
import fdtmc.*;

public class SequenceDiagramTransformer {

	RDGNode root;
	HashMap<String, fdtmc.State> fdtmcStateById;

	public SequenceDiagramTransformer() {
		fdtmcStateById = new HashMap<String, fdtmc.State>();
		root = null;
	}

	public RDGNode transformSD(SequenceDiagram s) {
		FDTMC f = new FDTMC();
		f.setVariableName(s.getName() + "_s");
		f.createErrorState();
		RDGNode answer = new RDGNode(s.getName(), s.getGuardCondition(), f);
		root = answer;

		LinkedList<SequenceDiagramElement> sde = (LinkedList<SequenceDiagramElement>) s
				.getElements().clone();
		State s0 = f.createInitialState();
		State target = transformSdElement(sde, f);
		f.createTransition(s0, target, "", Double.toString(1.0));
		// State s0 = transformSdElement(sde, f);
		// s0.setLabel(FDTMC.INITIAL_LABEL);

//		System.out.println(f);
		SPLFilePersistence.fdtmc2Dot(f, s.getName());

		return answer;
	}

	private State transformSdElement(LinkedList<SequenceDiagramElement> sde,
			FDTMC f) {
		State source;
		State target;

//		SequenceDiagramElement e = sde.removeFirst();
		SequenceDiagramElement e = null;
		String sdClass;
		if (sde.isEmpty()) {
			target = f.createSuccessState();
			return target;
		} else {
			e = sde.removeFirst();
			sdClass = e.getClass().getSimpleName();
			target = transformSdElement(sde, f);
		}

		source = f.createState();

//		String sdClass = e.getClass().getSimpleName();

		switch (sdClass) {
		case "Message":
			Message m = (Message) e;
			if (m.getType() == Message.SYNCHRONOUS) {
				// Double probability = new BigDecimal(m.getProbability())
				// .setScale(ValuesGenerator.getPrecision(),
				// BigDecimal.ROUND_HALF_UP).doubleValue();
				// Double failure = new BigDecimal(1 - m.getProbability())
				// .setScale(ValuesGenerator.getPrecision(),
				// BigDecimal.ROUND_HALF_UP).doubleValue();
				Double probability = m.getProbability();
				Double failure = 1 - m.getProbability();
				f.createTransition(source, target, m.getName(),
						probability.toString());
				f.createTransition(source, f.getErrorState(), m.getName(),
						failure.toString());
				// f.createTransition(source, target, m.getName(),
				// Double.toString(m.getProbability()));
				// f.createTransition(source, f.getErrorState(), m.getName(),
				// Double.toString(1 - m.getProbability()));
			}
			if (m.getType() == Message.ASYNCHRONOUS) {
				f.createTransition(source, target, m.getName(),
						Double.toString(m.getProbability()));
				f.createTransition(source, f.getErrorState(), m.getName(),
						Double.toString(1 - m.getProbability()));
			}
			break;

		case "Fragment":
			Fragment fr = (Fragment) e;
//			System.out.println(fr.getName());
			if (fr.getType() == Fragment.OPTIONAL) {
				f.createTransition(source, target, "", fr.getSequenceDiagrams()
						.getFirst().getName());
				f.createTransition(source, f.getErrorState(), "", "1-"
						+ fr.getSequenceDiagrams().getFirst().getName());
				
				for (SequenceDiagram s : fr.getSequenceDiagrams()) {
					SequenceDiagramTransformer transformer = new SequenceDiagramTransformer();
					this.root.addDependency(transformer.transformSD(s));
				}
			} else if (fr.getType() == Fragment.ALTERNATIVE) {
				for (SequenceDiagram s : fr.getSequenceDiagrams()) {
					target = transformSdElement(s.getElements(), f);
					f.createTransition(source, target, "alt", Double.toString(1/fr.getSequenceDiagrams().size())); 
				}
			}
			break;

		default:
			break;
		}

		return source;
	}

}
