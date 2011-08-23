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

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.jface.viewers.IFontProvider;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.ViewPart;
import org.eclipse.ui.themes.ITheme;
import org.eclipse.ui.themes.IThemeManager;
import org.eclipselabs.matclipse.mconsole.MConsolePlugin;
import org.eclipselabs.matclipse.mconsole.internal.ui.preferences.IPreferenceConstants;
import org.eclipselabs.matclipse.mconsole.internal.ui.preferences.ThemeConstants;
import org.eclipselabs.matclipse.mconsole.internal.ui.preferences.ThemeWrapper;
import org.eclipselabs.matclipse.mconsole.matlab.MOutputParser;
import org.eclipselabs.matclipse.mconsole.matlab.MatlabCommunicationException;
import org.eclipselabs.matclipse.mconsole.matlab.MatlabData;
import org.eclipselabs.matclipse.mconsole.matlab.MatlabDataObject;
import org.eclipselabs.matclipse.mconsole.matlab.MatlabDataParent;
import org.eclipselabs.matclipse.mconsole.matlab.MatlabNotStartedException;


public class MatlabWorkspaceView extends ViewPart {
	public static String VIEW_ID="org.eclipselabs.matclipse.mconsole.views.MatlabWorkspaceView";
	private StyledText text;

	private static TreeViewer viewer;

	private List<MOutputParser> vars;

	private List<MOutputParser> globalvars;

	private static MatlabWorkspaceView myself;

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
			} else if (event.getProperty()
					.equals(ThemeConstants.WORKSPACE_FONT)) {
				ThemeWrapper theme = getCurrentTheme();
				text.setFont(theme.getFont(ThemeConstants.WORKSPACE_FONT));
			} else if (event.getProperty().equals(
					ThemeConstants.WORKSPACE_BACKGROUND_COLOR)) {
				ThemeWrapper theme = getCurrentTheme();
				text.setBackground(theme
						.getColor(ThemeConstants.WORKSPACE_BACKGROUND_COLOR));
			} else if (event.getProperty().equals(
					ThemeConstants.WORKSPACE_TEXT_COLOR)) {
				ThemeWrapper theme = getCurrentTheme();
				text.setForeground(theme
						.getColor(ThemeConstants.WORKSPACE_TEXT_COLOR));
			} else if (event.getProperty().equals(
					ThemeConstants.WORKSPACE_TREE_FONT)) {
				ThemeWrapper theme = getCurrentTheme();
				viewer.getTree().setFont(
						theme.getFont(ThemeConstants.WORKSPACE_TREE_FONT));
				refresh();
			}
		}
	};

	class ViewContentProvider implements IStructuredContentProvider,
			ITreeContentProvider {
		private MatlabDataParent invisibleRoot;

		public void inputChanged(Viewer v, Object oldInput, Object newInput) {
		}

		public void dispose() {
		}

		public Object[] getElements(Object parent) {
			if (parent.equals(getViewSite())) {
				if (invisibleRoot == null)
					initialize();
				return getChildren(invisibleRoot);
			}
			return getChildren(parent);
		}

		public Object getParent(Object child) {
			if (child instanceof MatlabDataObject) {
				return ((MatlabDataObject) child).getParent();
			}
			return null;
		}

		public Object[] getChildren(Object parent) {
			if (parent instanceof MatlabDataParent) {
				return ((MatlabDataParent) parent).getChildren();
			}
			return new Object[0];
		}

		public boolean hasChildren(Object parent) {
			if (parent instanceof MatlabDataParent)
				return ((MatlabDataParent) parent).hasChildren();
			return false;
		}

		/*
		 * We will set up a dummy model to initialize tree heararchy. In a real
		 * code, you will connect to a real model and expose its hierarchy.
		 */

		public MatlabDataParent outputMatlabParent(String name, MatlabData data) {
			try {
				MatlabDataParent output;
				List<?> contents = data.getData();
				String[] childrensname = new String[contents.size()];

				String sizestring = data.getSize();

				try {

					sizestring = sizestring.substring(1,
							sizestring.length() - 1);

					String[] sizestringsplit = sizestring.split(",");
					StringBuilder middle = new StringBuilder(sizestringsplit[1]
							+ "," + sizestringsplit[0] + ",");
					for (int i = 2; i < sizestringsplit.length; i++) {
						middle.append(sizestringsplit[i] + ",");
					}

					sizestring = "[" + middle.substring(0, middle.length() - 1)
							+ "]";

				} catch (Exception e) {

					sizestring = data.getSize();
				}

				output = new MatlabDataParent(name + " (" + data.getType()
						+ ")" + sizestring, data);

				if (data.getType().equals("struct")) {

					childrensname = data.getName().trim().split(",");
					String[] sizearr = sizestring
							.substring(1, sizestring.length() - 1).trim()
							.split(",");
					Integer sx = 0, sy = 0;

					if (!(sizearr.length > 2)) {
						sx = new Integer(sizearr[0]).intValue();
						sy = new Integer(sizearr[1]).intValue();
					}
					
					try {
						for (int i = 0; i < sx; i++) {
							for (int j = 0; j < sy; j++) {
								Integer index = j + i * sx;
								String structurename = "structure element"
										+ " " + "(" + (i + 1) + "," + (j + 1)
										+ ")";
								// childrensname[index] =
								// "structure element"+" "+"("
								// +(j+1)+","+(i+1)+")";
								MatlabDataParent child = new MatlabDataParent(
										structurename, new MatlabData());
								output.addChild(child);

								for (int k = 0; k < childrensname.length; k++) {
									MatlabData structElementData = (MatlabData) contents
											.get(k * sx * sy + i * sy + j);
									MatlabDataParent grandchild = outputMatlabParent(
											childrensname[k], structElementData);
									child.addChild(grandchild);

								}
							}
						}
						
					} catch (Throwable t) {
						t.printStackTrace();
					}

				}
				if (data.getType().equals("cell")) {

					String[] sizearr = sizestring
							.substring(1, sizestring.length() - 1).trim()
							.split(",");

					Integer sx = 0, sy = 0;
					if (!(sizearr.length > 2)) {
						sx = new Integer(sizearr[0]).intValue();
						sy = new Integer(sizearr[1]).intValue();
					}
					for (int i = 0; i < sx; i++) {
						for (int j = 0; j < sy; j++) {
								Integer index = j + i * sy;
								if (sx == 1 && sy == 1)
									childrensname[index] = "cell element";
								else
									childrensname[index] = "cell element "
											+ "{" + (i + 1) + "," + (j + 1)
											+ "}";
								MatlabDataParent child = outputMatlabParent(
										childrensname[index],
										(MatlabData) contents.get(j + i*sy));
								output.addChild(child);
							}
					
					}
				}

				return output;
			} catch (Exception e) {
				e.printStackTrace();
				return null;
			}

		}

		private void initialize() {
			invisibleRoot = new MatlabDataParent("", new MatlabData());
			if (MConsolePlugin.getDefault().isMatlabInitialized()) {
				try {
					vars = MConsolePlugin.getDefault().getMatlab()
							.getLocalVars();
					globalvars = MConsolePlugin.getDefault().getMatlab()
							.getGlobalVars();
				} catch (NullPointerException e) {
					vars = new ArrayList<MOutputParser>();
					globalvars = new ArrayList<MOutputParser>();
				} catch (MatlabCommunicationException e) {
					vars = new ArrayList<MOutputParser>();
					globalvars = new ArrayList<MOutputParser>();
				} catch (MatlabNotStartedException e) {
					vars = new ArrayList<MOutputParser>();
					globalvars = new ArrayList<MOutputParser>();
				}
				MatlabDataParent localRoot = new MatlabDataParent(
						"Local Variables - name(class)[size]<min,mean,max>",
						new MatlabData());
				MatlabDataParent globalRoot = new MatlabDataParent(
						"Global Variables - name(class)[size]<min,mean,max>",
						new MatlabData());
				MatlabDataParent testresultRoot = MConsolePlugin.getDefault()
						.getTestResults();
				invisibleRoot.addChild(localRoot);
				invisibleRoot.addChild(globalRoot);
				if (testresultRoot != null)
					invisibleRoot.addChild(testresultRoot);
				for (int i = 0; i < vars.size(); i++) {
					MOutputParser var = vars.get(i);
					String varname = var.getName();
					MatlabDataParent p1 = outputMatlabParent(varname,
							var.getData());

					localRoot.addChild(p1);
				}
				for (int i = 0; i < globalvars.size(); i++) {
					// lobal
					MOutputParser globalvar = globalvars.get(i);
					String varname = globalvar.getName();

					MatlabDataParent p1 = outputMatlabParent(varname,
							globalvar.getData());
					globalRoot.addChild(p1);
				}
			}

		}

		public MatlabDataParent getInvisibleRoot() {
			return invisibleRoot;
		}

		public void setInvisibleRoot(MatlabDataParent invisibleRoot) {
			this.invisibleRoot = invisibleRoot;
		}
	}

	class ViewLabelProvider extends LabelProvider implements IFontProvider {

		@Override
		public String getText(Object obj) {
			if (obj instanceof MatlabDataParent) {
				MatlabDataParent matlabDataParent = (MatlabDataParent) obj;
				MatlabData matlabData = matlabDataParent.getMatlabdata();
				if (matlabData.isNumeric()) {
					if (!matlabData.getSize().equals("[1,1]"))
						return obj.toString() + " <" + matlabData.getMin()
								+ "," + matlabData.getMean() + ","
								+ matlabData.getMax() + ">";
				}
			}
			return obj.toString();
		}

		@Override
		public Image getImage(Object obj) {
			String imageKey = ISharedImages.IMG_OBJ_ELEMENT;

			if (obj instanceof MatlabDataParent) {
				MatlabDataParent matlabdata = (MatlabDataParent) obj;
				try {
					String type = matlabdata.getMatlabdata().getType();
					if (type.equals("char"))
						return ImageDescriptor.createFromURL(
								MConsolePlugin.getDefault().getBundle()
										.getEntry("icons/c.gif")).createImage();
					else if (type.equals("dchar"))
						return ImageDescriptor.createFromURL(
								MConsolePlugin.getDefault().getBundle()
										.getEntry("icons/dc.gif"))
								.createImage();
					else if (type.equals("single"))
						return ImageDescriptor.createFromURL(
								MConsolePlugin.getDefault().getBundle()
										.getEntry("icons/s.gif")).createImage();
					else if (type.equals("double"))
						return ImageDescriptor.createFromURL(
								MConsolePlugin.getDefault().getBundle()
										.getEntry("icons/d.gif")).createImage();
					else if (type.equals("logical"))
						return ImageDescriptor.createFromURL(
								MConsolePlugin.getDefault().getBundle()
										.getEntry("icons/L.gif")).createImage();
					else if (type.equals("int8"))
						return ImageDescriptor.createFromURL(
								MConsolePlugin.getDefault().getBundle()
										.getEntry("icons/i8.gif"))
								.createImage();
					else if (type.equals("uint8"))
						return ImageDescriptor.createFromURL(
								MConsolePlugin.getDefault().getBundle()
										.getEntry("icons/ui8.gif"))
								.createImage();
					else if (type.equals("int16"))
						return ImageDescriptor.createFromURL(
								MConsolePlugin.getDefault().getBundle()
										.getEntry("icons/i16.gif"))
								.createImage();
					else if (type.equals("uint16"))
						return ImageDescriptor.createFromURL(
								MConsolePlugin.getDefault().getBundle()
										.getEntry("icons/ui16.gif"))
								.createImage();
					else if (type.equals("int32"))
						return ImageDescriptor.createFromURL(
								MConsolePlugin.getDefault().getBundle()
										.getEntry("icons/i32.gif"))
								.createImage();
					else if (type.equals("uint32"))
						return ImageDescriptor.createFromURL(
								MConsolePlugin.getDefault().getBundle()
										.getEntry("icons/ui32.gif"))
								.createImage();
					else if (type.equals("int64"))
						return ImageDescriptor.createFromURL(
								MConsolePlugin.getDefault().getBundle()
										.getEntry("icons/i64.gif"))
								.createImage();
					else if (type.equals("uint64"))
						return ImageDescriptor.createFromURL(
								MConsolePlugin.getDefault().getBundle()
										.getEntry("icons/ui64.gif"))
								.createImage();
					else if (type.equals("handle"))
						return ImageDescriptor.createFromURL(
								MConsolePlugin.getDefault().getBundle()
										.getEntry("icons/h.gif")).createImage();
					else if (type.equals("function_handle"))
						return ImageDescriptor.createFromURL(
								MConsolePlugin.getDefault().getBundle()
										.getEntry("icons/h.gif")).createImage();
					else if (type.equals("cell"))
						return ImageDescriptor.createFromURL(
								MConsolePlugin.getDefault().getBundle()
										.getEntry("icons/cl.gif"))
								.createImage();
					else if (type.equals("sparse"))
						return ImageDescriptor.createFromURL(
								MConsolePlugin.getDefault().getBundle()
										.getEntry("icons/sp.gif"))
								.createImage();
					else if (type.equals("struct"))
						return ImageDescriptor.createFromURL(
								MConsolePlugin.getDefault().getBundle()
										.getEntry("icons/st.gif"))
								.createImage();
					else if (type.equals("inline"))
						return ImageDescriptor.createFromURL(
								MConsolePlugin.getDefault().getBundle()
										.getEntry("icons/il.gif"))
								.createImage();
					else if (type.equals("sym"))
						return ImageDescriptor.createFromURL(
								MConsolePlugin.getDefault().getBundle()
										.getEntry("icons/sy.gif"))
								.createImage();
					else
						imageKey = ISharedImages.IMG_OBJ_ELEMENT;
				} catch (Exception e) {
					if (matlabdata.getName().equals("Local Variables"))
						return ImageDescriptor.createFromURL(
								MConsolePlugin.getDefault().getBundle()
										.getEntry("icons/local.gif"))
								.createImage();
					else
						return ImageDescriptor.createFromURL(
								MConsolePlugin.getDefault().getBundle()
										.getEntry("icons/global.gif"))
								.createImage();
				}
			} else
				imageKey = ISharedImages.IMG_OBJ_FOLDER;

			return PlatformUI.getWorkbench().getSharedImages()
					.getImage(imageKey);
		}

		public Font getFont(Object element) {
			ThemeWrapper theme = getCurrentTheme();
			return theme.getFont(ThemeConstants.WORKSPACE_TREE_FONT);

		}
	}

	// class NameSorter extends ViewerSorter {
	// }

	/**
	 * The constructor.
	 */
	public MatlabWorkspaceView() {
		myself = this;
	}

	/**
	 * This is a callback that will allow us to create the viewer and initialize
	 * it.
	 */

	@Override
	public void createPartControl(Composite parent) {

		System.currentTimeMillis();
		final SashForm sashForm1 = new SashForm(parent, SWT.BORDER);
		final Composite composite_1 = new Composite(sashForm1, SWT.NONE);
		composite_1.setLayout(new FillLayout());
		// final ScrolledComposite = new ScrolledComposite(parent, SWT.BORDER |
		// SWT.H_SCROLL | SWT.V_SCROLL);
		final GridLayout gridLayout = new GridLayout();
		gridLayout.numColumns = 1;
		sashForm1.setLayout(gridLayout);
		ThemeWrapper theme = getCurrentTheme();

		viewer = new TreeViewer(composite_1, SWT.BORDER);
		viewer.addSelectionChangedListener(new ISelectionChangedListener() {
			public void selectionChanged(final SelectionChangedEvent e) {
				ISelection selection = viewer.getSelection();
				// TODO: Run in background
				Object obj = ((IStructuredSelection) selection)
						.getFirstElement();
				if (obj instanceof MatlabDataParent) {
					// System.out.println("TreeParent");
					try {
						MatlabDataParent select = (MatlabDataParent) obj;
						MatlabData data = select.getMatlabdata();
						String type = data.getType();
						if (type.equals("single") || type.equals("double")
								|| type.equals("logical")
								|| type.equals("int8") || type.equals("uint8")
								|| type.equals("int16")
								|| type.equals("uint16")
								|| type.equals("int32")
								|| type.equals("uint32")
								|| type.equals("int64")
								|| type.equals("uint64")

						) {
							List<?> contentstruct = data.getData();
							MConsolePlugin.getDefault().waitForAutoBuild();

							String sizestr = data.getSize();
							String[] sizearr = sizestr
									.substring(1, sizestr.length() - 1).trim()
									.split(",");
							String contenttext = "";
							StringBuilder contenttextstruct = new StringBuilder();
							for (int i = 0; i < contentstruct.size(); i++) {
								contenttextstruct.append((String) contentstruct
										.get(i));
							}

							if (!MConsolePlugin
									.getDefault()
									.getPreferenceStore()
									.getBoolean(
											IPreferenceConstants.P_SHRINKOUTPUT)) {
								if (!(sizearr.length > 2)) {
									Integer sx = new Integer(sizearr[0])
											.intValue();
									Integer sy = new Integer(sizearr[1])
											.intValue();
									MatlabData datastruct = MConsolePlugin
											.getDefault()
											.getMatlab()
											.getMatlabVarXML(
													contenttextstruct
															.toString())
											.getData();
									List<?> content = datastruct.getData();
									sizestr = datastruct.getSize();
									sizearr = sizestr
											.substring(1, sizestr.length() - 1)
											.trim().split(",");
									sx = new Integer(sizearr[0]).intValue();
									sy = new Integer(sizearr[1]).intValue();
									Integer maxLength = 0;
									for (int i = 0; i < sy; i++) {
										for (int j = 0; j < sx; j++) {
											int length = ((String) content
													.get(j + i * sx)).length();
											if (length > maxLength)
												maxLength = length;
										}
									}
									for (int i = 0; i < sy; i++) {
										for (int j = 0; j < sx; j++) {
											final int index = j + i * sx;
											String s = ((String) content
													.get(index)).trim();
											int length = s.length();

											for (int k = 0; k < maxLength
													- length; k++)
												contenttext = contenttext + " ";
											contenttext = contenttext + s + " ";
										}
										contenttext = contenttext + '\n';
									}
									if (sx == 0 && sy == 0)
										contenttext = contenttext + "[]";
								} else {
									contenttext = MConsolePlugin
											.getDefault()
											.getMatlab()
											.eval(contenttextstruct.toString(),
													false, false);
								}
							} else {
								MConsolePlugin
										.getDefault()
										.getMatlab()
										.evalNoOutput(
												"shrink=shrink_output("
														+ contenttextstruct
														+ ");");
								MOutputParser outputparser = MConsolePlugin
										.getDefault().getMatlab()
										.getMatlabVarXML("shrink");
								List<?> content = outputparser.getData()
										.getData();
								for (int i = 0; i < content.size(); i++) {
									contenttext = contenttext
											+ (String) content.get(i);
								}

								MConsolePlugin.getDefault().getMatlab()
										.evalNoOutput("clear shrink;");
							}

							text.setText(contenttext);

						} else if (type.equals("char") || type.equals("dchar")) {
							List<?> content = data.getData();
							String sizestr = data.getSize();
							String[] sizearr = sizestr
									.substring(1, sizestr.length() - 1).trim()
									.split(",");
							Integer sx = new Integer(sizearr[0]).intValue();
							Integer sy = new Integer(sizearr[1]).intValue();
							String contenttext = "";
							for (int i = 0; i < content.size(); i++) {
								contenttext = contenttext
										+ (String) content.get(i);
							}

							String outputText = "";
							for (int i = 0; i < sy; i++) {
								for (int j = 0; j < sx; j++) {
									final int index = j + i * sx;
									char c = (contenttext.charAt(index));
									outputText = outputText + c;
								}
								outputText = outputText + '\n';
							}
							if (sx == 0 && sy == 0)
								outputText = outputText + "[]";
							text.setText(outputText);

						} else if (type.equals("function_handle")
								|| type.equals("inline") || type.equals("sym")
								|| type.equals("sparse")) {
							List<?> content = data.getData();
							String outputText = "";
							for (int i = 0; i < content.size(); i++) {
								outputText = outputText
										+ (String) content.get(i);
							}
							String sizestr = data.getSize();
							String[] sizearr = sizestr
									.substring(1, sizestr.length() - 1).trim()
									.split(",");
							Integer sx = new Integer(sizearr[0]).intValue();
							Integer sy = new Integer(sizearr[1]).intValue();
							if (sx == 0 && sy == 0)
								outputText = outputText + "[]";
							text.setText(outputText);

						} else {
							text.setText("");
						}

					} catch (Throwable t) {
						text.setText("");
					}
				}

			}
		});
		viewer.setAutoExpandLevel(1);

		viewer.setContentProvider(new ViewContentProvider());
		viewer.setLabelProvider(new ViewLabelProvider());

		viewer.setInput(getViewSite());

		final SashForm sashForm = new SashForm(sashForm1, SWT.VERTICAL);

		final Composite composite_2 = new Composite(sashForm, SWT.NONE);
		composite_2.setLayout(new FillLayout());

		text = new StyledText(composite_2, SWT.V_SCROLL | SWT.MULTI
				| SWT.H_SCROLL);
		text.setEditable(false);
		// text.set
		sashForm.setWeights(new int[] { 262 });
		sashForm.setSize(247, 342);
		sashForm1.setWeights(new int[] { 100, 200 });
		sashForm1.setSize(200, 200);
		text.setFont(theme.getFont(ThemeConstants.WORKSPACE_FONT));
		text.setBackground(theme
				.getColor(ThemeConstants.WORKSPACE_BACKGROUND_COLOR));
		text.setForeground(theme.getColor(ThemeConstants.WORKSPACE_TEXT_COLOR));
		text.setWordWrap(false);
		IThemeManager themeManager = MConsolePlugin.getDefault().getWorkbench()
				.getThemeManager();
		// listen for the default workbench theme to change.
		themeManager.addPropertyChangeListener(valueChangeListener);
		// listen for changes to the current theme values
		themeManager.getCurrentTheme().getColorRegistry()
				.addListener(valueChangeListener);
		themeManager.getCurrentTheme().getFontRegistry()
				.addListener(valueChangeListener);

		viewer.getTree().setFont(
				theme.getFont(ThemeConstants.WORKSPACE_TREE_FONT));

	}

	public void refresh() {
		if (MConsolePlugin.getDefault().isMatlabInitialized()) {
			getViewer().refresh();
			getViewer().expandAll();
			text.setText("");
		} else {
			ViewContentProvider contentprovider = (ViewContentProvider) getViewer()
					.getContentProvider();
			contentprovider.setInvisibleRoot(null);
			getViewer().refresh();
			text.setText("");
		}
	}

	public TreeViewer getViewer() {
		viewer.setContentProvider(new ViewContentProvider());
		viewer.setLabelProvider(new ViewLabelProvider());
		// viewer.setSorter(new NameSorter());
		viewer.setInput(getViewSite());
		return viewer;
	}

	public ThemeWrapper getCurrentTheme() {
		return new ThemeWrapper(MConsolePlugin.getDefault().getWorkbench()
				.getThemeManager().getCurrentTheme());
	}

	public static MatlabWorkspaceView getDefault() {
		return myself;
	}

	/**
	 * Passing the focus request to the viewer's control.
	 */
	@Override
	public void setFocus() {
		viewer.getControl().setFocus();
	}

	public StyledText getText() {
		return text;
	}
}
