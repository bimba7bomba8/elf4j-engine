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

package elf4j.engine.writer.pattern;

import elf4j.Level;
import elf4j.engine.NativeLogger;
import elf4j.engine.service.LogEntry;
import elf4j.engine.service.LogService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@ExtendWith(MockitoExtension.class)
class MessageAndExceptionPatternSegmentTest {
    @Mock LogService stubLogService;
    LogEntry mockLogEntry;
    String mockMessage = "testLogMessage {}";
    Object[] mockArgs = new Object[] { "testArg1" };
    Exception mockException = new Exception("testExceptionMessage");

    @BeforeEach
    void beforeEach() {
        mockLogEntry = LogEntry.builder()
                .nativeLogger(new NativeLogger("testLoggerName", Level.ERROR, stubLogService))
                .callerThread(LogEntry.ThreadInformation.builder()
                        .name(Thread.currentThread().getName())
                        .id(Thread.currentThread().getId())
                        .build())
                .callerFrame(LogEntry.StackTraceFrame.builder()
                        .className(JsonPatternSegment.class.getName())
                        .methodName("testMethod")
                        .lineNumber(42)
                        .fileName("testFileName")
                        .build())
                .message(mockMessage)
                .arguments(mockArgs)
                .exception(mockException)
                .build();
    }

    @Nested
    class from {
        @Test
        void errorOnInvalidPatternText() {
            assertThrows(IllegalArgumentException.class,
                    () -> MessageAndExceptionPatternSegment.from("badPatternText"));
        }
    }

    @Nested
    class render {
        @Test
        void includeBothMessageAndException() {
            MessageAndExceptionPatternSegment messageAndExceptionPatternSegment =
                    MessageAndExceptionPatternSegment.from("message");
            StringBuilder logText = new StringBuilder();

            messageAndExceptionPatternSegment.render(mockLogEntry, logText);
            String rendered = logText.toString();

            assertTrue(rendered.contains(mockLogEntry.getResolvedMessage()));
            assertTrue(rendered.contains(mockException.getMessage()));
        }
    }
}