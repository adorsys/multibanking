package de.adorsys.multibanking.service.analytics;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.type.TypeReference;

import de.adorsys.multibanking.domain.CustomCategoryEntity;
import de.adorsys.multibanking.service.base.UserObjectService;
import de.adorsys.multibanking.service.base.CacheBasedService;
import de.adorsys.multibanking.service.helper.BookingCategoryServiceTemplate;

/**
 * TODO Reset rules provider after every change.
 * 
 * @author fpo 2018-03-24 04:34
 *
 */
@Service
public class CustomBookingCategoryService extends BookingCategoryServiceTemplate<CustomCategoryEntity>{
	@Autowired
	private UserObjectService uos;

	protected TypeReference<List<CustomCategoryEntity>> listType(){
		return new TypeReference<List<CustomCategoryEntity>>() {};
	}

	@Override
	protected CacheBasedService cbs() {
		return uos;
	}
}
