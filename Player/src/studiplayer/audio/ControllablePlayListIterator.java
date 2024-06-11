package studiplayer.audio;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

public class ControllablePlayListIterator<T extends AudioFile> implements Iterator<T>
{
	    private List<T> list; 
	    private int i = 0;
	    private Boolean flag = true;

	    public ControllablePlayListIterator(List<T> list)
	    {
	    	this.list = new LinkedList<T>(list);
	    }
	    
	    public T getCur()
	    {
	    	if (!list.isEmpty())
	    		return list.get(i);
	    	return null;
	    }
	    
	    public void setInx(int x) {this.i = x;}
	    
	    public ControllablePlayListIterator(List<T> lst, String search, SortCriterion sort)
	    {
	    	this.list = new LinkedList<T>();
	    	
	    	for (T item : lst)
	    	{
	    		if (search == null || search.isEmpty() ||
	    				item.getAuthor().contains(search) || 
	                    item.getTitle().contains(search) || ((item instanceof TaggedFile) &&
	                    ((TaggedFile)item).getAlbum().contains(search)))
	             	this.list.add(item);
	    	}
	        if (sort != SortCriterion.DEFAULT)
	        {
	            Comparator<T> comparator = null;
	            switch (sort)
	            {
	                case ALBUM:
	                    comparator = new AlbumComparator<>();
	                    break;
	                case AUTHOR:
	                    comparator = new AuthorComparator<>();
	                    break;
	                case TITLE:
	                    comparator = new TitleComparator<>();
	                    break;
	                case DURATION:
	                    comparator = new DurationComparator<>();
	                    break;
				default:
					break;
	            }
	            if (comparator != null)
	                Collections.sort(this.list, comparator);
	        }
	    }

	    public boolean hasNext()
	    {
	        return i + 1 < list.size();
	    }

		public T next()
	    {
			if (flag)
			{
				flag = false;
				return list.get(i);
			}
			else
	        	return list.get(++i);
	    }

	    public T jumpToAudioFile(T file)
	    {
	        int index = list.indexOf(file);
	        if (index == -1)
	            return null;
	        i = index;
	        return list.get(index);
	    }
}