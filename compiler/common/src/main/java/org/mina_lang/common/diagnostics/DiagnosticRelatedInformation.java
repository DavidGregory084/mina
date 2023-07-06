/*
 * SPDX-FileCopyrightText:  © 2022-2023 David Gregory
 * SPDX-License-Identifier: Apache-2.0
 */
package org.mina_lang.common.diagnostics;

import org.mina_lang.common.Location;

public record DiagnosticRelatedInformation(Location location, String message) {

}
