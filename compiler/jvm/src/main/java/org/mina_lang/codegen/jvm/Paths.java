package org.mina_lang.codegen.jvm;

import java.nio.file.Path;

import org.mina_lang.common.names.ConstructorName;
import org.mina_lang.common.names.DataName;
import org.mina_lang.common.names.NamespaceName;


public class Paths {
    public static Path namespacePackagePath(Path enclosing, NamespaceName nsName) {
        var path = enclosing;
        for (var segment : nsName.pkg()) {
            path = path.resolve(segment);
        }
        path = path.resolve(nsName.name());
        return path;
    }

    public static Path namespacePath(Path enclosing, NamespaceName nsName) {
        return namespacePackagePath(enclosing, nsName).resolve("$namespace.class");
    }

    public static Path dataPath(Path enclosing, DataName dataName) {
        var pkgPath = namespacePackagePath(enclosing, dataName.name().ns());
        return pkgPath.resolve(dataName.name().name() + ".class");
    }

    public static Path constructorPath(Path enclosing, ConstructorName constrName) {
        var pkgPath = namespacePackagePath(enclosing, constrName.name().ns());
        return pkgPath.resolve(constrName.name().name() + ".class");
    }
}
