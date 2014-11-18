/**
 * Created by xavier on 11/17/14.
 */
class Server implements Runnable
{
    private port
    private ip
    private static ports_in_use = []
    private num_clients = 0
    private LinkedList<String> messageList
    private client_list = []

    public static void main(String... args)
    {
        def server
        def port = (args.length == 1) ? args[0] as Integer : 1024

        while(port < 1024 /*|| available(port)*/) {
            println "in"
            port++
        }


        def choice = System.console().readLine "Cool with using $port? (y/n)"
        (choice.toLowerCase() == "y") ? server = new Server(port) : System.exit(0)

        new Thread(server).start()
    }

    private static boolean available(int port)
    {
        try
        {
            new ServerSocket(port)
            return true
        }
        catch (IOException ioe)
        {
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

    def getPort() {
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
            def server_socket = new ServerSocket(port)
            ports_in_use.add(port)

            println "[Server up]"
            println "IP: $ip"
            println "Port: $port"
            println "===Messages==="

            HandleClosures()

            while(true)
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
    void HandleMessages(Socket client, client_num)
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
                        def to_client = new PrintWriter(it.getOutputStream(), true)
                        to_client.println "Other client: $message"
                    }

                }
            }
        }
    }

    /**
     * This removes all of the clients that leave
     * in order to make sure that no messages are being sent to closed sockets
     */
    void HandleClosures()
    {
        def stdin = new BufferedReader(new InputStreamReader(System.in))

        def th1 = Thread.start {
            while(true) {
                stdin.readLine().eachLine
                        {
                            if (it.toLowerCase() == "exit()") {
                                println "Closing server"
                                System.exit(0)
                            }
                        }
            }
        }

        def th = Thread.start {
            while (true) {
                for (int i = 0; i < client_list.size(); i++) {
                    if (client_list.get(i).isClosed()) {
                        client_list.remove(i)
                        i--;
                        println "Client removed"
                    }
                }
            }
        }
    }

}
