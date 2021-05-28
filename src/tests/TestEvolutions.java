package tests;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
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
import splar.core.constraints.BooleanVariableInterface;
import splar.core.constraints.CNFFormula;
import splar.core.constraints.CNFLiteral;
import splar.core.fm.FeatureModel;
import splar.core.fm.FeatureTreeNode;
import splar.core.fm.RootNode;
import splar.core.fm.personalization.PersonalFeatureModel;
import tool.CyclicRdgException;
import tool.RDGNode;


@RunWith(Parameterized.class)
public class TestEvolutions{

	private RDGNode root; 
//	private static String behavioralModel = "/home/igorbispo/Downloads/spl-generator/bin/generatedModels/evo_fragmento/3.xml";
	private static String behavioralModel = "/home/igorbispo/Downloads/spl-generator/src/seedModels/CloudComputing/UML_CloudComputing.xml";
	private static String featureModel = "/home/igorbispo/Downloads/spl-generator/src/seedModels/CloudComputing/fm_CloudComputing.xml";
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
		
		//CNFFormula nodes = pfm.FT2CNF();
		spl.setFeatureModel(pfm);
		ActivityDiagram ad = spl.getActivityDiagram();
		
		/******** Adicionando 10 Mensagens *********/
		/*
		String[] lifelines = {"Lifeline_0", "Mock lifeline"};
		
		Random r = new Random();
		
		String path = "/home/igorbispo/Downloads/spl-generator/bin/generatedModels/lift_evo/evo_msg/";
				
		for (int i = 1; i < 10 * 21; i++) {
			String fragName = "n0";
			
			int msgNumber = r.nextInt(lifelines.length);
			assertEquals("Nao pode adicionar mensagem", 0, addMessage(ad, "sequenceDiagram1", fragName,
					lifelines[msgNumber], lifelines[(msgNumber + 1) % 2], Message.SYNCHRONOUS, "Msg n." + Integer.toString(i), 0.99));
			
			
			if (i % 10 == 0) {
			
				String xmlContent = spl.getXmlRepresentation();
				String file_name = Integer.toString(i / 10) + ".xml";
				FileWriter fw = new FileWriter(new File(path + file_name));
				
				fw.write(xmlContent);
				fw.close();
			}
		}*/
		
		/******** Adicionando um fragmento opcional com 10 mensagens ********/
		
		/*
		String path = "/home/igorbispo/Downloads/spl-generator/bin/generatedModels/lift_evo/evo_frag/";
		int n_sd_0 = 2;
		int n_f_0 = 0; 
		for (int i = 1;i < 21; i++) {
			assertEquals("Frag_new não foi adicionado", 0, addFragment(ad, "sequenceDiagram1",
					"Fragment_"+Integer.toString(n_f_0), "SD_"+Integer.toString(n_sd_0), "true", 10));
			
			String xmlContent = spl.getXmlRepresentation();
			String file_name = Integer.toString(i) + ".xml";
			FileWriter fw = new FileWriter(new File(path + file_name));
			
			fw.write(xmlContent);
			fw.close();
			
			n_sd_0 += 3;
			n_f_0 += 2;
		}
	*/
	
		/******* Modificando presence condition "fortalecer" *********/
		
		String path = "/home/igorbispo/Downloads/spl-generator/bin/generatedModels/new_evo//cloud_evo/fortalecer/";
		ArrayList<String> features = new ArrayList<String>(Arrays.asList("VirtualMachine", "PriceModel", "OnDemand", "Reserved", "Spot", "InstanceType", "HardwareConfiguration", "ComputeUnit", "BusSize", "b32",
				"b64", "RamMemory", "Network", "FamilyType", "Compute", "General", "Accelerator", "GPU", "IntelPhi", "FPGA", "Memory", "Storage", "InstanceDisk", "VMI", "VirtualizationTechnique", "HVM", "PVM",
				"OperatingSystem", "Platform", "Linux", "Windows", "Architecture", "x32", "x64", "SoftwarePackage", "Region", "Africa", "Asia", "Australia", "NorthAmerica", "Europe", "SouthAmerica", "Disk", "DiskType",
				"Ephemeral", "Persistent", "ObjectStore", "DiskTechnology", "SSD", "Standard", "Zone", "Group"));		
		String old_pc = features.get(0);
		int feature_added_idx = 0;

		Random r = new Random();
		
		for (int i = 1;i < 21; i++) {
			
			assertEquals("Presence condition não foi alterada", 0, changePresenceCondition(ad, "executingAnApplicationInTheArchitecture", "n0", old_pc));
			features.remove(feature_added_idx);
			
			if (features.size() == 0) break;
			
			feature_added_idx = r.nextInt(features.size());
			
			old_pc += " && " + features.get(feature_added_idx);
			
			String xmlContent = spl.getXmlRepresentation();
			String file_name = Integer.toString(i) + ".xml";
			FileWriter fw = new FileWriter(new File(path + file_name));
			
			fw.write(xmlContent);
			fw.close();
		}
		
		
	
		/******** Modificando presence condition "enfraquecer" *********/
		/*
		String path = "/home/igorbispo/Downloads/spl-generator/bin/generatedModels/new_evo/cloud_evo/enfraquecer/";
		ArrayList<String> features = new ArrayList<String>(Arrays.asList("VirtualMachine", "PriceModel", "OnDemand", "Reserved", "Spot", "InstanceType", "HardwareConfiguration", "ComputeUnit", "BusSize", "b32",
				"b64", "RamMemory", "Network", "FamilyType", "Compute", "General", "Accelerator", "GPU", "IntelPhi", "FPGA", "Memory", "Storage", "InstanceDisk", "VMI", "VirtualizationTechnique", "HVM", "PVM",
				"OperatingSystem", "Platform", "Linux", "Windows", "Architecture", "x32", "x64", "SoftwarePackage", "Region", "Africa", "Asia", "Australia", "NorthAmerica", "Europe", "SouthAmerica", "Disk", "DiskType",
				"Ephemeral", "Persistent", "ObjectStore", "DiskTechnology", "SSD", "Standard", "Zone", "Group"));
		
		String old_pc = features.get(0);
		int feature_added_idx = 0;
		
		Random r = new Random();
		
		for (int i = 1;i < 21; i++) {
			assertEquals("Presence condition não foi alterada", 0, changePresenceCondition(ad, "executingAnApplicationInTheArchitecture", "n0", old_pc));
			features.remove(feature_added_idx);
			
			if (features.size() == 0) break;
			
			feature_added_idx = r.nextInt(features.size());
			
			old_pc += " || " + features.get(feature_added_idx);
			
			String xmlContent = spl.getXmlRepresentation();
			String file_name = Integer.toString(i) + ".xml";
			FileWriter fw = new FileWriter(new File(path + file_name));
			
			fw.write(xmlContent);
			fw.close();
		}	
		*/	
		
		
		/******** Adicionando uma mensagem em Capture *********/
		//assertEquals("Nova Msg Igor não foi adicionada", 0, addMessage(ad, "Capture", "n0", "Lifeline_0", "Mock lifeline", Message.SYNCHRONOUS, "Nova Msg Igor", 0.999));
		
		/******** Adicionando uma mensagem em Situation ********/
		//assertEquals("Nova Msg Igor2 não foi adicionada", 0, addMessage(ad, "Situation", "n10", "Mock lifeline", "Lifeline_0", Message.ASYNCHRONOUS, "Nova Msg Igor2", 0.9));
		
		/******** Adicionando um fragmento em Capture ********/
		//assertEquals("Frag_new não foi adicionado", 0, addFragment(ad, "Capture", "Frag_new", "Temperature", "ACC", 0));
		
		/******** Adicionando uma mensagem no fragmento recém adicionado (Frag_new) ********/
		//assertEquals("Nova Msg Igor3 não foi adicionada", 0, addMessage(ad, "Capture", "Frag_new", "Mock lifeline", "Lifeline_0", Message.ASYNCHRONOUS, "Nova Msg Igor3", 0.7));
	
		
		// Persisting models
		

	
	}
	
	Fragment getRandomFragment(Activity act) {
		HashSet<Fragment> frags = act.getSequenceDiagrams().getFirst().getFragments();
		
		Random r = new Random();
		
		int fragIdx = r.nextInt(frags.size());
		int i = 0;
		
		for (Fragment f_: frags) {
			if (i == fragIdx) return f_;
			i += 1;
		}
		
		return null;	
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
		
		/*
		HashSet<Fragment> frag = seq.getFragments();
		
		Fragment f = getFragment(frag, fragName);
		
		if (f == null) return -1;
		
		Lifeline src = getLifeline(f.getTransitiveLifeline(), srcName);
		Lifeline dst = getLifeline(f.getTransitiveLifeline(), dstName);
		
		*/
		Lifeline src = getLifeline(seq.getTransitiveLifeline(), srcName);
		Lifeline dst = getLifeline(seq.getTransitiveLifeline(), dstName);		
		
		if (src == null || dst == null) return -1;
		
		//Fragment frag = (Fragment) SequenceDiagramElement.getElementByName(fragName);
		
		/*
		LinkedList<SequenceDiagram> seq_diag = f.getSequenceDiagrams();
		seq_diag.get(0).createMessage(src, dst, type, msgName, prob);
		*/
		
		seq.createMessage(src, dst, type, msgName, prob);
		
		return 0;
		
	}
	
	// Add fragment named "fragName" at "ad" ActivityDiagram
	int addFragment(ActivityDiagram ad, String actName, String fragName, String rowName, String guardName, int msgAmount) {
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
		
		if (msgAmount != 0) {
			String[] lifelines = {"Lifeline_0", "Mock lifeline"};
			
			Random r = new Random();
			
			for (int i = 0; i < msgAmount; i++) {
				int msgNumber = r.nextInt(lifelines.length);
				
				assertEquals("Nao pode adicionar mensagem", 0, addMessage(ad, actName, fragName,
						lifelines[msgNumber], lifelines[(msgNumber + 1) % 2], Message.SYNCHRONOUS, "Msg n." + Integer.toString(i), 0.99));
			}
					
		}
		
		return 0;
		
	}
	
	int changePresenceCondition(ActivityDiagram ad, String actName, String fragName, String newPresenceCondition) {
		Activity act = ad.getActivityByName(actName);
		
		if (act == null) return -1;
		
		SequenceDiagram seq = act.getSequenceDiagrams().getFirst();
		
		HashSet<Fragment> frag = seq.getFragments();
		
		Fragment f = getFragment(frag, fragName);
		
		if (f == null) return -1;
		
		SequenceDiagram sd = f.getSequenceDiagrams().getFirst();
		sd.setGuard(newPresenceCondition);
		
		return 0;
	}
	
	ArrayList<String> get_all_features_name(SPL spl) {
		ArrayList<String> features = new ArrayList<String>();
		CNFFormula formula = spl.getFeatureModel().FT2CNF();
		
		for (BooleanVariableInterface f: formula.getVariables()) {
			features.add(f.getID());
		}
		
		return features;
	}

	
	
}




