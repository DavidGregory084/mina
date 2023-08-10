/*
 * SPDX-FileCopyrightText:  © 2023 David Gregory
 * SPDX-License-Identifier: Apache-2.0
 */
package org.mina_lang.langserver.util;

import org.slf4j.Logger;

import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicLong;

public class DaemonThreadFactory implements ThreadFactory {
    private final Logger logger;
    private final String nameFormat;
    private final AtomicLong count = new AtomicLong(0);
    private final Thread.UncaughtExceptionHandler uncaughtExceptionHandler;

    private DaemonThreadFactory(Logger logger, String nameFormat) {
       this.logger = logger;
       this.nameFormat = nameFormat;
       this.uncaughtExceptionHandler = (t, ex) -> {
           this.logger.error("Uncaught exception in thread {}", t.getName(), ex);
       };
    }

    @Override
    public Thread newThread(Runnable runnable) {
        var thread = Executors.defaultThreadFactory().newThread(runnable);
        thread.setName(String.format(nameFormat, count.getAndIncrement()));
        thread.setUncaughtExceptionHandler(uncaughtExceptionHandler);
        thread.setDaemon(true);
        return thread;
    }

    public static ThreadFactory create(Logger logger, String nameFormat) {
        return new DaemonThreadFactory(logger, nameFormat);
    }
}
