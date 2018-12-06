package segmentedfilesystem;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;

public class PacketController
{
    private int size = 0;
    public int maxSize = 0;
    public boolean hasHeader = false;
    public boolean isFinished = false;
    public String filename = "";
    public ArrayList<byte[]> storage = new ArrayList<byte[]>();

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

    //Adds a packet number into the correct spot in storage so we don't have to sort later
    public void AddElement(int position, byte[] data)
    {
        //Make sure the arraylist is large enough
        ensureSize(storage, position + 1);

        //Set it's position
        storage.set(position, data);

        size++;
        System.out.println("File " + getFileName() + " " + size + " out of " + maxSize);

        //If we have everything we need, then we can mark the file as complete
        if(maxSize > 0 && size == maxSize && getHeader())
        {
            isFinished = true;
        }
    }

    // This is a taxing process. To better this, instead of increasing size each time, it would be better to
    // start at some value (maybe 50) and double it each time we need it.
    public void ensureSize(ArrayList<byte[]> list, int size)
    {
        // Prevent excessive copying while we're adding
        list.ensureCapacity(size);
        while (list.size() < size)
        {
            list.add(null);
        }
    }

    //Builds the file from all of the bytes collected
    public void BuildFile() throws IOException
    {

        System.out.println("Writing file " + filename);

        //Gets the total amount of bytes
        int numBytes = 0;
        for(int i = 0; i < storage.size(); i++)
        {
            numBytes += storage.get(i).length;
        }

        //Creates the 'file' in terms of bytes
        byte[] finishedFile = new byte[numBytes];

        int tempSize = 0;

        //Adds the bytes to the file
        for(int i = 0; i < storage.size(); i++)
        {
            for(int o = 0; o < storage.get(i).length; o++)
            {
                finishedFile[tempSize] = storage.get(i)[o];
                tempSize++;
            }
        }

        //Writes the file to directory
        try(FileOutputStream fos = new FileOutputStream("output/" + filename))
        {
            fos.write(finishedFile);
        }
        catch(FileNotFoundException e)
        {
            System.out.println(e);
        }
    }
}
