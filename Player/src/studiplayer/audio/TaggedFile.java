package studiplayer.audio;
import java.util.*;

import studiplayer.basic.TagReader;
public class TaggedFile extends SampledFile
{
	private String album = "";
	
	public String getAlbum() {return album;}
	public void	setAlbum(String a) {album = a;}
	public TaggedFile() throws NotPlayableException
	{
		super();
		readAndStoreTags();
	}
	public TaggedFile(String path) throws NotPlayableException 
	{
		super(path);
		readAndStoreTags();
	}
	
	public void readAndStoreTags() throws NotPlayableException
	{
		try
		{
			Map<String, Object> tagMap = TagReader.readTags(this.getPathname());
			if (tagMap.get("title") != null)
				title = ((String)tagMap.get("title")).trim();
			if (tagMap.get("author") != null)
				author = ((String)tagMap.get("author")).trim();
			if (tagMap.get("album") != null)
				album = ((String)tagMap.get("album")).trim();
			if (tagMap.get("duration") != null)
				setDuration((long)tagMap.get("duration"));
		}
		catch (Exception e)
		{
			throw new NotPlayableException (this.getPathname(), e);
		}
	}
	
	public String toString()
	{
		if (album.isEmpty())
			return super.toString() + " - " + formatDuration();
		else
			return super.toString() + " - " + album + " - " + formatDuration();
	}
}