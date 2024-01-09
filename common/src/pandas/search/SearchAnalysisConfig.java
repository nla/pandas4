package pandas.search;

import org.apache.lucene.analysis.*;
import org.apache.lucene.analysis.core.LowerCaseFilterFactory;
import org.apache.lucene.analysis.miscellaneous.ASCIIFoldingFilterFactory;
import org.apache.lucene.analysis.snowball.SnowballPorterFilterFactory;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.analysis.standard.StandardTokenizerFactory;
import org.apache.lucene.analysis.util.CharTokenizer;
import org.apache.lucene.util.AttributeFactory;
import org.hibernate.search.backend.lucene.analysis.LuceneAnalysisConfigurationContext;
import org.hibernate.search.backend.lucene.analysis.LuceneAnalysisConfigurer;

import java.util.Map;
import java.util.Set;

public class SearchAnalysisConfig implements LuceneAnalysisConfigurer {
    @Override
    public void configure(LuceneAnalysisConfigurationContext context) {
        context.analyzer("standard").instance(new StandardAnalyzer());
        context.analyzer("url").custom()
                .tokenizer(UrlTokenizerFactory.class)
                .tokenFilter(UrlStopWordsFilterFactory.class)
                .tokenFilter(LowerCaseFilterFactory.class)
                .tokenFilter(SnowballPorterFilterFactory.class).param("language", "English")
                .tokenFilter(ASCIIFoldingFilterFactory.class);
        context.analyzer("english").custom()
                .tokenizer(StandardTokenizerFactory.class)
                .tokenFilter(LowerCaseFilterFactory.class)
                .tokenFilter(SnowballPorterFilterFactory.class).param("language", "English")
                .tokenFilter(ASCIIFoldingFilterFactory.class);
    }

    public static class UrlStopWordsFilterFactory extends TokenFilterFactory {
        private final CharArraySet stopWords = new CharArraySet(Set.of("https", "http", "www"), true);
        public UrlStopWordsFilterFactory(Map<String, String> args) {
            super(args);
        }

        @Override
        public TokenStream create(TokenStream input) {
            return new StopFilter(input, stopWords);
        }
    }

    public static class UrlTokenizerFactory extends TokenizerFactory {
        public UrlTokenizerFactory(Map<String, String> args) {
            super(args);
        }

        @Override
        public Tokenizer create(AttributeFactory factory) {
            return new CharTokenizer() {
                @Override
                protected boolean isTokenChar(int c) {
                    switch (c) {
                        case '.':
                        case ':':
                        case '/':
                        case '?':
                        case '&':
                        case '=':
                        case ';':
                        case '@':
                        case '+':
                        case '-':
                        case '_':
                            return false;
                        default:
                            return !Character.isWhitespace(c);
                    }
                }
            };
        }
    }
}
