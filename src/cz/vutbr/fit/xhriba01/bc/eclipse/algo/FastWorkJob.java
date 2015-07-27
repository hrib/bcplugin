package cz.vutbr.fit.xhriba01.bc.eclipse.algo;

import cz.vutbr.fit.xhriba01.bc.eclipse.algo.UserBytecode.UserBytecodeDocument;
import cz.vutbr.fit.xhriba01.bc.lib.NodeProcessor;
import cz.vutbr.fit.xhriba01.bc.lib.Result;

public class FastWorkJob {

	private Result fResult;
	
	private Style fStyle;
	
	private UserBytecode fUserBytecode;
	
	public FastWorkJob(Result result, Style style) {
		fResult = result;
		fStyle = style;
		// TODO Auto-generated constructor stub
	}

	public void run() {
		// TODO Auto-generated method stub
		
		fUserBytecode = new UserBytecode(fStyle);
		
		NodeProcessor.process(fResult.getClassSourceResult().getResultNode(), fUserBytecode);
		
	}
	
	public UserBytecodeDocument getUserBytecodeDocument() {
		return fUserBytecode.getDocument();
	}
	
}
