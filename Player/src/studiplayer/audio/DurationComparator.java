package studiplayer.audio;
import java.util.Comparator;

public class DurationComparator<T extends AudioFile> implements Comparator<T>
{
	public int compare(AudioFile o1, AudioFile o2)
	{
		if (o1 == null || o2 == null)
			throw new NullPointerException();
		return (Long.compare(o1.getDuration(), o2.getDuration()));
	}
}