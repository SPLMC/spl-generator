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
import java.util.Scanner;

import splGenerator.Activity;
import splGenerator.ActivityDiagram;
import splGenerator.ActivityDiagramElement;
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

	private static final ActivityDiagram NULL = null;

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		
		Scanner scan = new Scanner(System.in);
		
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
				SplGenerator.SYMMETRIC, null,null, null);
		
		//Pega o diagrama de atividades
		ActivityDiagram evolutioned = spl.getActivityDiagram();
		//System.out.println(evolutioned.getName());
		
		List<ActivityDiagramElement> setOfElements = evolutioned.getSetOfElements();
		
		RDGNode root = new Transformer().transformAD(spl.getActivityDiagram());
		
		int option = 1;
		
		//Menu de evolução
		loop: while(option!=0) {
			System.out.println("Boas vindas ao SPL generator. Deseja fazer alguma alteração nos modelos UML ou manter o estado atual dele?");
			System.out.println("1- Fazer alteração");
			System.out.println("2- Manter estado atual");
			System.out.println("0- Sair");
			
			String input = scan.nextLine();
			option = Integer.parseInt(input);
			
			System.out.println(option);
			
			switch(option) {
			case 1:
				
				System.out.println("1- Alterar nome do diagrama de atividades");
				System.out.println("2- Alterar nome de uma atividade");
				System.out.println("3- Adicionar atividade");
				System.out.println("4- Deletar atividade");
				System.out.println("5- Alterar nome de um fragmento");
				System.out.println("6- Adicionar fragmento");
				System.out.println("7- Deletar fragmento");
				System.out.println("8- Alterar nome de uma mensagem");
				System.out.println("9- Adicionar mensagem");
				System.out.println("10- Deletar mensagem");
				System.out.println("0- Cancelar");
				System.out.println("Escolha uma opção de alteração:");
				
				String input2 = scan.nextLine();
				int option2 = Integer.parseInt(input2);
				
				switch(option2) {
				case 1:
					
					muda_nome(1, generator, spl, evolutioned,setOfElements);
					break;
					
				case 2:
					
					muda_nome(2, generator, spl, evolutioned,setOfElements);
					break;
					
				case 3:
					
					adiciona(1, generator, spl, evolutioned,setOfElements);
					break;
					
				case 4:
					
					deleta(1, generator, spl, evolutioned,setOfElements);
					break;
					
				case 5:
					
					muda_nome(3, generator, spl, evolutioned,setOfElements);
					break;
					
				case 6:
					
					adiciona(2, generator, spl, evolutioned,setOfElements);
					break;
					
				case 7:
					
					deleta(2, generator, spl, evolutioned,setOfElements);
					break;
					
				case 8:
					
					muda_nome(1, generator, spl, evolutioned,setOfElements);
					break;
					
				case 9:
					
					adiciona(3, generator, spl, evolutioned,setOfElements);
					break;
					
				case 10:
					
					deleta(3, generator, spl, evolutioned,setOfElements);
					break;
					
				case 0:
					System.out.println("Alteração cancelada");
					break;
				}
				break;
			case 2:
				System.out.println(option);
				System.out.println("Sem alteração");
				spl.getXmlRepresentation();
				break;
				
			case 0:
				break loop;
			default:
				System.out.println("Insira um valor válido");
			}
		}
		
		
		/*
		List<Activity> listinha = evolutioned.getSetOfActivities();
		Activity a = listinha.get(0);
		System.out.println(a.getElementName());
		*/
		
		root = new Transformer().transformAD(spl.getActivityDiagram());

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
	
	public static void muda_nome(int elemento, SplGenerator generator, SPL spl, ActivityDiagram evolutioned,
								 List<ActivityDiagramElement> setOfElements) {
		switch (elemento) {
			case 1:
				evolutioned.setName("Diagrama_de_atividades");
				
				spl = generator.generateSPL(SplGenerator.SPLOT,
						SplGenerator.SYMMETRIC, evolutioned,null,null);
				
				ActivityDiagram evolutioned2 = spl.getActivityDiagram();
				System.out.println(evolutioned2.getName());
				
				spl.getXmlRepresentation();
				
				System.out.println("Alteração feita");
				break;
			case 2:
				Activity atividade_alterada = evolutioned.getActivityByName("Activity_0");
				
				atividade_alterada.setElementName("Atividade_Suprema");
				
				System.out.println(setOfElements.get(1));
				setOfElements.set(1, atividade_alterada);
				
				spl = generator.generateSPL(SplGenerator.SPLOT,
						SplGenerator.SYMMETRIC, null,setOfElements, null);
				
				System.out.println("Atividade alterada");
				break;
			case 3:
				spl = generator.generateSPL(SplGenerator.SPLOT,
						SplGenerator.SYMMETRIC, null,null,"nome_frag");
				
				spl.getXmlRepresentation();
				System.out.println("Nome do fragmento alterado");
				break;
			case 4:
				spl = generator.generateSPL(SplGenerator.SPLOT,
						SplGenerator.SYMMETRIC, null,null,"nome_mensagem");
				
				spl.getXmlRepresentation();
				System.out.println("Nome da mensagem alterado");
				break;
				
		}
	}
	
	public static void adiciona(int elemento, SplGenerator generator, SPL spl, ActivityDiagram evolutioned,
						 List<ActivityDiagramElement> setOfElements) {
		switch (elemento) {
			case 1:
				Activity atividade_nova = new Activity("Atividade_nova");
				
				setOfElements.add(atividade_nova);
				spl = generator.generateSPL(SplGenerator.SPLOT,
						SplGenerator.SYMMETRIC, null,setOfElements, null);
				
				spl.getXmlRepresentation();
				
				System.out.println("Atividade adicionada");
				break;
			case 2:
				spl = generator.generateSPL(SplGenerator.SPLOT,
						SplGenerator.SYMMETRIC, null,null,"fragmento");
				
				spl.getXmlRepresentation();
				
				System.out.println("Fragmento adicionado");
				break;
			case 3:
				spl = generator.generateSPL(SplGenerator.SPLOT,
						SplGenerator.SYMMETRIC, null,null,"Mensagem");
				
				spl.getXmlRepresentation();
				System.out.println("Mensagem adicionada");
				break;
			
		}
			
	}
	
	public static void deleta(int elemento, SplGenerator generator, SPL spl, ActivityDiagram evolutioned,
					   List<ActivityDiagramElement> setOfElements) {
		switch (elemento) {
			case 1:
				System.out.println(setOfElements.get(1));
				setOfElements.remove(1);
				spl = generator.generateSPL(SplGenerator.SPLOT,
						SplGenerator.SYMMETRIC, null,setOfElements, null);
				
				spl.getXmlRepresentation();
				
				System.out.println("Atividade deletada");
				break;
			case 2:
				spl = generator.generateSPL(SplGenerator.SPLOT,
						SplGenerator.SYMMETRIC, null,null,"delfrag");
				spl.getXmlRepresentation();
				System.out.println("Fragmento deletado");
				break;
			case 3:
				spl = generator.generateSPL(SplGenerator.SPLOT,
						SplGenerator.SYMMETRIC, null,null,"delmsg");
				
				spl.getXmlRepresentation();
				
				System.out.println("Mensagem deletada");
				break;
		
	}
		
	}
}
