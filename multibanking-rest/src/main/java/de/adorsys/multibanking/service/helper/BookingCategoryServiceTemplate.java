package de.adorsys.multibanking.service.helper;

import java.util.Collections;
import java.util.List;

import org.adorsys.docusafe.business.DocumentSafeService;
import org.adorsys.docusafe.business.types.complex.DSDocument;
import org.springframework.beans.factory.annotation.Autowired;

import com.fasterxml.jackson.core.type.TypeReference;

import de.adorsys.multibanking.domain.CategoryEntity;
import de.adorsys.multibanking.service.base.CacheBasedService;
import de.adorsys.multibanking.service.base.ListUtils;

/**
 * TODO Reset rules provider after every change.
 * 
 * @author fpo 2018-03-24 04:34
 *
 */
public abstract class BookingCategoryServiceTemplate<T extends CategoryEntity> {
	
	protected abstract CacheBasedService cbs();
	protected abstract TypeReference<List<T>> listType();
	
    @Autowired
    private DocumentSafeService documentSafeService;

	public DSDocument getBookingCategories() {
        return documentSafeService.readDocument(cbs().auth(), CategoryUtils.bookingCategoriesFQN);
    }

	public void createOrUpdateCategory(T categoryEntity) {
		createOrUpdateCategories(Collections.singletonList(categoryEntity));
	}
	
	public void createOrUpdateCategories(List<T> categoryEntities) {
		List<T> persList = cbs().load(CategoryUtils.bookingCategoriesFQN, listType()).orElse(Collections.emptyList());
		persList = ListUtils.updateList(categoryEntities, persList);
		cbs().store(CategoryUtils.bookingCategoriesFQN, listType(), persList);
	}
	
	public void replceCategories(List<T> categoryEntities) {
		categoryEntities = ListUtils.setId(categoryEntities);
		cbs().store(CategoryUtils.bookingCategoriesFQN, listType(), categoryEntities);
	}
	
	public boolean deleteCategory(String categoryId) {
		return deleteCategories(Collections.singletonList(categoryId));
	}
	public boolean deleteCategories(List<String> categoryIds) {
		List<T> persList = cbs().load(CategoryUtils.bookingCategoriesFQN, listType()).orElse(Collections.emptyList());
		List<T> newPersList = ListUtils.deleteListById(categoryIds, persList);
		cbs().store(CategoryUtils.bookingCategoriesFQN, listType(), newPersList);
		return (newPersList.size() - persList.size()) !=0;
	}
}
