package org.eclipselabs.matclipse.mconsole.matlab.impl;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.action.Action;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.navigator.CommonNavigator;
import org.eclipselabs.matclipse.mconsole.MConsolePlugin;
import org.eclipselabs.matclipse.mconsole.internal.ui.preferences.IPreferenceConstants;
import org.eclipselabs.matclipse.mconsole.matlab.MOutputParser;
import org.eclipselabs.matclipse.mconsole.matlab.MatlabCommunicationException;
import org.eclipselabs.matclipse.mconsole.matlab.MatlabData;
import org.eclipselabs.matclipse.mconsole.matlab.MatlabInterpreter;
import org.eclipselabs.matclipse.mconsole.matlab.MatlabNotStartedException;
import org.eclipselabs.matclipse.mconsole.views.MatlabConsoleView;
import org.eclipselabs.matclipse.mconsole.views.MatlabWorkspaceView;
import org.eclipselabs.matclipse.util.MatclipseUtilPlugin;


public abstract class XMLMatlabInterpreter implements MatlabInterpreter {
	protected boolean logging;
	protected boolean matlabAvailable = false;
	String pid = null;

	public void setLogging(boolean logging) {
		this.logging = logging;
	}

	public void changeMatlabDirectoryToResource(IResource resource) {
		String wspath = ResourcesPlugin.getWorkspace().getRoot().getLocation()
				.toString();
		String path = "";
		MConsolePlugin.getDefault().waitForAutoBuild();
		if (resource instanceof IFile) {
			path = wspath + resource.getParent().getFullPath().toString();
		} else {
			path = wspath + resource.getFullPath().toString();
		}
		try {
			if (!MConsolePlugin.getDefault().getMatlab().getMatlabPwd()
					.equals(path))
				MConsolePlugin.getDefault().getMatlab()
						.changeMatlabDirectory(path, false);
		} catch (Exception e) {
			// Activator.getDefault().errorDialog("Test
			// Error", e);
		}
	}

	public void changeMatlabDirectoryToPath(String path) {
		MConsolePlugin.getDefault().waitForAutoBuild();
		try {
			if (!MConsolePlugin.getDefault().getMatlab().getMatlabPwd()
					.equals(path))
				MConsolePlugin.getDefault().getMatlab()
						.changeMatlabDirectory(path, false);
		} catch (Exception e) {
			// TODO: Unhandled Exception
			System.err.println(e);
		}
	}

	protected void outputConsoleError(final String message, Throwable t) {
		MatclipseUtilPlugin.getDefault().logError(message, t);
		try {
			MConsolePlugin
					.getDefault()
					.getWorkbench()
					.getActiveWorkbenchWindow()
					.getActivePage()
					.showView(
							"org.eclipselabs.matclipse.mconsole.views.MatlabConsoleView");
		} catch (Exception e) {

		}
		MConsolePlugin.getDefault().getWorkbench().getDisplay()
				.asyncExec(new Runnable() {
					public void run() {
						MatlabConsoleView.getDefault().outputError(message);
					}
				});

	}

	public void outputDirectoryChangeError(Throwable t) {
		outputConsoleError("Matlab is busy. Directory change not possible..", t);

	}

	public void outputBusyError(Throwable t) {
		outputConsoleError("Matlab is busy. Try again later..", t);

	}

	public void outputNotStartedError(Throwable t) {
		outputConsoleError("Matlab not started.", t);

	}

	protected void outputMemoryError(Throwable t) {
		outputConsoleError(
				"Java Virtual Machine out of memory. Please change your memory settings and restart the Workbench",
				t);

	}

	protected void outputCommunicationError(Throwable t) {
		outputConsoleError("Matlab Communication Error.", t);
	}

	public MOutputParser getMatlabVarXMLStructure(String var)
			throws MatlabNotStartedException, MatlabCommunicationException {

		String output = "";
		String outputtext = eval("output_xml(" + var + ",[],[],1);");

		outputtext = outputtext + "</matlab>\n";

		MOutputParser parser = new MOutputParser(true);
		try {
			output = outputtext.substring(outputtext.indexOf("<matlab"),
					outputtext.indexOf("</matlab") + 9);
			parser = new MOutputParser(output);
			parser.parse();

		} catch (Throwable t) {
			return parser;

		}

		return parser;

	}

	public MOutputParser getMatlabVarXML(String var)
			throws MatlabNotStartedException, MatlabCommunicationException {
		String output = "";
		String outputtext = eval("output_xml(" + var + ")");

		outputtext = outputtext + "</matlab>\n";

		MOutputParser parser = new MOutputParser();
		try {
			output = outputtext.substring(outputtext.indexOf("<matlab"),
					outputtext.indexOf("</matlab") + 9);
			parser = new MOutputParser(output);
			parser.parse();

		} catch (Throwable t) {
			return parser;

		}

		return parser;
	}

	public ArrayList<String> getGlobalVarNames()
			throws MatlabCommunicationException, MatlabNotStartedException {
		ArrayList<String> linelist = new ArrayList<String>();

		evalNoOutput("matclipse_who=who('global');");

		MOutputParser outputparser = getMatlabVarXML("matclipse_who");
		List<?> contents = outputparser.getData().getData();
		for (int i = 0; i < contents.size(); i++) {

			MatlabData content = (MatlabData) contents.get(i);
			String line = (String) content.getData().get(0);
			linelist.add(line.trim());

		}

		evalNoOutput("clear matclipse_who");

		return linelist;
	}

	public ArrayList<String> getLocalVarNames()
			throws MatlabCommunicationException, MatlabNotStartedException {

		ArrayList<String> linelist = new ArrayList<String>();
		evalNoOutput("matclipse_who=who;");

		MOutputParser outputparser = getMatlabVarXML("matclipse_who");
		List<?> contents = outputparser.getData().getData();

		for (int i = 0; i < contents.size(); i++) {

			MatlabData content = (MatlabData) contents.get(i);
			String line = (String) content.getData().get(0);
			linelist.add(line.trim());

		}

		evalNoOutput("clear matclipse_who;");

		return linelist;
	}

	public ArrayList<MOutputParser> getGlobalVars()
			throws MatlabCommunicationException, MatlabNotStartedException {
		ArrayList<MOutputParser> GlobalMatlabVars = new ArrayList<MOutputParser>();
		ArrayList<String> globalvars = getGlobalVarNames();
		for (int i = 0; i < globalvars.size(); i++) {
			String varname = globalvars.get(i);

			MOutputParser outputparser = getMatlabVarXMLStructure(varname);
			outputparser.parse();

			GlobalMatlabVars.add(outputparser);
		}
		return GlobalMatlabVars;
	}

	public ArrayList<MOutputParser> getLocalVars()
			throws MatlabCommunicationException, MatlabNotStartedException {
		ArrayList<MOutputParser> localMatlabVars = new ArrayList<MOutputParser>();
		localMatlabVars.clear();
		ArrayList<String> vars = getLocalVarNames();
		for (int i = 0; i < vars.size(); i++) {
			String varname = vars.get(i);
			MOutputParser outputparser = getMatlabVarXMLStructure(varname);
			outputparser.parse();
			localMatlabVars.add(outputparser);
		}
		return localMatlabVars;
	}

	public String getMatlabPwd() throws MatlabCommunicationException,
			MatlabNotStartedException {
		StringBuilder dir = new StringBuilder();
		evalNoOutput("matclipse_pwd=pwd;");

		MOutputParser outputparser = getMatlabVarXML("matclipse_pwd");
		List<?> content = outputparser.getData().getData();

		for (int i = 0; i < content.size(); i++) {
			dir.append((String) content.get(i));
		}

		evalNoOutput("clear matclipse_pwd;");

		return dir.toString().trim();
	}

	public void changeMatlabDirectory(String directory, boolean focus)
			throws MatlabCommunicationException, MatlabNotStartedException {
		changeMatlabDirectory(
				directory,
				MConsolePlugin.getDefault().getPreferenceStore()
						.getBoolean(IPreferenceConstants.P_CDFEEDBACK), focus);

	}

	public void changeMatlabDirectory(String directory, boolean feedback,
			boolean focus) throws MatlabCommunicationException,
			MatlabNotStartedException {
		if (feedback) {
			MatlabConsoleView.getDefault().run("cd '" + directory + "'", null,
					false, focus);
		} else {
			eval("cd '" + directory + "'", true, false);
		}

	}

	public void changeMatlabDirectoryNoOutput(String directory)
			throws MatlabCommunicationException, MatlabNotStartedException {
		evalNoOutput("cd '" + directory + "'");

	}

	public void evalNoOutput(String command)
			throws MatlabCommunicationException, MatlabNotStartedException {
		if (MConsolePlugin.getDefault().isMatlabInitialized()) {
			if (matlabAvailable) {
				try {
					matlabAvailable = false;
					send(command);
					getMatlabOutput();
					matlabAvailable = true;

				} catch (java.lang.OutOfMemoryError e) {
					stop();
					outputMemoryError(e);

				} catch (IOException ioe) {
					outputNotStartedError(ioe);
					throw new MatlabNotStartedException();
				} catch (NullPointerException npe) {
					outputNotStartedError(npe);
					throw new MatlabNotStartedException();
				} catch (Throwable t) {
					this.outputCommunicationError(t);
					throw new MatlabCommunicationException();
				}
			}
		} else
			outputNotStartedError(new Throwable());
	}

	public String eval(String command) throws MatlabCommunicationException,
			MatlabNotStartedException {
		return eval(command, false, false, false);
	}

	public String eval(String command, boolean updateDirectory, boolean unsafe)
			throws MatlabCommunicationException, MatlabNotStartedException {
		return eval(command, updateDirectory, unsafe, true);
	}

	public String eval(String command, boolean updateDirectory, boolean unsafe,
			boolean cleanFile) throws MatlabNotStartedException,
			MatlabCommunicationException {
		if (MConsolePlugin.getDefault().isMatlabInitialized()) {
			if (matlabAvailable) {

				try {
					matlabAvailable = false;

					String outputtext_cleaned = "";
					if (unsafe)
						sendUnsafe(command);
					else
						send(command);
					String[] outputtext = getMatlabOutput();
					StringBuffer buffer = new StringBuffer();
					for (int i = 0; i < outputtext.length; i++) {
						buffer.append(outputtext[i]);
						buffer.append('\n');
					}

					try {

						outputtext_cleaned = buffer.toString().substring(
								buffer.toString().indexOf(">> ") + 3);
						// Removing only if there's more than one
						// if
						// (outputtext_cleaned.indexOf("\n")!=outputtext_cleaned.lastIndexOf("\n"))
						// outputtext_cleaned=outputtext_cleaned.replaceFirst("\n",
						// "");

					} catch (Exception e) {

					}
					if (unsafe) {
						try {
							outputtext_cleaned = outputtext_cleaned
									.substring(
											0,
											outputtext_cleaned
													.indexOf("Error in ==> matexec"));

						} catch (Exception e) {

						}
						if (cleanFile) {
							try {
								if (outputtext_cleaned.indexOf("Error: File:") != -1) {
									String part1 = outputtext_cleaned
											.substring(0, outputtext_cleaned
													.indexOf("Error: File:"));
									String part2 = outputtext_cleaned
											.substring(
													outputtext_cleaned
															.indexOf("Line"),
													outputtext_cleaned.indexOf(System
															.getProperty("line.separator")));
									String part3 = " "
											+ outputtext_cleaned
													.substring(outputtext_cleaned.indexOf(System
															.getProperty("line.separator")) + 1);
									outputtext_cleaned = part1 + part2 + part3;
								}
							} catch (Exception e) {
								e.printStackTrace();
							}
						}
					}
					matlabAvailable = true;
					if (updateDirectory) {
						try {
							// final String matlabpwd = getMatlabPwd();
							MConsolePlugin.getDefault().getWorkbench()
									.getDisplay().syncExec(new Runnable() {
										public void run() {
											try {
												MConsolePlugin
														.getDefault()
														.getWorkbench()
														.getActiveWorkbenchWindow()
														.getActivePage()
														.showView(
																"org.eclipselabs.matclipse.mconsole.views.MatlabConsoleView");
											} catch (Exception e) {

											}
											MatlabConsoleView.getDefault()
													.addDirectoryHistoryEntry();
											MatlabConsoleView.getDefault()
													.setFocustoConsole();
										}
									});

						} catch (Throwable t) {
							MatclipseUtilPlugin
									.getDefault()
									.errorDialog(
											"Exception when adding to Directory History",
											t);
						}
					}

					return outputtext_cleaned;
				} catch (java.lang.OutOfMemoryError e) {
					stop();
					outputMemoryError(e);

					return null;
				} catch (IOException ioe) {
					outputNotStartedError(ioe);
					throw new MatlabNotStartedException();
				} catch (NullPointerException npe) {
					outputNotStartedError(npe);
					throw new MatlabNotStartedException();
				} catch (Throwable t) {
					this.outputCommunicationError(t);
					throw new MatlabCommunicationException();
				}
			} else
				return null;
		} else {
			outputNotStartedError(new Throwable());
			return null;

		}
	}

	public boolean isMatlabAvailable() {
		return matlabAvailable;
	}

	public MOutputParser MatlabMLint(String filename)
			throws MatlabCommunicationException, MatlabNotStartedException {
		if (MConsolePlugin.getDefault().isMatlabInitialized()) {
			evalNoOutput("matclipse_mlint=mlint('" + filename
					+ "','-fullpath','-id');");

			MOutputParser outputparser = getMatlabVarXML("matclipse_mlint");

			evalNoOutput("clear matclipse_mlint;");

			return outputparser;
		} else {
			return null;
		}
	}

	protected Job startMatlabJob = new Job("Starting Matlab") {
		protected Action getCompletedAction() {
			return new Action("Matlab started") {
				@Override
				public void run() {
					MConsolePlugin.getDefault().setMatlabInitialized(true);
					matlabAvailable = true;
					String pwd = System.getProperty("user.dir");
					try {
						evalNoOutput("addpath('"
								+ MConsolePlugin.getMatlabLibPath() + "');");

						evalNoOutput("cd '" + pwd + "';");
						String rootpath = ResourcesPlugin.getWorkspace()
								.getRoot().getLocation().toString();
						evalNoOutput("addpath('" + rootpath + "');");
						evalNoOutput("cd " + rootpath + ";");
						// TODO: Throw Exception if no pid could be found
						try {
							// send("!echo $$"); Does not work, MATLAB creates
							// new shell
							send("!perl -e 'print getppid().\"\\n\";'"); // pid
						} catch (IOException e) {

							e.printStackTrace();
						}
						String mhelperpid = "";
						try {
							mhelperpid = getMatlabOutput()[0];
							// if
							// (MConsolePlugin.getDefault().getSystem().equals(
							// "linux")) mhelperpid=mhelperpid.split(" ")[1];
							if (mhelperpid.startsWith(">> "))
								mhelperpid = mhelperpid.split(" ")[1];

						} catch (Exception e) {

							e.printStackTrace();
						}
						try {

							String cmd = "!ps -o ppid " + mhelperpid;
							if(Platform.inDebugMode())
								System.out.println(cmd);
							// send("!echo $$"); Does not work, MATLAB creates
							// new shell
							send(cmd); // pid of MATLAB
						} catch (Exception e) {

							e.printStackTrace();
						}
						try {
							String[] pidOutput = getMatlabOutput();
							if(Platform.inDebugMode())
								System.out.println("Length: " + pidOutput.length);
							pid = pidOutput[1].trim().split(" ")[0];

						} catch (Exception e) {

							e.printStackTrace();
						}

						if (MConsolePlugin.getSystem().equals("linux"))
							evalNoOutput("opengl software;");
					} catch (MatlabNotStartedException e) {

					} catch (MatlabCommunicationException e) {

					}

					try {
						MatlabConsoleView.getDefault().setEnabled(true, true);
						MatlabConsoleView.getDefault()
								.getCommandLineResultsText().setText("");
						MatlabConsoleView.getDefault()
								.addDirectoryHistoryEntry();
					} catch (Exception e) {
					}

					try {
						IViewPart viewPart = PlatformUI
								.getWorkbench()
								.getActiveWorkbenchWindow()
								.getActivePage()
								.findView(
										"org.eclipselabs.matclipse.mconsole.views.MatlabProjectNavigator");
						// MatlabProjectNavigator matlabProjectNavigator =
						// MatlabProjectNavigator.getDefault();
						CommonNavigator matlabProjectNavigator = (CommonNavigator) viewPart;

						if (matlabProjectNavigator.getCommonViewer()
								.getSelection() != null)
							matlabProjectNavigator.getCommonViewer()
									.setSelection(
											matlabProjectNavigator
													.getCommonViewer()
													.getSelection());
					} catch (Exception e) {
					}
					try {
						for (int i = 0; i < MConsolePlugin.getDefault()
								.getWorkbench().getActiveWorkbenchWindow()
								.getPages().length; i++) {
							MConsolePlugin.getDefault().getWorkbench()
									.getActiveWorkbenchWindow().getPages()[i]
									.addPartListener(MatlabConsoleView
											.getDefault());
						}
					} catch (Exception e) {
					}
					try {
						MConsolePlugin
								.getDefault()
								.getWorkbench()
								.getActiveWorkbenchWindow()
								.addPerspectiveListener(
										(MatlabConsoleView.getDefault()));

						MatlabWorkspaceView.getDefault().refresh();
					} catch (Exception e) {
					}

				}
			};
		}

		protected void showResults() {
			try {
				MConsolePlugin.getDefault().getWorkbench().getDisplay()
						.asyncExec(new Runnable() {
							public void run() {
								getCompletedAction().run();
							}
						});
			} catch (NullPointerException npe) {

			}

		}

		@Override
		protected IStatus run(IProgressMonitor monitor) {
			new Thread() {
				@Override
				public void run() {
					MConsolePlugin.getDefault().setMatlabInitialized(false);
					try {
						MatlabConsoleView.getDefault().setEnabled(false, true);

					} catch (Exception e) {
					}
					if (!startLocal())
						return;
					showResults();
				}
			}.start();
			return Status.OK_STATUS;
		}
	};

	public void start() throws CoreException {
		stop();

		startMatlabJob.setPriority(Job.LONG);
		startMatlabJob.setUser(true);
		startMatlabJob.schedule();
	}

	protected abstract void send(String command) throws IOException;

	protected abstract void sendUnsafe(String command);

	protected abstract boolean startLocal();

	protected abstract String[] getMatlabOutput() throws IOException;
}
