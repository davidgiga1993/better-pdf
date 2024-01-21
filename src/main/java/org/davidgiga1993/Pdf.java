package org.davidgiga1993;

import org.davidgiga1993.utils.InvalidArgument;
import org.sejda.core.notification.context.GlobalNotificationContext;
import org.sejda.core.service.DefaultTaskExecutionService;
import org.sejda.core.service.TaskExecutionService;
import org.sejda.model.input.PdfFileSource;
import org.sejda.model.input.PdfMixInput;
import org.sejda.model.notification.EventListener;
import org.sejda.model.notification.event.PercentageOfWorkDoneChangedEvent;
import org.sejda.model.notification.event.TaskExecutionCompletedEvent;
import org.sejda.model.notification.event.TaskExecutionFailedEvent;
import org.sejda.model.output.FileTaskOutput;
import org.sejda.model.parameter.AlternateMixMultipleInputParameters;
import org.sejda.model.parameter.base.TaskParameters;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.util.List;

public class Pdf
{
	private static final Logger log = LoggerFactory.getLogger(Pdf.class);

	public Pdf()
	{
		registerTaskListeners();
	}

	void mergeDuplex(File output, List<File> inputs) throws InvalidArgument
	{
		if (inputs.size() != 2)
		{
			throw new InvalidArgument("2 input pdf required");
		}

		AlternateMixMultipleInputParameters taskParameters = new AlternateMixMultipleInputParameters();
		taskParameters.addInput(new PdfMixInput(PdfFileSource.newInstanceNoPassword(inputs.get(0)), false, 1));
		taskParameters.addInput(new PdfMixInput(PdfFileSource.newInstanceNoPassword(inputs.get(1)), true, 1));
		taskParameters.setOutput(new FileTaskOutput(output));
		execute(taskParameters);

		try
		{
			// Unix: Make sure the permissions are the same
			var inputPermissions = Files.getPosixFilePermissions(inputs.get(0).toPath(), LinkOption.NOFOLLOW_LINKS);
			Files.setPosixFilePermissions(output.toPath(), inputPermissions);
		}
		catch (UnsupportedOperationException | IOException e)
		{
			// OS might not be supported - ignore
		}
	}

	private void execute(TaskParameters taskParameters)
	{
		TaskExecutionService taskExecutionService = new DefaultTaskExecutionService();
		taskExecutionService.execute(taskParameters);
	}

	private void registerTaskListeners()
	{
		GlobalNotificationContext.getContext().addListener(new ProgressListener());
		GlobalNotificationContext.getContext().addListener(new FailureListener());
		GlobalNotificationContext.getContext().addListener(new CompletionListener());
	}

	private static class ProgressListener implements EventListener<PercentageOfWorkDoneChangedEvent>
	{

		@Override
		public void onEvent(PercentageOfWorkDoneChangedEvent event)
		{
			log.info("Task progress: {}% done.", event.getPercentage().toPlainString());
		}
	}

	private static class FailureListener implements EventListener<TaskExecutionFailedEvent>
	{

		@Override
		public void onEvent(TaskExecutionFailedEvent event)
		{
			log.error("Task execution failed", event.getFailingCause());
		}
	}

	private static class CompletionListener implements EventListener<TaskExecutionCompletedEvent>
	{

		@Override
		public void onEvent(TaskExecutionCompletedEvent event)
		{
			log.info("Task completed in {} millis.", event.getExecutionTime());
		}

	}
}
