package studiplayer.audio;
import studiplayer.basic.WavParamReader;

public class WavFile extends SampledFile
{
	public WavFile() {super();}
	public WavFile(String path) throws NotPlayableException 
	{
		super(path);
		try
		{
			WavParamReader.readParams(path);
			readAndSetDurationFromFile();
		}
		catch (Exception e)
		{
			throw new NotPlayableException(getPathname(), e);
		}
	}
	
	public void	readAndSetDurationFromFile() throws NotPlayableException
	{
		try
		{
			setDuration(computeDuration(WavParamReader.getNumberOfFrames(), WavParamReader.getFrameRate()));
		}
		catch (Exception e)
		{
			throw new NotPlayableException(getPathname(), e);
		}
	}
		
	
    public String toString()
    {
    	return super.toString() + " - " + formatDuration();
    }
	
	public static long	computeDuration(long numberOfFrames, float frameRate){return (long)(numberOfFrames/frameRate * 1000000);}
}