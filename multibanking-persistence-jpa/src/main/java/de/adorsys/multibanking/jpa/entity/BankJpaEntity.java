package de.adorsys.multibanking.jpa.entity;

import de.adorsys.multibanking.domain.BankApi;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.apache.lucene.analysis.core.KeywordTokenizerFactory;
import org.apache.lucene.analysis.core.LowerCaseFilterFactory;
import org.apache.lucene.analysis.core.StopFilterFactory;
import org.apache.lucene.analysis.miscellaneous.WordDelimiterFilterFactory;
import org.apache.lucene.analysis.ngram.EdgeNGramFilterFactory;
import org.apache.lucene.analysis.ngram.NGramFilterFactory;
import org.apache.lucene.analysis.pattern.PatternReplaceFilterFactory;
import org.apache.lucene.analysis.standard.StandardTokenizerFactory;
import org.hibernate.search.annotations.*;

import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;

@Indexed
@Entity(name="bank")
@Data
@EqualsAndHashCode(callSuper = false)
@AnalyzerDefs({
        @AnalyzerDef(name = "autocompleteEdgeAnalyzer",
                tokenizer = @TokenizerDef(factory = KeywordTokenizerFactory.class),
                filters = {
                        @TokenFilterDef(factory = PatternReplaceFilterFactory.class, params = {
                                @Parameter(name = "pattern", value = "([^a-zA-Z0-9\\.])"),
                                @Parameter(name = "replacement", value = " "),
                                @Parameter(name = "replace", value = "all")}),
                        @TokenFilterDef(factory = LowerCaseFilterFactory.class),
                        @TokenFilterDef(factory = StopFilterFactory.class),
                        @TokenFilterDef(factory = EdgeNGramFilterFactory.class, params = {
                                @Parameter(name = "minGramSize", value = "3"),
                                @Parameter(name = "maxGramSize", value = "50")})}),

        @AnalyzerDef(name = "autocompleteNGramAnalyzer",
                tokenizer = @TokenizerDef(factory = StandardTokenizerFactory.class),
                filters = {
                        @TokenFilterDef(factory = WordDelimiterFilterFactory.class),
                        @TokenFilterDef(factory = LowerCaseFilterFactory.class),
                        @TokenFilterDef(factory = NGramFilterFactory.class, params = {
                                @Parameter(name = "minGramSize", value = "3"),
                                @Parameter(name = "maxGramSize", value = "5")}),
                        @TokenFilterDef(factory = PatternReplaceFilterFactory.class, params = {
                                @Parameter(name = "pattern", value = "([^a-zA-Z0-9\\.])"),
                                @Parameter(name = "replacement", value = " "),
                                @Parameter(name = "replace", value = "all")})
                }),

        @AnalyzerDef(name = "standardAnalyzer",
                tokenizer = @TokenizerDef(factory = StandardTokenizerFactory.class),
                filters = {
                        @TokenFilterDef(factory = WordDelimiterFilterFactory.class),
                        @TokenFilterDef(factory = LowerCaseFilterFactory.class),
                        @TokenFilterDef(factory = PatternReplaceFilterFactory.class, params = {
                                @Parameter(name = "pattern", value = "([^a-zA-Z0-9\\.])"),
                                @Parameter(name = "replacement", value = " "),
                                @Parameter(name = "replace", value = "all")}
                        ),
                        @TokenFilterDef(factory = EdgeNGramFilterFactory.class, params = {
                                @Parameter(name = "minGramSize", value = "4"),
                                @Parameter(name = "maxGramSize", value = "8")})
                })
})
public class BankJpaEntity {

    @Id
    //@GeneratedValue
    private Long id;
    private String blzHbci;

    private String bankingUrl;
    @Field(name = "bankCode", index = Index.YES, store = Store.YES,
            analyze = Analyze.YES, analyzer = @Analyzer(definition = "standardAnalyzer"))
    @Field(name = "edgeNGramBankCode", index = Index.YES, store = Store.NO,
            analyze = Analyze.YES, analyzer = @Analyzer(definition = "standardAnalyzer"))
    @Field(name = "nGramBankCode", index = Index.YES, store = Store.NO,
            analyze = Analyze.YES, analyzer = @Analyzer(definition = "standardAnalyzer"))
    private String bankCode;
    private String bic;
    @Field(name = "name", index = Index.YES, store = Store.YES,
            analyze = Analyze.YES, analyzer = @Analyzer(definition = "standardAnalyzer"))
    @Field(name = "edgeNGramName", index = Index.YES, store = Store.NO,
            analyze = Analyze.YES, analyzer = @Analyzer(definition = "autocompleteEdgeAnalyzer"))
    @Field(name = "nGramName", index = Index.YES, store = Store.NO,
            analyze = Analyze.YES, analyzer = @Analyzer(definition = "autocompleteNGramAnalyzer"))
    private String name;
    @Embedded
    private BankLoginSettingsJpaEntity loginSettings;
    private BankApi bankApi;
    private boolean ibanRequired;

}
