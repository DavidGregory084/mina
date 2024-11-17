/*
 * SPDX-FileCopyrightText:  © 2024 David Gregory
 * SPDX-License-Identifier: Apache-2.0
 */
package org.mina_lang.proto;

import net.jqwik.api.ForAll;
import net.jqwik.api.Property;
import net.jqwik.api.ShrinkingMode;
import org.mina_lang.common.Attributes;
import org.mina_lang.syntax.NamespaceNode;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;

public class ProtobufReaderWriterTest {
    ProtobufWriter writer = new ProtobufWriter();
    ProtobufReader reader = new ProtobufReader();

    @Property(shrinking = ShrinkingMode.OFF)
    void roundTripsArbitraryNamespace(@ForAll NamespaceNode<Attributes> namespace) {
        var originalScope = namespace.getScope();
        var roundTripScope = reader.fromProto(writer.toProto(originalScope));
        assertThat(roundTripScope.values(), is(equalTo(originalScope.values())));
        assertThat(roundTripScope.types(), is(equalTo(originalScope.types())));
        assertThat(roundTripScope.fields(), is(equalTo(originalScope.fields())));
    }
}
