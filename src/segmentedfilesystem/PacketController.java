package segmentedfilesystem;

import java.util.ArrayList;

public class PacketController
{
    private int size = 0;
    public int maxSize = 0;
    public boolean hasHeader = false;
    public boolean isFinished = false;
    public String filename ="";
    public ArrayList<byte[]> storage = new ArrayList<>();

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

    public String getFileName()
    {
        return this.filename;
    }

    public void setFileName(String newfilename)
    {
        this.filename = newfilename;
    }

    public void AddElement(int position, byte[] data)
    {
        ensureSize(storage, position + 1);

        storage.set(position, data);

        size++;
        System.out.println("File " + getFileName() + " " + size + " out of " + maxSize);

        if(maxSize > 0 && size == maxSize && getHeader())
        {
            isFinished = true;
        }
    }

    public void ensureSize(ArrayList<byte[]> list, int size)
    {
        // Prevent excessive copying while we're adding
        list.ensureCapacity(size);
        while (list.size() < size)
        {
            list.add(null);
        }
    }
}
