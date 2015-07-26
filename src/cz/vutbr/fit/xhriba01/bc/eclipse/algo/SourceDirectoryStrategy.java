package cz.vutbr.fit.xhriba01.bc.eclipse.algo;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IPath;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IPackageDeclaration;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.JavaModelException;

import cz.vutbr.fit.xhriba01.bc.lib.BinaryName;
import cz.vutbr.fit.xhriba01.bc.lib.IClassContainer;

public class SourceDirectoryStrategy implements IClassFromJavaStrategy {

	@Override
	public IClassContainer getClassFromJava(ICompilationUnit compilationUnit) {
		
		IJavaProject javaProject = compilationUnit.getJavaProject();
		
		if (!javaProject.isOnClasspath(compilationUnit)) {
			return null;
		}
		
		IPackageFragmentRoot compilationUnitPackageFragmentRoot = (IPackageFragmentRoot) compilationUnit.getAncestor(IJavaElement.PACKAGE_FRAGMENT_ROOT);
		
		try {
			
			IClasspathEntry compilationUnitClasspathEntry = compilationUnitPackageFragmentRoot.getRawClasspathEntry();
			
			if (compilationUnitClasspathEntry.getEntryKind() != IClasspathEntry.CPE_SOURCE) {
				return null;
			}
			
			IPath sourceOutputLocation = compilationUnitClasspathEntry.getOutputLocation();
			
			if (sourceOutputLocation == null) {
				sourceOutputLocation = javaProject.getOutputLocation();
			}
			
			IPackageDeclaration[] packagees = compilationUnit.getPackageDeclarations();
			
			String packagee = null;
			
			if (packagees.length == 0) {
				packagee = BinaryName.DEFAULT_PACKAGE;
			}
			else {
				packagee = packagees[0].getElementName();
			}
			
			//String[] packageeParts = packagee.split("/");
			
			String binaryPackage = packagee.replace('.', '/');
			
			IPath sourceOutputPackagePath = sourceOutputLocation.append(binaryPackage);
			
			IWorkspaceRoot workspaceRoot = ResourcesPlugin.getWorkspace().getRoot();
			
			IResource sourceOutputPackageResource = workspaceRoot.findMember(sourceOutputPackagePath);
			
			//System.out.println(sourceOutputPackageResource);
			
			if (!sourceOutputPackageResource.exists() || !(sourceOutputPackageResource instanceof IContainer)) {
				return null;
			}
			
			return new ResourceContainer((IContainer) sourceOutputPackageResource);
			
			
		} catch (JavaModelException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return null;
	}

}
