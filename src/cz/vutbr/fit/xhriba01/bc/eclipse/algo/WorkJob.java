package cz.vutbr.fit.xhriba01.bc.eclipse.algo;

import java.util.Map;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;

import cz.vutbr.fit.xhriba01.bc.eclipse.views.BytecodeView;
import cz.vutbr.fit.xhriba01.bc.lib.BytecodeAlgorithm;
import cz.vutbr.fit.xhriba01.bc.lib.ClassSourceResult;
import cz.vutbr.fit.xhriba01.bc.lib.IClassContainer;
import cz.vutbr.fit.xhriba01.bc.lib.IFile2;
import cz.vutbr.fit.xhriba01.bc.lib.Node;
import cz.vutbr.fit.xhriba01.bc.lib.NodeProcessor;
import cz.vutbr.fit.xhriba01.bc.lib.Result;

public class WorkJob extends Job {
	
	private IFile2 fJavaSource;
	
	private Map<String, Object> fOptions;
	
	private IClassContainer fClassContainer;
	
	private BytecodeView fBytecodeView;
	
	private Result fResult;
	
	private UserBytecode fBytecode;
	
	private Style fStyle;
	
	public BytecodeView getBytecodeView() {
		return fBytecodeView;
	}
	
	public WorkJob(String name, Style style, BytecodeView bytecodeView, IFile2 javaSource, IClassContainer classContainer, Map<String, Object> options) {
		super(name);
		fJavaSource = javaSource;
		fOptions = options;
		fClassContainer = classContainer;
		fBytecodeView = bytecodeView;
		fStyle = style;
	}

	@Override
	protected IStatus run(IProgressMonitor monitor) {
		
		fResult = BytecodeAlgorithm.run(fJavaSource, fClassContainer, fOptions);
		
		if (fResult == null) {
			return Status.OK_STATUS;
		}
		
		ClassSourceResult classResult = fResult.getClassSourceResult();
		
		if (classResult == null) {
			return Status.OK_STATUS;
		}
		
		Node node = classResult.getResultNode();
		
		fBytecode = new UserBytecode(fStyle);
		
		NodeProcessor.process(node, fBytecode);
		
		return Status.OK_STATUS;
	}
	
	public Style getStyle() {
		return fStyle;
	}
	
	public UserBytecode getUserBytecode() {
		return fBytecode;
	}
	
	public Result getBytecodeAlgorithmResult() {
		return fResult;
	}

}
