package com.jas.cache;

public class CachePendingUpdateException extends RuntimeException {

	private static final long serialVersionUID = 6681399876131453822L;

	public CachePendingUpdateException( String message ) {
		super( message );
	}
}
