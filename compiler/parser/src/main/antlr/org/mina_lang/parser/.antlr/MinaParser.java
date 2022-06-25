// Generated from /Users/davidgregory/Repos/mina/langserver/src/main/antlr/org/mina_lang/parser/Mina.g4 by ANTLR 4.9.2
import org.antlr.v4.runtime.atn.*;
import org.antlr.v4.runtime.dfa.DFA;
import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.misc.*;
import org.antlr.v4.runtime.tree.*;
import java.util.List;
import java.util.Iterator;
import java.util.ArrayList;

@SuppressWarnings({"all", "warnings", "unchecked", "unused", "cast"})
public class MinaParser extends Parser {
	static { RuntimeMetaData.checkVersion("4.9.2", RuntimeMetaData.VERSION); }

	protected static final DFA[] _decisionToDFA;
	protected static final PredictionContextCache _sharedContextCache =
		new PredictionContextCache();
	public static final int
		WHITESPACE=1, RSLASH=2, MODULE=3, IMPORT=4, DATA=5, LET=6, IF=7, THEN=8, 
		ELSE=9, MATCH=10, WITH=11, CASE=12, LBRACE=13, RBRACE=14, LPAREN=15, RPAREN=16, 
		EQ=17, DOT=18, COMMA=19, ARROW=20, AT=21, SEMICOLON=22, COLON=23, TRUE=24, 
		FALSE=25, SQUOTE=26, DQUOTE=27, LITERAL_CHAR=28, LITERAL_INT=29, ID=30;
	public static final int
		RULE_compilationUnit = 0, RULE_module = 1, RULE_importDeclaration = 2, 
		RULE_importSelector = 3, RULE_importSymbols = 4, RULE_declaration = 5, 
		RULE_dataDeclaration = 6, RULE_letDeclaration = 7, RULE_expr = 8, RULE_ifExpr = 9, 
		RULE_lambdaExpr = 10, RULE_lambdaParams = 11, RULE_matchExpr = 12, RULE_matchCase = 13, 
		RULE_pattern = 14, RULE_constructorPattern = 15, RULE_fieldPatterns = 16, 
		RULE_fieldPattern = 17, RULE_patternAlias = 18, RULE_applicableExpr = 19, 
		RULE_application = 20, RULE_parenExpr = 21, RULE_literal = 22, RULE_literalBoolean = 23, 
		RULE_literalInt = 24, RULE_literalChar = 25, RULE_packageId = 26, RULE_moduleId = 27, 
		RULE_qualifiedId = 28;
	private static String[] makeRuleNames() {
		return new String[] {
			"compilationUnit", "module", "importDeclaration", "importSelector", "importSymbols", 
			"declaration", "dataDeclaration", "letDeclaration", "expr", "ifExpr", 
			"lambdaExpr", "lambdaParams", "matchExpr", "matchCase", "pattern", "constructorPattern", 
			"fieldPatterns", "fieldPattern", "patternAlias", "applicableExpr", "application", 
			"parenExpr", "literal", "literalBoolean", "literalInt", "literalChar", 
			"packageId", "moduleId", "qualifiedId"
		};
	}
	public static final String[] ruleNames = makeRuleNames();

	private static String[] makeLiteralNames() {
		return new String[] {
			null, null, "'/'", "'module'", "'import'", "'data'", "'let'", "'if'", 
			"'then'", "'else'", "'match'", "'with'", "'case'", "'{'", "'}'", "'('", 
			"')'", "'='", "'.'", "','", "'->'", "'@'", "';'", "':'", "'true'", "'false'", 
			"'''", "'\"'"
		};
	}
	private static final String[] _LITERAL_NAMES = makeLiteralNames();
	private static String[] makeSymbolicNames() {
		return new String[] {
			null, "WHITESPACE", "RSLASH", "MODULE", "IMPORT", "DATA", "LET", "IF", 
			"THEN", "ELSE", "MATCH", "WITH", "CASE", "LBRACE", "RBRACE", "LPAREN", 
			"RPAREN", "EQ", "DOT", "COMMA", "ARROW", "AT", "SEMICOLON", "COLON", 
			"TRUE", "FALSE", "SQUOTE", "DQUOTE", "LITERAL_CHAR", "LITERAL_INT", "ID"
		};
	}
	private static final String[] _SYMBOLIC_NAMES = makeSymbolicNames();
	public static final Vocabulary VOCABULARY = new VocabularyImpl(_LITERAL_NAMES, _SYMBOLIC_NAMES);

	/**
	 * @deprecated Use {@link #VOCABULARY} instead.
	 */
	@Deprecated
	public static final String[] tokenNames;
	static {
		tokenNames = new String[_SYMBOLIC_NAMES.length];
		for (int i = 0; i < tokenNames.length; i++) {
			tokenNames[i] = VOCABULARY.getLiteralName(i);
			if (tokenNames[i] == null) {
				tokenNames[i] = VOCABULARY.getSymbolicName(i);
			}

			if (tokenNames[i] == null) {
				tokenNames[i] = "<INVALID>";
			}
		}
	}

	@Override
	@Deprecated
	public String[] getTokenNames() {
		return tokenNames;
	}

	@Override

	public Vocabulary getVocabulary() {
		return VOCABULARY;
	}

	@Override
	public String getGrammarFileName() { return "Mina.g4"; }

	@Override
	public String[] getRuleNames() { return ruleNames; }

	@Override
	public String getSerializedATN() { return _serializedATN; }

	@Override
	public ATN getATN() { return _ATN; }

	public MinaParser(TokenStream input) {
		super(input);
		_interp = new ParserATNSimulator(this,_ATN,_decisionToDFA,_sharedContextCache);
	}

	public static class CompilationUnitContext extends ParserRuleContext {
		public TerminalNode EOF() { return getToken(MinaParser.EOF, 0); }
		public List<ModuleContext> module() {
			return getRuleContexts(ModuleContext.class);
		}
		public ModuleContext module(int i) {
			return getRuleContext(ModuleContext.class,i);
		}
		public CompilationUnitContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_compilationUnit; }
	}

	public final CompilationUnitContext compilationUnit() throws RecognitionException {
		CompilationUnitContext _localctx = new CompilationUnitContext(_ctx, getState());
		enterRule(_localctx, 0, RULE_compilationUnit);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(61);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==MODULE) {
				{
				{
				setState(58);
				module();
				}
				}
				setState(63);
				_errHandler.sync(this);
				_la = _input.LA(1);
			}
			setState(64);
			match(EOF);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class ModuleContext extends ParserRuleContext {
		public TerminalNode MODULE() { return getToken(MinaParser.MODULE, 0); }
		public ModuleIdContext moduleId() {
			return getRuleContext(ModuleIdContext.class,0);
		}
		public TerminalNode LBRACE() { return getToken(MinaParser.LBRACE, 0); }
		public TerminalNode RBRACE() { return getToken(MinaParser.RBRACE, 0); }
		public List<ImportDeclarationContext> importDeclaration() {
			return getRuleContexts(ImportDeclarationContext.class);
		}
		public ImportDeclarationContext importDeclaration(int i) {
			return getRuleContext(ImportDeclarationContext.class,i);
		}
		public List<DeclarationContext> declaration() {
			return getRuleContexts(DeclarationContext.class);
		}
		public DeclarationContext declaration(int i) {
			return getRuleContext(DeclarationContext.class,i);
		}
		public ModuleContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_module; }
	}

	public final ModuleContext module() throws RecognitionException {
		ModuleContext _localctx = new ModuleContext(_ctx, getState());
		enterRule(_localctx, 2, RULE_module);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(66);
			match(MODULE);
			setState(67);
			moduleId();
			setState(68);
			match(LBRACE);
			setState(72);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==IMPORT) {
				{
				{
				setState(69);
				importDeclaration();
				}
				}
				setState(74);
				_errHandler.sync(this);
				_la = _input.LA(1);
			}
			setState(78);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==DATA || _la==LET) {
				{
				{
				setState(75);
				declaration();
				}
				}
				setState(80);
				_errHandler.sync(this);
				_la = _input.LA(1);
			}
			setState(81);
			match(RBRACE);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class ImportDeclarationContext extends ParserRuleContext {
		public TerminalNode IMPORT() { return getToken(MinaParser.IMPORT, 0); }
		public ImportSelectorContext importSelector() {
			return getRuleContext(ImportSelectorContext.class,0);
		}
		public ImportDeclarationContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_importDeclaration; }
	}

	public final ImportDeclarationContext importDeclaration() throws RecognitionException {
		ImportDeclarationContext _localctx = new ImportDeclarationContext(_ctx, getState());
		enterRule(_localctx, 4, RULE_importDeclaration);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(83);
			match(IMPORT);
			setState(84);
			importSelector();
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class ImportSelectorContext extends ParserRuleContext {
		public ModuleIdContext moduleId() {
			return getRuleContext(ModuleIdContext.class,0);
		}
		public TerminalNode DOT() { return getToken(MinaParser.DOT, 0); }
		public TerminalNode ID() { return getToken(MinaParser.ID, 0); }
		public TerminalNode LBRACE() { return getToken(MinaParser.LBRACE, 0); }
		public ImportSymbolsContext importSymbols() {
			return getRuleContext(ImportSymbolsContext.class,0);
		}
		public TerminalNode RBRACE() { return getToken(MinaParser.RBRACE, 0); }
		public ImportSelectorContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_importSelector; }
	}

	public final ImportSelectorContext importSelector() throws RecognitionException {
		ImportSelectorContext _localctx = new ImportSelectorContext(_ctx, getState());
		enterRule(_localctx, 6, RULE_importSelector);
		try {
			setState(97);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,3,_ctx) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				setState(86);
				moduleId();
				}
				break;
			case 2:
				enterOuterAlt(_localctx, 2);
				{
				setState(87);
				moduleId();
				setState(88);
				match(DOT);
				setState(89);
				match(ID);
				}
				break;
			case 3:
				enterOuterAlt(_localctx, 3);
				{
				setState(91);
				moduleId();
				setState(92);
				match(DOT);
				setState(93);
				match(LBRACE);
				setState(94);
				importSymbols();
				setState(95);
				match(RBRACE);
				}
				break;
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class ImportSymbolsContext extends ParserRuleContext {
		public List<TerminalNode> ID() { return getTokens(MinaParser.ID); }
		public TerminalNode ID(int i) {
			return getToken(MinaParser.ID, i);
		}
		public List<TerminalNode> COMMA() { return getTokens(MinaParser.COMMA); }
		public TerminalNode COMMA(int i) {
			return getToken(MinaParser.COMMA, i);
		}
		public ImportSymbolsContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_importSymbols; }
	}

	public final ImportSymbolsContext importSymbols() throws RecognitionException {
		ImportSymbolsContext _localctx = new ImportSymbolsContext(_ctx, getState());
		enterRule(_localctx, 8, RULE_importSymbols);
		try {
			int _alt;
			enterOuterAlt(_localctx, 1);
			{
			setState(103);
			_errHandler.sync(this);
			_alt = getInterpreter().adaptivePredict(_input,4,_ctx);
			while ( _alt!=2 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
				if ( _alt==1 ) {
					{
					{
					setState(99);
					match(ID);
					setState(100);
					match(COMMA);
					}
					} 
				}
				setState(105);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,4,_ctx);
			}
			setState(106);
			match(ID);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class DeclarationContext extends ParserRuleContext {
		public DataDeclarationContext dataDeclaration() {
			return getRuleContext(DataDeclarationContext.class,0);
		}
		public LetDeclarationContext letDeclaration() {
			return getRuleContext(LetDeclarationContext.class,0);
		}
		public DeclarationContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_declaration; }
	}

	public final DeclarationContext declaration() throws RecognitionException {
		DeclarationContext _localctx = new DeclarationContext(_ctx, getState());
		enterRule(_localctx, 10, RULE_declaration);
		try {
			setState(110);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case DATA:
				enterOuterAlt(_localctx, 1);
				{
				setState(108);
				dataDeclaration();
				}
				break;
			case LET:
				enterOuterAlt(_localctx, 2);
				{
				setState(109);
				letDeclaration();
				}
				break;
			default:
				throw new NoViableAltException(this);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class DataDeclarationContext extends ParserRuleContext {
		public TerminalNode DATA() { return getToken(MinaParser.DATA, 0); }
		public TerminalNode ID() { return getToken(MinaParser.ID, 0); }
		public TerminalNode LBRACE() { return getToken(MinaParser.LBRACE, 0); }
		public TerminalNode RBRACE() { return getToken(MinaParser.RBRACE, 0); }
		public DataDeclarationContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_dataDeclaration; }
	}

	public final DataDeclarationContext dataDeclaration() throws RecognitionException {
		DataDeclarationContext _localctx = new DataDeclarationContext(_ctx, getState());
		enterRule(_localctx, 12, RULE_dataDeclaration);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(112);
			match(DATA);
			setState(113);
			match(ID);
			setState(114);
			match(LBRACE);
			setState(115);
			match(RBRACE);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class LetDeclarationContext extends ParserRuleContext {
		public TerminalNode LET() { return getToken(MinaParser.LET, 0); }
		public TerminalNode ID() { return getToken(MinaParser.ID, 0); }
		public TerminalNode EQ() { return getToken(MinaParser.EQ, 0); }
		public ExprContext expr() {
			return getRuleContext(ExprContext.class,0);
		}
		public LetDeclarationContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_letDeclaration; }
	}

	public final LetDeclarationContext letDeclaration() throws RecognitionException {
		LetDeclarationContext _localctx = new LetDeclarationContext(_ctx, getState());
		enterRule(_localctx, 14, RULE_letDeclaration);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(117);
			match(LET);
			setState(118);
			match(ID);
			setState(119);
			match(EQ);
			setState(120);
			expr();
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class ExprContext extends ParserRuleContext {
		public IfExprContext ifExpr() {
			return getRuleContext(IfExprContext.class,0);
		}
		public LambdaExprContext lambdaExpr() {
			return getRuleContext(LambdaExprContext.class,0);
		}
		public MatchExprContext matchExpr() {
			return getRuleContext(MatchExprContext.class,0);
		}
		public LiteralContext literal() {
			return getRuleContext(LiteralContext.class,0);
		}
		public ApplicableExprContext applicableExpr() {
			return getRuleContext(ApplicableExprContext.class,0);
		}
		public ExprContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_expr; }
	}

	public final ExprContext expr() throws RecognitionException {
		ExprContext _localctx = new ExprContext(_ctx, getState());
		enterRule(_localctx, 16, RULE_expr);
		try {
			setState(127);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,6,_ctx) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				setState(122);
				ifExpr();
				}
				break;
			case 2:
				enterOuterAlt(_localctx, 2);
				{
				setState(123);
				lambdaExpr();
				}
				break;
			case 3:
				enterOuterAlt(_localctx, 3);
				{
				setState(124);
				matchExpr();
				}
				break;
			case 4:
				enterOuterAlt(_localctx, 4);
				{
				setState(125);
				literal();
				}
				break;
			case 5:
				enterOuterAlt(_localctx, 5);
				{
				setState(126);
				applicableExpr(0);
				}
				break;
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class IfExprContext extends ParserRuleContext {
		public TerminalNode IF() { return getToken(MinaParser.IF, 0); }
		public List<ExprContext> expr() {
			return getRuleContexts(ExprContext.class);
		}
		public ExprContext expr(int i) {
			return getRuleContext(ExprContext.class,i);
		}
		public TerminalNode THEN() { return getToken(MinaParser.THEN, 0); }
		public TerminalNode ELSE() { return getToken(MinaParser.ELSE, 0); }
		public IfExprContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_ifExpr; }
	}

	public final IfExprContext ifExpr() throws RecognitionException {
		IfExprContext _localctx = new IfExprContext(_ctx, getState());
		enterRule(_localctx, 18, RULE_ifExpr);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(129);
			match(IF);
			setState(130);
			expr();
			setState(131);
			match(THEN);
			setState(132);
			expr();
			setState(133);
			match(ELSE);
			setState(134);
			expr();
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class LambdaExprContext extends ParserRuleContext {
		public LambdaParamsContext lambdaParams() {
			return getRuleContext(LambdaParamsContext.class,0);
		}
		public TerminalNode ARROW() { return getToken(MinaParser.ARROW, 0); }
		public ExprContext expr() {
			return getRuleContext(ExprContext.class,0);
		}
		public LambdaExprContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_lambdaExpr; }
	}

	public final LambdaExprContext lambdaExpr() throws RecognitionException {
		LambdaExprContext _localctx = new LambdaExprContext(_ctx, getState());
		enterRule(_localctx, 20, RULE_lambdaExpr);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(136);
			lambdaParams();
			setState(137);
			match(ARROW);
			setState(138);
			expr();
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class LambdaParamsContext extends ParserRuleContext {
		public List<TerminalNode> ID() { return getTokens(MinaParser.ID); }
		public TerminalNode ID(int i) {
			return getToken(MinaParser.ID, i);
		}
		public TerminalNode LPAREN() { return getToken(MinaParser.LPAREN, 0); }
		public TerminalNode RPAREN() { return getToken(MinaParser.RPAREN, 0); }
		public List<TerminalNode> COMMA() { return getTokens(MinaParser.COMMA); }
		public TerminalNode COMMA(int i) {
			return getToken(MinaParser.COMMA, i);
		}
		public LambdaParamsContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_lambdaParams; }
	}

	public final LambdaParamsContext lambdaParams() throws RecognitionException {
		LambdaParamsContext _localctx = new LambdaParamsContext(_ctx, getState());
		enterRule(_localctx, 22, RULE_lambdaParams);
		try {
			int _alt;
			setState(153);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,8,_ctx) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				setState(140);
				match(ID);
				}
				break;
			case 2:
				enterOuterAlt(_localctx, 2);
				{
				setState(141);
				match(LPAREN);
				setState(142);
				match(RPAREN);
				}
				break;
			case 3:
				enterOuterAlt(_localctx, 3);
				{
				setState(143);
				match(LPAREN);
				setState(148);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,7,_ctx);
				while ( _alt!=2 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
					if ( _alt==1 ) {
						{
						{
						setState(144);
						match(ID);
						setState(145);
						match(COMMA);
						}
						} 
					}
					setState(150);
					_errHandler.sync(this);
					_alt = getInterpreter().adaptivePredict(_input,7,_ctx);
				}
				setState(151);
				match(ID);
				setState(152);
				match(RPAREN);
				}
				break;
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class MatchExprContext extends ParserRuleContext {
		public TerminalNode MATCH() { return getToken(MinaParser.MATCH, 0); }
		public ExprContext expr() {
			return getRuleContext(ExprContext.class,0);
		}
		public TerminalNode WITH() { return getToken(MinaParser.WITH, 0); }
		public TerminalNode LBRACE() { return getToken(MinaParser.LBRACE, 0); }
		public TerminalNode RBRACE() { return getToken(MinaParser.RBRACE, 0); }
		public List<MatchCaseContext> matchCase() {
			return getRuleContexts(MatchCaseContext.class);
		}
		public MatchCaseContext matchCase(int i) {
			return getRuleContext(MatchCaseContext.class,i);
		}
		public MatchExprContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_matchExpr; }
	}

	public final MatchExprContext matchExpr() throws RecognitionException {
		MatchExprContext _localctx = new MatchExprContext(_ctx, getState());
		enterRule(_localctx, 24, RULE_matchExpr);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(155);
			match(MATCH);
			setState(156);
			expr();
			setState(157);
			match(WITH);
			setState(158);
			match(LBRACE);
			setState(162);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==CASE) {
				{
				{
				setState(159);
				matchCase();
				}
				}
				setState(164);
				_errHandler.sync(this);
				_la = _input.LA(1);
			}
			setState(165);
			match(RBRACE);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class MatchCaseContext extends ParserRuleContext {
		public TerminalNode CASE() { return getToken(MinaParser.CASE, 0); }
		public PatternContext pattern() {
			return getRuleContext(PatternContext.class,0);
		}
		public TerminalNode ARROW() { return getToken(MinaParser.ARROW, 0); }
		public ExprContext expr() {
			return getRuleContext(ExprContext.class,0);
		}
		public MatchCaseContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_matchCase; }
	}

	public final MatchCaseContext matchCase() throws RecognitionException {
		MatchCaseContext _localctx = new MatchCaseContext(_ctx, getState());
		enterRule(_localctx, 26, RULE_matchCase);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(167);
			match(CASE);
			setState(168);
			pattern();
			setState(169);
			match(ARROW);
			setState(170);
			expr();
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class PatternContext extends ParserRuleContext {
		public TerminalNode ID() { return getToken(MinaParser.ID, 0); }
		public LiteralContext literal() {
			return getRuleContext(LiteralContext.class,0);
		}
		public ConstructorPatternContext constructorPattern() {
			return getRuleContext(ConstructorPatternContext.class,0);
		}
		public PatternContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_pattern; }
	}

	public final PatternContext pattern() throws RecognitionException {
		PatternContext _localctx = new PatternContext(_ctx, getState());
		enterRule(_localctx, 28, RULE_pattern);
		try {
			setState(175);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,10,_ctx) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				setState(172);
				match(ID);
				}
				break;
			case 2:
				enterOuterAlt(_localctx, 2);
				{
				setState(173);
				literal();
				}
				break;
			case 3:
				enterOuterAlt(_localctx, 3);
				{
				setState(174);
				constructorPattern();
				}
				break;
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class ConstructorPatternContext extends ParserRuleContext {
		public QualifiedIdContext qualifiedId() {
			return getRuleContext(QualifiedIdContext.class,0);
		}
		public TerminalNode LBRACE() { return getToken(MinaParser.LBRACE, 0); }
		public TerminalNode RBRACE() { return getToken(MinaParser.RBRACE, 0); }
		public PatternAliasContext patternAlias() {
			return getRuleContext(PatternAliasContext.class,0);
		}
		public FieldPatternsContext fieldPatterns() {
			return getRuleContext(FieldPatternsContext.class,0);
		}
		public ConstructorPatternContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_constructorPattern; }
	}

	public final ConstructorPatternContext constructorPattern() throws RecognitionException {
		ConstructorPatternContext _localctx = new ConstructorPatternContext(_ctx, getState());
		enterRule(_localctx, 30, RULE_constructorPattern);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(178);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,11,_ctx) ) {
			case 1:
				{
				setState(177);
				patternAlias();
				}
				break;
			}
			setState(180);
			qualifiedId();
			setState(181);
			match(LBRACE);
			setState(183);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==ID) {
				{
				setState(182);
				fieldPatterns();
				}
			}

			setState(185);
			match(RBRACE);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class FieldPatternsContext extends ParserRuleContext {
		public List<FieldPatternContext> fieldPattern() {
			return getRuleContexts(FieldPatternContext.class);
		}
		public FieldPatternContext fieldPattern(int i) {
			return getRuleContext(FieldPatternContext.class,i);
		}
		public List<TerminalNode> COMMA() { return getTokens(MinaParser.COMMA); }
		public TerminalNode COMMA(int i) {
			return getToken(MinaParser.COMMA, i);
		}
		public FieldPatternsContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_fieldPatterns; }
	}

	public final FieldPatternsContext fieldPatterns() throws RecognitionException {
		FieldPatternsContext _localctx = new FieldPatternsContext(_ctx, getState());
		enterRule(_localctx, 32, RULE_fieldPatterns);
		try {
			int _alt;
			enterOuterAlt(_localctx, 1);
			{
			setState(192);
			_errHandler.sync(this);
			_alt = getInterpreter().adaptivePredict(_input,13,_ctx);
			while ( _alt!=2 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
				if ( _alt==1 ) {
					{
					{
					setState(187);
					fieldPattern();
					setState(188);
					match(COMMA);
					}
					} 
				}
				setState(194);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,13,_ctx);
			}
			setState(195);
			fieldPattern();
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class FieldPatternContext extends ParserRuleContext {
		public TerminalNode ID() { return getToken(MinaParser.ID, 0); }
		public TerminalNode COLON() { return getToken(MinaParser.COLON, 0); }
		public PatternContext pattern() {
			return getRuleContext(PatternContext.class,0);
		}
		public FieldPatternContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_fieldPattern; }
	}

	public final FieldPatternContext fieldPattern() throws RecognitionException {
		FieldPatternContext _localctx = new FieldPatternContext(_ctx, getState());
		enterRule(_localctx, 34, RULE_fieldPattern);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(197);
			match(ID);
			setState(200);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==COLON) {
				{
				setState(198);
				match(COLON);
				setState(199);
				pattern();
				}
			}

			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class PatternAliasContext extends ParserRuleContext {
		public TerminalNode ID() { return getToken(MinaParser.ID, 0); }
		public TerminalNode AT() { return getToken(MinaParser.AT, 0); }
		public PatternAliasContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_patternAlias; }
	}

	public final PatternAliasContext patternAlias() throws RecognitionException {
		PatternAliasContext _localctx = new PatternAliasContext(_ctx, getState());
		enterRule(_localctx, 36, RULE_patternAlias);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(202);
			match(ID);
			setState(203);
			match(AT);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class ApplicableExprContext extends ParserRuleContext {
		public ParenExprContext parenExpr() {
			return getRuleContext(ParenExprContext.class,0);
		}
		public QualifiedIdContext qualifiedId() {
			return getRuleContext(QualifiedIdContext.class,0);
		}
		public ApplicableExprContext applicableExpr() {
			return getRuleContext(ApplicableExprContext.class,0);
		}
		public ApplicationContext application() {
			return getRuleContext(ApplicationContext.class,0);
		}
		public ApplicableExprContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_applicableExpr; }
	}

	public final ApplicableExprContext applicableExpr() throws RecognitionException {
		return applicableExpr(0);
	}

	private ApplicableExprContext applicableExpr(int _p) throws RecognitionException {
		ParserRuleContext _parentctx = _ctx;
		int _parentState = getState();
		ApplicableExprContext _localctx = new ApplicableExprContext(_ctx, _parentState);
		ApplicableExprContext _prevctx = _localctx;
		int _startState = 38;
		enterRecursionRule(_localctx, 38, RULE_applicableExpr, _p);
		try {
			int _alt;
			enterOuterAlt(_localctx, 1);
			{
			setState(208);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case LPAREN:
				{
				setState(206);
				parenExpr();
				}
				break;
			case ID:
				{
				setState(207);
				qualifiedId();
				}
				break;
			default:
				throw new NoViableAltException(this);
			}
			_ctx.stop = _input.LT(-1);
			setState(214);
			_errHandler.sync(this);
			_alt = getInterpreter().adaptivePredict(_input,16,_ctx);
			while ( _alt!=2 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
				if ( _alt==1 ) {
					if ( _parseListeners!=null ) triggerExitRuleEvent();
					_prevctx = _localctx;
					{
					{
					_localctx = new ApplicableExprContext(_parentctx, _parentState);
					pushNewRecursionContext(_localctx, _startState, RULE_applicableExpr);
					setState(210);
					if (!(precpred(_ctx, 1))) throw new FailedPredicateException(this, "precpred(_ctx, 1)");
					setState(211);
					application();
					}
					} 
				}
				setState(216);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,16,_ctx);
			}
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			unrollRecursionContexts(_parentctx);
		}
		return _localctx;
	}

	public static class ApplicationContext extends ParserRuleContext {
		public TerminalNode LPAREN() { return getToken(MinaParser.LPAREN, 0); }
		public TerminalNode RPAREN() { return getToken(MinaParser.RPAREN, 0); }
		public List<ExprContext> expr() {
			return getRuleContexts(ExprContext.class);
		}
		public ExprContext expr(int i) {
			return getRuleContext(ExprContext.class,i);
		}
		public List<TerminalNode> COMMA() { return getTokens(MinaParser.COMMA); }
		public TerminalNode COMMA(int i) {
			return getToken(MinaParser.COMMA, i);
		}
		public ApplicationContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_application; }
	}

	public final ApplicationContext application() throws RecognitionException {
		ApplicationContext _localctx = new ApplicationContext(_ctx, getState());
		enterRule(_localctx, 40, RULE_application);
		try {
			int _alt;
			setState(231);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,18,_ctx) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				setState(217);
				match(LPAREN);
				setState(218);
				match(RPAREN);
				}
				break;
			case 2:
				enterOuterAlt(_localctx, 2);
				{
				setState(219);
				match(LPAREN);
				setState(225);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,17,_ctx);
				while ( _alt!=2 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
					if ( _alt==1 ) {
						{
						{
						setState(220);
						expr();
						setState(221);
						match(COMMA);
						}
						} 
					}
					setState(227);
					_errHandler.sync(this);
					_alt = getInterpreter().adaptivePredict(_input,17,_ctx);
				}
				setState(228);
				expr();
				setState(229);
				match(RPAREN);
				}
				break;
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class ParenExprContext extends ParserRuleContext {
		public TerminalNode LPAREN() { return getToken(MinaParser.LPAREN, 0); }
		public ExprContext expr() {
			return getRuleContext(ExprContext.class,0);
		}
		public TerminalNode RPAREN() { return getToken(MinaParser.RPAREN, 0); }
		public ParenExprContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_parenExpr; }
	}

	public final ParenExprContext parenExpr() throws RecognitionException {
		ParenExprContext _localctx = new ParenExprContext(_ctx, getState());
		enterRule(_localctx, 42, RULE_parenExpr);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(233);
			match(LPAREN);
			setState(234);
			expr();
			setState(235);
			match(RPAREN);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class LiteralContext extends ParserRuleContext {
		public LiteralBooleanContext literalBoolean() {
			return getRuleContext(LiteralBooleanContext.class,0);
		}
		public LiteralIntContext literalInt() {
			return getRuleContext(LiteralIntContext.class,0);
		}
		public LiteralCharContext literalChar() {
			return getRuleContext(LiteralCharContext.class,0);
		}
		public LiteralContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_literal; }
	}

	public final LiteralContext literal() throws RecognitionException {
		LiteralContext _localctx = new LiteralContext(_ctx, getState());
		enterRule(_localctx, 44, RULE_literal);
		try {
			setState(240);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case TRUE:
			case FALSE:
				enterOuterAlt(_localctx, 1);
				{
				setState(237);
				literalBoolean();
				}
				break;
			case LITERAL_INT:
				enterOuterAlt(_localctx, 2);
				{
				setState(238);
				literalInt();
				}
				break;
			case LITERAL_CHAR:
				enterOuterAlt(_localctx, 3);
				{
				setState(239);
				literalChar();
				}
				break;
			default:
				throw new NoViableAltException(this);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class LiteralBooleanContext extends ParserRuleContext {
		public TerminalNode TRUE() { return getToken(MinaParser.TRUE, 0); }
		public TerminalNode FALSE() { return getToken(MinaParser.FALSE, 0); }
		public LiteralBooleanContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_literalBoolean; }
	}

	public final LiteralBooleanContext literalBoolean() throws RecognitionException {
		LiteralBooleanContext _localctx = new LiteralBooleanContext(_ctx, getState());
		enterRule(_localctx, 46, RULE_literalBoolean);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(242);
			_la = _input.LA(1);
			if ( !(_la==TRUE || _la==FALSE) ) {
			_errHandler.recoverInline(this);
			}
			else {
				if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
				_errHandler.reportMatch(this);
				consume();
			}
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class LiteralIntContext extends ParserRuleContext {
		public TerminalNode LITERAL_INT() { return getToken(MinaParser.LITERAL_INT, 0); }
		public LiteralIntContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_literalInt; }
	}

	public final LiteralIntContext literalInt() throws RecognitionException {
		LiteralIntContext _localctx = new LiteralIntContext(_ctx, getState());
		enterRule(_localctx, 48, RULE_literalInt);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(244);
			match(LITERAL_INT);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class LiteralCharContext extends ParserRuleContext {
		public TerminalNode LITERAL_CHAR() { return getToken(MinaParser.LITERAL_CHAR, 0); }
		public LiteralCharContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_literalChar; }
	}

	public final LiteralCharContext literalChar() throws RecognitionException {
		LiteralCharContext _localctx = new LiteralCharContext(_ctx, getState());
		enterRule(_localctx, 50, RULE_literalChar);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(246);
			match(LITERAL_CHAR);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class PackageIdContext extends ParserRuleContext {
		public List<TerminalNode> ID() { return getTokens(MinaParser.ID); }
		public TerminalNode ID(int i) {
			return getToken(MinaParser.ID, i);
		}
		public List<TerminalNode> RSLASH() { return getTokens(MinaParser.RSLASH); }
		public TerminalNode RSLASH(int i) {
			return getToken(MinaParser.RSLASH, i);
		}
		public PackageIdContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_packageId; }
	}

	public final PackageIdContext packageId() throws RecognitionException {
		PackageIdContext _localctx = new PackageIdContext(_ctx, getState());
		enterRule(_localctx, 52, RULE_packageId);
		try {
			int _alt;
			enterOuterAlt(_localctx, 1);
			{
			setState(252);
			_errHandler.sync(this);
			_alt = getInterpreter().adaptivePredict(_input,20,_ctx);
			while ( _alt!=2 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
				if ( _alt==1 ) {
					{
					{
					setState(248);
					match(ID);
					setState(249);
					match(RSLASH);
					}
					} 
				}
				setState(254);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,20,_ctx);
			}
			setState(255);
			match(ID);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class ModuleIdContext extends ParserRuleContext {
		public TerminalNode ID() { return getToken(MinaParser.ID, 0); }
		public PackageIdContext packageId() {
			return getRuleContext(PackageIdContext.class,0);
		}
		public TerminalNode RSLASH() { return getToken(MinaParser.RSLASH, 0); }
		public ModuleIdContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_moduleId; }
	}

	public final ModuleIdContext moduleId() throws RecognitionException {
		ModuleIdContext _localctx = new ModuleIdContext(_ctx, getState());
		enterRule(_localctx, 54, RULE_moduleId);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(260);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,21,_ctx) ) {
			case 1:
				{
				setState(257);
				packageId();
				setState(258);
				match(RSLASH);
				}
				break;
			}
			setState(262);
			match(ID);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class QualifiedIdContext extends ParserRuleContext {
		public TerminalNode ID() { return getToken(MinaParser.ID, 0); }
		public ModuleIdContext moduleId() {
			return getRuleContext(ModuleIdContext.class,0);
		}
		public TerminalNode DOT() { return getToken(MinaParser.DOT, 0); }
		public QualifiedIdContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_qualifiedId; }
	}

	public final QualifiedIdContext qualifiedId() throws RecognitionException {
		QualifiedIdContext _localctx = new QualifiedIdContext(_ctx, getState());
		enterRule(_localctx, 56, RULE_qualifiedId);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(267);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,22,_ctx) ) {
			case 1:
				{
				setState(264);
				moduleId();
				setState(265);
				match(DOT);
				}
				break;
			}
			setState(269);
			match(ID);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public boolean sempred(RuleContext _localctx, int ruleIndex, int predIndex) {
		switch (ruleIndex) {
		case 19:
			return applicableExpr_sempred((ApplicableExprContext)_localctx, predIndex);
		}
		return true;
	}
	private boolean applicableExpr_sempred(ApplicableExprContext _localctx, int predIndex) {
		switch (predIndex) {
		case 0:
			return precpred(_ctx, 1);
		}
		return true;
	}

	public static final String _serializedATN =
		"\3\u608b\ua72a\u8133\ub9ed\u417c\u3be7\u7786\u5964\3 \u0112\4\2\t\2\4"+
		"\3\t\3\4\4\t\4\4\5\t\5\4\6\t\6\4\7\t\7\4\b\t\b\4\t\t\t\4\n\t\n\4\13\t"+
		"\13\4\f\t\f\4\r\t\r\4\16\t\16\4\17\t\17\4\20\t\20\4\21\t\21\4\22\t\22"+
		"\4\23\t\23\4\24\t\24\4\25\t\25\4\26\t\26\4\27\t\27\4\30\t\30\4\31\t\31"+
		"\4\32\t\32\4\33\t\33\4\34\t\34\4\35\t\35\4\36\t\36\3\2\7\2>\n\2\f\2\16"+
		"\2A\13\2\3\2\3\2\3\3\3\3\3\3\3\3\7\3I\n\3\f\3\16\3L\13\3\3\3\7\3O\n\3"+
		"\f\3\16\3R\13\3\3\3\3\3\3\4\3\4\3\4\3\5\3\5\3\5\3\5\3\5\3\5\3\5\3\5\3"+
		"\5\3\5\3\5\5\5d\n\5\3\6\3\6\7\6h\n\6\f\6\16\6k\13\6\3\6\3\6\3\7\3\7\5"+
		"\7q\n\7\3\b\3\b\3\b\3\b\3\b\3\t\3\t\3\t\3\t\3\t\3\n\3\n\3\n\3\n\3\n\5"+
		"\n\u0082\n\n\3\13\3\13\3\13\3\13\3\13\3\13\3\13\3\f\3\f\3\f\3\f\3\r\3"+
		"\r\3\r\3\r\3\r\3\r\7\r\u0095\n\r\f\r\16\r\u0098\13\r\3\r\3\r\5\r\u009c"+
		"\n\r\3\16\3\16\3\16\3\16\3\16\7\16\u00a3\n\16\f\16\16\16\u00a6\13\16\3"+
		"\16\3\16\3\17\3\17\3\17\3\17\3\17\3\20\3\20\3\20\5\20\u00b2\n\20\3\21"+
		"\5\21\u00b5\n\21\3\21\3\21\3\21\5\21\u00ba\n\21\3\21\3\21\3\22\3\22\3"+
		"\22\7\22\u00c1\n\22\f\22\16\22\u00c4\13\22\3\22\3\22\3\23\3\23\3\23\5"+
		"\23\u00cb\n\23\3\24\3\24\3\24\3\25\3\25\3\25\5\25\u00d3\n\25\3\25\3\25"+
		"\7\25\u00d7\n\25\f\25\16\25\u00da\13\25\3\26\3\26\3\26\3\26\3\26\3\26"+
		"\7\26\u00e2\n\26\f\26\16\26\u00e5\13\26\3\26\3\26\3\26\5\26\u00ea\n\26"+
		"\3\27\3\27\3\27\3\27\3\30\3\30\3\30\5\30\u00f3\n\30\3\31\3\31\3\32\3\32"+
		"\3\33\3\33\3\34\3\34\7\34\u00fd\n\34\f\34\16\34\u0100\13\34\3\34\3\34"+
		"\3\35\3\35\3\35\5\35\u0107\n\35\3\35\3\35\3\36\3\36\3\36\5\36\u010e\n"+
		"\36\3\36\3\36\3\36\2\3(\37\2\4\6\b\n\f\16\20\22\24\26\30\32\34\36 \"$"+
		"&(*,.\60\62\64\668:\2\3\3\2\32\33\2\u0112\2?\3\2\2\2\4D\3\2\2\2\6U\3\2"+
		"\2\2\bc\3\2\2\2\ni\3\2\2\2\fp\3\2\2\2\16r\3\2\2\2\20w\3\2\2\2\22\u0081"+
		"\3\2\2\2\24\u0083\3\2\2\2\26\u008a\3\2\2\2\30\u009b\3\2\2\2\32\u009d\3"+
		"\2\2\2\34\u00a9\3\2\2\2\36\u00b1\3\2\2\2 \u00b4\3\2\2\2\"\u00c2\3\2\2"+
		"\2$\u00c7\3\2\2\2&\u00cc\3\2\2\2(\u00d2\3\2\2\2*\u00e9\3\2\2\2,\u00eb"+
		"\3\2\2\2.\u00f2\3\2\2\2\60\u00f4\3\2\2\2\62\u00f6\3\2\2\2\64\u00f8\3\2"+
		"\2\2\66\u00fe\3\2\2\28\u0106\3\2\2\2:\u010d\3\2\2\2<>\5\4\3\2=<\3\2\2"+
		"\2>A\3\2\2\2?=\3\2\2\2?@\3\2\2\2@B\3\2\2\2A?\3\2\2\2BC\7\2\2\3C\3\3\2"+
		"\2\2DE\7\5\2\2EF\58\35\2FJ\7\17\2\2GI\5\6\4\2HG\3\2\2\2IL\3\2\2\2JH\3"+
		"\2\2\2JK\3\2\2\2KP\3\2\2\2LJ\3\2\2\2MO\5\f\7\2NM\3\2\2\2OR\3\2\2\2PN\3"+
		"\2\2\2PQ\3\2\2\2QS\3\2\2\2RP\3\2\2\2ST\7\20\2\2T\5\3\2\2\2UV\7\6\2\2V"+
		"W\5\b\5\2W\7\3\2\2\2Xd\58\35\2YZ\58\35\2Z[\7\24\2\2[\\\7 \2\2\\d\3\2\2"+
		"\2]^\58\35\2^_\7\24\2\2_`\7\17\2\2`a\5\n\6\2ab\7\20\2\2bd\3\2\2\2cX\3"+
		"\2\2\2cY\3\2\2\2c]\3\2\2\2d\t\3\2\2\2ef\7 \2\2fh\7\25\2\2ge\3\2\2\2hk"+
		"\3\2\2\2ig\3\2\2\2ij\3\2\2\2jl\3\2\2\2ki\3\2\2\2lm\7 \2\2m\13\3\2\2\2"+
		"nq\5\16\b\2oq\5\20\t\2pn\3\2\2\2po\3\2\2\2q\r\3\2\2\2rs\7\7\2\2st\7 \2"+
		"\2tu\7\17\2\2uv\7\20\2\2v\17\3\2\2\2wx\7\b\2\2xy\7 \2\2yz\7\23\2\2z{\5"+
		"\22\n\2{\21\3\2\2\2|\u0082\5\24\13\2}\u0082\5\26\f\2~\u0082\5\32\16\2"+
		"\177\u0082\5.\30\2\u0080\u0082\5(\25\2\u0081|\3\2\2\2\u0081}\3\2\2\2\u0081"+
		"~\3\2\2\2\u0081\177\3\2\2\2\u0081\u0080\3\2\2\2\u0082\23\3\2\2\2\u0083"+
		"\u0084\7\t\2\2\u0084\u0085\5\22\n\2\u0085\u0086\7\n\2\2\u0086\u0087\5"+
		"\22\n\2\u0087\u0088\7\13\2\2\u0088\u0089\5\22\n\2\u0089\25\3\2\2\2\u008a"+
		"\u008b\5\30\r\2\u008b\u008c\7\26\2\2\u008c\u008d\5\22\n\2\u008d\27\3\2"+
		"\2\2\u008e\u009c\7 \2\2\u008f\u0090\7\21\2\2\u0090\u009c\7\22\2\2\u0091"+
		"\u0096\7\21\2\2\u0092\u0093\7 \2\2\u0093\u0095\7\25\2\2\u0094\u0092\3"+
		"\2\2\2\u0095\u0098\3\2\2\2\u0096\u0094\3\2\2\2\u0096\u0097\3\2\2\2\u0097"+
		"\u0099\3\2\2\2\u0098\u0096\3\2\2\2\u0099\u009a\7 \2\2\u009a\u009c\7\22"+
		"\2\2\u009b\u008e\3\2\2\2\u009b\u008f\3\2\2\2\u009b\u0091\3\2\2\2\u009c"+
		"\31\3\2\2\2\u009d\u009e\7\f\2\2\u009e\u009f\5\22\n\2\u009f\u00a0\7\r\2"+
		"\2\u00a0\u00a4\7\17\2\2\u00a1\u00a3\5\34\17\2\u00a2\u00a1\3\2\2\2\u00a3"+
		"\u00a6\3\2\2\2\u00a4\u00a2\3\2\2\2\u00a4\u00a5\3\2\2\2\u00a5\u00a7\3\2"+
		"\2\2\u00a6\u00a4\3\2\2\2\u00a7\u00a8\7\20\2\2\u00a8\33\3\2\2\2\u00a9\u00aa"+
		"\7\16\2\2\u00aa\u00ab\5\36\20\2\u00ab\u00ac\7\26\2\2\u00ac\u00ad\5\22"+
		"\n\2\u00ad\35\3\2\2\2\u00ae\u00b2\7 \2\2\u00af\u00b2\5.\30\2\u00b0\u00b2"+
		"\5 \21\2\u00b1\u00ae\3\2\2\2\u00b1\u00af\3\2\2\2\u00b1\u00b0\3\2\2\2\u00b2"+
		"\37\3\2\2\2\u00b3\u00b5\5&\24\2\u00b4\u00b3\3\2\2\2\u00b4\u00b5\3\2\2"+
		"\2\u00b5\u00b6\3\2\2\2\u00b6\u00b7\5:\36\2\u00b7\u00b9\7\17\2\2\u00b8"+
		"\u00ba\5\"\22\2\u00b9\u00b8\3\2\2\2\u00b9\u00ba\3\2\2\2\u00ba\u00bb\3"+
		"\2\2\2\u00bb\u00bc\7\20\2\2\u00bc!\3\2\2\2\u00bd\u00be\5$\23\2\u00be\u00bf"+
		"\7\25\2\2\u00bf\u00c1\3\2\2\2\u00c0\u00bd\3\2\2\2\u00c1\u00c4\3\2\2\2"+
		"\u00c2\u00c0\3\2\2\2\u00c2\u00c3\3\2\2\2\u00c3\u00c5\3\2\2\2\u00c4\u00c2"+
		"\3\2\2\2\u00c5\u00c6\5$\23\2\u00c6#\3\2\2\2\u00c7\u00ca\7 \2\2\u00c8\u00c9"+
		"\7\31\2\2\u00c9\u00cb\5\36\20\2\u00ca\u00c8\3\2\2\2\u00ca\u00cb\3\2\2"+
		"\2\u00cb%\3\2\2\2\u00cc\u00cd\7 \2\2\u00cd\u00ce\7\27\2\2\u00ce\'\3\2"+
		"\2\2\u00cf\u00d0\b\25\1\2\u00d0\u00d3\5,\27\2\u00d1\u00d3\5:\36\2\u00d2"+
		"\u00cf\3\2\2\2\u00d2\u00d1\3\2\2\2\u00d3\u00d8\3\2\2\2\u00d4\u00d5\f\3"+
		"\2\2\u00d5\u00d7\5*\26\2\u00d6\u00d4\3\2\2\2\u00d7\u00da\3\2\2\2\u00d8"+
		"\u00d6\3\2\2\2\u00d8\u00d9\3\2\2\2\u00d9)\3\2\2\2\u00da\u00d8\3\2\2\2"+
		"\u00db\u00dc\7\21\2\2\u00dc\u00ea\7\22\2\2\u00dd\u00e3\7\21\2\2\u00de"+
		"\u00df\5\22\n\2\u00df\u00e0\7\25\2\2\u00e0\u00e2\3\2\2\2\u00e1\u00de\3"+
		"\2\2\2\u00e2\u00e5\3\2\2\2\u00e3\u00e1\3\2\2\2\u00e3\u00e4\3\2\2\2\u00e4"+
		"\u00e6\3\2\2\2\u00e5\u00e3\3\2\2\2\u00e6\u00e7\5\22\n\2\u00e7\u00e8\7"+
		"\22\2\2\u00e8\u00ea\3\2\2\2\u00e9\u00db\3\2\2\2\u00e9\u00dd\3\2\2\2\u00ea"+
		"+\3\2\2\2\u00eb\u00ec\7\21\2\2\u00ec\u00ed\5\22\n\2\u00ed\u00ee\7\22\2"+
		"\2\u00ee-\3\2\2\2\u00ef\u00f3\5\60\31\2\u00f0\u00f3\5\62\32\2\u00f1\u00f3"+
		"\5\64\33\2\u00f2\u00ef\3\2\2\2\u00f2\u00f0\3\2\2\2\u00f2\u00f1\3\2\2\2"+
		"\u00f3/\3\2\2\2\u00f4\u00f5\t\2\2\2\u00f5\61\3\2\2\2\u00f6\u00f7\7\37"+
		"\2\2\u00f7\63\3\2\2\2\u00f8\u00f9\7\36\2\2\u00f9\65\3\2\2\2\u00fa\u00fb"+
		"\7 \2\2\u00fb\u00fd\7\4\2\2\u00fc\u00fa\3\2\2\2\u00fd\u0100\3\2\2\2\u00fe"+
		"\u00fc\3\2\2\2\u00fe\u00ff\3\2\2\2\u00ff\u0101\3\2\2\2\u0100\u00fe\3\2"+
		"\2\2\u0101\u0102\7 \2\2\u0102\67\3\2\2\2\u0103\u0104\5\66\34\2\u0104\u0105"+
		"\7\4\2\2\u0105\u0107\3\2\2\2\u0106\u0103\3\2\2\2\u0106\u0107\3\2\2\2\u0107"+
		"\u0108\3\2\2\2\u0108\u0109\7 \2\2\u01099\3\2\2\2\u010a\u010b\58\35\2\u010b"+
		"\u010c\7\24\2\2\u010c\u010e\3\2\2\2\u010d\u010a\3\2\2\2\u010d\u010e\3"+
		"\2\2\2\u010e\u010f\3\2\2\2\u010f\u0110\7 \2\2\u0110;\3\2\2\2\31?JPcip"+
		"\u0081\u0096\u009b\u00a4\u00b1\u00b4\u00b9\u00c2\u00ca\u00d2\u00d8\u00e3"+
		"\u00e9\u00f2\u00fe\u0106\u010d";
	public static final ATN _ATN =
		new ATNDeserializer().deserialize(_serializedATN.toCharArray());
	static {
		_decisionToDFA = new DFA[_ATN.getNumberOfDecisions()];
		for (int i = 0; i < _ATN.getNumberOfDecisions(); i++) {
			_decisionToDFA[i] = new DFA(_ATN.getDecisionState(i), i);
		}
	}
}