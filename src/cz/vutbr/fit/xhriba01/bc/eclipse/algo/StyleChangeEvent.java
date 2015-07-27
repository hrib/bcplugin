package cz.vutbr.fit.xhriba01.bc.eclipse.algo;

import java.util.EventObject;

public class StyleChangeEvent extends EventObject {
	
	private static final long serialVersionUID = -4778595492800044211L;

	private Style fNewStyle;
	
	private Style fOldStyle;
	
	public StyleChangeEvent(Object source, Style newStyles, Style oldStyles) {
		super(source);
		fNewStyle = newStyles;
		fOldStyle = oldStyles;
	}

	public Style getNewStyle() {
		return fNewStyle;
	}
	
	public Style getOldStyle() {
		return fOldStyle;
	}
	
	public StyleManager getStyleManager() {
		return (StyleManager) source;
	}
	
}