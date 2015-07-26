package cz.vutbr.fit.xhriba01.bc.eclipse.algo;

import org.eclipse.jdt.core.IPackageFragment;

import cz.vutbr.fit.xhriba01.bc.lib.IClassContainer;
import cz.vutbr.fit.xhriba01.bc.lib.IFile2;

public class ClassPackage implements IClassContainer {
	
	private IPackageFragment fPackageFragment;
	
	public ClassPackage(IPackageFragment packageFragment) {
		
		fPackageFragment = packageFragment;
		
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
