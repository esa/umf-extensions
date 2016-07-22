/*******************************************************************************
 * Copyright (c) 2007, 2008  Intel Corporation and others.
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

//NOTICE: Apart from changes in the class name and visibility, as well as in some
//methods' visibilities this class is a copy of
//org.eclipse.cdt.managedbuilder.ui.properties.BuildBehaviourTab (cf. copyright
//notice above).
//It has been taken over into the present project because some of its methods
//with package visibility are being accessed by the customised version CustomisedBuilderSettingsTab
//of org.eclipse.cdt.managedbuilder.ui.properties.BuilderSettingsTab.
//Notice that CustomisedBuilderSettingsTab cannot access the methods of the original
//BuildBehaviourTab: even if the two classes belong to the same package, they
//are loaded by different class loaders since they reside in different plugins.

package org.eclipse.cdt.managedbuilder.ui.properties;

import org.eclipse.cdt.core.settings.model.ICConfigurationDescription;
import org.eclipse.cdt.core.settings.model.ICMultiConfigDescription;
import org.eclipse.cdt.core.settings.model.ICMultiItemsHolder;
import org.eclipse.cdt.core.settings.model.ICResourceDescription;
import org.eclipse.cdt.managedbuilder.core.IBuilder;
import org.eclipse.cdt.managedbuilder.core.IConfiguration;
import org.eclipse.cdt.managedbuilder.core.IMultiConfiguration;
import org.eclipse.cdt.managedbuilder.internal.buildmodel.BuildProcessManager;
import org.eclipse.cdt.managedbuilder.internal.core.Builder;
import org.eclipse.cdt.managedbuilder.internal.core.Configuration;
import org.eclipse.cdt.managedbuilder.internal.core.MultiConfiguration;
import org.eclipse.cdt.managedbuilder.internal.ui.Messages;
import org.eclipse.cdt.newmake.core.IMakeBuilderInfo;
import org.eclipse.cdt.ui.newui.AbstractCPropertyTab;
import org.eclipse.cdt.ui.newui.ICPropertyProvider;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.swt.SWT;
import org.eclipse.swt.accessibility.AccessibleAdapter;
import org.eclipse.swt.accessibility.AccessibleEvent;
import org.eclipse.swt.accessibility.AccessibleListener;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Spinner;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.Widget;

public class CustomisedBuildBehaviourTab extends AbstractCBuildPropertyTab {

	private static final int TRI_STATES_SIZE = 4;
	// Widgets
	// 3
	private Button b_stopOnError; // 3
	private Button b_parallel; // 3

	private Button b_parallelOpt;
	private Button b_parallelNum;
	private Spinner parallelProcesses;

	private Label title2;
	private Button b_autoBuild; // 3
	private Text t_autoBuild;
	private Button b_cmdBuild; // 3
	private Text t_cmdBuild;
	private Button b_cmdClean; // 3
	private Text t_cmdClean;

	private IBuilder bldr;
	private IConfiguration icfg;
	private boolean canModify = true;

	protected final int cpuNumber = BuildProcessManager.checkCPUNumber();

	@Override
	public void createControls(Composite parent) {
		super.createControls(parent);
		this.usercomp.setLayout(new GridLayout(1, false));

		// Build setting group
		Group g3 = setupGroup(this.usercomp, Messages.BuilderSettingsTab_9, 2,
				GridData.FILL_HORIZONTAL);
		GridLayout gl = new GridLayout(2, true);
		gl.verticalSpacing = 0;
		gl.marginWidth = 0;
		g3.setLayout(gl);

		Composite c1 = new Composite(g3, SWT.NONE);
		setupControl(c1, 1, GridData.FILL_BOTH);
		GridData gd = (GridData) c1.getLayoutData();
		gd.verticalSpan = 2;
		gd.verticalIndent = 0;
		c1.setLayoutData(gd);
		gl = new GridLayout(1, false);
		gl.verticalSpacing = 0;
		gl.marginWidth = 0;
		gl.marginHeight = 0;
		c1.setLayout(gl);

		this.b_stopOnError = setupCheck(c1, Messages.BuilderSettingsTab_10, 1,
				GridData.BEGINNING);

		Composite c2 = new Composite(g3, SWT.NONE);
		setupControl(c2, 1, GridData.FILL_BOTH);
		gl = new GridLayout(1, false);
		gl.verticalSpacing = 0;
		gl.marginWidth = 0;
		gl.marginHeight = 0;
		c2.setLayout(gl);

		this.b_parallel = setupCheck(c2, Messages.BuilderSettingsTab_11, 1,
				GridData.BEGINNING);

		Composite c3 = new Composite(g3, SWT.NONE);
		setupControl(c3, 1, GridData.FILL_BOTH);
		gl = new GridLayout(2, false);
		gl.verticalSpacing = 0;
		gl.marginWidth = 0;
		gl.marginHeight = 0;
		c3.setLayout(gl);

		this.b_parallelOpt = new Button(c3, SWT.RADIO);
		this.b_parallelOpt.setText(Messages.BuilderSettingsTab_12);
		setupControl(this.b_parallelOpt, 2, GridData.BEGINNING);
		((GridData) (this.b_parallelOpt.getLayoutData())).horizontalIndent = 15;
		this.b_parallelOpt.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent event) {
				setParallelDef(CustomisedBuildBehaviourTab.this.b_parallelOpt
						.getSelection());
				updateButtons();
			}
		});

		this.b_parallelNum = new Button(c3, SWT.RADIO);
		this.b_parallelNum.setText(Messages.BuilderSettingsTab_13);
		setupControl(this.b_parallelNum, 1, GridData.BEGINNING);
		((GridData) (this.b_parallelNum.getLayoutData())).horizontalIndent = 15;
		this.b_parallelNum.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent event) {
				setParallelDef(!CustomisedBuildBehaviourTab.this.b_parallelNum
						.getSelection());
				updateButtons();
			}
		});

		this.parallelProcesses = new Spinner(c3, SWT.BORDER);
		setupControl(this.parallelProcesses, 1, GridData.BEGINNING);
		this.parallelProcesses.setValues(this.cpuNumber, 1, 10000, 0, 1, 10);
		this.parallelProcesses.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				setParallelNumber(CustomisedBuildBehaviourTab.this.parallelProcesses
						.getSelection());
				updateButtons();
			}
		});

		// Workbench behaviour group
		AccessibleListener makeTargetLabelAccessibleListener = new AccessibleAdapter() {
			@Override
			public void getName(AccessibleEvent e) {
				e.result = Messages.BuilderSettingsTab_16;
			}
		};
		Group g4 = setupGroup(this.usercomp, Messages.BuilderSettingsTab_14, 3,
				GridData.FILL_HORIZONTAL);
		setupLabel(g4, Messages.BuilderSettingsTab_15, 1, GridData.BEGINNING);
		this.title2 = setupLabel(g4, Messages.BuilderSettingsTab_16, 2,
				GridData.BEGINNING);
		this.b_autoBuild = setupCheck(g4, Messages.BuilderSettingsTab_17, 1,
				GridData.BEGINNING);
		this.t_autoBuild = setupBlock(g4, this.b_autoBuild);
		this.t_autoBuild.addModifyListener(new ModifyListener() {
			@Override
			public void modifyText(ModifyEvent e) {
				if (CustomisedBuildBehaviourTab.this.canModify) {
					setBuildAttribute(IMakeBuilderInfo.BUILD_TARGET_AUTO,
							CustomisedBuildBehaviourTab.this.t_autoBuild
									.getText());
				}
			}
		});
		this.t_autoBuild.getAccessible().addAccessibleListener(
				makeTargetLabelAccessibleListener);
		setupLabel(g4, Messages.BuilderSettingsTab_18, 3, GridData.BEGINNING);
		this.b_cmdBuild = setupCheck(g4, Messages.BuilderSettingsTab_19, 1,
				GridData.BEGINNING);
		this.t_cmdBuild = setupBlock(g4, this.b_cmdBuild);
		this.t_cmdBuild.addModifyListener(new ModifyListener() {
			@Override
			public void modifyText(ModifyEvent e) {
				if (CustomisedBuildBehaviourTab.this.canModify) {
					setBuildAttribute(
							IMakeBuilderInfo.BUILD_TARGET_INCREMENTAL,
							CustomisedBuildBehaviourTab.this.t_cmdBuild
									.getText());
				}
			}
		});
		this.t_cmdBuild.getAccessible().addAccessibleListener(
				makeTargetLabelAccessibleListener);
		this.b_cmdClean = setupCheck(g4, Messages.BuilderSettingsTab_20, 1,
				GridData.BEGINNING);
		this.t_cmdClean = setupBlock(g4, this.b_cmdClean);
		this.t_cmdClean.addModifyListener(new ModifyListener() {
			@Override
			public void modifyText(ModifyEvent e) {
				if (CustomisedBuildBehaviourTab.this.canModify) {
					setBuildAttribute(IMakeBuilderInfo.BUILD_TARGET_CLEAN,
							CustomisedBuildBehaviourTab.this.t_cmdClean
									.getText());
				}
			}
		});
		this.t_cmdClean.getAccessible().addAccessibleListener(
				makeTargetLabelAccessibleListener);
	}

	/**
	 * 
	 * @return: Mode 0: 0: bld.isManagedBuildOn() 1: bld.isDefaultBuildCmd() 2:
	 *          bld.canKeepEnvironmentVariablesInBuildfile() 3:
	 *          bld.keepEnvironmentVariablesInBuildfile() Mode 1: 0:
	 *          isStopOnError 1: supportsStopOnError(true) 2:
	 *          bld.supportsStopOnError(false) 3:
	 *          cfg.getInternalBuilderParallel() Mode 2: 0:
	 *          b.isAutoBuildEnable() 1: b.isIncrementalBuildEnabled() 2:
	 *          b.isCleanBuildEnabled() 3: getParallelDef()
	 */
	static int[] calc3states(ICPropertyProvider p, IConfiguration c, int mode) {
		if (p.isMultiCfg() && c instanceof ICMultiItemsHolder) {
			boolean p0 = (mode == 0);
			boolean p1 = (mode == 1);

			IConfiguration[] cfs = (IConfiguration[]) ((ICMultiItemsHolder) c)
					.getItems();
			IBuilder b = cfs[0].getBuilder();
			int[] res = new int[TRI_STATES_SIZE];
			boolean[] x = new boolean[TRI_STATES_SIZE];
			x[0] = p0 ? b.isManagedBuildOn() : (p1 ? b.isStopOnError() : b
					.isAutoBuildEnable());
			x[1] = p0 ? b.isDefaultBuildCmd() : (p1 ? b
					.supportsStopOnError(true) : b.isIncrementalBuildEnabled());
			x[2] = p0 ? b.canKeepEnvironmentVariablesInBuildfile() : (p1 ? b
					.supportsStopOnError(false) : b.isCleanBuildEnabled());
			x[3] = p0 ? b.keepEnvironmentVariablesInBuildfile()
					: (p1 ? ((Configuration) cfs[0])
							.getInternalBuilderParallel() : getParallelDef(c));
			for (int i = 1; i < cfs.length; i++) {
				b = cfs[i].getBuilder();
				if (x[0] != (p0 ? b.isManagedBuildOn() : (p1 ? b
						.isStopOnError() : b.isAutoBuildEnable()))) {
					res[0] = TRI_UNKNOWN;
				}
				if (x[1] != (p0 ? b.isDefaultBuildCmd() : (p1 ? b
						.supportsStopOnError(true) : b
						.isIncrementalBuildEnabled()))) {
					res[1] = TRI_UNKNOWN;
				}
				if (x[2] != (p0 ? b.canKeepEnvironmentVariablesInBuildfile()
						: (p1 ? b.supportsStopOnError(false) : b
								.isCleanBuildEnabled()))) {
					res[2] = TRI_UNKNOWN;
				}
				if (x[3] != (p0 ? b.keepEnvironmentVariablesInBuildfile()
						: (p1 ? ((Configuration) cfs[i])
								.getInternalBuilderParallel()
								: getParallelDef(c)))) {
					res[3] = TRI_UNKNOWN;
				}
			}
			for (int i = 0; i < TRI_STATES_SIZE; i++) {
				if (res[i] != TRI_UNKNOWN) {
					res[i] = x[i] ? TRI_YES : TRI_NO;
				}
			}
			return res;
		} else {
			return null;
		}
	}

	/**
	 * sets widgets states
	 */
	@Override
	protected void updateButtons() {
		this.bldr = this.icfg.getEditableBuilder();
		this.canModify = false;
		int[] extStates = calc3states(this.page, this.icfg, 1);

		if (extStates != null) {
			setTriSelection(this.b_stopOnError, extStates[0]);
			this.b_stopOnError.setEnabled(extStates[1] == TRI_YES
					&& extStates[2] == TRI_YES);
		} else {
			setTriSelection(this.b_stopOnError, this.bldr.isStopOnError());
			this.b_stopOnError.setEnabled(this.bldr.supportsStopOnError(true)
					&& this.bldr.supportsStopOnError(false));
		}
		// parallel
		if (extStates == null) {
			setTriSelection(this.b_parallel, getInternalBuilderParallel());
		} else {
			setTriSelection(this.b_parallel, extStates[3]);
		}

		int n = getParallelNumber();
		if (n < 0) {
			n = -n;
		}
		this.parallelProcesses.setSelection(n);

		this.b_parallel.setVisible(this.bldr.supportsParallelBuild());
		this.b_parallelOpt.setVisible(this.bldr.supportsParallelBuild());
		this.b_parallelNum.setVisible(this.bldr.supportsParallelBuild());
		this.parallelProcesses.setVisible(this.bldr.supportsParallelBuild());

		extStates = calc3states(this.page, this.icfg, 2);
		if (extStates == null) {
			setTriSelection(this.b_autoBuild, this.bldr.isAutoBuildEnable());
			setTriSelection(this.b_cmdBuild,
					this.bldr.isIncrementalBuildEnabled());
			setTriSelection(this.b_cmdClean, this.bldr.isCleanBuildEnabled());
			this.b_parallelOpt.setSelection(getParallelDef(this.icfg));
			this.b_parallelNum.setSelection(!getParallelDef(this.icfg));
		} else {
			setTriSelection(this.b_autoBuild, extStates[0]);
			setTriSelection(this.b_cmdBuild, extStates[1]);
			setTriSelection(this.b_cmdClean, extStates[2]);
			if (extStates[3] == TRI_UNKNOWN) {
				this.b_parallelOpt.setSelection(false);
				this.b_parallelNum.setSelection(false);
			} else {
				this.b_parallelOpt.setSelection(getParallelDef(this.icfg));
				this.b_parallelNum.setSelection(!getParallelDef(this.icfg));
			}
		}

		if (this.page.isMultiCfg()) {
			MultiConfiguration mc = (MultiConfiguration) this.icfg;
			this.t_autoBuild.setText(mc.getBuildAttribute(
					IMakeBuilderInfo.BUILD_TARGET_AUTO, EMPTY_STR));
			this.t_cmdBuild.setText(mc.getBuildAttribute(
					IMakeBuilderInfo.BUILD_TARGET_INCREMENTAL, EMPTY_STR));
			this.t_cmdClean.setText(mc.getBuildAttribute(
					IMakeBuilderInfo.BUILD_TARGET_CLEAN, EMPTY_STR));
		} else {
			this.t_autoBuild.setText(this.bldr.getBuildAttribute(
					IMakeBuilderInfo.BUILD_TARGET_AUTO, EMPTY_STR));
			this.t_cmdBuild.setText(this.bldr.getBuildAttribute(
					IMakeBuilderInfo.BUILD_TARGET_INCREMENTAL, EMPTY_STR));
			this.t_cmdClean.setText(this.bldr.getBuildAttribute(
					IMakeBuilderInfo.BUILD_TARGET_CLEAN, EMPTY_STR));
		}

		boolean external = !isInternalBuilderEnabled();
		boolean parallel = this.b_parallel.getSelection();

		this.b_parallelNum.setEnabled(parallel);
		this.b_parallelOpt.setEnabled(parallel);
		this.parallelProcesses.setEnabled(parallel
				& this.b_parallelNum.getSelection());

		this.title2.setVisible(external);
		this.t_autoBuild.setVisible(external);
		((Control) this.t_autoBuild.getData()).setVisible(external);
		this.t_cmdBuild.setVisible(external);
		((Control) this.t_cmdBuild.getData()).setVisible(external);
		this.t_cmdClean.setVisible(external);
		((Control) this.t_cmdClean.getData()).setVisible(external);

		if (external) {
			checkPressed(this.b_autoBuild, false);
			checkPressed(this.b_cmdBuild, false);
			checkPressed(this.b_cmdClean, false);
		}
		this.canModify = true;
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
			String x = AbstractCPropertyTab.getVariableDialog(
					this.usercomp.getShell(), getResDesc().getConfiguration());
			if (x != null) {
				((Text) b.getData()).insert(x);
			}
		}
	}

	@Override
	public void checkPressed(SelectionEvent e) {
		checkPressed((Control) e.widget, true);
		updateButtons();
	}

	private void checkPressed(Control b, boolean needsUpdate) {
		if (b == null) {
			return;
		}

		boolean val = false;
		if (b instanceof Button) {
			val = ((Button) b).getSelection();
		}

		if (b.getData() instanceof Text) {
			Text t = (Text) b.getData();
			t.setEnabled(val);
			if (t.getData() != null && t.getData() instanceof Control) {
				Control c = (Control) t.getData();
				c.setEnabled(val);
			}
		}
		if (needsUpdate) {
			setValue(b, val);
		}
	}

	/*
	 * Performs common settings for all controls (Copy from config to widgets)
	 * 
	 * @param cfgd -
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
		apply(src, dst, this.page.isMultiCfg());
	}

	static void apply(ICResourceDescription src, ICResourceDescription dst,
			boolean multi) {
		if (multi) {
			ICMultiConfigDescription mc1 = (ICMultiConfigDescription) src
					.getConfiguration();
			ICMultiConfigDescription mc2 = (ICMultiConfigDescription) dst
					.getConfiguration();
			ICConfigurationDescription[] cds1 = (ICConfigurationDescription[]) mc1
					.getItems();
			ICConfigurationDescription[] cds2 = (ICConfigurationDescription[]) mc2
					.getItems();
			for (int i = 0; i < cds1.length; i++) {
				applyToCfg(cds1[i], cds2[i]);
			}
		} else {
			applyToCfg(src.getConfiguration(), dst.getConfiguration());
		}
	}

	private static void applyToCfg(ICConfigurationDescription c1,
			ICConfigurationDescription c2) {
		Configuration cfg01 = (Configuration) getCfg(c1);
		Configuration cfg02 = (Configuration) getCfg(c2);
		cfg02.enableInternalBuilder(cfg01.isInternalBuilderEnabled());
		copyBuilders(cfg01.getBuilder(), cfg02.getEditableBuilder());
	}

	static void copyBuilders(IBuilder b1, IBuilder b2) {
		try {
			b2.setUseDefaultBuildCmd(b1.isDefaultBuildCmd());
			if (!b1.isDefaultBuildCmd()) {
				b2.setCommand(b1.getCommand());
				b2.setArguments(b1.getArguments());
			} else {
				b2.setCommand(null);
				b2.setArguments(null);
			}
			b2.setStopOnError(b1.isStopOnError());
			b2.setParallelBuildOn(b1.isParallelBuildOn());
			b2.setParallelizationNum(b1.getParallelizationNum());
			if (b2.canKeepEnvironmentVariablesInBuildfile()) {
				b2.setKeepEnvironmentVariablesInBuildfile(b1
						.keepEnvironmentVariablesInBuildfile());
			}
			((Builder) b2).setBuildPath(((Builder) b1).getBuildPathAttribute());

			b2.setAutoBuildEnable((b1.isAutoBuildEnable()));
			b2.setBuildAttribute(IMakeBuilderInfo.BUILD_TARGET_AUTO, (b1
					.getBuildAttribute(IMakeBuilderInfo.BUILD_TARGET_AUTO,
							EMPTY_STR)));
			b2.setCleanBuildEnable(b1.isCleanBuildEnabled());
			b2.setBuildAttribute(IMakeBuilderInfo.BUILD_TARGET_CLEAN, (b1
					.getBuildAttribute(IMakeBuilderInfo.BUILD_TARGET_CLEAN,
							EMPTY_STR)));
			b2.setIncrementalBuildEnable(b1.isIncrementalBuildEnabled());
			b2.setBuildAttribute(IMakeBuilderInfo.BUILD_TARGET_INCREMENTAL, (b1
					.getBuildAttribute(
							IMakeBuilderInfo.BUILD_TARGET_INCREMENTAL,
							EMPTY_STR)));

			b2.setManagedBuildOn(b1.isManagedBuildOn());
		} catch (CoreException ex) {
			ManagedBuilderUIPlugin.log(ex);
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
				copyBuilders(b.getSuperClass(), b);
			}
		} else {
			copyBuilders(this.bldr.getSuperClass(), this.bldr);
		}
		updateData(getResDesc());
	}

	private static boolean getParallelDef(IConfiguration cfg) {
		if (cfg instanceof Configuration) {
			return ((Configuration) cfg).getParallelDef();
		}
		if (cfg instanceof IMultiConfiguration) {
			return ((IMultiConfiguration) cfg).getParallelDef();
		}
		return false;
	}

	private void setParallelDef(boolean def) {
		if (this.icfg instanceof Configuration) {
			((Configuration) this.icfg).setParallelDef(def);
		}
		if (this.icfg instanceof IMultiConfiguration) {
			((IMultiConfiguration) this.icfg).setParallelDef(def);
		}
	}

	private int getParallelNumber() {
		if (this.icfg instanceof Configuration) {
			return ((Configuration) this.icfg).getParallelNumber();
		}
		if (this.icfg instanceof IMultiConfiguration) {
			return ((IMultiConfiguration) this.icfg).getParallelNumber();
		}
		return 0;
	}

	private void setParallelNumber(int num) {
		if (this.icfg instanceof Configuration) {
			((Configuration) this.icfg).setParallelNumber(num);
		}
		if (this.icfg instanceof IMultiConfiguration) {
			((IMultiConfiguration) this.icfg).setParallelNumber(num);
		}
	}

	private boolean getInternalBuilderParallel() {
		if (this.icfg instanceof Configuration) {
			return ((Configuration) this.icfg).getInternalBuilderParallel();
		}
		if (this.icfg instanceof IMultiConfiguration) {
			return ((IMultiConfiguration) this.icfg)
					.getInternalBuilderParallel();
		}
		return false;
	}

	private boolean isInternalBuilderEnabled() {
		if (this.icfg instanceof Configuration) {
			return ((Configuration) this.icfg).isInternalBuilderEnabled();
		}
		if (this.icfg instanceof IMultiConfiguration) {
			return ((IMultiConfiguration) this.icfg).isInternalBuilderEnabled();
		}
		return false;
	}

	private void setBuildAttribute(String name, String value) {
		try {
			if (this.icfg instanceof IMultiConfiguration) {
				IConfiguration[] cfs = (IConfiguration[]) ((IMultiConfiguration) this.icfg)
						.getItems();
				for (IConfiguration cf : cfs) {
					IBuilder b = cf.getEditableBuilder();
					b.setBuildAttribute(name, value);
				}
			} else {
				this.icfg.getEditableBuilder().setBuildAttribute(name, value);
			}
		} catch (CoreException e) {
			ManagedBuilderUIPlugin.log(e);
		}
	}

	private void setValue(Control b, boolean val) {
		try {
			if (this.icfg instanceof IMultiConfiguration) {
				IConfiguration[] cfs = (IConfiguration[]) ((IMultiConfiguration) this.icfg)
						.getItems();
				for (IConfiguration cf : cfs) {
					IBuilder bld = cf.getEditableBuilder();
					if (b == this.b_autoBuild) {
						bld.setAutoBuildEnable(val);
					} else if (b == this.b_cmdBuild) {
						bld.setIncrementalBuildEnable(val);
					} else if (b == this.b_cmdClean) {
						bld.setCleanBuildEnable(val);
					} else if (b == this.b_stopOnError) {
						bld.setStopOnError(val);
					} else if (b == this.b_parallel) {
						bld.setParallelBuildOn(val);
					}
				}
			} else {
				if (b == this.b_autoBuild) {
					this.bldr.setAutoBuildEnable(val);
				} else if (b == this.b_cmdBuild) {
					this.bldr.setIncrementalBuildEnable(val);
				} else if (b == this.b_cmdClean) {
					this.bldr.setCleanBuildEnable(val);
				} else if (b == this.b_stopOnError) {
					this.bldr.setStopOnError(val);
				} else if (b == this.b_parallel) {
					this.bldr.setParallelBuildOn(val);
				}
			}
		} catch (CoreException e) {
			ManagedBuilderUIPlugin.log(e);
		}
	}
}

// -----------------------------------------------------------------------------
// $Log: CustomisedBuildBehaviourTab.java,v $
// Revision 1.1 2010-09-30 15:48:08 stp
// Released.
//
