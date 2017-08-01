package de.adorsys.multibanking.impl;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import de.adorsys.multibanking.domain.BankEntity;
import de.adorsys.multibanking.pers.spi.repository.BankRepositoryIf;
import de.adorsys.multibanking.repository.BankRepositoryMongodb;
import domain.Bank;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.regex.Pattern;

@Profile({"mongo", "fongo"})
@Service
public class BankRepositoryImpl implements BankRepositoryIf {

    @Autowired
    BankRepositoryMongodb bankRepositoryMongodb;

    @Autowired
    MongoTemplate mongoTemplate;

    @Override
    public Optional<BankEntity> findByBankCode(String blz) {
        return bankRepositoryMongodb.findByBankCode(blz);
    }

    @Override
    public void save(BankEntity bank) {
        bankRepositoryMongodb.save(bank);
    }

    @Override
    public List<BankEntity> search(String text) {
        Collection<String> terms = Sets.newHashSet(Arrays.asList(text.split(" ")));
        ArrayList<String> searchIdx = Lists.newArrayList();
        for (String term : terms) {
            searchIdx.add(term.toLowerCase());
        }

        Criteria criteria = new Criteria();
        for (String term : searchIdx) {
            criteria
                    .and("searchIndex")
                    .regex(Pattern.compile("^" + Pattern.quote(term)));
        }

        List<BankEntity> bankEntities = mongoTemplate.find(Query.query(criteria), BankEntity.class);

        return bankEntities;
    }


}
