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
package org.eclipselabs.matclipse.mconsole.views;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.ControlContribution;
import org.eclipse.jface.action.GroupMarker;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.text.Document;
import org.eclipse.jface.text.IDocumentPartitioner;
import org.eclipse.jface.text.ITextOperationTarget;
import org.eclipse.jface.text.rules.FastPartitioner;
import org.eclipse.jface.text.source.SourceViewer;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.TreePath;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyleRange;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.dnd.Clipboard;
import org.eclipse.swt.dnd.TextTransfer;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.events.FocusAdapter;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IFileEditorInput;
import org.eclipse.ui.IPartListener;
import org.eclipse.ui.IPerspectiveDescriptor;
import org.eclipse.ui.IPerspectiveListener;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IWorkbenchActionConstants;
import org.eclipse.ui.IWorkbenchCommandConstants;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.actions.ActionFactory;
import org.eclipse.ui.navigator.CommonNavigator;
import org.eclipse.ui.part.ViewPart;
import org.eclipse.ui.texteditor.DefaultRangeIndicator;
import org.eclipse.ui.texteditor.IUpdate;
import org.eclipse.ui.themes.ITheme;
import org.eclipse.ui.themes.IThemeManager;
import org.eclipselabs.matclipse.mconsole.MConsolePlugin;
import org.eclipselabs.matclipse.mconsole.internal.ui.preferences.IPreferenceConstants;
import org.eclipselabs.matclipse.mconsole.internal.ui.preferences.ThemeConstants;
import org.eclipselabs.matclipse.mconsole.internal.ui.preferences.ThemeWrapper;
import org.eclipselabs.matclipse.mconsole.matlab.MatlabCommunicationException;
import org.eclipselabs.matclipse.mconsole.matlab.MatlabNotStartedException;

import org.eclipselabs.matclipse.meditor.editors.MatlabConfiguration;
import org.eclipselabs.matclipse.meditor.editors.MatlabEditor;
import org.eclipselabs.matclipse.meditor.editors.partitioner.MatlabPartitionScanner;
import org.eclipselabs.matclipse.meditor.util.ColorManager;
import org.eclipselabs.matclipse.util.MatclipseUtilPlugin;
import org.eclipselabs.matclipse.util.TextViewerAction;

public class MatlabConsoleView extends ViewPart implements IPartListener,
		IPerspectiveListener {
	public static String VIEW_ID="org.eclipselabs.matclipse.mconsole.views.MatlabConsoleView";
	private static MatlabConsoleView plugin;

	public static MatlabConsoleView getDefault() {
		return plugin;
	}

	boolean automaticDirectoryChange = false;
	private String[] directoryHistory;
	private int currentConsoleIndex;
	private Action clearCommandHistoryAction;
	private Action copyCommandHistoryAction;
	private Action selectAllCommandHistoryAction;
	private Composite composite;
	private Action dirChooseAction;
	private Action stopMatlabAction;
	private Action pauseMatlabAction;
	private Action startMatlabAction;
	private Action dirUpAction;
	protected Map<String, IAction> fGlobalActions = new HashMap<String, IAction>();
	protected List<String> fSelectionActions = new ArrayList<String>();
	private Action raiseFiguresAction;
	private Action raiseMltFiguresAction;
	private Action closeAllFiguresAction;
	private Action closeMltFiguresAction;

	private Action helpBrowserAction;
	private Action debugConsoleViewAction;
	private StyledText commandLineResultsText;
	private SourceViewer commandLineTextViewer;
	private StyledText commandLineText;
	// Invisible TextViewer, so that there can be syntax highlighting even when
	// called from somewhere else
	private SourceViewer invisibleTextViewer;
	private Combo dirChooser;
	private final Document document;
	private final Document invisibleDocument;

	private final ISelectionChangedListener selectionChangedListener = new ISelectionChangedListener() {
		public void selectionChanged(SelectionChangedEvent event) {
			updateSelectionDependentActions();
		}
	};

	protected void updateSelectionDependentActions() {
		Iterator<String> iterator = fSelectionActions.iterator();
		while (iterator.hasNext()) {
			updateAction(iterator.next());
		}
	}

	protected void updateAction(String actionId) {
		IAction action = fGlobalActions.get(actionId);
		if (action instanceof IUpdate) {
			((IUpdate) action).update();
		}
	}

	protected void setGlobalAction(IActionBars actionBars, String actionID,
			IAction action) {
		fGlobalActions.put(actionID, action);
		actionBars.setGlobalActionHandler(actionID, action);
	}

	private final IPropertyChangeListener valueChangeListener = new IPropertyChangeListener() {

		public void propertyChange(PropertyChangeEvent event) {
			if (event.getProperty().equals(IThemeManager.CHANGE_CURRENT_THEME)) {
				((ITheme) event.getOldValue()).getColorRegistry()
						.removeListener(this);
				((ITheme) event.getNewValue()).getColorRegistry().addListener(
						this);
				((ITheme) event.getOldValue()).getFontRegistry()
						.removeListener(this);
				((ITheme) event.getNewValue()).getFontRegistry().addListener(
						this);
			} else if (event.getProperty().equals(ThemeConstants.CONSOLE_FONT)) {
				ThemeWrapper theme = MConsolePlugin.getDefault()
						.getCurrentTheme();
				commandLineResultsText.setFont(theme
						.getFont(ThemeConstants.CONSOLE_FONT));
			} else if (event.getProperty().equals(
					ThemeConstants.CONSOLE_BACKGROUND_COLOR)) {
				ThemeWrapper theme = MConsolePlugin.getDefault()
						.getCurrentTheme();
				commandLineResultsText.setBackground(theme
						.getColor(ThemeConstants.CONSOLE_BACKGROUND_COLOR));

			} else if (event.getProperty().equals(
					ThemeConstants.CONSOLE_TEXT_COLOR)) {
				ThemeWrapper theme = MConsolePlugin.getDefault()
						.getCurrentTheme();
				commandLineResultsText.setForeground(theme
						.getColor(ThemeConstants.CONSOLE_TEXT_COLOR));
			} else if (event.getProperty().equals(
					ThemeConstants.CONSOLE_INPUT_FONT)) {
				ThemeWrapper theme = MConsolePlugin.getDefault()
						.getCurrentTheme();
				Font commandLineTextFont = theme
						.getFont(ThemeConstants.CONSOLE_INPUT_FONT);
				commandLineText.setFont(commandLineTextFont);
				GridData commandLineTextData = new GridData(GridData.FILL,
						GridData.CENTER, true, false);
				commandLineTextData.heightHint = commandLineTextFont
						.getFontData()[0].getHeight() + 6;
				commandLineText.setLayoutData(commandLineTextData);
				composite.layout();
			} else if (event.getProperty().equals(
					ThemeConstants.PROJECTSVIEW_TREE_FONT)) {
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

					ThemeWrapper theme = MConsolePlugin.getDefault()
							.getCurrentTheme();

					matlabProjectNavigator
							.getCommonViewer()
							.getTree()
							.setFont(
									theme.getFont(ThemeConstants.PROJECTSVIEW_TREE_FONT));
					matlabProjectNavigator.getCommonViewer().refresh();
				} catch (Exception e) {

				}
			}
		}
	};

	public void setDirChooserColor() {

	}

	/**
	 * The constructor.
	 */
	public MatlabConsoleView() {
		plugin = this;
		currentConsoleIndex = 0;
		directoryHistory = new String[] {};
		document = new Document("");
		invisibleDocument = new Document("");
	}

	/**
	 * Adds an Entry to the Command History
	 * 
	 * @param commandHistoryString
	 *            the String to add
	 */
	public void addCommandHistoryEntry(String commandHistoryString) {

		Vector<CommandHistoryEntry> removed = new Vector<CommandHistoryEntry>();
		for (int i = 0; i < MConsolePlugin.getDefault().getCommandHistory()
				.size(); i++) {
			CommandHistoryEntry historyEntry = MConsolePlugin.getDefault()
					.getCommandHistory().get(i);
			if (historyEntry.getName().equals(commandHistoryString)) {
				removed.add(historyEntry);
			}
		}
		for (int i = 0; i < removed.size(); i++) {
			MConsolePlugin.getDefault().getCommandHistory()
					.remove(removed.get(i));
		}
		MConsolePlugin.getDefault().addCommandHistoryEntry(
				new CommandHistoryEntry(commandHistoryString));
		try {
			CommandHistoryView.getDefault().update();
		} catch (NullPointerException e) {

		}

	}

	public void addDirectoryHistoryEntry() {

		String item;
		try {
			item = MConsolePlugin.getDefault().getMatlab().getMatlabPwd();
		} catch (MatlabCommunicationException e) {
			return;
		} catch (MatlabNotStartedException e) {
			return;
		}

		boolean isalreadyinthere = false;
		final String homeDirectory;
		if (MConsolePlugin.getSystem().contains("windows"))
			homeDirectory = System.getenv("HOMEPATH");
		else
			homeDirectory = System.getenv("HOME");
		final String workspaceRoot = ResourcesPlugin.getWorkspace().getRoot()
				.getLocation().toString();
		ThemeWrapper theme = MConsolePlugin.getDefault().getCurrentTheme();
		if (item.indexOf(workspaceRoot) > -1) {
			String displayName = item.substring(item.indexOf(workspaceRoot)
					+ workspaceRoot.length(), item.length());
			if (displayName.length() == 0) {
				item = "%WS/";
			} else
				item = "%WS" + displayName;
			dirChooser.setForeground(theme
					.getColor(ThemeConstants.CONSOLE_DIRCHOOSER_WSCOLOR));
		} else if (item.indexOf(homeDirectory) > -1) {
			String displayName = item.substring(item.indexOf(homeDirectory)
					+ homeDirectory.length(), item.length());
			item = "~" + displayName;
			dirChooser.setForeground(theme
					.getColor(ThemeConstants.CONSOLE_TEXT_COLOR));
		} else {
			dirChooser.setForeground(theme
					.getColor(ThemeConstants.CONSOLE_TEXT_COLOR));
		}
		for (int i = 0; i < directoryHistory.length; i++)
			if (directoryHistory[i].equals(item))
				isalreadyinthere = true;

		if (!isalreadyinthere) {
			String[] newDirectoryHistory = new String[directoryHistory.length + 1];
			for (int i = 0; i < directoryHistory.length; i++)
				newDirectoryHistory[i] = directoryHistory[i];
			newDirectoryHistory[directoryHistory.length] = item;
			directoryHistory = newDirectoryHistory;
			dirChooser.setItems(directoryHistory);
		}
		dirChooser.setText(item);
	}

	private void contributeToActionBars() {
		IActionBars bars = getViewSite().getActionBars();
		fillLocalPullDown(bars.getMenuManager());
		fillLocalToolBar(bars.getToolBarManager());
		TextViewerAction selectAllActionCommandLineText = new TextViewerAction(
				commandLineTextViewer, ITextOperationTarget.SELECT_ALL);
		selectAllActionCommandLineText.configureAction("Select All",
				"Select All", "Select All");
		selectAllActionCommandLineText
				.setActionDefinitionId(IWorkbenchCommandConstants.EDIT_SELECT_ALL);
		setGlobalAction(bars, ActionFactory.SELECT_ALL.getId(),
				selectAllActionCommandLineText);

		TextViewerAction cutActionCommandLineText = new TextViewerAction(
				commandLineTextViewer, ITextOperationTarget.CUT);
		cutActionCommandLineText.configureAction("Cut", "Cut", "Cut");
		cutActionCommandLineText
				.setActionDefinitionId(IWorkbenchCommandConstants.EDIT_CUT);
		setGlobalAction(bars, ActionFactory.CUT.getId(),
				cutActionCommandLineText);

		TextViewerAction copyActionCommandLineText = new TextViewerAction(
				commandLineTextViewer, ITextOperationTarget.COPY);
		copyActionCommandLineText.configureAction("Copy", "Copy", "Copy");
		copyActionCommandLineText
				.setActionDefinitionId(IWorkbenchCommandConstants.EDIT_COPY);
		setGlobalAction(bars, ActionFactory.COPY.getId(),
				copyActionCommandLineText);

		TextViewerAction pasteActionCommandLineText = new TextViewerAction(
				commandLineTextViewer, ITextOperationTarget.PASTE);
		pasteActionCommandLineText.configureAction("Paste", "Paste", "Paste");
		pasteActionCommandLineText
				.setActionDefinitionId(IWorkbenchCommandConstants.EDIT_PASTE);
		setGlobalAction(bars, ActionFactory.PASTE.getId(),
				pasteActionCommandLineText);

		MenuManager menuMgr = new MenuManager("#PopupMenu");
		Menu menu = menuMgr.createContextMenu(commandLineText);

		menuMgr.add(cutActionCommandLineText);
		menuMgr.add(copyActionCommandLineText);
		menuMgr.add(pasteActionCommandLineText);
		menuMgr.add(selectAllActionCommandLineText);
		commandLineText.setMenu(menu);

		fSelectionActions.add(ActionFactory.CUT.getId());
		fSelectionActions.add(ActionFactory.COPY.getId());
		fSelectionActions.add(ActionFactory.PASTE.getId());

		bars.updateActionBars();
	}

	private void lastCommandHistory() {
		currentConsoleIndex = currentConsoleIndex + 1;
		if (currentConsoleIndex > MConsolePlugin.getDefault()
				.getCommandHistory().size())
			currentConsoleIndex = MConsolePlugin.getDefault()
					.getCommandHistory().size();

		String currentText = getConsoleHistory(currentConsoleIndex).toString();
		setCommandHistorySelection(currentText);
		// commandLineText.setSelection(0,currenttext.length()-1);
	}

	private void setCommandHistorySelection(final String currentText) {
		document.set(currentText);
		commandLineTextViewer.setDocument(document);

		MConsolePlugin.getDefault().getWorkbench().getDisplay()
				.asyncExec(new Runnable() {
					public void run() {
						commandLineText.setSelection(currentText.length());
						commandLineText.setFocus();
						CommandHistoryView.getDefault().setSelection(
								getConsoleHistory(currentConsoleIndex));
						CommandHistoryView.getDefault().getViewer().getTable()
								.showSelection();
					}
				});
		// commandLineTextViewer.getVisibleRegion().getLength();
		// commandLineTextViewer.invalidateTextPresentation(commandLineTextViewer.getVisibleRegion().getOffset(),commandLineTextViewer.getVisibleRegion().getLength());
		try {

			document.notify();
		} catch (java.lang.IllegalMonitorStateException e) {

		}

	}

	private void nextCommandHistory() {
		currentConsoleIndex = currentConsoleIndex - 1;
		String currentText = "";
		if (currentConsoleIndex >= 1)
			currentText = getConsoleHistory(currentConsoleIndex).toString();
		if (currentConsoleIndex <= 1)
			currentConsoleIndex = 1;
		setCommandHistorySelection(currentText);
	}

	private void runCommandLineText() {
		if (commandLineTextViewer.getDocument().get() != "") {
			run(commandLineTextViewer.getDocument().get().trim()
					.replaceAll("\n", ""), null);
		}
	}

	@Override
	public void createPartControl(Composite parent) {
		parent.setLayout(new FillLayout());
		composite = new Composite(parent, SWT.NONE);
		final GridLayout gridLayout = new GridLayout();
		gridLayout.numColumns = 3;
		composite.setLayout(gridLayout);

		commandLineResultsText = new StyledText(composite, SWT.V_SCROLL
				| SWT.MULTI | SWT.BORDER | SWT.H_SCROLL | SWT.READ_ONLY
				| SWT.NO_FOCUS);
		commandLineResultsText.setLayoutData(new GridData(GridData.FILL,
				GridData.FILL, true, true, 3, 1));

		ThemeWrapper theme = MConsolePlugin.getDefault().getCurrentTheme();
		commandLineResultsText.setFont(theme
				.getFont(ThemeConstants.CONSOLE_FONT));
		commandLineResultsText.setBackground(theme
				.getColor(ThemeConstants.CONSOLE_BACKGROUND_COLOR));
		// commandLineResultsText.setForeground(theme
		// TODO: Check if it's working under Linux
		commandLineResultsText.addFocusListener(new FocusAdapter() {
			@Override
			public void focusGained(FocusEvent e) {
				MConsolePlugin.getDefault().getWorkbench().getDisplay()
						.syncExec(new Runnable() {
							public void run() {
								commandLineText.setFocus();

							}
						});
			}

		});
		commandLineResultsText.addSelectionListener(new SelectionAdapter() {

			@Override
			public void widgetSelected(SelectionEvent e) {
				if (commandLineResultsText.getSelectionText().equals("")) {
					copyCommandHistoryAction.setEnabled(false);
				} else
					copyCommandHistoryAction.setEnabled(true);
			}

		});

		commandLineResultsText.setEditable(false);

		final Label commandLineLabel = new Label(composite, SWT.NONE);
		commandLineLabel.setText("Command Line:");
		// commandLineTextViewer = new SourceViewer()
		invisibleTextViewer = new SourceViewer(composite, null, SWT.BORDER
				| SWT.MULTI);
		invisibleTextViewer.configure(new MatlabConfiguration(
				new ColorManager()));

		IDocumentPartitioner invisiblePartitioner = new FastPartitioner(
				new MatlabPartitionScanner(),
				MatlabPartitionScanner.getConfiguredContentTypes());
		invisiblePartitioner.connect(invisibleDocument);
		invisibleDocument.setDocumentPartitioner(invisiblePartitioner);

		invisibleTextViewer.setInput(invisibleDocument);
		invisibleTextViewer.setRangeIndicator(new DefaultRangeIndicator());
		invisibleTextViewer.getTextWidget().setVisible(false);

		commandLineTextViewer = new SourceViewer(composite, null, SWT.BORDER
				| SWT.MULTI);

		commandLineTextViewer.configure(new MatlabConfiguration(
				new ColorManager()));

		IDocumentPartitioner partitionerForCommandLine = new FastPartitioner(
				new MatlabPartitionScanner(),
				MatlabPartitionScanner.getConfiguredContentTypes());
		partitionerForCommandLine.connect(document);
		document.setDocumentPartitioner(partitionerForCommandLine);

		commandLineTextViewer.setInput(document);
		commandLineTextViewer.setRangeIndicator(new DefaultRangeIndicator());

		commandLineText = commandLineTextViewer.getTextWidget();

		// commandLineText = new StyledText(composite, SWT.BORDER | SWT.SINGLE);
		Font commandLineTextFont = theme
				.getFont(ThemeConstants.CONSOLE_INPUT_FONT);

		commandLineText.setFont(commandLineTextFont);
		commandLineText.addKeyListener(new KeyAdapter() {
			@Override
			public void keyPressed(final KeyEvent e) {

				if (e.keyCode == SWT.ARROW_DOWN) {
					nextCommandHistory();
				} else if (e.keyCode == SWT.ARROW_UP) {
					lastCommandHistory();
				} else if (e.character == SWT.CR) {
					runCommandLineText();
				}
			}
		});

		GridData commandLineTextData = new GridData(GridData.FILL,
				GridData.CENTER, true, false);
		commandLineTextData.heightHint = commandLineTextFont.getFontData()[0]
				.getHeight() + 6;
		commandLineText.setLayoutData(commandLineTextData);
		// new Label(composite, SWT.NONE);
		makeActions();
		hookContextMenu();
		contributeToActionBars();
		setFocustoConsole();

		commandLineTextViewer.getSelectionProvider()
				.addSelectionChangedListener(selectionChangedListener);

		IThemeManager themeManager = MConsolePlugin.getDefault().getWorkbench()
				.getThemeManager();
		// listen for the default workbench theme to change.
		themeManager.addPropertyChangeListener(valueChangeListener);
		// listen for changes to the current theme values
		themeManager.getCurrentTheme().getColorRegistry()
				.addListener(valueChangeListener);
		themeManager.getCurrentTheme().getFontRegistry()
				.addListener(valueChangeListener);
		if (MConsolePlugin.getDefault().isMatlabInitialized()) {
			setEnabled(true, true);
		} else {
			MatlabConsoleView.getDefault().outputStyledText("",
					"Loading Matlab..");
			setEnabled(false, true);
		}

	}

	private void fillContextMenu(IMenuManager manager) {
		manager.add(copyCommandHistoryAction);

		manager.add(clearCommandHistoryAction);
		manager.add(selectAllCommandHistoryAction);
		manager.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS));
	}

	private void fillLocalPullDown(IMenuManager manager) {
		manager.add(clearCommandHistoryAction);

	}

	private void fillLocalToolBar(IToolBarManager manager) {

		manager.add(new GroupMarker("matlabpause"));
		manager.add(new Separator("matlabpause"));
		manager.add(pauseMatlabAction);

		manager.add(new GroupMarker("chooser"));
		manager.add(new Separator("chooser"));

		manager.appendToGroup("chooser", new ControlContribution("none") { //$NON-NLS-1$
					@Override
					protected int computeWidth(Control control) {
						return 300;
					}

					@Override
					protected Control createControl(Composite parent) {
						dirChooser = new Combo(parent, SWT.NONE);
						dirChooser.addSelectionListener(new SelectionAdapter() {
							@Override
							public void widgetSelected(final SelectionEvent e) {
								String dirtochange = dirChooser.getText();
								final String workspaceRoot = ResourcesPlugin
										.getWorkspace().getRoot().getLocation()
										.toString();
								if (dirtochange.indexOf("%WS") > -1) {
									dirtochange = dirtochange.replaceFirst(
											"%WS", workspaceRoot);
								} else if (dirtochange.indexOf("~") > -1) {
									dirtochange = dirtochange.replaceFirst("~",
											System.getenv("HOME"));

								}
								MConsolePlugin
										.getDefault()
										.getMatlab()
										.changeMatlabDirectoryToPath(
												dirtochange);
							}
						});
						dirChooser.addKeyListener(new KeyAdapter() {
							@Override
							public void keyPressed(final KeyEvent e) {
								if (e.character == SWT.CR) {
									run("cd " + dirChooser.getText(), null);
								}
							}
						});
						if (MConsolePlugin.getDefault().isMatlabInitialized())
							try {
								dirChooser.setText(MConsolePlugin.getDefault()
										.getMatlab().getMatlabPwd());
							} catch (Exception e) {
							}
						dirChooser.setEnabled(false);
						return dirChooser;
					}
				});
		manager.appendToGroup("chooser", dirChooseAction);
		manager.appendToGroup("chooser", dirUpAction);
		manager.add(new GroupMarker("matclipse"));
		manager.add(new Separator("matclipse"));
		manager.add(raiseFiguresAction);
		manager.add(closeAllFiguresAction);
		manager.add(new GroupMarker("others"));
		manager.add(new Separator("others"));
		manager.add(clearCommandHistoryAction);
		manager.add(helpBrowserAction);
		manager.add(new GroupMarker("debug"));
		manager.add(new Separator("debug"));
		manager.add(debugConsoleViewAction);
		manager.add(new GroupMarker("matlab"));
		manager.add(new Separator("matlab"));
		manager.add(stopMatlabAction);
		manager.add(startMatlabAction);
	}

	public StyledText getCommandLineResultsText() {
		return commandLineResultsText;
	}

	public CommandHistoryEntry getConsoleHistory(int i) {
		commandLineText.setFocus();

		int length = MConsolePlugin.getDefault().getCommandHistory().size();
		if (i > length)
			i = length;
		else if (i < 0) {
			i = 0;
			return new CommandHistoryEntry("");
		}
		CommandHistoryEntry retVal = MConsolePlugin
				.getDefault()
				.getCommandHistory()
				.get(MConsolePlugin.getDefault().getCommandHistory().size() - i);
		return retVal;
	}

	public Combo getDirChooser() {
		return dirChooser;
	}

	private void hookContextMenu() {
		MenuManager menuMgr = new MenuManager("#PopupMenu");
		menuMgr.setRemoveAllWhenShown(true);
		menuMgr.addMenuListener(new IMenuListener() {
			public void menuAboutToShow(IMenuManager manager) {
				MatlabConsoleView.this.fillContextMenu(manager);
			}
		});
		Menu menu = menuMgr.createContextMenu(commandLineResultsText);
		commandLineResultsText.setMenu(menu);
	}

	private void makeActions() {

		pauseMatlabAction = new Action() {
			@Override
			public void run() {
				try {
					MConsolePlugin.getDefault().getMatlab().sendBreak();
				} catch (Exception e) {
					MatclipseUtilPlugin.getDefault().errorDialog(
							"Unable to cancel Matlab evaluation", e);
				}
			}
		};
		pauseMatlabAction.setText("Cancel Evaluation");
		pauseMatlabAction.setToolTipText("Cancel Evaluation");

		pauseMatlabAction.setImageDescriptor(ImageDescriptor
				.createFromURL(MConsolePlugin.getDefault().getBundle()
						.getEntry("icons/matlab_pause_action.gif")));
		pauseMatlabAction.setEnabled(false);
		startMatlabAction = new Action() {
			@Override
			public void run() {
				try {
					MConsolePlugin.getDefault().getMatlab().start();
				} catch (Exception e) {
					MatclipseUtilPlugin.getDefault().errorDialog(
							"Unable to start Matlab", e);
				}

			}
		};
		startMatlabAction.setText("Start Matlab");
		startMatlabAction.setToolTipText("Start Matlab");

		startMatlabAction.setImageDescriptor(ImageDescriptor
				.createFromURL(MConsolePlugin.getDefault().getBundle()
						.getEntry("icons/matlab_start_action.gif")));

		stopMatlabAction = new Action() {
			@Override
			public void run() {
				MConsolePlugin.getDefault().getMatlab().stop();
			}
		};
		stopMatlabAction.setText("Stop Matlab");
		stopMatlabAction.setToolTipText("Stop Matlab");

		stopMatlabAction.setImageDescriptor(ImageDescriptor
				.createFromURL(MConsolePlugin.getDefault().getBundle()
						.getEntry("icons/matlab_stop_action.gif")));

		clearCommandHistoryAction = new Action() {
			@Override
			public void run() {
				commandLineResultsText.setText("");
				commandLineText.setFocus();
			}
		};
		clearCommandHistoryAction.setText("Clear");
		clearCommandHistoryAction.setToolTipText("Clear Matlab Console");
		clearCommandHistoryAction.setImageDescriptor(ImageDescriptor
				.createFromURL(MConsolePlugin.getDefault().getBundle()
						.getEntry("icons/clear.gif")));

		copyCommandHistoryAction = new Action() {
			@Override
			public void run() {
				Clipboard clipboard = new Clipboard(MatlabConsoleView.this
						.getSite().getShell().getDisplay());
				String plainText = commandLineResultsText.getSelectionText();
				TextTransfer textTransfer = TextTransfer.getInstance();
				clipboard.setContents(new String[] { plainText },
						new Transfer[] { textTransfer });
				clipboard.dispose();
			}
		};

		copyCommandHistoryAction.setText("Copy");
		copyCommandHistoryAction.setToolTipText("Copy");
		copyCommandHistoryAction.setImageDescriptor(PlatformUI.getWorkbench()
				.getSharedImages()
				.getImageDescriptor(ISharedImages.IMG_TOOL_COPY));
		copyCommandHistoryAction.setEnabled(false);

		selectAllCommandHistoryAction = new Action() {
			@Override
			public void run() {
				commandLineResultsText.selectAll();
				copyCommandHistoryAction.setEnabled(true);
			}
		};

		selectAllCommandHistoryAction.setText("Select All");
		selectAllCommandHistoryAction.setToolTipText("Select All");

		dirChooseAction = new Action() {
			@Override
			public void run() {
				Shell shell = MatlabConsoleView.this.getSite().getShell();
				DirectoryDialog dialog = new DirectoryDialog(shell);

				try {
					dialog.setText(MConsolePlugin.getDefault().getMatlab()
							.getMatlabPwd());
				} catch (MatlabCommunicationException e) {
					return;
				} catch (MatlabNotStartedException e) {

					return;
				}

				dialog.setMessage("Select directory");
				String path = dialog.open();
				if (path == null)
					return;
				else
					MatlabConsoleView.getDefault().run("cd " + path, null);
			}
		};
		dirChooseAction.setText("Open Directory");
		dirChooseAction.setToolTipText("Open Directory");
		dirChooseAction.setImageDescriptor(PlatformUI.getWorkbench()
				.getSharedImages()
				.getImageDescriptor(ISharedImages.IMG_OBJ_FOLDER));

		dirUpAction = new Action() {
			@Override
			public void run() {
				String path = "";
				try {
					path = MConsolePlugin.getDefault().getMatlab()
							.getMatlabPwd();
				} catch (MatlabCommunicationException e) {
					return;
				} catch (MatlabNotStartedException e) {
					return;
				}
				File pathfile = new File(path);
				int index;
				if(!MConsolePlugin.getSystem().contains("windows"))
					index = pathfile.getAbsolutePath().lastIndexOf("/");
				else
					index = pathfile.getAbsolutePath().lastIndexOf("\\");
				String pathwo = pathfile.getAbsolutePath().substring(0, index);
				MatlabConsoleView.getDefault().run("cd " + pathwo, null);
			}
		};
		dirUpAction.setText("Change to parent directory");
		dirUpAction.setToolTipText("Change to parent directory");
		dirUpAction.setImageDescriptor(PlatformUI.getWorkbench()
				.getSharedImages()
				.getImageDescriptor(ISharedImages.IMG_TOOL_UP));

		raiseFiguresAction = new Action() {
			@Override
			public void run() {
				try {
					MConsolePlugin.getDefault().getMatlab()
							.eval("raise_figure('')", false, false);
				} catch (MatlabCommunicationException e) {
				} catch (MatlabNotStartedException e) {
				}
			}
		};
		raiseFiguresAction.setText("Raise my figures");
		raiseFiguresAction.setToolTipText("Raise my figures");
		raiseFiguresAction.setImageDescriptor(PlatformUI.getWorkbench()
				.getSharedImages()
				.getImageDescriptor(ISharedImages.IMG_TOOL_NEW_WIZARD));

		closeAllFiguresAction = new Action() {
			@Override
			public void run() {
				try {
					MConsolePlugin.getDefault().getMatlab()
							.eval("close_figure('')", false, false);
				} catch (MatlabCommunicationException e) {
				} catch (MatlabNotStartedException e) {
				}
			}
		};
		closeAllFiguresAction.setText("Close my figures");
		closeAllFiguresAction.setToolTipText("Close my figures");
		closeAllFiguresAction.setImageDescriptor(PlatformUI.getWorkbench()
				.getSharedImages()
				.getImageDescriptor(ISharedImages.IMG_DEF_VIEW));

		helpBrowserAction = new Action() {
			@Override
			public void run() {
				try {
					MConsolePlugin.getDefault().getMatlab()
							.eval("helpbrowser", false, false);
				} catch (MatlabCommunicationException e) {

				} catch (MatlabNotStartedException e) {

				}
			}
		};
		helpBrowserAction.setText("Matlab Helpbrowser");
		helpBrowserAction.setToolTipText("Matlab Helpbrowser");
		helpBrowserAction.setImageDescriptor(ImageDescriptor
				.createFromURL(MConsolePlugin.getDefault().getBundle()
						.getEntry("icons/help.gif")));

		// TODO: Hide Debug View on Shutdown
		debugConsoleViewAction = new Action() {
			@Override
			public void run() {
				try {
					if (!debugConsoleViewAction.isChecked()) {
						MConsolePlugin
								.getDefault()
								.getWorkbench()
								.getActiveWorkbenchWindow()
								.getActivePage()
								.showView(
										"org.eclipselabs.matclipse.mconsole.views.MatlabConsoleDebugView");
						MConsolePlugin
								.getDefault()
								.getWorkbench()
								.getActiveWorkbenchWindow()
								.getActivePage()
								.showView(
										"org.eclipselabs.matclipse.mconsole.views.MatlabConsoleView");
						MConsolePlugin.getDefault().getMatlab()
								.setLogging(true);
						debugConsoleViewAction.setChecked(true);
						debugConsoleViewAction.setText("Turn debugging off");
					} else {
						IViewPart debugConsoleView = MConsolePlugin
								.getDefault()
								.getWorkbench()
								.getActiveWorkbenchWindow()
								.getActivePage()
								.findView(
										"org.eclipselabs.matclipse.mconsole.views.MatlabConsoleDebugView");
						MConsolePlugin.getDefault().getWorkbench()
								.getActiveWorkbenchWindow().getActivePage()
								.hideView(debugConsoleView);
						MConsolePlugin.getDefault().getMatlab()
								.setLogging(false);
						debugConsoleViewAction.setChecked(false);
						debugConsoleViewAction.setText("Turn debugging off");
					}
					// Activator.getDefault().getWorkbench().getActiveWorkbenchWindow().getActivePage().hi
				} catch (Exception e) {

				}
			}
		};

		debugConsoleViewAction.setText("Turn debugging on");
		debugConsoleViewAction.setToolTipText("Toggle Matlab Console Debug");

		debugConsoleViewAction.setImageDescriptor(ImageDescriptor
				.createFromURL(MConsolePlugin.getDefault().getBundle()
						.getEntry("icons/debug_view.gif")));

	}

	public void outputError(String errorText) {
		outputStyledText("", "\n??? " + errorText + "\n");
	}

	public void outputStyledText(String inputText, String outputText) {
		StyleRange[] inputStyleRanges = invisibleTextViewer.getTextWidget()
				.getStyleRanges();
		StyleRange[] oldStyleRanges = commandLineResultsText.getStyleRanges();
		// StyleRange[] newStyleRanges = new StyleRange[commandLineResultsText
		// .getStyleRanges().length + 2];
		StyleRange[] newStyleRanges = new StyleRange[commandLineResultsText
				.getStyleRanges().length + 2 + inputStyleRanges.length];

		String oldtext = commandLineResultsText.getText();
		String inputOutputText = inputText + outputText;
		commandLineResultsText.setText(oldtext + inputOutputText);
		ThemeWrapper theme = MConsolePlugin.getDefault().getCurrentTheme();

		StyleRange styleRangeInputSign = new StyleRange();
		styleRangeInputSign.start = oldtext.length();
		styleRangeInputSign.length = 3;
		// styleRangeInputSign.length = inputText.length();
		styleRangeInputSign.fontStyle = SWT.NORMAL;
		styleRangeInputSign.foreground = theme
				.getColor(ThemeConstants.CONSOLE_TEXTINPUT_COLOR);

		for (int i = 0; i < inputStyleRanges.length; i++) {
			inputStyleRanges[i].start = oldtext.length()
					+ inputStyleRanges[i].start + 3;
		}
		StyleRange styleRangeOutput = new StyleRange();
		styleRangeOutput.start = oldtext.length() + inputText.length();
		styleRangeOutput.length = outputText.length();

		if (outputText.trim().indexOf("???") != -1) {
			styleRangeOutput.foreground = theme
					.getColor(ThemeConstants.CONSOLE_TEXTERROR_COLOR);
			MConsolePlugin.getDefault().getWorkbench()
					.getActiveWorkbenchWindow().getActivePage()
					.activate(MatlabConsoleView.this);
		}

		for (int i = 0; i < oldStyleRanges.length; i++) {
			newStyleRanges[i] = oldStyleRanges[i];
		}
		newStyleRanges[oldStyleRanges.length] = styleRangeInputSign;
		newStyleRanges[oldStyleRanges.length + 1] = styleRangeOutput;
		for (int i = 0; i < inputStyleRanges.length; i++) {
			newStyleRanges[oldStyleRanges.length + 2 + i] = inputStyleRanges[i];
		}
		for (int i = 0; i < newStyleRanges.length; i++) {
			try {
				commandLineResultsText.setStyleRange(newStyleRanges[i]);
			} catch (Exception e) {

			}
		}

		// commandLineResultsText.setStyleRanges(newStyleRanges);
		commandLineResultsText.setStyleRange(styleRangeInputSign);
		commandLineText.setText("");

		currentConsoleIndex = 0;

		commandLineResultsText.setSelection(commandLineResultsText.getText()
				.length());
		commandLineText.setFocus();
		try {
			CommandHistoryView.getDefault().setSelection(
					new CommandHistoryEntry(""));
		} catch (NullPointerException e) {

		}
	}

	public void run(String command, String filePath) {
		run(command, filePath, true, true);
	}

	public void run(String command, String filePath,
			final boolean addToCommandHistory, final boolean setfocus) {

		if (MConsolePlugin.getDefault().isMatlabInitialized()) {
			if (MConsolePlugin.getDefault().getMatlab().isMatlabAvailable()) {
				final String fp = filePath;
				final String cmd = command;

				invisibleDocument.set(command);
				invisibleTextViewer.setDocument(invisibleDocument);
				try {
					invisibleDocument.notify();
				} catch (java.lang.IllegalMonitorStateException e) {

				}

				currentConsoleIndex = 0;
				setEnabled(false, setfocus);
				Job job = new Job("Matclipse Console") {
					private String outputText;
					private String inputText;

					protected Action getCompletedAction() {
						return new Action("Update Workspace View") {
							@Override
							public void run() {
								try {
									MatlabWorkspaceView.getDefault().refresh();
								} catch (Exception e) {

								}
								outputStyledText(inputText, outputText);

							}

						};

					}

					@Override
					protected IStatus run(IProgressMonitor monitor) {

						new Thread() {
							@Override
							public void run() {

								String iText = ">> ";

								if (addToCommandHistory)
									MConsolePlugin.getDefault().getWorkbench()
											.getDisplay()
											.asyncExec(new Runnable() {
												public void run() {
													addCommandHistoryEntry(cmd);
												}
											});
								if (fp != null) {
									try {
										MatlabConsoleView.getDefault()
												.getViewSite().getShell()
												.getDisplay()
												.syncExec(new Runnable() {
													public void run() {
														try {
															MConsolePlugin
																	.getDefault()
																	.getMatlab()
																	.eval("cd '"
																			+ fp
																			+ "'");
														} catch (MatlabCommunicationException e) {

															e.printStackTrace();
														} catch (MatlabNotStartedException e) {

															e.printStackTrace();
														}
													}
												});

									} catch (NullPointerException e) {

										MatlabConsoleView.getDefault()
												.getViewSite().getShell()
												.getDisplay()
												.asyncExec(new Runnable() {
													public void run() {
														setEnabled(true,
																setfocus);
													}
												});
									}
									iText = iText + "script " + cmd + " in "
											+ fp + "\n";
								} else {
									iText = iText + cmd + "\n";

								}
								inputText = iText;
								try {
									if (fp != null)
										outputText = MConsolePlugin
												.getDefault().getMatlab()
												.eval(cmd, true, true, false);
									else {
										outputText = MConsolePlugin
												.getDefault().getMatlab()
												.eval(cmd, true, true);
									}
								} catch (NullPointerException e) {
									MatlabConsoleView.getDefault()
											.getViewSite().getShell()
											.getDisplay()
											.asyncExec(new Runnable() {
												public void run() {
													setEnabled(true, setfocus);
												}
											});
								} catch (MatlabNotStartedException e) {

								} catch (MatlabCommunicationException e) {

								}

								showResults();
							}

						}.start();

						return Status.OK_STATUS;

					}

					protected void showResults() {
						try {
							MConsolePlugin.getDefault().getWorkbench()
									.getDisplay().asyncExec(new Runnable() {
										public void run() {
											getCompletedAction().run();
										}
									});
						} catch (NullPointerException npe) {

						}
						MConsolePlugin.getDefault().getWorkbench().getDisplay()
								.syncExec(new Runnable() {
									public void run() {
										if (MConsolePlugin.getDefault()
												.isMatlabInitialized())
											setEnabled(true, setfocus);
										if (setfocus)
											commandLineText.setFocus();
									}
								});
					}
				};

				job.setPriority(Job.SHORT);
				Thread t = new Thread();
				t.run();
				job.setUser(true);
				job.setThread(t);
				job.schedule();
			} else
				MConsolePlugin.getDefault().getMatlab()
						.outputBusyError(new Throwable());
		} else
			MConsolePlugin.getDefault().getMatlab()
					.outputNotStartedError(new Throwable());

	}

	public void setDirChooser(Combo dirchooser) {
		this.dirChooser = dirchooser;
	}

	public void setDirChooser(String dir) {
		try {
			dirChooser.setText(dir);
		} catch (Throwable t) {

		}
	}

	public void setEnabled(boolean enabled, boolean focus) {

		if (enabled == true) {
			commandLineText.setEnabled(true);
			commandLineResultsText.setEnabled(true);
			clearCommandHistoryAction.setEnabled(true);
			dirChooseAction.setEnabled(true);
			try {
				dirChooser.setEnabled(true);
			} catch (NullPointerException e) {

			}
			stopMatlabAction.setEnabled(true);
			startMatlabAction.setEnabled(false);
			dirUpAction.setEnabled(true);
			raiseFiguresAction.setEnabled(true);
			closeAllFiguresAction.setEnabled(true);
			pauseMatlabAction.setEnabled(false);
			helpBrowserAction.setEnabled(true);
			debugConsoleViewAction.setEnabled(true);

			if (focus)
				setFocustoConsole();
		} else {
			pauseMatlabAction.setEnabled(true);
			commandLineText.setEnabled(false);
			commandLineResultsText.setEnabled(false);
			clearCommandHistoryAction.setEnabled(true);
			dirChooseAction.setEnabled(false);
			try {
				dirChooser.setEnabled(false);
			} catch (NullPointerException e) {

			}
			stopMatlabAction.setEnabled(true);
			startMatlabAction.setEnabled(false);
			dirUpAction.setEnabled(false);
			raiseFiguresAction.setEnabled(false);
			closeAllFiguresAction.setEnabled(false);
			helpBrowserAction.setEnabled(false);
			debugConsoleViewAction.setEnabled(false);

		}
	}

	public void enableMatlabStartAction() {
		startMatlabAction.setEnabled(true);
		stopMatlabAction.setEnabled(false);
		pauseMatlabAction.setEnabled(false);
	}

	@Override
	public void setFocus() {
		setFocustoConsole();
	}

	public void setFocustoConsole() {
		commandLineText.setFocus();
	}

	public void setSelection(ISelection selection) {

	}

	public void partActivated(IWorkbenchPart part) {
		if (MConsolePlugin.getDefault().isMatlabInitialized()) {

			if (MConsolePlugin.getDefault().getPreferenceStore()
					.getBoolean(IPreferenceConstants.P_AUTOMATICCDEDITOR)) {

				if (part instanceof MatlabEditor) {
					if (automaticDirectoryChange) {
						if (MConsolePlugin.getDefault().getMatlab()
								.isMatlabAvailable()) {
							MatlabEditor matlabEditor = (MatlabEditor) part;
							IEditorInput editorInput = matlabEditor
									.getEditorInput();
							IFile aFile = null;
							if (editorInput instanceof IFileEditorInput) {
								aFile = ((IFileEditorInput) editorInput)
										.getFile();
								MConsolePlugin.getDefault().getMatlab()
										.changeMatlabDirectoryToResource(aFile);
							}
						} else
							MConsolePlugin
									.getDefault()
									.getMatlab()
									.outputDirectoryChangeError(new Throwable());
					}

				}
			}

		}

	}

	public void partBroughtToTop(IWorkbenchPart part) {

	}

	public void partClosed(IWorkbenchPart part) {

	}

	public void partDeactivated(IWorkbenchPart part) {

	}

	public void partOpened(IWorkbenchPart part) {

	}

	public void perspectiveActivated(IWorkbenchPage page,
			IPerspectiveDescriptor perspective) {
		if (perspective
				.getId()
				.equals("org.eclipselabs.matclipse.mconsole.perspectives.MatlabWorkbench")) {
			automaticDirectoryChange = true;

			try {
				// MatlabProjectNavigator matlabProjectNavigator =
				// MatlabProjectNavigator.getDefault();

				IViewPart viewPart = PlatformUI
						.getWorkbench()
						.getActiveWorkbenchWindow()
						.getActivePage()
						.findView(
								"org.eclipselabs.matclipse.mconsole.views.MatlabProjectNavigator");
				// MatlabProjectNavigator matlabProjectNavigator =
				// MatlabProjectNavigator.getDefault();
				CommonNavigator matlabProjectNavigator = (CommonNavigator) viewPart;
				// System.out.println(matlabProjectNavigator);
				ThemeWrapper theme = MConsolePlugin.getDefault()
						.getCurrentTheme();

				matlabProjectNavigator.getCommonViewer()
						.addSelectionChangedListener(
								new ISelectionChangedListener() {

									public void selectionChanged(
											SelectionChangedEvent event) {
										if (MConsolePlugin
												.getDefault()
												.getPreferenceStore()
												.getBoolean(
														IPreferenceConstants.P_AUTOMATICCD)) {
											if (MConsolePlugin.getDefault()
													.isMatlabInitialized()) {
												if (MConsolePlugin.getDefault()
														.getMatlab()
														.isMatlabAvailable()) {
													try {
														Object obj = ((IStructuredSelection) event
																.getSelection())
																.getFirstElement();
														if (obj instanceof IResource) {
															IResource resource = (IResource) obj;
															MConsolePlugin
																	.getDefault()
																	.getMatlab()
																	.changeMatlabDirectoryToResource(
																			resource);
														}
													} catch (Exception e) {
														// Activator.getDefault().errorDialog("Test
														// Error", e);
													}
													try {
														MConsolePlugin
																.getDefault()
																.getWorkbench()
																.getActiveWorkbenchWindow()
																.getActivePage()
																.showView(
																		"org.eclipselabs.matclipse.mconsole.views.MatlabProjectNavigator");
													} catch (Exception e) {

													}
												} else
													MConsolePlugin
															.getDefault()
															.getMatlab()
															.outputDirectoryChangeError(
																	new Throwable());
											}
										}

									}

								});

				matlabProjectNavigator
						.getCommonViewer()
						.getTree()
						.setFont(
								theme.getFont(ThemeConstants.PROJECTSVIEW_TREE_FONT));
				// matlabProjectNavigator.getCommonViewer().re
			} catch (Exception e) {
				e.printStackTrace();
			}
			try {
				IViewPart viewPart = PlatformUI
						.getWorkbench()
						.getActiveWorkbenchWindow()
						.getActivePage()
						.findView(
								"org.eclipselabs.matclipse.mconsole.views.MatlabProjectNavigator");

				CommonNavigator matlabProjectNavigator = (CommonNavigator) viewPart;
				TreePath[] treePaths = matlabProjectNavigator.getCommonViewer()
						.getExpandedTreePaths();
				matlabProjectNavigator.getCommonViewer().refresh(true);
				matlabProjectNavigator.getCommonViewer().setExpandedTreePaths(
						treePaths);
			} catch (Exception e) {
				e.printStackTrace();
			}

		} else
			automaticDirectoryChange = false;
	}

	public void perspectiveChanged(IWorkbenchPage page,
			IPerspectiveDescriptor perspective, String changeId) {

	}

}
