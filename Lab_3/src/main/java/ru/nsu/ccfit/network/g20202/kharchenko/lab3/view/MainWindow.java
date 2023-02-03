package ru.nsu.ccfit.network.g20202.kharchenko.lab3.view;

import ru.nsu.ccfit.network.g20202.kharchenko.lab3.model.response.Place;
import ru.nsu.ccfit.network.g20202.kharchenko.lab3.model.response.Sight;
import ru.nsu.ccfit.network.g20202.kharchenko.lab3.model.response.Weather;

import javax.swing.*;
import java.awt.*;
import java.util.List;

public class MainWindow {

    JFrame baseFrame;

    JPanel findingsPanel;
    JTextField searchBar;
    JButton searchButton;
    List<Place> placeList;
    DefaultListModel<String> placeListModel;
    JList<String> placeListView;

    JPanel infoPanel;
    JTextArea weatherArea;
    List<Sight> sightsList;
    DefaultListModel<String> sightsListModel;
    JList<String> sightsListView;


    JPanel descriptionPanel;
    JTextArea descArea;

    public MainWindow() {
        // Define window view
        baseFrame = new JFrame("Place Finder");
        baseFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        baseFrame.setBounds(250, 150, 1000, 500);
        baseFrame.setLayout(new GridLayout(1, 3, 10, 10));

        // Create findings frame
        findingsPanel = createFindingsPanel();
        findingsPanel.setVisible(true);
        baseFrame.add(findingsPanel);

        // Create info frame
        infoPanel = createInfoPanel();
        infoPanel.setVisible(true);
        baseFrame.add(infoPanel);

        // Create description frame
        descriptionPanel = createDescriptionPanel();
        descriptionPanel.setVisible(true);
        baseFrame.add(descriptionPanel);

        baseFrame.setVisible(true);
    }

    private JPanel createFindingsPanel() {
        JPanel panel = new JPanel(new BorderLayout());

        // Create input bar
        searchBar = new JTextField("Find place...");
        searchBar.setVisible(true);

        // Create button for input
        searchButton = new JButton("Find");
        searchButton.setVisible(true);

        // Create searchBar pane
        JPanel searchPane = new JPanel();
        searchPane.setLayout(new BoxLayout(searchPane, BoxLayout.LINE_AXIS));
        searchPane.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        searchPane.add(Box.createHorizontalGlue());
        searchPane.add(searchBar);
        searchPane.add(Box.createRigidArea(new Dimension(10, 0)));
        searchPane.add(searchButton);

        // Create JList
        placeListModel = new DefaultListModel<>();
        placeListView = new JList<>(placeListModel);
        placeListView.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        placeListView.setVisible(true);

        // Add elements
        panel.add(searchPane, BorderLayout.PAGE_START);
        panel.add(new JScrollPane(placeListView), BorderLayout.CENTER);

        return panel;
    }

    private JPanel createInfoPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));

        // Create weather label
        JLabel weatherLabel = new JLabel("Weather");
        weatherLabel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        weatherLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        // Create weather text area
        weatherArea = new JTextArea();
        weatherArea.setEditable(false);
        weatherArea.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        weatherArea.setAlignmentX(Component.LEFT_ALIGNMENT);
        weatherArea.setVisible(true);

        // Create sights label
        JLabel sightsLabel = new JLabel("Sights");
        sightsLabel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        sightsLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        // Create sights list
        sightsListModel = new DefaultListModel<>();
        sightsListView = new JList<>(sightsListModel);
        sightsListView.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        sightsListView.setAlignmentX(Component.LEFT_ALIGNMENT);
        sightsListView.setVisible(true);

        // Make list scrollable
        JScrollPane sightScroll = new JScrollPane(sightsListView);
        sightScroll.setAlignmentX(Component.LEFT_ALIGNMENT);

        panel.add(weatherLabel);
        panel.add(weatherArea);
        panel.add(sightsLabel);
        panel.add(sightScroll);

        return panel;
    }

    private JPanel createDescriptionPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));

        // Create description label
        JLabel descLabel = new JLabel("Description");
        descLabel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        descLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        // Create weather text area
        descArea = new JTextArea();
        descArea.setEditable(false);
        descArea.setLineWrap(true);
        descArea.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        descArea.setAlignmentX(Component.LEFT_ALIGNMENT);
        descArea.setVisible(true);

        panel.add(descLabel);
        panel.add(descArea);

        return panel;
    }

    public JButton getSearchComponent() {
        return searchButton;
    }

    public String getSearchInput() {
        return searchBar.getText();
    }

    public JList<String> getPlaceListView() {
        return placeListView;
    }

    public void displayPlaces(List<Place> list) {
        placeList = list;
        placeListModel.clear();
        for (Place el : list) {
            placeListModel.addElement(el.toString());
        }
    }

    public void displayWeather(Weather weather) {
        weatherArea.setText(weather.toString());
    }

    public void displaySights(List<Sight> list) {
        sightsList = list;
        sightsListModel.clear();
        for (Sight el : list) {
            sightsListModel.addElement(el.toString());
        }
    }

    public void displaySightDesc(String sightDesc) {
        descArea.setText(sightDesc);
    }

    public Place getPlace(int index) {
        return placeList.get(index);
    }

    public JList<String> getSightsListView() {
        return sightsListView;
    }

    public Sight getSight(int index) {
        return sightsList.get(index);
    }

}
