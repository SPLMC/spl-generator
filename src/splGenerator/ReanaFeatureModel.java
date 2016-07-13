package splGenerator;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashSet;
import java.util.Iterator;

public class ReanaFeatureModel {

	
	
	private String fmName;
	private HashSet<String> crossTreeConstraints;
	private Feature root; 

	
	private ReanaFeatureModel() {
		root = new Feature("Root");
		root.setMandatory(true);
		crossTreeConstraints = new HashSet<String>(); 
	}
	
	private ReanaFeatureModel(String fmName) {
		this();
		this.fmName = fmName;
	}

	public static ReanaFeatureModel createFeatureModel(String fmName) {
		return new ReanaFeatureModel(fmName);
	}

	public String getName() {
		return fmName;
	}

	/**
	 * This method returns the Root feature of a Feature Model object
	 * @return root Feature.
	 */
	public Feature getRoot() {
		return root;
	}

	public HashSet<String> getCrossTreeConstraints() {
		return crossTreeConstraints;
	}

	public boolean addCrossTreeConstraint(String ctc) {
		return crossTreeConstraints.add(ctc);
	}

	public String exportXML() {
		StringBuilder str = new StringBuilder(); 
		String header = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?>";
		String fmInit = "<featureModel chosenLayoutAlgorithm=\"1\">";
		String fmEnd  = "</featureModel>";
		String strInit = "<struct>";
		String strEnd = "</struct>";
		String cstInit = "<constraints>"; 
		String cstEnd = "</constraints>";
		String cstEmpty = "<constraints/>";
		String calculations = "<calculations Auto=\"true\" Constraints=\"true\" "
				+ "Features=\"true\" Redundant=\"true\" Tautology=\"true\"/>";
		String comments = "<comments/>";
		String featureOrder = "<featureOrder userDefined=\"false\"/>";
		
		str.append(header);
		str.append(fmInit); 
		//Beginning of <struct> generation
		//Code for <struct> generation
		str.append(strInit); 
		str.append(getRoot().exportXml());
		str.append(strEnd);
		//End of <struct generation>
		
		
		//Beginning of <constraints> generation
		//Code for <constraints> generation
		if(crossTreeConstraints.size() == 0) 
			str.append(cstEmpty);
		else {
			Iterator <String> it = crossTreeConstraints.iterator(); 
			while (it.hasNext())
				str.append(it.next());
		}
		//End of <constraints> generation
		
		//Beginning of <calculations> generation
		//Code for <calculations> generation
		str.append(calculations);
		//End of <calculations> generation
				
		//Beginning of <comments> generation
		//Code for <comments> generation
		str.append(comments); 
		//End of <comments> generation
				
		//Beginning of <featureOrder> generation
		//Code for <featureOrder> generation
		str.append(featureOrder);
		//End of <featureOrder> generation
		
		str.append(fmEnd);
		
		String xml = str.toString();
		
		persist(xml); 
		
		return str.toString();
	}

	private void persist(String xml) {
		//Persist the Feature Model in an XML, in an affordable way. 
		//The file name is fm_<fmName>.xml
		String fileName = "fm_" + getName() + ".xml";
		
		try {
			BufferedWriter buffer = new BufferedWriter(new FileWriter(fileName));
			buffer.write(xml);
			buffer.close();
			
		} catch (Exception e) {
			e.printStackTrace();
		}
		
	}

	public void persistXMLFile() {
		try {
			FileWriter file = new FileWriter(fmName+".xml");
			BufferedWriter buffer = new BufferedWriter(file);
			buffer.write(exportXML());
			buffer.close();
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}

}
