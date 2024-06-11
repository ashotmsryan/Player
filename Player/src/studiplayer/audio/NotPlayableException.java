package studiplayer.audio;

public class NotPlayableException extends Exception 
{
	private static final long serialVersionUID = -4739760064658942414L;
	private String pathname;

	public NotPlayableException(String pathname, String msg)
	{
		super(msg);
		this.pathname = pathname;
//		System.out.print(msg);
	}
	
	public NotPlayableException(String pathname, Throwable t)
	{
		super(t);
		this.pathname = pathname;
	}
	
	public NotPlayableException(String pathname, String msg, Throwable t)
	{
		super (msg, t);
		this.pathname = pathname;
//		System.out.print(msg);
	}
	
    public String getPathname() {return pathname;}
	public String toString(){return "studiplayer.audio.NotPlayableException: " + pathname + ": " + getMessage();}
	
}