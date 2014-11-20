import groovy.util.logging.Log

/**
 * Created by xavier on 11/17/14.
 * Creates a new Server for Clients to connect to.
 * Can be run on its own or called and started by an outside method
 */

@Log
class Server implements Runnable
{
    private ServerSocket server_socket
    private port
    private ip
    private static ports_in_use = []
    private num_clients = 0
    private LinkedList<String> messageList
    private client_list = []
    private static Reader stdin

    public static void main(String... args)
    {
        def server
        def port = (args.length == 1) ? args[0] as Integer : 1024

        while(port < 1024 || !available(port)) {
            println "in"
            port++
        }

        stdin = (System?.console()?.reader()) ?: (new BufferedReader(new InputStreamReader(System.in)))

        print "Cool with using port: $port? (y/n)"
        def choice = stdin.readLine()
        (choice.toLowerCase() == "y") ? server = new Server(port) : System.exit(0)

        def ServerThread = new Thread(server).start()
    }


    private static boolean available(int port)
    {
        try
        {
            ServerSocket testSocket = new ServerSocket(port)
            testSocket.close()
            return true
        }
        catch (IOException ioe)
        {
            log.info(ioe.getMessage() + "\nunavailabe port: $port")
            return false
        }
    }

    Server(port) {
        this.port = port
        this.ip = InetAddress.getLocalHost().getHostAddress()
        messageList = new LinkedList<String>()
    }

    Server()
    {
        this(1025)
    }

    public getPort() {
        return port
    }

    /**
     * This starts up the server and begins accepting clients
     * Also starts the HandleClosures and HandleMessages threads
     */
    @Override
    void run()
    {
        try
        {
            server_socket = new ServerSocket(port)
            ports_in_use.add(port)

            printf "[%s]\n", "Server up".center(22)
            printf "[%s]\n","IP: $ip".center(22)
            printf "[%s]\n","Port: $port".center(22)
            printf "[%s]\n","exit() to close server".center(22)
            printf "[%s]\n","===Messages==="

            HandleClosures()

            while(!server_socket.isClosed())
            {
                def client = server_socket.accept()
                client_list.add(client)
                num_clients++
                HandleMessages(client, num_clients)
                println "Num Clients: $num_clients"
            }
        }
        catch (IOException ioe)
        {
            ioe.printStackTrace()
        }
    }

    /**
     * Handles all of the ingoing and outgoing messages
     * Gets input messages from a single client, then sends that message to every other client on the server
     * Also logs all messages in the server output
     *
     * @param client
     * @param client_num
     */
    private HandleMessages(Socket client, client_num)
    {
        def th = Thread.start {
            def from_client = new BufferedReader( new InputStreamReader(client.getInputStream()))

//            from_client.eachLine {client_list.each {new PrintWriter(it.getOutputStream(), true).println("Other $it")}}
            from_client.eachLine {

                def message = it
                println "Client $client_num: $it"

                client_list.each {
                    if(it != client)
                    {
                        PrintWriter to_client = new PrintWriter(it.getOutputStream(), true)
                        to_client.println "Other client: $message"
                    }

                }
            }
        }

        th.interrupt()
    }

    /**
     * This removes all of the clients that leave
     * in order to make sure that no messages are being sent to closed sockets
     */
    private HandleClosures()
    {
        def th1 = Thread.start {
            while(!server_socket.isClosed()) {
                stdin.eachLine
                        {
                            if (it.toLowerCase() == "exit()") {
                                println "Closing server"
                                System.exit(0)
                            }
                        }
            }
        }

        def th = Thread.start {
            while (!server_socket.isClosed()) {
                for (int i = 0; i < client_list.size(); i++) {
                    if (client_list.get(i).isClosed()) {
                        client_list.remove(i)
                        i--;
                        println "Client removed"
                    }
                }
            }
        }

        th1.interrupt()
        th.interrupt()
    }

}
