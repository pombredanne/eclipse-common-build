/**
 * Copyright (c) 2015 KDM Analytics, Inc. All rights reserved.
 */
package net.ossindex.eclipse.common.builder;

import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;

/** Interface to be implemented by builders that can run recursively
 * on entire directories. Particularly interesting for speeding up
 * Java builds.
 * 
 * @author Ken Duck
 *
 */
public interface IBatchBuilder
{
	/**
	 * Build everything in a project
	 */
	public void buildAll(IProject project);
	
	/** Build the recursive contents of the specified directory
	 * 
	 * @param folder
	 */
	public void buildDir(IFolder folder);

	/** Mark all applicable files as "built". This is used to speed up
	 * incremental builds by indicating when the files were last built
	 * so we can compare that time with the file modification time.
	 * 
	 * This runs recursively to mark all applicable files.
	 * 
	 * @param project
	 */
	public void markAllBuilt(IResource resource);

	/** Identify if there are any interesting files that are dirty.
	 * 
	 * @param project
	 * @return
	 */
	public boolean areFilesDirty(IResource resource);

}
