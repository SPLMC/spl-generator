package tests;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.List;

import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import splGenerator.parsing.RDGBuilder;
import tool.CyclicRdgException;
import tool.RDGNode;


@RunWith(Parameterized.class)
public class RDGCreationFromMagicDrawTest {

	private RDGNode root; 
	private static String behavioralModel = "/home/andrelanna/eclipse-workspace/spl-generator/src/seedModels/BSN/UML_BSN.xml";
	private String id;
	private String presenceCondition;
	private static RDGBuilder builder;

	
	public RDGCreationFromMagicDrawTest(String id, String pc) {
		this.id = id;
		this.presenceCondition = pc;
	}
	
	@BeforeClass
	public static void setup() {
		builder = new RDGBuilder(behavioralModel);
	}
	
	@Parameters
	public static Iterable getParameters() {
		return Arrays.asList(new Object[][] {
			{"BSN", "true"},
			{ "QoSChange", "true" },
			{ "Capture", "true" },
			{ "n1", "SPO2" },
			{ "n3", "ACC" },
			{ "n2", "TEMP" },
			{ "n0", "ECG" },
			{ "Reconfiguration", "true" },
			{ "Situation", "true" },
			{ "n16", "Temperature" },
			{ "n4", "Fall" },
			{ "n7", "Oxygenation" },
			{ "n13", "PulseRate" },
			{ "n10", "Position" }, 
			{ "n6", "Memory"},
			{ "n5", "SQLite"}
		});
	}
	
	
	/**
	 * Test if behavioral models are represented in memory accordingly
	 * after reading them from magicdraw files	
	 */
	@Test
	public void readMagicDrawModels() {
		
		//Test if builder is instantiated accordingly
		assertNotNull(builder);
		
		
		//Test if all RDG nodes have an id and presence condition 
		//BSN has 14 RDG nodes, including the root node. 
		root = builder.getRDGNode();
		List<RDGNode> dependencies = null;
		try {
			dependencies = root.getDependenciesTransitiveClosure();
		} catch (CyclicRdgException e) {
			e.printStackTrace();
		}
		
		assertEquals("Number of RDG nodes is different of expected." + dependencies.size(), 
				16, 
				dependencies.size());
		
		boolean encontrou = false;
		for (RDGNode r : dependencies) {
			if (r.getId().equalsIgnoreCase(id) && 
					r.getPresenceCondition().equalsIgnoreCase(presenceCondition)) {
				encontrou = true;
				break;
			}
		}
		assertTrue(encontrou);
	}
}
