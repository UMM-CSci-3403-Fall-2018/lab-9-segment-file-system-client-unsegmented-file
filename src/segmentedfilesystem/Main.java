package segmentedfilesystem;

import java.io.IOException;
import java.net.*;
import java.util.ArrayList;

public class Main
{
    private static DatagramSocket socket;
    private static InetAddress server;
    private static int port = 6014;

    private byte[] small;
    private byte[] AsYouLikeIt;
    private byte[] binary;

    public static ArrayList<Byte> bigStorage = new ArrayList<Byte>();

    public static PacketController smallController = new PacketController();
    public static PacketController AsYouLikeItController = new PacketController();
    public static PacketController binaryController = new PacketController();

    public static void main(String[] args)
    {
        //
        try
        {
            server = InetAddress.getByName("heartofgold.morris.umn.edu");
        }
        catch(UnknownHostException e)
        {
            System.out.println(e);
        }

        //
        try
        {
            socket = new DatagramSocket();
        }
        catch(SocketException e)
        {
            System.out.println(e);
        }


        byte[] buff = new byte[0];
        DatagramPacket helloPacket = new DatagramPacket(buff, buff.length, server, port);

        try
        {
            socket.send(helloPacket);
        }
        catch(IOException e)
        {
            System.out.println(e);
        }


        byte[] bufferPacket = new byte[1024 + 4];
        DatagramPacket bigPacket = new DatagramPacket(bufferPacket, bufferPacket.length);

        //Need to keep requesting packets until we have all of them
        while(!smallController.isFinished && !AsYouLikeItController.isFinished && !binaryController.isFinished)
        {
            try
            {
                socket.receive(bigPacket);
            }
            catch(IOException e)
            {
                System.out.println(e);
            }
        }


    }

}
