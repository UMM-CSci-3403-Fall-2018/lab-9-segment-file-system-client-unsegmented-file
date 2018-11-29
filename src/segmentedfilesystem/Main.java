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

    public static ArrayList<Byte> bigStorage = new ArrayList<Byte>();

    public static PacketController File1 = new PacketController();
    public static PacketController File2 = new PacketController();
    public static PacketController File3 = new PacketController();

    public static void main(String[] args) throws UnknownHostException, SocketException, IOException
    {

        HashMap<Integer, Integer> fileMap = new HashMap<Integer, Integer>();

        server = InetAddress.getByName("heartofgold.morris.umn.edu");

        socket = new DatagramSocket();

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
                socket.receive(bigPacket);

                //System.out.println("Server not broken");
                System.out.println(bufferPacket[0]);
                System.out.println(bufferPacket[1]);
                //Determine what type of packet we get from the server
                switch(bufferPacket[0])
                {
                    //Packet is even, so it is a header packet
                    case 0:

                        //If the file ID is not in the HashMap, add it
                        if(!fileMap.containsKey(bufferPacket[1]))
                        {
                            //Add a new entry to the next spot in the HashMap
                            fileMap.put((int)bufferPacket[1], fileMap.size() + 1);
                        }

                        String name = "";

                        //Constructing the filenames for each respective file using a HashMap to look up which file ID corresponds to which file
                        switch(fileMap.get((int)bufferPacket[1]))
                        {
                            //Belongs to File1
                            case 1:

                                for(int i = 0; i < bigPacket.getLength() - 2; i++)
                                {
                                    byte charByte = bufferPacket[i + 2];
                                    name += (char)charByte;
                                }

                                System.out.println(name);

                                File1.setFileName(name);

                                break;

                            case 2:

                                for(int i = 0; i < bigPacket.getLength() - 2; i++)
                                {
                                    byte charByte = bufferPacket[i + 2];
                                    name += (char)charByte;
                                }

                                System.out.println(name);

                                File2.setFileName(name);

                                break;

                            case 3:

                                for(int i = 0; i < bigPacket.getLength() - 2; i++)
                                {
                                    byte charByte = bufferPacket[i + 2];
                                    name += (char)charByte;
                                }

                                System.out.println(name);

                                File3.setFileName(name);
                                break;
                        }

                        break;

                    //Packet is odd, so it is a data packet
                    case 1:


                        break;

                    //Packet is the last data packet for a given file
                    case 3:
                        break;
                }

            }
            catch(IOException e)
            {
                System.out.println("More" + e);
            }
        }

    }

}

