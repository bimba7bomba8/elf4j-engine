/*
 * MIT License
 *
 * Copyright (c) 2023 Qingtian Wang
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 *
 */

package elf4j.engine.util;

import elf4j.engine.service.LogEntry;
import lombok.NonNull;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.NoSuchElementException;

/**
 *
 */
public class StackTraceUtils {
    private StackTraceUtils() {
    }

    /**
     * @param calleeClass
     *         whose caller is being searched for
     * @return immediate caller frame of the specified callee class
     */
    public static LogEntry.StackTraceFrame callerOf(@NonNull Class<?> calleeClass) {
        StackTraceElement[] stackTrace = new Throwable().getStackTrace();
        int depth = 0;
        String calleeClassName = calleeClass.getName();
        for (; depth < stackTrace.length; depth++) {
            if (stackTrace[depth].getClassName().equals(calleeClassName)) {
                depth++;
                break;
            }
        }
        for (; depth < stackTrace.length; depth++) {
            if (!stackTrace[depth].getClassName().equals(calleeClassName)) {
                StackTraceElement stackTraceElement = stackTrace[depth];
                return LogEntry.StackTraceFrame.builder()
                        .className(stackTraceElement.getClassName())
                        .methodName(stackTraceElement.getMethodName())
                        .lineNumber(stackTraceElement.getLineNumber())
                        .fileName(stackTraceElement.getFileName())
                        .build();
            }
        }
        throw new NoSuchElementException("Caller of class '" + calleeClass + "' not found in call stack");
    }

    /**
     * @param throwable
     *         to extract stack trace text from
     * @return stack trace buffer as the specified throwable prints it
     */
    public static StringBuffer getTraceAsBuffer(@NonNull Throwable throwable) {
        StringWriter stringWriter = new StringWriter();
        try (PrintWriter printWriter = new PrintWriter(stringWriter)) {
            throwable.printStackTrace(printWriter);
            return stringWriter.getBuffer();
        }
    }
}
