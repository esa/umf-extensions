/*
 * Copyright 2005 The Apache Software Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

//-----------------------------------------------------------------------------
//
// Copyright (c) 2007-2010 Terma GmbH
// Darmstadt, Germany
//
// Update : see bottom of file
//
// Remarks: 
// This file has been adapted from source files which are licensed 
// by the Apache Software Foundation and which have been distributed under
// the terms of the the Apache License, Version 2.0 (cf. the copyright statement
// above).
// 
// It has been MODIFIED by Terma GmbH and is being re-destributed under the 
// terms of the Apache License, Version 2.0. 
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
// This file has been adapted from source files which are licensed by the
// Apache Software Foundation and Terma GmbH and which have been distributed 
// under the terms of the the Apache License, Version 2.0 
// (see copyright statements above).
//
// It has been MODIFIED by VEGA Space GmbH and is being re-destributed under 
// the terms of the Apache License, Version 2.0. 
//-----------------------------------------------------------------------------
//-----------------------------------------------------------------------------

package com.vegaspace.datetime;

import java.io.IOException;
import java.io.ObjectStreamException;
import java.io.Serializable;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeConstants;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.Duration;
import javax.xml.datatype.XMLGregorianCalendar;

import org.apache.xerces.util.DatatypeMessageFormatter;

/**
 *
 * <B>This is pached copy of com.sun.org.apache.xerces.internal.jaxp.datatype.DurationImpl<br></B>
 * <br>
 * A patch is needed for the correct operation of the eclipse property view.
 * The changes to the original code are limited to the equals() method in this class.
 * See the equals() method for more details.
 * <br> 
 * 
 * <p>Immutable representation of a time span as defined in
 * the W3C XML Schema 1.0 specification.</p>
 * 
 * <p>A Duration object represents a period of Gregorian time,
 * which consists of six fields (years, months, days, hours,
 * minutes, and seconds) plus a sign (+/-) field.</p>
 * 
 * <p>The first five fields have non-negative (>=0) integers or null
 * (which represents that the field is not set),
 * and the seconds field has a non-negative decimal or null.
 * A negative sign indicates a negative duration.</p> 
 * 
 * <p>This class provides a number of methods that make it easy
 * to use for the duration datatype of XML Schema 1.0 with
 * the errata.</p>
 * 
 * <h2>Order relationship</h2>
 * <p>Duration objects only have partial order, where two values A and B
 * maybe either:</p>
 * <ol>
 *  <li>A&lt;B (A is shorter than B)
 *  <li>A&gt;B (A is longer than B)
 *  <li>A==B   (A and B are of the same duration)
 *  <li>A&lt;>B (Comparison between A and B is indeterminate)
 * </ol>
 * <p>For example, 30 days cannot be meaningfully compared to one month.
 * The {@link #compare(Duration)} method implements this
 * relationship.</p>
 * 
 * <p>See the {@link #isLongerThan(Duration)} method for details about
 * the order relationship among {@link Duration} objects.</p>
 * 
 * 
 * 
 * <h2>Operations over Duration</h2>
 * <p>This class provides a set of basic arithmetic operations, such
 * as addition, subtraction and multiplication.
 * Because durations don't have total order, an operation could
 * fail for some combinations of operations. For example, you cannot
 * subtract 15 days from 1 month. See the javadoc of those methods
 * for detailed conditions where this could happen.</p>
 * 
 * <p>Also, division of a duration by a number is not provided because
 * the {@link Duration} class can only deal with finite precision
 * decimal numbers. For example, one cannot represent 1 sec divided by 3.</p> 
 * 
 * <p>However, you could substitute a division by 3 with multiplying
 * by numbers such as 0.3 or 0.333.</p>
 *
 *
 * 
 * <h2>Range of allowed values</h2>
 * <p>
 * Because some operations of {@link Duration} rely on {@link Calendar}
 * even though {@link Duration} can hold very large or very small values,
 * some of the methods may not work correctly on such {@link Duration}s.
 * The impacted methods document their dependency on {@link Calendar}.
 * 
 *  
 * @author <a href="mailto:Kohsuke.Kawaguchi@Sun.com">Kohsuke Kawaguchi</a>
 * @author <a href="mailto:Joseph.Fialli@Sun.com">Joseph Fialli</a>
 * @version $Revision: 2 $, $Date: 2012-06-22 12:39:09 +0000 (Fri, 22 Jun 2012) $    
 *
 * @see XMLGregorianCalendar#add(Duration)
 * @since 1.5
 */
public class PatchedDurationImpl
    extends Duration
    implements Serializable {

	//Constants
    private static final BigDecimal SECONDS_PER_MINUTE = new BigDecimal(60);
    private static final BigInteger MINUTES_PER_HOUR = BigInteger.valueOf(60);
    private static final BigInteger HOURS_PER_DAY = BigInteger.valueOf(24);
	private static final BigDecimal NSECS_CUTOFF_VALUE = new BigDecimal("1e100");
    
    /**
     * <p>Number of Fields.</p>
     */
//    private static final int FIELD_NUM = 6;
    
    /**
     * <p>Internal array of value Fields.</p>
     */
    private static final DatatypeConstants.Field[] FIELDS = new DatatypeConstants.Field[]{
            DatatypeConstants.YEARS,
            DatatypeConstants.MONTHS,
            DatatypeConstants.DAYS,
            DatatypeConstants.HOURS,
            DatatypeConstants.MINUTES,
            DatatypeConstants.SECONDS
        };

        /**
         * <p>Internal array of value Field ids.</p>
         */
//        private static final int[] FIELD_IDS = {
//                DatatypeConstants.YEARS.getId(),
//                DatatypeConstants.MONTHS.getId(),
//                DatatypeConstants.DAYS.getId(),
//                DatatypeConstants.HOURS.getId(),
//                DatatypeConstants.MINUTES.getId(),
//                DatatypeConstants.SECONDS.getId()
//            };

    /**
     * <p>Indicates the sign. -1, 0 or 1 if the duration is negative,
     * zero, or positive.</p>
     */
    private final int signum;
    
    /**
     * <p>Years of this <code>Duration</code>.</p>
     */
    private final BigInteger years;
    
    /**
     * <p>Months of this <code>Duration</code>.</p>
     */
    private final BigInteger months;
    
    /**
     * <p>Days of this <code>Duration</code>.</p>
     */
    private final BigInteger days;
    
    /**
     * <p>Hours of this <code>Duration</code>.</p>
     */
    private final BigInteger hours;
    
    /**
     * <p>Minutes of this <code>Duration</code>.</p>
     */
    private final BigInteger minutes;
    
    /**
     * <p>Seconds of this <code>Duration</code>.</p>
     */
    private final BigDecimal seconds;

    /**
     * Returns the sign of this duration in -1,0, or 1.
     * 
     * @return
     *      -1 if this duration is negative, 0 if the duration is zero,
     *      and 1 if the duration is postive.
     */
    @Override
	public int getSign() {
        
        return signum;
    }
    
    // this method has been overriden in order to overcome the problem that miliseconds are not 
    // taken into account when comparing two Durations. This poses a problem for the eclipse 
    // property view since it does not update a value if it is not different from the old value. 
    // As a consequence changing the miliseconds of a Duration was impossible. 
    // By always returning false in this method it is enforced that the value is updated 
    // in the property view.   
    @Override
	public boolean equals(final Object duration) 
    {
        return false;
    }

    /**
     *  TBD: Javadoc
     * @param isPositive Sign.
     * 
     * @return 1 if positive, else -1.
     */         
    private int calcSignum(boolean isPositive) {
        if ((years == null || years.signum() == 0)
            && (months == null || months.signum() == 0)
            && (days == null || days.signum() == 0)
            && (hours == null || hours.signum() == 0)
            && (minutes == null || minutes.signum() == 0)
            && (seconds == null || seconds.signum() == 0)) {
            return 0;
            }

            if (isPositive) {
                return 1;
            } else {
                return -1;
            }

    }
    
    /**
     * <p>Constructs a new Duration object by specifying each field individually.</p>
     * 
     * <p>All the parameters are optional as long as at least one field is present.
     * If specified, parameters have to be zero or positive.</p>
     * 
     * @param isPositive Set to <code>false</code> to create a negative duration. When the length
     *   of the duration is zero, this parameter will be ignored.
     * @param years of this <code>Duration</code>
     * @param months of this <code>Duration</code>
     * @param days of this <code>Duration</code>
     * @param hours of this <code>Duration</code>
     * @param minutes of this <code>Duration</code>
     * @param seconds of this <code>Duration</code>
     * 
     * @throws IllegalArgumentException
     *    If years, months, days, hours, minutes and
     *    seconds parameters are all <code>null</code>. Or if any
     *    of those parameters are negative.
     */
    protected PatchedDurationImpl(
        boolean isPositive,
        BigInteger years,
        BigInteger months,
        BigInteger days,
        BigInteger hours,
        BigInteger minutes,
        BigDecimal seconds) {
            
        this.years = years;
        this.months = months;
        this.days = days;
        this.hours = hours;
        this.minutes = minutes;
        this.seconds = seconds;

        this.signum = calcSignum(isPositive);

        // sanity check
        if (years == null
            && months == null
            && days == null
            && hours == null
            && minutes == null
            && seconds == null) {
            throw new IllegalArgumentException(
            //"all the fields are null"
            DatatypeMessageFormatter.formatMessage(null, "AllFieldsNull", null)
            );
        }
        testNonNegative(years, DatatypeConstants.YEARS);
        testNonNegative(months, DatatypeConstants.MONTHS);
        testNonNegative(days, DatatypeConstants.DAYS);
        testNonNegative(hours, DatatypeConstants.HOURS);
        testNonNegative(minutes, DatatypeConstants.MINUTES);
        testNonNegative(seconds, DatatypeConstants.SECONDS);
    }
    
    /**
     * <p>Makes sure that the given number is non-negative. If it is not,
     * throw {@link IllegalArgumentException}.</p>
     * 
     * @param n Number to test.
     * @param f Field to test.
     */
    private static void testNonNegative(BigInteger n, DatatypeConstants.Field f) {
        if (n != null && n.signum() < 0) {
            throw new IllegalArgumentException(             
                DatatypeMessageFormatter.formatMessage(null, "NegativeField", new Object[]{f.toString()})
            );
        }
    }
    
    /**
     * <p>Makes sure that the given number is non-negative. If it is not,
     * throw {@link IllegalArgumentException}.</p>
     * 
     * @param n Number to test.
     * @param f Field to test.
     */
    private static void testNonNegative(BigDecimal n, DatatypeConstants.Field f) {
        if (n != null && n.signum() < 0) {
            
            throw new IllegalArgumentException(
                DatatypeMessageFormatter.formatMessage(null, "NegativeField", new Object[]{f.toString()})            
            );
        }
    }
    
    /**
     * <p>Constructs a new Duration object by specifying each field
     * individually.</p>
     * 
     * <p>This method is functionally equivalent to
     * invoking another constructor by wrapping
     * all non-zero parameters into {@link BigInteger} and {@link BigDecimal}.
     * Zero value of int parameter is equivalent of null value of
     * the corresponding field.</p> 
     * 
     * @see #DurationImpl(boolean, BigInteger, BigInteger, BigInteger, BigInteger,
     *   BigInteger, BigDecimal)
     */
    protected PatchedDurationImpl(
        final boolean isPositive,
        final int years,
        final int months,
        final int days,
        final int hours,
        final int minutes,
        final int seconds) {
        this(
            isPositive,
            wrap(years),
            wrap(months),
            wrap(days),
            wrap(hours),
            wrap(minutes),
            seconds != 0 ? new BigDecimal(String.valueOf(seconds)) : null);
    }

    /**
     *  TBD: Javadoc
     * 
     * @param i int to convert to BigInteger.
     * 
     * @return BigInteger representation of int.
     */
    private static BigInteger wrap(final int i) {
        
        // field may not be set
        if (i == DatatypeConstants.FIELD_UNDEFINED) {
            return null;
        }
        
        // int -> BigInteger
        return new BigInteger(String.valueOf(i));
    }
    
    /**
     * <p>Constructs a new Duration object by specifying the duration
     * in milliseconds.</p>
     * 
     * <p>The DAYS, HOURS, MINUTES and SECONDS fields are used to
     * represent the specifed duration in a reasonable way.
     * That is, the constructed object <code>x</code> satisfies
     * the following conditions:</p>
     * <ul>
     *  <li>x.getHours()&lt;24
     *  <li>x.getMinutes()&lt;60
     *  <li>x.getSeconds()&lt;60 
     * </ul>
     * 
     * @param durationInMilliSeconds
     *      The length of the duration in milliseconds.
     */
    protected PatchedDurationImpl(final long durationInMilliSeconds) {
        
        boolean is0x8000000000000000L = false;
        long l = durationInMilliSeconds;
        
        if (l > 0) {
            signum = 1;
        } else if (l < 0) {
            signum = -1;
            if (l == 0x8000000000000000L) {
                // negating 0x8000000000000000L causes an overflow
                l++;
                is0x8000000000000000L = true;
            }
            l *= -1;
        } else {
            signum = 0;
        }
        
        this.years = null;
        this.months = null;
        
        this.seconds =
            BigDecimal.valueOf((l % 60000L) + (is0x8000000000000000L ? 1 : 0), 3);
        
        l /= 60000L;
        this.minutes = (l == 0) ? null : BigInteger.valueOf(l % 60L);

        l /= 60L;
        this.hours = (l == 0) ? null : BigInteger.valueOf(l % 24L);
        
        l /= 24L;
        this.days = (l == 0) ? null : BigInteger.valueOf(l);
    }
    
    /**
     * Constructs a new Duration object by
     * parsing its string representation
     * "PnYnMnDTnHnMnS" as defined in XML Schema 1.0 section 3.2.6.1.
     * 
     * <p>
     * The string representation may not have any leading
     * and trailing whitespaces.
     * 
     * <p>
     * For example, this method parses strings like
     * "P1D" (1 day), "-PT100S" (-100 sec.), "P1DT12H" (1 days and 12 hours).
     *  
     * <p>
     * The parsing is done field by field so that   
     * the following holds for any lexically correct string x:
     * <pre>
     * new Duration(x).toString().equals(x)
     * </pre>
     * 
     * Returns a non-null valid duration object that holds the value
     * indicated by the lexicalRepresentation parameter.
     *
     * @param lexicalRepresentation
     *      Lexical representation of a duration.
     * @throws IllegalArgumentException
     *      If the given string does not conform to the aforementioned
     *      specification.
     * @throws NullPointerException
     *      If the given string is null.
     */
    public PatchedDurationImpl(String lexicalRepresentation)
        throws IllegalArgumentException {
        // only if I could use the JDK1.4 regular expression ....
        
        final String s = lexicalRepresentation;
        boolean positive;
        int[] idx = new int[1];
    int length = s.length();
    boolean timeRequired = false;

    if (lexicalRepresentation == null) {
        throw new NullPointerException();
    }
        
    idx[0] = 0;
    if (length != idx[0] && s.charAt(idx[0]) == '-') {
        idx[0]++;
        positive = false;
    } else {
        positive = true;
    }
        
    if (length != idx[0] && s.charAt(idx[0]++) != 'P') {
        // string represents nanoseconds in decimal format
        try
        {
            BigDecimal decimalNanos = new BigDecimal(s);
            
            //NOTICE: Arbitrary precision arithmetic can take arbitrarily long time if the values dealt
            //with are big enough; to avoid this we reject values exceeding NSECS_CUTOFF_VALUE.
            if (1 == decimalNanos.compareTo(NSECS_CUTOFF_VALUE))
            {
                throw new IllegalArgumentException("Argument too big: " + s);
            }
            
            BigDecimal millis = decimalNanos.movePointLeft(6);
            
            // parse into numbers
            if (millis.compareTo(BigDecimal.valueOf(0)) == 1) {
                signum = 1;
            } else if (millis.compareTo(BigDecimal.valueOf(0)) == -1) {
                signum = -1;
                millis = millis.negate();
            } else {
                signum = 0;
            }
            
            this.years = null;
            this.months = null;
            
            this.seconds = ((millis.movePointLeft(3)).remainder(SECONDS_PER_MINUTE));
            
            millis = millis.movePointLeft(3).divide(SECONDS_PER_MINUTE, BigDecimal.ROUND_HALF_EVEN);
            BigInteger millisAsBigInt = millis.toBigInteger();
            
            this.minutes = (millisAsBigInt.equals(BigInteger.ZERO)) ? null : millisAsBigInt.remainder(MINUTES_PER_HOUR);

            millisAsBigInt = millisAsBigInt.divide(MINUTES_PER_HOUR);
            this.hours = (millisAsBigInt.equals(BigInteger.ZERO)) ? null : millisAsBigInt.remainder(HOURS_PER_DAY);
            
            millisAsBigInt = millisAsBigInt.divide(HOURS_PER_DAY);
            this.days = (millisAsBigInt.equals(BigInteger.ZERO)) ? null : millisAsBigInt;
            return;
        }
        catch(IllegalArgumentException e)
        {
            throw e;
        }
        catch(Exception e)
        {
            throw new IllegalArgumentException(s);
        }
    }

        
        // phase 1: chop the string into chunks
        // (where a chunk is '<number><a symbol>'
        //--------------------------------------
        int dateLen = 0;
        String[] dateParts = new String[3];
        int[] datePartsIndex = new int[3];
        while (length != idx[0]
            && isDigit(s.charAt(idx[0]))
            && dateLen < 3) {
            datePartsIndex[dateLen] = idx[0];
            dateParts[dateLen++] = parsePiece(s, idx);
        }
        
    if (length != idx[0]) {
        if (s.charAt(idx[0]++) == 'T') {
        timeRequired = true;
        } else {
        throw new IllegalArgumentException(s); // ,idx[0]-1);
        }
    }
        
        int timeLen = 0;
        String[] timeParts = new String[3];
        int[] timePartsIndex = new int[3];
        while (length != idx[0]
            && isDigitOrPeriod(s.charAt(idx[0]))
            && timeLen < 3) {
            timePartsIndex[timeLen] = idx[0];
            timeParts[timeLen++] = parsePiece(s, idx);
        }
        
    if (timeRequired && timeLen == 0) {
            throw new IllegalArgumentException(s); // ,idx[0]);
    }

        if (length != idx[0]) {
            throw new IllegalArgumentException(s); // ,idx[0]);
        }
        if (dateLen == 0 && timeLen == 0) {
            throw new IllegalArgumentException(s); // ,idx[0]);
        }
        
        // phase 2: check the ordering of chunks
        //--------------------------------------
        organizeParts(s, dateParts, datePartsIndex, dateLen, "YMD");
        organizeParts(s, timeParts, timePartsIndex, timeLen, "HMS");
        
        // parse into numbers
        years = parseBigInteger(s, dateParts[0], datePartsIndex[0]);
        months = parseBigInteger(s, dateParts[1], datePartsIndex[1]);
        days = parseBigInteger(s, dateParts[2], datePartsIndex[2]);
        hours = parseBigInteger(s, timeParts[0], timePartsIndex[0]);
        minutes = parseBigInteger(s, timeParts[1], timePartsIndex[1]);
        seconds = parseBigDecimal(s, timeParts[2], timePartsIndex[2]);
        signum = calcSignum(positive);
    }
        
     
    /**
     *  TBD: Javadoc
     * 
     * @param ch char to test.
     * 
     * @return true if ch is a digit, else false.
     */
    private static boolean isDigit(char ch) {
        return '0' <= ch && ch <= '9';
    }
    
    /**
     *  TBD: Javadoc
     * 
     * @param ch to test.
     * 
     * @return true if ch is a digit or a period, else false.
     */
    private static boolean isDigitOrPeriod(char ch) {
        return isDigit(ch) || ch == '.';
    }
    
    /**
     *  TBD: Javadoc
     * 
     * @param whole String to parse.
     * @param idx  TBD: ???
     * 
     * @return Result of parsing.
     * 
     * @throws IllegalArgumentException If whole cannot be parsed.
     */
    private static String parsePiece(String whole, int[] idx)
        throws IllegalArgumentException {
        int start = idx[0];
        while (idx[0] < whole.length()
            && isDigitOrPeriod(whole.charAt(idx[0]))) {
            idx[0]++;
            }
        if (idx[0] == whole.length()) {
            throw new IllegalArgumentException(whole); // ,idx[0]);
        }

        idx[0]++;

        return whole.substring(start, idx[0]);
    }
    
    /**
     *  TBD: Javadoc.
     * 
     * @param whole  TBD: ???
     * @param parts  TBD: ???
     * @param partsIndex  TBD: ???
     * @param len  TBD: ???
     * @param tokens  TBD: ???
     * 
     * @throws IllegalArgumentException  TBD: ???
     */
    private static void organizeParts(
        String whole,
        String[] parts,
        int[] partsIndex,
        int len,
        String tokens)
        throws IllegalArgumentException {

        int idx = tokens.length();
        for (int i = len - 1; i >= 0; i--) {
            int nidx =
                tokens.lastIndexOf(
                    parts[i].charAt(parts[i].length() - 1),
                    idx - 1);
            if (nidx == -1) {
                throw new IllegalArgumentException(whole);
                // ,partsIndex[i]+parts[i].length()-1);
            }

            for (int j = nidx + 1; j < idx; j++) {
                parts[j] = null;
            }
            idx = nidx;
            parts[idx] = parts[i];
            partsIndex[idx] = partsIndex[i];
        }
        for (idx--; idx >= 0; idx--) {
            parts[idx] = null;
        }
    }
    
    /**
     *  TBD: Javadoc
     * 
     * @param whole  TBD: ???.
     * @param part  TBD: ???.
     * @param index  TBD: ???.
     * 
     * @return  TBD: ???.
     * 
     * @throws IllegalArgumentException  TBD: ???.
     */
    private static BigInteger parseBigInteger(
        String whole,
        String part,
        int index)
        throws IllegalArgumentException {
        if (part == null) {
            return null;
        }
        part = part.substring(0, part.length() - 1);
        //        try {
        return new BigInteger(part);
        //        } catch( NumberFormatException e ) {
        //            throw new ParseException( whole, index );
        //        }
    }
    
    /**
     *  TBD: Javadoc.
     * 
     * @param whole  TBD: ???.
     * @param part  TBD: ???.
     * @param index  TBD: ???.
     * 
     * @return  TBD: ???.
     * 
     * @throws IllegalArgumentException  TBD: ???.
     */
    private static BigDecimal parseBigDecimal(
        String whole,
        String part,
        int index)
        throws IllegalArgumentException {
        if (part == null) {
            return null;
        }
        part = part.substring(0, part.length() - 1);
        // NumberFormatException is IllegalArgumentException
        //        try {
        return new BigDecimal(part);
        //        } catch( NumberFormatException e ) {
        //            throw new ParseException( whole, index );
        //        }
    }
    
    /**
     * <p>Four constants defined for the comparison of durations.</p>
     */
    private static XMLGregorianCalendar[] tmp_test_points = null;
    static
    {
    	try {
			tmp_test_points = new XMLGregorianCalendar[] {
				DatatypeFactory.newInstance().newXMLGregorianCalendar("1696-09-01T00:00:00Z"),
				DatatypeFactory.newInstance().newXMLGregorianCalendar("1697-02-01T00:00:00Z"),
				DatatypeFactory.newInstance().newXMLGregorianCalendar("1903-03-01T00:00:00Z"),
				DatatypeFactory.newInstance().newXMLGregorianCalendar("1903-07-01T00:00:00Z"),
			};
		} catch (DatatypeConfigurationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    }
    
    private final XMLGregorianCalendar[] TEST_POINTS = tmp_test_points;
    
    /**
     * <p>Partial order relation comparison with this <code>Duration</code> instance.</p>
     * 
     * <p>Comparison result must be in accordance with
     * <a href="http://www.w3.org/TR/xmlschema-2/#duration-order">W3C XML Schema 1.0 Part 2, Section 3.2.7.6.2,
     * <i>Order relation on duration</i></a>.</p>
     * 
     * <p>Return:</p>
     * <ul>
     *   <li>{@link DatatypeConstants#LESSER} if this <code>Duration</code> is shorter than <code>duration</code> parameter</li>
     *   <li>{@link DatatypeConstants#EQUAL} if this <code>Duration</code> is equal to <code>duration</code> parameter</li>
     *   <li>{@link DatatypeConstants#GREATER} if this <code>Duration</code> is longer than <code>duration</code> parameter</li>
     *   <li>{@link DatatypeConstants#INDETERMINATE} if a conclusive partial order relation cannot be determined</li>
     * </ul>
     *
     * @param duration to compare
     * 
     * @return the relationship between <code>this</code> <code>Duration</code>and <code>duration</code> parameter as
     *   {@link DatatypeConstants#LESSER}, {@link DatatypeConstants#EQUAL}, {@link DatatypeConstants#GREATER}
     *   or {@link DatatypeConstants#INDETERMINATE}.
     * 
     * @throws UnsupportedOperationException If the underlying implementation
     *   cannot reasonably process the request, e.g. W3C XML Schema allows for
     *   arbitrarily large/small/precise values, the request may be beyond the
     *   implementations capability.
     * @throws NullPointerException if <code>duration</code> is <code>null</code>. 
     *
     * @see #isShorterThan(Duration)
     * @see #isLongerThan(Duration)
     */
    @Override
	public int compare(Duration rhs) {
        
        BigInteger maxintAsBigInteger = BigInteger.valueOf((long) Integer.MAX_VALUE);
//        BigInteger minintAsBigInteger = BigInteger.valueOf((long) Integer.MIN_VALUE);

        // check for fields that are too large in this Duration
        if (years != null && years.compareTo(maxintAsBigInteger) == 1) {            
            throw new UnsupportedOperationException(
                        DatatypeMessageFormatter.formatMessage(null, "TooLarge", 
                            new Object[]{this.getClass().getName() + "#compare(Duration duration)" + DatatypeConstants.YEARS.toString(), years.toString()})
                        //this.getClass().getName() + "#compare(Duration duration)"
                        //+ " years too large to be supported by this implementation "
                        //+ years.toString()
                    );
        }
        if (months != null && months.compareTo(maxintAsBigInteger) == 1) {
            throw new UnsupportedOperationException(
                        DatatypeMessageFormatter.formatMessage(null, "TooLarge", 
                            new Object[]{this.getClass().getName() + "#compare(Duration duration)" + DatatypeConstants.MONTHS.toString(), months.toString()})
            
                        //this.getClass().getName() + "#compare(Duration duration)"
                        //+ " months too large to be supported by this implementation "
                        //+ months.toString()
                    );
        }
        if (days != null && days.compareTo(maxintAsBigInteger) == 1) {
            throw new UnsupportedOperationException(
                        DatatypeMessageFormatter.formatMessage(null, "TooLarge", 
                            new Object[]{this.getClass().getName() + "#compare(Duration duration)" + DatatypeConstants.DAYS.toString(), days.toString()})
            
                        //this.getClass().getName() + "#compare(Duration duration)"
                        //+ " days too large to be supported by this implementation "
                        //+ days.toString()
                    );
        }
        if (hours != null && hours.compareTo(maxintAsBigInteger) == 1) {
            throw new UnsupportedOperationException(
                        DatatypeMessageFormatter.formatMessage(null, "TooLarge", 
                            new Object[]{this.getClass().getName() + "#compare(Duration duration)" + DatatypeConstants.HOURS.toString(), hours.toString()})
            
                        //this.getClass().getName() + "#compare(Duration duration)"
                        //+ " hours too large to be supported by this implementation "
                        //+ hours.toString()
                    );
        }
        if (minutes != null && minutes.compareTo(maxintAsBigInteger) == 1) {
            throw new UnsupportedOperationException(
                        DatatypeMessageFormatter.formatMessage(null, "TooLarge", 
                            new Object[]{this.getClass().getName() + "#compare(Duration duration)" + DatatypeConstants.MINUTES.toString(), minutes.toString()})
            
                        //this.getClass().getName() + "#compare(Duration duration)"
                        //+ " minutes too large to be supported by this implementation "
                        //+ minutes.toString()
                    );
        }
        if (seconds != null && seconds.toBigInteger().compareTo(maxintAsBigInteger) == 1) {
            throw new UnsupportedOperationException(
                        DatatypeMessageFormatter.formatMessage(null, "TooLarge", 
                            new Object[]{this.getClass().getName() + "#compare(Duration duration)" + DatatypeConstants.SECONDS.toString(), seconds.toString()})
            
                        //this.getClass().getName() + "#compare(Duration duration)"
                        //+ " seconds too large to be supported by this implementation "
                        //+ seconds.toString()
                    );
        }
        
        // check for fields that are too large in rhs Duration
        BigInteger rhsYears = (BigInteger) rhs.getField(DatatypeConstants.YEARS);
        if (rhsYears != null && rhsYears.compareTo(maxintAsBigInteger) == 1) {
            throw new UnsupportedOperationException(
                        DatatypeMessageFormatter.formatMessage(null, "TooLarge", 
                            new Object[]{this.getClass().getName() + "#compare(Duration duration)" + DatatypeConstants.YEARS.toString(), rhsYears.toString()})
            
                        //this.getClass().getName() + "#compare(Duration duration)"
                        //+ " years too large to be supported by this implementation "
                        //+ rhsYears.toString()
                    );
        }
        BigInteger rhsMonths = (BigInteger) rhs.getField(DatatypeConstants.MONTHS);
        if (rhsMonths != null && rhsMonths.compareTo(maxintAsBigInteger) == 1) {
            throw new UnsupportedOperationException(
                        DatatypeMessageFormatter.formatMessage(null, "TooLarge", 
                            new Object[]{this.getClass().getName() + "#compare(Duration duration)" + DatatypeConstants.MONTHS.toString(), rhsMonths.toString()})
            
                        //this.getClass().getName() + "#compare(Duration duration)"
                        //+ " months too large to be supported by this implementation "
                        //+ rhsMonths.toString()
                    );
        }
        BigInteger rhsDays = (BigInteger) rhs.getField(DatatypeConstants.DAYS);
        if (rhsDays != null && rhsDays.compareTo(maxintAsBigInteger) == 1) {
            throw new UnsupportedOperationException(
                        DatatypeMessageFormatter.formatMessage(null, "TooLarge", 
                            new Object[]{this.getClass().getName() + "#compare(Duration duration)" + DatatypeConstants.DAYS.toString(), rhsDays.toString()})
            
                        //this.getClass().getName() + "#compare(Duration duration)"
                        //+ " days too large to be supported by this implementation "
                        //+ rhsDays.toString()
                    );
        }
        BigInteger rhsHours = (BigInteger) rhs.getField(DatatypeConstants.HOURS);
        if (rhsHours != null && rhsHours.compareTo(maxintAsBigInteger) == 1) {
            throw new UnsupportedOperationException(
                        DatatypeMessageFormatter.formatMessage(null, "TooLarge", 
                            new Object[]{this.getClass().getName() + "#compare(Duration duration)" + DatatypeConstants.HOURS.toString(), rhsHours.toString()})
            
                        //this.getClass().getName() + "#compare(Duration duration)"
                        //+ " hours too large to be supported by this implementation "
                        //+ rhsHours.toString()
                    );
        }
        BigInteger rhsMinutes = (BigInteger) rhs.getField(DatatypeConstants.MINUTES);
        if (rhsMinutes != null && rhsMinutes.compareTo(maxintAsBigInteger) == 1) {
            throw new UnsupportedOperationException(
                        DatatypeMessageFormatter.formatMessage(null, "TooLarge", 
                            new Object[]{this.getClass().getName() + "#compare(Duration duration)" + DatatypeConstants.MINUTES.toString(), rhsMinutes.toString()})
            
                        //this.getClass().getName() + "#compare(Duration duration)"
                        //+ " minutes too large to be supported by this implementation "
                        //+ rhsMinutes.toString()
                    );
        }
        BigDecimal rhsSecondsAsBigDecimal = (BigDecimal) rhs.getField(DatatypeConstants.SECONDS);
        BigInteger rhsSeconds = null;
        if ( rhsSecondsAsBigDecimal != null ) {
                rhsSeconds =  rhsSecondsAsBigDecimal.toBigInteger();
        }
        if (rhsSeconds != null && rhsSeconds.compareTo(maxintAsBigInteger) == 1) {
            throw new UnsupportedOperationException(
                        DatatypeMessageFormatter.formatMessage(null, "TooLarge", 
                            new Object[]{this.getClass().getName() + "#compare(Duration duration)" + DatatypeConstants.SECONDS.toString(), rhsSeconds.toString()})
            
                        //this.getClass().getName() + "#compare(Duration duration)"
                        //+ " seconds too large to be supported by this implementation "
                        //+ rhsSeconds.toString()
                    );
        }

        // turn this Duration into a GregorianCalendar
        GregorianCalendar lhsCalendar = new GregorianCalendar(
                1970,
                1,
                1,
                0,
                0,
                0);
        lhsCalendar.add(GregorianCalendar.YEAR, getYears() * getSign());
        lhsCalendar.add(GregorianCalendar.MONTH, getMonths() * getSign());
        lhsCalendar.add(GregorianCalendar.DAY_OF_YEAR, getDays() * getSign());
        lhsCalendar.add(GregorianCalendar.HOUR_OF_DAY, getHours() * getSign());
        lhsCalendar.add(GregorianCalendar.MINUTE, getMinutes() * getSign());
        lhsCalendar.add(GregorianCalendar.SECOND, getSeconds() * getSign());
        
        // turn compare Duration into a GregorianCalendar
        GregorianCalendar rhsCalendar = new GregorianCalendar(
                1970,
                1,
                1,
                0,
                0,
                0);
        rhsCalendar.add(GregorianCalendar.YEAR, rhs.getYears() * rhs.getSign());
        rhsCalendar.add(GregorianCalendar.MONTH, rhs.getMonths() * rhs.getSign());
        rhsCalendar.add(GregorianCalendar.DAY_OF_YEAR, rhs.getDays() * rhs.getSign());
        rhsCalendar.add(GregorianCalendar.HOUR_OF_DAY, rhs.getHours() * rhs.getSign());
        rhsCalendar.add(GregorianCalendar.MINUTE, rhs.getMinutes() * rhs.getSign());
        rhsCalendar.add(GregorianCalendar.SECOND, rhs.getSeconds() * rhs.getSign());
        
        if (lhsCalendar.before(rhsCalendar)) {
            return DatatypeConstants.LESSER;
        }
        
        if (lhsCalendar.after(rhsCalendar)) {
            return DatatypeConstants.GREATER;
        }
        
        if (lhsCalendar.equals(rhsCalendar)) {
            return DatatypeConstants.EQUAL;
        }

        return DatatypeConstants.INDETERMINATE;
    }
    
    /**
     * Returns a hash code consistent with the definition of the equals method.
     * 
     * @see Object#hashCode() 
     */
    @Override
	public int hashCode() {
        // component wise hash is not correct because 1day = 24hours
    Calendar cal = TEST_POINTS[0].toGregorianCalendar();
    this.addTo(cal);
    return (int) getCalendarTimeInMillis(cal);
    }
    
    /**
     * Returns a string representation of this duration object.
     * 
     * <p>
     * The result is formatter according to the XML Schema 1.0
     * spec and can be always parsed back later into the
     * equivalent duration object by
     * the {@link #DurationImpl(String)} constructor.
     * 
     * <p>
     * Formally, the following holds for any {@link Duration}
     * object x. 
     * <pre>
     * new Duration(x.toString()).equals(x)
     * </pre>
     * 
     * @return
     *      Always return a non-null valid String object.
     */
    @Override
	public String toString() {
        StringBuffer buf = new StringBuffer();
        if (signum < 0) {
            buf.append('-');
        }
        buf.append('P');
        
        if (years != null) {
            buf.append(years + "Y");
        }
        if (months != null) {
            buf.append(months + "M");
        }
        if (days != null) {
            buf.append(days + "D");
        }

        if (hours != null || minutes != null || seconds != null) {
            buf.append('T');
            if (hours != null) {
                buf.append(hours + "H");
            }
            if (minutes != null) {
                buf.append(minutes + "M");
            }
            if (seconds != null) {
                buf.append(toString(seconds) + "S");
            }
        }
        
        return buf.toString();
    }

    /**
     * <p>Turns {@link BigDecimal} to a string representation.</p>
     * 
     * <p>Due to a behavior change in the {@link BigDecimal#toString()}
     * method in JDK1.5, this had to be implemented here.</p>
     * 
     * @param bd <code>BigDecimal</code> to format as a <code>String</code>
     * 
     * @return  <code>String</code> representation of <code>BigDecimal</code> 
     */
    private String toString(BigDecimal bd) {
        String intString = bd.unscaledValue().toString();
        int scale = bd.scale();

        if (scale == 0) {
            return intString;
        }

        /* Insert decimal point */
        StringBuffer buf;
        int insertionPoint = intString.length() - scale;
        if (insertionPoint == 0) { /* Point goes right before intVal */
            return "0." + intString;
        } else if (insertionPoint > 0) { /* Point goes inside intVal */
            buf = new StringBuffer(intString);
            buf.insert(insertionPoint, '.');
        } else { /* We must insert zeros between point and intVal */
            buf = new StringBuffer(3 - insertionPoint + intString.length());
            buf.append("0.");
            for (int i = 0; i < -insertionPoint; i++) {
                buf.append('0');
            }
            buf.append(intString);
        }
        return buf.toString();
    }

    /**
     * Checks if a field is set.
     * 
     * A field of a duration object may or may not be present.
     * This method can be used to test if a field is present.
     * 
     * @param field
     *      one of the six Field constants (YEARS,MONTHS,DAYS,HOURS,
     *      MINUTES, or SECONDS.)
     * @return
     *      true if the field is present. false if not.
     * 
     * @throws NullPointerException
     *      If the field parameter is null.
     */
    @Override
	public boolean isSet(DatatypeConstants.Field field) {
        
        if (field == null) {
            String methodName = "javax.xml.datatype.Duration" + "#isSet(DatatypeConstants.Field field)" ;
            throw new NullPointerException(                
                //"cannot be called with field == null"
                DatatypeMessageFormatter.formatMessage(null, "FieldCannotBeNull", new Object[]{methodName})                
            );
        }
        
        if (field == DatatypeConstants.YEARS) {
            return years != null;
        }

        if (field == DatatypeConstants.MONTHS) {
            return months != null;
        }

        if (field == DatatypeConstants.DAYS) {
            return days != null;
        }

        if (field == DatatypeConstants.HOURS) {
            return hours != null;
        }

        if (field == DatatypeConstants.MINUTES) {
            return minutes != null;
        }
        
        if (field == DatatypeConstants.SECONDS) {
            return seconds != null;
        }
        String methodName = "javax.xml.datatype.Duration" + "#isSet(DatatypeConstants.Field field)";
        
        throw new IllegalArgumentException(
            DatatypeMessageFormatter.formatMessage(null,"UnknownField", new Object[]{methodName, field.toString()})         
        );
        
    }
    
    /**
     * Gets the value of a field. 
     * 
     * Fields of a duration object may contain arbitrary large value.
     * Therefore this method is designed to return a {@link Number} object.
     * 
     * In case of YEARS, MONTHS, DAYS, HOURS, and MINUTES, the returned
     * number will be a non-negative integer. In case of seconds,
     * the returned number may be a non-negative decimal value.
     * 
     * @param field
     *      one of the six Field constants (YEARS,MONTHS,DAYS,HOURS,
     *      MINUTES, or SECONDS.)
     * @return
     *      If the specified field is present, this method returns
     *      a non-null non-negative {@link Number} object that
     *      represents its value. If it is not present, return null.
     *      For YEARS, MONTHS, DAYS, HOURS, and MINUTES, this method
     *      returns a {@link BigInteger} object. For SECONDS, this
     *      method returns a {@link BigDecimal}. 
     * 
     * @throws NullPointerException
     *      If the field parameter is null.
     */
    @Override
	public Number getField(DatatypeConstants.Field field) {

        if (field == null) {
            String methodName = "javax.xml.datatype.Duration" + "#isSet(DatatypeConstants.Field field) " ;
            
            throw new NullPointerException(             
                DatatypeMessageFormatter.formatMessage(null,"FieldCannotBeNull", new Object[]{methodName})
                );
        }
        
        if (field == DatatypeConstants.YEARS) {
            return years;
        }

        if (field == DatatypeConstants.MONTHS) {
            return months;
        }

        if (field == DatatypeConstants.DAYS) {
            return days;
        }

        if (field == DatatypeConstants.HOURS) {
            return hours;
        }

        if (field == DatatypeConstants.MINUTES) {
            return minutes;
        }
        
        if (field == DatatypeConstants.SECONDS) {
            return seconds;
        }
        /**
        throw new IllegalArgumentException(
            "javax.xml.datatype.Duration"
            + "#(getSet(DatatypeConstants.Field field) called with an unknown field: "
            + field.toString()
        );
        */
        String methodName = "javax.xml.datatype.Duration" + "#(getSet(DatatypeConstants.Field field)";
        
        throw new IllegalArgumentException(
            DatatypeMessageFormatter.formatMessage(null,"UnknownField", new Object[]{methodName, field.toString()})         
        );
        
    }
    
    /**
     * Obtains the value of the YEARS field as an integer value,
     * or 0 if not present.
     * 
     * <p>
     * This method is a convenience method around the 
     * {@link #getField(DatatypeConstants.Field)} method.
     * 
     * <p>
     * Note that since this method returns <tt>int</tt>, this
     * method will return an incorrect value for {@link Duration}s
     * with the year field that goes beyond the range of <tt>int</tt>.
     * Use <code>getField(YEARS)</code> to avoid possible loss of precision.</p>
     * 
     * @return
     *      If the YEARS field is present, return
     *      its value as an integer by using the {@link Number#intValue()}
     *      method. If the YEARS field is not present, return 0.
     */
    @Override
	public int getYears() {
        return getInt(DatatypeConstants.YEARS);
    }
    
    /**
     * Obtains the value of the MONTHS field as an integer value,
     * or 0 if not present.
     * 
     * This method works just like {@link #getYears()} except
     * that this method works on the MONTHS field.
     * 
     * @return Months of this <code>Duration</code>.
     */
    @Override
	public int getMonths() {
        return getInt(DatatypeConstants.MONTHS);
    }
    
    /**
     * Obtains the value of the DAYS field as an integer value,
     * or 0 if not present.
     * 
     * This method works just like {@link #getYears()} except
     * that this method works on the DAYS field.
     * 
     * @return Days of this <code>Duration</code>.
     */
    @Override
	public int getDays() {
        return getInt(DatatypeConstants.DAYS);
    }
    
    /**
     * Obtains the value of the HOURS field as an integer value,
     * or 0 if not present.
     * 
     * This method works just like {@link #getYears()} except
     * that this method works on the HOURS field.
     * 
     * @return Hours of this <code>Duration</code>.
     * 
     */
    @Override
	public int getHours() {
        return getInt(DatatypeConstants.HOURS);
    }
    
    /**
     * Obtains the value of the MINUTES field as an integer value,
     * or 0 if not present.
     * 
     * This method works just like {@link #getYears()} except
     * that this method works on the MINUTES field.
     * 
     * @return Minutes of this <code>Duration</code>.
     * 
     */
    @Override
	public int getMinutes() {
        return getInt(DatatypeConstants.MINUTES);
    }
    
    /**
     * Obtains the value of the SECONDS field as an integer value,
     * or 0 if not present.
     * 
     * This method works just like {@link #getYears()} except
     * that this method works on the SECONDS field.
     * 
     * @return seconds in the integer value. The fraction of seconds
     *   will be discarded (for example, if the actual value is 2.5,
     *   this method returns 2)
     */
    @Override
	public int getSeconds() {
        return getInt(DatatypeConstants.SECONDS);
    }
    
    /**
     * Obtains the value of the SECONDS field as an decimal value,
     * or 0 if not present.
     * 
     * @return seconds in the decimal value.
     */
    public BigDecimal getDecimalSeconds() {
    	if (null == seconds)
    	{
    		return new BigDecimal(0);
    	}
        return seconds;
    }
    
    public BigInteger getDaysAsBigInteger() {
    	if (null == days)
    	{
    		return BigInteger.ZERO;
    	}
        return days;
    }
    /**
     * <p>Return the requested field value as an int.</p>
     * 
     * <p>If field is not set, i.e. == null, 0 is returned.</p>
     * 
     * @param field To get value for.
     * 
     * @return int value of field or 0 if field is not set.
     */
    private int getInt(DatatypeConstants.Field field) {
        Number n = getField(field);
        if (n == null) {
            return 0;
        } else {
            return n.intValue();
        }
    }
        
    /**
     * <p>Returns the length of the duration in milli-seconds.</p>
     * 
     * <p>If the seconds field carries more digits than milli-second order,
     * those will be simply discarded (or in other words, rounded to zero.)  
     * For example, for any Calendar value <code>x<code>,</p>
     * <pre>
     * <code>new Duration("PT10.00099S").getTimeInMills(x) == 10000</code>.
     * <code>new Duration("-PT10.00099S").getTimeInMills(x) == -10000</code>.
     * </pre>
     * 
     * <p>
     * Note that this method uses the {@link #addTo(Calendar)} method,
     * which may work incorectly with {@link Duration} objects with
     * very large values in its fields. See the {@link #addTo(Calendar)}
     * method for details.
     * 
     * @param startInstant
     *      The length of a month/year varies. The <code>startInstant</code> is
     *      used to disambiguate this variance. Specifically, this method
     *      returns the difference between <code>startInstant</code> and
     *      <code>startInstant+duration</code>
     * 
     * @return milliseconds between <code>startInstant</code> and
     *   <code>startInstant</code> plus this <code>Duration</code>
     *
     * @throws NullPointerException if <code>startInstant</code> parameter 
     * is null.
     * 
     */
    @Override
	public long getTimeInMillis(final Calendar startInstant) {
        Calendar cal = (Calendar) startInstant.clone();
        addTo(cal);
        return getCalendarTimeInMillis(cal)
                    - getCalendarTimeInMillis(startInstant);
    }
    
    /**
     * <p>Returns the length of the duration in milli-seconds.</p>
     * 
     * <p>If the seconds field carries more digits than milli-second order,
     * those will be simply discarded (or in other words, rounded to zero.)
     * For example, for any <code>Date</code> value <code>x<code>,</p>   
     * <pre>
     * <code>new Duration("PT10.00099S").getTimeInMills(x) == 10000</code>.
     * <code>new Duration("-PT10.00099S").getTimeInMills(x) == -10000</code>.
     * </pre>
     * 
     * <p>
     * Note that this method uses the {@link #addTo(Date)} method,
     * which may work incorectly with {@link Duration} objects with
     * very large values in its fields. See the {@link #addTo(Date)}
     * method for details.
     * 
     * @param startInstant
     *      The length of a month/year varies. The <code>startInstant</code> is
     *      used to disambiguate this variance. Specifically, this method
     *      returns the difference between <code>startInstant</code> and
     *      <code>startInstant+duration</code>.
     * 
     * @throws NullPointerException
     *      If the startInstant parameter is null.
     * 
     * @return milliseconds between <code>startInstant</code> and
     *   <code>startInstant</code> plus this <code>Duration</code>
     *
     * @see #getTimeInMillis(Calendar)
     */
    @Override
	public long getTimeInMillis(final Date startInstant) {
        Calendar cal = new GregorianCalendar();
        cal.setTime(startInstant);
        this.addTo(cal);
        return getCalendarTimeInMillis(cal) - startInstant.getTime();
    }
    
//    /**
//     * Returns an equivalent but "normalized" duration value.
//     * 
//     * Intuitively, the normalization moves YEARS into
//     * MONTHS (by x12) and moves DAYS, HOURS, and MINUTES fields
//     * into SECONDS (by x86400, x3600, and x60 respectively.)
//     * 
//     * 
//     * Formally, this method satisfies the following conditions:
//     * <ul>
//     *  <li>x.normalize().equals(x)
//     *  <li>!x.normalize().isSet(Duration.YEARS)
//     *  <li>!x.normalize().isSet(Duration.DAYS)
//     *  <li>!x.normalize().isSet(Duration.HOURS)
//     *  <li>!x.normalize().isSet(Duration.MINUTES)
//     * </ul>
//     * 
//     * @return
//     *      always return a non-null valid value. 
//     */
//    public Duration normalize() {
//        return null;
//    }
    
    /**
     * <p>Converts the years and months fields into the days field
     * by using a specific time instant as the reference point.</p>
     * 
     * <p>For example, duration of one month normalizes to 31 days
     * given the start time instance "July 8th 2003, 17:40:32".</p>
     * 
     * <p>Formally, the computation is done as follows:</p>
     * <ol>
     *  <li>The given Calendar object is cloned.
     *  <li>The years, months and days fields will be added to
     *      the {@link Calendar} object
     *      by using the {@link Calendar#add(int,int)} method. 
     *  <li>The difference between two Calendars are computed in terms of days.
     *  <li>The computed days, along with the hours, minutes and seconds
     *      fields of this duration object is used to construct a new
     *      Duration object.
     * </ol>
     * 
     * <p>Note that since the Calendar class uses <code>int</code> to
     * hold the value of year and month, this method may produce
     * an unexpected result if this duration object holds
     * a very large value in the years or months fields.</p>
     *
     * @param startTimeInstant <code>Calendar</code> reference point.
     *  
     * @return <code>Duration</code> of years and months of this <code>Duration</code> as days.
     * 
     * @throws NullPointerException If the startTimeInstant parameter is null.
     */
    @Override
	public Duration normalizeWith(Calendar startTimeInstant) {
        
        Calendar c = (Calendar) startTimeInstant.clone();
        
        // using int may cause overflow, but 
        // Calendar internally treats value as int anyways.
    c.add(Calendar.YEAR, getYears() * signum);
        c.add(Calendar.MONTH, getMonths() * signum);
        c.add(Calendar.DAY_OF_MONTH, getDays() * signum);
        
        // obtain the difference in terms of days
        long diff = getCalendarTimeInMillis(c) - getCalendarTimeInMillis(startTimeInstant);
        int days = (int) (diff / (1000L * 60L * 60L * 24L));
        
        return new PatchedDurationImpl(
            days >= 0,
            null,
            null,
            wrap(Math.abs(days)),
            (BigInteger) getField(DatatypeConstants.HOURS),
            (BigInteger) getField(DatatypeConstants.MINUTES),
            (BigDecimal) getField(DatatypeConstants.SECONDS));
    }
    
    /**
     * <p>Computes a new duration whose value is <code>factor</code> times
     * longer than the value of this duration.</p>
     * 
     * <p>This method is provided for the convenience.
     * It is functionally equivalent to the following code:</p>
     * <pre>
     * multiply(new BigDecimal(String.valueOf(factor)))
     * </pre>
     * 
     * @param factor Factor times longer of new <code>Duration</code> to create.
     * 
     * @return New <code>Duration</code> that is <code>factor</code>times longer than this <code>Duration</code>.
     * 
     * @see #multiply(BigDecimal)
     */
    @Override
	public Duration multiply(int factor) {
        return multiply(BigDecimal.valueOf(factor));
    }
    
    /**
     * Computes a new duration whose value is <code>factor</code> times
     * longer than the value of this duration.
     * 
     * <p>
     * For example,
     * <pre>
     * "P1M" (1 month) * "12" = "P12M" (12 months)
     * "PT1M" (1 min) * "0.3" = "PT18S" (18 seconds)
     * "P1M" (1 month) * "1.5" = IllegalStateException
     * </pre>
     *  
     * <p>
     * Since the {@link Duration} class is immutable, this method
     * doesn't change the value of this object. It simply computes
     * a new Duration object and returns it.
     * 
     * <p>
     * The operation will be performed field by field with the precision
     * of {@link BigDecimal}. Since all the fields except seconds are
     * restricted to hold integers,
     * any fraction produced by the computation will be
     * carried down toward the next lower unit. For example,
     * if you multiply "P1D" (1 day) with "0.5", then it will be 0.5 day,
     * which will be carried down to "PT12H" (12 hours).
     * When fractions of month cannot be meaningfully carried down
     * to days, or year to months, this will cause an
     * {@link IllegalStateException} to be thrown. 
     * For example if you multiple one month by 0.5.</p>
     * 
     * <p>
     * To avoid {@link IllegalStateException}, use
     * the {@link #normalizeWith(Calendar)} method to remove the years
     * and months fields.
     * 
     * @param factor to multiply by
     * 
     * @return
     *      returns a non-null valid {@link Duration} object
     *
     * @throws IllegalStateException if operation produces fraction in 
     * the months field.
     *
     * @throws NullPointerException if the <code>factor</code> parameter is 
     * <code>null</code>.
     *
     */
    @Override
	public Duration multiply(BigDecimal factor) {
        BigDecimal carry = BigDecimal.ZERO;
        int factorSign = factor.signum();
        factor = factor.abs();
        
        BigDecimal[] buf = new BigDecimal[6];
        
        for (int i = 0; i < 5; i++) {
            BigDecimal bd = getFieldAsBigDecimal(FIELDS[i]);
            bd = bd.multiply(factor).add(carry);
            
            buf[i] = bd.setScale(0, BigDecimal.ROUND_DOWN);
            
            bd = bd.subtract(buf[i]);
            if (i == 1) {
                if (bd.signum() != 0) {
                    throw new IllegalStateException(); // illegal carry-down
                } else {
                    carry = BigDecimal.ZERO;
                }
            } else {
                carry = bd.multiply(FACTORS[i]);
            }
        }
        
        if (seconds != null) {
            buf[5] = seconds.multiply(factor).add(carry);
        } else {
            buf[5] = carry;
        }
                
        return new PatchedDurationImpl(
            this.signum * factorSign >= 0,
            toBigInteger(buf[0], null == years),
            toBigInteger(buf[1], null == months),
            toBigInteger(buf[2], null == days),
            toBigInteger(buf[3], null == hours),
            toBigInteger(buf[4], null == minutes),
            (buf[5].signum() == 0 && seconds == null) ? null : buf[5]);
    }
    
    /**
     * <p>Gets the value of the field as a {@link BigDecimal}.</p>
     * 
     * <p>If the field is unset, return 0.</p>
     * 
     * @param f Field to get value for.
     * 
     * @return  non-null valid {@link BigDecimal}.
     */
    private BigDecimal getFieldAsBigDecimal(DatatypeConstants.Field f) {
        if (f == DatatypeConstants.SECONDS) {
            if (seconds != null) {
                return seconds;
            } else {
                return BigDecimal.ZERO;
            }
        } else {
            BigInteger bi = (BigInteger) getField(f);
            if (bi == null) {
                return BigDecimal.ZERO;
            } else {
                return new BigDecimal(bi);
            }
        }
    }
    
    /**
     * <p>BigInteger value of BigDecimal value.</p>
     * 
     * @param value Value to convert.
     * @param canBeNull Can returned value be null?
     * 
     * @return BigInteger value of BigDecimal, possibly null.
     */
    private static BigInteger toBigInteger(
        BigDecimal value,
        boolean canBeNull) {
        if (canBeNull && value.signum() == 0) {
            return null;
        } else {
            return value.unscaledValue();
        }
    }
    
    /**
     * 1 unit of FIELDS[i] is equivalent to <code>FACTORS[i]</code> unit of
     * FIELDS[i+1].
     */
    private static final BigDecimal[] FACTORS = new BigDecimal[]{
        BigDecimal.valueOf(12),
        null/*undefined*/,
        BigDecimal.valueOf(24),
        BigDecimal.valueOf(60),
        BigDecimal.valueOf(60)
    };    
    
    /**
     * <p>Computes a new duration whose value is <code>this+rhs</code>.</p>
     * 
     * <p>For example,</p>
     * <pre>
     * "1 day" + "-3 days" = "-2 days"
     * "1 year" + "1 day" = "1 year and 1 day"
     * "-(1 hour,50 minutes)" + "-20 minutes" = "-(1 hours,70 minutes)"
     * "15 hours" + "-3 days" = "-(2 days,9 hours)"
     * "1 year" + "-1 day" = IllegalStateException
     * </pre>
     * 
     * <p>Since there's no way to meaningfully subtract 1 day from 1 month,
     * there are cases where the operation fails in
     * {@link IllegalStateException}.</p> 
     * 
     * <p>
     * Formally, the computation is defined as follows.</p>
     * <p>
     * Firstly, we can assume that two {@link Duration}s to be added
     * are both positive without losing generality (i.e.,
     * <code>(-X)+Y=Y-X</code>, <code>X+(-Y)=X-Y</code>,
     * <code>(-X)+(-Y)=-(X+Y)</code>)
     * 
     * <p>
     * Addition of two positive {@link Duration}s are simply defined as  
     * field by field addition where missing fields are treated as 0.
     * <p>
     * A field of the resulting {@link Duration} will be unset if and
     * only if respective fields of two input {@link Duration}s are unset. 
     * <p>
     * Note that <code>lhs.add(rhs)</code> will be always successful if
     * <code>lhs.signum()*rhs.signum()!=-1</code> or both of them are
     * normalized.</p>
     * 
     * @param rhs <code>Duration</code> to add to this <code>Duration</code>
     * 
     * @return
     *      non-null valid Duration object.
     * 
     * @throws NullPointerException
     *      If the rhs parameter is null.
     * @throws IllegalStateException
     *      If two durations cannot be meaningfully added. For
     *      example, adding negative one day to one month causes
     *      this exception.
     * 
     * 
     * @see #subtract(Duration)
     */
    @Override
	public Duration add(final Duration rhs) {
        Duration lhs = this;
        BigDecimal[] buf = new BigDecimal[6];
        
        buf[0] = sanitize((BigInteger) lhs.getField(DatatypeConstants.YEARS),
            lhs.getSign()).add(sanitize((BigInteger) rhs.getField(DatatypeConstants.YEARS),  rhs.getSign()));
        buf[1] = sanitize((BigInteger) lhs.getField(DatatypeConstants.MONTHS),
            lhs.getSign()).add(sanitize((BigInteger) rhs.getField(DatatypeConstants.MONTHS), rhs.getSign()));
        buf[2] = sanitize((BigInteger) lhs.getField(DatatypeConstants.DAYS),
            lhs.getSign()).add(sanitize((BigInteger) rhs.getField(DatatypeConstants.DAYS),   rhs.getSign()));
        buf[3] = sanitize((BigInteger) lhs.getField(DatatypeConstants.HOURS),
            lhs.getSign()).add(sanitize((BigInteger) rhs.getField(DatatypeConstants.HOURS),  rhs.getSign()));
        buf[4] = sanitize((BigInteger) lhs.getField(DatatypeConstants.MINUTES),
            lhs.getSign()).add(sanitize((BigInteger) rhs.getField(DatatypeConstants.MINUTES), rhs.getSign()));
        buf[5] = sanitize((BigDecimal) lhs.getField(DatatypeConstants.SECONDS),
            lhs.getSign()).add(sanitize((BigDecimal) rhs.getField(DatatypeConstants.SECONDS), rhs.getSign()));
        
        // align sign
        alignSigns(buf, 0, 2); // Y,M
        alignSigns(buf, 2, 6); // D,h,m,s
        
        // make sure that the sign bit is consistent across all 6 fields.
        int s = 0;
        for (int i = 0; i < 6; i++) {
            if (s * buf[i].signum() < 0) {
                throw new IllegalStateException();
            }
            if (s == 0) {
                s = buf[i].signum();
            }
        }
        
        return new PatchedDurationImpl(
            s >= 0,
            toBigInteger(sanitize(buf[0], s),
                lhs.getField(DatatypeConstants.YEARS) == null && rhs.getField(DatatypeConstants.YEARS) == null),
            toBigInteger(sanitize(buf[1], s),
                lhs.getField(DatatypeConstants.MONTHS) == null && rhs.getField(DatatypeConstants.MONTHS) == null),
            toBigInteger(sanitize(buf[2], s),
                lhs.getField(DatatypeConstants.DAYS) == null && rhs.getField(DatatypeConstants.DAYS) == null),
            toBigInteger(sanitize(buf[3], s),
                lhs.getField(DatatypeConstants.HOURS) == null && rhs.getField(DatatypeConstants.HOURS) == null),
            toBigInteger(sanitize(buf[4], s),
                lhs.getField(DatatypeConstants.MINUTES) == null && rhs.getField(DatatypeConstants.MINUTES) == null),
             (buf[5].signum() == 0
             && lhs.getField(DatatypeConstants.SECONDS) == null
             && rhs.getField(DatatypeConstants.SECONDS) == null) ? null : sanitize(buf[5], s));
    }
    
    private static void alignSigns(BigDecimal[] buf, int start, int end) {
        // align sign
        boolean touched;
        
        do { // repeat until all the sign bits become consistent
            touched = false;
            int s = 0; // sign of the left fields

            for (int i = start; i < end; i++) {
                if (s * buf[i].signum() < 0) {
                    // this field has different sign than its left field.
                    touched = true;

                    // compute the number of unit that needs to be borrowed.
                    BigDecimal borrow =
                        buf[i].abs().divide(
                            FACTORS[i - 1],
                            BigDecimal.ROUND_UP);
                    if (buf[i].signum() > 0) {
                        borrow = borrow.negate();
                    }

                    // update values
                    buf[i - 1] = buf[i - 1].subtract(borrow);
                    buf[i] = buf[i].add(borrow.multiply(FACTORS[i - 1]));
                }
                if (buf[i].signum() != 0) {
                    s = buf[i].signum();
                }
            }
        } while (touched);
    }
    
    /**
     * Compute <code>value*signum</code> where value==null is treated as
     * value==0.
     * @param value Value to sanitize.
     * @param signum 0 to sanitize to 0, > 0 to sanitize to <code>value</code>, < 0 to sanitize to negative <code>value</code>.
     *
     * @return non-null {@link BigDecimal}.
     */
    private static BigDecimal sanitize(BigInteger value, int signum) {
        if (signum == 0 || value == null) {
            return BigDecimal.ZERO;
        }
        if (signum > 0) {
            return new BigDecimal(value);
        }
        return new BigDecimal(value.negate());
    }
        
    /**
     * <p>Compute <code>value*signum</code> where <code>value==null</code> is treated as <code>value==0</code></p>.
     * 
     * @param value Value to sanitize.
     * @param signum 0 to sanitize to 0, > 0 to sanitize to <code>value</code>, < 0 to sanitize to negative <code>value</code>.
     * 
     * @return non-null {@link BigDecimal}.
     */
    static BigDecimal sanitize(BigDecimal value, int signum) {
        if (signum == 0 || value == null) {
            return BigDecimal.ZERO;
        }
        if (signum > 0) {
            return value;
        }
        return value.negate();
    }
    
    /**
     * <p>Computes a new duration whose value is <code>this-rhs</code>.</p>
     * 
     * <p>For example:</p>
     * <pre>
     * "1 day" - "-3 days" = "4 days"
     * "1 year" - "1 day" = IllegalStateException
     * "-(1 hour,50 minutes)" - "-20 minutes" = "-(1hours,30 minutes)"
     * "15 hours" - "-3 days" = "3 days and 15 hours"
     * "1 year" - "-1 day" = "1 year and 1 day"
     * </pre>
     * 
     * <p>Since there's no way to meaningfully subtract 1 day from 1 month,
     * there are cases where the operation fails in {@link IllegalStateException}.</p> 
     * 
     * <p>Formally the computation is defined as follows.
     * First, we can assume that two {@link Duration}s are both positive
     * without losing generality.  (i.e.,
     * <code>(-X)-Y=-(X+Y)</code>, <code>X-(-Y)=X+Y</code>,
     * <code>(-X)-(-Y)=-(X-Y)</code>)</p>
     *  
     * <p>Then two durations are subtracted field by field.
     * If the sign of any non-zero field <tt>F</tt> is different from
     * the sign of the most significant field,
     * 1 (if <tt>F</tt> is negative) or -1 (otherwise)
     * will be borrowed from the next bigger unit of <tt>F</tt>.</p>
     * 
     * <p>This process is repeated until all the non-zero fields have
     * the same sign.</p> 
     * 
     * <p>If a borrow occurs in the days field (in other words, if
     * the computation needs to borrow 1 or -1 month to compensate
     * days), then the computation fails by throwing an
     * {@link IllegalStateException}.</p>
     * 
     * @param rhs <code>Duration</code> to substract from this <code>Duration</code>.
     *  
     * @return New <code>Duration</code> created from subtracting <code>rhs</code> from this <code>Duration</code>.
     * 
     * @throws IllegalStateException
     *      If two durations cannot be meaningfully subtracted. For
     *      example, subtracting one day from one month causes
     *      this exception.
     * 
     * @throws NullPointerException
     *      If the rhs parameter is null.
     * 
     * @see #add(Duration)
     */
    @Override
	public Duration subtract(final Duration rhs) {
        return add(rhs.negate());
    }
    
    /**
     * Returns a new {@link Duration} object whose
     * value is <code>-this</code>.
     * 
     * <p>
     * Since the {@link Duration} class is immutable, this method
     * doesn't change the value of this object. It simply computes
     * a new Duration object and returns it.
     * 
     * @return
     *      always return a non-null valid {@link Duration} object.
     */
    @Override
	public Duration negate() {
        return new PatchedDurationImpl(
            signum <= 0,
            years,
            months,
            days,
            hours,
            minutes,
            seconds);
    }
    
    /**
     * Returns the sign of this duration in -1,0, or 1.
     * 
     * @return
     *      -1 if this duration is negative, 0 if the duration is zero,
     *      and 1 if the duration is postive.
     */
    public int signum() {
        return signum;
    }
    
    
    /**
     * Adds this duration to a {@link Calendar} object.
     * 
     * <p>
     * Calls {@link java.util.Calendar#add(int,int)} in the
     * order of YEARS, MONTHS, DAYS, HOURS, MINUTES, SECONDS, and MILLISECONDS
     * if those fields are present. Because the {@link Calendar} class
     * uses int to hold values, there are cases where this method
     * won't work correctly (for example if values of fields
     * exceed the range of int.) 
     * </p>
     * 
     * <p>
     * Also, since this duration class is a Gregorian duration, this
     * method will not work correctly if the given {@link Calendar}
     * object is based on some other calendar systems. 
     * </p>
     * 
     * <p>
     * Any fractional parts of this {@link Duration} object
     * beyond milliseconds will be simply ignored. For example, if
     * this duration is "P1.23456S", then 1 is added to SECONDS,
     * 234 is added to MILLISECONDS, and the rest will be unused. 
     * </p>
     * 
     * <p>
     * Note that because {@link Calendar#add(int, int)} is using
     * <tt>int</tt>, {@link Duration} with values beyond the
     * range of <tt>int</tt> in its fields
     * will cause overflow/underflow to the given {@link Calendar}.
     * {@link XMLGregorianCalendar#add(Duration)} provides the same
     * basic operation as this method while avoiding
     * the overflow/underflow issues.
     * 
     * @param calendar
     *      A calendar object whose value will be modified.
     * @throws NullPointerException
     *      if the calendar parameter is null.
     */
    @Override
	public void addTo(Calendar calendar) {
        calendar.add(Calendar.YEAR, getYears() * signum);
        calendar.add(Calendar.MONTH, getMonths() * signum);
        calendar.add(Calendar.DAY_OF_MONTH, getDays() * signum);
        calendar.add(Calendar.HOUR, getHours() * signum);
        calendar.add(Calendar.MINUTE, getMinutes() * signum);
        calendar.add(Calendar.SECOND, getSeconds() * signum);

        if (seconds != null) {
            BigDecimal fraction =
                seconds.subtract(seconds.setScale(0, BigDecimal.ROUND_DOWN));
            int millisec = fraction.movePointRight(3).intValue();
            calendar.add(Calendar.MILLISECOND, millisec * signum);
        }
    }
    
    /**
     * Adds this duration to a {@link Date} object.
     * 
     * <p>
     * The given date is first converted into
     * a {@link java.util.GregorianCalendar}, then the duration
     * is added exactly like the {@link #addTo(Calendar)} method.
     * 
     * <p>
     * The updated time instant is then converted back into a
     * {@link Date} object and used to update the given {@link Date} object.
     * 
     * <p>
     * This somewhat redundant computation is necessary to unambiguously
     * determine the duration of months and years.
     * 
     * @param date
     *      A date object whose value will be modified.
     * @throws NullPointerException
     *      if the date parameter is null.
     */
    @Override
	public void addTo(Date date) {
        Calendar cal = new GregorianCalendar();
        cal.setTime(date); // this will throw NPE if date==null
        this.addTo(cal);
        date.setTime(getCalendarTimeInMillis(cal));
    }
    
    /**
     * <p>Stream Unique Identifier.</p>
     * 
     * <p> TBD: Serialization should use the XML string representation as
     * the serialization format to ensure future compatibility.</p>
     */
    private static final long serialVersionUID = 1L; 
    
    /**
     * Writes {@link Duration} as a lexical representation
     * for maximum future compatibility.
     * 
     * @return
     *      An object that encapsulates the string
     *      returned by <code>this.toString()</code>.
     */
    private Object writeReplace() throws IOException {
        return new DurationStream(this.toString());
    }
    
    /**
     * Representation of {@link Duration} in the object stream.
     * 
     * @author Kohsuke Kawaguchi (kohsuke.kawaguchi@sun.com)
     */
    private static class DurationStream implements Serializable {
        private final String lexical;

        private DurationStream(String _lexical) {
            this.lexical = _lexical;
        }
        
        private Object readResolve() throws ObjectStreamException {
            //            try {
            return new PatchedDurationImpl(lexical);
            //            } catch( ParseException e ) {
            //                throw new StreamCorruptedException("unable to parse "+lexical+" as duration");
            //            }
        }
        
        private static final long serialVersionUID = 1L; 
    }
    
    /**
     * Calls the {@link Calendar#getTimeInMillis} method.
     * Prior to JDK1.4, this method was protected and therefore
     * cannot be invoked directly.
     * 
     * In future, this should be replaced by
     * <code>cal.getTimeInMillis()</code>
     */
    private static long getCalendarTimeInMillis(Calendar cal) {
        return cal.getTime().getTime();
    }
}

//-----------------------------------------------------------------------------
// $Log: PatchedDurationImpl.java,v $
// Revision 1.2  2010-10-04 15:33:56  stp
// Fixed header comment.
//
// Revision 1.1  2010-09-30 15:48:08  stp
// Released.
//