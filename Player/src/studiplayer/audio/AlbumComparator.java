package studiplayer.audio;
import java.util.Comparator;

public class AlbumComparator<T extends AudioFile> implements Comparator<T>
{
	public int compare(AudioFile o1, AudioFile o2)
	{
		if (o1 == null || o2 == null 
				|| ((o1 instanceof TaggedFile) && ((TaggedFile) o1).getAlbum() == null)
				|| ((o2 instanceof TaggedFile) && ((TaggedFile) o2).getAlbum() == null))
			throw new NullPointerException();
		if (o1 instanceof TaggedFile && o2 instanceof TaggedFile)
			return (((TaggedFile) o1).getAlbum()).compareTo(((TaggedFile) o2).getAlbum());
		else if (o1 instanceof TaggedFile)
			return 1;
		else if (o2 instanceof TaggedFile)
			return -1;
		return 0;
	}
}