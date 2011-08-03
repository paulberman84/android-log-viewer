package org.bitbucket.mlopatkin.android.logviewer;

import java.awt.EventQueue;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.text.StrMatcher;
import org.apache.commons.lang3.text.StrTokenizer;
import org.bitbucket.mlopatkin.android.liblogcat.LogRecord;
import org.bitbucket.mlopatkin.android.liblogcat.LogRecordStream;

public class AdbDataSource {
    private static final String ADB_BASE_COMMANDLINE = Configuration.adb.commandline();

    private LogRecordStream input;
    private LogRecordDataSourceListener listener;
    private Process adbProcess;

    public AdbDataSource(LogRecordDataSourceListener listener) {
        this.listener = listener;
        ProcessBuilder pb = new ProcessBuilder(makeCommandLine());

        try {
            adbProcess = pb.start();
            input = new LogRecordStream(adbProcess.getInputStream());
            (new AdbPollingThread()).start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void close() {
        adbProcess.destroy();
    }

    private List<String> makeCommandLine() {
        List<String> commands = new ArrayList<String>(splitCommandLine(ADB_BASE_COMMANDLINE));
        for (String buffer : Configuration.adb.buffers()) {
            commands.add(Configuration.adb.bufferswitch());
            commands.add(buffer);
        }
        return commands;
    }

    private static List<String> splitCommandLine(String commandLine) {
        StrTokenizer tokenizer = new StrTokenizer(commandLine, StrMatcher.splitMatcher(),
                StrMatcher.quoteMatcher());
        return tokenizer.getTokenList();
    }

    private void pushRecord(final LogRecord record) {
        EventQueue.invokeLater(new Runnable() {
            @Override
            public void run() {
                listener.onNewRecord(record);
            }
        });
    }

    private class AdbPollingThread extends Thread {
        AdbPollingThread() {
            super("ADB-Polling");
            setDaemon(true);
        }

        @Override
        public void run() {
            LogRecord record = input.next();
            while (record != null) {
                pushRecord(record);
                record = input.next();
            }
        }
    }
}