/**
 * Created by xavier on 11/17/14.
 */

class Client implements Runnable
{
    def ip
    def port
    def name

    public static void main(String... args)
    {
        Client client
        def ip, port
        if(args.length == 2)
        {
            port = Integer.parseInt(args[1])
            client = new Client(args[0], port)
        }
        else
            client = new Client()

        new Thread(client).start()
    }

    Client(ip, port, name) {
        this.ip = ip
        this.port = port
        this.name = name
    }

    Client(ip, port)
    {
        this(ip, port, "Client")
    }

    Client(port)
    {
        this(InetAddress.getLocalHost().getHostAddress(), port, "Client")
    }

    Client()
    {
        this(InetAddress.getLocalHost().getHostAddress(), 1025, "Client")
    }

    /**
     * Creates a new client and connects to the server
     */
    @Override
    void run() {

        try
        {
            def client_socket = new Socket(ip, port)
            def connection_string = "Connected to server\nIP: ${client_socket.inetAddress.address}\nPort: ${client_socket.port}"
            println connection_string

            def stdin = new BufferedReader(new InputStreamReader(System.in))
            HandleMessages(client_socket, stdin)
        }
        catch (IOException ioe)
        {
            if(ioe instanceof ConnectException)
                println "Connection Error: ${ioe.getMessage()}"
        }
    }

    /**
     * Handles all of the incoming and outgoing messages to and from the server
     * @param client_socket
     * @param stdin
     */
    void HandleMessages(client_socket, stdin)
    {
        def from_server =
                new BufferedReader(
                        new InputStreamReader(client_socket.getInputStream()))

        def to_server = new PrintWriter(client_socket.getOutputStream(), true)

        //  Sends messages to the server
        def outgoing = Thread.start {
            stdin.each {
                to_server.println(it)
            }
        }

        //  Continuously receives and prints messages from the server
        def incoming = Thread.start {
            from_server.each {
                println it
            }
        }
    }

}

