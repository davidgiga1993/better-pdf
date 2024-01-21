package org.davidgiga1993.utils;

import java.io.File;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class FileUtils
{

	/**
	 * Returns all files in the given dir which match the given extension, sorted by name ascending
	 *
	 * @param dir       Directory
	 * @param extension Extension
	 * @return Files
	 */
	public static List<File> get(File dir, String extension)
	{
		var files = dir.listFiles((unusedDir, name) -> name.endsWith(extension));
		if (files == null)
		{
			return new ArrayList<>();
		}

		var list = new ArrayList<>(List.of(files));
		list.sort(Comparator.comparing(File::getName));
		return list;
	}
}
