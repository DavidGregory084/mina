package org.mina_lang.gradle;

import org.gradle.api.file.ConfigurableFileCollection;
import org.gradle.api.file.DirectoryProperty;
import org.gradle.api.provider.Property;
import org.gradle.workers.WorkParameters;

public interface MinaCompileParameters extends WorkParameters {
    Property<String> getCompilerClassName();
    Property<MinaCompileOptions> getMinaCompileOptions();
    ConfigurableFileCollection getClasspath();
    DirectoryProperty getDestinationDirectory();
    ConfigurableFileCollection getSourceFiles();
}
