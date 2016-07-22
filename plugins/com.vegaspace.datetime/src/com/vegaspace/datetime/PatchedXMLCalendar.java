/**
 * <copyright>
 *
 * Copyright (c) 2004-2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * IBM - Initial API and implementation
 *
 * </copyright>
 *
 * $Id: PatchedXMLCalendar.java 2 2012-06-22 12:39:09Z peter.ellsiepen@gmail.com $
 *
 * ---------------------------------------------------------------------
 *
 * The Apache Software License, Version 1.1
 *
 *
 * Copyright (c) 1999-2004 The Apache Software Foundation. All rights
 * reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * 1. Redistributions of source code must retain the above copyright
 * notice, this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright
 * notice, this list of conditions and the following disclaimer in
 * the documentation and/or other materials provided with the
 * distribution.
 *
 * 3. The end-user documentation included with the redistribution,
 * if any, must include the following acknowledgment:
 * "This product includes software developed by the
 * Apache Software Foundation (http://www.apache.org/)."
 * Alternately, this acknowledgment may appear in the software itself,
 * if and wherever such third-party acknowledgments normally appear.
 *
 * 4. The names "Xerces" and "Apache Software Foundation" must
 * not be used to endorse or promote products derived from this
 * software without prior written permission. For written
 * permission, please contact apache@apache.org.
 *
 * 5. Products derived from this software may not be called "Apache",
 * nor may "Apache" appear in their name, without prior written
 * permission of the Apache Software Foundation.
 *
 * THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE APACHE SOFTWARE FOUNDATION OR
 * ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF
 * USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT
 * OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
 * SUCH DAMAGE.
 * ====================================================================
 *
 * This software consists of voluntary contributions made by many
 * individuals on behalf of the Apache Software Foundation and was
 * originally based on software copyright (c) 1999-2003, International
 * Business Machines, Inc., http://www.apache.org. For more
 * information on the Apache Software Foundation, please see
 * <http://www.apache.org/>.
 */

//-----------------------------------------------------------------------------
//
// Copyright (c) 2008-2010 Terma GmbH
// Darmstadt, Germany
//
// Update : see bottom of file
//
// Remarks: 
// This file has been adapted from Eclipse source files which are licensed 
// by IBM Corporation and others, which contain software developed by the
// Apache Software Foundation and which have been distributed under
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
// This file has been adapted from Eclipse source files which are licensed 
// by IBM Corporation and others, which contain software developed by the
// Apache Software Foundation and which have been modified and distributed 
// by Terma GmbH under the terms of the Eclipse Public License v1.0
// (see the copyright statements above).
//
// It has been MODIFIED by VEGA Space GmbH and is being re-destributed under 
// the terms of the Eclipse Public License v1.0. 
//-----------------------------------------------------------------------------
//-----------------------------------------------------------------------------

package com.vegaspace.datetime;

import java.math.BigDecimal;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

import org.eclipse.emf.common.util.WrappedException;
import org.eclipse.emf.ecore.xml.type.InvalidDatatypeValueException;
import org.eclipse.emf.ecore.xml.type.internal.XMLCalendar;
import org.eclipse.emf.ecore.xml.type.internal.DataValue.TypeValidator;

/**
 * @author M. Cardone
 *
 * Representation for the <a href="http://www.w3.org/TR/2001/REC-xmlschema-2-20010502/">W3C XML Schema 1.0</a> 
 * dateTime, time, date, gYearMonth,  gYear, gMonthDay, gDay, gMonth datatypes.
 * 
 * <B>This is pached copy of org.eclipse.emf.ecore.xml.type.internal.XMLCalendar<br></B>
 * <br>
 * A patch is needed for the correct operation of the eclipse property view.
 * An additional field 'decimalSeconds' of type BigDecimal is defined in order to also represent fractions of 
 * seconds (milliseconds, nanoseconds). 
 */
public class PatchedXMLCalendar
{
    public final static short DATETIME = 0;

    public final static short TIME = 1;

    public final static short DATE = 2;

    public final static short GYEARMONTH = 3;

    public final static short GYEAR = 4;

    public final static short GMONTHDAY = 5;

    public final static short GDAY = 6;

    public final static short GMONTH = 7;
    
    public final static int EQUALS = 0;
    public final static int LESS_THAN = -1;
    public final static int GREATER_THAN = 1;
    public final static int INDETERMINATE = 2;
    

    //define shared variables for date/time

    //define constants
    protected final static int CY = 0, M = 1, D = 2, h = 3, m = 4, s = 5, ms = 6, utc = 7, hh = 0, mm = 1;

    //size for all objects must have the same fields:
    //CCYY, MM, DD, h, m, s, ms + timeZone
    protected final static int TOTAL_SIZE = 8;

    //size without time zone: ---09
    private final static int DAY_SIZE = 5;

    //size without time zone: --MM-DD
    private final static int MONTHDAY_SIZE = 7;

    //define constants to be used in assigning default values for
    //all date/time excluding duration
    protected final static int YEAR = 2000;

    protected final static int MONTH = 01;

    protected final static int DAY = 15;

    private int[] dateValue;
    
    private BigDecimal decimalSeconds =  null;
    
    final short dataType;

    private Date date;
    
    protected static final DateFormat [] EDATE_FORMATS =
    {
      new SafeSimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'"),
      new SafeSimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss"),
      new SafeSimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'.'S"), 
      new SafeSimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'.'S'Z'"),
      new SafeSimpleDateFormat("yyyy-MM-dd'Z'"),
      new SafeSimpleDateFormat("yyyy-MM-dd")
    };

    {
      EDATE_FORMATS[0].setTimeZone(TimeZone.getTimeZone("GMT"));
      EDATE_FORMATS[3].setTimeZone(TimeZone.getTimeZone("GMT"));    
    }

    public PatchedXMLCalendar(String value, short datatype)
    {
      if (value == null || value.length() == 0)
      {
        throw new InvalidDatatypeValueException("Incomplete value");
      }

      this.dataType = datatype;
      switch (dataType)
      {
        case DATETIME:
          {
            this.dateValue = parseDateTime(value);
            break;
          }
        case TIME:
          {
            this.dateValue = parseTime(value);
            break;
          }
        case DATE:
          {
            this.dateValue = parseDate(value);
            break;
          }
        case GYEAR:
          {
            this.dateValue = parseYear(value);
            break;
          }
        case GYEARMONTH:
          {
            this.dateValue = parseYearMonth(value);
            break;
          }
        case GMONTHDAY:
          {
            this.dateValue = parseMonthDay(value);
            break;
          }
        case GMONTH:
          {
            this.dateValue = parseMonth(value);
            break;
          }
        case GDAY:
          {
            this.dateValue = parseDay(value);
            break;
          }
        default:
          throw new IllegalArgumentException("Illegal datatype value");
      }
    }

    public PatchedXMLCalendar(Date date, short dataType)
    {
      this.date = date;
      this.dataType = dataType;
    }

    @Override
	public boolean equals(Object obj)
    {
      if (!(obj instanceof XMLCalendar))
      {
        return false;
      }

      PatchedXMLCalendar xmlCalendarObj = (PatchedXMLCalendar)obj;
      if (dataType != xmlCalendarObj.dataType)
      {
        return false;
      }

      return compare(this, (PatchedXMLCalendar)obj) == EQUALS;
    }

    @Override
	public int hashCode()
    {
      int hashCode = dataType;
      int [] dateValue = getDateValue();
      for (int i=0;i<TOTAL_SIZE;i++)
      {
        hashCode^=dateValue[i];
      }
      return hashCode;
    }
    
    public String getDateTimeDMYFormat()
    {
        int[] dateValue = getDateValue();
        StringBuffer message = new StringBuffer(25);
        append(message, dateValue[D], 2);
        message.append('.');
        append(message, dateValue[M], 2);
        message.append('.');
        append(message, dateValue[CY], 4);
        message.append(' ');
        append(message, dateValue[h], 2);
        message.append(':');
        append(message, dateValue[m], 2);
        message.append(':');
        // Patched to better represent nanoseconds
        append(message, decimalSeconds, 2);
        return message.toString();
    }
    
    @Override
	public String toString()
    {
      switch (dataType)
      {
        case DATETIME: return dateTimeToString();
        case TIME: return timeToString();
        case DATE: return dateToString(); 
        case GYEAR: return yearToString();
        case GYEARMONTH: return yearMonthToString();
        case GMONTHDAY: return monthDayToString();
        case GMONTH: return monthToString();
        case GDAY: return dayToString();
      }
      return null;
    }

    // the parameters are in compiled form (from getActualValue)
    public static int compare(PatchedXMLCalendar value1, PatchedXMLCalendar value2)
    {
      return (value1.dataType != value2.dataType) ? INDETERMINATE : compareDates(value1.getDateValue(), value2.getDateValue(), true); 
    }//compare()

    protected int[] getDateValue()
    {
      if (dateValue == null)
      {
        dateValue = parseDateTime(PatchedXMLCalendar.EDATE_FORMATS[0].format(date));
      }
      return dateValue;
    }

    public Date getDate()
    {
      if (date == null)
      {
        try
        {
          if (dataType == PatchedXMLCalendar.DATETIME)
          {
            try
            {
              date = PatchedXMLCalendar.EDATE_FORMATS[0].parse(dateTimeToString());
            }
            catch (Exception e)
            {
              try
              {
                date = PatchedXMLCalendar.EDATE_FORMATS[1].parse(dateTimeToString());
              }
              catch (Exception e2)
              {
                try
                {
                  date = PatchedXMLCalendar.EDATE_FORMATS[2].parse(dateTimeToString());
                }
                catch (Exception e3)
                {
                  date = PatchedXMLCalendar.EDATE_FORMATS[3].parse(dateTimeToString());
                }
              }
            }
          }
          else if (dataType == PatchedXMLCalendar.DATE)
          {
            try
            {
              date = PatchedXMLCalendar.EDATE_FORMATS[4].parse(dateToString());
            }
            catch (Exception e)
            {
              date = PatchedXMLCalendar.EDATE_FORMATS[5].parse(dateToString());
            }
          }
        }
        catch (Exception e)
        {
          throw new WrappedException(e);
        }
      }
      return date;
    }

    /**
     * Compare algorithm described in dateDime (3.2.7).
     * Duration datatype overwrites this method
     *
     * @param date1  normalized date representation of the first value
     * @param date2  normalized date representation of the second value
     * @param strict
     * @return less, greater, less_equal, greater_equal, equal
     */
    private static int compareDates(int[] date1, int[] date2, boolean strict)
    {
      if (date1[utc] == date2[utc])
      {
        return compareOrder(date1, date2);
      }
      short c1, c2;

      int[] tempDate = new int [TOTAL_SIZE];
      int[] timeZone = new int [2];

      if (date1[utc] == 'Z')
      {

        //compare date1<=date1<=(date2 with time zone -14)
        //
        cloneDate(date2, tempDate); //clones date1 value to global temporary storage: fTempDate
        timeZone[hh] = 14;
        timeZone[mm] = 0;
        tempDate[utc] = '+';
        normalize(tempDate, timeZone);
        c1 = compareOrder(date1, tempDate);
        if (c1 == TypeValidator.LESS_THAN)
          return c1;

        //compare date1>=(date2 with time zone +14)
        //
        cloneDate(date2, tempDate); //clones date1 value to global temporary storage: tempDate
        timeZone[hh] = 14;
        timeZone[mm] = 0;
        tempDate[utc] = '-';
        normalize(tempDate, timeZone);
        c2 = compareOrder(date1, tempDate);
        if (c2 == TypeValidator.GREATER_THAN)
          return c2;

        return TypeValidator.INDETERMINATE;
      }
      else if (date2[utc] == 'Z')
      {

        //compare (date1 with time zone -14)<=date2
        //
        cloneDate(date1, tempDate); //clones date1 value to global temporary storage: tempDate
        timeZone[hh] = 14;
        timeZone[mm] = 0;
        tempDate[utc] = '-';
        
        normalize(tempDate, timeZone);
        c1 = compareOrder(tempDate, date2);

        if (c1 == TypeValidator.LESS_THAN)
          return c1;

        //compare (date1 with time zone +14)<=date2
        //
        cloneDate(date1, tempDate); //clones date1 value to global temporary storage: tempDate
        timeZone[hh] = 14;
        timeZone[mm] = 0;
        tempDate[utc] = '+';
        normalize(tempDate, timeZone);
        c2 = compareOrder(tempDate, date2);

        if (c2 == TypeValidator.GREATER_THAN)
          return c2;

        return TypeValidator.INDETERMINATE;
      }
      return TypeValidator.INDETERMINATE;

    }

    /**
     * Given normalized values, determines order-relation
     * between give date/time objects.
     *
     * @param date1  date/time object
     * @param date2  date/time object
     * @return 0 if date1 and date2 are equal, a value less than 0 if date1 is less than date2, a value greater than 0 if date1 is greater than date2
     */
    protected static short compareOrder(int[] date1, int[] date2)
    {

      for (int i = 0; i < TOTAL_SIZE; i++)
      {
        if (date1[i] < date2[i])
        {
          return -1;
        }
        else if (date1[i] > date2[i])
        {
          return 1;
        }
      }
      return 0;
    }

    /**
     * Parses time hh:mm:ss.sss and time zone if any
     *
     */
    protected static Object getTime(String buffer, int start, int end, int[] data, BigDecimal decimalSeconds, int[] timeZone)
    {

      int stop = start + 2;

      //get hours (hh)
      data[h] = parseInt(buffer, start, stop);

      //get minutes (mm)

      if (buffer.charAt(stop++) != ':')
      {
        throw new InvalidDatatypeValueException("Error in parsing time zone");
      }
      start = stop;
      stop = stop + 2;
      data[m] = parseInt(buffer, start, stop);

      //get seconds (ss)
      if (buffer.charAt(stop++) != ':')
      {
        throw new InvalidDatatypeValueException("Error in parsing time zone");
      }
      start = stop;
      stop = stop + 2;
      data[s] = parseInt(buffer, start, stop);

      if (stop == end)
        return null;

      //get miliseconds (ms)
      start = stop;
      int milisec = buffer.charAt(start) == '.' ? start : -1;

      //find UTC sign if any
      int sign = findUTCSign(buffer, start, end);

      //parse miliseconds
      if (milisec != -1)
      {
        // The end of millisecond part is between . and
        // either the end of the UTC sign
        start = sign < 0 ? end : sign;
        data[ms] = parseInt(buffer, milisec + 1, start);
        
        // store seconds represented in decimal form
        String valueString = buffer.substring(milisec-2, start);
        decimalSeconds = new BigDecimal(valueString.toCharArray());
        decimalSeconds = decimalSeconds.setScale(start - milisec - 1, BigDecimal.ROUND_HALF_EVEN);
      }

      //parse UTC time zone (hh:mm)
      if (sign > 0)
      {
        if (start != sign)
          throw new InvalidDatatypeValueException("Error in parsing time zone");
        getTimeZone(buffer, data, sign, end, timeZone);
      }
      else if (start != end)
      {
        throw new InvalidDatatypeValueException("Error in parsing time zone");
      }
      return decimalSeconds;
    }

    /**
     * Parses date CCYY-MM-DD
     *
     * @param start
     * @param end
     * @param date
     */
    protected static int getDate(String buffer, int start, int end, int[] date)
    {

      start = getYearMonth(buffer, start, end, date);

      if (buffer.charAt(start++) != '-')
      {
        throw new InvalidDatatypeValueException("CCYY-MM must be followed by '-' sign");
      }
      int stop = start + 2;
      date[D] = parseInt(buffer, start, stop);
      return stop;
    }

    /**
     * Parses date CCYY-MM
     *
     * @param start
     * @param end
     * @param date
     */
    protected static int getYearMonth(String buffer, int start, int end, int[] date)
    {

      if (buffer.charAt(0) == '-')
      {
        // REVISIT: date starts with preceding '-' sign
        //          do we have to do anything with it?
        //
        start++;
      }
      int i = indexOf(buffer, start, end, '-');
      if (i == -1)
        throw new InvalidDatatypeValueException("Year separator is missing or misplaced");
      int length = i - start;
      if (length < 4)
      {
        throw new InvalidDatatypeValueException("Year must have 'CCYY' format");
      }
      else if (length > 4 && buffer.charAt(start) == '0')
      {
        throw new InvalidDatatypeValueException(
            "Leading zeros are required if the year value would otherwise have fewer than four digits; otherwise they are forbidden");
      }
      date[CY] = parseIntYear(buffer, i);
      if (buffer.charAt(i) != '-')
      {
        throw new InvalidDatatypeValueException("CCYY must be followed by '-' sign");
      }
      start = ++i;
      i = start + 2;
      date[M] = parseInt(buffer, start, i);
      return i; //fStart points right after the MONTH
    }

    /**
     * Shared code from Date and YearMonth datatypes.
     * Finds if time zone sign is present
     *
     * @param end
     * @param date
     */
    protected static void parseTimeZone(String buffer, int start, int end, int[] date, int[] timeZone)
    {

      //fStart points right after the date

      if (start < end)
      {
        int sign = findUTCSign(buffer, start, end);
        if (sign < 0)
        {
          throw new InvalidDatatypeValueException("Error in month parsing");
        }
        else
        {
          getTimeZone(buffer, date, sign, end, timeZone);
        }
      }
    }

    /**
     * Parses time zone: 'Z' or {+,-} followed by  hh:mm
     *
     * @param data
     * @param sign
     */
    protected static void getTimeZone(String buffer, int[] data, int sign, int end, int[] timeZone)
    {
      data[utc] = buffer.charAt(sign);

      if (buffer.charAt(sign) == 'Z')
      {
        if (end > (++sign))
        {
          throw new InvalidDatatypeValueException("Error in parsing time zone");
        }
        return;
      }
      if (sign <= (end - 6))
      {

        //parse [hh]
        int stop = ++sign + 2;
        timeZone[hh] = parseInt(buffer, sign, stop);
        if (buffer.charAt(stop++) != ':')
        {
          throw new InvalidDatatypeValueException("Error in parsing time zone");
        }

        //parse [ss]
        timeZone[mm] = parseInt(buffer, stop, stop + 2);

        if (stop + 2 != end)
        {
          throw new InvalidDatatypeValueException("Error in parsing time zone");
        }

      }
      else
      {
        throw new InvalidDatatypeValueException("Error in parsing time zone");
      }
    }

    /**
     * Computes index of given char within StringBuffer
     *
     * @param start
     * @param end
     * @param ch     character to look for in StringBuffer
     * @return index of ch within StringBuffer
     */
    protected static int indexOf(String buffer, int start, int end, char ch)
    {
      for (int i = start; i < end; i++)
      {
        if (buffer.charAt(i) == ch)
        {
          return i;
        }
      }
      return -1;
    }

    /**
     * Validates given date/time object accoring to W3C PR Schema
     * [D.1 ISO 8601 Conventions]
     *
     * @param data
     */
    protected static void validateDateTime(int[] data, int[] timeZone)
    {

      //REVISIT: should we throw an exception for not valid dates
      //          or reporting an error message should be sufficient?
      if (data[CY] == 0)
      {
        throw new InvalidDatatypeValueException("The year \"0000\" is an illegal year value");

      }

      if (data[M] < 1 || data[M] > 12)
      {
        throw new InvalidDatatypeValueException("The month must have values 1 to 12");

      }

      //validate days
      if (data[D] > maxDayInMonthFor(data[CY], data[M]) || data[D] < 1)
      {
        throw new InvalidDatatypeValueException("The day must have values 1 to 31");
      }

      //validate hours
      if (data[h] > 23 || data[h] < 0)
      {
        if (data[h] == 24 && data[m] == 0 && data[s] == 0 && data[ms] == 0)
        {
          data[h] = 0;
          if (++data[D] > maxDayInMonthFor(data[CY], data[M]))
          {
            data[D] = 1;
            if (++data[M] > 12)
            {
              data[M] = 1;
              if (++data[CY] == 0)
                data[CY] = 1;
            }
          }
        }
        else
        {
          throw new InvalidDatatypeValueException("Hour must have values 0-23, unless 24:00:00");
        }
      }

      //validate
      if (data[m] > 59 || data[m] < 0)
      {
        throw new InvalidDatatypeValueException("Minute must have values 0-59");
      }

      //validate
      if (data[s] > 60 || data[s] < 0)
      {
        throw new InvalidDatatypeValueException("Second must have values 0-60");

      }

      //validate
      if (timeZone[hh] > 14 || timeZone[hh] < -14)
      {
        throw new InvalidDatatypeValueException("Time zone should have range -14..+14");
      }

      //validate
      if (timeZone[mm] > 59 || timeZone[mm] < -59)
      {
        throw new InvalidDatatypeValueException("Minute must have values 0-59");
      }
    }

    /**
     * Return index of UTC char: 'Z', '+', '-'
     *
     * @param start
     * @param end
     * @return index of the UTC character that was found
     */
    private static int findUTCSign(String buffer, int start, int end)
    {
      int c;
      for (int i = start; i < end; i++)
      {
        c = buffer.charAt(i);
        if (c == 'Z' || c == '+' || c == '-')
        {
          return i;
        }

      }
      return -1;
    }

    /**
     * Given start and end position, parses string value
     *
     * @param buffer  string to parse
     * @param start  Start position
     * @param end    end position
     * @return  return integer representation of characters
     */
    protected static int parseInt(String buffer, int start, int end) throws NumberFormatException
    {
      //REVISIT: more testing on this parsing needs to be done.
      int radix = 10;
      int result = 0;
      int digit = 0;
      int limit = -Integer.MAX_VALUE;
      int multmin = limit / radix;
      int i = start;
      do
      {
        digit = TypeValidator.getDigit(buffer.charAt(i));
        if (digit < 0)
          throw new NumberFormatException("'" + buffer.toString() + "' has wrong format");
        if (result < multmin)
          throw new NumberFormatException("'" + buffer.toString() + "' has wrong format");
        result *= radix;
        if (result < limit + digit)
          throw new NumberFormatException("'" + buffer.toString() + "' has wrong format");
        result -= digit;

      }
      while (++i < end);
      return -result;
    }
    
    /**
     * Given start and end position, parses string value
     *
     * @param buffer  string to parse
     * @param start  Start position
     * @param end    end position
     * @return  return double representation of characters
     */
    protected static double parseDouble(String buffer, int start, int end) throws NumberFormatException
    {
      double result = 0;
      
      if (buffer != null && start > 0 && start < end)
      {
          String valueString = buffer.substring(start, end);
          result = Double.parseDouble(valueString);
      }
      
      return result;
    }

    // parse Year differently to support negative value.
    protected static int parseIntYear(String buffer, int end)
    {
      int radix = 10;
      int result = 0;
      boolean negative = false;
      int i = 0;
      int limit;
      int multmin;
      int digit = 0;

      if (buffer.charAt(0) == '-')
      {
        negative = true;
        limit = Integer.MIN_VALUE;
        i++;

      }
      else
      {
        limit = -Integer.MAX_VALUE;
      }
      multmin = limit / radix;
      while (i < end)
      {
        digit = TypeValidator.getDigit(buffer.charAt(i++));
        if (digit < 0)
          throw new NumberFormatException("'" + buffer.toString() + "' has wrong format");
        if (result < multmin)
          throw new NumberFormatException("'" + buffer.toString() + "' has wrong format");
        result *= radix;
        if (result < limit + digit)
          throw new NumberFormatException("'" + buffer.toString() + "' has wrong format");
        result -= digit;
      }

      if (negative)
      {
        if (i > 1)
          return result;
        else
          throw new NumberFormatException("'" + buffer.toString() + "' has wrong format");
      }
      return -result;

    }

    /**
     * If timezone present - normalize dateTime  [E Adding durations to dateTimes]
     *
     * @param date   CCYY-MM-DDThh:mm:ss+03
     * @return CCYY-MM-DDThh:mm:ssZ
     */
    protected static void normalize(int[] date, int[] timeZone)
    {

      // REVISIT: we have common code in addDuration() for durations
      //          should consider reorganizing it.
      //

      //add minutes (from time zone)
      int negate = 1;
      if (date[utc] == '+')
      {
        negate = -1;
      }
      int temp = date[m] + negate * timeZone[mm];
      int carry = fQuotient(temp, 60);
      date[m] = mod(temp, 60, carry);

      //add hours
      temp = date[h] + negate * timeZone[hh] + carry;
      carry = fQuotient(temp, 24);
      date[h] = mod(temp, 24, carry);

      date[D] = date[D] + carry;

      while (true)
      {
        temp = maxDayInMonthFor(date[CY], date[M]);
        if (date[D] < 1)
        {
          date[D] = date[D] + maxDayInMonthFor(date[CY], date[M] - 1);
          carry = -1;
        }
        else if (date[D] > temp)
        {
          date[D] = date[D] - temp;
          carry = 1;
        }
        else
        {
          break;
        }
        temp = date[M] + carry;
        date[M] = modulo(temp, 1, 13);
        date[CY] = date[CY] + fQuotient(temp, 1, 13);
      }
      date[utc] = 'Z';
    }

    /**
     * Resets object representation of date/time
     *
     * @param data   date/time object
     */
    protected static void resetDateObj(int[] data)
    {
      for (int i = 0; i < TOTAL_SIZE; i++)
      {
        data[i] = 0;
      }
    }

    /**
     * Given {year,month} computes maximum
     * number of days for given month
     *
     * @param year
     * @param month
     * @return integer containg the number of days in a given month
     */
    protected static int maxDayInMonthFor(int year, int month)
    {
      //validate days
      if (month == 4 || month == 6 || month == 9 || month == 11)
      {
        return 30;
      }
      else if (month == 2)
      {
        if (isLeapYear(year))
        {
          return 29;
        }
        else
        {
          return 28;
        }
      }
      else
      {
        return 31;
      }
    }

    private static boolean isLeapYear(int year)
    {

      //REVISIT: should we take care about Julian calendar?
      return ((year % 4 == 0) && ((year % 100 != 0) || (year % 400 == 0)));
    }

    //
    // help function described in W3C PR Schema [E Adding durations to dateTimes]
    //
    protected static int mod(int a, int b, int quotient)
    {
      //modulo(a, b) = a - fQuotient(a,b)*b
      return (a - quotient * b);
    }

    //
    // help function described in W3C PR Schema [E Adding durations to dateTimes]
    //
    protected static int fQuotient(int a, int b)
    {

      //fQuotient(a, b) = the greatest integer less than or equal to a/b
      return (int)Math.floor((float)a / b);
    }

    //
    // help function described in W3C PR Schema [E Adding durations to dateTimes]
    //
    protected static int modulo(int temp, int low, int high)
    {
      //modulo(a - low, high - low) + low
      int a = temp - low;
      int b = high - low;
      return (mod(a, b, fQuotient(a, b)) + low);
    }

    //
    // help function described in W3C PR Schema [E Adding durations to dateTimes]
    //
    protected static int fQuotient(int temp, int low, int high)
    {
      //fQuotient(a - low, high - low)
      return fQuotient(temp - low, high - low);
    }

    private String dateTimeToString()
    {
      int[] dateValue = getDateValue();
      StringBuffer message = new StringBuffer(25);
      append(message, dateValue[CY], 4);
      message.append('-');
      append(message, dateValue[M], 2);
      message.append('-');
      append(message, dateValue[D], 2);
      message.append('T');
      append(message, dateValue[h], 2);
      message.append(':');
      append(message, dateValue[m], 2);
      message.append(':');
//      append(message, dateValue[s], 2);
//      if (dateValue[ms] > 0)
//      {
//          message.append('.');
//          message.append(dateValue[ms]);
//      }
      // Patched to better represent nanoseconds
      append(message, decimalSeconds, 2);
      append(message, (char)dateValue[utc], 0);
      return message.toString();
    }

    private String dateToString()
    {
      StringBuffer message = new StringBuffer(25);
      int[] dateValue = getDateValue();
      append(message, dateValue[CY], 4);
      message.append('-');
      append(message, dateValue[M], 2);
      message.append('-');
      append(message, dateValue[D], 2);
      append(message, (char)dateValue[utc], 0);
      return message.toString();
    }

    private String dayToString()
    {
      StringBuffer message = new StringBuffer(6);
      message.append('-');
      message.append('-');
      message.append('-');
      append(message, dateValue[D], 2);
      append(message, (char)dateValue[utc], 0);
      return message.toString();
    }

    private String monthDayToString()
    {
      StringBuffer message = new StringBuffer(8);
      message.append('-');
      message.append('-');
      append(message, dateValue[M], 2);
      message.append('-');
      append(message, dateValue[D], 2);
      append(message, (char)dateValue[utc], 0);
      return message.toString();
    }

    private String monthToString()
    {
      StringBuffer message = new StringBuffer(5);
      message.append('-');
      message.append('-');
      append(message, dateValue[M], 2);
      append(message, (char)dateValue[utc], 0);
      return message.toString();
    }

    private String timeToString()
    {
      StringBuffer message = new StringBuffer(16);
      append(message, dateValue[h], 2);
      message.append(':');
      append(message, dateValue[m], 2);
      message.append(':');
//      append(message, dateValue[s], 2);
//      message.append('.');
//      message.append(dateValue[ms]);
//    Patched to better represent nanoseconds
      append(message, decimalSeconds, 2);
      append(message, (char)dateValue[utc], 0);
      return message.toString();
    }

    private String yearToString()
    {
      StringBuffer message = new StringBuffer(5);
      append(message, dateValue[CY], 4);
      append(message, (char)dateValue[utc], 0);
      return message.toString();
    }

    private String yearMonthToString()
    {
      StringBuffer message = new StringBuffer(25);
      append(message, dateValue[CY], 4);
      message.append('-');
      append(message, dateValue[M], 2);
      append(message, (char)dateValue[utc], 0);
      return message.toString();
    }

    private static void append(StringBuffer message, int value, int nch)
    {
      if (value < 0)
      {
        message.append('-');
        value = -value;
      }
      if (nch == 4)
      {
        if (value < 10)
          message.append("000");
        else if (value < 100)
          message.append("00");
        else if (value < 1000)
          message.append("0");
        message.append(value);
      }
      else if (nch == 2)
      {
        if (value < 10)
          message.append('0');
        message.append(value);
      }
      else
      {
        if (value != 0)
          message.append((char)value);
      }
    }
    
    private static void append(StringBuffer message, BigDecimal decimalValue, int nch)
    {
        double value = decimalValue.doubleValue();
        if (value < 0)
        {
          message.append('-');
          value = -value;
        }
        if (nch == 4)
        {
          if (value < 10)
            message.append("000");
        else if (value < 100)
          message.append("00");
        else if (value < 1000)
          message.append("0");
            message.append(decimalValue.toPlainString());
          }
          else if (nch == 2)
          {
            if (value < 10)
              message.append('0');
            message.append(decimalValue.toPlainString());
          }
          else
          {
            if (value != 0)
              message.append(decimalValue.toPlainString());
          }
    }

    //
    //Private help functions
    //

    private static void cloneDate(int[] finalValue, int[] tempDate)
    {
      System.arraycopy(finalValue, 0, tempDate, 0, TOTAL_SIZE);
    }

    private int[] parseDateTime(String str) throws InvalidDatatypeValueException
    {
      int len = str.length();
      int[] date = new int [TOTAL_SIZE];
      BigDecimal decimalSecs = new BigDecimal(0);
      int[] timeZone = new int [2];

      int end = indexOf(str, 0, len, 'T');

      // both time and date
      getDate(str, 0, end, date);
      this.decimalSeconds = (BigDecimal)getTime(str, end + 1, len, date, decimalSecs, timeZone);

      //validate and normalize
      validateDateTime(date, timeZone);

      if (date[utc] != 0 && date[utc] != 'Z')
      {
        normalize(date, timeZone);
      }
      return date;
    }

    private int[] parseDate(String str) throws InvalidDatatypeValueException
    {
      int len = str.length();
      int[] date = new int [TOTAL_SIZE];
      int[] timeZone = new int [2];

      int end = getDate(str, 0, len, date);
      parseTimeZone(str, end, len, date, timeZone);

      //validate and normalize
      validateDateTime(date, timeZone);

      if (date[utc] != 0 && date[utc] != 'Z')
      {
        normalize(date, timeZone);
      }
      return date;
    }

    private int[] parseDay(String str) throws InvalidDatatypeValueException
    {
      int len = str.length();
      int[] date = new int [TOTAL_SIZE];
      int[] timeZone = new int [2];

      if (str.charAt(0) != '-' || str.charAt(1) != '-' || str.charAt(2) != '-')
      {
        throw new InvalidDatatypeValueException("Error in day parsing");
      }

      //initialize values
      date[CY] = YEAR;
      date[M] = MONTH;

      date[D] = parseInt(str, 3, 5);

      if (DAY_SIZE < len)
      {
        int sign = findUTCSign(str, DAY_SIZE, len);
        if (sign < 0)
        {
          throw new InvalidDatatypeValueException("Error in day parsing");
        }
        else
        {
          getTimeZone(str, date, sign, len, timeZone);
        }
      }

      //validate and normalize
      validateDateTime(date, timeZone);

      if (date[utc] != 0 && date[utc] != 'Z')
      {
        normalize(date, timeZone);
      }
      return date;
    }

    private int[] parseMonthDay(String str) throws InvalidDatatypeValueException
    {
      int len = str.length();
      int[] date = new int [TOTAL_SIZE];
      int[] timeZone = new int [2];

      //initialize
      date[CY] = YEAR;

      if (str.charAt(0) != '-' || str.charAt(1) != '-')
      {
        throw new InvalidDatatypeValueException("Invalid format for gMonthDay: " + str);
      }
      date[M] = parseInt(str, 2, 4);
      int start = 4;

      if (str.charAt(start++) != '-')
      {
        throw new InvalidDatatypeValueException("Invalid format for gMonthDay: " + str);
      }

      date[D] = parseInt(str, start, start + 2);

      if (MONTHDAY_SIZE < len)
      {
        int sign = findUTCSign(str, MONTHDAY_SIZE, len);
        if (sign < 0)
        {
          throw new InvalidDatatypeValueException("Error in month parsing:" + str);
        }
        else
        {
          getTimeZone(str, date, sign, len, timeZone);
        }
      }
      //validate and normalize

      validateDateTime(date, timeZone);

      if (date[utc] != 0 && date[utc] != 'Z')
      {
        normalize(date, timeZone);
      }
      return date;
    }

    private int[] parseMonth(String str) throws InvalidDatatypeValueException
    {
      int len = str.length();
      int[] date = new int [TOTAL_SIZE];
      int[] timeZone = new int [2];

      //set constants
      date[CY] = YEAR;
      date[D] = DAY;
      if (str.charAt(0) != '-' || str.charAt(1) != '-')
      {
        throw new InvalidDatatypeValueException("Invalid format for gMonth: " + str);
      }
      int stop = 4;
      date[M] = parseInt(str, 2, stop);

      // REVISIT: allow both --MM and --MM-- now.
      // need to remove the following 4 lines to disallow --MM--
      // when the errata is offically in the rec.
      if (str.length() >= stop + 2 && str.charAt(stop) == '-' && str.charAt(stop + 1) == '-')
      {
        stop += 2;
      }
      if (stop < len)
      {
        int sign = findUTCSign(str, stop, len);
        if (sign < 0)
        {
          throw new InvalidDatatypeValueException("Error in month parsing: " + str);
        }
        else
        {
          getTimeZone(str, date, sign, len, timeZone);
        }
      }
      //validate and normalize
      validateDateTime(date, timeZone);

      if (date[utc] != 0 && date[utc] != 'Z')
      {
        normalize(date, timeZone);
      }
      return date;
    }

    private int[] parseYear(String str) throws InvalidDatatypeValueException
    {
      int len = str.length();
      int[] date = new int [TOTAL_SIZE];
      int[] timeZone = new int [2];

      // check for preceding '-' sign
      int start = 0;
      if (str.charAt(0) == '-')
      {
        start = 1;
      }
      int sign = findUTCSign(str, start, len);
      if (sign == -1)
      {
        date[CY] = parseIntYear(str, len);
      }
      else
      {
        date[CY] = parseIntYear(str, sign);
        getTimeZone(str, date, sign, len, timeZone);
      }

      //initialize values
      date[M] = MONTH;
      date[D] = 1;

      //validate and normalize
      validateDateTime(date, timeZone);

      if (date[utc] != 0 && date[utc] != 'Z')
      {
        normalize(date, timeZone);
      }
      return date;
    }

    private int[] parseYearMonth(String str) throws InvalidDatatypeValueException
    {
      int len = str.length();
      int[] date = new int [TOTAL_SIZE];
      int[] timeZone = new int [2];

      // get date
      int end = getYearMonth(str, 0, len, date);
      date[D] = DAY;
      parseTimeZone(str, end, len, date, timeZone);

      //validate and normalize
      validateDateTime(date, timeZone);

      if (date[utc] != 0 && date[utc] != 'Z')
      {
        normalize(date, timeZone);
      }
      return date;
    }

    private int[] parseTime(String str) throws InvalidDatatypeValueException
    {
      int len = str.length();
      int[] date = new int [TOTAL_SIZE];
      int[] timeZone = new int [2];

      // time
      // initialize to default values
      date[CY] = YEAR;
      date[M] = MONTH;
      date[D] = DAY;
      this.decimalSeconds = (BigDecimal)getTime(str, 0, len, date, decimalSeconds, timeZone);

      //validate and normalize

      validateDateTime(date, timeZone);

      if (date[utc] != 0)
      {
        normalize(date, timeZone);
      }
      return date;
    }
    

    private static class SafeSimpleDateFormat extends SimpleDateFormat
    {
      /**
         * 
         */
        private static final long serialVersionUID = 1L;

    public SafeSimpleDateFormat(String pattern)
      {
        super(pattern);
      }

      @Override
	public synchronized Date parse(String source) throws ParseException
      {
        return super.parse(source);
      }
    }
}

//-----------------------------------------------------------------------------
// $Log: PatchedXMLCalendar.java,v $
// Revision 1.1  2010-09-30 15:48:08  stp
// Released.
//
