package cz.vutbr.fit.xhriba01.bc.eclipse.algo;

import java.io.InputStream;

import org.eclipse.core.runtime.CoreException;

import cz.vutbr.fit.xhriba01.bc.lib.IFile;

public class EclipseFileFile implements IFile  {

	private org.eclipse.core.resources.IFile fEclipseFile; 
	
	public EclipseFileFile(org.eclipse.core.resources.IFile eclipseFile) {
		
		fEclipseFile = eclipseFile;
	}
	
	
	@Override
	public InputStream getContent() {
		
		try {
			
			return fEclipseFile.getContents();
			
		} catch (CoreException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return null;
		
	}

	@Override
	public String getFilename() {
		return fEclipseFile.getName();
	}

}
