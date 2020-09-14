package pandas.admin.search;

import org.apache.lucene.analysis.en.EnglishAnalyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.hibernate.search.backend.lucene.analysis.LuceneAnalysisConfigurationContext;
import org.hibernate.search.backend.lucene.analysis.LuceneAnalysisConfigurer;

public class SearchAnalysisConfig implements LuceneAnalysisConfigurer {
    @Override
    public void configure(LuceneAnalysisConfigurationContext context) {
        context.analyzer("standard").instance(new StandardAnalyzer());
        context.analyzer( "english" ).instance(new StandardAnalyzer(EnglishAnalyzer.ENGLISH_STOP_WORDS_SET));
        context.analyzer( "url" ).instance(new StandardAnalyzer());
    }
}
