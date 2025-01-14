package mpo.dayon.common;

import mpo.dayon.assistant.AssistantRunner;
import mpo.dayon.assisted.AssistedRunner;
import mpo.dayon.common.error.FatalErrorHandler;
import mpo.dayon.common.log.Log;

import javax.swing.*;
import java.io.File;
import java.util.Arrays;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

import static mpo.dayon.common.utils.SystemUtilities.*;

public interface Runner {

    public static void main(String[] args) {
        Runner.setDebug(args);
        Runner.disableDynamicScale();
        Runner.getOrCreateAppHomeDir();
        Map<String, String> programArgs = Runner.extractProgramArgs(args);
        Runner.overrideLocale(programArgs.get("lang"));
        if (hasAssistant(args)) {
            Runner.logAppInfo("dayon_assistant");
            try {
                SwingUtilities.invokeLater(AssistantRunner::launchAssistant);
            } catch (Exception ex) {
                FatalErrorHandler.bye("The assistant is dead!", ex);
            }
        } else {
            Runner.logAppInfo("dayon_assisted");
            try {
                SwingUtilities.invokeLater(() -> AssistedRunner.launchAssisted(programArgs.get("ah"), programArgs.get("ap")));
            } catch (Exception ex) {
                FatalErrorHandler.bye("The assisted is dead!", ex);
            }
        }
    }

    static void logAppInfo(String appName) {
        System.setProperty("dayon.application.name", appName);
        Log.info("============================================================================================");
        getSystemProperties().forEach(Log::info);
        Log.info("============================================================================================");
    }

    static Map<String, String> extractProgramArgs(String[] args) {
        return Arrays.stream(args)
                .map(arg -> arg
                .replace(",", "")
                .trim()
                .split("="))
                .filter(pair -> pair.length == 2)
                .collect(Collectors.toMap(pair -> pair[0], pair -> pair[1], (a, b) -> b));
    }

    static void overrideLocale(String arg) {
        final String[] supported = {"de", "en", "es", "fr", "it", "ru", "tr", "zh"};
        if (arg != null && Arrays.stream(supported).anyMatch(e -> e.equalsIgnoreCase(arg))) {
            Locale.setDefault(new Locale(arg));
        }
    }

    static void setDebug(String[] args) {
        if (Arrays.stream(args).anyMatch(a -> a.equalsIgnoreCase("debug"))) {
            System.setProperty("dayon.debug", "on");
        }
    }

    static boolean hasAssistant(String[] args) {
        return Arrays.stream(args).anyMatch(a -> a.equalsIgnoreCase("assistant"));
    }

    static void disableDynamicScale() {
        System.setProperty("sun.java2d.uiScale", "1");
    }

    static File getOrCreateAppHomeDir() {
        final String homeDir = System.getProperty("user.home"); // *.log4j.xml are using that one (!)
        if (homeDir == null) {
            Log.warn("Home directory [user.home] is null!");
            return null;
        }

        final File home = new File(homeDir);
        if (!home.isDirectory()) {
            Log.warn("Home directory [" + homeDir + "] is not a directory!");
            return null;
        }

        File appHomeDir;
        if (isSnapped()) {
            final String classPath = System.getProperty(JAVA_CLASS_PATH);
            final String userDataDir = String.format("%s%s", homeDir, classPath.substring(0, classPath.indexOf("/jar/dayon.jar")));
            appHomeDir = new File(userDataDir, ".dayon");
        } else {
            appHomeDir = new File(home, ".dayon");
        }

        if (!appHomeDir.exists() && !appHomeDir.mkdir()) {
            Log.warn("Could not create the application directory [" + appHomeDir.getAbsolutePath() + "]!");
            return home;
        }
        System.setProperty("dayon.home", appHomeDir.getAbsolutePath());
        return appHomeDir;
    }
}
