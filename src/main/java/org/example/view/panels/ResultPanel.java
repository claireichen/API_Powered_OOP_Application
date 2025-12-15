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
        tableModel = new DefaultTableModel(new Object[]{"Title", "Artist", "Album"}, 0);
        table = new JTable(tableModel);

        add(new JScrollPane(table), BorderLayout.CENTER);
    }

    @SuppressWarnings("unchecked")
    public void updateResults(Object payload) {
        tableModel.setRowCount(0);

        if (payload instanceof List<?> list) {
            for (Object o : list) {
                if (o instanceof Track track) {
                    tableModel.addRow(new Object[]{
                            track.getName(),
                            track.getArtist(),
                            track.getAlbum()
                    });
                }
            }
        }
    }
}

