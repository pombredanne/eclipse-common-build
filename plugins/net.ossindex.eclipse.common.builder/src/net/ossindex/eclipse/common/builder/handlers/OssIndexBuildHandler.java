package net.ossindex.eclipse.common.builder.handlers;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ITreeSelection;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.ISelectionService;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;

import net.ossindex.eclipse.common.builder.ManualBuildJob;

/**
 * Our sample handler extends AbstractHandler, an IHandler base class.
 * @see org.eclipse.core.commands.IHandler
 * @see org.eclipse.core.commands.AbstractHandler
 */
public class OssIndexBuildHandler extends AbstractHandler {
	/**
	 * The constructor.
	 */
	public OssIndexBuildHandler() {
	}

	/**
	 * the command has been executed, so extract extract the needed information
	 * from the application context.
	 */
	public Object execute(ExecutionEvent event) throws ExecutionException {
		Display.getDefault().asyncExec(new Runnable() {
			@Override
			public void run() {
				IWorkbench wb = PlatformUI.getWorkbench();
				final IWorkbenchWindow win = wb.getActiveWorkbenchWindow();
				ISelectionService selectionService = win.getSelectionService();
				ISelection selection = selectionService.getSelection();
				if(selection instanceof ITreeSelection)
				{
					ITreeSelection s = (ITreeSelection)selection;
					Object obj = s.getFirstElement();
					if(obj instanceof IProject)
					{
						ManualBuildJob job = new ManualBuildJob((IProject)obj);
						job.setUser(true);
						job.setPriority(Job.LONG);
						job.schedule();
					}
				}
			}
		});
		return null;
	}
}
