package cz.vutbr.fit.xhriba01.bc.eclipse.ui;

import org.eclipse.jdt.ui.JavaUI;
import org.eclipse.jface.text.source.Annotation;
import org.eclipse.jface.text.source.CompositeRuler;
import org.eclipse.jface.text.source.IAnnotationAccess;
import org.eclipse.jface.text.source.ISharedTextColors;
import org.eclipse.jface.text.source.projection.ProjectionSupport;
import org.eclipse.jface.text.source.projection.ProjectionViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.texteditor.DefaultMarkerAnnotationAccess;



public class BytecodeViewer extends ProjectionViewer {

	
	public BytecodeViewer(Composite parent) {
		
		super(parent, new CompositeRuler(), null, true, SWT.H_SCROLL | SWT.V_SCROLL);
	
		ProjectionSupport projectionSupport = new ProjectionSupport(this, getAnnotationAccess(), getSharedTextColors());
		
		projectionSupport.install();		
		
		
		
	}
	
	private ISharedTextColors getSharedTextColors() {
		return JavaUI.getColorManager();
	}
	
	private IAnnotationAccess getAnnotationAccess() {
		return new DefaultMarkerAnnotationAccess();
	}
	

}
