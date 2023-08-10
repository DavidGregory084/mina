/*
 * SPDX-FileCopyrightText:  © 2023 David Gregory
 * SPDX-License-Identifier: Apache-2.0
 */
package org.mina_lang.langserver.bsp;

import ch.epfl.scala.bsp4j.BspConnectionDetails;
import org.eclipse.lsp4j.WorkspaceFolder;

import java.io.IOException;

public interface BuildServerLauncher {
    BuildServerConnection launch(WorkspaceFolder workspaceFolder, BspConnectionDetails details) throws IOException;
}
