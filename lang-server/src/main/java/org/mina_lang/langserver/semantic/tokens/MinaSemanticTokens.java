package org.mina_lang.langserver.semantic.tokens;

import org.eclipse.collections.api.factory.Lists;
import org.eclipse.collections.api.list.ImmutableList;
import org.eclipse.collections.api.map.ImmutableMap;
import org.eclipse.collections.api.tuple.Pair;

import static org.eclipse.lsp4j.SemanticTokenModifiers.*;
import static org.eclipse.lsp4j.SemanticTokenTypes.*;

public class MinaSemanticTokens {
    public static final ImmutableList<String> tokenTypes;
    public static final ImmutableList<String> tokenModifiers;
    public static final ImmutableMap<String, Integer> typeIndices;
    public static final ImmutableMap<String, Integer> modifierIndices;

    static {
        tokenModifiers = Lists.immutable.of(Declaration, DefaultLibrary, Static, Readonly);
        tokenTypes = Lists.immutable.of(Namespace, Class, Parameter, Variable, Function,
                String, Keyword, Number, Operator, Enum, EnumMember);
        typeIndices = tokenTypes.zipWithIndex().toImmutableMap(Pair::getOne, Pair::getTwo);
        modifierIndices = tokenModifiers.zipWithIndex().toImmutableMap(Pair::getOne, Pair::getTwo);
    }
}
