/*******************************************************************************
 * Copyright (c) 2005, 2008 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     QNX Software Systems - initial API and implementation
 *     Ken Ryall (Nokia) - bug 178731
 *	   IBM Corporation
 *******************************************************************************/

//-----------------------------------------------------------------------------
//
// Copyright (c) 2009,2010 Terma GmbH
// Darmstadt, Germany
//
// Update : see bottom of file
//
// Remarks: 
// This file has been adapted from Eclipse source files which are licensed 
// by QNX Software Systems and others and which have been distributed under
// the terms of the Eclipse Public License v1.0 (cf. the copyright statement
// above).
// 
// It has been MODIFIED by Terma GmbH and is being re-destributed under the 
// terms of the Eclipse Public License v1.0. 
//-----------------------------------------------------------------------------
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
//
// Copyright (c) 2011-2012 VEGA Space GmbH
// Darmstadt, Germany
//
// Updates:
// - Updated for Eclipse 3.7.x (Indigo)
// - Modified code is now distributed via Google Code
//
// Remarks: 
// This file has been adapted from Eclipse source files which are licensed by 
// QNX Software Systems and others and which have been modified and distributed
// by Terma GmbH under the terms of the Eclipse Public License v1.0
// (see the copyright statements above).
//
// It has been MODIFIED by VEGA Space GmbH and is being re-destributed under 
// the terms of the Eclipse Public License v1.0. 
//-----------------------------------------------------------------------------
//-----------------------------------------------------------------------------

//NOTICE: This class' implementation has been derived from
//org.eclipse.cdt.launch.ui.CMainTab. 

package com.vegaspace.cdt.launch.ui;

import java.io.File;
import java.io.IOException;
import java.text.MessageFormat;
import java.util.ArrayList;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.IBinaryParser;
import org.eclipse.cdt.core.IBinaryParser.IBinaryObject;
import org.eclipse.cdt.core.ICDescriptor;
import org.eclipse.cdt.core.ICExtensionReference;
import org.eclipse.cdt.core.model.CModelException;
import org.eclipse.cdt.core.model.CoreModel;
import org.eclipse.cdt.core.model.IBinary;
import org.eclipse.cdt.core.model.ICElement;
import org.eclipse.cdt.core.model.ICProject;
import org.eclipse.cdt.core.settings.model.ICProjectDescription;
import org.eclipse.cdt.debug.core.ICDTLaunchConfigurationConstants;
import org.eclipse.cdt.debug.mi.core.MIPlugin;
import org.eclipse.cdt.launch.internal.ui.LaunchImages;
import org.eclipse.cdt.launch.internal.ui.LaunchMessages;
import org.eclipse.cdt.launch.internal.ui.LaunchUIPlugin;
import org.eclipse.cdt.launch.ui.CLaunchConfigurationTab;
import org.eclipse.cdt.launch.ui.ICDTLaunchHelpContextIds;
import org.eclipse.cdt.ui.CElementLabelProvider;
import org.eclipse.cdt.utils.pty.PTY;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.ui.DebugUITools;
import org.eclipse.debug.ui.IDebugUIConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.BusyIndicator;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.dialogs.ElementListSelectionDialog;
import org.eclipse.ui.dialogs.TwoPaneElementSelector;

/**
 * A launch configuration tab that displays and edits project and main type name
 * launch configuration attributes.
 * <p>
 * This class may be instantiated. This class is not intended to be subclassed.
 * </p>
 * 
 * @since 2.0
 */

@SuppressWarnings({ "deprecation" })
public class UmfCMainTab extends CLaunchConfigurationTab {

	static interface IUmfCLaunchConfigurationConstants {
		public static final String ATTR_HOST_NAME = MIPlugin
				.getUniqueIdentifier() + ".HOST_NAME";

		public static final String ATTR_USER_NAME = MIPlugin
				.getUniqueIdentifier() + ".USER_NAME";

		public static final String ATTR_DAEMON_NAME = MIPlugin
				.getUniqueIdentifier() + ".DAEMON_NAME";

		public static final String ATTR_SIMULATION_NAME = MIPlugin
				.getUniqueIdentifier() + ".SIMULATION_NAME";

		public static final String ATTR_REGISTRY_LOCATION = MIPlugin
				.getUniqueIdentifier() + ".REGISTRY_LOCATION";
	}

	static interface Utilities {
		static final String DEFAULT_DAEMON_NAME = "UMF_DEBUG";

		static final String DEFAULT_SIMULATION_NAME = "";

		static final String EMPTY_STRING = "";
	}

	// Project UI widgets
	protected Label fProjLabel;

	protected Text fProjText;

	protected Button fProjButton;

	// Main class UI widgets
	protected Label fRegistryLabel;

	protected Text fRegistryText;

	protected Label fDaemonNameLabel = null;

	protected Text fDaemonNameText = null;

	protected Label fSimNameLabel = null;

	protected Text fSimNameText = null;

	protected Label fProgLabel;

	protected Text fProgText;

	protected Button fSearchButton;

	private final boolean fWantsTerminalOption;

	protected Button fTerminalButton;

	private final boolean dontCheckProgram;

	private String filterPlatform = Utilities.EMPTY_STRING;

	public static final int WANTS_TERMINAL = 1;

	public static final int DONT_CHECK_PROGRAM = 2;

	private static final String SIMHOST_RELATIVE_PATH = "bin/SimHost";

	public UmfCMainTab() {
		this(0);
	}

	public UmfCMainTab(boolean terminalOption) {
		this(terminalOption ? WANTS_TERMINAL : 0);
	}

	public UmfCMainTab(int flags) {
		this.fWantsTerminalOption = (flags & WANTS_TERMINAL) != 0;
		this.dontCheckProgram = (flags & DONT_CHECK_PROGRAM) != 0;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.debug.ui.ILaunchConfigurationTab#createControl(org.eclipse
	 * .swt.widgets.Composite)
	 */
	@Override
	public void createControl(Composite parent) {
		Composite comp = new Composite(parent, SWT.NONE);
		setControl(comp);

		LaunchUIPlugin
				.getDefault()
				.getWorkbench()
				.getHelpSystem()
				.setHelp(
						getControl(),
						ICDTLaunchHelpContextIds.LAUNCH_CONFIGURATION_DIALOG_MAIN_TAB);

		GridLayout topLayout = new GridLayout();
		comp.setLayout(topLayout);

		createVerticalSpacer(comp, 1);
		createProjectGroup(comp, 1);
		createExeFileGroup(comp, 1);
		createVerticalSpacer(comp, 1);
		if (wantsTerminalOption() /* && ProcessFactory.supportesTerminal() */) {
			createTerminalOption(comp, 1);
		}
		LaunchUIPlugin.setDialogShell(parent.getShell());
	}

	protected void createProjectGroup(Composite parent, int colSpan) {
		Composite projComp = new Composite(parent, SWT.NONE);
		GridLayout projLayout = new GridLayout();
		projLayout.numColumns = 2;
		projLayout.marginHeight = 0;
		projLayout.marginWidth = 0;
		projComp.setLayout(projLayout);
		GridData gd = new GridData(GridData.FILL_HORIZONTAL);
		gd.horizontalSpan = colSpan;
		projComp.setLayoutData(gd);

		this.fProjLabel = new Label(projComp, SWT.NONE);
		this.fProjLabel.setText(LaunchMessages.CMainTab_ProjectColon);
		gd = new GridData();
		gd.horizontalSpan = 2;
		this.fProjLabel.setLayoutData(gd);

		this.fProjText = new Text(projComp, SWT.SINGLE | SWT.BORDER);
		gd = new GridData(GridData.FILL_HORIZONTAL);
		this.fProjText.setLayoutData(gd);
		this.fProjText.addModifyListener(new ModifyListener() {

			@Override
			public void modifyText(ModifyEvent evt) {
				updateLaunchConfigurationDialog();
			}
		});

		this.fProjButton = createPushButton(projComp,
				LaunchMessages.Launch_common_Browse_1, null);
		this.fProjButton.addSelectionListener(new SelectionAdapter() {

			@Override
			public void widgetSelected(SelectionEvent evt) {
				handleProjectButtonSelected();
				updateLaunchConfigurationDialog();
			}
		});
	}

	protected void createExeFileGroup(Composite parent, int colSpan) {
		Composite mainComp = new Composite(parent, SWT.NONE);
		GridLayout mainLayout = new GridLayout();
		mainLayout.numColumns = 3;
		mainLayout.marginHeight = 0;
		mainLayout.marginWidth = 0;
		mainComp.setLayout(mainLayout);
		GridData gd = new GridData(GridData.FILL_HORIZONTAL);
		gd.horizontalSpan = colSpan;
		mainComp.setLayoutData(gd);

		this.fRegistryLabel = new Label(mainComp, SWT.NONE);
		this.fRegistryLabel.setText("Simulation platform registry:");
		gd = new GridData();
		gd.horizontalSpan = 3;
		this.fRegistryLabel.setLayoutData(gd);
		this.fRegistryText = new Text(mainComp, SWT.SINGLE | SWT.BORDER);
		gd = new GridData(GridData.FILL_HORIZONTAL);
		this.fRegistryText.setLayoutData(gd);
		this.fRegistryText.addModifyListener(new ModifyListener() {
			@Override
			public void modifyText(ModifyEvent evt) {
				handleRegistryTextModified();
				updateLaunchConfigurationDialog();
			}
		});

		Button fBrowseForRegistryButton;
		fBrowseForRegistryButton = createPushButton(mainComp,
				LaunchMessages.Launch_common_Browse_2, null);
		fBrowseForRegistryButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent evt) {
				handleRegistryBrowseButtonSelected();
				updateLaunchConfigurationDialog();
			}
		});

		this.fProgLabel = new Label(mainComp, SWT.NONE);
		this.fProgLabel.setText("Simulation executable:");
		gd = new GridData();
		gd.horizontalSpan = 3;
		this.fProgLabel.setLayoutData(gd);
		this.fProgText = new Text(mainComp, SWT.SINGLE | SWT.BORDER);
		gd = new GridData(GridData.FILL_HORIZONTAL);
		this.fProgText.setLayoutData(gd);
		this.fProgText.addModifyListener(new ModifyListener() {
			@Override
			public void modifyText(ModifyEvent evt) {
				updateLaunchConfigurationDialog();
			}
		});

		Button fBrowseForBinaryButton;
		fBrowseForBinaryButton = createPushButton(mainComp,
				LaunchMessages.Launch_common_Browse_2, null);
		fBrowseForBinaryButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent evt) {
				handleBinaryBrowseButtonSelected();
				updateLaunchConfigurationDialog();
			}
		});

		this.fDaemonNameLabel = new Label(mainComp, SWT.NONE);
		this.fDaemonNameLabel.setText("Simulation platform daemon name:");
		gd = new GridData();
		gd.horizontalSpan = 3;
		this.fDaemonNameLabel.setLayoutData(gd);
		this.fDaemonNameText = new Text(mainComp, SWT.SINGLE | SWT.BORDER);
		gd = new GridData(GridData.FILL_HORIZONTAL);
		this.fDaemonNameText.setLayoutData(gd);
		this.fDaemonNameText.addModifyListener(new ModifyListener() {
			@Override
			public void modifyText(ModifyEvent evt) {
				updateLaunchConfigurationDialog();
			}
		});

		this.fSimNameLabel = new Label(mainComp, SWT.NONE);
		this.fSimNameLabel.setText("Simulation name:");
		gd = new GridData();
		gd.horizontalSpan = 3;
		this.fSimNameLabel.setLayoutData(gd);
		this.fSimNameText = new Text(mainComp, SWT.SINGLE | SWT.BORDER);
		gd = new GridData(GridData.FILL_HORIZONTAL);
		this.fSimNameText.setLayoutData(gd);
		this.fSimNameText.addModifyListener(new ModifyListener() {
			@Override
			public void modifyText(ModifyEvent evt) {
				updateLaunchConfigurationDialog();
			}
		});
	}

	protected boolean wantsTerminalOption() {
		return this.fWantsTerminalOption;
	}

	protected void createTerminalOption(Composite parent, int colSpan) {
		Composite mainComp = new Composite(parent, SWT.NONE);
		GridLayout mainLayout = new GridLayout();
		mainLayout.numColumns = 1;
		mainLayout.marginHeight = 0;
		mainLayout.marginWidth = 0;
		mainComp.setLayout(mainLayout);
		GridData gd = new GridData(GridData.FILL_HORIZONTAL);
		gd.horizontalSpan = colSpan;
		mainComp.setLayoutData(gd);

		this.fTerminalButton = createCheckButton(mainComp,
				LaunchMessages.CMainTab_UseTerminal);
		this.fTerminalButton.addSelectionListener(new SelectionAdapter() {

			@Override
			public void widgetSelected(SelectionEvent evt) {
				updateLaunchConfigurationDialog();
			}
		});
		this.fTerminalButton.setEnabled(PTY.isSupported());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.debug.ui.ILaunchConfigurationTab#initializeFrom(org.eclipse
	 * .debug.core.ILaunchConfiguration)
	 */
	@Override
	public void initializeFrom(ILaunchConfiguration config) {
		this.filterPlatform = getPlatform(config);
		updateProjectFromConfig(config);
		updateProgramFromConfig(config);
		updateTerminalFromConfig(config);
		updateRegistryFromConfig(config);
		updateDaemonNameFromConfig(config);
		updateSimNameFromConfig(config);
	}

	protected void updateDaemonNameFromConfig(ILaunchConfiguration configuration) {
		String daemonName = Utilities.DEFAULT_DAEMON_NAME;
		try {
			daemonName = configuration.getAttribute(
					IUmfCLaunchConfigurationConstants.ATTR_DAEMON_NAME,
					Utilities.DEFAULT_DAEMON_NAME);
		} catch (CoreException e) {
		}

		this.fDaemonNameText.setText(daemonName);
	}

	protected void updateSimNameFromConfig(ILaunchConfiguration configuration) {
		String simName = Utilities.DEFAULT_SIMULATION_NAME;
		try {
			simName = configuration.getAttribute(
					IUmfCLaunchConfigurationConstants.ATTR_SIMULATION_NAME,
					Utilities.DEFAULT_SIMULATION_NAME);
		} catch (CoreException e) {
		}

		this.fSimNameText.setText(simName);
	}

	protected void updateRegistryFromConfig(ILaunchConfiguration config) {
		String registryLocation = Utilities.EMPTY_STRING;
		try {
			registryLocation = config.getAttribute(
					IUmfCLaunchConfigurationConstants.ATTR_REGISTRY_LOCATION,
					Utilities.EMPTY_STRING);
		} catch (CoreException ce) {
			LaunchUIPlugin.log(ce);
		}
		this.fRegistryText.setText(registryLocation);
	}

	protected void updateTerminalFromConfig(ILaunchConfiguration config) {
		if (this.fTerminalButton != null) {
			boolean useTerminal = true;
			try {
				useTerminal = config.getAttribute(
						ICDTLaunchConfigurationConstants.ATTR_USE_TERMINAL,
						ICDTLaunchConfigurationConstants.USE_TERMINAL_DEFAULT);
			} catch (CoreException e) {
				LaunchUIPlugin.log(e);
			}
			this.fTerminalButton.setSelection(useTerminal);
		}
	}

	protected void updateProjectFromConfig(ILaunchConfiguration config) {
		String projectName = Utilities.EMPTY_STRING;
		try {
			projectName = config.getAttribute(
					ICDTLaunchConfigurationConstants.ATTR_PROJECT_NAME,
					Utilities.EMPTY_STRING);
		} catch (CoreException ce) {
			LaunchUIPlugin.log(ce);
		}
		this.fProjText.setText(projectName);
	}

	protected void updateProgramFromConfig(ILaunchConfiguration config) {
		String programName = Utilities.EMPTY_STRING;
		try {
			programName = config.getAttribute(
					ICDTLaunchConfigurationConstants.ATTR_PROGRAM_NAME,
					Utilities.EMPTY_STRING);
		} catch (CoreException ce) {
			LaunchUIPlugin.log(ce);
		}
		this.fProgText.setText(programName);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.debug.ui.ILaunchConfigurationTab#performApply(org.eclipse
	 * .debug.core.ILaunchConfigurationWorkingCopy)
	 */
	@Override
	public void performApply(ILaunchConfigurationWorkingCopy config) {
		ICProject cProject = getCProject();
		if (cProject != null) {
			config.setMappedResources(new IResource[] { cProject.getProject() });
			try { // Only initialize the build config ID once.
				if (config
						.getAttribute(
								ICDTLaunchConfigurationConstants.ATTR_PROJECT_BUILD_CONFIG_ID,
								"").length() == 0)//$NON-NLS-1$
				{
					ICProjectDescription projDes = CCorePlugin.getDefault()
							.getProjectDescription(cProject.getProject());
					if (projDes != null) {
						String buildConfigID = projDes.getActiveConfiguration()
								.getId();
						config.setAttribute(
								ICDTLaunchConfigurationConstants.ATTR_PROJECT_BUILD_CONFIG_ID,
								buildConfigID);
					}
				}
			} catch (CoreException e) {
				e.printStackTrace();
			}
		}
		config.setAttribute(ICDTLaunchConfigurationConstants.ATTR_PROJECT_NAME,
				this.fProjText.getText());
		config.setAttribute(ICDTLaunchConfigurationConstants.ATTR_PROGRAM_NAME,
				this.fProgText.getText());
		if (this.fTerminalButton != null) {
			config.setAttribute(
					ICDTLaunchConfigurationConstants.ATTR_USE_TERMINAL,
					this.fTerminalButton.getSelection());
		}

		// Set registry location.
		config.setAttribute(
				IUmfCLaunchConfigurationConstants.ATTR_REGISTRY_LOCATION,
				this.fRegistryText.getText());

		// Set simulation platform daemon name.
		config.setAttribute(IUmfCLaunchConfigurationConstants.ATTR_DAEMON_NAME,
				this.fDaemonNameText.getText().trim());

		// Set simulation name.
		config.setAttribute(
				IUmfCLaunchConfigurationConstants.ATTR_SIMULATION_NAME,
				this.fSimNameText.getText().trim());
	}

	/**
	 * Show a dialog that lists all main types
	 */
	protected void handleSearchButtonSelected() {

		if (getCProject() == null) {
			MessageDialog
					.openInformation(
							getShell(),
							LaunchMessages.CMainTab_Project_required,
							LaunchMessages.CMainTab_Enter_project_before_searching_for_program);
			return;
		}

		ILabelProvider programLabelProvider = new CElementLabelProvider() {

			@Override
			public String getText(Object element) {
				if (element instanceof IBinary) {
					IBinary bin = (IBinary) element;
					StringBuffer name = new StringBuffer();
					name.append(bin.getPath().lastSegment());
					return name.toString();
				}
				return super.getText(element);
			}

			@Override
			public Image getImage(Object element) {
				if (!(element instanceof ICElement)) {
					return super.getImage(element);
				}
				ICElement celement = (ICElement) element;

				if (celement.getElementType() == ICElement.C_BINARY) {
					IBinary belement = (IBinary) celement;
					if (belement.isExecutable()) {
						return DebugUITools
								.getImage(IDebugUIConstants.IMG_ACT_RUN);
					}
				}

				return super.getImage(element);
			}
		};

		ILabelProvider qualifierLabelProvider = new CElementLabelProvider() {

			@Override
			public String getText(Object element) {
				if (element instanceof IBinary) {
					IBinary bin = (IBinary) element;
					StringBuffer name = new StringBuffer();
					name.append(bin.getCPU()
							+ (bin.isLittleEndian() ? "le" : "be")); //$NON-NLS-1$ //$NON-NLS-2$
					name.append(" - "); //$NON-NLS-1$
					name.append(bin.getPath().toString());
					return name.toString();
				}
				return super.getText(element);
			}
		};

		TwoPaneElementSelector dialog = new TwoPaneElementSelector(getShell(),
				programLabelProvider, qualifierLabelProvider);
		dialog.setElements(getBinaryFiles(getCProject()));
		dialog.setMessage(LaunchMessages.CMainTab_Choose_program_to_run);
		dialog.setTitle(LaunchMessages.CMainTab_Program_Selection);
		dialog.setUpperListLabel(LaunchMessages.Launch_common_BinariesColon);
		dialog.setLowerListLabel(LaunchMessages.Launch_common_QualifierColon);
		dialog.setMultipleSelection(false);
		// dialog.set
		if (dialog.open() == Window.OK) {
			IBinary binary = (IBinary) dialog.getFirstResult();
			this.fProgText.setText(binary.getResource()
					.getProjectRelativePath().toString());
		}

	}

	protected void handleRegistryBrowseButtonSelected() {
		DirectoryDialog directoryDialog = new DirectoryDialog(getShell(),
				SWT.NONE);
		String text = directoryDialog.open();
		if (text != null) {
			this.fRegistryText.setText(text);
		}
	}

	/**
	 * Show a dialog that lets the user select a project. This in turn provides
	 * context for the main type, allowing the user to key a main type name, or
	 * constraining the search for main types to the specified project.
	 */
	protected void handleBinaryBrowseButtonSelected() {
		FileDialog fileDialog = new FileDialog(getShell(), SWT.NONE);
		fileDialog.setFileName(this.fProgText.getText());
		String text = fileDialog.open();
		if (text != null) {
			this.fProgText.setText(text);
		}
	}

	/**
	 * Iterate through and suck up all of the executable files that we can find.
	 */
	protected IBinary[] getBinaryFiles(final ICProject cproject) {
		final Display display;
		if (cproject == null || !cproject.exists()) {
			return null;
		}
		if (getShell() == null) {
			display = LaunchUIPlugin.getShell().getDisplay();
		} else {
			display = getShell().getDisplay();
		}
		final Object[] ret = new Object[1];
		BusyIndicator.showWhile(display, new Runnable() {

			@Override
			public void run() {
				try {
					ret[0] = cproject.getBinaryContainer().getBinaries();
				} catch (CModelException e) {
					LaunchUIPlugin.errorDialog("Launch UI internal error", e); //$NON-NLS-1$
				}
			}
		});

		return (IBinary[]) ret[0];
	}

	/**
	 * Show a dialog that lets the user select a project. This in turn provides
	 * context for the main type, allowing the user to key a main type name, or
	 * constraining the search for main types to the specified project.
	 */
	protected void handleProjectButtonSelected() {
		ICProject project = chooseCProject();
		if (project == null) {
			return;
		}

		String projectName = project.getElementName();
		this.fProjText.setText(projectName);
	}

	/**
	 * Realize a C Project selection dialog and return the first selected
	 * project, or null if there was none.
	 */
	protected ICProject chooseCProject() {
		try {
			ICProject[] projects = getCProjects();

			ILabelProvider labelProvider = new CElementLabelProvider();
			ElementListSelectionDialog dialog = new ElementListSelectionDialog(
					getShell(), labelProvider);
			dialog.setTitle(LaunchMessages.CMainTab_Project_Selection);
			dialog.setMessage(LaunchMessages.CMainTab_Choose_project_to_constrain_search_for_program);
			dialog.setElements(projects);

			ICProject cProject = getCProject();
			if (cProject != null) {
				dialog.setInitialSelections(new Object[] { cProject });
			}
			if (dialog.open() == Window.OK) {
				return (ICProject) dialog.getFirstResult();
			}
		} catch (CModelException e) {
			LaunchUIPlugin.errorDialog("Launch UI internal error", e); //$NON-NLS-1$			
		}
		return null;
	}

	/**
	 * Return an array a ICProject whose platform match that of the runtime env.
	 */
	protected ICProject[] getCProjects() throws CModelException {
		ICProject cproject[] = CoreModel.getDefault().getCModel()
				.getCProjects();
		ArrayList<ICProject> list = new ArrayList<ICProject>(cproject.length);

		for (ICProject element : cproject) {
			ICDescriptor cdesciptor = null;
			try {
				cdesciptor = CCorePlugin.getDefault().getCProjectDescription(
						(IProject) element.getResource(), false);
				if (cdesciptor != null) {
					String projectPlatform = cdesciptor.getPlatform();
					if (this.filterPlatform.equals("*") //$NON-NLS-1$
							|| projectPlatform.equals("*") //$NON-NLS-1$
							|| this.filterPlatform
									.equalsIgnoreCase(projectPlatform) == true) {
						list.add(element);
					}
				} else {
					list.add(element);
				}
			} catch (CoreException e) {
				list.add(element);
			}
		}
		return list.toArray(new ICProject[list.size()]);
	}

	/**
	 * Return the ICProject corresponding to the project name in the project
	 * name text field, or null if the text does not match a project name.
	 */
	protected ICProject getCProject() {
		String projectName = this.fProjText.getText().trim();
		if (projectName.length() < 1) {
			return null;
		}
		return CoreModel.getDefault().getCModel().getCProject(projectName);
	}

	private boolean validateFields() {
		{
			String fProjTextValue = null;

			if (null != this.fProjText) {
				fProjTextValue = this.fProjText.getText();
			}

			if ((null == this.fProjText) || (null == fProjTextValue)
					|| ("".equals(fProjTextValue))) {
				setErrorMessage("Value for project is not valid.");
				return false;
			}
		}

		{
			String fRegistryTextValue = null;

			if (null != this.fRegistryText) {
				fRegistryTextValue = this.fRegistryText.getText();
			}

			if ((null == this.fRegistryText) || (null == fRegistryTextValue)
					|| ("".equals(fRegistryTextValue))) {
				setErrorMessage("Value for registry directory is not valid.");
				return false;
			}
		}

		{
			String fDaemonNameTextValue = null;

			if (null != this.fDaemonNameText) {
				fDaemonNameTextValue = this.fDaemonNameText.getText();
			}

			if ((null == this.fDaemonNameText)
					|| (null == fDaemonNameTextValue)
					|| ("".equals(fDaemonNameTextValue))) {
				setErrorMessage("Value for simulation platform daemon is not valid.");
				return false;
			}
		}

		{
			String fSimNameTextValue = null;

			if (null != this.fSimNameText) {
				fSimNameTextValue = this.fSimNameText.getText();
			}

			if ((null == this.fSimNameText) || (null == fSimNameTextValue)
					|| ("".equals(fSimNameTextValue))) {
				setErrorMessage("Value for simulation is not valid.");
				return false;
			}
		}

		{
			String fProgTextValue = null;

			if (null != this.fProgText) {
				fProgTextValue = this.fProgText.getText();
			}

			if ((null == this.fProgText) || (null == fProgTextValue)
					|| ("".equals(fProgTextValue))) {
				setErrorMessage("Value for binary is not valid.");
				return false;
			}
		}

		return true;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.debug.ui.ILaunchConfigurationTab#isValid(org.eclipse.debug
	 * .core.ILaunchConfiguration)
	 */
	@Override
	public boolean isValid(ILaunchConfiguration config) {

		setErrorMessage(null);
		setMessage(null);

		if (!validateFields()) {
			return false;
		}

		if (this.dontCheckProgram) {
			return true;
		}

		String name = this.fProjText.getText().trim();
		if (name.length() == 0) {
			setErrorMessage(LaunchMessages.CMainTab_Project_not_specified);
			return false;
		}
		if (!ResourcesPlugin.getWorkspace().getRoot().getProject(name).exists()) {
			setErrorMessage(LaunchMessages.Launch_common_Project_does_not_exist);
			return false;
		}
		IProject project = ResourcesPlugin.getWorkspace().getRoot()
				.getProject(name);
		if (!project.isOpen()) {
			setErrorMessage(LaunchMessages.CMainTab_Project_must_be_opened);
			return false;
		}

		// Validate simulation platform registry location
		{
			String registryLocation = this.fRegistryText.getText().trim();
			File registryDirectory = null;

			if (registryLocation.length() == 0) {
				setErrorMessage("Simulation platform registry not specified");
				return false;
			}

			registryDirectory = new File(registryLocation);

			if (!registryDirectory.exists() || !registryDirectory.isDirectory()) {
				setErrorMessage("Simulation platform registry directory does not exist.");
				return false;
			}
		}
		name = this.fProgText.getText().trim();
		if (name.length() == 0) {
			setErrorMessage(LaunchMessages.CMainTab_Program_not_specified);
			return false;
		}
		if (name.equals(".") || name.equals("..")) { //$NON-NLS-1$ //$NON-NLS-2$
			setErrorMessage(LaunchMessages.CMainTab_Program_does_not_exist);
			return false;
		}
		IPath exePath = new Path(name);
		if (!exePath.isAbsolute()) {
			IFile projFile = null;
			try {
				projFile = project.getFile(name);
			} catch (Exception exc) {
				// throws an exception if it's a relative path pointing outside
				// project
				setErrorMessage(LaunchMessages.CMainTab_Program_invalid_proj_path);
				return false;
			}
			if (projFile == null || !projFile.exists()) {
				setErrorMessage(LaunchMessages.CMainTab_Program_does_not_exist);
				return false;
			}
			exePath = projFile.getLocation();
		} else {
			if (!exePath.toFile().exists()) {
				setErrorMessage(LaunchMessages.CMainTab_Program_does_not_exist);
				return false;
			}
		}
		try {
			if (!isBinary(project, exePath)) {
				setErrorMessage(LaunchMessages.CMainTab_Program_does_not_exist);
				return false;
			}
		} catch (CoreException e) {
			LaunchUIPlugin.log(e);
			setErrorMessage(e.getLocalizedMessage());
			return false;
		}

		return true;
	}

	@Override
	public boolean canSave() {
		final boolean canSuperSave = super.canSave();

		if (!validateFields()) {
			return false;
		}

		return canSuperSave;
	}

	/**
	 * @param project
	 * @param exePath
	 * @return
	 * @throws CoreException
	 */
	protected boolean isBinary(IProject project, IPath exePath)
			throws CoreException {
		ICExtensionReference[] parserRef = CCorePlugin.getDefault()
				.getBinaryParserExtensions(project);
		for (ICExtensionReference element : parserRef) {
			try {
				IBinaryParser parser = (IBinaryParser) element
						.createExtension();
				IBinaryObject exe = (IBinaryObject) parser.getBinary(exePath);
				if (exe != null) {
					return true;
				}
			} catch (ClassCastException e) {
			} catch (IOException e) {
			}
		}
		IBinaryParser parser = CCorePlugin.getDefault()
				.getDefaultBinaryParser();
		try {
			IBinaryObject exe = (IBinaryObject) parser.getBinary(exePath);
			return exe != null;
		} catch (ClassCastException e) {
		} catch (IOException e) {
		}
		return false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.debug.ui.ILaunchConfigurationTab#setDefaults(org.eclipse.
	 * debug.core.ILaunchConfigurationWorkingCopy)
	 */
	@Override
	public void setDefaults(ILaunchConfigurationWorkingCopy config) {
		// We set empty attributes for project & program so that when one config
		// is
		// compared to another, the existence of empty attributes doesn't cause
		// an
		// incorrect result (the performApply() method can result in empty
		// values
		// for these attributes being set on a config if there is nothing in the
		// corresponding text boxes)
		// plus getContext will use this to base context from if set.
		config.setAttribute(ICDTLaunchConfigurationConstants.ATTR_PROJECT_NAME,
				Utilities.EMPTY_STRING);
		ICElement cElement = null;
		cElement = getContext(config, getPlatform(config));
		if (cElement != null) {
			initializeCProject(cElement, config);
			initializeProgramName(cElement, config);
		}
		if (wantsTerminalOption()) {
			config.setAttribute(
					ICDTLaunchConfigurationConstants.ATTR_USE_TERMINAL,
					ICDTLaunchConfigurationConstants.USE_TERMINAL_DEFAULT);
		}

		// Set default for simulation platform registry location, daemon name
		// and simulation name.
		config.setAttribute(
				IUmfCLaunchConfigurationConstants.ATTR_REGISTRY_LOCATION,
				Utilities.EMPTY_STRING);
		config.setAttribute(IUmfCLaunchConfigurationConstants.ATTR_DAEMON_NAME,
				Utilities.DEFAULT_DAEMON_NAME);
		config.setAttribute(
				IUmfCLaunchConfigurationConstants.ATTR_SIMULATION_NAME,
				Utilities.DEFAULT_SIMULATION_NAME);
	}

	/**
	 * Set the program name attributes on the working copy based on the
	 * ICElement
	 */
	protected void initializeProgramName(ICElement cElement,
			ILaunchConfigurationWorkingCopy config) {

		boolean renamed = false;

		if (!(cElement instanceof IBinary)) {
			cElement = cElement.getCProject();
		}

		if (cElement instanceof ICProject) {

			IProject project = cElement.getCProject().getProject();
			String name = project.getName();
			ICProjectDescription projDes = CCorePlugin.getDefault()
					.getProjectDescription(project);
			if (projDes != null) {
				String buildConfigName = projDes.getActiveConfiguration()
						.getName();
				// bug 234951
				name = MessageFormat.format(
						LaunchMessages.CMainTab_Configuration_name,
						(Object[]) new String[] { name, buildConfigName });
			}
			name = getLaunchConfigurationDialog().generateName(name);
			config.rename(name);
			renamed = true;
		}

		IBinary binary = null;
		if (cElement instanceof ICProject) {
			IBinary[] bins = getBinaryFiles((ICProject) cElement);
			if (bins != null && bins.length == 1) {
				binary = bins[0];
			}
		} else if (cElement instanceof IBinary) {
			binary = (IBinary) cElement;
		}

		if (binary != null) {
			String path;
			path = binary.getResource().getProjectRelativePath().toOSString();
			config.setAttribute(
					ICDTLaunchConfigurationConstants.ATTR_PROGRAM_NAME, path);
			if (!renamed) {
				String name = binary.getElementName();
				int index = name.lastIndexOf('.');
				if (index > 0) {
					name = name.substring(0, index);
				}
				name = getLaunchConfigurationDialog().generateName(name);
				config.rename(name);
				renamed = true;
			}
		}

		if (!renamed) {
			String name = getLaunchConfigurationDialog().generateName(
					cElement.getCProject().getElementName());
			config.rename(name);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.debug.ui.ILaunchConfigurationTab#getName()
	 */
	@Override
	public String getName() {
		return LaunchMessages.CMainTab_Main;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.debug.ui.ILaunchConfigurationTab#getImage()
	 */
	@Override
	public Image getImage() {
		return LaunchImages.get(LaunchImages.IMG_VIEW_MAIN_TAB);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.debug.ui.AbstractLaunchConfigurationTab#
	 * updateLaunchConfigurationDialog()
	 */
	@Override
	protected void updateLaunchConfigurationDialog() {
		super.updateLaunchConfigurationDialog();
	}

	protected void handleRegistryTextModified() {
		final String registryLocation = this.fRegistryText.getText();
		final File registryDirectory = new File(registryLocation);
		final File simulationPlatformBinary = new File(registryDirectory,
				SIMHOST_RELATIVE_PATH);

		if (registryDirectory.exists() && registryDirectory.isDirectory()
				&& simulationPlatformBinary.exists()
				&& simulationPlatformBinary.isFile()) {
			this.fProgText.setText(simulationPlatformBinary.getAbsolutePath());
		}
	}
}

// -----------------------------------------------------------------------------
// $Log: UmfCMainTab.java,v $
// Revision 1.1 2010-09-30 15:48:08 stp
// Released.
//
