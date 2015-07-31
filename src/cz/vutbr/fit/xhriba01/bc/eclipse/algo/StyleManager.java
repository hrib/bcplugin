package cz.vutbr.fit.xhriba01.bc.eclipse.algo;

import org.eclipse.core.runtime.ListenerList;
import org.eclipse.jdt.ui.PreferenceConstants;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.editors.text.EditorsUI;

public final class StyleManager implements IPropertyChangeListener {
	
	private static StyleManager fSingleton;
	
	private ListenerList fStyleListeners;
	
	private Style fStyle;
	
	/**
	 * Must be called in UI thread.
	 */
	private StyleManager() {
		fStyle = createNewStyle();
		PlatformUI.getWorkbench().getThemeManager().getCurrentTheme().getFontRegistry().addListener(this);
		PreferenceConstants.getPreferenceStore().addPropertyChangeListener(this);
		PlatformUI.getPreferenceStore().addPropertyChangeListener(this);
		EditorsUI.getPreferenceStore().addPropertyChangeListener(this);
		fStyleListeners = new ListenerList();
	}
	
	public synchronized Style getStyle() {
		return fStyle;
	}
	
	private synchronized Style createNewStyle() {
		
		return new Style();
		
	}
	
	public static synchronized StyleManager getDefault() {
		
		if (fSingleton == null) {
			
			PlatformUI.getWorkbench().getDisplay().syncExec(new Runnable() {

				@Override
				public void run() {
					
					fSingleton = new StyleManager();
					
				}
				
				
			});

		}
		
		return fSingleton;
		
	}

	@Override
	public void propertyChange(PropertyChangeEvent event) {
		
		if (fStyle.affectsPresentation(event)) {
			
			PlatformUI.getWorkbench().getDisplay().asyncExec(new Runnable() {

				@Override
				public void run() {
					Style newStyle = createNewStyle();
					fireStyleChanged(newStyle, fStyle);
				}
				
			});
		}
		
	}
	
	/**
	 * Listeners are always called in UI thread !!
	 */
	private void fireStyleChanged(Style newStyles, Style oldStyles) {
		
		Object[] listeners = fStyleListeners.getListeners();
		
		StyleChangeEvent event = new StyleChangeEvent(this, newStyles, oldStyles);
		
		for (int i = 0; i < listeners.length; i++) {
			
			((IStyleListener) listeners[i]).styleChanged(event);
			
		}
		
	}
	
	public void addStyleListener(IStyleListener listener) {
		
		fStyleListeners.add(listener);
		
	}
	
	public void removeStyleListener(IStyleListener listener) {
		fStyleListeners.remove(listener);
	}
}
