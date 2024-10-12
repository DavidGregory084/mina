package org.mina_lang.testing;

import org.eclipse.collections.api.stack.MutableStack;

public record GenEnvironment(
    MutableStack<GenScope> scopes
) {}
