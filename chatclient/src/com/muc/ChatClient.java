package com.muc;

import org.apache.commons.lang3.StringUtils;

import java.io.*;
import java.net.Socket;
import java.util.ArrayList;

public class ChatClient {
    private final String serverName;
    private final int serverPort;
    private Socket socket;
    private OutputStream serverOut;
    private InputStream serverIn;
    private LineNumberReader bufferedIn;

    private ArrayList<UserStatusListener> userStatusListeners = new ArrayList<>();

    public ChatClient(String serverName, int serverPort) {
        this.serverName = serverName;
        this.serverPort = serverPort;
    }
    public static void Main (String[] args) throws IOException {
        ChatClient client = new ChatClient("localhost", 8818);
        client.addUserStatusListener(new UserStatusListener() {
            @Override
            public void online(String login) {
                System.out.println("ONLINE: " + login);
            }
            @Override
            public void offline(String login) {
                System.out.println("OFFLINE: " + login);
            }
        });
        client.addMessageListener(new MessageListener() {

            @Override
            public void onMessage(String fromLogin, String msgBody) {
                System.out.println("you got a message from " + fromLogin + "=========>" + msgBody);
            }

            @Override
            public void add(MessageListener messageListener) {
                
            }

            @Override
            public void remove(MessageListener messageListener) {

            }
        });


        if (!client.connect()) {
            System.err.println("Connection Failed");
        } else {
            System.out.println("Connection successful");


            if(client.login("guest", "temp")) {
                System.out.println("Login successful");
            } else {
                System.err.println("Login failed");
            }
        }
    }


    public boolean login(String username, String password) throws IOException {
        String cmd = "login " + username + " " + password;
        serverOut.write(cmd.getBytes());

        String response = bufferedIn.readLine();
        System.out.println("Response line: " + response);

        if ("ok login".equalsIgnoreCase(response)) {
            startMessageReader();
            return true;
        } else {
            return false;
        }

    }

    private void startMessageReader() {
        Thread t = new Thread() {
            public void run(){
                readMessageLoop();
            }
        };
        t.start();
    }

    private void readMessageLoop() {
        try {
            String line;
            while( (line = bufferedIn.readLine()) != null) {
                String[] tokens = StringUtils.split(line);
                if(tokens != null && tokens.length > 0) {
                    String cmd = tokens[0];
                    if("online".equalsIgnoreCase(cmd)) {
                        handleOnline(tokens);
                    } else if ("offline".equalsIgnoreCase(cmd)) {
                        handleOffline(tokens);
                    }
                }
            }

        } catch (Exception ex) {
            ex.printStackTrace();
            try {
                socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void handleOffline(String[] tokens) {
        String login = tokens[1];
        for(UserStatusListener listener: userStatusListeners) {
            listener.offline(login);
        }
    }

    private void handleOnline(String[] tokens) {
        String login = tokens[1];
        for(UserStatusListener listener: userStatusListeners) {
            listener.online(login);
        }
    }

    public boolean connect() {
        try {
            this.socket = new Socket(serverName,serverPort);
            System.out.println("Client port is " + socket.getLocalPort());
            this.serverOut = socket.getOutputStream();
            this.serverIn = socket.getInputStream();
            return true;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }

    public void addUserStatusListener(UserStatusListener listener) {
        userStatusListeners.add(listener);
    }
    public void removeUserStatusListener(UserStatusListener listener) {
        userStatusListeners.remove(listener);
    }


    public void addMessageListener(MessageListener messageListener) {
        messageListener.add(messageListener);
    }
    public void removeMessageListener(MessageListener messageListener) {
        messageListener.remove(messageListener);
    }

    public void message(String login, String text) {
        String cmd = "msg: " + login + " " + text;

    }
}
