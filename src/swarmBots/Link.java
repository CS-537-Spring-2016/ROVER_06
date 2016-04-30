package swarmBots;

class Link {

    String ip;
    int port;
    String group;

    public Link(String group, String ip, int port) {
        this.group = group;
        this.ip = ip;
        this.port = port;
    }
}
