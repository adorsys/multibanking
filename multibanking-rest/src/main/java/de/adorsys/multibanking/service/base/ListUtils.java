package de.adorsys.multibanking.service.base;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import de.adorsys.multibanking.domain.common.IdentityIf;
import de.adorsys.multibanking.utils.Ids;

/**
 * Base class for providing access to object.
 * 
 * Provides caching functionality when enabled.
 * 
 * @author fpo 2018-04-06 04:36
 *
 */
public abstract class ListUtils {
	
	/**
	 * Find and return the object with id from klass with type from document.
	 * 
	 * @param id
	 * @param list
	 * @return
	 */
	public static <T extends IdentityIf> Optional<T> find(String id, List<T> list) {
		return list.stream().filter(t -> Ids.eq(id, t.getId())).findFirst();
	}

	/**
	 * Sets the id if none is existent.
	 * 
	 * @param inputList
	 */
	public static <T extends IdentityIf> List<T> setId(List<T> inputList) {
		inputList.stream().forEach(n -> Ids.id(n));
		return inputList;
	}
	
	/**
	 * Update a list.
	 * 
	 * @param inputList
	 * @param persList
	 * @return
	 */
	public static <T extends IdentityIf> List<T> updateList(List<T> inputList, List<T> persList) {

		// Existing elements. We assume the equals method proceed by id.
		List<T> foundElements = inputList.stream().filter(i -> persList.contains(i))
				.collect(Collectors.toList());
		ArrayList<T> newList = new ArrayList<>(inputList);
		newList.removeAll(foundElements);

		// Override existing.
		foundElements.stream().forEach(e -> {
			int indexOf = persList.indexOf(e);
			persList.set(indexOf, e);
//			IdentityIf pers = persList.get(indexOf);
//			BeanUtils.copyProperties(e, pers);
		});

		// Add new
		List<T> finalList = new ArrayList<>(persList);
		newList.stream().forEach(n -> {
			Ids.id(n);
			finalList.add(n);
		});

		return finalList;
	}

	/**
	 * Deletes all occurrences of the list from the persistent storage.
	 * 
	 * @param inputList
	 * @param persList
	 * @return
	 */
	public static <T extends IdentityIf> List<T> deleteList(List<T> inputList, List<T> persList) {
		persList.removeAll(inputList);
		return persList;
	}

	/**
	 * Returns the number of record deleted.
	 *
	 * @param inputIdList
	 * @param persList
	 * @return
	 */
	public static <T extends IdentityIf> List<T> deleteListById(List<String> inputIdList, List<T> persList) {
		List<T> inputList = persList.stream().filter(e -> inputIdList.contains(e.getId())).collect(Collectors.toList());
		if (!inputList.isEmpty()) {
			persList.removeAll(inputList);
		}
		return persList;
	}
}
