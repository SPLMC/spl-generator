package splGenerator.parsing;

import java.io.File;
import java.util.List;
import java.util.Map;

import magicDrawParser.parsing.activitydiagrams.ADReader;
import magicDrawParser.parsing.exceptions.InvalidNodeClassException;
import magicDrawParser.parsing.exceptions.InvalidNodeType;
import magicDrawParser.parsing.exceptions.InvalidNumberOfOperandsException;
import magicDrawParser.parsing.sequencediagrams.SDReader;
import splGenerator.SPL;
import splGenerator.transformation.Transformer;
import tool.RDGNode;
import fdtmc.FDTMC;
import magicDrawParser.modeling.IModelerAPI;

public class SplGeneratorModelingAPI implements magicDrawParser.modeling.IModelerAPI {

	SPL spl; 
	
	public SplGeneratorModelingAPI() {
		// No-op
	}
	
	public SplGeneratorModelingAPI(File umlModels) {
		spl = SPL.getSplFromXml(umlModels.getAbsolutePath());
	}
	
	@Override
	public RDGNode transform() {
		Transformer t = new Transformer(); 
		RDGNode root = t.transformAD(spl.getActivityDiagram());
		return root;
	}

	@Override
	public void measureSizeModel(FDTMC fdtmc) {
		// TODO Auto-generated method stub

	}

	@Override
	public void printNumberOfCalls(String name) {
		// TODO Auto-generated method stub

	}

	@Override
	public Map<String, FDTMC> getFdtmcByName() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<SDReader> getSdParsers() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public ADReader getAdParser() {
		// TODO Auto-generated method stub
		return null;
	}

}
