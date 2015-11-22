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
package net.ossindex.eclipse.common.builder.service;

import java.util.HashSet;
import java.util.Set;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;

/** Implementation of the ICommonBuildService
 * 
 * @author Ken Duck
 *
 */
public class CommonBuildServiceImpl implements ICommonBuildService
{
	private static CommonBuildServiceImpl mInstance;

	/**
	 * Registered natures
	 */
	private Set<String> natures = new HashSet<String>();

	/**
	 * Registered builders
	 */
	private Set<String> builders = new HashSet<String>();

	private CommonBuildServiceImpl()
	{

	}

	/** Get the singleton instance
	 * 
	 * @return
	 */
	public synchronized static CommonBuildServiceImpl getInstance()
	{
		if(mInstance == null) mInstance = new CommonBuildServiceImpl();
		return mInstance;
	}

	/*
	 * (non-Javadoc)
	 * @see net.ossindex.eclipse.common.builder.service.ICommonBuildService#registerNature(java.lang.String)
	 */
	@Override
	public void registerNature(String natureId)
	{
		natures.add(natureId);
	}

	/*
	 * (non-Javadoc)
	 * @see net.ossindex.eclipse.common.builder.service.ICommonBuildService#registerBuilder(java.lang.String)
	 */
	@Override
	public void registerBuilder(String builderId)
	{
		builders.add(builderId);
	}

	/*
	 * (non-Javadoc)
	 * @see net.ossindex.eclipse.common.builder.service.ICommonBuildService#containsBuilder(java.lang.String)
	 */
	@Override
	public boolean containsBuilder(String builderId)
	{
		return builders.contains(builderId);
	}

	/*
	 * (non-Javadoc)
	 * @see net.ossindex.eclipse.common.builder.service.ICommonBuildService#supportsProject(org.eclipse.core.resources.IProject)
	 */
	@Override
	public boolean supportsProject(IProject project)
	{
		if(project != null)
		{
			// Check all registered natures against the project's natures
			for(String nature: natures)
			{
				try
				{
					if(project.hasNature(nature)) return true;
				}
				catch (CoreException e)
				{
					e.printStackTrace();
				}
			}
		}
		return false;
	}
}
