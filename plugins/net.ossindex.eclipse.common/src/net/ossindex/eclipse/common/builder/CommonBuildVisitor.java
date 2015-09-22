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

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.resources.IResourceDeltaVisitor;
import org.eclipse.core.resources.IResourceVisitor;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.QualifiedName;

/** Common code for the build visitors.
 * 
 * @author Ken Duck
 *
 */
public abstract class CommonBuildVisitor implements IResourceVisitor, IResourceDeltaVisitor
{
	/**
	 * Used to determine whether the builder has run on specific files or not.
	 */
	private QualifiedName timestampQualifier;

	/** Every builder should have a unique ID. This is used to determine whether the builder
	 * has run on specific files or not.
	 * 
	 * @param builderId
	 */
	public CommonBuildVisitor(String builderId)
	{
		timestampQualifier = new QualifiedName(builderId, ".TIMESTAMP");
	}
	
	/** Only rebuild dirty resources
	 * 
	 * @param resource
	 * @return
	 */
	protected boolean isDirty(IFile file)
	{
		try
		{
			String timeString = file.getPersistentProperty(timestampQualifier);
			if(timeString != null)
			{
				long timestamp = Long.parseLong(timeString);
				long modified = file.getLocation().toFile().lastModified();
				if(timestamp > modified)
				{
					return false;
				}
			}
		}
		catch (CoreException | NumberFormatException e)
		{
			e.printStackTrace();
		}
		// If there is an exception, try and do the build
		return true;
	}

	/** Mark the resource as "built" which means give it a timestamp. This allows
	 * us to identify dirty files.
	 * 
	 * @param resource
	 */
	protected void markBuilt(IResource resource)
	{
		try
		{
			resource.setPersistentProperty(timestampQualifier, Long.toString(System.currentTimeMillis()));
		}
		catch (CoreException e)
		{
			e.printStackTrace();
		}
	}
	
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
}
