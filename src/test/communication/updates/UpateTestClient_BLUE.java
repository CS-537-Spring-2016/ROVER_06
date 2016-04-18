package test.communication.updates;


public class UpateTestClient_BLUE {
    
    public static void main(String[] args) {
        UpdateClient client = new UpdateClient("localhost", 9000, "BLUE");
        client.run();
    }
}
