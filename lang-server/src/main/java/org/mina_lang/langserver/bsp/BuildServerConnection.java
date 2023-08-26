/*
 * SPDX-FileCopyrightText:  © 2023 David Gregory
 * SPDX-License-Identifier: Apache-2.0
 */
package org.mina_lang.langserver.bsp;

import java.io.InputStream;
import java.io.OutputStream;

public interface BuildServerConnection {
   InputStream input();
   OutputStream output();
}
