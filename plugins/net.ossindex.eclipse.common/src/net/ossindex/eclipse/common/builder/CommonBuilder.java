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

import java.util.Map;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.resources.IResourceDeltaVisitor;
import org.eclipse.core.resources.IResourceVisitor;
import org.eclipse.core.resources.IncrementalProjectBuilder;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;

/** 
 * 
 * @author Ken Duck
 *
 */
public abstract class CommonBuilder extends IncrementalProjectBuilder
{
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
			System.err.println("BEGIN BUILD");
			IResourceVisitor visitor = getBuildVisitor(monitor);
			if(visitor instanceof IBatchBuilder)
			{
				((IBatchBuilder)visitor).buildAll(getProject());
			}
			else
			{
				if(visitor != null) getProject().accept(visitor);
				if(visitor instanceof IDelayedBuild)
				{
					((IDelayedBuild)visitor).finish();
				}
			}
			System.err.println("END BUILD");
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
			IResourceDeltaVisitor visitor = getDeltaVisitor(monitor);
			if(visitor != null) delta.accept(visitor);
		}
		catch (CoreException e)
		{
			System.err.println("Exception performing incremental build");
			e.printStackTrace();
		}
	}

	protected abstract IResourceDeltaVisitor getDeltaVisitor(IProgressMonitor monitor);
}
