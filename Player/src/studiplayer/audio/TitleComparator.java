package studiplayer.audio;
import java.util.Comparator;

public class TitleComparator<T extends AudioFile> implements Comparator<T>
{
	public int compare(AudioFile o1, AudioFile o2)
	{
		if (o1 == null || o2 == null)
			throw new NullPointerException();
		return o1.getTitle().compareTo(o2.getTitle());
	}
}