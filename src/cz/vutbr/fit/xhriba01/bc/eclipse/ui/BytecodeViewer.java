package cz.vutbr.fit.xhriba01.bc.eclipse.ui;

import org.eclipse.jface.text.source.CompositeRuler;
import org.eclipse.jface.text.source.SourceViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;

public class BytecodeViewer extends SourceViewer {

	public BytecodeViewer(Composite parent) {
		super(parent, new CompositeRuler(), SWT.H_SCROLL | SWT.V_SCROLL);
	}

}
