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
package net.ossindex.eclipse.common.builder;

import net.ossindex.eclipse.common.ICUtils;
import net.ossindex.eclipse.common.Utils;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.resources.IResourceDeltaVisitor;
import org.eclipse.core.resources.IResourceVisitor;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.SubMonitor;

/** Build visitor that identifies appropriate files, collects interesting
 * build information, and calls a method that will be useful for sub-classing.
 * 
 * This class is responsible for managing progress.
 * 
 * @author Ken Duck
 *
 */
public abstract class CBuildVisitor implements IResourceVisitor, IResourceDeltaVisitor
{
	/**
	 * Progress monitor
	 */
	private SubMonitor progress;
	
	private ICUtils cutils = Utils.getCUtils();
	
	public CBuildVisitor(IProgressMonitor monitor)
	{
		progress = SubMonitor.convert(monitor);
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.core.resources.IResourceVisitor#visit(org.eclipse.core.resources.IResource)
	 */
	@Override
	public boolean visit(IResource resource) throws CoreException
	{
		// Handle cancellation
		if(cutils.isCppCompilationUnit(resource))
		{
//			System.out.println("  C VISIT: " + resource);
			
			// Regardless of the amount of progress reported so far,
	        // use 2% of the space remaining in the monitor to process the next node.
			progress.setWorkRemaining(50);
			build(resource);
			progress.worked(1);
		}
		return true;
	}

	protected abstract void build(IResource resource);

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.core.resources.IResourceDeltaVisitor#visit(org.eclipse.core.resources.IResourceDelta)
	 */
	@Override
	public boolean visit(IResourceDelta delta) throws CoreException
	{
		IResource resource = delta.getResource();
		return visit(resource);
	}

	/**
	 * Get the settings for the provided resource
	 */
	protected String[] getIncludePaths(IResource resource)
	{
		return cutils.getIncludePaths(resource);
	}

	/**
	 * Get the settings for the provided resource
	 */
	protected String[] getMacros(IResource resource)
	{
		return cutils.getMacros(resource);
	}
	
	/** Override the task name for the progress monitor
	 * 
	 * @param name
	 */
	protected void setTaskName(String name)
	{
		progress.setTaskName(name);
	}
}
