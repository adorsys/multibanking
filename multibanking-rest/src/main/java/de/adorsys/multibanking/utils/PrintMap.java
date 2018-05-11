package de.adorsys.multibanking.utils;

import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

public class PrintMap {
	public static <K, V> String print(Map<K, V> map) {
		StringBuilder b = new StringBuilder();
		b.append("----------").append('\n');
		Set<Entry<K,V>> entrySet = map.entrySet();
		for (Entry<K, V> entry : entrySet) {
			b.append(entry).append('\n');
		}
		b.append("----------").append('\n');
		return b.toString();
	}

}
