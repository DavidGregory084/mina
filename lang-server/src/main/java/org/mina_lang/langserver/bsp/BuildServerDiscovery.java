/*
 * SPDX-FileCopyrightText:  © 2023 David Gregory
 * SPDX-License-Identifier: Apache-2.0
 */
package org.mina_lang.langserver.bsp;

import ch.epfl.scala.bsp4j.BspConnectionDetails;
import org.eclipse.lsp4j.WorkspaceFolder;
import reactor.core.publisher.Flux;

public interface BuildServerDiscovery {
    Flux<BspConnectionDetails> discover(WorkspaceFolder folder);
}
