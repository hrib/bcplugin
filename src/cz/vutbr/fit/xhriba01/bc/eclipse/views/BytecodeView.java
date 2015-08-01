package cz.vutbr.fit.xhriba01.bc.eclipse.views;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.part.*;
import org.eclipse.ui.progress.IWorkbenchSiteProgressService;
import org.eclipse.ui.texteditor.AbstractDecoratedTextEditor;
import org.eclipse.ui.texteditor.IDocumentProvider;
import org.eclipse.core.runtime.ListenerList;
import org.eclipse.core.runtime.jobs.IJobChangeEvent;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.ui.ISharedImages;
import org.eclipse.jdt.ui.JavaUI;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.Document;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.JFaceTextUtil;
import org.eclipse.jface.text.Position;
import org.eclipse.jface.text.TextPresentation;
import org.eclipse.jface.text.TextViewer;
import org.eclipse.jface.text.projection.ProjectionDocument;
import org.eclipse.jface.text.source.Annotation;
import org.eclipse.jface.text.source.AnnotationModel;
import org.eclipse.jface.text.source.IAnnotationModel;
import org.eclipse.jface.text.source.projection.ProjectionAnnotation;
import org.eclipse.jface.text.source.projection.ProjectionAnnotationModel;
import org.eclipse.jface.text.source.projection.ProjectionViewer;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;

import cz.vutbr.fit.xhriba01.bc.eclipse.algo.Style;
import cz.vutbr.fit.xhriba01.bc.eclipse.algo.StyleChangeEvent;
import cz.vutbr.fit.xhriba01.bc.eclipse.algo.UserBytecode;
import cz.vutbr.fit.xhriba01.bc.eclipse.algo.UserBytecode.UserBytecodeDocument;
import cz.vutbr.fit.xhriba01.bc.eclipse.BcUtils;
import cz.vutbr.fit.xhriba01.bc.eclipse.algo.FastWorkJob;
import cz.vutbr.fit.xhriba01.bc.eclipse.algo.IStyleListener;
import cz.vutbr.fit.xhriba01.bc.eclipse.algo.LineMap;
import cz.vutbr.fit.xhriba01.bc.eclipse.algo.StyleManager;
import cz.vutbr.fit.xhriba01.bc.eclipse.algo.WorkJob;
import cz.vutbr.fit.xhriba01.bc.eclipse.algo.WorkJobListener;
import cz.vutbr.fit.xhriba01.bc.eclipse.ui.BytecodeViewer;
import cz.vutbr.fit.xhriba01.bc.eclipse.ui.BytecodeViewerConfiguration;
import cz.vutbr.fit.xhriba01.bc.eclipse.ui.MappedLineNumberRulerColumn;
import cz.vutbr.fit.xhriba01.bc.lib.IClassContainer;
import cz.vutbr.fit.xhriba01.bc.lib.IFile;
import cz.vutbr.fit.xhriba01.bc.lib.Result;
import cz.vutbr.fit.xhriba01.bc.lib.Utils;


public class BytecodeView extends ViewPart implements IStyleListener, ISelectionProvider {

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
	
	private ListenerList fSelectionListeners = new ListenerList(ListenerList.IDENTITY);
	
	private MouseListener fJavaEditorMouseListener;
	
	private IStructuredSelection fSelection = StructuredSelection.EMPTY;
	
	private class JavaEditorMouseListener extends MouseAdapter {
			
		@Override
		public void mouseUp(MouseEvent e) {
			
			if (fEditor == null) return;
			
			IDocument doc = BcUtils.getEditorDocument(getJavaEditor());
				
			if (doc == null) return;
				
			StyledText widget = BcUtils.getEditorStyledText(getJavaEditor());
					
			if (widget == null) return;
						
			int lineIndex = widget.getLineIndex(e.y);
						
			TextViewer viewer = BcUtils.getEditorTextViewer(getJavaEditor());
						
			if (viewer == null) return;
							
			int modelLine = JFaceTextUtil.widgetLine2ModelLine(viewer, lineIndex);
							
			if (modelLine == -1) return;
			
			handleJavaLineSelected(modelLine);
			
		}
		
	};
	
	/**
	 * The constructor.
	 */
	public BytecodeView() {
		
	}
	
	private void handleJavaLineSelected(int javaLine) {
		
		LineMap lineMap = fMappedLineNumberRulerColumn.getLineMap();
		
		removeSelectedLineIndexes();
		
		if (lineMap == null) return;
		
		List<Integer> froms = lineMap.getFrom(widgetLineIndexToLineMap(javaLine));
		
		if (froms.isEmpty()) {
			return;
		}
		
		for (int from : froms) {
			
			addSelectedLineIndex(lineMapLineToWidgetLine(from));
			
		}
		
		int firstFrom = froms.get(0);
		int lastFrom = froms.get(froms.size()-1);
		
		IDocument doc = fBytecodeViewer.getDocument();
		
		try {
		
			int firstLineOffset = doc.getLineOffset(firstFrom);
			int lastLineOffset = doc.getLineOffset(lastFrom);
			
			fBytecodeViewer.revealRange(firstLineOffset, lastLineOffset-firstLineOffset);
			
		} 
		catch (BadLocationException e) {
			e.printStackTrace();
			return;
		}
		
	}
	
	private int lineMapLineToWidgetLine(int lineMapLine) {
		return lineMapLine-1;
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
		
		if (this.fJavaEditorMouseListener == null) {
			// register mouse listener on java editor to handle mouse click (line selections)
			StyledText javaWidget = getJavaStyledText();
			
			if (javaWidget != null) {
				
				this.fJavaEditorMouseListener = new JavaEditorMouseListener();
				
				javaWidget.addMouseListener(this.fJavaEditorMouseListener);
				
			}
			
			
		}
		
		System.out.println("##job result handled!!");
		
	}
	
	/**
	 * Returns StyledText widget for currently used AbstractTextEditor
	 * @return StyledText or null
	 */
	private StyledText getJavaStyledText() {
		
		if (this.fEditor != null) {
			
			StyledText javaWidget = (StyledText)this.getJavaEditor().getAdapter(Control.class);
			
			return javaWidget;
			
		}
		
		return null;
		
	}
	
	private void handleDocument(UserBytecodeDocument doc) {
		
		fBytecodeViewer.setDocument(doc, new AnnotationModel());
		
		fBytecodeViewer.configure(new BytecodeViewerConfiguration(doc.getTextPresentation()));
		
		fBytecodeViewer.enableProjection();
		
		ProjectionAnnotationModel paModel = fBytecodeViewer.getProjectionAnnotationModel();
		
		HashMap<Annotation, Position> map = new HashMap<>();
		
		for (Position pos : doc.getProjectionPositions()) {
			map.put(new ProjectionAnnotation(), pos);
		}
		
		paModel.modifyAnnotations(null, map, null);
		
		//TextPresentation.applyTextPresentation(doc.getTextPresentation(), fBytecodeViewer.getTextWidget());
		
		fMappedLineNumberRulerColumn.setLineMap(doc.getLineMap());
		
		fMappedLineNumberRulerColumn.redraw();
		
	}
	
	public void clean() {
		fJavaElement = null;
		if (fJob != null) fJob.cancel();
		fJob = null;
		fEditor = null;
		fShouldRefresh = false;
		fResult = null;
		fOptions = null;
		fBytecodeViewer.disableProjection();
		fBytecodeViewer.unconfigure();
		
		if (this.fJavaEditorMouseListener != null) {
			// remove java styled text mouse listener
			StyledText javaWidget = this.getJavaStyledText();
			
			if (javaWidget != null) {
				javaWidget.removeMouseListener(this.fJavaEditorMouseListener);
			}
			
			this.fJavaEditorMouseListener = null;
		}
		
		if (!fDisposed) {
			fSelectedLines = new ArrayList<Integer>();
			fMappedLineNumberRulerColumn.setLineMap(null);
			fBytecodeViewer.setDocument(new Document(""));
		}
	}
	
	private void setMessage(String message) {
		clean();
		fBytecodeViewer.setDocument(new Document(message));
	}
	
	public void setInput(IFile javaSource, IClassContainer classContainer, IJavaElement javaElement, IEditorPart editor, Map<String, Object> options) {
		
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
		
		
		/*
		if (fJob != null) {
			clean();
		}
		*/
		
		clean();
		
		fOptions = options;
		fJavaElement = javaElement;
		fEditor = editor;
		
		//System.out.println(Utils.inputStreamToString(javaSource.getContent()));
		
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
		
		fMappedLineNumberRulerColumn = new MappedLineNumberRulerColumn(this);
		
		fBytecodeViewer.addVerticalRulerColumn(fMappedLineNumberRulerColumn);
		
		StyleManager.getDefault().addStyleListener(this);
		
		fStyle = StyleManager.getDefault().getStyle();
		
		fBytecodeViewer.getTextWidget().setEditable(false);
		
		this.setTitleImage(JavaUI.getSharedImages().getImage(ISharedImages.IMG_OBJS_CFILE));
		
		initUIStyle();
		
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
	
	private void initUIStyle() {
		fBytecodeViewer.getTextWidget().setFont(fStyle.getFont());
		
		fBytecodeViewer.getTextWidget().setForeground(fStyle.getDefaultColor());
		
		fMappedLineNumberRulerColumn.setForeground(fStyle.getLineNumberRulerColor());
		
		fMappedLineNumberRulerColumn.setFont(fStyle.getFont());
		
		fMappedLineNumberRulerColumn.redraw();
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
	
	public void handleLineSelect(int line) {
		
		LineMap lineMap = fMappedLineNumberRulerColumn.getLineMap();
		
		if (lineMap == null) {
			removeSelectedLineIndexes();
			return;
			
		}
		
		int lineMapLine = widgetLineIndexToLineMap(line);
		
		int javaLine = lineMap.getTo(lineMapLine);
		
		if (javaLine == Utils.INVALID_LINE) {
			removeSelectedLineIndexes();
			return;
		}
		
		removeSelectedLineIndexes();
		
		addSelectedLineIndex(line);
		
		highlightInJavaEditor(javaLine);
		
		fireLineSelectionChanged();
				
	}
	
	private void fireLineSelectionChanged() {
		
		if (this.fSelectedLines.isEmpty()) {
			this.fSelection = StructuredSelection.EMPTY;
		}
		else {
			this.fSelection = new StructuredSelection(this.fSelectedLines);
		}
		
		for (Object listener : this.fSelectionListeners.getListeners()) {
			
			((ISelectionChangedListener) listener).selectionChanged(new SelectionChangedEvent(this, this.fSelection));
			
		}
		
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
		
		initUIStyle();
		
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

	@Override
	public void addSelectionChangedListener(ISelectionChangedListener listener) {
		// TODO Auto-generated method stub
		this.fSelectionListeners.add(listener);
	}

	@Override
	public ISelection getSelection() {
		// TODO Auto-generated method stub
		return this.fSelection;
	}

	@Override
	public void removeSelectionChangedListener(ISelectionChangedListener listener) {
		// TODO Auto-generated method stub
		this.fSelectionListeners.remove(listener);
	}

	@Override
	public void setSelection(ISelection selection) {
		// TODO Auto-generated method stub
		this.fSelection = (IStructuredSelection) selection;
		fireLineSelectionChanged();
	}

}