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

    //Create objects to keep track of each file
    public static PacketController File1 = new PacketController();
    public static PacketController File2 = new PacketController();
    public static PacketController File3 = new PacketController();

    //Create has map to map a generic file to a File1, File2, or File3
    public static HashMap<Integer, Integer> fileMap = new HashMap<Integer, Integer>();

    public static void main(String[] args) throws UnknownHostException, SocketException, IOException
    {

        //Begin to 'connect' to the server by sending a UDP packet
        server = InetAddress.getByName("heartofgold.morris.umn.edu");
        socket = new DatagramSocket();

        //Actually send the hello message to initiate packet sending
        byte[] buff = new byte[0];
        DatagramPacket helloPacket = new DatagramPacket(buff, buff.length, server, port);
        socket.send(helloPacket);

        //Create a packet that can contain the max amount of bytes in a packet
        byte[] bufferPacket = new byte[1024 + 4];
        DatagramPacket bigPacket = new DatagramPacket(bufferPacket, bufferPacket.length);

        //Need to keep requesting packets until we have all of them
        System.out.println("Start getting packets from server...");
        while(!File1.getIsFinished() || !File2.getIsFinished() || !File3.getIsFinished())
        {
            try
            {
                //Request a packet
                socket.receive(bigPacket);

                //If the file ID is not in the HashMap, add it
                HashLogic(bufferPacket[1], fileMap);

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

                                //Set the filename and mark the file as having received the header packet;
                                File1.setFileName(ReadHeaderFilename(bufferPacket, bigPacket.getLength()));
                                File1.setHasHeader(true);
                                break;

                            //Duplicate logic to case 1, but for File2
                            case 2:

                                File2.setFileName(ReadHeaderFilename(bufferPacket, bigPacket.getLength()));
                                File2.setHasHeader(true);
                                break;

                            //Duplicate logic to case 1, but for File3
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

                        //Create a packet number from the two bytes
                        int PacketNumber = MakePacketNumber(bufferPacket[2], bufferPacket[3]);

                        //We need to determine which file this data packet belongs to. So we look into the hash mash.
                        switch(fileMap.get((int)bufferPacket[1]))
                        {
                            //Belongs to File1
                            case 1:
                                //Add that data packet into that file's storage at the correct position
                                File1.AddElement(PacketNumber, ReadDataPacket(bufferPacket, bigPacket.getLength()));
                                break;

                            //Duplicate logic but for File2
                            case 2:
                                File2.AddElement(PacketNumber, ReadDataPacket(bufferPacket, bigPacket.getLength()));
                                break;

                            //Duplicate logic but for File3
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

                        //Construct packet number
                        int LastPacketNumber = MakePacketNumber(bufferPacket[2], bufferPacket[3]);

                        //This is the last data packet for a file, and we need to determine which file
                        switch(fileMap.get((int)bufferPacket[1]))
                        {

                            //Belongs to File1
                            case 1:
                                System.out.println("Got File1 last packet");

                                //Set the max size to be equal to the packet number we found plus one (we treated the first packet as packet 1, not 0, oops)
                                File1.setMaxSize(LastPacketNumber + 1);

                                //Add that data packet into that file's storage at the correct position
                                File1.AddElement(LastPacketNumber, ReadDataPacket(bufferPacket, bigPacket.getLength()));
                                break;

                            case 2:
                                System.out.println("Got File2 last packet");
                                File2.setMaxSize(LastPacketNumber + 1);
                                File2.AddElement(LastPacketNumber, ReadDataPacket(bufferPacket, bigPacket.getLength()));
                                break;

                            case 3:
                                System.out.println("Got File3 last packet");
                                File3.setMaxSize(LastPacketNumber + 1);
                                File3.AddElement(LastPacketNumber, ReadDataPacket(bufferPacket, bigPacket.getLength()));
                                break;
                        }


                        break;
                }

                //Debugging, having packet loss has occurred, so if the program hangs, try enabling this to see what happened.
                //System.out.println(File1.getIsFinished() + " " + File2.getIsFinished() + " " + File3.getIsFinished());

            }
            catch(IOException e)
            {
                System.out.println("More" + e);
            }
        }

        //Tell the controller to build the files
        System.out.println("Read to process files");
        File1.BuildFile();
        File2.BuildFile();
        File3.BuildFile();

    }

    public static void HashLogic(byte bufferPacket, HashMap fileMap)
    {
        //If the file ID is not in the HashMap, add it
        if(!fileMap.containsKey((int)bufferPacket))
        {
            //Add a new entry to the next spot in the HashMap
            fileMap.put((int)bufferPacket, fileMap.size() + 1);
        }
    }

    public static String ReadHeaderFilename(byte[] bufferPacket, int length)
    {
        //Get the name from the header packet.
        String name = "";

        //We do minus 2 here since we constructed a generic packet of size 1024 + 4 to handle both a header, and data packet. Since the header
        //has two less bytes, we just need to ignore them.
        for(int i = 0; i < length - 2; i++)
        {
            //Offset the position so we can read the data
            byte charByte = bufferPacket[i + 2];
            name += (char)charByte;
        }

        System.out.println(name);
        return name;
    }

    public static byte[] ReadDataPacket(byte[] bufferPacket, int length)
    {
        //Get the data from the data packet

        //We do minus 4 here since the first four bytes are all info, and the data is after
        byte[] data = new byte[length - 4];

        //Read all of the data in the packet, offsetting by 4
        for(int i = 0; i < length - 4; i++)
        {
            //Offset by 4 to get the data
            byte dataByte = bufferPacket[i + 4];
            data[i] = dataByte;
        }

        return data;
    }

    public static int MakePacketNumber(byte LOB, byte HOB) {

        //Cast the byte as an int
        int iLOB = (int)LOB;
        int iHOB = (int)HOB;

        //If the bytes are negative, add 256 to them since java doesn't support unsigned ints
        if (iLOB < 0)
        {
            iLOB = iLOB + 256;
        }

        if(iHOB < 0)
        {
            iHOB = iHOB + 256;
        }

        //Do math to convert to proper packet number
        int result = 256 * iLOB + iHOB;

        //System.out.println(result + " packet");
        return result;
    }

}

