package cz.vutbr.fit.xhriba01.bc.eclipse.algo;

import org.eclipse.jdt.core.IClassFile;

import cz.vutbr.fit.xhriba01.bc.lib.IFile;

public interface IJavaFromClassStrategy {
	
	IFile getJavaSource(IClassFile classFile);
	
}
