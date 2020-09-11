package splGenerator.Util;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.SortedSet;
import java.util.TreeSet;

import fdtmc.FDTMC;
import fdtmc.State;
import fdtmc.Transition;
import splGenerator.SPL;
import splGenerator.transformation.Transformer;
import splar.core.fm.FeatureModel;
import tool.RDGNode;

public class SPLFilePersistence {

	private static String modelsPath = "/home/igorbispo/Downloads/spl-generator/src/generatedModels/";
	private static String cnfFilePrefix = "cnf_";
	private static String rdgFilePrefix = "rdg_";
	private static String fdtmcFilePrefix = "fdtmc_";
//	private static String dotFilePrefix = "dot_";
	private static String splName = "spl";

	/**
	 * This method is responsible for creating a text file with the CNF content
	 * representing a Feature Model. The feature model is passed as input
	 * parameter. The method returns the File object representing the file
	 * recently created.
	 * 
	 * @param fm
	 *            the Feature Model object containing the feature model to be
	 *            persisted.
	 * @return the File object representing the file containing the CNF
	 *         representation of the feature model.
	 */
	public static File FM2JavaCNF(FeatureModel fm) {
		File answer = null;

		Path path = Paths.get(modelsPath + cnfFilePrefix + splName + ".txt");
		String fileContent = fm.FM2JavaCNF();
		byte[] buffer = fileContent.getBytes();

		Path a;
		try {
			a = Files.write(path, buffer);
			answer = a.toFile();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return answer;

	}

	/**
	 * This function is responsible for creating a DOT representation for an
	 * FDTMC passed as an argument. The resulting graph is persisted in a DOT
	 * file, named as the value passed by the parameter "name".
	 * 
	 * @param f
	 *            - the FDTMC that will be persisted at a DOT file.
	 * @param name
	 *            - the filename of the DOT file.
	 * @author andlanna
	 */
	public static File fdtmc2Dot(FDTMC f, String name) {
		File answer = null;
		StringBuilder builder = new StringBuilder();

		builder.append("digraph graphname {\n");

		// creation of states strings
		for (State s : f.getStates()) {
			String entry = s.getVariableName() + s.getIndex();
			builder.append(entry + " [");
			builder.append("label=\"" + entry + "\"");

			if (s.getLabel() != null) {
				if (s.getLabel().equals("initial")) {
					builder.append(",color=blue,shape=doublecircle");
				}
				if (s.getLabel().equals("success")) {
					builder.append(",color=green,shape=doublecircle");
				}
				if (s.getLabel().equals("error")) {
					builder.append(",color=red,shape=doublecircle");
				}
			}
			builder.append(" ];\n");
		}

		// Creation of edges in graph
		for (State s : f.getStates()) {
			String sourceEntry = s.getVariableName() + s.getIndex();
			// System.out.println(sourceEntry);
			// for (State s1: f.getTransitions().keySet()) {
			// System.out.println(s1.getLabel() + s.getIndex());
			// }
			for (Transition t : f.getTransitions().get(s)) {
				State target = t.getTarget();
				String targetEntry = target.getVariableName()
						+ target.getIndex();
				
				builder.append(sourceEntry);
				builder.append(" -> ");
				builder.append(targetEntry);
				builder.append(" [label=\"" + t.getActionName() + " / ");
				builder.append(t.getProbability() + "\"");

				if (target.getLabel() != null)
					if (target.getLabel().equals("error"))
						builder.append(", style=dotted");
				builder.append("];");
				builder.append("\n");
			}
		}

		builder.append("}");
		Path path = Paths.get(modelsPath + fdtmcFilePrefix + name + ".dot"); 
		byte[] fileContent = builder.toString().getBytes();

		Path a = null;
		// Persist the dot content into a file
		try {
			a = Files.write(path, fileContent);
		} catch (IOException e) {
			// TODO: handle exception
			e.printStackTrace();
		}
		answer = a.toFile();
		return answer;
	}

	/**
	 * This method is responsible to create an RDG representation using the DOT
	 * format. It receives an RDG node and the filename as input parameters,
	 * creates the DOT structure and persist it at an DOT file at the specified
	 * folder.
	 * 
	 * @param node
	 *            the initial RDG node from where the structure will be created
	 * @param filename
	 *            the filename where the DOT structure will be persisted.
	 */
	public static void rdg2Dot(RDGNode node, String filename) {
		StringBuilder builder = new StringBuilder();

		builder.append("digraph graphname {\n");

		String nodesDefinition = createNodeDefinition(node);
		builder.append(nodesDefinition);

		String edgesDefinition = createEdgeDefinition(node);
		builder.append(edgesDefinition);

		builder.append("}");
		try {
			// FileWriter file = new FileWriter(modelsPath + dotFilePrefix +
			// "RDG_" + filename + ".dot");
			FileWriter file = new FileWriter(modelsPath + rdgFilePrefix
					+ filename + ".dot");
			file.write(builder.toString());
			file.flush();
			file.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private static String createEdgeDefinition(RDGNode node) {
		StringBuilder answer = new StringBuilder();

		for (RDGNode r : node.getDependencies()) {
			answer.append(node.getId());
			answer.append(" -> ");
			answer.append(r.getId());
			answer.append(";");
			answer.append("\n");

			answer.append(createEdgeDefinition(r));
		}
		return answer.toString();
	}

	private static String createNodeDefinition(RDGNode node) {
		StringBuilder answer = new StringBuilder();

		answer.append(node.getId());
		answer.append("[");

		answer.append("shape=record");
		answer.append(", ");

		answer.append("label=\"{");
		answer.append("{");
		answer.append(node.getId());
		answer.append("|");
		answer.append(node.getPresenceCondition());
		answer.append("|");
		answer.append(node.getFDTMC().getVariableName());
		answer.append("}");
		answer.append("}\"");

		answer.append("];");
		answer.append("\n");

		for (RDGNode n : node.getDependencies()) {
			answer.append(createNodeDefinition(n));
		}
		return answer.toString();
	}

	public static void persistSPLs(LinkedList<SPL> spls) {
		String ancientModelsPath = modelsPath;
		for (SPL spl : spls) {
			int index = spls.indexOf(spl);
			index++; //Number of model starts from 1.
			String path = modelsPath + index + "/";
			modelsPath = path;
			File dir = new File(path); 
			dir.mkdirs(); 
			
//			FM2JavaCNF(spl.getFeatureModel());
			Transformer t = new Transformer(); 
			RDGNode r = t.transformAD(spl.getActivityDiagram());
			rdg2Dot(r, "rdg");
			
			spl.setName(Integer.toString(spls.indexOf(spl) + 1));
			
			spl.getXmlRepresentation();
			
			
			
			//print the featureIDE files
			PrintStream oldOut = java.lang.System.out;
			String filePath = dir.getAbsolutePath() + "/" + "fmIDE_" + index + ".xml";
			File fOut = new File(filePath);
			
			try {
				PrintStream p = new PrintStream(fOut);
				java.lang.System.setOut(p);
				System.out.println(spl.getFeatureModel().dumpFeatureIdeXML());
				p.flush();
				p.close();
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} 
			java.lang.System.setOut(oldOut);
			modelsPath = ancientModelsPath;
		}
		
		generateListOfSPLs(spls);
	}

	private static void generateListOfSPLs(LinkedList<SPL> spls) {
		StringBuilder chars = new StringBuilder();
		for (SPL spl : spls) {
			HashMap<String, Integer> ch = spl.getAdditionalCharacteristics();
			SortedSet<String> keys = new TreeSet<String>(ch.keySet());
			chars.append(spl.getName() + ',');
			chars.append(spls.indexOf(spl) + 1 + "_behavioral_model.xml,");
			chars.append(spls.indexOf(spl) + 1 + ".txt,");
			for (String key : keys) {
				chars.append(key);
				chars.append(',');
				chars.append(ch.get(key));
				chars.append(',');
			}
			chars.append('\n');
		}
		
		PrintStream ancientStream = System.out;
		File dir= new File(modelsPath);
		String filePath = dir.getAbsolutePath() + "/available_spl" ;
		File fOut = new File(filePath);
		System.out.println(filePath);
		try {
			PrintStream p = new PrintStream(fOut);
			java.lang.System.setOut(p);
			System.out.print(chars);
			p.flush();
			p.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		java.lang.System.setOut(ancientStream);
		
	}

	public static void setModelsPath(String path) {
		modelsPath = path;
	}
}
