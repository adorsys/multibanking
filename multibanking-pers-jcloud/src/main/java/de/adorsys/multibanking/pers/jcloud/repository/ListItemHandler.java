package de.adorsys.multibanking.pers.jcloud.repository;

public interface ListItemHandler<T> {
	public boolean idEquals(T a, T b);
	public boolean newId(T a);
}
