package studiplayer.audio;

public class AudioFileFactory
{	
	public static AudioFile createAudioFile(String path) throws NotPlayableException
	{
		int i = path.lastIndexOf('.');
		String ext;
		if (i != -1)
			ext = (path.trim()).substring(path.lastIndexOf('.')).toLowerCase();
		else
			ext = "";
		if (ext.equals(".wav"))
			return new WavFile(path);
		else if (ext.equals(".ogg") || ext.equals(".mp3"))
			return new TaggedFile(path);
		throw new NotPlayableException (path, "Unknown suffix for AudioFile \"" + path + "\"");
	}
	
}