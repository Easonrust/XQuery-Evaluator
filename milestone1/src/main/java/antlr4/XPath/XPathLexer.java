// Generated from XPath.g4 by ANTLR 4.7.2

package antlr4.XPath;

import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.atn.ATN;
import org.antlr.v4.runtime.atn.ATNDeserializer;
import org.antlr.v4.runtime.atn.LexerATNSimulator;
import org.antlr.v4.runtime.atn.PredictionContextCache;
import org.antlr.v4.runtime.dfa.DFA;

@SuppressWarnings({"all", "warnings", "unchecked", "unused", "cast"})
public class XPathLexer extends Lexer {
	static { RuntimeMetaData.checkVersion("4.7.2", RuntimeMetaData.VERSION); }

	protected static final DFA[] _decisionToDFA;
	protected static final PredictionContextCache _sharedContextCache =
		new PredictionContextCache();
	public static final int
		T__0=1, T__1=2, T__2=3, T__3=4, T__4=5, T__5=6, T__6=7, T__7=8, T__8=9, 
		T__9=10, T__10=11, T__11=12, T__12=13, T__13=14, T__14=15, T__15=16, STRING=17, 
		ESCAPE=18, DOC=19, LPR=20, RPR=21, NAME=22, TXT=23, WS=24;
	public static String[] channelNames = {
		"DEFAULT_TOKEN_CHANNEL", "HIDDEN"
	};

	public static String[] modeNames = {
		"DEFAULT_MODE"
	};

	private static String[] makeRuleNames() {
		return new String[] {
			"T__0", "T__1", "T__2", "T__3", "T__4", "T__5", "T__6", "T__7", "T__8", 
			"T__9", "T__10", "T__11", "T__12", "T__13", "T__14", "T__15", "STRING", 
			"ESCAPE", "DOC", "D", "O", "C", "LPR", "RPR", "NAME", "TXT", "WS"
		};
	}
	public static final String[] ruleNames = makeRuleNames();

	private static String[] makeLiteralNames() {
		return new String[] {
			null, "'/'", "'//'", "'*'", "'.'", "'..'", "'@'", "'['", "']'", "','", 
			"'='", "'eq'", "'=='", "'is'", "'and'", "'or'", "'not'", null, null, 
			null, "'('", "')'", null, "'text()'"
		};
	}
	private static final String[] _LITERAL_NAMES = makeLiteralNames();
	private static String[] makeSymbolicNames() {
		return new String[] {
			null, null, null, null, null, null, null, null, null, null, null, null, 
			null, null, null, null, null, "STRING", "ESCAPE", "DOC", "LPR", "RPR", 
			"NAME", "TXT", "WS"
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


	public XPathLexer(CharStream input) {
		super(input);
		_interp = new LexerATNSimulator(this,_ATN,_decisionToDFA,_sharedContextCache);
	}

	@Override
	public String getGrammarFileName() { return "XPath.g4"; }

	@Override
	public String[] getRuleNames() { return ruleNames; }

	@Override
	public String getSerializedATN() { return _serializedATN; }

	@Override
	public String[] getChannelNames() { return channelNames; }

	@Override
	public String[] getModeNames() { return modeNames; }

	@Override
	public ATN getATN() { return _ATN; }

	public static final String _serializedATN =
		"\3\u608b\ua72a\u8133\ub9ed\u417c\u3be7\u7786\u5964\2\32\u009b\b\1\4\2"+
		"\t\2\4\3\t\3\4\4\t\4\4\5\t\5\4\6\t\6\4\7\t\7\4\b\t\b\4\t\t\t\4\n\t\n\4"+
		"\13\t\13\4\f\t\f\4\r\t\r\4\16\t\16\4\17\t\17\4\20\t\20\4\21\t\21\4\22"+
		"\t\22\4\23\t\23\4\24\t\24\4\25\t\25\4\26\t\26\4\27\t\27\4\30\t\30\4\31"+
		"\t\31\4\32\t\32\4\33\t\33\4\34\t\34\3\2\3\2\3\3\3\3\3\3\3\4\3\4\3\5\3"+
		"\5\3\6\3\6\3\6\3\7\3\7\3\b\3\b\3\t\3\t\3\n\3\n\3\13\3\13\3\f\3\f\3\f\3"+
		"\r\3\r\3\r\3\16\3\16\3\16\3\17\3\17\3\17\3\17\3\20\3\20\3\20\3\21\3\21"+
		"\3\21\3\21\3\22\3\22\3\22\7\22g\n\22\f\22\16\22j\13\22\3\22\3\22\3\22"+
		"\3\22\7\22p\n\22\f\22\16\22s\13\22\3\22\5\22v\n\22\3\23\3\23\3\23\3\24"+
		"\3\24\3\24\3\24\3\25\3\25\3\26\3\26\3\27\3\27\3\30\3\30\3\31\3\31\3\32"+
		"\6\32\u008a\n\32\r\32\16\32\u008b\3\33\3\33\3\33\3\33\3\33\3\33\3\33\3"+
		"\34\6\34\u0096\n\34\r\34\16\34\u0097\3\34\3\34\2\2\35\3\3\5\4\7\5\t\6"+
		"\13\7\r\b\17\t\21\n\23\13\25\f\27\r\31\16\33\17\35\20\37\21!\22#\23%\24"+
		"\'\25)\2+\2-\2/\26\61\27\63\30\65\31\67\32\3\2\n\4\2$$^^\4\2))^^\5\2$"+
		"$))^^\4\2FFff\4\2QQqq\4\2EEee\7\2//\62;C\\aac|\5\2\13\f\17\17\"\"\2\u009e"+
		"\2\3\3\2\2\2\2\5\3\2\2\2\2\7\3\2\2\2\2\t\3\2\2\2\2\13\3\2\2\2\2\r\3\2"+
		"\2\2\2\17\3\2\2\2\2\21\3\2\2\2\2\23\3\2\2\2\2\25\3\2\2\2\2\27\3\2\2\2"+
		"\2\31\3\2\2\2\2\33\3\2\2\2\2\35\3\2\2\2\2\37\3\2\2\2\2!\3\2\2\2\2#\3\2"+
		"\2\2\2%\3\2\2\2\2\'\3\2\2\2\2/\3\2\2\2\2\61\3\2\2\2\2\63\3\2\2\2\2\65"+
		"\3\2\2\2\2\67\3\2\2\2\39\3\2\2\2\5;\3\2\2\2\7>\3\2\2\2\t@\3\2\2\2\13B"+
		"\3\2\2\2\rE\3\2\2\2\17G\3\2\2\2\21I\3\2\2\2\23K\3\2\2\2\25M\3\2\2\2\27"+
		"O\3\2\2\2\31R\3\2\2\2\33U\3\2\2\2\35X\3\2\2\2\37\\\3\2\2\2!_\3\2\2\2#"+
		"u\3\2\2\2%w\3\2\2\2\'z\3\2\2\2)~\3\2\2\2+\u0080\3\2\2\2-\u0082\3\2\2\2"+
		"/\u0084\3\2\2\2\61\u0086\3\2\2\2\63\u0089\3\2\2\2\65\u008d\3\2\2\2\67"+
		"\u0095\3\2\2\29:\7\61\2\2:\4\3\2\2\2;<\7\61\2\2<=\7\61\2\2=\6\3\2\2\2"+
		">?\7,\2\2?\b\3\2\2\2@A\7\60\2\2A\n\3\2\2\2BC\7\60\2\2CD\7\60\2\2D\f\3"+
		"\2\2\2EF\7B\2\2F\16\3\2\2\2GH\7]\2\2H\20\3\2\2\2IJ\7_\2\2J\22\3\2\2\2"+
		"KL\7.\2\2L\24\3\2\2\2MN\7?\2\2N\26\3\2\2\2OP\7g\2\2PQ\7s\2\2Q\30\3\2\2"+
		"\2RS\7?\2\2ST\7?\2\2T\32\3\2\2\2UV\7k\2\2VW\7u\2\2W\34\3\2\2\2XY\7c\2"+
		"\2YZ\7p\2\2Z[\7f\2\2[\36\3\2\2\2\\]\7q\2\2]^\7t\2\2^ \3\2\2\2_`\7p\2\2"+
		"`a\7q\2\2ab\7v\2\2b\"\3\2\2\2ch\7$\2\2dg\5%\23\2eg\n\2\2\2fd\3\2\2\2f"+
		"e\3\2\2\2gj\3\2\2\2hf\3\2\2\2hi\3\2\2\2ik\3\2\2\2jh\3\2\2\2kv\7$\2\2l"+
		"q\7)\2\2mp\5%\23\2np\n\3\2\2om\3\2\2\2on\3\2\2\2ps\3\2\2\2qo\3\2\2\2q"+
		"r\3\2\2\2rt\3\2\2\2sq\3\2\2\2tv\7)\2\2uc\3\2\2\2ul\3\2\2\2v$\3\2\2\2w"+
		"x\7^\2\2xy\t\4\2\2y&\3\2\2\2z{\5)\25\2{|\5+\26\2|}\5-\27\2}(\3\2\2\2~"+
		"\177\t\5\2\2\177*\3\2\2\2\u0080\u0081\t\6\2\2\u0081,\3\2\2\2\u0082\u0083"+
		"\t\7\2\2\u0083.\3\2\2\2\u0084\u0085\7*\2\2\u0085\60\3\2\2\2\u0086\u0087"+
		"\7+\2\2\u0087\62\3\2\2\2\u0088\u008a\t\b\2\2\u0089\u0088\3\2\2\2\u008a"+
		"\u008b\3\2\2\2\u008b\u0089\3\2\2\2\u008b\u008c\3\2\2\2\u008c\64\3\2\2"+
		"\2\u008d\u008e\7v\2\2\u008e\u008f\7g\2\2\u008f\u0090\7z\2\2\u0090\u0091"+
		"\7v\2\2\u0091\u0092\7*\2\2\u0092\u0093\7+\2\2\u0093\66\3\2\2\2\u0094\u0096"+
		"\t\t\2\2\u0095\u0094\3\2\2\2\u0096\u0097\3\2\2\2\u0097\u0095\3\2\2\2\u0097"+
		"\u0098\3\2\2\2\u0098\u0099\3\2\2\2\u0099\u009a\b\34\2\2\u009a8\3\2\2\2"+
		"\n\2fhoqu\u008b\u0097\3\b\2\2";
	public static final ATN _ATN =
		new ATNDeserializer().deserialize(_serializedATN.toCharArray());
	static {
		_decisionToDFA = new DFA[_ATN.getNumberOfDecisions()];
		for (int i = 0; i < _ATN.getNumberOfDecisions(); i++) {
			_decisionToDFA[i] = new DFA(_ATN.getDecisionState(i), i);
		}
	}
}