package billboard.viewer;

/**
 * Main class for running the billboard viewer
 */
public class Main {
    /**
     * Runs the billboard viewer, updating every 15 seconds
     * @param args - Main args
     */
    public static void main(String[] args) {
        Viewer viewer = new Viewer();

        while (viewer.open) {
            viewer.UpdateBillboard();

            final int secondsGap = 15;
            try {
                Thread.sleep(1000 * secondsGap);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}
