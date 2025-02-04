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

package elf4j.engine.service;

import elf4j.Level;
import elf4j.engine.NativeLogger;
import elf4j.engine.configuration.LogServiceConfiguration;
import elf4j.engine.writer.LogWriter;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Objects;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.never;

@ExtendWith(MockitoExtension.class)
class DefaultLogServiceTest {

    private static class StubWriterThread implements WriterThread {
        @Override
        public void shutdown() {
        }

        @Override
        public void execute(Runnable command) {
            command.run();
        }
    }

    @Nested
    class isEnabled {
        NativeLogger stubLogger;
        DefaultLogService logService;
        @Mock LogServiceConfiguration mockLogServiceConfiguration;
        @Mock WriterThread mockWriterThread;

        @Test
        void delegateToConfiguration() {
            logService = new DefaultLogService(mockLogServiceConfiguration, mockWriterThread);
            stubLogger = new NativeLogger(this.getClass().getName(), Level.TRACE, logService);

            logService.isEnabled(stubLogger);

            then(mockLogServiceConfiguration).should().isEnabled(stubLogger);
        }
    }

    @Nested
    class log {
        NativeLogger stubLogger;
        DefaultLogService logService;
        @Mock LogServiceConfiguration mockLogServiceConfiguration;
        @Mock LogWriter mockLogWriter;
        @Mock WriterThread mockWriterThread;
        @Captor ArgumentCaptor<LogEntry> captorLogEntry;

        @Test
        void async() {
            logService = new DefaultLogService(mockLogServiceConfiguration, mockWriterThread);
            stubLogger = new NativeLogger(this.getClass().getName(), Level.TRACE, logService);
            given(mockLogServiceConfiguration.isEnabled(any(NativeLogger.class))).willReturn(true);
            given(mockLogServiceConfiguration.getLogServiceWriter()).willReturn(mockLogWriter);

            logService.log(stubLogger, this.getClass(), null, null, null);

            then(mockWriterThread).should().execute(any(Runnable.class));
        }

        @Test
        void callThreadRequired() {
            logService = new DefaultLogService(mockLogServiceConfiguration, new StubWriterThread());
            stubLogger = new NativeLogger(this.getClass().getName(), Level.TRACE, logService);
            given(mockLogWriter.includeCallerThread()).willReturn(true);
            given(mockLogServiceConfiguration.isEnabled(any(NativeLogger.class))).willReturn(true);
            given(mockLogServiceConfiguration.getLogServiceWriter()).willReturn(mockLogWriter);

            logService.log(stubLogger, this.getClass(), null, null, null);

            then(mockLogWriter).should().write(captorLogEntry.capture());
            assertEquals(Thread.currentThread().getName(),
                    Objects.requireNonNull(captorLogEntry.getValue().getCallerThread()).getName());
            assertEquals(Thread.currentThread().getId(), captorLogEntry.getValue().getCallerThread().getId());
        }

        @Test
        void callThreadNotRequired() {
            logService = new DefaultLogService(mockLogServiceConfiguration, new StubWriterThread());
            stubLogger = new NativeLogger(this.getClass().getName(), Level.TRACE, logService);
            given(mockLogWriter.includeCallerThread()).willReturn(false);
            given(mockLogServiceConfiguration.isEnabled(any(NativeLogger.class))).willReturn(true);
            given(mockLogServiceConfiguration.getLogServiceWriter()).willReturn(mockLogWriter);

            logService.log(stubLogger, this.getClass(), null, null, null);

            then(mockLogWriter).should().write(captorLogEntry.capture());
            assertNull(captorLogEntry.getValue().getCallerThread());
        }

        @Test
        void callerDetailRequired() {
            logService = new DefaultLogService(mockLogServiceConfiguration, new StubWriterThread());
            stubLogger = new NativeLogger(this.getClass().getName(), Level.TRACE, logService);
            given(mockLogServiceConfiguration.isEnabled(any(NativeLogger.class))).willReturn(true);
            given(mockLogServiceConfiguration.getLogServiceWriter()).willReturn(mockLogWriter);
            given(mockLogWriter.includeCallerDetail()).willReturn(true);

            logService.log(stubLogger, this.getClass(), null, null, null);

            then(mockLogWriter).should().write(captorLogEntry.capture());
            assertNotNull(captorLogEntry.getValue().getCallerFrame());
        }

        @Test
        void callDetailNotRequired() {
            logService = new DefaultLogService(mockLogServiceConfiguration, new StubWriterThread());
            stubLogger = new NativeLogger(this.getClass().getName(), Level.TRACE, logService);
            given(mockLogServiceConfiguration.isEnabled(any(NativeLogger.class))).willReturn(true);
            given(mockLogWriter.includeCallerDetail()).willReturn(false);
            given(mockLogServiceConfiguration.getLogServiceWriter()).willReturn(mockLogWriter);

            logService.log(stubLogger, this.getClass(), null, null, null);

            then(mockLogWriter).should().write(captorLogEntry.capture());
            assertNull(captorLogEntry.getValue().getCallerFrame());
        }

        @Test
        void onlyLogWhenEnabled() {
            logService = new DefaultLogService(mockLogServiceConfiguration, new StubWriterThread());
            stubLogger = new NativeLogger(this.getClass().getName(), Level.TRACE, logService);
            given(mockLogServiceConfiguration.isEnabled(any(NativeLogger.class))).willReturn(false);

            logService.log(stubLogger, this.getClass(), null, null, null);

            then(mockLogServiceConfiguration).should(never()).getLogServiceWriter();
        }
    }
}