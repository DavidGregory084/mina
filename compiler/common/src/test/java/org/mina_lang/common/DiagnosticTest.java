package org.mina_lang.common;

import org.junit.jupiter.api.Test;
import org.mina_lang.common.diagnostics.Diagnostic;

import com.jparams.verifier.tostring.ToStringVerifier;

import nl.jqno.equalsverifier.EqualsVerifier;

public class DiagnosticTest {
    @Test
    void testEquals() {
        EqualsVerifier
                .forClass(Diagnostic.class)
                .usingGetClass()
                .withIgnoredFields("backtrace", "detailMessage", "cause", "stackTrace", "depth", "suppressedExceptions") // Fields of Throwable
                .verify();
    }

    @Test
    void testToString() {
        ToStringVerifier
                .forClass(Diagnostic.class)
                .withIgnoredFields("backtrace", "detailMessage", "cause", "stackTrace", "depth", "suppressedExceptions") // Fields of Throwable
                .verify();
    }
}
