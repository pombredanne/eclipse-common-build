/**
 *	Copyright (c) 2015 V�r Security Inc.
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
package net.ossindex.eclipse.common.impl;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.QualifiedName;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;

/** Common code for both C and Java utils
 * 
 * @author Ken Duck
 *
 */
public abstract class CommonUtils
{
	/** Given a builder ID, clear all the timestamps
	 * 
	 * @param builderId
	 */
	public void clean(String builderId)
	{
		// Clear the timestamp. This tells the builder it needs to run again.
		final QualifiedName timestampQualifier = new QualifiedName(builderId, ".TIMESTAMP");

		String toolName = builderId;
		int index = toolName.lastIndexOf('.');
		if(index > 0) toolName = toolName.substring(index + 1);
		toolName = toolName.replace("Adaptor", "");
		Job job = new Job("Clean " + toolName + " build flags...") {
			@Override
			protected IStatus run(IProgressMonitor monitor) {

				IProject[] projects = ResourcesPlugin.getWorkspace().getRoot().getProjects();
				for (IProject project : projects)
				{
					if(project.isOpen())
					{
						clean(timestampQualifier, project);
					}
				}

				return Status.OK_STATUS;
			}
		};

		// Start the Job
		job.setPriority(Job.BUILD);
		job.schedule(); 
	}

	/** Recursively clear the resource
	 * 
	 * @param id
	 * @param resource
	 */
	protected void clean(QualifiedName id, IResource resource)
	{
		if(resource instanceof IFile)
		{
			try
			{
				((IFile)resource).setPersistentProperty(id, null);
			}
			catch (CoreException e)
			{
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		else if(resource instanceof IContainer)
		{
			try
			{
				IResource[] members = ((IContainer)resource).members();
				for (IResource member : members)
				{
					clean(id, member);
				}
			}
			catch (CoreException e)
			{
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

}
