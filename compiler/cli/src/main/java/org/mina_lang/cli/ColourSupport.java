package org.mina_lang.cli;

import picocli.CommandLine.Help.Ansi;

public class ColourSupport {
    private static final boolean isSupported;

    static {
       isSupported = Ansi.AUTO.enabled();
    }

    public static boolean isSupported() {
        return isSupported;
    }
}
