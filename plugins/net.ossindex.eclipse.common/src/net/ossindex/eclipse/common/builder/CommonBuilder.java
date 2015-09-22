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
import java.util.Map;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.resources.IResourceDeltaVisitor;
import org.eclipse.core.resources.IResourceVisitor;
import org.eclipse.core.resources.IncrementalProjectBuilder;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.SubMonitor;

/** 
 * 
 * @author Ken Duck
 *
 */
public abstract class CommonBuilder extends IncrementalProjectBuilder
{
	/**
	 * For debug purposes only
	 */
	private static final boolean IGNORE_BATCH = false;

	public CommonBuilder()
	{
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.core.resources.IncrementalProjectBuilder#build(int, java.util.Map, org.eclipse.core.runtime.IProgressMonitor)
	 */
	@Override
	protected IProject[] build(int kind, Map<String, String> args, IProgressMonitor monitor) throws CoreException
	{
		// get the project to build  
		getProject();  

		if(kind == FULL_BUILD)
		{
			fullBuild(monitor);
		}
		else
		{
			IResourceDelta delta = getDelta(getProject());
			incrementalBuild(delta, monitor);
		}
		return null;
	}

	/**
	 * 
	 * @param monitor
	 */
	private void fullBuild(IProgressMonitor monitor)
	{
		try
		{
			IResourceVisitor visitor = getBuildVisitor(monitor);
			if(!IGNORE_BATCH && (visitor instanceof IBatchBuilder))
			{
				if(((IBatchBuilder)visitor).areFilesDirty(getProject()))
				{
					((IBatchBuilder)visitor).buildAll(getProject());
					((IBatchBuilder)visitor).markAllBuilt(getProject());
				}
			}
			else
			{
				if(visitor != null)
				{
					getProject().accept(visitor);
				}
				if(visitor instanceof IDelayedBuild)
				{
					((IDelayedBuild)visitor).finish();
				}
			}
		}
		catch (CoreException e)
		{
			System.err.println("Exception performing full build");
			e.printStackTrace();
		}
	}

	/** A Build Visitor is a class that actually performs the build operations.
	 * @param monitor 
	 * 
	 * @return
	 */
	protected abstract IResourceVisitor getBuildVisitor(IProgressMonitor monitor);

	/**
	 * 
	 * @param delta
	 * @param monitor
	 */
	private void incrementalBuild(IResourceDelta delta, IProgressMonitor monitor)
	{
		try
		{
			final CommonBuildVisitor visitor = (CommonBuildVisitor)getDeltaVisitor(null);

			// Get a full list of changed files. We want to do this instead of the
			// visitor so we know exactly how many files there are. This will be
			// used to provide better progress monitoring, but more importantly
			// it will allow us to decide whether to do batch processing
			// or not.
			final List<IFile> changed = new LinkedList<IFile>();
			IResourceDeltaVisitor deltaVisitor = new IResourceDeltaVisitor()
			{
				public boolean visit(IResourceDelta delta)
				{
					//only interested in content changes and added files
					if ((delta.getFlags() & IResourceDelta.CONTENT) == 0 &&
							(delta.getKind() & IResourceDelta.ADDED) == 0) return true;

					IResource resource = delta.getResource();
					//only interested in files with the "txt" extension
					if (resource instanceof IFile)
					{
						if(visitor.accepts((IFile)resource))
						{
							changed.add((IFile)resource);
						}
					}
					return true;
				}
			};

			delta.accept(deltaVisitor);

			if(changed.size() > 0)
			{
				if(!IGNORE_BATCH && (visitor instanceof IBatchBuilder))
				{
					if(changed.size() > getFullBuildThreshold())
					{
						visitor.setProgressMonitor(monitor);
						((IBatchBuilder)visitor).buildAll(getProject());
						((IBatchBuilder)visitor).markAllBuilt(getProject());
						return;
					}
				}

				// If we get here, then perform individual builds
				buildFiles(changed, monitor);
			}
		}
		catch (CoreException e)
		{
			System.err.println("Exception performing incremental build");
			e.printStackTrace();
		}
	}

	/** Number of individual files we are willing to build before forcing a full build.
	 * 
	 * Bear in mind for Java this number will include the changed class files as well,
	 * so make the number large enough to make a difference.
	 * 
	 * @return
	 */
	protected int getFullBuildThreshold()
	{
		return 4;
	}

	/** Build a list of files
	 * 
	 * @param changed
	 * @param monitor
	 */
	private void buildFiles(List<IFile> changed, IProgressMonitor monitor)
	{
		SubMonitor progress = SubMonitor.convert(monitor);
		progress.setWorkRemaining(changed.size());

		IResourceVisitor visitor = getBuildVisitor(null);

		for (IFile file : changed)
		{
			progress.setTaskName("Processing " + file.getName());
			try
			{
				visitor.visit(file);
			}
			catch (CoreException e)
			{
				e.printStackTrace();
			}
			progress.worked(1);
		}
	}

	protected abstract IResourceDeltaVisitor getDeltaVisitor(IProgressMonitor monitor);
}
