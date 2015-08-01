package cz.vutbr.fit.xhriba01.bc.eclipse.ui;

import java.util.Iterator;

import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.ITypedRegion;
import org.eclipse.jface.text.TextPresentation;
import org.eclipse.jface.text.hyperlink.DefaultHyperlinkPresenter;
import org.eclipse.jface.text.hyperlink.IHyperlinkPresenter;
import org.eclipse.jface.text.presentation.IPresentationReconciler;
import org.eclipse.jface.text.presentation.IPresentationRepairer;
import org.eclipse.jface.text.presentation.PresentationReconciler;
import org.eclipse.jface.text.source.ISourceViewer;
import org.eclipse.jface.text.source.SourceViewerConfiguration;
import org.eclipse.swt.custom.StyleRange;


public class BytecodeViewerConfiguration extends SourceViewerConfiguration {
	
	private TextPresentation fTextPresentation;
	
	public BytecodeViewerConfiguration(TextPresentation presentation) {
		fTextPresentation = presentation;
	}

	
	/**
	 * Returns the hyperlink presenter for the given source viewer.
	 * This implementation always returns the {@link DefaultHyperlinkPresenter}.
	 *
	 * @param sourceViewer the source viewer to be configured by this configuration
	 * @return the hyperlink presenter or <code>null</code> if no hyperlink support should be installed
	 * @since 3.1
	 */
	@Override
	public IHyperlinkPresenter getHyperlinkPresenter(ISourceViewer sourceViewer) {
		return null;
	}
	
	/**
	 * Returns the presentation reconciler ready to be used with the given source viewer.
	 *
	 * @param sourceViewer the source viewer
	 * @return the presentation reconciler or <code>null</code> if presentation reconciling should not be supported
	 */
	@Override
	public IPresentationReconciler getPresentationReconciler(ISourceViewer sourceViewer) {
		
		PresentationReconciler reconciler= new PresentationReconciler();
		
		reconciler.setDocumentPartitioning(getConfiguredDocumentPartitioning(sourceViewer));
		
		IPresentationRepairer repairer = new IPresentationRepairer() {
			
			@Override
			public void setDocument(IDocument document) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void createPresentation(TextPresentation presentation, ITypedRegion damage) {
				
				for(Iterator<StyleRange> it = fTextPresentation.getAllStyleRangeIterator(); it.hasNext(); ) {
					
					StyleRange styleRange = it.next();
					
					presentation.addStyleRange(styleRange);
					
				}
				
			}
		};
		
		reconciler.setRepairer(repairer, IDocument.DEFAULT_CONTENT_TYPE);
		
		return reconciler;
	}
	
}
