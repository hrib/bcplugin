package cz.vutbr.fit.xhriba01.bc.eclipse.algo;

import org.eclipse.jdt.core.IClassFile;
import org.eclipse.jdt.core.ISourceRange;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.SourceRange;

import cz.vutbr.fit.xhriba01.bc.lib.IFile;
import cz.vutbr.fit.xhriba01.bc.lib.StringFile;

public class SourceAttachmentStrategy implements IJavaFromClassStrategy {

	@Override
	public IFile getJavaSource(IClassFile classFile) {
			
		ISourceRange sourceRange = null;
		
		try {
			sourceRange = classFile.getSourceRange();
			
			if (sourceRange == null || !SourceRange.isAvailable(sourceRange)) {
				return null;
			}
			
			return new StringFile(classFile.getSource());
			
		} catch (JavaModelException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return null;
		
	}

}
