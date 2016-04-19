package test.com.updates;

/*
 * display current time in millecond since 1970
 */
public class TimeCounterThread  implements Runnable{

    private long sleepTime = 1000L;
    
    @Override
    public void run() {
        while(true) {
            System.out.println(System.currentTimeMillis());
            try {
                Thread.sleep(sleepTime);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        
    }

}
