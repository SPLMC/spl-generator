package splGenerator;

import java.io.IOException;
import java.io.StringWriter;
import java.io.File;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import javax.sound.midi.Sequence;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import splGenerator.Util.SPLFilePersistence;
import splGenerator.parsing.ActivityDiagramParser;
import splGenerator.parsing.SequenceDiagramParser;
import splar.core.fm.FeatureGroup;
import splar.core.fm.FeatureModel;
import splar.core.fm.FeatureModelException;
import splar.core.fm.FeatureTreeNode;
import splar.core.fm.GroupedFeature;
import splar.core.fm.RootNode;
import splar.core.fm.SolitaireFeature;
import splar.core.fm.TreeNodeRendererFactory;
import splar.core.fm.randomization.Random3CNFFeatureModel;
import splar.core.fm.randomization.RandomFeatureModel2;
import tool.CyclicRdgException;
import tool.RDGNode;

public class SPL implements Cloneable {

	/**
	 * This attribute is redundant with SPLGenerator.modelsPath attribute. We
	 * should prune it soon.
	 */
	// private String modelsPath =
	// "/home/andlanna/workspace2/reana/src/splGenerator/generatedModels/";
	private String modelsPath = "/home/igorbispo/Documents/spl-generator/src/splGenerator/";

	String name;
	FeatureModel fm;
	ActivityDiagram ad;
	ConfigurationKnowledge ck;

	private SplGenerator splGenerator;

	private static SPL instance;

	private HashMap<String, Integer> additionalCharacteristics = new HashMap<String, Integer>();

	public SPL(String name) {
		this();
		this.name = name;
	}

	public SPL() {
		this.ad = new ActivityDiagram();
	}

	/**
	 * This method is a factory method for instantiating a new SPL object
	 * containing its activity diagram.
	 * 
	 * @param name
	 *            - the parameter representing the name of the SPL.
	 * @return The SPL object created for the SPL.
	 */
	public static SPL createSPL(String name) {
		instance = new SPL(name);
		return instance;
	}

	public String getXmlRepresentation() {
		StringWriter answer = new StringWriter();
		System.out.println(new String(modelsPath + name
				+ "_behavioral_model.xml").replaceAll("\\s+", "_"));
		
		File output = new java.io.File(new String(modelsPath + name
				+ "_behavioral_model.xml").replaceAll("\\s+", "_"));

		try {
			DocumentBuilderFactory docFactory = DocumentBuilderFactory
					.newInstance();
			DocumentBuilder docBuilder;

			docBuilder = docFactory.newDocumentBuilder();

			// CREATING THE XML STRUCTURE
			// root elements
			Document doc = docBuilder.newDocument();
			Element rootElement = doc.createElement("SplBehavioralModel");
			Attr splBehavioralModelName = doc.createAttribute("name");
			splBehavioralModelName.setValue(name);
			rootElement.setAttributeNode(splBehavioralModelName);
			doc.appendChild(rootElement);

			// Creating the DOM object representing the activity diagram
			Element domActDiagram = ad.getDOM(doc);
			rootElement.appendChild(domActDiagram);

			// Creating the DOM object representing the sequence diagrams
			List<Activity> setOfActivities = ad.getSetOfActivities();
			HashSet<SequenceDiagram> setOfSequenceDiagrams = new HashSet<SequenceDiagram>();
			HashSet<Lifeline> setOfLifelines = new HashSet<Lifeline>();
			HashSet<Fragment> setOfFragments = new HashSet<Fragment>();

			// 1st step --> get all the SequenceDiagrams, Lifelines and
			// Fragments used by the SPL.
			Iterator<Activity> ita = setOfActivities.iterator();
			while (ita.hasNext()) {
				Activity a = ita.next();
				// get all the sequence diagrams associated to the activity and
				// add them to the set of sequence diagrams.
				// System.out.println("----->");
				setOfSequenceDiagrams.addAll(a.getTransitiveSequenceDiagram());
				// System.out.println("=====>");
				setOfLifelines.addAll(a.getTransitiveLifelines());
				setOfFragments.addAll(a.getTransitiveFragments());
			}

			Iterator<SequenceDiagram> its = setOfSequenceDiagrams.iterator();

			Element domSeqDiagram = doc.createElement("SequenceDiagrams");
			its = setOfSequenceDiagrams.iterator();
			while (its.hasNext()) {
				SequenceDiagram d = its.next();
				Element e = d.getDOM(doc);
				domSeqDiagram.appendChild(e);
			}

			Element domLifelines = doc.createElement("Lifelines");
			Iterator<Lifeline> itl = setOfLifelines.iterator();
			while (itl.hasNext()) {
				Lifeline l = itl.next();
				Element domLife = doc.createElement("Lifeline");
				domLife.setAttribute("name", l.getName());
				domLife.setAttribute("reliability",
						Double.toString(l.getReliability()));
				domLifelines.appendChild(domLife);
			}

			Element domFragments = doc.createElement("Fragments");
			Iterator<Fragment> itf = setOfFragments.iterator();
			while (itf.hasNext()) {
				Fragment f = itf.next();
				Element domF = f.getDOM(doc);
				domFragments.appendChild(domF);
			}

			domSeqDiagram.appendChild(domLifelines);
			domSeqDiagram.appendChild(domFragments);
			rootElement.appendChild(domSeqDiagram);

			// Transform the content into an xml representation
			TransformerFactory transFactory = TransformerFactory.newInstance();
			Transformer transformer = transFactory.newTransformer();
			DOMSource source = new DOMSource(doc);
			StreamResult result = new StreamResult(answer);
			StreamResult result_file = new StreamResult(output);
			transformer.transform(source, result);
			transformer.transform(source, result_file);

		} catch (ParserConfigurationException e) {
			e.printStackTrace();
		} catch (TransformerException e) {
			e.printStackTrace();
		}
		return answer.toString();
	}

	public ActivityDiagram createActivityDiagram(String name) {
		ad = new ActivityDiagram();
		ad.setName(name);
		return ad;
	}

	public ActivityDiagram getActivityDiagram() {
		return ad;
	}

	/**
	 * This method's role is to read an XML file representing the behavioral
	 * models of a software product line, parse its document and create the
	 * models in memory.
	 * 
	 * @param fileName
	 *            the path of the file to be parsed
	 * @return the SPL object containing the behavioral models.
	 */
	public static SPL getSplFromXml(String fileName) {

		try {
			File xmlFile = new File(fileName);
			DocumentBuilderFactory dbFactory = DocumentBuilderFactory
					.newInstance();
			DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
			Document doc = dBuilder.parse(xmlFile);

			// get the root element and extract the SPL name from it
			Element root = doc.getDocumentElement();
			Node nSplName = root.getAttributeNode("name");
			String splName = nSplName.getNodeValue();
			instance = new SPL(splName);

			// Call the parser of sequence diagrams elements initially, so it
			// allows to create in memory all the objects representing the SPL's
			// sequence diagrams.
			// Later, such objects will be linked to Activity Diagrams objects.
			SequenceDiagramParser.parse(doc);

			// build the activity diagram from the <ActivityDiagram> tag.
			NodeList nActivityDiagram = root
					.getElementsByTagName("ActivityDiagram");
			ActivityDiagram a = ActivityDiagramParser.parse(doc);
			instance.ad = a;

		} catch (Exception e) {
			e.printStackTrace();
		}

		return instance;
	}

	public String getName() {
		return name;
	}

	/**
	 * This method is used for defining the activity diagram describing the
	 * coarse-grained behavior of the software product line to the SPL object.
	 * 
	 * @param ad
	 *            - the activity diagram that will be assigned to the SPL
	 *            object.
	 */
	public void setActivityDiagram(ActivityDiagram ad) {
		this.ad = ad;
	}

	/**
	 * This method returns the FeatureModel object associated to the SPL.
	 * 
	 * @return the FeatureModel associated to the SPL.
	 */
	public FeatureModel getFeatureModel() {
		return fm;
	}

	/**
	 * This method is used for assigning a FeatureModel object to the SPL.
	 * 
	 * @param fm
	 */
	public void setFeatureModel(FeatureModel fm) {
		this.fm = fm;
	}

	public Object clone() throws CloneNotSupportedException {
		return super.clone();
	}

	public void setName(String name) {
		this.name = name;
	}

	public ConfigurationKnowledge getCk() {
		return ck;
	}

	public void setSplGenerator(SplGenerator splGenerator) {
		this.splGenerator = splGenerator;
	}

	public SplGenerator getSplGenerator() {
		return splGenerator;
	}

	/**
	 * This method's role is to build an entire SPL from a given RDG structure.
	 * In other words, it is responsible for applying the transformation
	 * templates in the reverse order such the behavioral models of the software
	 * product line are re-created from their FDTMC representation.
	 * 
	 * @param root
	 *            the starting node of the software product line
	 * @return a SPL object containing the feature and all behavioral models of
	 *         the software product line
	 */
	public static SPL createSplFromRDG(RDGNode root) {
		SPL answer = new SPL();
		answer.setName("New SPL created from an RDG");

		/*
		 * 1st step: for all RDG nodes, we must extract its sequence diagram
		 * from its fdtmc. The following loop has the role of re-build the
		 * sequence diagram for each RDG node.
		 */
		try {
			for (RDGNode n : root.getDependenciesTransitiveClosure()) {
				splGenerator.transformation.Transformer transSD = new splGenerator.transformation.Transformer();
				transSD.getSequenceDiagramFromFDTMC(n);
			}
		} catch (CyclicRdgException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		ActivityDiagram ad = null;

		/*
		 * 2nd step: similar to first step, this step's role is to re-build the
		 * activity diagram from the FDTMC associated to the root node.
		 */
		splGenerator.transformation.Transformer transAD = new splGenerator.transformation.Transformer();
		ad = transAD.getActivityDiagramFromFDTMC(root);
		answer.setActivityDiagram(ad);

		/*
		 * 3rd step: after creating all the behavioral elements of activity and
		 * sequence diagrams, it's time to link such elements according to the
		 * RDG topology.
		 */
		splGenerator.transformation.Transformer t = new splGenerator.transformation.Transformer();
		t.linkBehavioralElements(answer);

		return answer;
	}

	public static FeatureModel readFeatureIDEfile(String initialFeatureModelPath) {

		FeatureModel answer = null;
		try {
			File xmlFeatureIDE = new File(initialFeatureModelPath);
			DocumentBuilderFactory dbFactory = DocumentBuilderFactory
					.newInstance();
			DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
			Document doc = dBuilder.parse(xmlFeatureIDE);
			// doc.getDocumentElement().normalize();

			// Document read, now let's parse it!
			Node structuralTag = doc.getElementsByTagName("struct").item(0)
					.getChildNodes().item(1);
			answer = new PersonalFeatureModel(structuralTag);
			
			FeatureTreeNode root = ((PersonalFeatureModel)answer).buildFM();
			
			answer.dumpXML();
		} catch (ParserConfigurationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SAXException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return answer;
	}
	
//	public static FeatureModel readFeatureIDEfile(String initialFeatureModelPath) {
//
//		FeatureModel answer = null;
//		try {
//			File xmlFeatureIDE = new File(initialFeatureModelPath);
//			DocumentBuilderFactory dbFactory = DocumentBuilderFactory
//					.newInstance();
//			DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
//			Document doc = dBuilder.parse(xmlFeatureIDE);
//			// doc.getDocumentElement().normalize();
//
//			// Document read, now let's parse it!
//			Node structuralTag = doc.getElementsByTagName("struct").item(0)
//					.getChildNodes().item(1);
//			FeatureTreeNode root = getFeatureModelStructure(structuralTag);
//			answer = new RandomFeatureModel2("name", 2, 0, 100, 0, 0, 1, 2, 1, 1);
//			answer.setRoot(root);
//			answer.saveModel();
//			answer.dumpXML();
//			System.out.println(answer.dumpFeatureIdeXML());
//		} catch (ParserConfigurationException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		} catch (SAXException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		} catch (IOException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//		return answer;
//	}

	private static FeatureTreeNode getFeatureModelStructure(Node structuralTag) {
		FeatureTreeNode answer = null;
		String isAbstract = null;
		String isMandatory = null;
		String featureName = null;

		// 1st step: discover the kind of node
		if (structuralTag.getAttributes().getNamedItem("name").getNodeValue()
				.equalsIgnoreCase("root")) { // ROOT
			answer = new RootNode("root", "root",
					TreeNodeRendererFactory.createRootRenderer());
			featureName = structuralTag.getAttributes().getNamedItem("name")
					.getNodeValue();
			answer.setName(featureName);
		}
		if (structuralTag.getNodeName().equalsIgnoreCase("feature")) { // LEAF
		// if (isMandatory.equalsIgnoreCase("true"))
			featureName = structuralTag.getAttributes().getNamedItem("name")
					.getNodeValue();
			answer = new SolitaireFeature(false, featureName, featureName,
					TreeNodeRendererFactory.createOptionalRenderer());
		} else if (structuralTag.getNodeName().equalsIgnoreCase("and")
				|| structuralTag.getNodeName().equalsIgnoreCase("or")) { // GROUPED
																			// FEATURE
																			// (AND
																			// /
																			// OR)
			FeatureTreeNode node;
			featureName = structuralTag.getAttributes().getNamedItem("name")
					.getNodeValue();
			List<Node> children = pruneNonElementNodes(structuralTag
					.getChildNodes());
			int numChildren = children.size();

			int lower = 1;
			int upper = -1;

			node = new FeatureGroup(featureName, featureName, lower, upper,
					TreeNodeRendererFactory.createFeatureGroupRenderer());
			for (int i = 0; i < numChildren; i++) {
				FeatureTreeNode child = getFeatureModelStructure(children
						.get(i));
				node.add(child);
			}
			answer = node;
		} else if (structuralTag.getNodeName().equalsIgnoreCase("alt")) { // ALTERNATIVE
																			// FEATURE
			FeatureTreeNode node;
			featureName = structuralTag.getAttributes().getNamedItem("name")
					.getNodeValue();
//			NodeList children = structuralTag.getChildNodes();
			List<Node> children = pruneNonElementNodes(structuralTag.getChildNodes());
			
			int numChildren = children.size();

			int lower = 1;
			int upper = 1;

			node = new FeatureGroup(featureName, featureName, lower, upper,
					TreeNodeRendererFactory.createGroupedRenderer());
			for (int i = 0; i < children.size(); i++) {
				FeatureTreeNode child = getFeatureModelStructure(children.get(i));
				node.add(child);
			}
			answer = node;
		}

		if (structuralTag.getAttributes().getNamedItem("abstract") != null) {
			isAbstract = structuralTag.getAttributes().getNamedItem("abstract")
					.getNodeValue();
			answer.setProperty("abstract", isAbstract);
		}
		if (structuralTag.getAttributes().getNamedItem("mandatory") != null) {
			System.out.println("Is mandatory" + featureName); // <<-----
			isMandatory = structuralTag.getAttributes()
					.getNamedItem("mandatory").getNodeValue();
			System.out.println(isMandatory);
			answer.setProperty("mandatory", isMandatory);
		}

		return answer;
	}

	private static List<Node> pruneNonElementNodes(NodeList children) {
		List<Node> answer = new LinkedList<Node>();
		for (int i = 0; i < children.getLength(); i++) {
			Node n = children.item(i);
			if (n.getNodeType() == Node.ELEMENT_NODE
					&& !n.getNodeName().equalsIgnoreCase("description")) {
				answer.add(n);
			}
		}
		return answer;
	}

	public void setModelsPath(String modelsPath) {
		this.modelsPath = modelsPath;
	}

	public void addCharacteristic(String characteristic, int value) {
		additionalCharacteristics .put(characteristic, value);
	}

	public HashMap<String, Integer> getAdditionalCharacteristics() {
		return additionalCharacteristics;
	}

}
