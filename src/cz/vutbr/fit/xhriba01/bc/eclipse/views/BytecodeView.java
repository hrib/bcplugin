package cz.vutbr.fit.xhriba01.bc.eclipse.views;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.part.*;
import org.eclipse.ui.progress.IWorkbenchSiteProgressService;
import org.eclipse.ui.texteditor.AbstractDecoratedTextEditor;
import org.eclipse.ui.texteditor.IDocumentProvider;
import org.eclipse.core.runtime.jobs.IJobChangeEvent;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jdt.core.IClassFile;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.ui.ISharedImages;
import org.eclipse.jdt.ui.JavaUI;
import org.eclipse.jdt.ui.cleanup.CleanUpContext;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.Document;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.TextPresentation;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.SelectionChangedEvent;

import cz.vutbr.fit.xhriba01.bc.eclipse.algo.Style;
import cz.vutbr.fit.xhriba01.bc.eclipse.algo.StyleChangeEvent;
import cz.vutbr.fit.xhriba01.bc.eclipse.algo.UserBytecode;
import cz.vutbr.fit.xhriba01.bc.eclipse.algo.UserBytecode.UserBytecodeDocument;
import cz.vutbr.fit.xhriba01.bc.eclipse.algo.FastWorkJob;
import cz.vutbr.fit.xhriba01.bc.eclipse.algo.IStyleListener;
import cz.vutbr.fit.xhriba01.bc.eclipse.algo.StyleManager;
import cz.vutbr.fit.xhriba01.bc.eclipse.algo.WorkJob;
import cz.vutbr.fit.xhriba01.bc.eclipse.algo.WorkJobListener;
import cz.vutbr.fit.xhriba01.bc.eclipse.ui.BytecodeViewer;
import cz.vutbr.fit.xhriba01.bc.eclipse.ui.MappedLineNumberRulerColumn;
import cz.vutbr.fit.xhriba01.bc.lib.IClassContainer;
import cz.vutbr.fit.xhriba01.bc.lib.IFile2;
import cz.vutbr.fit.xhriba01.bc.lib.Result;
import cz.vutbr.fit.xhriba01.bc.lib.Utils;


public class BytecodeView extends ViewPart implements IStyleListener {

	/**
	 * The ID of the view as specified by the extension.
	 */
	public static final String ID = "cz.vutbr.fit.xhriba01.bc.eclipse.views.BytecodeView";

	private BytecodeViewer fBytecodeViewer;
	
	private IJavaElement fJavaElement;
	
	private IEditorPart fEditor;
	
	private WorkJob fJob;
	
	private boolean fShouldRefresh;
	
	private Style fStyle;
	
	private Result fResult;
	
	private MappedLineNumberRulerColumn fMappedLineNumberRulerColumn;
	
	private List<Integer> fSelectedLines = new ArrayList<Integer>();
	
	private Map<String, Object> fOptions;
	
	private boolean fDisposed;
	
	/**
	 * The constructor.
	 */
	public BytecodeView() {
		
	}
	
	public IJavaElement getJavaElement() {
		return fJavaElement;
	}
	
	/**
	 * Must be called in UI thread
	 * @param event job event
	 */
	public void setJobResult(IJobChangeEvent event) {
		
		if (fDisposed) {
			return;
		}
		
		WorkJob job = (WorkJob) event.getJob();
		
		if (fJob != job) {
			return;
		}
		
		fJob = null;
		
		if (!job.getResult().isOK()) {
			clean();
			return;
		}
		
		UserBytecode bytecode = job.getUserBytecode();
		
		if (bytecode == null) {
			clean();
			return;
		}
		
		fResult = job.getBytecodeAlgorithmResult();
 		
		if (fShouldRefresh) {
			refresh();
			return;
		}
		
		UserBytecodeDocument doc = bytecode.getDocument();
		
		handleDocument(doc);
		
		System.out.println("##job result handled!!");
		
	}
	
	private void handleDocument(UserBytecodeDocument doc) {
		
		fBytecodeViewer.setDocument(doc);
		
		TextPresentation.applyTextPresentation(doc.getTextPresentation(), fBytecodeViewer.getTextWidget());
		
		fMappedLineNumberRulerColumn.setLineMap(doc.getLineMap(), doc.getMaxLineNumber());
		
		fMappedLineNumberRulerColumn.redraw();
		
	}
	
	public void clean() {
		fJavaElement = null;
		if (fJob != null) fJob.cancel();
		fJob = null;
		fEditor = null;
		fShouldRefresh = false;
		fResult = null;
		if (!fDisposed) {
			fSelectedLines = new ArrayList<Integer>();
			fMappedLineNumberRulerColumn.setLineMap(new HashMap<Integer, Integer>(), 0);
			fOptions = null;
			fBytecodeViewer.setDocument(new Document(""));
		}
	}
	
	private void setMessage(String message) {
		clean();
		fBytecodeViewer.setDocument(new Document(message));
	}
	
	public void setInput(IFile2 javaSource, IClassContainer classContainer, IJavaElement javaElement, IEditorPart editor, Map<String, Object> options) {
		
		if (fDisposed) {
			return;
		}
		
		if (!javaElement.exists()) {
			clean();
			return;
		}
		
		if (!(editor instanceof AbstractDecoratedTextEditor)) {
			setMessage("Unsupported editor " + editor.getClass().getName());
			return;
		}
		
		
		if (fJob != null) {
			clean();
		}
		
		fOptions = options;
		fJavaElement = javaElement;
		fEditor = editor;
		
		System.out.println(Utils.inputStreamToString(javaSource.getContent()));
		
		fJob = new WorkJob("Bytecode job", fStyle, this, javaSource, classContainer, options);
		
		fJob.setPriority(Job.INTERACTIVE);
		
		fJob.addJobChangeListener(new WorkJobListener());
		
		IWorkbenchSiteProgressService service = (IWorkbenchSiteProgressService) getViewSite().getService(IWorkbenchSiteProgressService.class);
		
		service.schedule(fJob, 0, true);
		
	}
	
	
	public BytecodeViewer getBytecodeViewer() {
		return fBytecodeViewer;
	}
	
	/**
	 * This is a callback that will allow us
	 * to create the viewer and initialize it.
	 */
	public void createPartControl(Composite parent) {
		
		fBytecodeViewer = new BytecodeViewer(parent);
		
		fMappedLineNumberRulerColumn = new MappedLineNumberRulerColumn();
		
		fBytecodeViewer.addVerticalRulerColumn(fMappedLineNumberRulerColumn);
		
		StyleManager.getDefault().addStyleListener(this);
		
		fStyle = StyleManager.getDefault().getStyle();
		
		fBytecodeViewer.getTextWidget().setFont(fStyle.getFont());
		
		fBytecodeViewer.getTextWidget().setEditable(false);
		
		this.setTitleImage(JavaUI.getSharedImages().getImage(ISharedImages.IMG_OBJS_CFILE));
		
		fBytecodeViewer.getTextWidget().addMouseListener(new MouseListener() {
			
			@Override
			public void mouseDoubleClick(MouseEvent e) {
				// TODO Auto-generated method stub
				
			}

			@Override
			public void mouseDown(MouseEvent e) {
				// TODO Auto-generated method stub
				
			}

			@Override
			public void mouseUp(MouseEvent e) {
				// TODO Auto-generated method stub
				if (e.button == 1) { //right click
					StyledText widget = fBytecodeViewer.getTextWidget();
					int line = widget.getLineIndex(e.y);
					handleLineSelect(line);
				}
			}
			
		});
	}
	
	@Override
	public void dispose() {
		fDisposed = true;
		clean();
		fMappedLineNumberRulerColumn = null;
		fStyle = null;
		StyleManager.getDefault().removeStyleListener(this);
	}
	
	private void addSelectedLineIndex(int line) {
		
		for (int line0 : fSelectedLines) {
			if (line0 == line) {
				return;
			}
		}
		
		fSelectedLines.add(line);
		
		StyledText widget = fBytecodeViewer.getTextWidget();
		
		widget.setLineBackground(line, 1 , fStyle.getSelectedLineBackgroundColor());
	}
	
	private void removeSelectedLineIndexes() {
		
		StyledText widget = fBytecodeViewer.getTextWidget();
		
		Color color = fStyle.getBackgroundColor();
		
		for (int line0 : fSelectedLines) {
			widget.setLineBackground(line0, 1, color);
		}
		
		fSelectedLines = new ArrayList<Integer>();
		
	}
	
	private void highlightInJavaEditor(int javaLine) {
		
		AbstractDecoratedTextEditor editor = getJavaEditor();
				
		try {
				
			IDocumentProvider docProvider = editor.getDocumentProvider();
			
			if (docProvider == null) return;
			
			IDocument doc = docProvider.getDocument(editor.getEditorInput());
			
			if (doc == null) return;
			
			int javaOffset = doc.getLineOffset(javaLine-1);
				
			editor.setHighlightRange(javaOffset, 0, true);
				
		} catch (BadLocationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return;
		}
		
	}
	
	private void handleLineSelect(int line) {
		
		Map<Integer, Integer> lineMap = fMappedLineNumberRulerColumn.getLineMap();
		
		if (lineMap == null) {
			removeSelectedLineIndexes();
			return;
			
		}
		
		int lineMapLine = widgetLineIndexToLineMap(line);
		
		Integer javaLine = lineMap.get(lineMapLine);
		
		if (javaLine == null) {
			removeSelectedLineIndexes();
			return;
		}
		
		removeSelectedLineIndexes();
		
		addSelectedLineIndex(line);
		
		highlightInJavaEditor(javaLine);
				
	}
	
	public AbstractDecoratedTextEditor getJavaEditor() {
		return (AbstractDecoratedTextEditor) fEditor;
	}
	
	private int widgetLineIndexToLineMap(int line) {
		return line+1;
	}
	
	/**
	 * Passing the focus request to the viewer's control.
	 */
	public void setFocus() {
		fBytecodeViewer.getControl().setFocus();
	}

	@Override
	public void styleChanged(StyleChangeEvent event) {
		
		Style currStyle = fStyle;
		
		fStyle = event.getNewStyle();
		
		fBytecodeViewer.getTextWidget().setFont(fStyle.getFont());
		
		if (fJob != null) {
			
			if (fJob.getStyle() != fStyle) {
				fShouldRefresh = true;
			}
		}
		else {
			if (currStyle != fStyle) {
				refresh();
			}
		}
	}
	
	/**
	 * Must be called in UI Thread.
	 */
	public void refresh() {
		
		if (fResult != null && !fDisposed) {
			
			FastWorkJob workJob = new FastWorkJob(fResult, fStyle);
			
			workJob.run();
			
			handleDocument(workJob.getUserBytecodeDocument());
			
		}
		
	}

}