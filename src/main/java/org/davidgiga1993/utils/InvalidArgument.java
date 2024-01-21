package org.davidgiga1993.utils;

public class InvalidArgument extends Exception
{
	public InvalidArgument(String message)
	{
		super(message);
	}

	public InvalidArgument(Throwable cause)
	{
		super(cause);
	}
}
