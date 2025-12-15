package org.example.view.panels;

import org.example.model.domain.Track;

import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;
import java.awt.BorderLayout;
import java.util.List;

public class ResultPanel extends JPanel {

    private final DefaultTableModel tableModel;
    private final JTable table;

    public ResultPanel() {
        setLayout(new BorderLayout());
        tableModel = new DefaultTableModel(
                new Object[] { "Title", "Artist", "Album" }, 0
        );
        table = new JTable(tableModel);
        add(new JScrollPane(table), BorderLayout.CENTER);
    }

    // Old API – keep for compatibility, delegate to setTracks
    public void updateResults(Object payload) {
        if (payload instanceof List<?> list) {
            java.util.List<Track> tracks = new java.util.ArrayList<>();
            for (Object o : list) {
                if (o instanceof Track t) {
                    tracks.add(t);
                }
            }
            setTracks(tracks);
        } else {
            setTracks(java.util.List.of());
        }
    }

    // New API used by MainFrame
    public void setTracks(java.util.List<Track> tracks) {
        tableModel.setRowCount(0);
        if (tracks == null) return;

        for (Track t : tracks) {
            tableModel.addRow(new Object[] {
                    t.getName(),
                    t.getArtist(),
                    t.getAlbum()
            });
        }
    }
}


