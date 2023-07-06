/*
 * SPDX-FileCopyrightText:  © 2023 David Gregory
 * SPDX-License-Identifier: Apache-2.0
 */
package org.mina_lang.common;

import com.jparams.verifier.tostring.ToStringVerifier;
import nl.jqno.equalsverifier.EqualsVerifier;
import org.junit.jupiter.api.Test;
import org.mina_lang.common.diagnostics.Diagnostic;

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
