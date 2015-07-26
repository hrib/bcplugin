package cz.vutbr.fit.xhriba01.bc.eclipse;

import org.eclipse.core.resources.IResource;
import org.eclipse.jdt.core.IClassFile;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.ui.JavaUI;
import org.eclipse.ui.IEditorPart;

public class BcUtils {
	
	public static IJavaElement getEditorJavaElement(IEditorPart part) {
		
		return JavaUI.getEditorInputJavaElement(part.getEditorInput());
		
	}
	
	public static Object getContainerOrPackageFragment(IClassFile classFile) {
		
		IJavaProject javaProject = classFile.getJavaProject();
		
		if (!javaProject.isOnClasspath(classFile)) {
			IResource classFileResource = classFile.getResource();
			IResource classFileResouceParent = classFileResource.getParent();
			
			if (classFileResouceParent == null) {
				return classFileResource;
			}
			
			return classFileResouceParent;
		}
		else {
			return classFile.getAncestor(IJavaElement.PACKAGE_FRAGMENT);
		}
		
	}
	
}
