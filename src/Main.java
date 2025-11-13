
/**
 * Read-Only File System Simulator
 * Main entry point for the application
 * Uses MVP pattern with Java Swing GUI
 */

import view.MainView;
import presenter.MainPresenter;
import model.FileSystem;

import javax.swing.SwingUtilities;
import javax.swing.UIManager;

public class Main {

    public static void main(String[] args) {
        // Set system look and feel
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            System.err.println("Warning: Could not set system look and feel");
        }

        // Initialize application on EDT (Event Dispatch Thread)
        SwingUtilities.invokeLater(() -> {
            try {
                // Create model instance
                FileSystem fileSystem = new FileSystem();

                // Create view instance
                MainView view = new MainView();

                // Create presenter to connect model and view
                MainPresenter presenter = new MainPresenter(view, fileSystem);
                presenter.run();

            } catch (Exception e) {
                System.err.println("Fatal error starting application: " + e.getMessage());
                e.printStackTrace();
                System.exit(1);
            }
        });
    }
}
