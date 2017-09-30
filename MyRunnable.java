public class MyRunnable implements Runnable {
    @Override
    public void run() {
        while( true ) {
            try {
                System.out.println("I am object " + this.toString());
                Thread.sleep((long) (Math.random() * 1000));
            }catch(InterruptedException ex )
            {
                break;
            }
        }
    }

    public static void main(String[] args) throws InterruptedException {
        Thread firstThread = new Thread( new MyRunnable());
        Thread secondThread = new Thread( new MyRunnable());

        firstThread.start();
        secondThread.start();
    }
}


