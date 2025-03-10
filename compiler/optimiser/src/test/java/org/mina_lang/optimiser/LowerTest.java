/*
 * SPDX-FileCopyrightText:  © 2025 David Gregory
 * SPDX-License-Identifier: Apache-2.0
 */
package org.mina_lang.optimiser;

import net.jqwik.api.ForAll;
import net.jqwik.api.Property;
import net.jqwik.api.ShrinkingMode;
import org.mina_lang.common.Attributes;
import org.mina_lang.common.names.SyntheticNameSupply;
import org.mina_lang.syntax.NamespaceNode;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

public class LowerTest {
    @Property(shrinking = ShrinkingMode.OFF)
    public void lowersArbitraryNamespaces(@ForAll NamespaceNode<Attributes> namespace) {
        var nameSupply = new SyntheticNameSupply();
        var lower = new Lower(nameSupply);
        assertDoesNotThrow(() -> lower.lower(namespace));
    }
}
