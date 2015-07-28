package cz.vutbr.fit.xhriba01.bc.eclipse.algo;

import org.eclipse.jdt.core.IPackageFragment;

import cz.vutbr.fit.xhriba01.bc.lib.IClassContainer;
import cz.vutbr.fit.xhriba01.bc.lib.IFile;

public class ClassPackage implements IClassContainer {
	
	private IPackageFragment fPackageFragment;
	
	public ClassPackage(IPackageFragment packageFragment) {
		
		fPackageFragment = packageFragment;
		
	}

	@Override
	public IFile getClassFile(String filename) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public IFile[] getClassFiles() {
		// TODO Auto-generated method stub
		return null;
	}
	
}
