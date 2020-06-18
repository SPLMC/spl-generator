package ui;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.util.LinkedList;
import java.util.List;

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

public class CommandLineInterface {

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		
		SplGenerator generator = SplGenerator.newInstance();

		// PROBLEM'S SPACE PARAMETERS
		FeatureModelParameters fmParameters = FeatureModelParameters
				.getConfiguration(FeatureModelParameters.GHEZZI_FEATURE_MODEL);
		generator.setNumberOfFeatures(2);
		generator.setFeatureModelParameters(fmParameters);

		// SOLUTION SPACE PARAMETERS
		generator.setNumberOfActivities(0);
		generator.setNumberOfDecisionNodes(0);

		generator.setFragmentSize(10);
		generator.setNumberOfLifelines(6);
		generator.setNumberOfReliabiliatiesValues(0.99, 0.99999, 5);
		generator.setNumberOfAltFragments(2);
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
		LinkedList<SPL> answer = null;
		try {
			answer = var.generateSplVariation(splsList);
		} catch (CloneNotSupportedException | IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		SPLFilePersistence.persistSPLs(answer);
	}

}
