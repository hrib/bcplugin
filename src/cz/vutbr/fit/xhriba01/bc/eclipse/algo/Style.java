package cz.vutbr.fit.xhriba01.bc.eclipse.algo;

import org.eclipse.jdt.ui.JavaUI;
import org.eclipse.jdt.ui.PreferenceConstants;
import org.eclipse.jdt.ui.text.IColorManager;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.PreferenceConverter;
import org.eclipse.jface.resource.FontDescriptor;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.TextStyle;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.editors.text.EditorsUI;
import org.eclipse.ui.texteditor.AbstractDecoratedTextEditor;
import org.eclipse.ui.texteditor.AbstractDecoratedTextEditorPreferenceConstants;
import org.eclipse.ui.texteditor.AbstractTextEditor;
import org.eclipse.ui.texteditor.ChainedPreferenceStore;

public class Style {
	
	public TextStyle KEYWORD;
	
	public TextStyle INSTRUCTION;
	
	public TextStyle CLASS_ACCESS;
	
	public TextStyle CLASS_NAME;
	
	public TextStyle METHOD_ACCESS;
	
	public TextStyle METHOD_NAME;
	
	public TextStyle FIELD_ACCESS;
	
	public TextStyle FIELD_NAME;
	
	public TextStyle REFERENCE_FIELD;
	
	public TextStyle REFERENCE_CLASS;
	
	public TextStyle TYPE_PRIMITIVE;
	
	public TextStyle TYPE_OBJECT;
	
	public TextStyle PARAMETER_NAME;
	
	public TextStyle ARRAY_DIMENSIONS;
	
	public TextStyle COMMENT;
	
	private Font fFont;
	
	private Color fSelectedLineColor;
	
	
	private Color fBackground;
	
	public Style() {
		
		fFont = PlatformUI.getWorkbench().getThemeManager().getCurrentTheme().getFontRegistry().get(PreferenceConstants.EDITOR_TEXT_FONT);
		
		IColorManager jdtColors = JavaUI.getColorManager();
		
		IPreferenceStore jdtUIPrefs = PreferenceConstants.getPreferenceStore();
		
		IPreferenceStore prefs = new ChainedPreferenceStore(new IPreferenceStore[] {jdtUIPrefs, PlatformUI.getPreferenceStore(), EditorsUI.getPreferenceStore()});
		
		Display display = PlatformUI.getWorkbench().getDisplay();
		
		FontDescriptor baseDesc = FontDescriptor.createFrom(fFont); 
				
		FontDescriptor keywordDesc = baseDesc;
		if (prefs.getBoolean(PreferenceConstants.EDITOR_JAVA_KEYWORD_BOLD)) {
			keywordDesc = keywordDesc.setStyle(SWT.BOLD);
		}
		KEYWORD = new TextStyle();
		KEYWORD.foreground = jdtColors.getColor(PreferenceConstants.EDITOR_JAVA_KEYWORD_COLOR);
		KEYWORD.font = keywordDesc.createFont(PlatformUI.getWorkbench().getDisplay());
		
		
		INSTRUCTION = KEYWORD;
		/*
		INSTRUCTION = new TextStyle();
		INSTRUCTION.foreground = jdtColors.getColor(PreferenceConstants.EDITOR_JAVA_KEYWORD_COLOR);
		*/
		
		//RGB bgRgb = PreferenceConverter.getColor(jdtUIPrefs,AbstractDecoratedTextEditor.);
		
		fSelectedLineColor = new Color(display, PreferenceConverter.getColor(prefs, AbstractDecoratedTextEditorPreferenceConstants.EDITOR_CURRENT_LINE_COLOR));

		
		boolean useSystemBg = prefs.getBoolean(AbstractTextEditor.PREFERENCE_COLOR_BACKGROUND_SYSTEM_DEFAULT);	
		if (useSystemBg) {
			fBackground = null;
		}
		else {
			fBackground = new Color(display, PreferenceConverter.getColor(prefs, AbstractDecoratedTextEditor.PREFERENCE_COLOR_BACKGROUND));
		}
				
		
		
	}
	
	public Color getSelectedLineBackgroundColor() {
		return fSelectedLineColor;
	}
	
	public Color getBackgroundColor() {
		return fBackground;
	}
	
	public Font getFont() {
		return fFont;
	}
	
	public boolean affectsPresentation(PropertyChangeEvent event) {
		
		return true;
		
		/*
		if (event.getProperty().equals(PreferenceConstants.EDITOR_TEXT_FONT)) {
			return true;
		}
		
		return false;
		*/
		
	}

}