package studiplayer.audio;
import studiplayer.basic.BasicPlayer;

abstract public class SampledFile extends AudioFile
{
	private static String format;
	
	public SampledFile() {super();}
	public SampledFile(String path) throws NotPlayableException 
	{
		super(path);
	}
	
	public void play() throws NotPlayableException 
	{
		try
		{
			BasicPlayer.play(pathname);
		}
		catch (Exception e)
		{
			throw new NotPlayableException(pathname, e);
		}
	}
	public void togglePause() {BasicPlayer.togglePause();}
	public void stop() {BasicPlayer.stop();}
	
	public String formatDuration() {return (SampledFile.timeFormatter(getDuration()));}
	
	public String formatPosition() {return (SampledFile.timeFormatter(BasicPlayer.getPosition()));}
	
	public static String timeFormatter(long timeInMicroSeconds)
	{
	    if (timeInMicroSeconds < 0)
	        throw new RuntimeException("Time cannot be negative");
		long s = timeInMicroSeconds / 1000000;
		long m = (s / 60);
		s = s - m*60;
		if (m > 99)
			throw new RuntimeException("Overflow");
		format = String.format("%02d:%02d", m, s);
		return format;
	}
}