package de.cgawron.upnp;

import java.awt.BorderLayout;
import java.util.List;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JProgressBar;
import javax.swing.SwingWorker;

public class ElapsedTime extends JComponent
{
	private final JLabel totalTimeLabel;
	private final JProgressBar progressBar;
	private int totalTime;
	private int elapsedTime;
	private long started;
	private boolean playing;
	private Updater updater;
	private static final Pattern durationPattern = Pattern.compile("(\\d\\d):(\\d\\d):(\\d\\d)");
	private static Logger logger = Logger.getLogger(ElapsedTime.class.getName());

	class Updater extends SwingWorker<Void, Long>
	{
		@Override
		public Void doInBackground()
		{
			while (playing) {
				publish(System.currentTimeMillis() - started);
				try {
					Thread.sleep(100);
				} catch (InterruptedException ignore) {
				}
			}
			return null;
		}

		@Override
		protected void process(List<Long> l)
		{
			long millis = l.get(0);
			int seconds = (int) (millis / 1000);
			progressBar.setValue(seconds);
			progressBar.setString(durationString(seconds));
		}
	}

	public ElapsedTime()
	{
		setLayout(new BorderLayout(0, 0));

		progressBar = new JProgressBar();
		progressBar.setStringPainted(true);
		add(progressBar);

		totalTimeLabel = new JLabel("0:00");
		add(totalTimeLabel, BorderLayout.EAST);
	}

	public String durationString(int sec)
	{
		int seconds = sec % 60;
		int minutes = sec / 60;
		int hours = minutes / 60;
		minutes = minutes % 60;
		return String.format("%d:%02d:%02d", hours, minutes, seconds);
	}

	private static final long serialVersionUID = 1L;

	public void setDuration(String duration)
	{
		totalTime = parseDuration(duration);
		totalTimeLabel.setText(duration);
		progressBar.setMaximum(totalTime);
	}

	public void startPlaying()
	{
		started = System.currentTimeMillis();
		playing = true;
		if (updater == null)
			updater = new Updater();
		updater.execute();
	}

	public void stopPlaying()
	{

	}

	public void reset()
	{
		elapsedTime = 0;
	}

	public static int parseDuration(String duration)
	{
		logger.info("Parsing " + duration);
		int d = 0;
		Matcher m = durationPattern.matcher(duration);
		m.matches();
		for (int i = 1; i <= 3; i++) {
			d *= 60;
			d += Integer.parseInt(m.group(i));
		}
		return d;
	}

}
