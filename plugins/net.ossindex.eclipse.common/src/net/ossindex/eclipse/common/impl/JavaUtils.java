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
package net.ossindex.eclipse.common.impl;

import java.io.File;
import java.util.LinkedList;
import java.util.List;

import net.ossindex.eclipse.common.IJavaUtils;

import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IPath;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;

/** Java platform specific utilities
 * 
 * @author Ken Duck
 *
 */
public class JavaUtils implements IJavaUtils
{
	private List<IPath> sourcePaths = new LinkedList<IPath>();
	private List<IPath> classPaths = new LinkedList<IPath>();
	
	public JavaUtils()
	{
	}
	
	/*
	 * (non-Javadoc)
	 * @see net.ossindex.eclipse.common.IJavaUtils#setProject(org.eclipse.core.resources.IProject)
	 */
	public void setProject(IProject project)
	{
		IJavaProject javaProject = JavaCore.create(project);
		loadSourcePaths(javaProject);
		loadClassPaths(javaProject);
	}
	

	/** Load the source paths for the project
	 * 
	 * @param javaProject
	 */
	private void loadSourcePaths(IJavaProject javaProject)
	{
		try
		{
			if(javaProject.exists())
			{
				IPackageFragmentRoot[] roots = javaProject.getAllPackageFragmentRoots();
				if(roots != null)
				{
					for (IPackageFragmentRoot root : roots)
					{
						switch(root.getKind())
						{
						case IPackageFragmentRoot.K_SOURCE:
						{
							IPath sourcePath = root.getPath();
							sourcePaths.add(sourcePath);
							break;
						}
						case IPackageFragmentRoot.K_BINARY:
						{
							// This gives you a bunch of jar files only.
							//						IPath classPath = root.getPath();
							//						classPaths.add(classPath);
							//						System.err.println("CLASS PATH: " + classPath);
							//						break;
						}
						}
					}
				}
			}
		}
		catch (JavaModelException e)
		{
			e.printStackTrace();
		}
	}

	/** Get the class paths for the project
	 * 
	 * @param javaProject
	 */
	private void loadClassPaths(IJavaProject javaProject)
	{
		try
		{
			if(javaProject.exists())
			{
				IClasspathEntry[] classpathEntries = javaProject.getRawClasspath();
				if(classpathEntries != null)
				{
					for (IClasspathEntry entry : classpathEntries)
					{
						// This technically works for finding source paths, but it
						// also gets a few other strange package type definitions
						// so to avoid confusion I use the above solution instead.
						//					IPath sourcePath = entry.getPath();
						//					sourcePaths.add(sourcePath);
						IPath classPath = entry.getOutputLocation();
						if(classPath != null)
						{
							classPaths.add(classPath);
						}
					}
				}
			}
		}
		catch (JavaModelException e)
		{
			e.printStackTrace();
		}
	}

	/**
	 * 
	 * @return
	 */
	public List<IPath> getSourcePaths()
	{
		return sourcePaths;
	}

	/**
	 * 
	 * @return
	 */
	public List<IPath> getClassPaths()
	{
		return classPaths;
	}
	

	/** Get the paths to source directories.
	 * 
	 * @param resource
	 * @return
	 */
	public String[] getSourcePaths(IResource resource)
	{
		IJavaProject javaProject = JavaCore.create(resource.getProject());
		List<String> results = new LinkedList<String>();
		try
		{
			if(javaProject.exists())
			{
				IPackageFragmentRoot[] roots = javaProject.getAllPackageFragmentRoots();
				if(roots != null)
				{
					for (IPackageFragmentRoot root : roots)
					{
						if(root.getKind() == IPackageFragmentRoot.K_SOURCE)
						{
							File file = root.getPath().toFile();
							results.add(file.getAbsolutePath());
						}
					}
				}
			}
		}
		catch (JavaModelException e)
		{
			e.printStackTrace();
		}

		return results.toArray(new String[results.size()]);
	}

	/** Get the class paths for the project
	 * 
	 * @param javaProject
	 */
	public String[] getClassPaths(IResource resource)
	{
		IProject project = resource.getProject();
		IJavaProject javaProject = JavaCore.create(project);
		List<String> results = new LinkedList<String>();
		try
		{
			if(javaProject.exists())
			{
				IClasspathEntry[] classpathEntries = javaProject.getRawClasspath();
				if(classpathEntries != null)
				{
					for (IClasspathEntry entry : classpathEntries)
					{
						// This technically works for finding source paths, but it
						// also gets a few other strange package type definitions
						// so to avoid confusion I use the above solution instead.
						//					IPath sourcePath = entry.getPath();
						//					sourcePaths.add(sourcePath);
						IPath classPath = entry.getOutputLocation();
						if(classPath != null)
						{
							IFolder ifile = project.getWorkspace().getRoot().getFolder(classPath);
							results.add(ifile.getLocation().toString());
						}
					}
				}
			}
		}
		catch (JavaModelException e)
		{
			e.printStackTrace();
		}
		return results.toArray(new String[results.size()]);
	}

	/*
	 * (non-Javadoc)
	 * @see net.ossindex.eclipse.common.IJavaUtils#getTargetPaths(org.eclipse.core.resources.IResource)
	 */
	@Override
	public String[] getTargetPaths(IResource resource)
	{
		IProject project = resource.getProject();
		IJavaProject javaProject = JavaCore.create(project);
		List<String> results = new LinkedList<String>();
		try
		{
			if(javaProject.exists())
			{
				IClasspathEntry[] classpathEntries = javaProject.getRawClasspath();
				if(classpathEntries != null)
				{
					for (IClasspathEntry entry : classpathEntries)
					{
						// This technically works for finding source paths, but it
						// also gets a few other strange package type definitions
						// so to avoid confusion I use the above solution instead.
						//					IPath sourcePath = entry.getPath();
						//					sourcePaths.add(sourcePath);
						IPath classPath = entry.getOutputLocation();
						if(classPath != null)
						{
							IFolder ifile = project.getWorkspace().getRoot().getFolder(classPath);
							results.add(ifile.getLocation().toString());
						}
					}
				}
			}
		}
		catch (JavaModelException e)
		{
			e.printStackTrace();
		}
		return results.toArray(new String[results.size()]);
	}

	/*
	 * (non-Javadoc)
	 * @see net.ossindex.eclipse.common.IJavaUtils#isAvailable()
	 */
	@Override
	public boolean isAvailable() {
		return true;
	}

	/*
	 * (non-Javadoc)
	 * @see net.ossindex.eclipse.common.IJavaUtils#getResource(java.lang.Object)
	 */
	@Override
	public Object getResource(Object resource) {
    	// If this is a C element then convert it to an IResource
    	if(resource instanceof IJavaProject)
    	{
    		resource = ((IJavaProject)resource).getResource();
    	}
		return resource;
	}

}
