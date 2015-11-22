package net.ossindex.eclipse.common.builder.handlers;

import java.util.Map;

import org.eclipse.cdt.core.model.ICProject;
import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ITreeSelection;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.ISelectionService;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.commands.IElementUpdater;
import org.eclipse.ui.menus.UIElement;

import net.ossindex.eclipse.common.builder.ManualBuildJob;
import net.ossindex.eclipse.common.builder.service.ICommonBuildService;

/**
 * Our sample handler extends AbstractHandler, an IHandler base class.
 * @see org.eclipse.core.commands.IHandler
 * @see org.eclipse.core.commands.AbstractHandler
 */
public class OssIndexBuildHandler extends AbstractHandler implements IElementUpdater
{
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
					if(obj instanceof IJavaProject)
					{
						obj = (IProject)((IJavaProject)obj).getProject();
					}
					if(obj instanceof ICProject)
					{
						obj = (IProject)((ICProject)obj).getProject();
					}
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

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.commands.IElementUpdater#updateElement(org.eclipse.ui.menus.UIElement, java.util.Map)
	 */
	@Override
	public void updateElement(UIElement element, @SuppressWarnings("rawtypes") Map parameters)
	{
		ICommonBuildService buildService = (ICommonBuildService) PlatformUI.getWorkbench().getService(ICommonBuildService.class);
		element.setText(buildService.getMenuText());
		element.setTooltip(buildService.getMenuText());
		ImageDescriptor icon = buildService.getIcon();
		if(icon != null) element.setIcon(icon);
	}
}
