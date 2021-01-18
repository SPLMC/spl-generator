package ui;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.io.File;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;
import java.util.Scanner;

import splGenerator.Activity;
import splGenerator.ActivityDiagram;
import splGenerator.ActivityDiagramElement;
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
import splGenerator.transformation.Transformer;
import splar.apps.generator.FMGeneratorEngine;
import splar.core.fm.FeatureModel;
import splar.core.fm.FeatureModelException;
import splar.core.fm.XMLFeatureModel;
import splar.core.fm.personalization.PersonalFeatureModel;
import splar.core.fm.randomization.Random3CNFFeatureModel;
import splar.core.fm.randomization.RandomFeatureModel2;
import tool.RDGNode;


public class CommandLineInterface {

	private static RDGNode root;
	private static RDGBuilder builder;
	
	private static List<String> arguments = new ArrayList<String> ();

	
	private static String behavioralModel = "/home/igorbispo/Downloads/spl-generator/src/seedModels/BSN/UML_BSN.xml";
//	private static String behavioralModel = "/home/igorbispo/Downloads/spl-generator/src/generatedModels/input.xml";

	private static String featureModel = "/home/igorbispo/Downloads/spl-generator/src/seedModels/BSN/fm_BSN.xml";
	
	
	private static String actName;
	private static String fragName;
	private static String srcName;
	private static String dstName;
	private static String msgName;
	private static String rowName;
	private static String guardName;
	private static String newPresenceCondition;

	private static int type;
	private static int msgAmount;
	private static double prob;
	
	public static void main(String[] args) {		
		builder = new RDGBuilder(behavioralModel);
		
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
		
		int option = 1;
		
		boolean show_menu = exec_args(ad, args);
		
		if (!show_menu) {
			// Creating a set of SPLs objects that will be transformed
			LinkedList<SPL> splsList = new LinkedList<SPL>();
			splsList.add(spl);

			SPLFilePersistence.persistSPLs(splsList);
			
			return;
		}
		
		Scanner scan = new Scanner(System.in);
		
		//Menu de evolução
		while(option!=0) {
			System.out.println("\nBoas vindas ao SPL generator. Qual evolução deseja fazer?");

			System.out.println("1 - Adicionar mensagem.");
			System.out.println("2 - Adicionar fragmento.");
			System.out.println("3 - Mudar presence condition.");
			System.out.println("4 - Remover fragmento.");
			
			System.out.println("0 - Sair.");

			System.out.println("Escolha uma opção de alteração:");
			
			String input = scan.nextLine();
			option = Integer.parseInt(input);
			
			switch(option) {
			case 1:
				get_input_add_msg(scan);
				
				actName = arguments.get(0);
				fragName = arguments.get(1);
				srcName = arguments.get(2);
				dstName = arguments.get(3);
				type = Integer.parseInt(arguments.get(4));
				msgName = arguments.get(5);
				prob = Double.parseDouble(arguments.get(6));
				
				if (0 != add_message(ad, actName, fragName, srcName, dstName, type, msgName, prob)) {
					System.out.println("Ocorreu um erro ao adicionar nova mensagem.");
					return;
				}
				
				System.out.println("Mensagem adicionada com sucesso.");

				break;
			case 2:
				get_input_add_frag(scan);
				
				actName = arguments.get(0);
				fragName = arguments.get(1);
				rowName = arguments.get(2);
				guardName = arguments.get(3);
				msgAmount = Integer.parseInt(arguments.get(4));

				if (0 != add_fragment(ad, actName, fragName, rowName, guardName, msgAmount) ) {
					System.out.println("Ocorreu um erro ao adicionar novo fragmento.");
					return;
				}
				
				System.out.println("Fragmento adicionado com sucesso.");


				break;
			case 3:
				get_input_ch_pc(scan);
				
				actName = arguments.get(0);
				fragName = arguments.get(1);	
				newPresenceCondition = arguments.get(2);
				
				if (0 != ch_presence_condition(ad, actName, fragName, newPresenceCondition)) {
					System.out.println("Ocorreu um erro ao modificar presence condition.");
					return;
				}
				
				System.out.println("Presence condition alterada com sucesso.");
				
				break;
			case 4:
				get_input_rem_frag(scan);
				
				actName = arguments.get(0);
				fragName = arguments.get(1);
				
				if (0 != remove_fragment(ad, actName, fragName)) {
					System.out.println("Ocorreu um erro ao remover o fragmento.");
					return;
				}
				
				System.out.println("Fragmento removido com sucesso.");
				break;
				
			case 0:
				System.out.println("Saindo.");
				break;
			}

		}
		
		// Creating a set of SPLs objects that will be transformed
		LinkedList<SPL> splsList = new LinkedList<SPL>();
		splsList.add(spl);

		SPLFilePersistence.persistSPLs(splsList);
	}
	
	// TODO refactoring
	//********** Evolution routines
	

	public static Fragment getRandomFragment(Activity act) {
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
	
	public static Fragment getFragment(HashSet<Fragment> f, String fragname) {
		for (Fragment f_: f) {
			if (f_.getName().equalsIgnoreCase(fragname))
				return f_;
		}
		
		return null;
	}
	
	public static Lifeline getLifeline(HashSet<Lifeline> l, String lname) {
		for (Lifeline l_: l) {
			if (l_.getName().equalsIgnoreCase(lname))
				return l_;
		}
		
		return null;
	}	
	
	// Add message at fragment "fragName" at "ad" ActivityDiagram
	public static int add_message(ActivityDiagram ad, String actName, String fragName, String srcName,
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
	public static int add_fragment(ActivityDiagram ad, String actName, String fragName, String rowName, String guardName, int msgAmount) {
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
				
				if (add_message(ad, actName, fragName, lifelines[msgNumber],
						lifelines[(msgNumber + 1) % 2], Message.SYNCHRONOUS,
						"Msg n." + Integer.toString(i), 0.99) != 0) {
					return -1;
				}
			}			
		}
		
		return 0;
	}
	
	// Remove a fragment named "fragName" at "ad" ActivityDiagram
	public static int remove_fragment(ActivityDiagram ad, String actName, String fragName) {
		Activity act = ad.getActivityByName(actName);
		
		if (act == null) return -1;
		
		SequenceDiagram seq = act.getSequenceDiagrams().getFirst();
		
		HashSet<Fragment> frag = seq.getFragments();
		
		Fragment f = getFragment(frag, fragName);
		
		if (f == null) return -1;
		
		frag.remove(f);
		
		return 0;
	}
	
	public static int ch_presence_condition(ActivityDiagram ad, String actName, String fragName, String newPresenceCondition) {
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
	
	//********** Interface operations
	
	public static void exit_error(String reason) {
		System.err.println("Ocorreu um erro no parsing dos argumentos");
		System.err.println("Motivo: " + reason);
		
		System.exit(1);
	}
	
	
	public static boolean exec_args(ActivityDiagram ad, String[] args) {
		boolean winteractions = false;
		
		for (int i = 0;i < args.length;) {
			if (args[i].equals("--winteractions")) {
				winteractions = true;
			
			// User wants to change...
			} else if (args[i].equals("--ch")) {
				winteractions = false;
				
				if (i + 1 >= args.length) exit_error("Sintaxe --ch inválida.");
				
				// ... presence condition
				if (args[i+1].equals("pc")) {
					if (i + 4 >= args.length) exit_error("Sintaxe --ch pc inválida.");

					actName = args[i+2];
					fragName = args[i+3];
					newPresenceCondition = args[i+4];
					
					if (0 != ch_presence_condition(ad, actName, fragName, newPresenceCondition)) {
						System.out.println("Erro modificando presence condition.");
						System.exit(1);
					}
					
					i += 5;
				}
				
			// User wants to add...			
			} else if (args[i].equals("--add")) {
				winteractions = false;
				
				if (i + 1 >= args.length) exit_error("Sintaxe --add inválida.");
				
				//... a new fragment
				if (args[i+1].equals("frag")) {
					if (i + 6 >= args.length) exit_error("Sintaxe --add frag inválida.");

					actName = args[i+2];
					fragName = args[i+3];
					rowName = args[i+4];
					guardName = args[i+5];
					msgAmount = Integer.parseInt(args[i+6]);
					
					if (0 != add_fragment(ad, actName, fragName, rowName, guardName, msgAmount)){
						System.out.println("Erro adicionando fragmento.");
						System.exit(1);
					}
					
					i += 7;
				//... a new message
				} else if (args[i+1].equals("msg")) {
					if (i + 8 >= args.length) exit_error("Sintaxe --add msg inválida.");

					actName = args[i+2];
					fragName = args[i+3];
					srcName = args[i+4];
					dstName = args[i+5];
					type = Integer.parseInt(args[i+6]);
					msgName = args[i+7];
					prob = Double.parseDouble(args[i+8]);

					
					if (0 != add_message(ad, actName, fragName, srcName, dstName, type, msgName, prob)) {
						System.out.println("Erro adicionando mensagem.");
						System.exit(1);
					}
					
					i += 9;				
				}
				
			}
			
			i += 1;
		}
		
		return winteractions;
	}

	public static void get_input_add_msg(Scanner scan) {
		arguments.clear();
		
		System.out.println("Insira o nome da atividade na qual deseja inserir a mensagem.");
		
		String act_name = scan.nextLine();
		
		System.out.println("Insira o nome do fragmento no qual deseja inserir a mensagem.");
	
		String frag_name = scan.nextLine();
		
		System.out.println("Insira o nome da lifeline de origem da mensagem.");
		
		String src_name = scan.nextLine();

		System.out.println("Insira o nome da lifeline de destino da mensagem.");
		
		String dst_name = scan.nextLine();
		
		System.out.println("Insira o tipo da mensagem. (0 para assíncrona, 1 para síncrona, 2 para reply)");
		
		String type = scan.nextLine();
		
		System.out.println("Insira o nome da mensagem.");
		
		String msg_name = scan.nextLine();
		
		System.out.println("Insira a probabilidade associada à mensagem.");
		
		String prob = scan.nextLine();
		
		arguments.add(act_name);
		arguments.add(frag_name);
		arguments.add(src_name);
		arguments.add(dst_name);
		arguments.add(type);
		arguments.add(msg_name);
		arguments.add(prob);
	}

	public static void get_input_add_frag(Scanner scan) {
		arguments.clear();
		
		System.out.println("Insira o nome da atividade na qual deseja inserir o fragmento.");
		
		String act_name = scan.nextLine();
		
		System.out.println("Insira o nome do fragmento que será inserido");
	
		String frag_name = scan.nextLine();
		
		System.out.println("Insira o nome da coluna associada ao fragmento.");
		
		String row_name = scan.nextLine();

		System.out.println("Insira a condição de guarda associada ao fragmento.");
		
		String guard_name = scan.nextLine();
		
		System.out.println("Insira a quantidade de mensagens que o fragmento terá.");
		
		String msg_amount = scan.nextLine();
		
		arguments.add(act_name);
		arguments.add(frag_name);
		arguments.add(row_name);
		arguments.add(guard_name);
		arguments.add(msg_amount);
	}
	
	public static void get_input_ch_pc(Scanner scan) {
		arguments.clear();
		
		System.out.println("Insira o nome da atividade a qual contém a presence condition que deseja modificar.");
		
		String act_name = scan.nextLine();
		
		System.out.println("Insira o nome do fragmento o qual contém a presence condition que deseja modificar.");
	
		String frag_name = scan.nextLine();
		
		System.out.println("Insira a nova presence condition.");
		
		String new_presence_condition = scan.nextLine();
		
		arguments.add(act_name);
		arguments.add(frag_name);
		arguments.add(new_presence_condition);
	}
	
	public static void get_input_rem_frag(Scanner scan) {
		arguments.clear();
		
		System.out.println("Insira o nome da atividade da qual deseja remover o fragmento.");
		
		String act_name = scan.nextLine();
		
		System.out.println("Insira o nome do fragmento que será removido.");
	
		String frag_name = scan.nextLine();
		
		arguments.add(act_name);
		arguments.add(frag_name);
	}

}
