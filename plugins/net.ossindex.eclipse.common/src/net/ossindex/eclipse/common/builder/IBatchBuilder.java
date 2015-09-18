/**
 * Copyright (c) 2015 KDM Analytics, Inc. All rights reserved.
 */
package net.ossindex.eclipse.common.builder;

import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;

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

}
