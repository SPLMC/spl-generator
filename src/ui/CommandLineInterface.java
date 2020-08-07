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
	private static SPL spl;
	private static SplGenerator generator;
	private static List<ActivityDiagramElement> setOfElements;
	

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		
		Scanner scan = new Scanner(System.in);
		
		generator = SplGenerator.newInstance();

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
		spl = generator.generateSPL(SplGenerator.SPLOT,
				SplGenerator.SYMMETRIC, null,null, null);
		
		//Pega o diagrama de atividades
		ActivityDiagram evolutioned = spl.getActivityDiagram();
		//System.out.println(evolutioned.getName());
		
		setOfElements = evolutioned.getSetOfElements();
		
		RDGNode root = new Transformer().transformAD(spl.getActivityDiagram());
		
		int option = 1;
		
		boolean show_menu = exec_args(args);
		
		if (!show_menu) return;
		
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
					Activity atividade_nova = new Activity("Atividade_nova");
					
					setOfElements.add(atividade_nova);
					spl = generator.generateSPL(SplGenerator.SPLOT,
							SplGenerator.SYMMETRIC, null,setOfElements, null);
					
					spl.getXmlRepresentation();
					
					System.out.println("Atividade adicionada");
					break;
				case 4:
					System.out.println(setOfElements.get(1));
					setOfElements.remove(1);
					spl = generator.generateSPL(SplGenerator.SPLOT,
							SplGenerator.SYMMETRIC, null,setOfElements, null);
					
					spl.getXmlRepresentation();
					
					System.out.println("Atividade deletada");
					break;
				case 5:
					System.out.println("Nome do fragmento alterado");
					break;
				case 6:
					spl = generator.generateSPL(SplGenerator.SPLOT,
							SplGenerator.SYMMETRIC, null,null,"fragmento");
					
					spl.getXmlRepresentation();
					
					System.out.println("Fragmento adicionado");
					break;
				case 7:
					spl = generator.generateSPL(SplGenerator.SPLOT,
							SplGenerator.SYMMETRIC, null,null,"delfrag");
					spl.getXmlRepresentation();
					System.out.println("Fragmento deletado");
					break;
				case 8:
					System.out.println("Nome da mensagem alterado");
					break;
				case 9:
					
					spl = generator.generateSPL(SplGenerator.SPLOT,
							SplGenerator.SYMMETRIC, null,null,"Mensagem");
					
					spl.getXmlRepresentation();
					
					
					System.out.println("Mensagem adicionada");
					break;
				case 10:
					
					spl = generator.generateSPL(SplGenerator.SPLOT,
							SplGenerator.SYMMETRIC, null,null,"delmsg");
					
					spl.getXmlRepresentation();
					
					System.out.println("Mensagem deletada");
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
	
	public static void exit_error(String reason) {
		System.err.println("Ocorreu um erro no parsing dos argumentos");
		System.err.println("Motivo: " + reason);
		
		System.exit(1);
	}
	
	public static SPL change_name_ad(String new_name) {
		ActivityDiagram evolutioned = spl.getActivityDiagram();

		evolutioned.setName(new_name);
		
		SPL new_spl = generator.generateSPL(SplGenerator.SPLOT,
				SplGenerator.SYMMETRIC, evolutioned,null,null);
		
		ActivityDiagram evolutioned2 = new_spl.getActivityDiagram();
		System.out.println(evolutioned2.getName());
		
		new_spl.getXmlRepresentation();
		
		System.out.println("Alteração feita");
		
		return new_spl;
	}
	
	public static SPL change_name_act(String old_name, String new_name) {
		ActivityDiagram evolutioned = spl.getActivityDiagram();

		Activity atividade_alterada = evolutioned.getActivityByName(old_name);
		
		atividade_alterada.setElementName(new_name);
		
		System.out.println(setOfElements.get(1));
		
		// TODO Fix this
		setOfElements.set(1, atividade_alterada);
		
		SPL new_spl = generator.generateSPL(SplGenerator.SPLOT,
				SplGenerator.SYMMETRIC, null,setOfElements, null);
		
		System.out.println("Atividade alterada");
		
		return new_spl;
	}
	
	public static boolean exec_args(String[] args) {
		boolean winteractions = false;
		
		for (int i = 0;i < args.length;) {
			if (args[i].equals("--winteractions")) {
				winteractions = true;
			
			// User wants to change names...
			} else if (args[i].equals("--chname")) {
				winteractions = false;
				if (i + 2 >= args.length) exit_error("Sintaxe --chname inválida");
				
				// ... from activity diagram
				if (args[i+1].equals("ad")) {
					String new_name = args[i+2];
					change_name_ad(new_name);
					i += 3;
					
				// ... from activities
				} else if (args[i+1].equals("act")) {
					
					if (i + 3 >= args.length) exit_error("Sintaxe --chname inválida");
					
					String old_name = args[i+2];
					String new_name = args[i+3];
					
					change_name_act(old_name, new_name);
					i += 4;
				}
				
			}
			
			i += 1;
		}
		
		return winteractions;
	}

	

}
