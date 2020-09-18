package tests;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;
import java.util.Scanner;

import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import splGenerator.Activity;
import splGenerator.ActivityDiagram;
import splGenerator.FeatureModelParameters;
import splGenerator.Fragment;
import splGenerator.Lifeline;
import splGenerator.Message;
import splGenerator.SPL;
import splGenerator.SequenceDiagram;
import splGenerator.SequenceDiagramElement;
import splGenerator.SplGenerator;
import splGenerator.VariableBehavioralParameters;
import splGenerator.Util.SPLFilePersistence;
import splGenerator.parsing.RDGBuilder;
import splar.core.fm.FeatureModel;
import splar.core.fm.personalization.PersonalFeatureModel;
import tool.CyclicRdgException;
import tool.RDGNode;


@RunWith(Parameterized.class)
public class TestEvolutions{

	private RDGNode root; 
	private static String behavioralModel = "/home/igorbispo/Downloads/spl-generator/src/seedModels/BSN/UML_BSN.xml";
	private static String featureModel = "/home/igorbispo/Downloads/spl-generator/src/seedModels/BSN/fm_BSN.xml";
	private String id;
	private String presenceCondition;
	private static RDGBuilder builder;



	
	public TestEvolutions (String id, String pc) {
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
			{"BSN", "true"}
		});
	}


	/**
	 * Test if behavioral evolutions are being doing accordingly
	 * after reading them from magicdraw files	
	 * @throws IOException 
	 * @throws CloneNotSupportedException 
	 */
	@Test
	public void testEvo() throws CloneNotSupportedException, IOException {
		
		//Test if builder is instantiated accordingly
		assertNotNull(builder);
		
		root = builder.getRDGNode();
		
		SplGenerator generator = SplGenerator.newInstance();
				
		// SPL GENERATION
		SPL spl = SPL.createSplFromRDG(root);
		
		spl.getXmlRepresentation();
		SPLFilePersistence.rdg2Dot(root, "rdg");

		PersonalFeatureModel pfm = new PersonalFeatureModel();
		pfm.loadFMfromFeatureIDEXML(featureModel);
		spl.setFeatureModel(pfm);
		
		ActivityDiagram ad = spl.getActivityDiagram();
				
		// Adicionando uma mensagem em Capture
		assertEquals("Nova Msg Igor não foi adicionada", 0, addMessage(ad, "Capture", "n0", "Lifeline_0", "Mock lifeline", Message.SYNCHRONOUS, "Nova Msg Igor", 0.999));
				
		// Adicionando uma mensagem em Situation
		assertEquals("Nova Msg Igor2 não foi adicionada", 0, addMessage(ad, "Situation", "n10", "Mock lifeline", "Lifeline_0", Message.ASYNCHRONOUS, "Nova Msg Igor2", 0.9));
		
		// Adicionando um fragmento em Capture
		assertEquals("Frag_new não foi adicionado", 0, addFragment(ad, "Capture", "Frag_new", "Temperature", "ACC"));
		
		// Adicionando uma mensagem no fragmento recém adicionado (Frag_new)
		assertEquals("Nova Msg Igor3 não foi adicionada", 0, addMessage(ad, "Capture", "Frag_new", "Mock lifeline", "Lifeline_0", Message.ASYNCHRONOUS, "Nova Msg Igor3", 0.7));
	
		
		// Creating a set of SPLs objects that will be transformed
		LinkedList<SPL> splsList = new LinkedList<SPL>();
		splsList.add(spl);
		
		SPLFilePersistence.persistSPLs(splsList);
	
	}
	
	Fragment getFragment(HashSet<Fragment> f, String fragname) {
		for (Fragment f_: f) {
			if (f_.getName().equalsIgnoreCase(fragname))
				return f_;
		}
		
		return null;
	}
	
	Lifeline getLifeline(HashSet<Lifeline> l, String lname) {
		for (Lifeline l_: l) {
			if (l_.getName().equalsIgnoreCase(lname))
				return l_;
		}
		
		return null;
	}	
	
	// Add message at fragment "fragName" at "ad" ActivityDiagram
	int addMessage(ActivityDiagram ad, String actName, String fragName, String srcName,
			String dstName, int type, String msgName,  double prob) {
		Activity act = ad.getActivityByName(actName);
	
		if (act == null) return -1;
		
		SequenceDiagram seq = act.getSequenceDiagrams().getFirst();
		
		HashSet<Fragment> frag = seq.getFragments();
		
		Fragment f = getFragment(frag, fragName);
		
		if (f == null) return -1;
		
		Lifeline src = getLifeline(f.getTransitiveLifeline(), srcName);
		Lifeline dst = getLifeline(f.getTransitiveLifeline(), dstName);
		
		if (src == null || dst == null) return -1;
		
		//Fragment frag = (Fragment) SequenceDiagramElement.getElementByName(fragName);
		LinkedList<SequenceDiagram> seq_diag = f.getSequenceDiagrams();
		seq_diag.get(0).createMessage(src, dst, type, msgName, prob);
		
		return 0;
		
	}
	
	// Add fragment named "fragName" at "ad" ActivityDiagram
	int addFragment(ActivityDiagram ad, String actName, String fragName, String rowName, String guardName) {
		Activity act = ad.getActivityByName(actName);
	
		if (act == null) return -1;
		
		SequenceDiagram seq = act.getSequenceDiagrams().getFirst();
				
		Fragment frag_new = (Fragment) Fragment.createElement(SequenceDiagramElement.FRAGMENT, fragName);
		frag_new.setType(Fragment.OPTIONAL);
				
		if (frag_new.addSequenceDiagram(rowName, guardName) == null) return -1;
		
		SequenceDiagram frag_new_seq = frag_new.getSequenceDiagrams().get(0);
		
		frag_new_seq.createLifeline("Mock lifeline");
		frag_new_seq.createLifeline("Lifeline_0");
		
		seq.addFragment(frag_new);
		
		return 0;
		
	}
	
}


