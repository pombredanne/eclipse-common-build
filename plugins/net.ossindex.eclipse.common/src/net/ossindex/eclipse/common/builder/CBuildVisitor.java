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

import org.eclipse.cdt.core.model.CModelException;
import org.eclipse.cdt.core.model.CoreModel;
import org.eclipse.cdt.core.model.IIncludeEntry;
import org.eclipse.cdt.core.model.IMacroEntry;
import org.eclipse.cdt.core.settings.model.ICConfigurationDescription;
import org.eclipse.cdt.core.settings.model.ICFolderDescription;
import org.eclipse.cdt.core.settings.model.ICLanguageSetting;
import org.eclipse.cdt.core.settings.model.ICLanguageSettingEntry;
import org.eclipse.cdt.core.settings.model.ICProjectDescription;
import org.eclipse.cdt.core.settings.model.ICSettingEntry;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.resources.IResourceDeltaVisitor;
import org.eclipse.core.resources.IResourceVisitor;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
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
		if(isCppCompilationUnit(resource))
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

	/** Return true if this is a C/C++ compilation unit
	 * 
	 * @param resource
	 * @return
	 */
	private boolean isCppCompilationUnit(IResource resource)
	{
		if(resource instanceof IFile)
		{
			IProject project = resource.getProject();
			String name = resource.getName();
			return CoreModel.isValidCXXSourceUnitName(project, name) ||
					CoreModel.isValidCSourceUnitName(project, name);
		}
		return false;
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

	/**
	 * Get the settings for the provided resource
	 */
	protected String[] getIncludePaths(IResource resource)
	{
		IPath path = resource.getLocation();
		List<String> results = new LinkedList<String>();
		
		try
		{
			IIncludeEntry[] includes = CoreModel.getIncludeEntries(path);
			if(includes != null)
			{
				for (IIncludeEntry include : includes)
				{
					results.add(include.getFullIncludePath().toString());
				}
			}
		}
		catch (CModelException e)
		{
			e.printStackTrace();
		}
		
		return results.toArray(new String[results.size()]);
	}

	/**
	 * Get the settings for the provided resource
	 */
	protected String[] getMacros(IResource resource)
	{
		IPath path = resource.getLocation();
		List<String> results = new LinkedList<String>();
		
		try
		{
			IMacroEntry[] macros = CoreModel.getMacroEntries(path);
			if(macros != null)
			{
				for (IMacroEntry macro : macros)
				{
					String name = macro.getMacroName();
					String value = macro.getMacroValue();
					if(value != null && !value.isEmpty())
					{
						results.add(name + "=" + value);
					}
					else
					{
						results.add(name);
					}
				}
			}
		}
		catch (CModelException e)
		{
			e.printStackTrace();
		}
		return results.toArray(new String[results.size()]);
	}

	/** Get the project level include settings.
	 * 
	 * This code is not currently used and does not actually do anything besides
	 * print some data to stderr.
	 * 
	 * It also contains some roughed in code for setting project wide includes.
	 * 
	 * @param project
	 */
	protected void getProjectSettings(IProject project)
	{
		System.err.println("SETTINGS FOR " + project);
		ICProjectDescription projectDescription = CoreModel.getDefault().getProjectDescription(project, true);
		ICConfigurationDescription configDecriptions[] = projectDescription.getConfigurations();
		
		for (ICConfigurationDescription configDescription : configDecriptions)
		{
			ICFolderDescription projectRoot = configDescription.getRootFolderDescription();
			ICLanguageSetting[] settings = projectRoot.getLanguageSettings();
			for (ICLanguageSetting setting : settings)
			{
				List<ICLanguageSettingEntry> includes = setting.getSettingEntriesList(ICSettingEntry.INCLUDE_PATH);
				for (ICLanguageSettingEntry include : includes)
				{
					System.err.println("  -I " + include);
				}
				
				List<ICLanguageSettingEntry> macros = setting.getSettingEntriesList(ICSettingEntry.MACRO);
				for (ICLanguageSettingEntry macro : macros)
				{
					System.err.println("  -D " + macro);
				}
				
//				includes.addAll(setting.getSettingEntriesList(ICSettingEntry.INCLUDE_PATH));
//				includes.add(new CIncludePathEntry("/my/local/include/path", ICSettingEntry.LOCAL));
//				setting.setSettingEntries(ICSettingEntry.INCLUDE_PATH, includes);
			}
		}
//		CoreModel.getDefault().setProjectDescription(project, projectDescription);
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
