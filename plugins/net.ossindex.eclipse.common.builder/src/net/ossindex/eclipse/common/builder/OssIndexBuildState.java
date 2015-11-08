package net.ossindex.eclipse.common.builder;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IWorkspaceDescription;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.ui.AbstractSourceProvider;
import org.eclipse.ui.ISources;

/** Properties that can be used in expressions for "enabledWhen" and "visibleWhen"
 * 
 * @author Ken Duck
 *
 */
public class OssIndexBuildState extends AbstractSourceProvider
{
	public final static String MY_STATE = "net.ossindex.eclipse.common.builder.buildAutomatically.active";
	public final static String ENABLED = "ENABLED";
	public final static String DISABLED = "DISABLED";
	private boolean initialized = false;
	
	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.ISourceProvider#dispose()
	 */
	@Override
	public void dispose()
	{
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.ISourceProvider#getCurrentState()
	 */
	@Override
	public Map<?,?> getCurrentState()
	{
		IWorkspace workspace= ResourcesPlugin.getWorkspace();
		IWorkspaceDescription desc= workspace.getDescription();
		boolean isAutoBuilding= desc.isAutoBuilding();
		Map<Object,Object> map = new HashMap<Object,Object>(1);
		String value = isAutoBuilding ? ENABLED : DISABLED;
		map.put(MY_STATE, value);
		return map;
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.ISourceProvider#getProvidedSourceNames()
	 */
	@Override
	public String[] getProvidedSourceNames()
	{
		return new String[] { MY_STATE };
	}

	/**
	 * Update the state of the variables.
	 */
	public void updateEnabled()
	{
		IWorkspace workspace= ResourcesPlugin.getWorkspace();
		IWorkspaceDescription desc= workspace.getDescription();
		boolean isAutoBuilding= desc.isAutoBuilding();
		String value = isAutoBuilding ? ENABLED : DISABLED;
	    fireSourceChanged(ISources.WORKBENCH, MY_STATE, value);
	}

}
