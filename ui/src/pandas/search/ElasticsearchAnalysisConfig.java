package pandas.search;

import org.hibernate.search.backend.elasticsearch.analysis.ElasticsearchAnalysisConfigurationContext;
import org.hibernate.search.backend.elasticsearch.analysis.ElasticsearchAnalysisConfigurer;

public class ElasticsearchAnalysisConfig implements ElasticsearchAnalysisConfigurer {

    @Override
    public void configure(ElasticsearchAnalysisConfigurationContext context) {

        // Built-in "standard" analyzer in Elasticsearch
        context.analyzer("standard").type("standard");

        // Reusable snowball filter with language param (must be defined separately in ES)
        context.tokenFilter("snowball_english")
                .type("snowball")
                .param("language", "English");

        // Stop filter for URL-ish tokens
        context.tokenFilter("url_stop")
                .type("stop")
                .param("stopwords", "https", "http", "www");

        // Tokenizer that splits on your punctuation set
        // char_group tokenizer splits on a list of chars
        context.tokenizer("url_tokenizer")
                .type("char_group")
                .param("tokenize_on_chars", ".", ":", "/", "?", "&", "=", ";", "@", "+", "-", "_");

        // "url" analyzer: url_tokenizer + stop + lowercase + snowball + asciifolding
        context.analyzer("url").custom()
                .tokenizer("url_tokenizer")
                .tokenFilters("url_stop", "lowercase", "snowball_english", "asciifolding");

        // "english" analyzer: standard tokenizer + lowercase + snowball + asciifolding
        context.analyzer("english").custom()
                .tokenizer("standard")
                .tokenFilters("lowercase", "snowball_english", "asciifolding");
    }
}