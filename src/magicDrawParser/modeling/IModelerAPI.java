package magicDrawParser.modeling;

import java.util.List;
import java.util.Map;

import magicDrawParser.parsing.activitydiagrams.ADReader;
import magicDrawParser.parsing.exceptions.InvalidNodeClassException;
import magicDrawParser.parsing.exceptions.InvalidNodeType;
import magicDrawParser.parsing.exceptions.InvalidNumberOfOperandsException;
import magicDrawParser.parsing.sequencediagrams.SDReader;
import tool.RDGNode;
import fdtmc.FDTMC;

public interface IModelerAPI {

	/**
	 * Triggers the applicable transformations, either AD or SD based
	 * @throws InvalidNumberOfOperandsException
	 * @throws InvalidNodeClassException
	 */
	public abstract RDGNode transform()
			throws InvalidNumberOfOperandsException, InvalidNodeClassException,
			InvalidNodeType;

	public abstract void measureSizeModel(FDTMC fdtmc);

	public abstract void printNumberOfCalls(String name);

	public abstract Map<String, FDTMC> getFdtmcByName();

	public abstract List<SDReader> getSdParsers();

	public abstract ADReader getAdParser();

}