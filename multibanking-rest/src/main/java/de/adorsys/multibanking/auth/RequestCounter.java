package de.adorsys.multibanking.auth;

import java.util.HashMap;
import java.util.Map;

import org.adorsys.docusafe.business.types.complex.DocumentFQN;

import de.adorsys.multibanking.utils.PrintMap;

public class RequestCounter {
	private Map<DocumentFQN, Counter> counterMap = new HashMap<>();
	public void load(DocumentFQN fqn){
		counter(fqn).load+=1;
	}
	public void store(DocumentFQN fqn){
		counter(fqn).store+=1;		
	}
	public void cacheHit(DocumentFQN fqn){
		counter(fqn).cacheHit+=1;		
	}
	public void flush(DocumentFQN fqn){
		counter(fqn).flush+=1;		
	}
	public void delete(DocumentFQN fqn) {
		counter(fqn).delete+=1;		
	}
	
	static class Counter {
		int load;
		int cacheHit;
		int store;
		int flush;
		int delete;
		@Override
		public String toString() {
			return "Counter [load=" + load + ", cacheHit=" + cacheHit + ", store=" + store + ", flush=" + flush
					+ ", delete=" + delete + "]";
		}
	}
	
	private Counter counter(DocumentFQN fqn){
		Counter counter = counterMap.get(fqn);
		if(counter==null){
			counter = new Counter();
			counterMap.put(fqn, counter);
		}
		return counter;
	}
	@Override
	public String toString() {
		return PrintMap.print(counterMap);
	}
	
	
}
