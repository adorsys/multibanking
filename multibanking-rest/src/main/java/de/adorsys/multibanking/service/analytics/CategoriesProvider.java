package de.adorsys.multibanking.service.analytics;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import domain.RuleCategory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

/**
 * Created by alexg on 15.08.17.
 */
@Service
public class CategoriesProvider {

    private List<RuleCategory> categoriesTree;

    private static final Logger LOG = LoggerFactory.getLogger(CategoriesProvider.class);

    @PostConstruct
    public void postConstruct() throws IOException {
        loadCategoriesTree();
    }

    private void loadCategoriesTree() throws IOException {
        try (InputStream inputStream = this.getClass().getClassLoader().getResource("analytics/categories.yml").openStream()) {
            final YAMLFactory ymlFactory = new YAMLFactory();
            ObjectMapper objectMapper = new ObjectMapper(ymlFactory);

            categoriesTree = objectMapper.readValue(inputStream, new TypeReference<List<RuleCategory>>() {
            });
        }
    }

    public List<RuleCategory> getCategoriesTree() {
        return this.categoriesTree;
    }
}
