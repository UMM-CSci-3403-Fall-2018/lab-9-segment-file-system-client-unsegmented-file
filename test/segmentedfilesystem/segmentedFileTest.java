package segmentedfilesystem;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;
import sun.net.www.HeaderParser;

import java.util.HashMap;


/**
 * This is just a stub test file. You should rename it to
 * something meaningful in your context and populate it with
 * useful tests.
 */
public class segmentedFileTest
{

    Main main = new Main();
    PacketController pc = new PacketController();

    byte[] HeaderPacket = new byte[8];
    byte[] DataPacket = new byte[8];
    byte[] LastDataPacket = new byte[6];
    byte[] SecondDataPacket = new byte[2];
    HashMap<Integer, Integer> map = new HashMap<>();


    @Before
    public void setUp()
    {
        //Header Packet
        HeaderPacket[0] = (byte) 0;
        HeaderPacket[1] = (byte) 24;
        HeaderPacket[2] = (byte) 'H';
        HeaderPacket[3] = (byte) 'i';
        HeaderPacket[4] = (byte) '.';
        HeaderPacket[5] = (byte) 't';
        HeaderPacket[6] = (byte) 'x';
        HeaderPacket[7] = (byte) 't';

        //Data Packet
        DataPacket[0] = (byte) 1;
        DataPacket[1] = (byte) 24;
        DataPacket[2] = (byte) 0;
        DataPacket[3] = (byte) 4;
        DataPacket[4] = (byte) 10;
        DataPacket[5] = (byte) 14;
        DataPacket[6] = (byte) 9;
        DataPacket[7] = (byte) 5;

        //Last Data Packet
        LastDataPacket[0] = (byte) 3;
        LastDataPacket[1] = (byte) 24;
        LastDataPacket[2] = (byte) 0;
        LastDataPacket[3] = (byte) 2;
        LastDataPacket[4] = (byte) 8;
        LastDataPacket[5] = (byte) 15;

        //Testing for Hash Maps
        SecondDataPacket[0] = (byte) 1;
        SecondDataPacket[1] = (byte) 32;
    }

    @Test
    public void GetsFileName()
    {
        assertEquals("Hi.txt", main.ReadHeaderFilename(HeaderPacket, HeaderPacket.length));
    }

    @Test
    public void GetsFileID()
    {
        assertEquals(24, (int)DataPacket[1]);
    }

    @Test
    public void DeterminesStatusByteCorrectly()
    {
        assertEquals(0, (int)HeaderPacket[0]);
        assertEquals(1, (int)DataPacket[0]);
        assertEquals(3, (int)LastDataPacket[0]);
    }

    @Test
    public void ReadsDataCorrectly()
    {

        assertEquals(10, (int)main.ReadDataPacket(DataPacket, DataPacket.length)[0]);
        assertEquals(14, (int)main.ReadDataPacket(DataPacket, DataPacket.length)[1]);
        assertEquals(9, (int)main.ReadDataPacket(DataPacket, DataPacket.length)[2]);
        assertEquals(5, (int)main.ReadDataPacket(DataPacket, DataPacket.length)[3]);

        assertEquals(8, (int)main.ReadDataPacket(LastDataPacket, LastDataPacket.length)[0]);
        assertEquals(15, (int)main.ReadDataPacket(LastDataPacket, LastDataPacket.length)[1]);

    }

    @Test
    public void MakePacketNumberCorrecyly()
    {
        assertEquals(4, main.MakePacketNumber(DataPacket[2], DataPacket[3]));
    }

    @Test
    public void ReadsVariablePacketSize()
    {
        assertEquals(2, main.ReadDataPacket(LastDataPacket, LastDataPacket.length).length);
    }

    @Test
    public void HashLogicWorksProperly()
    {
        //Shouldn't exist in the map, so it should add it to it, increasing the size.
        main.HashLogic(DataPacket[1], map);
        assertEquals(1, map.size());

        //Already exists, so it shouldn't add it to the hash map, keeping the size the same.
        main.HashLogic(DataPacket[1], map);
        assertEquals(1, map.size());

        //Shouldn't exist in the map, so it should add it to it, increasing the size.
        main.HashLogic(SecondDataPacket[1], map);
        assertEquals(2, map.size());
    }

}
