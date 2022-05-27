package org.mina_lang.gradle;

import org.gradle.api.internal.tasks.compile.JvmLanguageCompileSpec;

public interface MinaCompileSpec extends JvmLanguageCompileSpec {
    MinimalMinaCompileOptions getMinaCompileOptions();
}