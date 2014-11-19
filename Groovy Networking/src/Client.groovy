/**
 * Created by xavier on 11/17/14.
 */

class Client implements Runnable
{
    private Socket client_socket
    def ip
    def port
    def name
    private static Reader stdin
    public static void main(String... args)
    {
        Client client
        def ip, port
        if(args.length == 2)
        {
            ip = args[0]
            port = Integer.parseInt(args[1])
        }
        else
        {
            stdin = (System?.console()?.reader()) ?: (new BufferedReader(new InputStreamReader(System.in)))
            print 'IP: '
            ip = stdin.readLine()
            print 'Port: '
            port = Integer.parseInt(stdin.readLine())

        }


        client = new Client(ip, port)
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
            client_socket = new Socket(ip, port)
            def connection_string = "Connected to server\nIP: ${client_socket.inetAddress.address}\nPort: ${client_socket.port}"

            println connection_string
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

        outgoing.interrupt()
        incoming.interrupt()
    }

}

