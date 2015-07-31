package cz.vutbr.fit.xhriba01.bc.eclipse.ui;

import java.util.HashMap;
import java.util.Map;
import java.util.Stack;

import org.eclipse.jdt.core.ElementChangedEvent;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IElementChangedListener;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaElementDelta;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.ui.JavaUI;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IPartListener2;
import org.eclipse.ui.IWindowListener;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchPartReference;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;

import cz.vutbr.fit.xhriba01.bc.eclipse.BcUtils;
import cz.vutbr.fit.xhriba01.bc.eclipse.algo.IClassFromJavaStrategy;
import cz.vutbr.fit.xhriba01.bc.eclipse.algo.SourceDirectoryStrategy;
import cz.vutbr.fit.xhriba01.bc.eclipse.views.BytecodeView;
import cz.vutbr.fit.xhriba01.bc.lib.IClassContainer;
import cz.vutbr.fit.xhriba01.bc.lib.IFile;
import cz.vutbr.fit.xhriba01.bc.lib.StringFile;

public class BcUI implements IWindowListener {
	
	private static class State implements IPartListener2, IElementChangedListener {
		
		private enum EDITOR_TYPE {
			NONE,
			UNKNOWN,
			CLASS,
			JAVA
		};
		
		private IWorkbenchWindow fWindow;
		
		private BytecodeView fBytecodeView;
		
		private boolean fIsFirstActivated = true;
		
		private IJavaElement fJavaElement;
		
		private void startRefresh(final IJavaElement javaElement) {
				
				PlatformUI.getWorkbench().getDisplay().asyncExec(new Runnable() {

					@Override
					public void run() {
						
						PlatformUI.getWorkbench().getDisplay().timerExec(2000, new Runnable() {
							
							@Override
							public void run() {
								State.this.refresh(javaElement);
							}
							
						});
					}
					
				});
			
		}
		
		@Override
		public void elementChanged(ElementChangedEvent event) {
			
			if (event.getType() != ElementChangedEvent.POST_CHANGE) {
				return;
			}
			
			IJavaElementDelta delta = event.getDelta();
			
			Stack<IJavaElementDelta> deltaStack = new Stack<IJavaElementDelta>();
			
			deltaStack.push(delta);
			
			do {
				
				IJavaElementDelta popedDelta = deltaStack.pop();
				
				IJavaElement el = popedDelta.getElement();
				
				if (el != null) {
					
					synchronized (this) {
						
						if (el.equals(fJavaElement)) {
							startRefresh(el);
							return;
						}
					
					}
				}
				
				for (IJavaElementDelta childDelta : popedDelta.getAffectedChildren()) {
					deltaStack.add(childDelta);
				}
				
			} while(!deltaStack.isEmpty());
			
		}
		
		private State(IWorkbenchWindow window) {
			fWindow = window;
			//fWindow.addPageListener(this);
			//fWindow.addPerspectiveListener(this);
			fWindow.getActivePage().addPartListener(this);
			JavaCore.addElementChangedListener(this);
		}
		
		private EDITOR_TYPE getEditorType(IWorkbenchPartReference partRef) {
			//return partRef.getId().equals(JavaUI.ID_CU_EDITOR);
			IWorkbenchPart part = partRef.getPart(false);
			
			if (!(part instanceof IEditorPart)) {
				return EDITOR_TYPE.NONE;
			}
			
			IEditorPart editorPart = (IEditorPart) part;
			IEditorInput editorInput = editorPart.getEditorInput();
			
			IJavaElement javaElement = JavaUI.getEditorInputJavaElement(editorInput);
			
			if (javaElement == null) {
				return EDITOR_TYPE.UNKNOWN;
			}
			
			int elementType = javaElement.getElementType();
			
			if (elementType == IJavaElement.COMPILATION_UNIT) {
				return EDITOR_TYPE.JAVA;
			}
			else if (elementType == IJavaElement.CLASS_FILE) {
				return EDITOR_TYPE.CLASS;
			}
			
			return EDITOR_TYPE.UNKNOWN;
		}
		
		private boolean isBytecodeView(IWorkbenchPartReference partRef) {
			return partRef.getId().equals(BytecodeView.ID);
		}
		
		private void initBytecodeView() {
			
			if (fBytecodeView != null) {
				return;
			}
			
			fBytecodeView = (BytecodeView) fWindow.getActivePage().findView(BytecodeView.ID);
		}
		
		private void refresh(IJavaElement javaElement) {
			
			if (fJavaElement != null && fJavaElement.equals(javaElement)) {
				fJavaElement = null;
				initEditorView();
			}
			
		}
		
		private synchronized void initEditorView() {
			
			if (fBytecodeView == null) {
				return;
			}
			
			IWorkbenchPage page = fWindow.getActivePage();
			IWorkbenchPart part = page.getActiveEditor();
			
			if (part == null) return;
			
			IWorkbenchPartReference partRef = page.getReference(part);
			
			EDITOR_TYPE editorType = getEditorType(partRef);
			
			if (editorType == EDITOR_TYPE.JAVA) {
				
				IJavaElement javaElement = BcUtils.getEditorJavaElement((IEditorPart) part);
				
				if (javaElement == null || (fJavaElement != null && fJavaElement.equals(javaElement))) {
					return;
				}
				
				handleJavaEditorChange((ICompilationUnit)javaElement, (IEditorPart) part);
				
				fJavaElement = javaElement;
				
				//fBytecodeView.test(javaElement);
			}
			else if (editorType == EDITOR_TYPE.CLASS) {
				
				/*
				IJavaElement javaElement = BcUtils.getEditorJavaElement((IEditorPart) part);
				
				if (javaElement == null || (fJavaElement != null && fJavaElement.equals(javaElement))) {
					return;
				}
				
				//handleClassEditorActivatedEvent((IEditorPart) part);
				
				fJavaElement = javaElement;
				
				//fBytecodeView.test(javaElement);
				 * 
				 */
			}
				
		}
		
		private void handleJavaEditorChange(ICompilationUnit compilationUnit, IEditorPart editor) {
			//sync(BcUtils.getEditorJavaElement(editor));
			
			/*
			IWorkbenchSiteProgressService progressService = 
					(IWorkbenchSiteProgressService) fBytecodeView.getViewSite().getAdapter(IWorkbenchSiteProgressService.class);
			
			progressService.schedule(new Job("Pokus") {

				@Override
				protected IStatus run(IProgressMonitor monitor) {
					
					try {
						Thread.sleep(5000);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					
					return Status.OK_STATUS;
				}
				
			}, 0, true);
			*/
			
			IClassFromJavaStrategy[] strategies = new IClassFromJavaStrategy[] {
					new SourceDirectoryStrategy()
			};
			
			IClassContainer classContainer = null;
			
			for (IClassFromJavaStrategy strategy : strategies) {
				
				classContainer = strategy.getClassFromJava(compilationUnit);
				
				if (classContainer != null) {
					break;
				}
				
			}
			
			if (classContainer == null) {
				return;
			}
			
			IFile javaFile = null;
			
			try {
				
				javaFile = new StringFile(compilationUnit.getSource(), compilationUnit.getElementName());
				
			} catch (JavaModelException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				return;
			}
			
			fBytecodeView.setInput(javaFile, classContainer, compilationUnit, editor, null);
			
		}
		
		/*
		private void handleClassEditorActivatedEvent(IEditorPart editor) {
			
			IClassFile classElement = (IClassFile)BcUtils.getEditorJavaElement(editor);
			
			editor.getEditorSite().getPage().closeEditor(editor, false);
			
			IJavaFromClassStrategy[] strategies = new IJavaFromClassStrategy[] {
				new SourceAttachmentStrategy(),
				new OutputLocationStrategy()
			};
			
			Object javaSource = null;
			
			for (IJavaFromClassStrategy strategy : strategies) {
				
				javaSource = strategy.getJavaSource(classElement);
				
				if (javaSource != null) {
					System.out.println(javaSource);
					System.out.println(strategy.getClass().getCanonicalName());
					break;
				}
				
			}
			
			if (javaSource == null) {
				System.out.println("Nepodarilo se nalezt java source!");
				return;
			}
			
			fBytecodeView.setByClass(classElement, javaSource, BcUtils.getContainerOrPackageFragment(classElement));
			
			System.out.println("Podarilo se nalezt java source!");
		}
		
		*/
		
		public void windowClosed() {
			
			//fWindow.removePageListener(this);
			
			//fWindow.removePerspectiveListener(this);
			
			fWindow.getActivePage().removePartListener(this);
			JavaCore.removeElementChangedListener(this);
			
		}
		
		public void windowActivated() {
			
			if (fIsFirstActivated) {
				fIsFirstActivated = false;
				initBytecodeView();
				initEditorView();
			}
			
		}

		public void windowDeactivated() {
			
		}
		
		@Override
		public void partActivated(IWorkbenchPartReference partRef) {
			
			initEditorView();
		}

		@Override
		public void partBroughtToTop(IWorkbenchPartReference partRef) {
			
			if (fBytecodeView != null) {
				return;
			}
			
			if (isBytecodeView(partRef)) {
				initBytecodeView();
				initEditorView();
			}
			
		}

		@Override
		public void partClosed(IWorkbenchPartReference partRef) {
			// TODO Auto-generated method stub
			if (isBytecodeView(partRef)) {
				
				fBytecodeView = null;
				fJavaElement = null;
	
			}
			else {
				
				if (fBytecodeView != null) {
				
					if (partRef.getPart(false) == fBytecodeView.getJavaEditor()) {
						fBytecodeView.clean();
						fJavaElement = null;
					}
				}
			}
			
		}

		@Override
		public void partDeactivated(IWorkbenchPartReference partRef) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void partOpened(IWorkbenchPartReference partRef) {
			
			if (isBytecodeView(partRef)) {
				initBytecodeView();
				initEditorView();
			}
			
		}

		@Override
		public void partHidden(IWorkbenchPartReference partRef) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void partVisible(IWorkbenchPartReference partRef) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void partInputChanged(IWorkbenchPartReference partRef) {
			// TODO Auto-generated method stub
			
		}
	}
		
	private static BcUI fInstance = new BcUI();
	
	/*
	private static final int INVALID_BYTECODEVIEW_STATE = -1;
	
	private int fBytecodeViewState = INVALID_BYTECODEVIEW_STATE;
	*/
	
	private Map<IWorkbenchWindow, State> fStates = new HashMap<>();
	
	private BcUI() {};
	
	public static BcUI getInstance() {
		return fInstance;
	}
	
	public void init() {
		PlatformUI.getWorkbench().getDisplay().asyncExec(new Runnable() {
			@Override
			public void run() {
				/*
				for (IWorkbenchWindow window : PlatformUI.getWorkbench().getWorkbenchWindows()) {
				}
				*/
				windowActivated(PlatformUI.getWorkbench().getActiveWorkbenchWindow());
			}
		});
	}
	
	public State getStateForWindow(IWorkbenchWindow window) {
		return fStates.get(window);
	}
	
	private void removeStateForWindow(IWorkbenchWindow window) {
		
		fStates.remove(window);
		
	}
	
	private void addStateForWindow(State state, IWorkbenchWindow window) {
		fStates.put(window, state);
	}
	
	private State createState(IWorkbenchWindow window) {
		
		return new State(window);
		
	}
	
	/*
	private void sync(IJavaElement element) {
		
		if (!element.exists() || element.getElementType() != IJavaElement.COMPILATION_UNIT) {
			return;
		}
		
		IJavaElement bytecodeJavaElement = fBytecodeView.getJavaElement();
		
		if (bytecodeJavaElement != null && bytecodeJavaElement.equals(element)) return;
		
		fBytecodeView.setJavaElement(element);
		
	}
	*/
	
	/*
	private void handleJavaEditorClosedEvent(IEditorPart editor) {
		IJavaElement element = JavaUI.getEditorInputJavaElement(editor.getEditorInput());
		if (!element.exists() || element.getElementType() != IJavaElement.COMPILATION_UNIT) {
			return;
		}
		IJavaElement bytecodeJavaElement = fBytecodeView.getJavaElement();
		
		if (bytecodeJavaElement != null && bytecodeJavaElement.equals(element)) fBytecodeView.clearJavaElement();
		
	}
	
	private void handleClassEditorOpenedEvent(IEditorPart part) {
		
		
		
	}
	
	private void handleClassEditorActivatedEvent(IEditorPart editor) {
		
		IClassFile classElement = (IClassFile)BcUtils.getEditorJavaElement(editor);
		
		editor.getEditorSite().getPage().closeEditor(editor, false);
		
		IJavaFromClassStrategy[] strategies = new IJavaFromClassStrategy[] {
			new SourceAttachmentStrategy(),
			new OutputLocationStrategy()
		};
		
		Object javaSource = null;
		
		for (IJavaFromClassStrategy strategy : strategies) {
			
			javaSource = strategy.getJavaSource(classElement);
			
			if (javaSource != null) {
				System.out.println(javaSource);
				System.out.println(strategy.getClass().getCanonicalName());
				break;
			}
			
		}
		
		if (javaSource == null) {
			System.out.println("Nepodarilo se nalezt java source!");
			return;
		}
		
		fBytecodeView.setByClass(classElement, javaSource, BcUtils.getContainerOrPackageFragment(classElement));
		
		System.out.println("Podarilo se nalezt java source!");
	}
	
	private void handleClassEditorClosedEvent(IEditorPart editor) {
		
	}
	
	public boolean isBytecodeViewVisible() {
		return hasBytecodeView() && fBytecodeViewState != IWorkbenchPage.STATE_MINIMIZED; 
	}

	 */
	
	@Override
	public synchronized void windowActivated(IWorkbenchWindow window) {	
		
		State state = getStateForWindow(window);
		
		if (state == null) {
			
			state = createState(window);
			
			addStateForWindow(state, window);
			
			state.windowActivated();
		}
		else {
			state.windowActivated();
		}
		
		//System.out.println("window activated...");
	}
	
	@Override
	public void windowDeactivated(IWorkbenchWindow window) {
		// TODO Auto-generated method stub
		
		State state = getStateForWindow(window);
		
		if (state == null) {
			return;
		}
		
		state.windowDeactivated();
		
	}

	@Override
	public void windowClosed(IWorkbenchWindow window) {
		// TODO Auto-generated method stub
		
		State state = getStateForWindow(window);
		
		if (state != null) {
			
			state.windowClosed();
		
			removeStateForWindow(window);
		}
		
	}

	@Override
	public void windowOpened(IWorkbenchWindow window) {
		// TODO Auto-generated method stub
		
	}

	
}
