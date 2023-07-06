/*
 * SPDX-FileCopyrightText:  © 2022 David Gregory
 * SPDX-License-Identifier: Apache-2.0
 */
package org.mina_lang.langserver;

import org.eclipse.lsp4j.Position;

import java.util.Comparator;

public class PositionComparator implements Comparator<Position> {
    @Override
    public int compare(Position left, Position right) {
        var lineComparison = Integer.compare(left.getLine(), right.getLine());

        if (lineComparison != 0) {
            return lineComparison;
        } else {
            return Integer.compare(left.getCharacter(), right.getCharacter());
        }
    }
}
