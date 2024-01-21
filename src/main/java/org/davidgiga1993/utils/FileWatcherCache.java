package org.davidgiga1993.utils;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class FileWatcherCache
{
	private final List<CacheEntry> lastProcessed = new ArrayList<>();

	/**
	 * Adds the given file to the cache and checks if the previously added
	 * files are the same (same timestamp and filename)
	 *
	 * @param files Files to be checked
	 * @return true if already processed
	 */
	public boolean didAlreadyProcess(List<File> files)
	{
		var newEntries = files.stream().map(CacheEntry::new).collect(Collectors.toList());
		if (lastProcessed.size() != files.size())
		{
			lastProcessed.clear();
			lastProcessed.addAll(newEntries);
			return false;
		}

		for (CacheEntry newEntry : newEntries)
		{
			if (!lastProcessed.contains(newEntry))
			{
				lastProcessed.clear();
				lastProcessed.addAll(newEntries);
				return false;
			}
		}

		return true;
	}

	protected static class CacheEntry
	{
		public final String name;
		public final long lastModified;

		public CacheEntry(File file)
		{
			this.name = file.getName();
			this.lastModified = file.lastModified();
		}

		@Override
		public boolean equals(Object o)
		{
			if (this == o)
			{
				return true;
			}
			if (o == null || getClass() != o.getClass())
			{
				return false;
			}
			CacheEntry that = (CacheEntry) o;
			return lastModified == that.lastModified && Objects.equals(name, that.name);
		}

		@Override
		public int hashCode()
		{
			return Objects.hash(name, lastModified);
		}
	}
}
