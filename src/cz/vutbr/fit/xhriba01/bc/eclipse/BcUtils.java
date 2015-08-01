package cz.vutbr.fit.xhriba01.bc.eclipse;

import org.eclipse.core.resources.IResource;
import org.eclipse.jdt.core.IClassFile;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.ui.JavaUI;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.TextViewer;
import org.eclipse.jface.text.source.CompositeRuler;
import org.eclipse.jface.text.source.IVerticalRuler;
import org.eclipse.jface.text.source.IVerticalRulerInfo;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.texteditor.AbstractTextEditor;
import org.eclipse.ui.texteditor.IDocumentProvider;

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
	
	public static IDocument getEditorDocument(AbstractTextEditor editor) {
		
		IDocumentProvider dp = editor.getDocumentProvider();
		
		if (dp == null) return null;
		
		IEditorInput input = editor.getEditorInput();
		
		if (input == null) return null;
		
		return dp.getDocument(input);
	}
	
	public static StyledText getEditorStyledText(AbstractTextEditor editor) {
		
		return (StyledText) editor.getAdapter(Control.class);
		
	}
	
	public static TextViewer getEditorTextViewer(AbstractTextEditor editor) {
		
		IVerticalRuler ruler = (IVerticalRuler) editor.getAdapter(IVerticalRulerInfo.class);
		
		if (ruler != null && (ruler instanceof CompositeRuler)) {
			
			return (TextViewer) ((CompositeRuler) ruler).getTextViewer();
			
		}
		
		return null;
	}
	
}
