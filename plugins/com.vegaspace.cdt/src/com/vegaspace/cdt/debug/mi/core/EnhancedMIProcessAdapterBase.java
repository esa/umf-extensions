/*******************************************************************************
 * Copyright (c) 2000, 2007 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     QNX Software Systems - Initial API and implementation
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
// It has been MODIFIED by VEGA and is being re-destributed under the 
// terms of the Eclipse Public License v1.0. 
//-----------------------------------------------------------------------------
//-----------------------------------------------------------------------------

package com.vegaspace.cdt.debug.mi.core;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.Reader;

import org.eclipse.cdt.debug.mi.core.MIInferior;
import org.eclipse.cdt.debug.mi.core.MIPlugin;
import org.eclipse.cdt.debug.mi.core.MIProcess;
import org.eclipse.cdt.utils.spawner.ProcessFactory;
import org.eclipse.cdt.utils.spawner.Spawner;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;

/**
 */
public abstract class EnhancedMIProcessAdapterBase implements MIProcess {

	private static final int ONE_SECOND = 1000;

	protected Process fGDBProcess;

	private long commandTimeout;

	public EnhancedMIProcessAdapterBase(String[] args, int launchTimeout,
			IProgressMonitor monitor) throws IOException {
		this.fGDBProcess = getGDBProcess(args, launchTimeout, monitor);
		this.commandTimeout = MIPlugin.getCommandTimeout();
	}

	/**
	 * Do some basic synchronisation, gdb may take some time to load for
	 * whatever reasons and we need to be able to let the user bailout.
	 * 
	 * @param args
	 * @return Process
	 * @throws IOException
	 */
	protected Process getGDBProcess(String[] args, int launchTimeout,
			IProgressMonitor monitor) throws IOException {
		final Process pgdb = ProcessFactory.getFactory().exec(args);
		Thread syncStartup = new Thread("GDB Start") { //$NON-NLS-1$
			@Override
			public void run() {
				try {
					String line;
					InputStream stream = pgdb.getInputStream();
					Reader r = new InputStreamReader(stream);
					BufferedReader reader = new BufferedReader(r);
					while ((line = reader.readLine()) != null) {
						line = line.trim();
						// System.out.println("GDB " + line);
						if (line.endsWith("(gdb)")) {
							break;
						}
					}
				} catch (Exception e) {
					// Do nothing, ignore the errors
				}
			}
		};
		syncStartup.start();

		int timepass = 0;
		if (launchTimeout <= 0) {
			// Simulate we are waiting forever.
			launchTimeout = Integer.MAX_VALUE;
		}

		// To respect the IProgressMonitor we can not use wait/notify
		// instead we have to loop and check for the monitor to allow to cancel
		// the thread.
		// The monitor is check every 1 second delay;
		for (timepass = 0; timepass < launchTimeout; timepass += ONE_SECOND) {
			if (syncStartup.isAlive() && !monitor.isCanceled()) {
				try {
					Thread.sleep(ONE_SECOND);
				} catch (InterruptedException e) {
					// ignore
				}
			} else {
				break;
			}
		}
		try {
			syncStartup.interrupt();
			syncStartup.join(ONE_SECOND);
		} catch (InterruptedException e) {
			// ignore
		}
		if (monitor.isCanceled()) {
			pgdb.destroy();
			throw new OperationCanceledException();
		} else if (timepass > launchTimeout) {
			pgdb.destroy();
			String message = MIPlugin
					.getResourceString("src.GDBDebugger.Error_launch_timeout"); //$NON-NLS-1$
			throw new IOException(message);
		}
		return pgdb;
	}

	@Override
	public boolean canInterrupt(MIInferior inferior) {
		return this.fGDBProcess instanceof Spawner;
	}

	@Override
	public abstract void interrupt(MIInferior inferior);

	protected boolean waitForInterrupt(MIInferior inferior) {
		synchronized (inferior) {
			// Allow MI command timeout for the interrupt to propagate.
			long maxSec = this.commandTimeout / ONE_SECOND + 1;
			for (int i = 0; inferior.isRunning() && i < maxSec; i++) {
				try {
					inferior.wait(ONE_SECOND);
				} catch (InterruptedException e) {
					// ignore
				}
			}
			return inferior.isRunning();
		}
	}

	/**
	 * Send an interrupt to the inferior process.
	 * 
	 * @param inferior
	 */
	protected void interruptInferior(MIInferior inferior) {
		if (this.fGDBProcess instanceof Spawner) {
			Spawner gdbSpawner = (Spawner) this.fGDBProcess;
			gdbSpawner.raise(inferior.getInferiorPID(), gdbSpawner.INT);
			waitForInterrupt(inferior);
		}
	}

	@Override
	public int exitValue() {
		return this.fGDBProcess.exitValue();
	}

	@Override
	public int waitFor() throws InterruptedException {
		return this.fGDBProcess.waitFor();
	}

	@Override
	public void destroy() {
		this.fGDBProcess.destroy();
	}

	@Override
	public InputStream getErrorStream() {
		return this.fGDBProcess.getErrorStream();
	}

	@Override
	public InputStream getInputStream() {
		return this.fGDBProcess.getInputStream();
	}

	@Override
	public OutputStream getOutputStream() {
		return this.fGDBProcess.getOutputStream();
	}

}

// -----------------------------------------------------------------------------
// $Log: EnhancedMIProcessAdapterBase.java,v $
// Revision 1.1 2010-09-30 15:48:08 stp
// Released.
//
