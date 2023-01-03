package org.mina_lang.codegen.jvm.scopes;

import org.objectweb.asm.Label;

public interface VarBindingScope extends CodegenScope {
    Label startLabel();

    Label endLabel();
}
