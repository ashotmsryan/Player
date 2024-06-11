package studiplayer.audio;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Scanner;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class PlayList implements Iterable<AudioFile>
{
	private List<AudioFile> list;
	private ControllablePlayListIterator<AudioFile> iterator;
	private String search = "";
	SortCriterion sortCriterion;
	private String path;
	Boolean flag = true;
	
	public PlayList()
	{
		list = new LinkedList<AudioFile>();
		setSortCriterion(SortCriterion.DEFAULT);
	}
	public PlayList(String path)
	{
		this();
		try 
		{
			loadFromM3U(path);
			resetIterator();
		}
		catch (NotPlayableException e) 
		{
			e.printStackTrace();
		}
	}
	
	public void resetIterator()
	{
		flag = true;
        this.iterator = new ControllablePlayListIterator<>(list, search, sortCriterion);
	}
	
	public Iterator<AudioFile> iterator()
	{
        return new ControllablePlayListIterator<>(list, search, sortCriterion);
    }
	
	public ControllablePlayListIterator<AudioFile> getIterator(){return iterator;}
	public String getSearch() {return search;}
	public SortCriterion getSortCriterion() {return sortCriterion;}
	public void setSortCriterion(SortCriterion sortCriterion)
	{
		this.sortCriterion = sortCriterion;
		resetIterator();
	}
	public void setSearch(String s)
	{
		this.search = s;
		resetIterator();
	}

	public List<AudioFile> getList(){return this.list;}
	public String getPath() {return path;};
	public void add(AudioFile obj)
	{
		this.list.add(obj);
		resetIterator();
	}
	public void remove(AudioFile obj)
	{
		this.list.remove(obj);
		resetIterator();
	}
	public int	size() {return this.list.size();}

	public AudioFile currentAudioFile()
	{
        if (iterator == null)
            return null;
        return iterator.getCur();
	}
	
	public void nextSong()
	{
        if (iterator == null || !iterator.hasNext())
        	resetIterator();
        else
        {
        	if (flag)
        	{
        		flag = false;
        		iterator.next();
        	}
        	iterator.next();
        }
        
	}

	
	public void saveAsM3U(String path)
	{
		FileWriter writer = null;
		String sep = System.getProperty("line.separator");
		try
		{
			writer = new FileWriter(path);
			for (int i = 0; i != list.size(); i++)
			{
				writer.write(list.get(i).getPathname() + sep);
			}
		}
		catch (IOException e) { throw new RuntimeException("Unable to write file " + path + "!");}
		finally
		{
			try
			{
				System.out.println("File " + path + " written!");
				writer.close();
			}
			catch (Exception e)
			{
				// ignore exception; probably because file could not be opened
			}
		}
	}
	
	
	public void loadFromM3U(String path) throws NotPlayableException
	{
		Scanner scanner = null;
        list.clear();
        try
        {
            scanner = new Scanner(new File(path));
        }
        catch (Exception e)
        {
        	e.printStackTrace();
        }
        try {
        	this.path = path;
            while (scanner.hasNextLine())
            {
                String line = scanner.nextLine();
                line = line.trim();
                if (!line.isEmpty() && line.charAt(0) != '#')
                {
                    try
                    {
                        list.add(AudioFileFactory.createAudioFile(line));
                    }
                    catch (NotPlayableException e)
                    {
                        System.err.println("Error playing file: " + path);
                        e.printStackTrace();
                        // Continue processing the next lines
                    }
                }
            }
        }
        finally
        {
            if (scanner != null)
            {
                try
                {
                    System.out.println("File " + path + " read!");
                    scanner.close();
                }
                catch (Exception e)
                {
                    // Ignore; probably because file could not be opened
                }
            }
        }
        resetIterator();
	}
	
	public void jumpToAudioFile(AudioFile obj)
	{
		resetIterator();
		while (iterator.hasNext())
            if (iterator.next().equals(obj))
                break;
		flag = false;
	}
	
	public String toString()
	{
		return list.toString();
	}
}