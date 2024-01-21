package org.davidgiga1993.utils;

import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.util.List;


import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class FileWatcherCacheTest
{
	@Test
	public void alreadyProcessed() throws IOException
	{
		File tmp = new File("test.tmp");
		tmp.deleteOnExit();
		assertTrue(tmp.createNewFile());

		FileWatcherCache cache = new FileWatcherCache();

		assertFalse(cache.didAlreadyProcess(List.of(tmp)));
		assertTrue(cache.didAlreadyProcess(List.of(tmp)));

		assertTrue(tmp.setLastModified(System.currentTimeMillis() + 10));
		assertFalse(cache.didAlreadyProcess(List.of(tmp)));
	}
}