package org.mina_lang.codegen.jvm.scopes;

import java.util.concurrent.atomic.AtomicInteger;

public interface LambdaLiftingScope extends JavaMethodScope {
    AtomicInteger nextLambdaId();
}
