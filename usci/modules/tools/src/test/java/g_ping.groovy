/**
 * Created by emles on 04.10.17
 */

if (true) {

    String host = "10.8.2.101"
    Integer port = 1521
    Integer timeout = 5 /* seconds */ * 1000

    Socket socket

    try {
        socket = new Socket()
        socket.connect(new InetSocketAddress(host, port), timeout)
        println "is connected: ${socket.connected}"
    } catch (Exception e) {
        println "is connected: ${socket.connected}"
        println "error message is: ${e.getMessage()}"
    }

    try {
        if (socket) socket.close();
    } catch (e) {
    }

}



