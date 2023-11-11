///////////////////////////////////////////////////////////////////////
//	Name:	LoginServer
//	Desc:	The login server
//	Date:	5/8/2003 - Gabe Jones
//	TODO:
///////////////////////////////////////////////////////////////////////
package leo.server;

// imports

import leo.shared.Constants;
import leo.shared.Log;
import leo.shared.network.SocketProvider;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;


public class LoginServer implements Runnable {

    /////////////////////////////////////////////////////////////////
    // Properties
    /////////////////////////////////////////////////////////////////
    private final int port;
    private final Thread runner;
    private final boolean useTls;
    private ServerSocket serverSocket;

    private final Server server;

    private boolean ready = false;

    /////////////////////////////////////////////////////////////////
    // Constructor
    /////////////////////////////////////////////////////////////////
    public LoginServer(Server server, int newPort, boolean useTls) {
        this.server = server;
        this.port = newPort;
        this.useTls = useTls;
        runner = new Thread(this, "LoginServerThread");
        runner.start();
    }

    public boolean isReady() {
        return ready;
    }

    /////////////////////////////////////////////////////////////////
    // The main loop
    /////////////////////////////////////////////////////////////////
    public void run() {
        // Create the server socket
        try {
            serverSocket = SocketProvider.newServerSocket(port, useTls);
        } catch (Exception e) {
            Log.error("Unable to start the login server. Exiting.");
            Log.error(e.toString());
            System.exit(0);
        }

        Log.system("Starting login server at " + serverSocket.getLocalSocketAddress());

        // Socket is ready by now, client shouldn't bounce off anymore.
        ready = true;

        // Loop indefinitely, accepting socket connections
        while (true) {
            try {
                Socket socket = serverSocket.accept();
                Log.system("Connection received at: " + socket.getInetAddress());
                User user = new User(server, socket);
                Thread.sleep(10);
            } catch (Exception e) {
            }
        }
    }
}
