package cz.vutbr.fit.xhriba01.bc.eclipse.algo;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;

import cz.vutbr.fit.xhriba01.bc.lib.ByteFile;
import cz.vutbr.fit.xhriba01.bc.lib.IClassContainer;
import cz.vutbr.fit.xhriba01.bc.lib.IFile;
import cz.vutbr.fit.xhriba01.bc.lib.Utils;

public class ResourceContainer implements IClassContainer {
	
	private IContainer fContainer;
	
	public ResourceContainer(IContainer container) {
		fContainer = container;
	}
	
	@Override
	public IFile getClassFile(String filename) {
		
		IResource member = fContainer.findMember(filename);
		
		if (member == null || !(member instanceof org.eclipse.core.resources.IFile)) {
			return null;
		}
		
		org.eclipse.core.resources.IFile eclipseFile = (org.eclipse.core.resources.IFile) member;
		
		byte[] content = null;
		
		try {
			
			content = Utils.inputStreamToBytes(eclipseFile.getContents());
			
		} catch (CoreException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}
		
		if (content == null) {
			return null;
		}
		
		return new ByteFile(content, eclipseFile.getName());
	}

	@Override
	public IFile[] getClassFiles() {
		
		return null;
		
	}

}
