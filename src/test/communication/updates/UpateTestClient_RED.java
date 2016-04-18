package test.communication.updates;


public class UpateTestClient_RED {
    
    public static void main(String[] args) {
        UpdateClient client = new UpdateClient("localhost", 9000, "RED");
        client.run();
    }
}
