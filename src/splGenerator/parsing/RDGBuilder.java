package splGenerator.parsing;

import java.io.File;
import java.io.IOException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import magicDrawParser.parsing.exceptions.InvalidNodeClassException;
import magicDrawParser.parsing.exceptions.InvalidNodeType;
import magicDrawParser.parsing.exceptions.InvalidNumberOfOperandsException;
import magicDrawParser.parsing.exceptions.InvalidTagException;
import magicDrawParser.parsing.exceptions.UnsupportedFragmentTypeException;
import tool.RDGNode;

public class RDGBuilder {
	
	private RDGNode answer;
	
	

	public RDGBuilder(String modelPath) {
		try {
			setAnswer(model(modelPath));
		} catch (InvalidNumberOfOperandsException | InvalidNodeClassException
				| InvalidNodeType | UnsupportedFragmentTypeException
				| InvalidTagException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private void setAnswer(RDGNode root) {
		answer = root;
		
	}

	private RDGNode model(String modelPath)
			throws InvalidNumberOfOperandsException, InvalidNodeClassException,
			InvalidNodeType, UnsupportedFragmentTypeException,
			InvalidTagException {
		File umlModels = new File(modelPath);
		String exporter = identifyExporter(umlModels);
		magicDrawParser.modeling.IModelerAPI modeler = null;

		switch (exporter) {
		case "MagicDraw":
			modeler = new magicDrawParser.modeling.DiagramAPI(umlModels);

			break;

		case "SplGenerator":
			modeler = new splGenerator.parsing.SplGeneratorModelingAPI(
					umlModels);
			break;

		default:
			break;
		}
		RDGNode result = modeler.transform();

		return result;
	}
	
	/**
	 * @author andlanna This method's role is to identify which behavioral model
	 *         exporter was used for generating activity and sequence diagrams.
	 * @param umlModels
	 *            - the XML file representing the SPL's activity and sequence
	 *            diagrams.
	 * @return a string with the name of the exporter
	 */
	private static String identifyExporter(File umlModels) {
		String answer = null;
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();

		DocumentBuilder builder;
		Document doc = null;
		try {
			builder = factory.newDocumentBuilder();
			doc = builder.parse(umlModels);
		} catch (ParserConfigurationException | SAXException | IOException e) {
			e.printStackTrace();
		}

		NodeList nodes = doc.getElementsByTagName("xmi:exporter");
		if (nodes.getLength() > 0) {
			Element e = (Element) nodes.item(0);
			if (e.getTextContent().equals("MagicDraw UML")) {
				answer = "MagicDraw";
			}
		} else {
			answer = "SplGenerator";
		}

		return answer;
	}

	public RDGNode getRDGNode() {
		return answer;
	}

	
}
