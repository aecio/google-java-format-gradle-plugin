package com.github.sherter.googlejavaformatgradleplugin

import org.gradle.api.tasks.TaskAction
import org.gradle.api.tasks.VerificationTask

import java.nio.charset.StandardCharsets
import java.util.concurrent.Executor
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicBoolean

class VerifyGoogleJavaFormat extends SourceStateTask implements VerificationTask {

    private static final int MAX_THREADS = 20;

    boolean ignoreFailures = false

    @TaskAction
    void verifySources() {
        String toolVersion = project.extensions.getByType(GoogleJavaFormatExtension).toolVersion
        Formatter formatter = new FormatterFactory(project, logger).create(toolVersion)
        Set<File> sourceFiles = getSource().getFiles()
        int numThreads = Math.min(sourceFiles.size(), MAX_THREADS)
        Executor executor = Executors.newFixedThreadPool(numThreads)

        AtomicBoolean success = new AtomicBoolean(true)
        sourceFiles.each { file ->
            if (!fileStateHandler.isUpToDate(file)) {
                executor.execute {
                    String content = file.getText(StandardCharsets.UTF_8.name())
                    try {
                        if (formatter.format(content) != content) {
                            success.set(false)
                            logger.error('{} is not formatted correctly', file)
                        }
                    } catch (FormatterException e) {
                        logger.error('{} is not a valid Java source file', file)
                        e.errors.each {
                            logger.info('{}:{}', file, it)
                        }
                    }
                }
            }
        }
        executor.shutdown()

        // blocks forever, unit is ignored if timeout is MAX_VALUE
        // (see http://docs.oracle.com/javase/7/docs/api/java/util/concurrent/package-summary.html)
        executor.awaitTermination(Long.MAX_VALUE, TimeUnit.DAYS)
        if (!ignoreFailures && !success.get()) {
            throw new RuntimeException("Found sources that are not formatted properly")
        }
    }
}
