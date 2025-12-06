package it.unibo.oop.reactivegui03;

import it.unibo.oop.JFrameUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serial;
import java.lang.reflect.InvocationTargetException;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

/**
 * Third experiment with reactive gui.
 */
public final class AnotherConcurrentGUI extends JFrame {

    @Serial
    private static final long serialVersionUID = 1L;
    private static final long TIME = 10_000;
    private static final Logger LOGGER = LoggerFactory.getLogger(AnotherConcurrentGUI.class);
    private final JLabel display = new JLabel();
    private final JButton up = new JButton("up");
    private final JButton down = new JButton("down");
    private final JButton stop = new JButton("stop");

    /**
     * Builds a new CGUI.
     */
    public AnotherConcurrentGUI() {
        super();
        JFrameUtil.dimensionJFrame(this);
        final JPanel panel = new JPanel();
        panel.add(display);
        panel.add(up);
        panel.add(down);
        panel.add(stop);
        this.getContentPane().add(panel);
        this.setVisible(true);
        /*
         * Create the counter agent and start it. This is actually not so good:
         * thread management should be left to
         * java.util.concurrent.ExecutorService
         */
        final Agent agent = new Agent();
        new Thread(agent).start();
        /*
         * Register a listener that stops it
         */
        up.addActionListener(e -> agent.setEnabled(true));
        down.addActionListener(e -> agent.setEnabled(false));
        stop.addActionListener(e -> agent.stopCounting());

        /*
         * New agent to handle the timer.
         */
        new Thread(() -> {
            try {
                Thread.sleep(TIME);
            } catch (final InterruptedException e) {
                LOGGER.error(e.getMessage(), e);
            }
            // Execute GUI updates on the Event Dispatch Thread
            SwingUtilities.invokeLater(() -> {
                agent.stopCounting();
                up.setEnabled(false);
                down.setEnabled(false);
                stop.setEnabled(false);
            });
        }).start();
    }

    /*
     * The counter agent is implemented as a nested class. This makes it
     * invisible outside and encapsulated.
     */
    private final class Agent implements Runnable {
        /*
         * Stop is volatile to ensure visibility. Look at:
         *
         * http://archive.is/9PU5N - Sections 17.3 and 17.4
         *
         * For more details on how to use volatile:
         *
         * http://archive.is/4lsKW
         *
         */
        private volatile boolean stop;
        private volatile boolean countDown = true;
        private int counter;

        @Override
        public void run() {
            while (!this.stop) {
                try {
                    final var nextText = Integer.toString(this.counter);
                    SwingUtilities.invokeAndWait(() -> AnotherConcurrentGUI.this.display.setText(nextText));
                    if (countDown) {
                        this.counter++;
                    } else {
                        this.counter--;
                    }
                    Thread.sleep(100);
                } catch (InvocationTargetException | InterruptedException ex) {
                    LOGGER.error(ex.getMessage(), ex);
                }
            }
        }

        /**
         * External command to stop counting.
         */
        public void stopCounting() {
            this.stop = true;
        }

        /**
         * Set Enabled boolean.
         * 
         * @param input true/false
         */
        public void setEnabled(final boolean input) {
            this.countDown = input;
        }
    }
}
