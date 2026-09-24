import javax.swing.SwingUtilities;
import login.LoginFrame;

/**
 * Main
 * -----
 * This is the file you actually run. It just opens the voter login screen;
 * everything else (registration, admin, voting, results) is reached by
 * clicking buttons from there.
 */
public class Main {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            new LoginFrame().setVisible(true);
        });
    }
}
