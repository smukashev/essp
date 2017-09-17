/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package kz.bsbnb.usci.receiver.tools

import org.apache.log4j.Layout
import org.apache.log4j.Level
import org.apache.log4j.helpers.Transform
import org.apache.log4j.spi.LocationInfo
import org.apache.log4j.spi.LoggingEvent

/**
 * Created by emles on 12.09.17
 */
class HTMLLayout extends Layout {

    /**
     * A string constant used in naming the option for setting the the
     * location information flag.  Current value of this string
     * constant is <b>LocationInfo</b>.
     * <p>
     * <p>Note that all option keys are case sensitive.
     *
     * @deprecated Options are now handled using the JavaBeans paradigm.
     * This constant is not longer needed and will be removed in the
     * <em>near</em> term.
     */
    static final String LOCATION_INFO_OPTION = "LocationInfo"

    /**
     * A string constant used in naming the option for setting the the
     * HTML document title.  Current value of this string
     * constant is <b>Title</b>.
     */
    static final String TITLE_OPTION = "Title"

    static String TRACE_PREFIX = "<br>&nbsp;&nbsp;&nbsp;&nbsp;"

    /**
     * This layout outputs events in a HTML table.
     * <p>
     * Appenders using this layout should have their encoding
     * set to UTF-8 or UTF-16, otherwise events containing
     * non ASCII characters could result in corrupted
     * log files.
     *
     * @author Ceki G&uuml;lc&uuml
     */

    protected final int BUF_SIZE = 256

    protected final int MAX_CAPACITY = 1024

    // Print no logger name by default
    boolean escapedLogger = false

    // Print no location info by default
    boolean locationInfo = false

    String title = "Log4J Log Messages"

    // output buffer appended to when format() is invoked
    private StringBuffer sbuf = new StringBuffer(BUF_SIZE)

    /**
     * Returns the current value of the <b>LocationInfo</b> option.
     */
    boolean getLocationInfo() {
        return locationInfo
    }

    /**
     * The <b>LocationInfo</b> option takes a boolean value. By
     * default, it is set to false which means there will be no location
     * information output by this layout. If the the option is set to
     * true, then the file name and line number of the statement
     * at the origin of the log statement will be output.
     * <p>
     * <p>If you are embedding this layout within an {@link
     * org.apache.log4j.net.SMTPAppender} then make sure to set the
     * <b>LocationInfo</b> option of that appender as well.
     */
    void setLocationInfo(boolean flag) {
        locationInfo = flag
    }

    /**
     * Returns the current value of the <b>Title</b> option.
     */
    String getTitle() {
        return title
    }

    /**
     * The <b>Title</b> option takes a String value. This option sets the
     * document title of the generated HTML document.
     * <p>
     * <p>Defaults to 'Log4J Log Messages'.
     */
    void setTitle(String title) {
        this.title = title
    }

    /**
     * Returns the content type output by this layout, i.e "text/html".
     */
    String getContentType() {
        return "text/html"
    }

    /**
     * No options to activate.
     */
    void activateOptions() {
    }

    String format(LoggingEvent event) {

        if (sbuf.capacity() > MAX_CAPACITY) {
            sbuf = new StringBuffer(BUF_SIZE)
        } else {
            sbuf.setLength(0)
        }

        sbuf.append(Layout.LINE_SEP + "<tr>" + Layout.LINE_SEP)

        sbuf.append("<td>")
        sbuf.append(event.timeStamp - LoggingEvent.getStartTime())
        sbuf.append("</td>" + Layout.LINE_SEP)

        String escapedThread = Transform.escapeTags(event.getThreadName())
        sbuf.append("<td>")
        sbuf.append(escapedThread)
        sbuf.append("</td>" + Layout.LINE_SEP)

        sbuf.append("<td>")
        if (event.getLevel().equals(Level.DEBUG)) {
            sbuf.append("<font color=\"#339933\">")
            sbuf.append(Transform.escapeTags(String.valueOf(event.getLevel())))
            sbuf.append("</font>")
        } else if (event.getLevel().isGreaterOrEqual(Level.WARN)) {
            sbuf.append("<font color=\"#993300\"><strong>")
            sbuf.append(Transform.escapeTags(String.valueOf(event.getLevel())))
            sbuf.append("</strong></font>")
        } else {
            sbuf.append(Transform.escapeTags(String.valueOf(event.getLevel())))
        }
        sbuf.append("</td>" + Layout.LINE_SEP)

        if (escapedLogger) {
            String escLogger = Transform.escapeTags(event.getLoggerName())
            sbuf.append("<td>")
            sbuf.append(escLogger)
            sbuf.append("</td>" + Layout.LINE_SEP)
        }

        if (locationInfo) {
            LocationInfo locInfo = event.getLocationInformation()
            sbuf.append("<td>")
            sbuf.append(Transform.escapeTags(locInfo.getFileName()))
            sbuf.append(':')
            sbuf.append(locInfo.getLineNumber())
            sbuf.append("</td>" + Layout.LINE_SEP)
        }

        sbuf.append("<td title=\"Message\">")
        //sbuf.append(Transform.escapeTags(event.getRenderedMessage()))
        sbuf.append(event.getRenderedMessage())
        sbuf.append("</td>" + Layout.LINE_SEP)
        sbuf.append("</tr>" + Layout.LINE_SEP)

        if (event.getNDC() != null) {
            sbuf.append("<tr><td bgcolor=\"#EEEEEE\" style=\"font-size : xx-small;\" colspan=\"6\" title=\"Nested Diagnostic Context\">")
            sbuf.append("NDC: " + Transform.escapeTags(event.getNDC()))
            sbuf.append("</td></tr>" + Layout.LINE_SEP)
        }

        String[] s = event.getThrowableStrRep()
        if (s != null) {
            sbuf.append("<tr><td bgcolor=\"#993300\" style=\"color:White; font-size : xx-small;\" colspan=\"6\">")
            appendThrowableAsHTML(s, sbuf)
            sbuf.append("</td></tr>" + Layout.LINE_SEP)
        }

        return sbuf.toString()
    }

    void appendThrowableAsHTML(String[] s, StringBuffer sbuf) {
        if (s != null) {
            int len = s.length
            if (len == 0)
                return
            sbuf.append(Transform.escapeTags(s[0]))
            sbuf.append(Layout.LINE_SEP)
            for (int i = 1; i < len; i++) {
                sbuf.append(TRACE_PREFIX)
                sbuf.append(Transform.escapeTags(s[i]))
                sbuf.append(Layout.LINE_SEP)
            }
        }
    }

    /**
     * Returns appropriate HTML headers.
     */
    String getHeader() {
        return """<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta charset="UTF-8">
<title>Log4J Log Messages</title>
<style type="text/css">
<!--
body, table {font-family: arial,sans-serif; font-size: x-small;}
th {background: #336699; color: #FFFFFF; text-align: left;}
td { text-align: left; vertical-align: top; }
-->
</style>
</head>
<body bgcolor="#FFFFFF" topmargin="6" leftmargin="6">
<hr size="1" noshade>
Log session start time Tue Sep 12 10:26:07 ALMT 2017<br>
<br>
<table cellspacing="0" cellpadding="4" border="1" bordercolor="#224466" width="100%">"""
    }

    /**
     * Returns the appropriate HTML footers.
     */
    String getFooter() {
        StringBuffer sbuf = new StringBuffer()
        sbuf.append("</table>" + Layout.LINE_SEP)
        sbuf.append("<br>" + Layout.LINE_SEP)
        sbuf.append("</body></html>")
        return sbuf.toString()
    }

    /**
     * The HTML layout handles the throwable contained in logging
     * events. Hence, this method return <code>false</code>.
     */
    boolean ignoresThrowable() {
        return false
    }

}



