package splGenerator;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Collection;
import java.util.LinkedList;

import splGenerator.Util.SPLFilePersistence;
import jdk.nashorn.internal.ir.WhileNode;

/**
 * This class is responsible for generating models considering the variation of
 * the parameters involved into Feature and Behavioral models comprising the
 * Software Product Line. Each model is build by employing a parameter variation
 * of its previous model. So this class is responsible for taking a SPL object
 * as input and manipulate it according to the parameter's variation. Each
 * subclass implements a parameter variation.
 * 
 * @author andlanna
 *
 */
public abstract class VariableBehavioralParameters {

	public static final int ACTIVITIES = 0;
	public static final int SEQUENCEDIAGRAMSIZE = 1;
	public static final int FRAGMENTSIZE = 2;
	public static final int DECISIONNODES = 3;
	public static final int SCATTEREDFRAGMENTS = 4; // per presence condition
	public static final int REPLICATEDFRAGMENTS = 5; // per presence condition
	public static final int LIFELINERELIABILITY = 6;
	public static final int CONFIGURATIONS = 7;
	public static final int NUMBEROFFEATURES = 8;

	/**
	 * Factory method for getting an instance of a VariableBehavioralParameters
	 * subclass, according to the input parameters. Parameter's values are
	 * defined as constants in this class, and are listed in the following:
	 * 
	 * @param option
	 * @return
	 */
	public static VariableBehavioralParameters getInstance(int option) {
		VariableBehavioralParameters answer = null;

		switch (option) {
		case LIFELINERELIABILITY:
			answer = new VariableLifelineReliability();
			break;

		case FRAGMENTSIZE:
			answer = new VariableFragmentSize();
			break;

		case ACTIVITIES:
			answer = new VariableActivities();
			break;

		case REPLICATEDFRAGMENTS:
			answer = new VariableReplicatedFragments();
			break;

		case NUMBEROFFEATURES:
			answer = new VariableNumberOfFeatures(); 
			break; 
			
		default:
			answer = null;
			break;
		}

		return answer;
	}

	protected int minValue;
	protected int maxValue;
	protected int variationStep;
	protected int currentValue;
	protected SPLGeneratorParameters generatorParameters;


	public VariableBehavioralParameters() {
		this.minValue = 0;
		this.maxValue = 0;
		this.variationStep = 0;
		this.currentValue = 0;
	}

	public void setVariationValues(int minValue, int maxValue, int variationStep) {
		this.minValue = minValue;
		this.maxValue = maxValue;
		this.variationStep = variationStep;
		this.currentValue = this.minValue;
	}

	/**
	 * This method is responsible for employing the variations at the input
	 * software product lines. For each Software Product Line passed contained
	 * in the input parameter, it apply the transformation defined by the method
	 * employTransformation (that is implemented differently according to each
	 * subclass). The transformation will be applied at the variable parameter
	 * according to the subclass instantiated. While the variable parameter's
	 * value is within the range (min and max value), a set of different models
	 * will be created, according to the step value. The result is a set of SPL
	 * objects containing different (evolved) Feature or Behavioral Models.
	 * 
	 * @param spls
	 *            This input parameter contains a set of SPL objects,
	 *            represented by a linked list, which will be subject to the
	 *            variations applied at the variable parameter.
	 * @return The function's return is a set of SPL objects, containing all the
	 *         variations of the input SPLs according to the variable parameter.
	 * @throws CloneNotSupportedException
	 * @throws IOException
	 */

	public LinkedList<SPL> generateSplVariation(LinkedList<SPL> spls)
			throws CloneNotSupportedException, IOException {
		LinkedList<SPL> answer = new LinkedList<SPL>();
		for (SPL s : spls) { // for each spl to be transformed,
			currentValue = minValue;
			answer.addAll(employTransformation(s));
		}
		return answer;
	}

	protected abstract LinkedList<SPL> employTransformation(SPL spl)
			throws CloneNotSupportedException;

	public int getCurrentValue() {
		return currentValue;
	}

	public int getMaximumValue() {
		return maxValue;
	}

	public int getVariationStep() {
		return variationStep;
	}

	/**
	 * This method is responsible for ensure a sequence of messages randomly
	 * generated is consistent. A (piece of) sequence diagram is consistent if
	 * all synchronous messages have a reply message associated or, if it is
	 * formed by a single message, such message must be asynchronous.
	 * 
	 * @param messages
	 *            the sequence of messages that will be inspected
	 * @return true if the set of messages is consistent, otherwise false is
	 *         returned
	 */
	protected boolean isSetOfMessagesConsistent(
			LinkedList<SequenceDiagramElement> messages) {
		boolean answer = false;
		if (messages.size() == 1) { // if it is a singleton set of messages, the
									// message must be asynchronous
			Message m = (Message) messages.get(0);
			if (m.getType() == Message.ASYNCHRONOUS)
				answer = true;
			else
				answer = false;
		} else { // if the set of new messages has more than one message, we
					// must ensure no synchronous message remains without a
					// reply message (it is not mandatory asynchronous messages
					// have reply messages associated)
			int pendingSyncMessages = 0;
			for (SequenceDiagramElement sde : messages) {
				Message m = (Message) sde;
				if (m.getType() == Message.SYNCHRONOUS) {
					pendingSyncMessages++;
				} else if (m.getType() == Message.REPLY) {
					pendingSyncMessages--;
				}
			}
			if (pendingSyncMessages == 0)
				answer = true;
			else
				answer = false;
		}
		return answer;
	}

	/**
	 * This method's role is to create a SPL clone for a given SPL. As clone()
	 * method offered by Java implements a shallow copy of an object we wrote
	 * this method for creating a copy of all objects related to a SPL. To
	 * accomplish this task, this method persists the whole SPL at a temporary
	 * file and read it again in memory, when new and distinct objects are
	 * created.
	 * 
	 * @param spl
	 *            The software product line that will be cloned
	 * @return the cloned software product line
	 */
	protected SPL createSplDeepCopy(SPL spl) {
		SPL answer = null;
		ActivityDiagram.reset();
		ActivityDiagramElement.reset();
		SequenceDiagram.reset();
		SequenceDiagramElement.reset();

		File f;
		try {
			f = File.createTempFile("spl", ".xml");
			f.deleteOnExit();
			FileOutputStream stream = new FileOutputStream(f);
			stream.write(spl.getXmlRepresentation().getBytes());
			stream.flush();
			stream.close();
			SPL t = SPL.getSplFromXml(f.getAbsolutePath()); // its "deep copy"
															// is produced
			t.setName("model_" + currentValue);
			t.setFeatureModel(spl.getFeatureModel());
			answer = t;
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return answer;
	}


}
