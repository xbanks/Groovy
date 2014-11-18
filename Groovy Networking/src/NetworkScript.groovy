def server = new Server()
//def server2 = new Server()
def client = new Client(server.getPort())

//new Thread(server2).start()
new Thread(server).start()
new Thread(client).start()
