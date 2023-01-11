package org.mina_lang.renamer.scopes;

import org.mina_lang.common.names.DeclarationName;

public interface DeclarationNamingScope extends NamingScope {
    DeclarationName declarationName();
}
