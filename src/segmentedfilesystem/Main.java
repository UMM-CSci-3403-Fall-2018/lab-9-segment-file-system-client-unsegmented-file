package segmentedfilesystem;

import java.io.IOException;
import java.net.*;
import java.util.ArrayList;
import java.util.HashMap;

public class Main
{
    private static DatagramSocket socket;
    private static InetAddress server;
    private static int port = 6014;

    public static PacketController File1 = new PacketController();
    public static PacketController File2 = new PacketController();
    public static PacketController File3 = new PacketController();

    public static HashMap<Integer, Integer> fileMap = new HashMap<Integer, Integer>();

    public static void main(String[] args) throws UnknownHostException, SocketException, IOException
    {

        server = InetAddress.getByName("heartofgold.morris.umn.edu");

        socket = new DatagramSocket();

        byte[][] storage = new byte[0][0];

        // 1_XX -> 00101010010100
        // [key]
        // 1 = File, so either 1, 2, or 3
        // XX = packet nunber

        byte[] buff = new byte[0];
        DatagramPacket helloPacket = new DatagramPacket(buff, buff.length, server, port);

        socket.send(helloPacket);

        byte[] bufferPacket = new byte[1024 + 4];
        DatagramPacket bigPacket = new DatagramPacket(bufferPacket, bufferPacket.length);

        //Need to keep requesting packets until we have all of them

        while(!File1.getIsFinished() && !File2.getIsFinished() && !File3.getIsFinished())
        {

            try
            {
                //Request a packet
                socket.receive(bigPacket);

                //If the file ID is not in the HashMap, add it
                HashLogic(bufferPacket[1]);

                //Determine what type of packet we get from the server
                switch(bufferPacket[0])
                {
                    //Packet is even, so it is a header packet
                    case 0:

                        // Header packet
                        // bufferPacket[0] = status bye
                        // bufferPacket[1] = file ID
                        // bufferPacket[2] = filename
                        // bufferPacket[3] = filename
                        // bufferPacket[4] = filename
                        // etc..

                        //Constructing the filenames for each respective file using a HashMap to look up which file ID corresponds to which file
                        switch(fileMap.get((int)bufferPacket[1]))
                        {
                            //Belongs to File1
                            case 1:

                                File1.setFileName(ReadHeaderFilename(bufferPacket, bigPacket.getLength()));
                                File1.setHasHeader(true);
                                break;

                            case 2:

                                File2.setFileName(ReadHeaderFilename(bufferPacket, bigPacket.getLength()));
                                File2.setHasHeader(true);
                                break;

                            case 3:

                                File3.setFileName(ReadHeaderFilename(bufferPacket, bigPacket.getLength()));
                                File3.setHasHeader(true);
                                break;
                        }

                        break;

                    //Packet is odd, so it is a data packet
                    case 1:

                        // Data packet
                        // bufferPacket[0] = status bye
                        // bufferPacket[1] = file ID
                        // bufferPacket[2] = packet number part 1
                        // bufferPacket[3] = packet number part 2
                        // bufferPacket[4] = data
                        // bufferPacket[5] = data
                        // bufferPacket[6] = data
                        // etc..

                        int PacketNumber = MakePacketNumber(bufferPacket[2], bufferPacket[3]);
                        System.out.println("Got a data packet " + fileMap.get((int)bufferPacket[1]));

                        switch(fileMap.get((int)bufferPacket[1]))
                        {
                            //Belongs to File1
                            case 1:
                                File1.AddElement(PacketNumber, ReadDataPacket(bufferPacket, bigPacket.getLength()));
                                break;

                            case 2:
                                File2.AddElement(PacketNumber, ReadDataPacket(bufferPacket, bigPacket.getLength()));
                                break;

                            case 3:
                                File3.AddElement(PacketNumber, ReadDataPacket(bufferPacket, bigPacket.getLength()));
                                break;
                        }

                        break;

                    //Packet is the last data packet for a given file
                    case 3:

                        // Final data packet
                        // bufferPacket[0] = status bye
                        // bufferPacket[1] = file ID
                        // bufferPacket[2] = packet number part 1
                        // bufferPacket[3] = packet number part 2
                        // bufferPacket[4] = data
                        // bufferPacket[5] = data
                        // bufferPacket[6] = data
                        // etc..

                        int LastPacketNumber = MakePacketNumber(bufferPacket[2], bufferPacket[3]);
                        System.out.println("Got final packet");

                        switch(fileMap.get((int)bufferPacket[1]))
                        {

                            //Belongs to File1
                            case 1:
                                File1.setMaxSize(LastPacketNumber);
                                File1.AddElement(LastPacketNumber, ReadDataPacket(bufferPacket, bigPacket.getLength()));
                                break;

                            case 2:
                                File2.setMaxSize(LastPacketNumber);
                                File2.AddElement(LastPacketNumber, ReadDataPacket(bufferPacket, bigPacket.getLength()));
                                break;

                            case 3:
                                File3.setMaxSize(LastPacketNumber);
                                File3.AddElement(LastPacketNumber, ReadDataPacket(bufferPacket, bigPacket.getLength()));
                                break;
                        }


                        break;
                }

            }
            catch(IOException e)
            {
                System.out.println("More" + e);
            }
        }

        System.out.println("Read to process files");

    }

    public static void HashLogic(byte bufferPacket)
    {
        //If the file ID is not in the HashMap, add it
        if(!fileMap.containsKey(bufferPacket))
        {
            //Add a new entry to the next spot in the HashMap
            fileMap.put((int)bufferPacket, fileMap.size());
        }
    }

    public static String ReadHeaderFilename(byte[] bufferPacket, int length)
    {
        String name = "";
        for(int i = 0; i < length - 2; i++)
        {
            byte charByte = bufferPacket[i + 2];
            name += (char)charByte;
        }

        System.out.println(name);
        return name;
    }

    public static byte[] ReadDataPacket(byte[] bufferPacket, int length)
    {
        byte[] data = new byte[length - 4];
        for(int i = 0; i < length - 4; i++)
        {
            byte dataByte = bufferPacket[i + 4];
            data[i] = dataByte;
        }

        return data;
    }

    public static int MakePacketNumber(byte LOB, byte HOB) {

        int iLOB = (int)LOB;
        int iHOB = (int)HOB;

        if (iLOB < 0)
        {
            iLOB = iLOB + 256;
        }

        if(iHOB < 0)
        {
            iHOB = iHOB + 256;
        }

        int result = 256 * iLOB + iHOB;

        //System.out.println(result + " packet");
        return result;
    }

}

