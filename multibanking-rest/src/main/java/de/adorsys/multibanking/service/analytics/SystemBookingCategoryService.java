package de.adorsys.multibanking.service.analytics;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.type.TypeReference;

import de.adorsys.multibanking.domain.CategoryEntity;
import de.adorsys.multibanking.service.base.SystemObjectService;
import de.adorsys.multibanking.service.base.CacheBasedService;
import de.adorsys.multibanking.service.helper.BookingCategoryServiceTemplate;

/**
 * TODO Reset rules provider after every change.
 * 
 * @author fpo 2018-03-24 04:34
 *
 */
@Service
public class SystemBookingCategoryService extends BookingCategoryServiceTemplate<CategoryEntity>{
	@Autowired
	private SystemObjectService sos;

	@Override
	protected CacheBasedService cbs() {
		return sos;
	}

	@Override
	protected TypeReference<List<CategoryEntity>> listType() {
		return new TypeReference<List<CategoryEntity>>() {};
	}
}
