package org.mina_lang.codegen.jvm;

import org.objectweb.asm.Label;

public record LocalVar(
        int access,
        int index,
        String name,
        String descriptor,
        String signature,
        Label startLabel,
        Label endLabel) {

}
