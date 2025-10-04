/*
 * SPDX-FileCopyrightText:  © 2024 David Gregory
 * SPDX-License-Identifier: Apache-2.0
 */
package org.mina_lang.codegen.jvm;

import org.mina_lang.common.Attributes;
import org.mina_lang.common.Meta;
import org.mina_lang.common.Scope;
import org.mina_lang.common.names.NamespaceName;
import org.mina_lang.proto.Environment;
import org.mina_lang.proto.ProtobufReader;
import org.objectweb.asm.Attribute;
import org.objectweb.asm.ClassReader;

import java.io.IOException;

public class NamespaceReader {
    private static final Attribute[] prototypes = new Attribute[] {
        new Asm.EnvironmentAttribute(new byte[0])
    };

    private static final ProtobufReader protobufReader = new ProtobufReader();

    public static Scope<Meta<Attributes>> readScope(ClassLoader classLoader, NamespaceName namespace) throws IOException {
        var className = Names.getInternalName(namespace) + ".class";
        try (var classData = classLoader.getResourceAsStream(className)) {
            if (classData == null) { return null; }
            var classReader = new ClassReader(classData);
            var attrVisitor = new Asm.EnvironmentAttributeVisitor();
            classReader.accept(attrVisitor, prototypes, 0);
            var protoEnv = Environment.parseFrom(attrVisitor.data());
            return protobufReader.fromProto(protoEnv);
        }
    }
}
