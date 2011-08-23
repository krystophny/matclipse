package org.eclipselabs.matclipse.mconsole.matlab.impl;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.ArrayList;

import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipselabs.matclipse.mconsole.MConsolePlugin;
import org.eclipselabs.matclipse.mconsole.internal.ui.preferences.IPreferenceConstants;
import org.eclipselabs.matclipse.mconsole.matlab.MatlabCommunicationException;
import org.eclipselabs.matclipse.mconsole.matlab.MatlabNotStartedException;
import org.eclipselabs.matclipse.mconsole.views.MatlabConsoleDebugView;
import org.eclipselabs.matclipse.mconsole.views.MatlabConsoleView;
import org.eclipselabs.matclipse.mconsole.views.MatlabWorkspaceView;


public class UnixMatlabInterpreter extends XMLMatlabInterpreter {

	private Process matlabProcess;
	private InputStream matlabInputStream;
	private OutputStream matlabOutputStream;

	@Override
	protected boolean startLocal() {
		String mlp = MConsolePlugin.getDefault().getPreferenceStore()
				.getString(IPreferenceConstants.P_MATLABPATH);
		File matlabPath = new File(mlp);
		if (!matlabPath.isDirectory()) {
			outputConsoleError(
					"Path to Matlab is no directory. Please take a look at the Preferences",
					new Throwable());
			return false;
		}

		String matlabStartString = matlabPath.toString() + File.separator
				+ "matlab";
		try {
			if (MConsolePlugin.getSystem().equals("macosx")
					|| MConsolePlugin.getSystem().equals("mac os x")) {
				matlabProcess = java.lang.Runtime.getRuntime().exec(
						matlabStartString + " -nodesktop -nosplash 2>&1",
						new String[] { "TERM=xterm-color", "DISPLAY=:0.0" });
			} else
				matlabProcess = java.lang.Runtime.getRuntime().exec(
						matlabStartString + " -nodesktop -nosplash 2>&1");
			matlabInputStream = matlabProcess.getInputStream();
			matlabOutputStream = matlabProcess.getOutputStream();
		} catch (Exception ce) {
			outputNotStartedError(ce);
			return false;
		}
		return true;
	}

	public void evalInConsole(String command, boolean addToCommandHistory)
			throws MatlabCommunicationException, MatlabNotStartedException {
		MConsolePlugin.getDefault().getWorkbench().getDisplay()
				.syncExec(new Runnable() {
					public void run() {
						try {
							MConsolePlugin
									.getDefault()
									.getWorkbench()
									.getActiveWorkbenchWindow()
									.getActivePage()
									.showView(
											MatlabConsoleView.VIEW_ID);

						} catch (Exception e) {

						}
					}
				});
		MatlabConsoleView.getDefault().run(command, null, addToCommandHistory,
				true);

	}

	public void sendBreak() throws IOException {
		String cmd = "kill -2 " + pid;
		Runtime.getRuntime().exec(cmd);

	}

	@Override
	protected void send(String command) throws IOException {

		if (logging)
			try {
				MatlabConsoleDebugView.getDefault().log(
						"\nINTERNAL>>" + command + "\n");
				MatlabConsoleDebugView.getDefault().jumpToEnd();
			} catch (Exception e) {
			}

		OutputStreamWriter osw = new OutputStreamWriter(matlabOutputStream);
		BufferedWriter bw = new BufferedWriter(osw);

		bw.write(command);

		bw.newLine();

		bw.write("disp('###########MatclipseEnd###########')");
		bw.newLine();
		bw.flush();

	}

	@Override
	protected void sendUnsafe(String command) {
		if (MConsolePlugin.getDefault().isMatlabInitialized()) {
			if (logging)
				try {
					MatlabConsoleDebugView.getDefault().log(
							"\nCONSOLE>>" + command + "\n");
				} catch (Exception e) {

				}

			try {

				String rootpath = ResourcesPlugin.getWorkspace().getRoot()
						.getLocation().toString();

				BufferedWriter out = new BufferedWriter(new FileWriter(rootpath
						+ "/" + "matexec.m"));

				out.write(command);
				out.close();

				OutputStreamWriter osw = new OutputStreamWriter(
						matlabOutputStream);
				BufferedWriter bw = new BufferedWriter(osw);

				bw.write("rehash;");
				bw.newLine();
				bw.write("matexec;");
				bw.newLine();
				bw.write("disp('###########MatclipseEnd###########');");
				bw.newLine();
				bw.flush();

			} catch (IOException ioe) {
				this.outputNotStartedError(ioe);
			} catch (NullPointerException npe) {
				this.outputNotStartedError(npe);
			} catch (Throwable t) {
				this.outputCommunicationError(t);
			}
		} else
			outputNotStartedError(new Throwable());

	}

	@Override
	protected String[] getMatlabOutput() throws IOException {
		InputStreamReader MatlabInputStreamReader;
		BufferedReader MatlabBufferedReader = null;

		MatlabInputStreamReader = new InputStreamReader(matlabInputStream);
		MatlabBufferedReader = new BufferedReader(MatlabInputStreamReader);

		ArrayList<String> linelist = new ArrayList<String>();

		while (true) {
			String line = "";
			try {
				line = MatlabBufferedReader.readLine();
			} catch (java.lang.OutOfMemoryError e) {
				linelist.clear();
				outputMemoryError(e);
				break;
			}
			if (line.indexOf("###########MatclipseEnd###########") == -1) {
				if (logging)
					try {
						MatlabConsoleDebugView.getDefault().log(line + "\n");
					} catch (Exception e) {

					}
				try {
					linelist.add(line);
				} catch (java.lang.OutOfMemoryError e) {
					linelist.clear();
					outputMemoryError(e);
					break;
				}
			}

			else
				break;
		}
		int size = linelist.size();
		String[] result = linelist.toArray(new String[size]);
		return result;

	}

	public void stop() {
		try {

			String cmd = "kill -9 " + pid;
			Runtime.getRuntime().exec(cmd);
			matlabProcess.getInputStream().close();
			matlabProcess.getOutputStream().close();
			matlabProcess.getErrorStream().close();
			matlabProcess.destroy();

		} catch (Exception e) {

		}
		matlabAvailable = false;
		MConsolePlugin.getDefault().setMatlabInitialized(false);
		try {
			MConsolePlugin.getDefault().getWorkbench().getDisplay()
					.syncExec(new Runnable() {
						public void run() {
							try {
								MatlabConsoleView.getDefault().setEnabled(
										false, true);
								MatlabConsoleView.getDefault()
										.enableMatlabStartAction();
							} catch (Exception e) {
							}
						}
					});
		} catch (Exception e) {

		}

		try {
			MConsolePlugin.getDefault().getWorkbench().getDisplay()
					.syncExec(new Runnable() {
						public void run() {
							try {
								MatlabWorkspaceView.getDefault().refresh();
							} catch (Exception e) {
							}
						}
					});
		} catch (Exception e) {

		}
		try {
			MConsolePlugin.getDefault().getWorkbench()
					.getActiveWorkbenchWindow().getActivePage()
					.removePartListener(MatlabConsoleView.getDefault());
		} catch (Exception e) {

		}
	}

}
