package tests;

import static org.junit.Assert.*;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.util.LinkedList;
import java.util.List;

import org.junit.Test;

import splGenerator.FeatureModelParameters;
import splGenerator.SPL;
import splGenerator.SplGenerator;
import splGenerator.VariableBehavioralParameters;
import splGenerator.Util.SPLFilePersistence;
import splGenerator.transformation.Transformer;
import splar.apps.generator.FMGeneratorEngine;
import splar.core.fm.FeatureModel;
import splar.core.fm.FeatureModelException;
import splar.core.fm.XMLFeatureModel;
import splar.core.fm.randomization.Random3CNFFeatureModel;
import splar.core.fm.randomization.RandomFeatureModel2;
import tool.RDGNode;

public class GhezzisWorkSimulationTest {

	@Test
	public void splcSimulationTest() throws CloneNotSupportedException,
			IOException {
		SplGenerator generator = SplGenerator.newInstance();

		// PROBLEM'S SPACE PARAMETERS
		FeatureModelParameters fmParameters = FeatureModelParameters
				.getConfiguration(FeatureModelParameters.GHEZZI_FEATURE_MODEL);
		generator.setNumberOfFeatures(10);
		generator.setFeatureModelParameters(fmParameters);

		// SOLUTION SPACE PARAMETERS
		generator.setNumberOfActivities(0);
		generator.setNumberOfDecisionNodes(0);

		generator.setFragmentSize(10);
		generator.setNumberOfLifelines(6);
		generator.setNumberOfReliabiliatiesValues(0.99, 0.99999, 5);
		generator.setNumberOfAltFragments(0);
		generator.setNumberOfLoopsFragments(0);

		// SPL GENERATION
		SPL spl = generator.generateSPL(SplGenerator.SPLOT,
				SplGenerator.SYMMETRIC);

		RDGNode root = new Transformer().transformAD(spl.getActivityDiagram());

		spl.getXmlRepresentation();
		SPLFilePersistence.rdg2Dot(root, "rdg");

		// Creating a set of SPLs objects that will be transformed
		LinkedList<SPL> splsList = new LinkedList<SPL>();
		splsList.add(spl);

		// Generate variations of SPL.
		VariableBehavioralParameters var = VariableBehavioralParameters
				.getInstance(VariableBehavioralParameters.LIFELINERELIABILITY);
		var.setVariationValues(3, 5, 1);
		// var.employTransformation(spl);
		LinkedList<SPL> answer = var.generateSplVariation(splsList);
		SPLFilePersistence.persistSPLs(answer);
	}

	@Test
	public void testFragmentsSizeVariation() throws CloneNotSupportedException,
			IOException {
		SplGenerator generator = SplGenerator.newInstance();

		// PROBLEM'S SPACE PARAMETERS
		FeatureModelParameters fmParameters = FeatureModelParameters
				.getConfiguration(FeatureModelParameters.GHEZZI_FEATURE_MODEL);
		generator.setNumberOfFeatures(10);
		generator.setFeatureModelParameters(fmParameters);

		// SOLUTION SPACE PARAMETERS
		generator.setNumberOfActivities(0);
		generator.setNumberOfDecisionNodes(0);

		generator.setFragmentSize(10);
		generator.setNumberOfLifelines(5);
		generator.setNumberOfReliabiliatiesValues(0.99, 0.99999, 5);
		generator.setNumberOfAltFragments(0);
		generator.setNumberOfLoopsFragments(0);

		// SPL GENERATION
		SPL spl = generator.generateSPL(SplGenerator.SPLOT,
				SplGenerator.SYMMETRIC);

		RDGNode root = new Transformer().transformAD(spl.getActivityDiagram());
		spl.getXmlRepresentation();

		// Creating a set of SPLs objects that will be transformed
		LinkedList<SPL> splsList = new LinkedList<SPL>();
		splsList.add(spl);

		// Generate variations of SPL
		VariableBehavioralParameters var = VariableBehavioralParameters
				.getInstance(VariableBehavioralParameters.FRAGMENTSIZE);
		var.setVariationValues(10, 15, 1);
		LinkedList<SPL> answer = var.generateSplVariation(splsList);
		SPLFilePersistence.persistSPLs(answer);
	}

	/***
	 * NEED TO BE REVISITED!
	 * 
	 * @throws CloneNotSupportedException
	 * @throws IOException
	 */
	@Test
	public void testNumberOfActivitiesVariation()
			throws CloneNotSupportedException, IOException {
		SplGenerator generator = SplGenerator.newInstance();

		// PROBLEM'S SPACE PARAMETERS
		FeatureModelParameters fmParameters = FeatureModelParameters
				.getConfiguration(FeatureModelParameters.GHEZZI_FEATURE_MODEL);
		generator.setNumberOfFeatures(10);
		generator.setFeatureModelParameters(fmParameters);

		// SOLUTION SPACE PARAMETERS
		generator.setNumberOfActivities(1);
		generator.setNumberOfDecisionNodes(0);

		generator.setFragmentSize(10);
		generator.setNumberOfLifelines(5);
		generator.setNumberOfReliabiliatiesValues(0.99, 0.99999, 5);
		generator.setNumberOfAltFragments(0);
		generator.setNumberOfLoopsFragments(0);

		// SPL GENERATION
		SPL spl = generator.generateSPL(SplGenerator.SPLOT,
				SplGenerator.SYMMETRIC);

		RDGNode root = new Transformer().transformAD(spl.getActivityDiagram());
		spl.getXmlRepresentation();

		// Creating a set of SPLs objects that will be transformed
		LinkedList<SPL> splsList = new LinkedList<SPL>();
		splsList.add(spl);

		// Generate variations of SPL
		VariableBehavioralParameters var = VariableBehavioralParameters
				.getInstance(VariableBehavioralParameters.ACTIVITIES);
		var.setVariationValues(1, 5, 2);
		LinkedList<SPL> answer = var.generateSplVariation(splsList);
		SPLFilePersistence.persistSPLs(answer);
	}

	@Test
	public void testNumberOfReplicatedFragments()
			throws CloneNotSupportedException, IOException {
		SplGenerator generator = SplGenerator.newInstance();

		// PROBLEM'S SPACE PARAMETERS
		FeatureModelParameters fmParameters = FeatureModelParameters
				.getConfiguration(FeatureModelParameters.GHEZZI_FEATURE_MODEL);
		generator.setNumberOfFeatures(10);
		generator.setFeatureModelParameters(fmParameters);

		// SOLUTION SPACE PARAMETERS
		generator.setNumberOfActivities(0);
		generator.setNumberOfDecisionNodes(0);

		generator.setFragmentSize(10);
		generator.setNumberOfLifelines(5);
		generator.setNumberOfReliabiliatiesValues(0.99, 0.99999, 5);
		generator.setNumberOfAltFragments(0);
		generator.setNumberOfLoopsFragments(0);

		// SPL GENERATION
		SPL spl = generator.generateSPL(SplGenerator.SPLOT,
				SplGenerator.SYMMETRIC);

		RDGNode root = new Transformer().transformAD(spl.getActivityDiagram());
		spl.getXmlRepresentation();
		SPLFilePersistence.rdg2Dot(root, "rdg");

		// Creating a set of SPLs objects that will be transformed
		LinkedList<SPL> splsList = new LinkedList<SPL>();
		splsList.add(spl);

		// Generate variations of SPL
		VariableBehavioralParameters var = VariableBehavioralParameters
				.getInstance(VariableBehavioralParameters.REPLICATEDFRAGMENTS);
		var.setVariationValues(1, 4, 1);
		LinkedList<SPL> answer = var.generateSplVariation(splsList);
		SPLFilePersistence.persistSPLs(answer);
	}

	@Test
	public void testTwoFactors() throws CloneNotSupportedException, IOException {
		SplGenerator generator = SplGenerator.newInstance();

		// PROBLEM'S SPACE PARAMETERS
		FeatureModelParameters fmParameters = FeatureModelParameters
				.getConfiguration(FeatureModelParameters.GHEZZI_FEATURE_MODEL);
		generator.setNumberOfFeatures(10);
		generator.setFeatureModelParameters(fmParameters);

		// SOLUTION SPACE PARAMETERS
		generator.setNumberOfActivities(0);
		generator.setNumberOfDecisionNodes(0);

		generator.setFragmentSize(10);
		generator.setNumberOfLifelines(5);
		generator.setNumberOfReliabiliatiesValues(0.99, 0.99999, 5);
		generator.setNumberOfAltFragments(0);
		generator.setNumberOfLoopsFragments(0);

		// SPL GENERATION
		SPL spl = generator.generateSPL(SplGenerator.SPLOT,
				SplGenerator.SYMMETRIC);

		RDGNode root = new Transformer().transformAD(spl.getActivityDiagram());
		spl.getXmlRepresentation();
		SPLFilePersistence.rdg2Dot(root, "rdg");

		// Creating a set of SPLs objects that will be transformed
		LinkedList<SPL> splsList = new LinkedList<SPL>();
		splsList.add(spl);

		// Generate variations of SPL
		LinkedList<SPL> answer = new LinkedList<SPL>();

		VariableBehavioralParameters var;

		// var =
		// VariableBehavioralParameters.getInstance(VariableBehavioralParameters.FRAGMENTSIZE);
		// var.setVariationValues(10, 14, 1);
		var = VariableBehavioralParameters
				.getInstance(VariableBehavioralParameters.ACTIVITIES);
		var.setVariationValues(1, 3, 1);
		answer = var.generateSplVariation(splsList);

		// var =
		// VariableBehavioralParameters.getInstance(VariableBehavioralParameters.LIFELINERELIABILITY);
		// var.setVariationValues(3, 5, 1);
		var = VariableBehavioralParameters
				.getInstance(VariableBehavioralParameters.REPLICATEDFRAGMENTS);
		var.setVariationValues(3, 5, 1);
		answer = var.generateSplVariation(answer);

		SPLFilePersistence.persistSPLs(answer);
	}

	@Test
	public void testFMVariation2() {

		SplGenerator generator = SplGenerator.newInstance();

		// Problem space definition
		FeatureModelParameters fmParameters = FeatureModelParameters
				.getConfiguration(FeatureModelParameters.GHEZZI_FEATURE_MODEL);
		generator.setFeatureModelParameters(fmParameters);
		generator.setNumberOfFeatures(10);

		// Solution space definition
		generator.setNumberOfActivities(1);
		generator.setNumberOfDecisionNodes(0);
		generator.setFragmentSize(10);
		generator.setNumberOfLifelines(6);
		generator.setNumberOfReliabiliatiesValues(0.990, 0.9999, 3);
		generator.setNumberOfAltFragments(0);
		generator.setNumberOfLoopsFragments(0);

		// SPL GENERATION
		SPL spl = generator.generateSPL(SplGenerator.SPLOT,
				SplGenerator.SYMMETRIC);

		System.out.println(spl.getCk());
		RDGNode root = new Transformer().transformAD(spl.getActivityDiagram());
		System.out.println("BEGIN: testFMVariation2()");
		spl.getXmlRepresentation();
		System.out.println("END: testFMVariation2()");
		SPLFilePersistence.rdg2Dot(root, "rdg");

		// Creating a set of SPLs objects that will be transformed
		LinkedList<SPL> splsList = new LinkedList<SPL>();
		splsList.add(spl);

		// Generate variations of SPL
		LinkedList<SPL> answer = new LinkedList<SPL>();

		VariableBehavioralParameters var;

			var = VariableBehavioralParameters
					.getInstance(VariableBehavioralParameters.NUMBEROFFEATURES);
			var.setVariationValues(12, 30, 2);
			try {
				answer = var.generateSplVariation(splsList);
				SPLFilePersistence.persistSPLs(answer);
			} catch (CloneNotSupportedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
	}

	@Test
	public void testFMVariation() {
		String filePath = "/home/andlanna/workspace2/reana/src/splGenerator/generatedModels/";
		String fileSufix = ".xml";

		SplGenerator generator = SplGenerator.newInstance();

		// Problem space definition
		FeatureModelParameters fmParameters = FeatureModelParameters
				.getConfiguration(FeatureModelParameters.GHEZZI_FEATURE_MODEL);
		generator.setFeatureModelParameters(fmParameters);
		generator.setNumberOfFeatures(10);

		// Solution space definition
		generator.setNumberOfActivities(1);
		generator.setNumberOfDecisionNodes(0);
		generator.setFragmentSize(10);
		generator.setNumberOfLifelines(6);
		generator.setNumberOfReliabiliatiesValues(0.990, 0.9999, 3);
		generator.setNumberOfAltFragments(0);
		generator.setNumberOfLoopsFragments(0);

		Object o = generator.generateFeatureModel(SplGenerator.SPLOT);
		splar.core.fm.FeatureModel fm0 = (splar.core.fm.FeatureModel) o;

		int i = 0;

		File file = new File(filePath + i + fileSufix);
		try {
			PrintStream oldOut = java.lang.System.out;
			PrintStream out = new PrintStream(file);
			java.lang.System.setOut(out);
			fm0.dumpXML();
			out.flush();
			out.close();
			java.lang.System.setOut(oldOut);
			System.out.println("troca com sucesso");
		} catch (FileNotFoundException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}

		SplGenerator generator2 = SplGenerator.newInstance();
		generator.setFeatureModelParameters(fmParameters);
		generator.setNumberOfFeatures(3);

		Object o2 = generator.generateFeatureModel(SplGenerator.SPLOT);
		FeatureModel fm2 = (FeatureModel) o2;

		try {
			// FeatureModel f = engine.generateFeatureModel(1, 2, 0.0, 0.0);
			fm2.setName("teste");
			fm2.dumpXML();
			fm2.dumpFeatureIdeXML();
			// fms.get(0).setName("teste");
			// fms.get(0).dumpXML();
			// fms.get(0).dumpFeatureIdeXML();
		} catch (Exception e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}

		XMLFeatureModel fm1 = new XMLFeatureModel(file.getAbsolutePath());
		File file2 = new File(filePath + 1 + fileSufix);
		try {
			fm1.loadModel();
			PrintStream oldOut = java.lang.System.out;
			PrintStream out = new PrintStream(file2);
			java.lang.System.setOut(out);
			fm1.dumpXML();
			out.flush();
			out.close();
			java.lang.System.setOut(oldOut);
		} catch (FeatureModelException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		assertEquals(fm0.getAverageDepth(), fm1.getAverageDepth(), 0);
	}

}
