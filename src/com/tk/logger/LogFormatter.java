package com.tk.logger;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Date;
import java.util.logging.Formatter;
import java.util.logging.LogRecord;

/**
 * 日志的记录类,此类用来按需要格式化输出的内容.
 * 
 * @author yimin
 *
 */
public class LogFormatter extends Formatter{
  private static final String format = "%1$tm-%1$td %1$tT.%1$tL %7$s %2$s%n%4$s: %5$s%6$s%n";
  
  private final Date dat = new Date();

	@Override
	public String format(LogRecord record) {
    dat.setTime(record.getMillis());
    String source;
    
    if (record.getSourceClassName() != null) {
        source = record.getSourceClassName();
        if (record.getSourceMethodName() != null) {
           source += " " + record.getSourceMethodName();
        }
    } else {
        source = record.getLoggerName();
    }
    String message = formatMessage(record);
    String throwable = "";
    if (record.getThrown() != null) {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        pw.println();
        record.getThrown().printStackTrace(pw);
        pw.close();
        throwable = sw.toString();
    }
    return String.format(format,
                         dat,
                         source,
                         record.getLoggerName(),
                         record.getLevel().getLocalizedName(),
                         message,
                         throwable,
                         Thread.currentThread().getId());
	}
}
