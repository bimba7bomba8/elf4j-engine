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

import elf4j.engine.service.LogEntry;
import lombok.NonNull;
import lombok.Value;

import javax.annotation.Nonnull;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

/**
 *
 */
@Value
public class TimestampPatternSegment implements LogPattern {
    private static final DateTimeFormatter DEFAULT_TIMESTAMP_FORMATTER =
            DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSZZZZZ");
    private static final ZoneId DEFAULT_TIMESTAMP_ZONE = ZoneId.systemDefault();
    DateTimeFormatter dateTimeFormatter;

    /**
     * @param patternSegment
     *         text pattern segment to convert
     * @return converted pattern segment object
     */
    @Nonnull
    public static TimestampPatternSegment from(@NonNull String patternSegment) {
        if (!PatternSegmentType.TIMESTAMP.isTargetTypeOf(patternSegment)) {
            throw new IllegalArgumentException("patternSegment: " + patternSegment);
        }
        DateTimeFormatter dateTimeFormatter = PatternSegmentType.getPatternSegmentOption(patternSegment)
                .map(DateTimeFormatter::ofPattern)
                .orElse(DEFAULT_TIMESTAMP_FORMATTER);
        if (dateTimeFormatter.getZone() == null) {
            dateTimeFormatter = dateTimeFormatter.withZone(DEFAULT_TIMESTAMP_ZONE);
        }
        return new TimestampPatternSegment(dateTimeFormatter);
    }

    @Override
    public boolean includeCallerDetail() {
        return false;
    }

    @Override
    public boolean includeCallerThread() {
        return false;
    }

    @Override
    public void render(@NonNull LogEntry logEntry, @NonNull StringBuilder target) {
        dateTimeFormatter.formatTo(logEntry.getTimestamp(), target);
    }
}
