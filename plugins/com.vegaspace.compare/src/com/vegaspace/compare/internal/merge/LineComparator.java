/*******************************************************************************
 * Copyright (c) 2004, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

//-----------------------------------------------------------------------------
//
// Copyright (c) 2007-2010 Terma GmbH
// Darmstadt, Germany
//
// Update : see bottom of file
//
// Remarks: 
// This file has been adapted from Eclipse source files which are licensed 
// by IBM Corporation and others and which have been distributed under
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

//Please notice that this class duplicates an internal Eclipse class named 
//LineComparator; the only modifications is that visibility of class, as well as
//of method getLine() has been changed to public (and that the package name has
//changed).
//The copyright for this code is the Eclipse copyright, cf. copyright notice above. 

package com.vegaspace.compare.internal.merge;

import java.io.*;
import java.util.ArrayList;
import org.eclipse.compare.rangedifferencer.IRangeComparator;

/**
 * This implementation of IRangeComparator breaks an input stream into lines.
 */
public class LineComparator implements IRangeComparator
{
    /** Internal line store. */
    private String[] fLines;

    /**
     * Creates a new instance of LineComparator.
     * @param is input stream
     */
    public LineComparator(InputStream is)
    {
        
        BufferedReader br = new BufferedReader(new InputStreamReader(is));
        String line;
        ArrayList<String> ar = new ArrayList<String>();
        try
        {
            while ((line = br.readLine()) != null)
                ar.add(line);
        }
        catch (IOException e)
        {
            // silently ignored
        }
        finally
        {
            try
            {
                is.close();
            }
            catch (IOException e)
            {
        		// silently ignored
            }
        }
        fLines = ar.toArray(new String[ar.size()]);
    }

    /**
     * Returns a line by index.
     * @param ix line index
     * @return line at given index
     */
    public String getLine(int ix)
    {
        return fLines[ix];
    }

    /**
     * {@inheritDoc}
     * @see org.eclipse.compare.rangedifferencer.IRangeComparator#getRangeCount()
     */
    @Override
	public int getRangeCount()
    {
        return fLines.length;
    }

    /**
     * {@inheritDoc}
     * @see org.eclipse.compare.rangedifferencer.IRangeComparator#rangesEqual(int,
     *      org.eclipse.compare.rangedifferencer.IRangeComparator, int)
     */
    @Override
	public boolean rangesEqual(int thisIndex, IRangeComparator other, int otherIndex)
    {
        String s1 = fLines[thisIndex];
        String s2 = ((LineComparator) other).fLines[otherIndex];
        return s1.equals(s2);
    }

    /**
     * {@inheritDoc}
     * @see org.eclipse.compare.rangedifferencer.IRangeComparator#skipRangeComparison(int,
     *      int, org.eclipse.compare.rangedifferencer.IRangeComparator)
     */
    @Override
	public boolean skipRangeComparison(int length, int maxLength, IRangeComparator other)
    {
        return false;
    }
}

//-----------------------------------------------------------------------------
//$Log: LineComparator.java,v $
//Revision 1.1  2010-09-30 15:48:08  stp
//Released.
//
