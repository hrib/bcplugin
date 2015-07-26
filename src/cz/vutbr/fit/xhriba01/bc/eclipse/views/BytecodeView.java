package cz.vutbr.fit.xhriba01.bc.eclipse.views;


import java.util.HashMap;
import java.util.Map;

import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.part.*;
import org.eclipse.ui.progress.IWorkbenchSiteProgressService;
import org.eclipse.ui.texteditor.AbstractDecoratedTextEditor;
import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.jobs.IJobChangeEvent;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jdt.core.IClassFile;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.ui.JavaUI;
import org.eclipse.jface.text.Document;
import org.eclipse.jface.text.TextPresentation;
import org.eclipse.jface.viewers.*;
import org.eclipse.swt.graphics.Image;
import org.eclipse.jface.action.*;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.ui.*;
import org.eclipse.swt.widgets.Menu;

import cz.vutbr.fit.xhriba01.bc.eclipse.algo.ClassDir;
import cz.vutbr.fit.xhriba01.bc.eclipse.algo.ClassPackage;
import cz.vutbr.fit.xhriba01.bc.eclipse.algo.UserBytecode;
import cz.vutbr.fit.xhriba01.bc.eclipse.algo.UserBytecode.UserBytecodeDocument;
import cz.vutbr.fit.xhriba01.bc.eclipse.algo.WorkJob;
import cz.vutbr.fit.xhriba01.bc.eclipse.algo.WorkJobListener;
import cz.vutbr.fit.xhriba01.bc.eclipse.ui.BytecodeViewer;
import cz.vutbr.fit.xhriba01.bc.eclipse.ui.MappedLineNumberRulerColumn;
import cz.vutbr.fit.xhriba01.bc.lib.IClassContainer;
import cz.vutbr.fit.xhriba01.bc.lib.IFile2;


/**
 * This sample class demonstrates how to plug-in a new
 * workbench view. The view shows data obtained from the
 * model. The sample creates a dummy model on the fly,
 * but a real implementation would connect to the model
 * available either in this or another plug-in (e.g. the workspace).
 * The view is connected to the model using a content provider.
 * <p>
 * The view uses a label provider to define how model
 * objects should be presented in the view. Each
 * view can present the same model objects using
 * different labels and icons, if needed. Alternatively,
 * a single label provider can be shared between views
 * in order to ensure that objects of the same type are
 * presented in the same way everywhere.
 * <p>
 */

public class BytecodeView extends ViewPart {

	/**
	 * The ID of the view as specified by the extension.
	 */
	public static final String ID = "cz.vutbr.fit.xhriba01.bc.eclipse.views.BytecodeView";

	private BytecodeViewer fBytecodeViewer;
	
	private IJavaElement fJavaElement;
	
	private IEditorPart fEditor;
	
	private WorkJob fJob;
	
	private MappedLineNumberRulerColumn fMappedLineNumberRulerColumn;
	
	private Action action1;
	private Action action2;
	private Action doubleClickAction;

	/*
	 * The content provider class is responsible for
	 * providing objects to the view. It can wrap
	 * existing objects in adapters or simply return
	 * objects as-is. These objects may be sensitive
	 * to the current input of the view, or ignore
	 * it and always show the same content 
	 * (like Task List, for example).
	 */
	 
	class ViewContentProvider implements IStructuredContentProvider {
		public void inputChanged(Viewer v, Object oldInput, Object newInput) {
		}
		public void dispose() {
		}
		public Object[] getElements(Object parent) {
			return new String[] { "One", "Two", "Three" };
		}
	}
	class ViewLabelProvider extends LabelProvider implements ITableLabelProvider {
		public String getColumnText(Object obj, int index) {
			return getText(obj);
		}
		public Image getColumnImage(Object obj, int index) {
			return getImage(obj);
		}
		public Image getImage(Object obj) {
			return PlatformUI.getWorkbench().
					getSharedImages().getImage(ISharedImages.IMG_OBJ_ELEMENT);
		}
	}
	class NameSorter extends ViewerSorter {
	}

	/**
	 * The constructor.
	 */
	public BytecodeView() {
		
	}
	
	public IJavaElement getJavaElement() {
		return fJavaElement;
	}
	
	int counter = 0;
	
	public void test(IJavaElement javaElement) {
		
		fBytecodeViewer.setDocument(new Document(counter + " - " + javaElement.getElementName()));
		
		counter++;
	}
	
	public void setJobResult(IJobChangeEvent event) {
		
		WorkJob job = (WorkJob) event.getJob();
		
		if (fJob != job) {
			return;
		}
		
		fJob = null;
		
		if (!job.getResult().isOK()) {
			return;
		}
		
		UserBytecode bytecode = job.getUserBytecode();
		
		if (bytecode == null) {
			return;
		}
		
		UserBytecodeDocument doc = bytecode.getDocument();
		
		fBytecodeViewer.setDocument(doc);
		
		TextPresentation.applyTextPresentation(doc.getTextPresentation(), fBytecodeViewer.getTextWidget());
		
		if (fEditor instanceof AbstractDecoratedTextEditor) {
			AbstractDecoratedTextEditor editor0 = (AbstractDecoratedTextEditor) fEditor;
			//editor0.
		}
		
		fMappedLineNumberRulerColumn.setLineMap(doc.getLineMap(), doc.getMaxLineNumber());
		
		fMappedLineNumberRulerColumn.redraw();
		
		System.out.println("##job result handled!!");
		
	}
	
	public void setByJava(IFile2 javaSource, IClassContainer classContainer, IJavaElement javaElement, IEditorPart editor) {
		
		handleInput(javaSource, classContainer, javaElement, editor);
		
	}
	
	public void setByClass(IClassFile classFile, IFile2 javaSource, IClassContainer classContainer) {
		
		/*
		try {
			
			String javaSource = null;
			
			if (javaStringOrUnit instanceof String) {
				javaSource = (String) javaStringOrUnit;
			}
			else if (javaStringOrUnit instanceof ICompilationUnit) {
				javaSource = ((ICompilationUnit) javaStringOrUnit).getSource();
			}
			
			fJavaElement = classFile;
			
			handleInput(javaSource, classContainer);
			
		} catch (JavaModelException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		*/
		
		//handleInput(javaSource, classContainer);
		
	}
	
	private void handleInput(IFile2 javaSource, IClassContainer classContainer, IJavaElement javaElement, IEditorPart editor) {
		
		/*
		IClassContainer classContainer = null;
		
		if (classContainer instanceof IContainer) {
			classContainer = new ClassDir((IContainer) classContainerr);
		}
		else if (classContainer instanceof IPackageFragment) {
			classContainer = new ClassPackage((IPackageFragment) classContainerr);
		}
		*/
		/*
		try {
			
			if (javaElement.isStructureKnown() == false) {
				return;
			}
			
		} catch (JavaModelException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return;
		}
		*/
		startJob(javaSource, classContainer, javaElement, editor, new HashMap<String, Object>());
		
	}
	
	private void startJob(IFile2 javaSource, IClassContainer classContainer, IJavaElement javaElement, IEditorPart editor, Map<String, Object> options) {
		
		if (fJob != null) {
			fJavaElement = null;
			fEditor = null;
			fJob.cancel();
		}
		
		fJavaElement = javaElement;
		fEditor = editor;
		
		fJob = new WorkJob("Bytecode job", this, javaSource, classContainer, options);
		
		fJob.setPriority(Job.INTERACTIVE);
		
		fJob.addJobChangeListener(new WorkJobListener());
		
		IWorkbenchSiteProgressService service = (IWorkbenchSiteProgressService) getViewSite().getService(IWorkbenchSiteProgressService.class);
		
		service.schedule(fJob, 0, true);
		
	}
	
	public void clearJavaElement() {
		fJavaElement = null;
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
		
		makeActions();
		hookContextMenu();
		hookDoubleClickAction();
		contributeToActionBars();
	}

	private void hookContextMenu() {
		MenuManager menuMgr = new MenuManager("#PopupMenu");
		menuMgr.setRemoveAllWhenShown(true);
		menuMgr.addMenuListener(new IMenuListener() {
			public void menuAboutToShow(IMenuManager manager) {
				BytecodeView.this.fillContextMenu(manager);
			}
		});
		Menu menu = menuMgr.createContextMenu(fBytecodeViewer.getControl());
		fBytecodeViewer.getControl().setMenu(menu);
		getSite().registerContextMenu(menuMgr, fBytecodeViewer);
	}

	private void contributeToActionBars() {
		IActionBars bars = getViewSite().getActionBars();
		fillLocalPullDown(bars.getMenuManager());
		fillLocalToolBar(bars.getToolBarManager());
	}

	private void fillLocalPullDown(IMenuManager manager) {
		manager.add(action1);
		manager.add(new Separator());
		manager.add(action2);
	}

	private void fillContextMenu(IMenuManager manager) {
		manager.add(action1);
		manager.add(action2);
		// Other plug-ins can contribute there actions here
		manager.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS));
	}
	
	private void fillLocalToolBar(IToolBarManager manager) {
		manager.add(action1);
		manager.add(action2);
	}

	private void makeActions() {
		action1 = new Action() {
			public void run() {
				showMessage("Action 1 executed");
			}
		};
		action1.setText("Action 1");
		action1.setToolTipText("Action 1 tooltip");
		action1.setImageDescriptor(PlatformUI.getWorkbench().getSharedImages().
			getImageDescriptor(ISharedImages.IMG_OBJS_INFO_TSK));
		
		action2 = new Action() {
			public void run() {
				showMessage("Action 2 executed");
			}
		};
		action2.setText("Action 2");
		action2.setToolTipText("Action 2 tooltip");
		action2.setImageDescriptor(PlatformUI.getWorkbench().getSharedImages().
				getImageDescriptor(ISharedImages.IMG_OBJS_INFO_TSK));
		doubleClickAction = new Action() {
			public void run() {
				/*
				ISelection selection = viewer.getSelection();
				Object obj = ((IStructuredSelection)selection).getFirstElement();
				showMessage("Double-click detected on "+obj.toString());
				*/
			}
		};
	}

	private void hookDoubleClickAction() {
		/*
		viewer.addDoubleClickListener(new IDoubleClickListener() {
			public void doubleClick(DoubleClickEvent event) {
				doubleClickAction.run();
			}
		});
		*/
	}
	private void showMessage(String message) {
		MessageDialog.openInformation(
			fBytecodeViewer.getControl().getShell(),
			"Bytecode",
			message);
	}

	/**
	 * Passing the focus request to the viewer's control.
	 */
	public void setFocus() {
		fBytecodeViewer.getControl().setFocus();
	}
}