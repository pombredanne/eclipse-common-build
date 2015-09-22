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

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResourceVisitor;
import org.eclipse.core.runtime.CoreException;

/** This class manages the running of build visitors concurrently. It can run
 * in two modes (*):
 * 
 *   1. Blocking: This blocks the build process until completion
 *   2. Non-blocking: Runs these builders as a separate non-blocking job
 * 
 *   (*) eventually. Currently only non-blocking is supported.
 * 
 * @author Ken Duck
 *
 */
public class ConcurrentBuildManager implements Iterable<Future<ConcurrentBuildJob>>
{
//	private int maxJobs = Runtime.getRuntime().availableProcessors() * 2;
	private int maxJobs = 2;

	private IResourceVisitor visitor;
	private ExecutorService executor;

	/**
	 * Current set of jobs to execute
	 */
	private List<Future<ConcurrentBuildJob>> jobs;
	private List<String> names;

	/**
	 * True while the executor has not been shut down
	 */
	private boolean isRunning = true;

	public ConcurrentBuildManager(IResourceVisitor visitor)
	{
		this.visitor = visitor;
		executor = Executors.newFixedThreadPool(maxJobs);
		jobs = new LinkedList<Future<ConcurrentBuildJob>>();
		names = new LinkedList<String>();
	}

	/** Request a new file be visited. This may block depending on the status of
	 * the job pool and whether we are running in blocking mode or not.
	 * 
	 * @param file
	 * @throws CoreException 
	 */
	public void visit(IFile file) throws CoreException
	{
		Callable<ConcurrentBuildJob> worker = new ConcurrentBuildJob(visitor, file);
		Future<ConcurrentBuildJob> job = executor.submit(worker);
		jobs.add(job);
		names.add(file.getName());
	}

	/**
	 * 
	 * @throws InterruptedException
	 * @throws ExecutionException
	 */
	public void runall() throws InterruptedException, ExecutionException
	{
		for(Future<ConcurrentBuildJob> future: jobs)
		{
			@SuppressWarnings("unused")
			ConcurrentBuildJob job = future.get();
		}
	}

	/**
	 * 
	 * @throws InterruptedException
	 * @throws ExecutionException
	 */
	public void shutdown() throws InterruptedException, ExecutionException
	{
		if(isRunning)
		{
			isRunning = false;
			executor.shutdown();
		}
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Iterable#iterator()
	 */
	@Override
	public Iterator<Future<ConcurrentBuildJob>> iterator()
	{
		return jobs.iterator();
	}

	/** Get the number of jobs
	 * 
	 * @return
	 */
	public int getSize()
	{
		return jobs.size();
	}

	/** Get the name for the specified job index
	 * 
	 * @param index
	 * @return
	 */
	public String getName(int index)
	{
		if(index < names.size()) return names.get(index);
		return "";
	}

	public void shutdownNow()
	{
		if(isRunning)
		{
			isRunning = false;
			executor.shutdownNow();
		}
	}

	public boolean isRunning()
	{
		return isRunning;
	}
}
