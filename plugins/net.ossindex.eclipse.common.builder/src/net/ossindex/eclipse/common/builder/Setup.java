package net.ossindex.eclipse.common.builder;

import org.eclipse.core.resources.IProject;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ITreeSelection;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.ISelectionListener;
import org.eclipse.ui.ISelectionService;
import org.eclipse.ui.IStartup;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.services.ISourceProviderService;

/** Register for selection events on projects. This allows us to ensure the
 * OssIndexBuildState is properly set.
 * 
 * @author Ken Duck
 *
 */
public class Setup implements IStartup {

	@Override
	public void earlyStartup()
	{
		Display.getDefault().asyncExec(new Runnable() {
			@Override
			public void run() {
				IWorkbench wb = PlatformUI.getWorkbench();
				final IWorkbenchWindow win = wb.getActiveWorkbenchWindow();
				ISelectionService selectionService = win.getSelectionService();

				ISelectionListener listener = new ISelectionListener()
				{

					/*
					 * (non-Javadoc)
					 * @see org.eclipse.ui.ISelectionListener#selectionChanged(org.eclipse.ui.IWorkbenchPart, org.eclipse.jface.viewers.ISelection)
					 */
					@Override
					public void selectionChanged(IWorkbenchPart part, ISelection selection)
					{
						if(selection instanceof ITreeSelection)
						{
							ITreeSelection s = (ITreeSelection)selection;
							Object obj = s.getFirstElement();
							if(obj instanceof IProject)
							{
								// Get the source provider service
								ISourceProviderService sourceProviderService = win.getService(ISourceProviderService.class);
								// now get my service
								ManualBuildState stateService = (ManualBuildState) sourceProviderService.getSourceProvider(ManualBuildState.MY_STATE);
								stateService.updateEnabled();
							}
						}
					}
				};

				selectionService.addSelectionListener(listener);

			}
		});
	}
}
