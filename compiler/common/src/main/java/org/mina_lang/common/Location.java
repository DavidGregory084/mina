/*
 * SPDX-FileCopyrightText:  © 2023 David Gregory
 * SPDX-License-Identifier: Apache-2.0
 */
package org.mina_lang.common;

import com.opencastsoftware.yvette.Range;

import java.net.URI;

public record Location(URI uri, Range range) {

}
