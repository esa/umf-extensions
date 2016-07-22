/*******************************************************************************
 * Copyright (c) 2007, 2008 Intel Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Intel Corporation - Initial API and implementation
 * IBM Corporation
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
// by Intel Corporation and others and which have been distributed under
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
// Intel Corporation and others and which have been modified and distributed
// by Terma GmbH under the terms of the Eclipse Public License v1.0
// (see the copyright statements above).
//
// It has been MODIFIED by VEGA Space GmbH and is being re-destributed under 
// the terms of the Eclipse Public License v1.0. 
//-----------------------------------------------------------------------------
//-----------------------------------------------------------------------------

//NOTICE: This class is a customised copy of
//org.eclipse.cdt.managedbuilder.ui.properties.BuilderSettingsTab (cf. copyright
//notice above).

package org.eclipse.cdt.managedbuilder.ui.properties;

import org.eclipse.cdt.core.settings.model.ICResourceDescription;
import org.eclipse.cdt.managedbuilder.core.IBuilder;
import org.eclipse.cdt.managedbuilder.core.IConfiguration;
import org.eclipse.cdt.managedbuilder.core.IMultiConfiguration;
import org.eclipse.cdt.managedbuilder.internal.core.Builder;
import org.eclipse.cdt.managedbuilder.internal.core.Configuration;
import org.eclipse.cdt.managedbuilder.internal.core.MultiConfiguration;
import org.eclipse.cdt.managedbuilder.internal.ui.Messages;
import org.eclipse.cdt.ui.newui.AbstractCPropertyTab;
import org.eclipse.cdt.ui.newui.ICPropertyProvider;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.Widget;

public class CustomisedBuilderSettingsTab extends AbstractCBuildPropertyTab {
	// Widgets
	// 1
	private Button b_useDefault;
	private Combo c_builderType;
	private Text t_buildCmd;
	// 2
	private Button b_genMakefileAuto;
	private Button b_expandVars;
	// 5
	private Text t_dir;
	private Button b_dirWsp;
	private Button b_dirFile;
	private Button b_dirVars;
	private Group group_dir;

	private IBuilder bldr;
	private IConfiguration icfg;
	private boolean canModify = true;

	@Override
	public void createControls(Composite parent) {
		super.createControls(parent);
		this.usercomp.setLayout(new GridLayout(1, false));

		// Builder group
		Group g1 = setupGroup(this.usercomp, Messages.BuilderSettingsTab_0, 3,
				GridData.FILL_HORIZONTAL);
		setupLabel(g1, Messages.BuilderSettingsTab_1, 1, GridData.BEGINNING);
		this.c_builderType = new Combo(g1, SWT.READ_ONLY | SWT.DROP_DOWN
				| SWT.BORDER);
		setupControl(this.c_builderType, 2, GridData.FILL_HORIZONTAL);
		this.c_builderType.add(Messages.BuilderSettingsTab_2);
		this.c_builderType.add(Messages.BuilderSettingsTab_3);
		this.c_builderType.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent event) {
				enableInternalBuilder(CustomisedBuilderSettingsTab.this.c_builderType
						.getSelectionIndex() == 1);
				updateButtons();
			}
		});

		this.b_useDefault = setupCheck(g1, Messages.BuilderSettingsTab_4, 3,
				GridData.BEGINNING);

		setupLabel(g1, Messages.BuilderSettingsTab_5, 1, GridData.BEGINNING);
		this.t_buildCmd = setupBlock(g1, this.b_useDefault);
		this.t_buildCmd.addModifyListener(new ModifyListener() {
			@Override
			public void modifyText(ModifyEvent e) {
				if (!CustomisedBuilderSettingsTab.this.canModify) {
					return;
				}
				String fullCommand = CustomisedBuilderSettingsTab.this.t_buildCmd
						.getText().trim();
				String buildCommand = parseMakeCommand(fullCommand);
				String buildArgs = fullCommand.substring(buildCommand.length())
						.trim();
				if (!buildCommand.equals(CustomisedBuilderSettingsTab.this.bldr
						.getCommand())
						|| !buildArgs
								.equals(CustomisedBuilderSettingsTab.this.bldr
										.getArguments())) {
					setCommand(buildCommand);
					setArguments(buildArgs);
				}
			}
		});

		Group g2 = setupGroup(this.usercomp, Messages.BuilderSettingsTab_6, 2,
				GridData.FILL_HORIZONTAL);
		((GridLayout) (g2.getLayout())).makeColumnsEqualWidth = true;

		this.b_genMakefileAuto = setupCheck(g2, Messages.BuilderSettingsTab_7,
				1, GridData.BEGINNING);
		this.b_expandVars = setupCheck(g2, Messages.BuilderSettingsTab_8, 1,
				GridData.BEGINNING);

		// Build location group
		this.group_dir = setupGroup(this.usercomp,
				Messages.BuilderSettingsTab_21, 2, GridData.FILL_HORIZONTAL);
		setupLabel(this.group_dir, Messages.BuilderSettingsTab_22, 1,
				GridData.BEGINNING);
		this.t_dir = setupText(this.group_dir, 1, GridData.FILL_HORIZONTAL);
		this.t_dir.addModifyListener(new ModifyListener() {
			@Override
			public void modifyText(ModifyEvent e) {
				if (CustomisedBuilderSettingsTab.this.canModify) {
					setBuildPath(CustomisedBuilderSettingsTab.this.t_dir
							.getText());
				}
			}
		});
		Composite c = new Composite(this.group_dir, SWT.NONE);
		setupControl(c, 2, GridData.FILL_HORIZONTAL);
		GridLayout f = new GridLayout(4, false);
		c.setLayout(f);
		Label dummy = new Label(c, 0);
		dummy.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		this.b_dirWsp = setupBottomButton(c, WORKSPACEBUTTON_NAME);
		this.b_dirFile = setupBottomButton(c, FILESYSTEMBUTTON_NAME);
		this.b_dirVars = setupBottomButton(c, VARIABLESBUTTON_NAME);
	}

	private void setManagedBuild(boolean enable) {
		setManagedBuildOn(enable);
		this.page.informPages(MANAGEDBUILDSTATE, null);
		updateButtons();
	}

	/**
	 * sets widgets states
	 */
	@Override
	protected void updateButtons() {
		this.bldr = this.icfg.getEditableBuilder();

		this.canModify = false; // avoid extra update from modifyListeners
		int[] extStates = CustomisedBuildBehaviourTab.calc3states(this.page,
				this.icfg, 0);

		this.b_genMakefileAuto.setEnabled(this.icfg.supportsBuild(true));
		if (extStates == null) { // no extended states available
			AbstractCPropertyTab.setTriSelection(this.b_genMakefileAuto,
					this.bldr.isManagedBuildOn());
			AbstractCPropertyTab.setTriSelection(this.b_useDefault,
					this.bldr.isDefaultBuildCmd());
			// b_expandVars.setGrayed(false);
			if (!this.bldr.canKeepEnvironmentVariablesInBuildfile()) {
				this.b_expandVars.setEnabled(false);
			} else {
				this.b_expandVars.setEnabled(true);
				AbstractCPropertyTab.setTriSelection(this.b_expandVars,
						!this.bldr.keepEnvironmentVariablesInBuildfile());
			}
		} else {
			AbstractCPropertyTab.setTriSelection(this.b_genMakefileAuto,
					extStates[0]);
			AbstractCPropertyTab.setTriSelection(this.b_useDefault,
					extStates[1]);
			if (extStates[2] != AbstractCPropertyTab.TRI_YES) {
				this.b_expandVars.setEnabled(false);
			} else {
				this.b_expandVars.setEnabled(true);
				AbstractCPropertyTab.setTriSelection(this.b_expandVars,
						extStates[3]);
			}
		}
		this.c_builderType.select(isInternalBuilderEnabled() ? 1 : 0);
		this.c_builderType.setEnabled(canEnableInternalBuilder(true)
				&& canEnableInternalBuilder(false));

		this.t_buildCmd.setText(getMC());

		if (this.page.isMultiCfg()) {
			this.group_dir.setVisible(false);
		} else {
			this.group_dir.setVisible(true);
			this.t_dir.setText(this.bldr.getBuildPath());
			boolean mbOn = this.bldr.isManagedBuildOn();
			this.t_dir.setEnabled(!mbOn);
			this.b_dirVars.setEnabled(!mbOn);
			this.b_dirWsp.setEnabled(!mbOn);
			this.b_dirFile.setEnabled(!mbOn);
		}
		boolean external = (this.c_builderType.getSelectionIndex() == 0);

		this.b_useDefault.setEnabled(external);
		this.t_buildCmd.setEnabled(external);
		((Control) this.t_buildCmd.getData()).setEnabled(external
				& !this.b_useDefault.getSelection());

		this.b_genMakefileAuto.setEnabled(external
				&& this.icfg.supportsBuild(true));
		if (this.b_expandVars.getEnabled()) {
			this.b_expandVars.setEnabled(external
					&& this.b_genMakefileAuto.getSelection());
		}

		if (external) { // just set relatet text widget state,
			checkPressed(this.b_useDefault, false); // do not update
		}
		this.canModify = true;
	}

	private Button setupBottomButton(Composite c, String name) {
		Button b = new Button(c, SWT.PUSH);
		b.setText(name);
		GridData fd = new GridData(GridData.CENTER);
		fd.minimumWidth = BUTTON_WIDTH;
		b.setLayoutData(fd);
		b.setData(this.t_dir);
		b.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent event) {
				buttonVarPressed(event);
			}
		});
		return b;
	}

	/**
	 * Sets up text + corresponding button Checkbox can be implemented either by
	 * Button or by TriButton
	 */
	private Text setupBlock(Composite c, Control check) {
		Text t = setupText(c, 1, GridData.FILL_HORIZONTAL);
		Button b = setupButton(c, VARIABLESBUTTON_NAME, 1, GridData.END);
		b.setData(t); // to get know which text is affected
		t.setData(b); // to get know which button to enable/disable
		b.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent event) {
				buttonVarPressed(event);
			}
		});
		if (check != null) {
			check.setData(t);
		}
		return t;
	}

	/*
	 * Unified handler for "Variables" buttons
	 */
	private void buttonVarPressed(SelectionEvent e) {
		Widget b = e.widget;
		if (b == null || b.getData() == null) {
			return;
		}
		if (b.getData() instanceof Text) {
			String x = null;
			if (b.equals(this.b_dirWsp)) {
				x = getWorkspaceDirDialog(this.usercomp.getShell(), EMPTY_STR);
				if (x != null) {
					((Text) b.getData()).setText(x);
				}
			} else if (b.equals(this.b_dirFile)) {
				x = getFileSystemDirDialog(this.usercomp.getShell(), EMPTY_STR);
				if (x != null) {
					((Text) b.getData()).setText(x);
				}
			} else {
				x = AbstractCPropertyTab.getVariableDialog(this.usercomp
						.getShell(), getResDesc().getConfiguration());
				if (x != null) {
					((Text) b.getData()).insert(x);
				}
			}
		}
	}

	@Override
	public void checkPressed(SelectionEvent e) {
		checkPressed((Control) e.widget, true);
		updateButtons();
	}

	private void checkPressed(Control b, boolean needUpdate) {
		if (b == null) {
			return;
		}

		boolean val = false;
		if (b instanceof Button) {
			val = ((Button) b).getSelection();
		}

		if (b.getData() instanceof Text) {
			Text t = (Text) b.getData();
			if (b == this.b_useDefault) {
				val = !val;
			}
			t.setEnabled(val);
			if (t.getData() != null && t.getData() instanceof Control) {
				Control c = (Control) t.getData();
				c.setEnabled(val);
			}
		}
		// call may be used just to set text state above
		// in this case, settings update is not required
		if (!needUpdate) {
			return;
		}

		if (b == this.b_useDefault) {
			setUseDefaultBuildCmd(!val);
		} else if (b == this.b_genMakefileAuto) {
			setManagedBuild(val);
		} else if (b == this.b_expandVars) {
			if (this.bldr.canKeepEnvironmentVariablesInBuildfile()) {
				setKeepEnvironmentVariablesInBuildfile(!val);
			}
		}
	}

	/**
	 * get make command
	 * 
	 * @return
	 */
	private String getMC() {
		String makeCommand = this.bldr.getCommand();
		String makeArgs = this.bldr.getArguments();
		if (makeArgs != null) {
			makeCommand += " " + makeArgs;} //$NON-NLS-1$
		return makeCommand;
	}

	/**
	 * Performs common settings for all controls (Copy from config to widgets)
	 * 
	 * @param cfgd
	 *            -
	 */

	@Override
	public void updateData(ICResourceDescription cfgd) {
		if (cfgd == null) {
			return;
		}
		this.icfg = getCfg(cfgd.getConfiguration());
		updateButtons();
	}

	@Override
	public void performApply(ICResourceDescription src,
			ICResourceDescription dst) {
		CustomisedBuildBehaviourTab.apply(src, dst, this.page.isMultiCfg());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @param string
	 * 
	 * @return
	 */
	private String parseMakeCommand(String rawCommand) {
		String[] result = rawCommand.split("\\s"); //$NON-NLS-1$
		if (result != null && result.length > 0) {
			return result[0];
		} else {
			return rawCommand;
		}

	}

	// This page can be displayed for project only
	@Override
	public boolean canBeVisible() {
		return this.page.isForProject() || this.page.isForPrefs();
	}

	@Override
	public void setVisible(boolean b) {
		super.setVisible(b);
	}

	@Override
	protected void performDefaults() {
		if (this.icfg instanceof IMultiConfiguration) {
			IConfiguration[] cfs = (IConfiguration[]) ((IMultiConfiguration) this.icfg)
					.getItems();
			for (IConfiguration cf : cfs) {
				IBuilder b = cf.getEditableBuilder();

				DefaultBuildPathWorkaround.fixDefaultBuildPath(
						b.getSuperClass(), this.page);

				CustomisedBuildBehaviourTab.copyBuilders(b.getSuperClass(), b);
			}
		} else {
			DefaultBuildPathWorkaround.fixDefaultBuildPath(
					this.bldr.getSuperClass(), this.page);
			CustomisedBuildBehaviourTab.copyBuilders(this.bldr.getSuperClass(),
					this.bldr);
		}
		updateData(getResDesc());
	}

	private boolean canEnableInternalBuilder(boolean v) {
		if (this.icfg instanceof Configuration) {
			return ((Configuration) this.icfg).canEnableInternalBuilder(v);
		}
		if (this.icfg instanceof IMultiConfiguration) {
			return ((IMultiConfiguration) this.icfg)
					.canEnableInternalBuilder(v);
		}
		return false;
	}

	private void enableInternalBuilder(boolean v) {
		if (this.icfg instanceof Configuration) {
			((Configuration) this.icfg).enableInternalBuilder(v);
		}
		if (this.icfg instanceof IMultiConfiguration) {
			((IMultiConfiguration) this.icfg).enableInternalBuilder(v);
		}
	}

	private boolean isInternalBuilderEnabled() {
		if (this.icfg instanceof Configuration) {
			return ((Configuration) this.icfg).isInternalBuilderEnabled();
		}
		if (this.icfg instanceof IMultiConfiguration) {
			return ((MultiConfiguration) this.icfg).isInternalBuilderEnabled();
		}
		return false;
	}

	private void setUseDefaultBuildCmd(boolean val) {
		try {
			if (this.icfg instanceof IMultiConfiguration) {
				IConfiguration[] cfs = (IConfiguration[]) ((IMultiConfiguration) this.icfg)
						.getItems();
				for (IConfiguration cf : cfs) {
					IBuilder b = cf.getEditableBuilder();
					if (b != null) {
						b.setUseDefaultBuildCmd(val);
					}
				}
			} else {
				this.icfg.getEditableBuilder().setUseDefaultBuildCmd(val);
			}
		} catch (CoreException e) {
			ManagedBuilderUIPlugin.log(e);
		}
	}

	private void setKeepEnvironmentVariablesInBuildfile(boolean val) {
		if (this.icfg instanceof IMultiConfiguration) {
			IConfiguration[] cfs = (IConfiguration[]) ((IMultiConfiguration) this.icfg)
					.getItems();
			for (IConfiguration cf : cfs) {
				IBuilder b = cf.getEditableBuilder();
				if (b != null) {
					b.setKeepEnvironmentVariablesInBuildfile(val);
				}
			}
		} else {
			this.icfg.getEditableBuilder()
					.setKeepEnvironmentVariablesInBuildfile(val);
		}
	}

	private void setCommand(String buildCommand) {
		if (this.icfg instanceof IMultiConfiguration) {
			IConfiguration[] cfs = (IConfiguration[]) ((IMultiConfiguration) this.icfg)
					.getItems();
			for (IConfiguration cf : cfs) {
				IBuilder b = cf.getEditableBuilder();
				b.setCommand(buildCommand);
			}
		} else {
			this.icfg.getEditableBuilder().setCommand(buildCommand);
		}
	}

	private void setArguments(String makeArgs) {
		if (this.icfg instanceof IMultiConfiguration) {
			IConfiguration[] cfs = (IConfiguration[]) ((IMultiConfiguration) this.icfg)
					.getItems();
			for (IConfiguration cf : cfs) {
				IBuilder b = cf.getEditableBuilder();
				b.setArguments(makeArgs);
			}
		} else {
			this.icfg.getEditableBuilder().setArguments(makeArgs);
		}
	}

	private void setBuildPath(String path) {
		if (this.icfg instanceof IMultiConfiguration) {
			IConfiguration[] cfs = (IConfiguration[]) ((IMultiConfiguration) this.icfg)
					.getItems();
			for (IConfiguration cf : cfs) {
				IBuilder b = cf.getEditableBuilder();
				b.setBuildPath(path);
			}
		} else {
			this.icfg.getEditableBuilder().setBuildPath(path);
		}
	}

	private void setManagedBuildOn(boolean on) {
		try {
			if (this.icfg instanceof IMultiConfiguration) {
				IConfiguration[] cfs = (IConfiguration[]) ((IMultiConfiguration) this.icfg)
						.getItems();
				for (IConfiguration cf : cfs) {
					IBuilder b = cf.getEditableBuilder();
					b.setManagedBuildOn(on);
				}
			} else {
				this.icfg.getEditableBuilder().setManagedBuildOn(on);
			}
		} catch (CoreException e) {
			ManagedBuilderUIPlugin.log(e);
		}
	}

	// NOTICE: This API provides access to private data of
	// CustomisedBuilderSettingsTab
	// that are needed by our clients (in this case the subclasses).
	// Using this approach instead of just changing the visibility of such data
	// we make clear the additional access that is needed by our clients.
	public class WorkaroundExtendedAPI {
		public String getBuildDirectoryText() {
			if (null == CustomisedBuilderSettingsTab.this.t_dir) {
				return null;
			}

			{
				final String buildPath = CustomisedBuilderSettingsTab.this.t_dir
						.getText();

				return buildPath;
			}
		}

		public void setBuildDirectoryText(String buildPath) {
			if (null == CustomisedBuilderSettingsTab.this.t_dir) {
				return;
			}

			CustomisedBuilderSettingsTab.this.t_dir.setText(buildPath);
		}
	}

	static class DefaultBuildPathWorkaround {
		private static final String DEFAULT_BUILD_FOLDER = "build";

		private static final String WORKSPACE_LOC_PREFIX = "${workspace_loc:/";
		private static final String WORKSPACE_LOC_SEPARATOR = "/";
		private static final String WORKSPACE_LOC_SUFFIX = "}";

		private static String getProjectName(
				ICPropertyProvider cPropertyProvider) {
			final IProject project = cPropertyProvider.getProject();
			final String projectName = project.getName();

			return projectName;
		}

		static void fixDefaultBuildPath(IBuilder builder,
				ICPropertyProvider cPropertyProvider) {
			if (!(builder instanceof Builder)) {
				return;
			}

			{
				final Builder builderImplementation = (Builder) builder;
				final String buildPath = builderImplementation
						.getBuildPathAttribute();

				if (null == buildPath) {
					final String projectName = getProjectName(cPropertyProvider);
					final String defaultUMFBuildPath = getDefaultBuildPath(projectName);

					builderImplementation
							.setBuildPathAttribute(defaultUMFBuildPath);
					builderImplementation.setBuildPath(defaultUMFBuildPath);
				}
			}
		}

		private static String getProjectSubfolderInWorkspaceLoc(
				String projectName, String subfolderName) {
			final String subfolderInWorkspaceLoc = WORKSPACE_LOC_PREFIX
					+ projectName + WORKSPACE_LOC_SEPARATOR + subfolderName
					+ WORKSPACE_LOC_SUFFIX;

			return subfolderInWorkspaceLoc;
		}

		private static String getDefaultBuildPath(String projectName) {
			final String buildPath = getProjectSubfolderInWorkspaceLoc(
					projectName, DEFAULT_BUILD_FOLDER);

			return buildPath;
		}

	}
}

// -----------------------------------------------------------------------------
// $Log: CustomisedBuilderSettingsTab.java,v $
// Revision 1.1 2010-09-30 15:48:08 stp
// Released.
//
