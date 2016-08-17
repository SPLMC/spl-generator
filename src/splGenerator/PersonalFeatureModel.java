package splGenerator;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.Stack;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import splar.core.fm.FeatureGroup;
import splar.core.fm.FeatureModelException;
import splar.core.fm.FeatureTreeNode;
import splar.core.fm.RootNode;
import splar.core.fm.SolitaireFeature;
import splar.core.fm.TreeNodeRendererFactory;

public class PersonalFeatureModel extends splar.core.fm.FeatureModel {
	ArrayList<Node> nodes = new ArrayList<Node>();
	private int numberOfFeaturesToCreate;
	private ArrayList<FeatureTreeNode> fmNodes;
	private FeatureTreeNode root;

	public PersonalFeatureModel(Node structuralTag) {
		nodes = getTransitiveElementChildren(structuralTag);
		numberOfFeaturesToCreate = countFeaturesInFeatureModel(structuralTag);
		
		fmNodes = new ArrayList<FeatureTreeNode>();
		for (Node n : nodes) {
			FeatureTreeNode feat = getFeatureObject(n);
			fmNodes.add(feat);
		}

		for (int i = 0; i < nodes.size(); i++) {
			StringBuilder str = new StringBuilder();
			str.append("xmlTagName: ");
			str.append(nodes.get(i).getAttributes().getNamedItem("name")
					.getNodeValue());
			str.append("\n");
			str.append("featurName: ");
			str.append(fmNodes.get(i).getName());
			str.append(" --> ");
			str.append(fmNodes.get(i).getClass().getName());
			str.append("\n");
			System.out.println(str);
		}
	}

	private int countFeaturesInFeatureModel(Node structuralTag) {
		int answer = 0;

		nodes = getTransitiveElementChildren(structuralTag);
		answer = nodes.size();

		return answer;
	}

	private ArrayList<Node> getTransitiveElementChildren(Node structuralTag) {
		ArrayList<Node> answer = new ArrayList<Node>();

		answer.add(structuralTag);
		NodeList children = structuralTag.getChildNodes();

		for (int i = 0; i < children.getLength(); i++) {
			Node child = children.item(i);
			if (child.getNodeType() == Element.ELEMENT_NODE
					&& !child.getNodeName().equalsIgnoreCase("description")) {
				answer.addAll(getTransitiveElementChildren(child));
			}
		}
		return answer;
	}

	@Override
	protected FeatureTreeNode createNodes() throws FeatureModelException {
		fmNodes = new ArrayList<FeatureTreeNode>();
		int countCreatedFeatures = 0;

//		root = getFeatureObject(nodes.get(0));
		root = fmNodes.get(0);
		numberOfFeaturesToCreate--;
		root.attachData(new Integer(numberOfFeaturesToCreate));
		countCreatedFeatures++;
		
		FeatureTreeNode parentFeature = root;
		Node parentNode = nodes.get(0);
		nodes.remove(0);

		LinkedList<Node> nodesToEvaluate = new LinkedList<Node>();
		nodesToEvaluate.add(parentNode);
		
		while (numberOfFeaturesToCreate > 0) {
			Node n = nodesToEvaluate.removeFirst();
			ArrayList<Node> children = getElementChildren(n);
			for (Node c : children) {
				FeatureTreeNode feat = getFeature(getFeatureName(c));
				feat.setParent(parentFeature);
				
				countCreatedFeatures++;
				numberOfFeaturesToCreate--;
				feat.attachData(new Integer(numberOfFeaturesToCreate));
			}
			
		}

		return root;
	}

	private FeatureTreeNode getFeature(String featureName) {
		FeatureTreeNode answer = null; 
		for (FeatureTreeNode f : fmNodes) {
			if (f.getName().equalsIgnoreCase(featureName)) {
				answer = f; 
			}
		}
		return answer;
	}

	private String getFeatureName(Node n) {
		return n.getAttributes().getNamedItem("name").getNodeValue();
	}

	private FeatureTreeNode getFeatureObject(Node node) {
		FeatureTreeNode answer = null;
		String isAbstract = null;
		String isMandatory = null;
		String featureName = null;

		// 1st step: get feature type
		if (getFeatureName(node)
				.equalsIgnoreCase("root")) { // ROOT
			answer = new RootNode("root", "root",
					TreeNodeRendererFactory.createRootRenderer());
			featureName = getFeatureName(node);
			answer.setName(featureName);
		} else if (node.getNodeName().equalsIgnoreCase("feature")) { // LEAF
			featureName = getFeatureName(node);
			answer = new SolitaireFeature(false, featureName, featureName,
					TreeNodeRendererFactory.createOptionalRenderer());
		} else if (node.getNodeName().equalsIgnoreCase("and")
				|| node.getNodeName().equalsIgnoreCase("or")) { // GROUPED
																// FEATURE (AND
																// / OR)
																// FeatureTreeNode
																// node;
			featureName = getFeatureName(node);

			int lower = 1;
			int upper = -1;

			answer = new FeatureGroup(featureName, featureName, lower, upper,
					TreeNodeRendererFactory.createFeatureGroupRenderer());
		} else if (node.getNodeName().equalsIgnoreCase("alt")) {// ALTERNATIVE
																// FEATURE
			featureName = getFeatureName(node);

			int lower = 1;
			int upper = 1;

			answer = new FeatureGroup(featureName, featureName, lower, upper,
					TreeNodeRendererFactory.createGroupedRenderer());
		}

		// 2nd step: get feature's characteristics
		if (node.getAttributes().getNamedItem("abstract") != null) {
			isAbstract = node.getAttributes().getNamedItem("abstract")
					.getNodeValue();
			answer.setProperty("abstract", isAbstract);
		}
		if (node.getAttributes().getNamedItem("mandatory") != null) {
			isMandatory = node.getAttributes().getNamedItem("mandatory")
					.getNodeValue();
			answer.setProperty("mandatory", isMandatory);
		}

		return answer;
	}

	private ArrayList<Node> getElementChildren(Node structuralTag) {
		ArrayList<Node> answer = new ArrayList<Node>();

		NodeList children = structuralTag.getChildNodes();

		for (int i = 0; i < children.getLength(); i++) {
			Node child = children.item(i);
			if (child.getNodeType() == Element.ELEMENT_NODE
					&& !child.getNodeName().equalsIgnoreCase("description")) {
				answer.add(child);
			}
		}
		return answer;
	}

	@Override
	protected void saveNodes() {
	}

	public FeatureTreeNode buildFM() {
		try {
			createNodes();
		} catch (FeatureModelException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return fmNodes.get(0);
	}

}
