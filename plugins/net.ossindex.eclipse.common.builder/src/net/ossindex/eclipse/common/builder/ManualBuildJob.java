/**
 *	Copyright (c) 2015 Vör Security Inc.
 *	All rights reserved.
 *	
 *	Redistribution and use in source and binary forms, with or without
 *	modification, are permitted provided that the following conditions are met:
 *	    * Redistributions of source code must retain the above copyright
 *	      notice, this list of conditions and the following disclaimer.
 *	    * Redistributions in binary form must reproduce the above copyright
 *	      notice, this list of conditions and the following disclaimer in the
 *	      documentation and/or other materials provided with the distribution.
 *	    * Neither the name of the <organization> nor the
 *	      names of its contributors may be used to endorse or promote products
 *	      derived from this software without specific prior written permission.
 *	
 *	THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 *	ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 *	WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 *	DISCLAIMED. IN NO EVENT SHALL <COPYRIGHT HOLDER> BE LIABLE FOR ANY
 *	DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 *	(INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 *	LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 *	ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 *	(INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 *	SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package net.ossindex.eclipse.common.builder;

import java.util.LinkedList;
import java.util.List;

import org.eclipse.core.resources.ICommand;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.resources.IncrementalProjectBuilder;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.QualifiedName;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.SubMonitor;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.ui.PlatformUI;

import net.ossindex.eclipse.common.Activator;
import net.ossindex.eclipse.common.builder.service.ICommonBuildService;

/** Provide a "manual build" which is separate from the standard Eclipse build
 * process. The manual build is always a full build.
 * 
 * @author Ken Duck
 *
 */
public class ManualBuildJob extends Job
{
	public static QualifiedName MANUAL_BUILD_NAME = new QualifiedName(Activator.PLUGIN_ID, "ManualBuild");
	
	private IProject project;

	public ManualBuildJob(IProject project)
	{
		super("Manual Build");

		this.project = project;
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.core.runtime.jobs.Job#run(org.eclipse.core.runtime.IProgressMonitor)
	 */
	@Override
	protected IStatus run(IProgressMonitor monitor)
	{
		System.err.println("Start manual build: " + project);

		SubMonitor progress = SubMonitor.convert(monitor);

		try
		{
			// Only perform builds on builders registered with out build service
			ICommonBuildService buildService = (ICommonBuildService) PlatformUI.getWorkbench().getService(ICommonBuildService.class);
			
			List<ICommand> commands = new LinkedList<ICommand>();
			IProjectDescription desc = project.getDescription();
			for (ICommand command : desc.getBuildSpec())
			{
				String name = command.getBuilderName();
				if(buildService.containsBuilder(name))
				{
					commands.add(command);
				}
			}

			progress.setWorkRemaining(commands.size() * 2);
			for (ICommand command : commands)
			{
				System.err.println("Running builder " + command.getBuilderName() + "...");
				try
				{
					// Clean needs to know that this is a manual build
					project.setSessionProperty(MANUAL_BUILD_NAME, true);
					project.build(IncrementalProjectBuilder.CLEAN_BUILD, command.getBuilderName(), null, progress.newChild(1));
					project.setSessionProperty(MANUAL_BUILD_NAME, false);
					project.build(IncrementalProjectBuilder.FULL_BUILD, command.getBuilderName(), null, progress.newChild(1));
				}
				catch (CoreException e)
				{
					e.printStackTrace();
				}
			}
		}
		catch (CoreException e)
		{
			e.printStackTrace();
		}

		System.err.println("Manual build complete");

		return Status.OK_STATUS;
	}

}
