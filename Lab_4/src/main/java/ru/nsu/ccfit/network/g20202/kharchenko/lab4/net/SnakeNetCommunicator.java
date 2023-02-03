package ru.nsu.ccfit.network.g20202.kharchenko.lab4.net;

import com.google.protobuf.InvalidProtocolBufferException;
import ru.nsu.ccfit.network.g20202.kharchenko.lab4.utils.*;

import java.io.IOException;
import java.net.*;
import java.util.HashMap;
import java.util.function.UnaryOperator;

public class SnakeNetCommunicator {

    // Communication parameters
    MulticastSocket publicSocket;
    InetAddress mcAddress;
    int mcPort;
    DatagramSocket personalSocket;
    int msg_seq = 1;

    // Multicast listening thread
    Thread multicastListener;

    // Assigned functions
    UnaryOperator<SnakeGameAnnouncement> announcementFunction = (e) -> e;
    UnaryOperator<SnakeJoinRequest> joinFunction = (e) -> e;
    UnaryOperator<SnakeGameState> stateFunction = (e) -> e;
    UnaryOperator<SnakeSteer> steerFunction = (e) -> e;

    // Master
    InetAddress masterAddress;
    int masterPort;
    Thread peerListener;

    public SnakeNetCommunicator(String multicastAddress, int multicastPort) throws IOException {
        // Join port
        publicSocket = new MulticastSocket(multicastPort);
        InetAddress address = InetAddress.getByName(multicastAddress);
        publicSocket.joinGroup(new InetSocketAddress(address, multicastPort), null);

        // Save multicast socket parameters
        this.mcAddress = address;
        this.mcPort = multicastPort;

        // Create socket for sending
        personalSocket = new DatagramSocket();

        // Create multicast listening thread
        multicastListener = new Thread(() -> {
            while (true)
                listenOnMulticast();
        });
        multicastListener.start();

        // Create peer listening thread
        peerListener = new Thread(() -> {
            while (true)
                listenPeers();
        });
        peerListener.start();
    }

    private void listenOnMulticast() {
        // Receive data
        byte[] buff = new byte[4096];
        DatagramPacket recvPacket = new DatagramPacket(buff, 4096);
        try {
            publicSocket.receive(recvPacket);

            // Collect data
            byte[] data = new byte[recvPacket.getLength()];
            System.arraycopy(recvPacket.getData(), 0, data, 0, data.length);

            SnakeProtoConverter.MessageType msgType = SnakeProtoConverter.readGameMessageType(data);

            switch (msgType) {
                case ANNOUNCEMENT:
                    SnakeGameAnnouncement gameAnnouncement = SnakeProtoConverter.readGameAnnouncement(data);
                    gameAnnouncement.setAddress(recvPacket.getAddress(), recvPacket.getPort());
                    announcementFunction.apply(gameAnnouncement);
                    break;
                case DISCOVER:
                    break;
                default:
                    System.out.println("Incorrect message type");
                    break;
            }
        } catch (InvalidProtocolBufferException e) {
            System.out.println("Failed to parse received message");
        } catch (IOException e) {
            System.out.println("Failed to receive message");
        }
    }

    private void listenPeers() {
        byte[] buff = new byte[4096];
        DatagramPacket recvPacket = new DatagramPacket(buff, 4096);
        try {
            personalSocket.receive(recvPacket);

            // Collect data
            byte[] data = new byte[recvPacket.getLength()];
            System.arraycopy(recvPacket.getData(), 0, data, 0, data.length);

            SnakeProtoConverter.MessageType msgType = SnakeProtoConverter.readGameMessageType(data);

            switch (msgType) {
                case JOIN:
                    SnakeJoinRequest joinRequest = SnakeProtoConverter.readJoinMsg(data);
                    joinRequest.senderAddress = recvPacket.getAddress();
                    joinRequest.senderPort = recvPacket.getPort();
                    joinFunction.apply(joinRequest);
                    break;
                case STATE:
                    SnakeGameState gameState = SnakeProtoConverter.readStateMsg(data);
                    stateFunction.apply(gameState);
                    break;
                case STEER:
                    SnakeSteer steer = SnakeProtoConverter.readSteerMsg(data);
                    steer.address = recvPacket.getAddress();
                    steer.port = recvPacket.getPort();
                    steerFunction.apply(steer);
                    break;
                default:
                    System.out.println("Incorrect peer-to-peer message type");
                    break;
            }
        } catch (InvalidProtocolBufferException e) {
            System.out.println("Failed to parse join message");
        } catch (IOException e) {
            System.out.println("Personal socket couldn't receive message");
        }
    }

    public void sendDiscoverMessage() {
        // Create proto message
        byte[] message = SnakeProtoConverter.writeDiscoverMessage(msg_seq++);

        // Create packet to send
        DatagramPacket packet = new DatagramPacket(message, message.length, mcAddress, mcPort);

        // Send
        try {
            personalSocket.send(packet);
        } catch (IOException e) {
            System.out.println("Couldn't send DiscoverMsg");
        }
    }

    public void sendAnnouncementMsg(SnakeGameAnnouncement gameAnnouncement) {
        // Create proto message
        byte[] message = SnakeProtoConverter.writeAnnouncementMessage(msg_seq++, gameAnnouncement);

        // Create packet to send
        DatagramPacket packet = new DatagramPacket(message, message.length, mcAddress, mcPort);

        // Send
        try {
            personalSocket.send(packet);
        } catch (IOException e) {
            System.out.println("Couldn't send AnnouncementMsg");
        }
    }

    public void onReceiveAnnouncement(UnaryOperator<SnakeGameAnnouncement> function) {
        announcementFunction = function;
    }

    public void joinGame(SnakeGameAnnouncement gameAnnouncement) {
        masterAddress = gameAnnouncement.address;
        masterPort = gameAnnouncement.port;
        sendJoinMsg(gameAnnouncement.name);
    }

    private void sendJoinMsg(String gameName) {
        // Create proto message
        byte[] message = SnakeProtoConverter.writeJoinMessage(msg_seq++, gameName);

        // Create packet to send
        DatagramPacket packet = new DatagramPacket(message, message.length, masterAddress, masterPort);

        // Send
        try {
            personalSocket.send(packet);
        } catch (IOException e) {
            System.out.println("Couldn't send JoinMsg");
        }
    }

    public void onJoinRequest(UnaryOperator<SnakeJoinRequest> function) {
        joinFunction = function;
    }

    public void destroyFunctions() {
        joinFunction = (e) -> e;
        stateFunction = (e) -> e;
    }

    public void sendGameState(SnakePlayer peer, SnakeGameState gameState, HashMap<Integer, SnakePlayer> peers) {
        // Create proto message
        byte[] message = SnakeProtoConverter.writeStateMessage(msg_seq++, gameState, peers);

        // Create packet to send
        DatagramPacket packet = new DatagramPacket(message, message.length, peer.address, peer.port);

        // Send
        try {
            personalSocket.send(packet);
        } catch (IOException e) {
            System.out.println("Couldn't send StateMsg");
        }
    }

    public void onStateGet(UnaryOperator<SnakeGameState> function) {
        stateFunction = function;
    }

    public void sendSteerMsg(SnakeDirection dir) {
        // Create proto message
        byte[] message = SnakeProtoConverter.writeSteerMsg(msg_seq++, dir);

        // Create packet to send
        DatagramPacket packet = new DatagramPacket(message, message.length, masterAddress, masterPort);

        // Send
        try {
            personalSocket.send(packet);
        } catch (IOException e) {
            System.out.println("Couldn't send SteerMsg");
        }
    }

    public void onSteerGet(UnaryOperator<SnakeSteer> function) {
        steerFunction = function;
    }
}
