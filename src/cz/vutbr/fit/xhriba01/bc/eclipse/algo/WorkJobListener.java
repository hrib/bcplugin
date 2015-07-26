package cz.vutbr.fit.xhriba01.bc.eclipse.algo;

import org.eclipse.core.runtime.jobs.IJobChangeEvent;
import org.eclipse.core.runtime.jobs.IJobChangeListener;
import org.eclipse.ui.PlatformUI;

import cz.vutbr.fit.xhriba01.bc.eclipse.views.BytecodeView;

public class WorkJobListener implements IJobChangeListener {

	@Override
	public void aboutToRun(IJobChangeEvent event) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void awake(IJobChangeEvent event) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void done(final IJobChangeEvent event) {
		
		final WorkJob job = (WorkJob) event.getJob();
		
		PlatformUI.getWorkbench().getDisplay().asyncExec(new Runnable () {
			
			@Override
			public void run() {
				
				BytecodeView bytecodeView = job.getBytecodeView();
				
				bytecodeView.setJobResult(event);
				
			}
			
		});
		
	}

	@Override
	public void running(IJobChangeEvent event) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void scheduled(IJobChangeEvent event) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void sleeping(IJobChangeEvent event) {
		// TODO Auto-generated method stub
		
	}
	
	
}
