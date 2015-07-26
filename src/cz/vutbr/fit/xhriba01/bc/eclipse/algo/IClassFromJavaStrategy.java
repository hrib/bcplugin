package cz.vutbr.fit.xhriba01.bc.eclipse.algo;

import org.eclipse.jdt.core.ICompilationUnit;

import cz.vutbr.fit.xhriba01.bc.lib.IClassContainer;

public interface IClassFromJavaStrategy {
	
	IClassContainer getClassFromJava(ICompilationUnit compilationUnit);
	
}
