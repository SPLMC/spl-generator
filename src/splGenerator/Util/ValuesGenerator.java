package splGenerator.Util;

import java.math.BigDecimal;
import java.util.HashSet;
import java.util.Random;

import com.sun.org.apache.xpath.internal.operations.And;
import com.sun.xml.internal.ws.api.pipe.NextAction;

public class ValuesGenerator {

	private static HashSet<Double> reliabilityValues;
	private static double minReliabibilityValue;
	private static double maxReliabilityValue;
	private static int numOfReliabilitiesValues;
	private static int reliabilityPrecision;
	
	private static int idxReliabilityValue = 0;  
	
	

	/**
	 * This method is responsible for creating a given number of different
	 * reliability values, according to the value informed by the input
	 * parameter.
	 * 
	 * @param numberOfValues
	 *            number of different reliability values to be created
	 */
	public static void generateRandomReliabilityValues(int numberOfValues) {
		reliabilityValues = new HashSet<Double>();
		Random r = new Random();

		int idxValues = 0;
		while (idxValues < numberOfValues) {
			double value = minReliabibilityValue + (maxReliabilityValue - minReliabibilityValue)
					* r.nextDouble();
			boolean answer = reliabilityValues.add(value);
			if (answer == true)
				idxValues++;
			else if (minReliabibilityValue == maxReliabilityValue)
				break;
		}
		numOfReliabilitiesValues = reliabilityValues.size();
		idxReliabilityValue = 0;
//		System.out.println("numOfReliabilitiesValues= " + numOfReliabilitiesValues);
//		System.out.println("idxReliabilityValue= " + idxReliabilityValue);
	}

	/**
	 * This method returns a random reliability value according to the minimum
	 * and maximum values previously informed.
	 * 
	 * @return a reliability value (double)
	 */
	public static double getReliabilityValue() {
		Object[] values = reliabilityValues.toArray();
		int p = idxReliabilityValue % numOfReliabilitiesValues;
		idxReliabilityValue++;
//		System.out.println("idxReliabilityValue= " + idxReliabilityValue);
		return (double) values[p]; 
	}

	/**
	 * Set the initial value for the reliability's range.
	 * 
	 * @param minValue
	 */
	public static void setMinReliabilityValue(double minValue) {
		minReliabibilityValue = minValue;
	}

	/**
	 * Set the final value for the reliability's range.
	 * 
	 * @param maxValue
	 */
	public static void setMaxReliabilityValue(double maxValue) {
		maxReliabilityValue = maxValue;
	}

	/**
	 * Set the number of reliability values which will be created in the
	 * reliability's range.
	 * 
	 * @param numOfValues
	 */
	public static void setNumOfReliabilitiesValues(int numOfValues) {
		numOfReliabilitiesValues = numOfValues;
	}

	/**
	 * Set the number of decimal places for representing the reliability's
	 * values randomly generated
	 * 
	 * @param precision 
	 */
	public static void setReliabilityPrecision(int precision) {
		reliabilityPrecision = precision; 
	}

	public static int getPrecision() {
		return reliabilityPrecision;
	}
}
