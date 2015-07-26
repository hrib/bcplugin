package cz.vutbr.fit.xhriba01.bc.eclipse;

import org.eclipse.ui.IStartup;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.PlatformUI;

import cz.vutbr.fit.xhriba01.bc.eclipse.ui.BcUI;
import cz.vutbr.fit.xhriba01.bc.lib.BinaryName;

public class Startup implements IStartup {

	@Override
	public void earlyStartup() {
		BinaryName binaryName = new BinaryName("kiklop");
		IWorkbench wb = PlatformUI.getWorkbench();
		wb.addWindowListener(BcUI.getInstance());
		BcUI.getInstance().init();
	}

}
