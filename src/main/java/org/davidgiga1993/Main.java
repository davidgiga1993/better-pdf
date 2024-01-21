package org.davidgiga1993;

import net.sourceforge.argparse4j.ArgumentParsers;
import net.sourceforge.argparse4j.inf.ArgumentParser;
import net.sourceforge.argparse4j.inf.ArgumentParserException;
import net.sourceforge.argparse4j.inf.Namespace;

import org.davidgiga1993.utils.FileUtils;
import org.davidgiga1993.utils.FileWatcherCache;
import org.davidgiga1993.utils.InvalidArgument;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileSystems;
import java.util.List;
import java.util.stream.Collectors;


import static java.nio.file.StandardWatchEventKinds.ENTRY_CREATE;

public class Main
{
	private static final Logger log = LoggerFactory.getLogger(Main.class);

	public static void main(String[] args) throws IOException, InvalidArgument
	{
		ArgumentParser parser = ArgumentParsers.newFor("better-pdf")
				.build()
				.defaultHelp(true)
				.description("PDF merging, splitting and other stuff. Daemon mode allows you to " +
						"create drop-in folders which automatically convert the pdfs");

		var modeParsers = parser.addSubparsers().help("Working mode");
		modeParsers.dest("mode");

		var duplexParser = modeParsers.addParser("duplex")
				.help("merges 2 pdf from duplex scanning into one");
		duplexParser.addArgument("-o", "--output")
				.dest("output")
				.help("The output pdf that should be created.")
				.required(true);
		duplexParser.addArgument("-i", "--input")
				.nargs(2)
				.dest("input")
				.help("The 2 input pdfs that should be merged. The first file must be the front, the 2nd the back pages.")
				.required(true);

		var duplexWatchParser = modeParsers.addParser("duplex-watch")
				.help("Same as 'duplex' but watches a directory for input pdfs. The action will be executed as soon as " +
						"2 pdfs have been found. The files will be sorted by name.");
		duplexWatchParser.addArgument("-o", "--output")
				.dest("output")
				.help("The output pdf that should be created.")
				.required(true);
		duplexWatchParser.addArgument("-s", "--source")
				.dest("source")
				.help("Source directory.")
				.required(true);

		Namespace parsedArgs;
		try
		{
			parsedArgs = parser.parseArgs(args);
		}
		catch (ArgumentParserException e)
		{
			parser.handleError(e);
			return;
		}

		new Main().handleArgs(parsedArgs);
	}


	private final Pdf pdf = new Pdf();
	private final FileWatcherCache cache = new FileWatcherCache();

	public Main()
	{
		System.setProperty("org.slf4j.simpleLogger.logFile", "System.out"); // Ugly
	}

	private void handleArgs(Namespace parsedArgs) throws InvalidArgument, IOException
	{
		String mode = parsedArgs.get("mode");
		if ("duplex-watch".equals(mode))
		{
			var source = new File((String) parsedArgs.get("source"));
			if (!source.exists())
			{
				throw new IOException("Source folder not found: " + source.getAbsolutePath());
			}
			var out = new File((String) parsedArgs.get("output"));

			watchForChanges(source, () -> {
				List<File> pdfs = FileUtils.get(source, ".pdf");
				if (pdfs.size() != 2 || cache.didAlreadyProcess(pdfs) || pdfs.contains(out))
				{
					return;
				}

				try
				{
					pdf.mergeDuplex(out, pdfs);
				}
				catch (InvalidArgument e)
				{
					log.error("Could not process: " + e.getMessage());
				}
			});

			return;
		}

		if ("duplex".equals(mode))
		{
			var out = new File((String) parsedArgs.get("output"));
			if (out.exists())
			{
				throw new IOException("Output file does already exist: " + out.getAbsolutePath());
			}
			List<String> inputPaths = parsedArgs.get("input");
			var inputFiles = inputPaths.stream().map(File::new).collect(Collectors.toList());
			pdf.mergeDuplex(out, inputFiles);
			return;
		}

		throw new InvalidArgument("Invalid mode: " + mode);
	}

	private void watchForChanges(File source, Runnable callback) throws IOException
	{
		try (var watchService = FileSystems.getDefault().newWatchService())
		{
			source.toPath().register(watchService, ENTRY_CREATE);

			while (true)
			{
				try
				{
					var result = watchService.take();
					callback.run();
					result.reset();
				}
				catch (InterruptedException e)
				{
					// Aborted
					return;
				}
			}
		}
	}
}
