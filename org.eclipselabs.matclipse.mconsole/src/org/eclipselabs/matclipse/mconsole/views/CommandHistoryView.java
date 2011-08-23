package org.eclipselabs.matclipse.mconsole.views;

import java.util.Vector;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.dnd.Clipboard;
import org.eclipse.swt.dnd.TextTransfer;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.dnd.TransferData;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.actions.ActionFactory;
import org.eclipse.ui.part.ViewPart;
import org.eclipse.ui.themes.ITheme;
import org.eclipse.ui.themes.IThemeManager;
import org.eclipselabs.matclipse.mconsole.MConsolePlugin;
import org.eclipselabs.matclipse.mconsole.internal.ui.preferences.ThemeConstants;
import org.eclipselabs.matclipse.mconsole.internal.ui.preferences.ThemeWrapper;

public class CommandHistoryView extends ViewPart implements IPropertyChangeListener {
	private TableViewer viewer;
	public static String VIEW_ID="org.eclipselabs.matclipse.mconsole.views.CommandHistoryView";
	private Action clearAction;
	private Action copyEntryAction;
	private Action pasteEntryAction;
	private Action cutEntryAction;
	private Action deleteEntryAction;
	private Action doubleClickAction;
	private static CommandHistoryView myself;

	
	private IPropertyChangeListener valueChangeListener = new IPropertyChangeListener() {

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
			} else if (event.getProperty().equals(ThemeConstants.COMMANDHISTORY_FONT) || event.getProperty().equals(
					ThemeConstants.COMMANDHISTORY_BACKGROUND_COLOR) || event.getProperty().equals(
							ThemeConstants.COMMANDHISTORY_FOREGROUND_COLOR) | event.getProperty().equals(
									ThemeConstants.COMMANDHISTORY_DATE_COLOR)) {
				ThemeWrapper theme = MConsolePlugin.getDefault().getCurrentTheme();
				viewer.getTable().setFont(theme
						.getFont(ThemeConstants.COMMANDHISTORY_FONT));
				viewer.refresh();
			}
		}
	};
	
	/**
	 * The constructor.
	 */
	public CommandHistoryView() {
		myself = this;

	}

	public void update() {
		viewer.setContentProvider(new CommandHistoryProvider());
		CommandHistoryLabelProvider labelprovider = new CommandHistoryLabelProvider();
		viewer.setLabelProvider(labelprovider);
		viewer.getTable().showItem(viewer.getTable().getItem(viewer.getTable().getItemCount() - 1));
		
	}

	public static CommandHistoryView getDefault() {
		return myself;
	}

	/**
	 * This is a callback that will allow us to create the viewer and initialize
	 * it.
	 */
	public void createPartControl(Composite parent) {

		viewer = new TableViewer(parent, SWT.MULTI | SWT.H_SCROLL
				| SWT.V_SCROLL);
		viewer.setContentProvider(new CommandHistoryProvider());
		CommandHistoryLabelProvider labelprovider = new CommandHistoryLabelProvider();
		viewer.setLabelProvider(labelprovider);

		viewer.setInput(getViewSite());
		org.eclipselabs.matclipse.meditor.Activator.getDefault().getPreferenceStore().addPropertyChangeListener(this);
		makeActions();
		hookContextMenu();
		hookGlobalActions();
		contributeToActionBars();
		hookDoubleClickAction();
		viewer.getTable().showItem(viewer.getTable().getItem(viewer.getTable().getItemCount() - 1));
		viewer.getTable().select(viewer.getTable().getItemCount() - 1);
		
		IThemeManager themeManager = MConsolePlugin.getDefault().getWorkbench()
		.getThemeManager();
		// listen for the default workbench theme to change.
		themeManager.addPropertyChangeListener(valueChangeListener);
		// listen for changes to the current theme values
		themeManager.getCurrentTheme().getColorRegistry().addListener(
				valueChangeListener);
		themeManager.getCurrentTheme().getFontRegistry().addListener(
				valueChangeListener);
		ThemeWrapper theme = MConsolePlugin.getDefault().getCurrentTheme();
		viewer.getTable().setFont(theme
				.getFont(ThemeConstants.COMMANDHISTORY_FONT));

	}

	private void hookGlobalActions() {
		
        IActionBars bars = getViewSite().getActionBars();
        //bars.setGlobalActionHandler(ActionFactory.SELECT_ALL.getId(), selectAllAction);
        bars.setGlobalActionHandler(ActionFactory.DELETE.getId(), deleteEntryAction);
        bars.setGlobalActionHandler(ActionFactory.CUT.getId(),cutEntryAction);
		bars.setGlobalActionHandler(ActionFactory.PASTE.getId(),pasteEntryAction);
		bars.setGlobalActionHandler(ActionFactory.COPY.getId(),copyEntryAction);
        viewer.getControl().addKeyListener(new KeyAdapter() {
                   public void keyPressed(KeyEvent event) {
                           if (event.character == SWT.DEL && 
                                   event.stateMask == 0 && 
                                   deleteEntryAction.isEnabled()) 
                           {
                        	   deleteEntryAction.run();
                           }
                   }
                   
           });
   }
	private void hookDoubleClickAction() {

		viewer.addDoubleClickListener(new IDoubleClickListener() {
			public void doubleClick(DoubleClickEvent event) {
				doubleClickAction.run();
			}
		});
	}

	private void hookContextMenu() {
		MenuManager menuMgr = new MenuManager("#PopupMenu");
		menuMgr.setRemoveAllWhenShown(true);
		menuMgr.addMenuListener(new IMenuListener() {
			public void menuAboutToShow(IMenuManager manager) {
				CommandHistoryView.this.fillContextMenu(manager);
			}
		});
		Menu menu = menuMgr.createContextMenu(viewer.getControl());
		viewer.getControl().setMenu(menu);
		getSite().registerContextMenu(menuMgr, viewer);
	}

	private void contributeToActionBars() {
		IActionBars bars = getViewSite().getActionBars();
		fillLocalPullDown(bars.getMenuManager());
		fillLocalToolBar(bars.getToolBarManager());
	}

	private void fillLocalPullDown(IMenuManager manager) {
		
		manager.add(deleteEntryAction);
		manager.add(new Separator());
		manager.add(clearAction);

	} 

	private void fillContextMenu(IMenuManager manager) {
		manager.add(cutEntryAction);
		manager.add(copyEntryAction);
		manager.add(pasteEntryAction);
		manager.add(new Separator());
		manager.add(deleteEntryAction);
		manager.add(new Separator());
		manager.add(clearAction);

	}

	private void fillLocalToolBar(IToolBarManager manager) {
		manager.add(clearAction);
	}

	private void makeActions() {

		pasteEntryAction = new Action() {

			public void run() {
				Clipboard clipboard = new Clipboard(CommandHistoryView.this.getSite().getShell().getDisplay());
		        
		        TransferData[] transferDatas = clipboard.getAvailableTypes();

		        for(int i=0; i<transferDatas.length; i++) {
		          // Checks whether RTF format is available.
		          if(TextTransfer.getInstance().isSupportedType(transferDatas[i])) {
		        	  String plainText = (String)clipboard.getContents(TextTransfer.getInstance());
		        	  MConsolePlugin.getDefault().addCommandHistoryEntry(new CommandHistoryEntry(plainText));
		        	  update();
		            break;
		          }
		        }
		        
		        clipboard.dispose();
			}
		};
		
		pasteEntryAction.setText("Paste");
		pasteEntryAction.setToolTipText("Pastes current clipboard contents");
		pasteEntryAction.setImageDescriptor(PlatformUI.getWorkbench()
				.getSharedImages().getImageDescriptor(
						ISharedImages.IMG_TOOL_PASTE));
		cutEntryAction = new Action() {
			
			public void run() {
				
				ISelection selection = viewer.getSelection();
				try {
					
					Object obj = ((IStructuredSelection) selection)
							.getFirstElement();
					if (obj instanceof CommandHistoryEntry) {
						Clipboard clipboard = new Clipboard(CommandHistoryView.this.getSite().getShell().getDisplay());
						CommandHistoryEntry entry = (CommandHistoryEntry) obj;
						String plainText = entry.getName();
						TextTransfer textTransfer = TextTransfer.getInstance();
						clipboard.setContents(new String[] { plainText },
								new Transfer[] { textTransfer });
						clipboard.dispose();
						MConsolePlugin.getDefault().deleteCommandHistoryEntry(entry);
						update();
					}

				} catch (Exception e) {

				}

			}
		};
		
		cutEntryAction.setText("Cut");
		cutEntryAction.setToolTipText("Cut");
		cutEntryAction.setImageDescriptor(PlatformUI.getWorkbench()
				.getSharedImages().getImageDescriptor(
						ISharedImages.IMG_TOOL_CUT));
		
		copyEntryAction = new Action() {
			
			public void run() {
				ISelection selection = viewer.getSelection();
				try {
					Object obj = ((IStructuredSelection) selection)
							.getFirstElement();
					if (obj instanceof CommandHistoryEntry) {
						Clipboard clipboard = new Clipboard(CommandHistoryView.this.getSite().getShell().getDisplay());
						CommandHistoryEntry entry = (CommandHistoryEntry) obj;
						String plainText = entry.getName();
						TextTransfer textTransfer = TextTransfer.getInstance();
						clipboard.setContents(new String[] { plainText },
								new Transfer[] { textTransfer });
						clipboard.dispose();
					}

				} catch (Exception e) {

				}

			}
		};
		
		copyEntryAction.setText("Copy");
		copyEntryAction.setToolTipText("Copy");
		copyEntryAction.setImageDescriptor(PlatformUI.getWorkbench()
				.getSharedImages().getImageDescriptor(
						ISharedImages.IMG_TOOL_COPY));
		
		deleteEntryAction = new Action() {
			public void run() {
				ISelection selection = viewer.getSelection();
				try {
					Object obj = ((IStructuredSelection) selection)
							.getFirstElement();
					if (obj instanceof CommandHistoryEntry) {
						CommandHistoryEntry entry = (CommandHistoryEntry) obj;
						MConsolePlugin.getDefault().deleteCommandHistoryEntry(entry);
						update();
					}

				} catch (Exception e) {

				}

			}
		};
		deleteEntryAction.setText("Delete");
		deleteEntryAction.setToolTipText("Delete");
		deleteEntryAction.setImageDescriptor(PlatformUI.getWorkbench()
				.getSharedImages().getImageDescriptor(
						ISharedImages.IMG_TOOL_DELETE));
		clearAction = new Action() {
			public void run() {
				MConsolePlugin.getDefault().setCommandHistory(
						new Vector<CommandHistoryEntry>());
				update();
			}
		};
		clearAction.setText("Clear");
		clearAction.setToolTipText("Clear Command History");
		clearAction.setImageDescriptor(ImageDescriptor
				.createFromURL(MConsolePlugin.getDefault().getBundle().getEntry(
				"icons/clear.gif")));
		doubleClickAction = new Action() {
			public void run() {
				ISelection selection = viewer.getSelection();
				Object obj = ((IStructuredSelection) selection)
						.getFirstElement();

				CommandHistoryEntry historyentry = (CommandHistoryEntry) obj;
				MatlabConsoleView.getDefault().run(historyentry.toString(),
						null);
			}
		};

	}

	public void setFocus() {
		viewer.getControl().setFocus();
	}

	public void setSelection(CommandHistoryEntry entry) {

		viewer.setSelection(new StructuredSelection(entry));
	}

	public TableViewer getViewer() {
		return viewer;
	}

	public void setViewer(TableViewer viewer) {
		this.viewer = viewer;
	}

	public void propertyChange(PropertyChangeEvent event) {
		//TODO: Beschr�nken auf Comment Color
		update();
	}

}
