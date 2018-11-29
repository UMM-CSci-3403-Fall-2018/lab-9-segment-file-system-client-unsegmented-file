package segmentedfilesystem;

public class PacketController
{
    public int size = 0;
    public int maxSize = 0;
    public boolean hasHeader = false;
    public boolean isFinished = false;
    public String filename ="";

    public int getSize()
    {
        return size;
    }

    public void setSize(int newMaxSize)
    {
        this.size += newMaxSize;

        if(this.size > 0 && this.size == this.maxSize)
        {
            this.isFinished = true;
        }
    }

    public int getMaxSize()
    {
        return maxSize;
    }

    public void setMaxSize(int newMaxSize)
    {
        this.maxSize = newMaxSize;
    }

    public boolean getHeader()
    {
        return hasHeader;
    }

    public void setHasHeader(boolean newHasHeader)
    {
        this.hasHeader = newHasHeader;
    }

    public boolean getIsFinished()
    {
        return this.isFinished;
    }

    public String getFileName(){ return this.filename; }

    public void setFileName(String newfilename){ this.filename = newfilename; }
}
