package cz.vutbr.fit.xhriba01.bc.eclipse.algo;

import org.eclipse.core.runtime.IPath;
import org.eclipse.jdt.core.IClassFile;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.JavaModelException;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.ClassNode;

import cz.vutbr.fit.xhriba01.bc.lib.BinaryName;
import cz.vutbr.fit.xhriba01.bc.lib.IFile;
import cz.vutbr.fit.xhriba01.bc.lib.StringFile;

public class OutputLocationStrategy implements IJavaFromClassStrategy {

	@Override
	public IFile getJavaSource(IClassFile classFile) {
		
		IJavaProject javaProject = classFile.getJavaProject();
		
		try {
			
			if (javaProject.isOnClasspath(classFile)) {
				return null;
			}
			
			IPath defaultOutputLoc = javaProject.getOutputLocation();
			
			IPath classFileDir = classFile.getResource().getParent().getFullPath();
			
			IClasspathEntry[] classpathEntries = javaProject.getResolvedClasspath(true);
			
			ClassReader classReader = new ClassReader(classFile.getBytes());
			
			BinaryName binaryClassName = new BinaryName(classReader.getClassName());
			
			String packagee = binaryClassName.getDotPackage();
			
			ClassNode classNode = new ClassNode(Opcodes.ASM5);
			
			classReader.accept(classNode, 0);
			
			String javaFileName = classNode.sourceFile;
			
			ICompilationUnit javaSource = null;
			
			foundLabel:
			for (IClasspathEntry classpathEntry : classpathEntries) {
				
				if (classpathEntry.getEntryKind() == IClasspathEntry.CPE_SOURCE) {
					
					IPath entryOutputLoc = classpathEntry.getOutputLocation();
					
					if (entryOutputLoc == null) {
						entryOutputLoc = defaultOutputLoc;
					}
					
					if (!entryOutputLoc.isPrefixOf(classFileDir)) {
						continue;
					}
					
					System.out.println("nalazena shoda!!");
					//System.out.println("SOURCE OUT LOC: " + entryOutputLoc.toString());
					
					IPackageFragmentRoot[] packageFragmentRoots = javaProject.findPackageFragmentRoots(classpathEntry);
					
					for (IPackageFragmentRoot packageFragmentRoot : packageFragmentRoots) {
						
						IPackageFragment javaSourcePackageFragment = packageFragmentRoot.getPackageFragment(packagee);
						
						if (javaSourcePackageFragment.exists()) {
							
							javaSource = javaSourcePackageFragment.getCompilationUnit(javaFileName);
							
							if (!javaSource.exists()) {
								javaSource = null;
								continue;
							}
							
							System.out.println("OK: " + javaSource.getPath().toString());
							
							break foundLabel;
							
						}
					}
					
				}
			}
			
			if (javaSource != null) {
				return new StringFile(javaSource.getSource(), javaFileName);
			}
			
		} catch (JavaModelException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return null;
	}

}
