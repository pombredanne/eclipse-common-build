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

import java.util.List;
import java.util.concurrent.Callable;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResourceVisitor;

/** A job that performs a build step concurrently
 * 
 * @author Ken Duck
 *
 */
public class ConcurrentBuildJob implements Callable<ConcurrentBuildJob>, Runnable
{

	private IResourceVisitor visitor;
	private IFile file;
	private List<IBuildJobListener> listeners;

	public ConcurrentBuildJob(IResourceVisitor visitor, IFile file, List<IBuildJobListener> listeners)
	{
		this.visitor = visitor;
		this.file = file;
		this.listeners = listeners;
	}

	/*
	 * (non-Javadoc)
	 * @see java.util.concurrent.Callable#call()
	 */
	@Override
	public ConcurrentBuildJob call() throws Exception
	{
		try
		{
			System.err.println("Pre-build:    " + file);
			if(listeners != null)
			{
				for (IBuildJobListener listener : listeners)
				{
					listener.buildStarted(file);
				}
			}
			System.err.println("  Building:   " + file);
			visitor.visit(file);
			System.err.println("  Built:      " + file);
		}
		finally
		{
			if(listeners != null)
			{
				for (IBuildJobListener listener : listeners)
				{
					listener.buildCompleted(file);
				}
			}
		}
		System.err.println("  Post-build: " + file);
		return this;
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Runnable#run()
	 */
	@Override
	public void run()
	{
		try
		{
			call();
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}

}
