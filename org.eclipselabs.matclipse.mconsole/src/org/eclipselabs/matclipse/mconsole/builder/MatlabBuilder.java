/*******************************************************************************
 * Copyright (c) 2006, 2011 Graz University of Technology,
 * Institute of Theoretical and Computational Physics (ITPCP) 
 *
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     David Camhy, Winfried Kernbichler, Georg Huhs (ITPCP) - 
 *        initial API and implementation
 *     Christopher Albert (ITPCP) - refactoring
 *******************************************************************************/
package org.eclipselabs.matclipse.mconsole.builder;

import java.util.List;
import java.util.Map;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.resources.IResourceDeltaVisitor;
import org.eclipse.core.resources.IResourceVisitor;
import org.eclipse.core.resources.IncrementalProjectBuilder;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipselabs.matclipse.mconsole.MConsolePlugin;
import org.eclipselabs.matclipse.mconsole.matlab.MOutputParser;
import org.eclipselabs.matclipse.mconsole.matlab.MatlabCommunicationException;
import org.eclipselabs.matclipse.mconsole.matlab.MatlabData;
import org.eclipselabs.matclipse.mconsole.matlab.MatlabNotStartedException;


public class MatlabBuilder extends IncrementalProjectBuilder {

	class SampleDeltaVisitor implements IResourceDeltaVisitor {
		/*
		 * (non-Javadoc)
		 * 
		 * @see
		 * org.eclipse.core.resources.IResourceDeltaVisitor#visit(org.eclipse
		 * .core.resources.IResourceDelta)
		 */
		public boolean visit(IResourceDelta delta) throws CoreException {
			IResource resource = delta.getResource();
			switch (delta.getKind()) {
			case IResourceDelta.ADDED:
				// handle added resource
				checkXML(resource);
				break;
			case IResourceDelta.REMOVED:
				// handle removed resource
				break;
			case IResourceDelta.CHANGED:
				// handle changed resource
				checkXML(resource);
				break;
			}
			// return true to continue visiting children.
			return true;
		}
	}

	class SampleResourceVisitor implements IResourceVisitor {
		public boolean visit(IResource resource) {
			checkXML(resource);
			// return true to continue visiting children.
			return true;
		}
	}

	public static final String BUILDER_ID = "org.eclipselabs.matclipse.matlabBuilder";

	private static final String MARKER_TYPE = "org.eclipselabs.matclipse.mconsole.matlabProblem";

	private void addMarker(IFile file, String message, int lineNumber,
			int severity) {
		try {
			IMarker marker = file.createMarker(MARKER_TYPE);
			marker.setAttribute(IMarker.MESSAGE, message);
			marker.setAttribute(IMarker.SEVERITY, severity);
			if (lineNumber == -1) {
				lineNumber = 1;
			}
			marker.setAttribute(IMarker.LINE_NUMBER, lineNumber);
		} catch (CoreException e) {
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.core.internal.events.InternalBuilder#build(int,
	 * java.util.Map, org.eclipse.core.runtime.IProgressMonitor)
	 */
	@Override
	@SuppressWarnings("rawtypes")
	protected IProject[] build(int kind, Map args, IProgressMonitor monitor)
			throws CoreException {
		if (kind == FULL_BUILD) {
			fullBuild(monitor);
		} else {
			IResourceDelta delta = getDelta(getProject());
			if (delta == null) {
				fullBuild(monitor);
			} else {
				incrementalBuild(delta, monitor);
			}
		}
		return null;
	}

	void checkXML(IResource resource) {
		if (resource instanceof IFile && resource.getName().endsWith(".m")) {
			IFile file = (IFile) resource;
			deleteMarkers(file);

			MOutputParser outputparser;
			try {
				outputparser = MConsolePlugin.getDefault().getMatlab()
						.MatlabMLint(file.getLocation().toOSString());
			} catch (MatlabCommunicationException e) {
				// TODO Auto-generated catch block
				return;
			} catch (MatlabNotStartedException e) {
				// TODO Auto-generated catch block
				return;
			}
			List<?> contents = null;
			try {
				contents = outputparser.getData().getData();
			} catch (NullPointerException npe) {
				return;
			}
			try {
				int nrerrors = contents.size() / 4;
				for (int i = 0; i < nrerrors; i++) {
					MatlabData content = (MatlabData) contents.get(i);
					String line = (String) content.getData().get(0);
					line = line.trim();

					content = (MatlabData) contents.get(i + nrerrors);
					String line2 = (String) content.getData().get(0);
					line2 = line2.trim();

					content = (MatlabData) contents.get(i + nrerrors * 2);
					String line3 = (String) content.getData().get(0);
					line3 = line3.trim();

					content = (MatlabData) contents.get(i + nrerrors * 3);
					String line4 = (String) content.getData().get(0);
					line4 = line4.trim();
					String[] error = new String[4];
					error[0] = line;
					error[1] = line2;
					error[2] = line3;
					error[3] = line4;
					Integer linenr = new Integer(line2);
					if (error[3].toString().indexOf("Inefficient") == -1
							&& error[3].toString().indexOf(
									"SyntaxErr:EmptyFile") == -1
							&& error[3].toString().indexOf(
									"SyntaxErr:ReadError") == -1
							&& error[3].toString().indexOf("NOPTS") == -1
							&& error[3].toString().indexOf("NASGU") == -1
							&& error[3].toString().indexOf("NBRAK") == -1
							&& error[3].toString().indexOf("CodingStyle:") == -1) {
						if (error[0].toString().indexOf(
								"Colon is apparently used to index") == -1
								&& error[0].toString().indexOf(
										"Use of brackets") == -1
								&& error[0].toString().indexOf(
										"Terminate statement with semicolon") == -1) {
							MatlabBuilder.this.addMarker(file, line,
									linenr.intValue(), IMarker.SEVERITY_ERROR);
						} else {
							MatlabBuilder.this.addMarker(file, line,
									linenr.intValue(), IMarker.SEVERITY_INFO);
						}
					} else {
						MatlabBuilder.this.addMarker(file, line,
								linenr.intValue(), IMarker.SEVERITY_WARNING);
					}

				}
			} catch (Throwable t) {

			}
		}
	}

	private void deleteMarkers(IFile file) {
		try {
			file.deleteMarkers(MARKER_TYPE, false, IResource.DEPTH_ZERO);
		} catch (CoreException ce) {
		}
	}

	protected void fullBuild(final IProgressMonitor monitor)
			throws CoreException {
		try {
			getProject().accept(new SampleResourceVisitor());
		} catch (CoreException e) {
		}
	}

	protected void incrementalBuild(IResourceDelta delta,
			IProgressMonitor monitor) throws CoreException {
		// the visitor does the work.
		delta.accept(new SampleDeltaVisitor());
	}
}
