package de.adorsys.multibanking.jpa.repository;

import de.adorsys.multibanking.jpa.entity.BankJpaEntity;
import lombok.RequiredArgsConstructor;
import org.apache.lucene.search.Query;
import org.hibernate.search.jpa.FullTextEntityManager;
import org.hibernate.search.jpa.FullTextQuery;
import org.hibernate.search.jpa.Search;
import org.hibernate.search.query.dsl.QueryBuilder;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.Profile;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;

import javax.persistence.EntityManagerFactory;

@RequiredArgsConstructor
@Profile({"jpa"})
@Service
public class HibernateSearchService implements ApplicationListener<ApplicationReadyEvent> {

    private static final String NAME_EDGE_NGRAM_INDEX = "edgeNGramName";
    private static final String NAME_NGRAM_INDEX = "nGramName";
    private static final String BANKCODE_EDGE_NGRAM_INDEX = "edgeNGramBankCode";
    private static final String BANKCODE_NGRAM_INDEX = "nGramBankCode";

    private final EntityManagerFactory entityManagerFactory;
    private FullTextEntityManager fullTextEntityManager;

    @Override
    public void onApplicationEvent(@NonNull ApplicationReadyEvent event) {
        reCreateIndex();
    }

    private void reCreateIndex() {
        try {
            fullTextEntityManager = Search.getFullTextEntityManager(entityManagerFactory.createEntityManager());
            fullTextEntityManager.createIndexer(BankJpaEntity.class).startAndWait();
        } catch (InterruptedException e) {
            System.out.println("An error occurred trying to build the search index: " + e.toString());
        }
    }

    public FullTextQuery searchBank(String terms) {
        QueryBuilder queryBuilder = fullTextEntityManager.getSearchFactory()
                .buildQueryBuilder().forEntity(BankJpaEntity.class).get();

        Query queryName = queryBuilder.phrase().withSlop(2).onField(NAME_NGRAM_INDEX)
                .andField(NAME_EDGE_NGRAM_INDEX).boostedTo(5)
                .sentence(terms.toLowerCase()).createQuery();

        Query queryBankCode = queryBuilder.phrase().withSlop(0).onField(BANKCODE_NGRAM_INDEX)
                .andField(BANKCODE_EDGE_NGRAM_INDEX).boostedTo(1)
                .sentence(terms.toLowerCase()).createQuery();

        Query query = queryBuilder
                .bool()
                .should(queryName)
                .should(queryBankCode)
                .createQuery();

        return fullTextEntityManager.createFullTextQuery(query, BankJpaEntity.class);

    }
}
