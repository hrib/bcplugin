package cz.vutbr.fit.xhriba01.bc.eclipse.algo;

import org.eclipse.jdt.core.IClassFile;

import cz.vutbr.fit.xhriba01.bc.lib.IFile2;

public interface IJavaFromClassStrategy {
	
	IFile2 getJavaSource(IClassFile classFile);
	
}
