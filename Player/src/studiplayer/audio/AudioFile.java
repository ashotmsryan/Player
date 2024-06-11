package studiplayer.audio;
import java.io.File;
abstract public class AudioFile
{
	private boolean isWindows(){return System.getProperty("os.name").toLowerCase().indexOf("win") >= 0;}
    
	protected String filename,pathname, author, title = "";
	private long duration;
    public AudioFile() {}
    
    public AudioFile(String path) throws NotPlayableException
    {
    	try 
    	{
    		filename = pathname = author = title = "";
    		parsePathname(path);
    		File fd = new File(pathname);
    		if (!fd.canRead())
    			throw new NotPlayableException(pathname, "file is not readable");
    		parseFilename(this.filename);
    	}
    	catch (Exception e)
    	{
    		throw new NotPlayableException(pathname, e);
    	}
    }

    
    public String getPathname(){return this.pathname;}
    public String getFilename(){return this.filename;}
    public String getAuthor(){return this.author;}
    public String getTitle(){return this.title;}
    public long getDuration() {return duration;}
    
    public void setDuration(long dur) {duration = dur;}

    public void parsePathname(String path)
    {
    	path = path.replaceAll("\\\\{2,}", "\\\\");
    	path = path.replaceAll("/{2,}", "/");
    	path = path.trim();
    	int i;
        if (!isWindows())
        {
        	path = path.replaceAll("\\\\+", "/"); 
        	path = path.replaceFirst("^([A-Za-z]):", "/$1");
        	i = path.length() - 1;
        	if (i == -1)
        		i = 0;
        	while(i != 0 && path.charAt(i) != '/')
        		i--;
        }
        else
        {
        	path = path.replaceAll("/", "\\\\");
        	i = path.length() - 1;
        	if (i == -1)
        		i = 0;
        	while(i != 0 && path.charAt(i) != '\\')
        		i--;
        	
        }
        if (i != 0)
        	i++;
        if (i == 0 && path.length() >= 1 && (path.charAt(i) == '/' || path.charAt(i) == '\\'))
        	this.filename = path.substring(i+1, path.length()).trim();
        else
        	this.filename = path.substring(i, path.length()).trim();
        if (i == 0)
        	this.pathname = path.substring(i, path.length());
        else
        	this.pathname = path;
    }
    
    public void parseFilename(String file)
    {
    	file = file.trim();
       	int i = file.lastIndexOf('.');
    	if (i != -1)
    		file = file.substring(0, i);
    	i = file.indexOf(" - ");
    	if (i == -1 || (i == 0 && file.length() == 1))
    	{
    		this.title = file;
    		title = title.trim();
        	this.author = "";
    	}
    	else if (i == -1 && file.length() == 1 && file.charAt(0) == '-')
    		title = author = "";
    	else
    	{
    		author = file.substring(0, i);
    		title = file.substring(i+3, file.length());
    		author = author.trim();
    		title = title.trim();
    	}
    }
    
    public String toString()
    {
    	if (getAuthor() == "")
    		return(this.title);
    	return (author + " - " + title);
    }
    
    abstract public void play() throws NotPlayableException; 
    abstract public void togglePause();
    abstract public void stop();
    abstract public String formatDuration();
    abstract public String formatPosition();
}
