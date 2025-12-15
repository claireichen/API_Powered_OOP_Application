package org.example;

import org.example.controller.MainController;
import org.example.model.APIClient;
import org.example.model.AppModel;
import org.example.service.MusicServiceFactory;
import org.example.service.SessionPersistenceService;
import org.example.service.SpotifyService;
import org.example.service.SunoService;
import org.example.view.MainFrame;

import javax.swing.*;

public class Main {

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            APIClient apiClient = APIClient.getInstance();
            SpotifyService spotifyService = new SpotifyService(apiClient);
            SunoService sunoService = new SunoService(apiClient);
            MusicServiceFactory factory = new MusicServiceFactory(spotifyService, sunoService);
            AppModel model = new AppModel();
            SessionPersistenceService sessionService = new SessionPersistenceService(); // NEW
            MainController controller = new MainController(model, factory, sessionService); // UPDATED

            MainFrame frame = new MainFrame(controller);
            frame.setVisible(true);
        });
    }
}
