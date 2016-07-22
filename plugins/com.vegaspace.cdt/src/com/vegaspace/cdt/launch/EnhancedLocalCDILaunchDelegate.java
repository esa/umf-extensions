/*******************************************************************************
 * Copyright (c) 2004, 2008 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * QNX Software Systems - Initial API and implementation
 * Anton Leherbauer (Wind River Systems) - bugs 205108, 212632, 224187
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
//org.eclipse.cdt.launch.internal.LocalCDILaunchDelegate. 

package com.vegaspace.cdt.launch;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.IBinaryParser.IBinaryObject;
import org.eclipse.cdt.core.IProcessInfo;
import org.eclipse.cdt.core.IProcessList;
import org.eclipse.cdt.core.model.ICProject;
import org.eclipse.cdt.debug.core.CDIDebugModel;
import org.eclipse.cdt.debug.core.ICDIDebugger;
import org.eclipse.cdt.debug.core.ICDIDebugger2;
import org.eclipse.cdt.debug.core.ICDTLaunchConfigurationConstants;
import org.eclipse.cdt.debug.core.ICDebugConfiguration;
import org.eclipse.cdt.debug.core.cdi.CDIException;
import org.eclipse.cdt.debug.core.cdi.ICDISession;
import org.eclipse.cdt.debug.core.cdi.model.ICDIRuntimeOptions;
import org.eclipse.cdt.debug.core.cdi.model.ICDITarget;
import org.eclipse.cdt.launch.AbstractCLaunchDelegate;
import org.eclipse.cdt.launch.internal.ui.LaunchMessages;
import org.eclipse.cdt.launch.internal.ui.LaunchUIPlugin;
import org.eclipse.cdt.utils.pty.PTY;
import org.eclipse.cdt.utils.spawner.ProcessFactory;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.core.ILaunchManager;
import org.eclipse.debug.core.IStatusHandler;
import org.eclipse.debug.core.model.IProcess;

/**
 * The launch configuration delegate for the CDI debugger session types.
 */
@SuppressWarnings("deprecation")
public abstract class EnhancedLocalCDILaunchDelegate extends
		AbstractCLaunchDelegate {
	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.cdt.launch.AbstractCLaunchDelegate#launch(org.eclipse.debug
	 * .core.ILaunchConfiguration, java.lang.String,
	 * org.eclipse.debug.core.ILaunch,
	 * org.eclipse.core.runtime.IProgressMonitor)
	 */
	@Override
	public void launch(ILaunchConfiguration config, String mode,
			ILaunch launch, IProgressMonitor monitor) throws CoreException {
		if (monitor == null) {
			monitor = new NullProgressMonitor();
		}
		if (mode.equals(ILaunchManager.RUN_MODE)) {
			runLocalApplication(config, launch, monitor);
		}
		if (mode.equals(ILaunchManager.DEBUG_MODE)) {
			launchDebugger(config, launch, monitor);
		}
	}

	private void runLocalApplication(ILaunchConfiguration config,
			ILaunch launch, IProgressMonitor monitor) throws CoreException {
		monitor.beginTask(LaunchMessages.LocalCDILaunchDelegate_0, 10);
		if (monitor.isCanceled()) {
			return;
		}
		monitor.worked(1);
		try {
			IPath exePath = verifyProgramPath(config);
			File wd = getWorkingDirectory(config);
			if (wd == null) {
				wd = new File(System.getProperty("user.home", ".")); //$NON-NLS-1$ //$NON-NLS-2$
			}
			String arguments[] = getProgramArgumentsArray(config);
			ArrayList<String> command = new ArrayList<String>(
					1 + arguments.length);
			command.add(exePath.toOSString());
			command.addAll(Arrays.asList(arguments));
			String[] commandArray = command.toArray(new String[command.size()]);
			boolean usePty = config.getAttribute(
					ICDTLaunchConfigurationConstants.ATTR_USE_TERMINAL,
					ICDTLaunchConfigurationConstants.USE_TERMINAL_DEFAULT);
			monitor.worked(2);
			Process process = exec(commandArray, getEnvironment(config), wd,
					usePty);
			monitor.worked(6);
			DebugPlugin.newProcess(launch, process,
					renderProcessLabel(commandArray[0]));
		} finally {
			monitor.done();
		}
	}

	private void launchDebugger(ILaunchConfiguration config, ILaunch launch,
			IProgressMonitor monitor) throws CoreException {
		monitor.beginTask(LaunchMessages.LocalCDILaunchDelegate_1, 10);
		if (monitor.isCanceled()) {
			return;
		}
		try {
			String debugMode = config.getAttribute(
					ICDTLaunchConfigurationConstants.ATTR_DEBUGGER_START_MODE,
					ICDTLaunchConfigurationConstants.DEBUGGER_MODE_RUN);
			if (debugMode
					.equals(ICDTLaunchConfigurationConstants.DEBUGGER_MODE_RUN)) {
				launchLocalDebugSession(config, launch, monitor);
			}
			if (debugMode
					.equals(ICDTLaunchConfigurationConstants.DEBUGGER_MODE_ATTACH)) {
				launchAttachDebugSession(config, launch, monitor);
			}
			if (debugMode
					.equals(ICDTLaunchConfigurationConstants.DEBUGGER_MODE_CORE)) {
				launchCoreDebugSession(config, launch, monitor);
			}
		} finally {
			monitor.done();
		}
	}

	private void launchLocalDebugSession(ILaunchConfiguration config,
			ILaunch launch, IProgressMonitor monitor) throws CoreException {
		if (monitor.isCanceled()) {
			return;
		}
		monitor.subTask(LaunchMessages.LocalCDILaunchDelegate_2);
		ICDISession dsession = null;
		try {
			IPath exePath = verifyProgramPath(config);
			ICProject project = verifyCProject(config);
			IBinaryObject exeFile = null;
			if (exePath != null) {
				exeFile = verifyBinary(project, exePath);
			}

			ICDebugConfiguration debugConfig = getDebugConfig(config);

			setDefaultSourceLocator(launch, config);

			dsession = createCDISession(config, launch, debugConfig, monitor);
			monitor.worked(6);

			setRuntimeOptions(config, dsession);
			monitor.worked(1);

			boolean stopInMain = config
					.getAttribute(
							ICDTLaunchConfigurationConstants.ATTR_DEBUGGER_STOP_AT_MAIN,
							false);
			String stopSymbol = null;
			if (stopInMain) {
				stopSymbol = launch
						.getLaunchConfiguration()
						.getAttribute(
								ICDTLaunchConfigurationConstants.ATTR_DEBUGGER_STOP_AT_MAIN_SYMBOL,
								ICDTLaunchConfigurationConstants.DEBUGGER_STOP_AT_MAIN_SYMBOL_DEFAULT);
			}
			ICDITarget[] targets = dsession.getTargets();
			for (ICDITarget target : targets) {
				Process process = target.getProcess();
				IProcess iprocess = null;
				if (process != null) {
					iprocess = DebugPlugin.newProcess(launch, process,
							renderProcessLabel(exePath.toOSString()),
							getDefaultProcessMap());
				}
				CDIDebugModel.newDebugTarget(launch, project.getProject(),
						target, renderTargetLabel(debugConfig), iprocess,
						exeFile, true, false, stopSymbol, true);
			}
		} catch (CoreException e) {
			try {
				if (dsession != null) {
					dsession.terminate();
				}
			} catch (CDIException e1) {
				// ignore
			}
			throw e;
		} finally {
			monitor.done();
		}
	}

	protected abstract int launchSimulation(ILaunchConfiguration config)
			throws CoreException;

	protected void fetchRemoteBinary(ILaunchConfiguration config)
			throws CoreException {
		// Body intentionally empty
	}

	private void launchAttachDebugSession(ILaunchConfiguration config,
			ILaunch launch, IProgressMonitor monitor) throws CoreException {
		if (monitor.isCanceled()) {
			return;
		}
		monitor.subTask(LaunchMessages.LocalCDILaunchDelegate_3);
		ILaunchConfigurationWorkingCopy wc = null;
		// NOTICE: The code here is possibly brittle: if for some
		// reason the debugger fails to clear config after a debugging session
		// finishes (and the corresponding simulation process is being
		// terminated),
		// then no other sessions using the same UMF debugging configuration
		// will be
		// possible (since the debugger will keep trying to attach to the PID of
		// the terminated process).
		// This problem occurred during development; it might be that it
		// will not occur under normal circumstances (e.g. because the
		// implementation takes care to clear config in all possible
		// situations).
		// Nevertheless we protect below the call to createCDISession() with
		// try-catch
		// and we reset ATTR_ATTACH_PROCESS_ID in config if an exception is
		// raised.
		int pid = config.getAttribute(
				ICDTLaunchConfigurationConstants.ATTR_ATTACH_PROCESS_ID, -1);
		if (pid == -1) {
			try {
				pid = launchSimulation(config);
			} catch (CoreException e) {
				throw e;
			}
			wc = config.getWorkingCopy();
			wc.setAttribute(
					ICDTLaunchConfigurationConstants.ATTR_ATTACH_PROCESS_ID,
					pid);
			try {
				wc.doSave().launch(ILaunchManager.DEBUG_MODE,
						new SubProgressMonitor(monitor, 9));
			} finally {
				// We need to reset the process id because the working copy will
				// be saved
				// when the target is terminated
				wc.setAttribute(
						ICDTLaunchConfigurationConstants.ATTR_ATTACH_PROCESS_ID,
						(String) null);
				wc.doSave();
			}
			cancel("", -1); //$NON-NLS-1$
		}

		fetchRemoteBinary(config);

		IPath exePath = verifyProgramPath(config);
		if (exePath == null) {
			exePath = getProgramPathForPid(pid);
		}
		ICProject project = verifyCProject(config);
		IBinaryObject exeFile = null;
		if (exePath != null) {
			exeFile = verifyBinary(project, exePath);
		}

		ICDebugConfiguration debugConfig = getDebugConfig(config);

		setDefaultSourceLocator(launch, config);

		ICDISession dsession = null;

		// NOTICE: The intention of this try-catch is to give us a possibility
		// to reset ATTR_ATTACH_PROCESS_ID in config if attaching to the
		// specified
		// process fails, cf. notice above.
		try {
			dsession = createCDISession(config, launch, debugConfig, monitor);
		} catch (CoreException e) {
			if (null == wc) {
				wc = config.getWorkingCopy();
			}

			wc.setAttribute(
					ICDTLaunchConfigurationConstants.ATTR_ATTACH_PROCESS_ID,
					(String) null);
			wc.doSave();

			throw e;
		}
		monitor.worked(7);

		try {
			ICDITarget[] targets = dsession.getTargets();
			for (ICDITarget target : targets) {
				CDIDebugModel.newDebugTarget(launch, project.getProject(),
						target, renderTargetLabel(debugConfig), null, exeFile,
						true, true, false);
			}
		} catch (CoreException e) {
			try {
				dsession.terminate();
			} catch (CDIException e1) {
				// ignore
			}
			throw e;
		} finally {
			if (wc != null) {
				wc.setAttribute(
						ICDTLaunchConfigurationConstants.ATTR_ATTACH_PROCESS_ID,
						(String) null);
			}
			monitor.done();
		}
	}

	private IPath getProgramPathForPid(int pid) {
		IProcessList processList = null;
		try {
			processList = CCorePlugin.getDefault().getProcessList();
		} catch (CoreException exc) {
			// ignored on purpose
		}
		if (processList != null) {
			IProcessInfo[] pInfos = processList.getProcessList();
			for (IProcessInfo processInfo : pInfos) {
				if (processInfo.getPid() == pid) {
					final String name = processInfo.getName();
					if (name != null) {
						return new Path(name);
					}
					break;
				}
			}
		}
		return null;
	}

	private void launchCoreDebugSession(ILaunchConfiguration config,
			ILaunch launch, IProgressMonitor monitor) throws CoreException {
		if (monitor.isCanceled()) {
			return;
		}
		monitor.beginTask(LaunchMessages.LocalCDILaunchDelegate_5, 10);
		ICDISession dsession = null;
		ILaunchConfigurationWorkingCopy wc = null;
		ICDebugConfiguration debugConfig = getDebugConfig(config);
		String path = config.getAttribute(
				ICDTLaunchConfigurationConstants.ATTR_COREFILE_PATH,
				(String) null);
		if (path == null) {
			ICProject project = verifyCProject(config);
			IPath corefile = promptForCoreFilePath(
					(IProject) project.getResource(), debugConfig);
			if (corefile == null) {
				cancel(LaunchMessages.LocalCDILaunchDelegate_6,
						ICDTLaunchConfigurationConstants.ERR_NO_COREFILE);
			}
			File file = new File(corefile.toString());
			if (!file.exists() || !file.canRead()) {
				cancel(LaunchMessages.LocalCDILaunchDelegate_7,
						ICDTLaunchConfigurationConstants.ERR_NO_COREFILE);
			}
			wc = config.getWorkingCopy();
			wc.setAttribute(
					ICDTLaunchConfigurationConstants.ATTR_COREFILE_PATH,
					corefile.toString());
			wc.launch(ILaunchManager.DEBUG_MODE, new SubProgressMonitor(
					monitor, 9));
			wc.setAttribute(
					ICDTLaunchConfigurationConstants.ATTR_COREFILE_PATH,
					(String) null);
			cancel("", -1); //$NON-NLS-1$
		}

		IPath exePath = verifyProgramPath(config);
		ICProject project = verifyCProject(config);
		IBinaryObject exeFile = null;
		if (exePath != null) {
			exeFile = verifyBinary(project, exePath);
		}

		setDefaultSourceLocator(launch, config);

		dsession = createCDISession(config, launch, debugConfig, monitor);
		monitor.worked(7);

		try {
			ICDITarget[] targets = dsession.getTargets();
			for (ICDITarget target : targets) {
				Process process = target.getProcess();
				IProcess iprocess = null;
				if (process != null) {
					iprocess = DebugPlugin.newProcess(launch, process,
							renderProcessLabel(exePath.toOSString()),
							getDefaultProcessMap());
				}
				CDIDebugModel.newDebugTarget(launch, project.getProject(),
						target, renderTargetLabel(debugConfig), iprocess,
						exeFile, true, false, false);
			}
		} catch (CoreException e) {
			try {
				if (dsession != null) {
					dsession.terminate();
				}
			} catch (CDIException e1) {
				// ignore
			}
			throw e;
		} finally {
			if (wc != null) {
				wc.setAttribute(
						ICDTLaunchConfigurationConstants.ATTR_COREFILE_PATH,
						(String) null);
			}
			monitor.done();
		}
	}

	private ICDISession launchOldDebugSession(ILaunchConfiguration config,
			ILaunch launch, ICDIDebugger debugger, IProgressMonitor monitor)
			throws CoreException {
		IBinaryObject exeFile = null;
		IPath exePath = verifyProgramPath(config);
		ICProject project = verifyCProject(config);
		if (exePath != null) {
			exeFile = verifyBinary(project, exePath);
		}
		return debugger.createDebuggerSession(launch, exeFile, monitor);
	}

	private ICDISession launchDebugSession(ILaunchConfiguration config,
			ILaunch launch, ICDIDebugger2 debugger, IProgressMonitor monitor)
			throws CoreException {
		IPath path = verifyProgramPath(config);
		File exeFile = path != null ? path.toFile() : null;
		return debugger.createSession(launch, exeFile, monitor);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.cdt.launch.AbstractCLaunchDelegate#getPluginID()
	 */
	@Override
	protected String getPluginID() {
		return LaunchUIPlugin.getUniqueIdentifier();
	}

	/**
	 * Performs a runtime exec on the given command line in the context of the
	 * specified working directory, and returns the resulting process. If the
	 * current runtime does not support the specification of a working
	 * directory, the status handler for error code
	 * <code>ERR_WORKING_DIRECTORY_NOT_SUPPORTED</code> is queried to see if the
	 * exec should be re-executed without specifying a working directory.
	 * 
	 * @param cmdLine
	 *            the command line
	 * @param workingDirectory
	 *            the working directory, or <code>null</code>
	 * @return the resulting process or <code>null</code> if the exec is
	 *         cancelled
	 * @see Runtime
	 */
	@SuppressWarnings("unused")
	protected Process exec(String[] cmdLine, String[] environ,
			File workingDirectory, boolean usePty) throws CoreException {
		Process p = null;
		try {
			if (workingDirectory == null) {
				p = ProcessFactory.getFactory().exec(cmdLine, environ);
			} else {
				if (usePty && PTY.isSupported()) {
					p = ProcessFactory.getFactory().exec(cmdLine, environ,
							workingDirectory, new PTY());
				} else {
					p = ProcessFactory.getFactory().exec(cmdLine, environ,
							workingDirectory);
				}
			}
		} catch (IOException e) {
			if (p != null) {
				p.destroy();
			}
			abort(LaunchMessages.LocalCDILaunchDelegate_8, e,
					ICDTLaunchConfigurationConstants.ERR_INTERNAL_ERROR);
		} catch (NoSuchMethodError e) {
			// attempting launches on 1.2.* - no ability to set working
			// directory
			IStatus status = new Status(
					IStatus.ERROR,
					LaunchUIPlugin.getUniqueIdentifier(),
					ICDTLaunchConfigurationConstants.ERR_WORKING_DIRECTORY_NOT_SUPPORTED,
					LaunchMessages.LocalCDILaunchDelegate_9, e);
			IStatusHandler handler = DebugPlugin.getDefault().getStatusHandler(
					status);
			if (handler != null) {
				Object result = handler.handleStatus(status, this);
				if (result instanceof Boolean
						&& ((Boolean) result).booleanValue()) {
					p = exec(cmdLine, environ, null, usePty);
				}
			}
		}
		return p;
	}

	protected int promptForProcessID(ILaunchConfiguration config)
			throws CoreException {
		IStatus fPromptStatus = new Status(IStatus.INFO,
				"org.eclipse.debug.ui", 200, "", null); //$NON-NLS-1$//$NON-NLS-2$
		IStatus processPrompt = new Status(IStatus.INFO,
				"org.eclipse.cdt.launch", 100, "", null); //$NON-NLS-1$//$NON-NLS-2$
		// consult a status handler
		IStatusHandler prompter = DebugPlugin.getDefault().getStatusHandler(
				fPromptStatus);
		if (prompter != null) {
			Object result = prompter.handleStatus(processPrompt, config);
			if (result instanceof Integer) {
				return ((Integer) result).intValue();
			}
		}
		return -1;
	}

	protected IPath promptForCoreFilePath(final IProject project,
			final ICDebugConfiguration debugConfig) throws CoreException {
		IStatus fPromptStatus = new Status(IStatus.INFO,
				"org.eclipse.debug.ui", 200, "", null); //$NON-NLS-1$//$NON-NLS-2$
		IStatus processPrompt = new Status(IStatus.INFO,
				"org.eclipse.cdt.launch", 1001, "", null); //$NON-NLS-1$//$NON-NLS-2$
		// consult a status handler
		IStatusHandler prompter = DebugPlugin.getDefault().getStatusHandler(
				fPromptStatus);
		if (prompter != null) {
			Object result = prompter.handleStatus(processPrompt, new Object[] {
					project, debugConfig });
			if (result instanceof IPath) {
				return (IPath) result;
			}
		}
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.cdt.launch.AbstractCLaunchDelegate#preLaunchCheck(org.eclipse
	 * .debug.core.ILaunchConfiguration, java.lang.String,
	 * org.eclipse.core.runtime.IProgressMonitor)
	 */
	@Override
	public boolean preLaunchCheck(ILaunchConfiguration config, String mode,
			IProgressMonitor monitor) throws CoreException {
		// no pre launch check for core file
		if (mode.equals(ILaunchManager.DEBUG_MODE)) {
			if (ICDTLaunchConfigurationConstants.DEBUGGER_MODE_CORE
					.equals(config
							.getAttribute(
									ICDTLaunchConfigurationConstants.ATTR_DEBUGGER_START_MODE,
									ICDTLaunchConfigurationConstants.DEBUGGER_MODE_RUN))) {
				return true;
			}
		}
		return super.preLaunchCheck(config, mode, monitor);
	}

	private void setRuntimeOptions(ILaunchConfiguration config,
			ICDISession session) throws CoreException {
		String arguments[] = getProgramArgumentsArray(config);
		try {
			ICDITarget[] dtargets = session.getTargets();
			for (int i = 0; i < dtargets.length; ++i) {
				ICDIRuntimeOptions opt = dtargets[i].getRuntimeOptions();
				opt.setArguments(arguments);
				File wd = getWorkingDirectory(config);
				if (wd != null) {
					opt.setWorkingDirectory(wd.getAbsolutePath());
				}
				opt.setEnvironment(getEnvironmentAsProperty(config));
			}
		} catch (CDIException e) {
			abort(LaunchMessages.LocalCDILaunchDelegate_10, e,
					ICDTLaunchConfigurationConstants.ERR_INTERNAL_ERROR);
		}
	}

	private ICDISession createCDISession(ILaunchConfiguration config,
			ILaunch launch, ICDebugConfiguration debugConfig,
			IProgressMonitor monitor) throws CoreException {
		ICDISession session = null;
		ICDIDebugger debugger = debugConfig.createDebugger();
		if (debugger instanceof ICDIDebugger2) {
			session = launchDebugSession(config, launch,
					(ICDIDebugger2) debugger, monitor);
		} else {
			// support old debugger types
			session = launchOldDebugSession(config, launch, debugger, monitor);
		}
		return session;
	}
}

// -----------------------------------------------------------------------------
// $Log: EnhancedLocalCDILaunchDelegate.java,v $
// Revision 1.1 2010-09-30 15:48:08 stp
// Released.
//
