package cz.vutbr.fit.xhriba01.bc.eclipse.algo;

import org.eclipse.core.resources.IContainer;

import cz.vutbr.fit.xhriba01.bc.lib.IClassContainer;
import cz.vutbr.fit.xhriba01.bc.lib.IFile2;

public class ClassDir implements IClassContainer {
	
	private IContainer fContainer;
	
	public ClassDir(IContainer container) {
		
		fContainer = container;
		
	}

	@Override
	public IFile2 getClassFile(String filename) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public IFile2[] getClassFiles() {
		// TODO Auto-generated method stub
		return null;
	}
	
	
}
