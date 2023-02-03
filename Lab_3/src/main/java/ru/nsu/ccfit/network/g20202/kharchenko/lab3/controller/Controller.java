package ru.nsu.ccfit.network.g20202.kharchenko.lab3.controller;

import ru.nsu.ccfit.network.g20202.kharchenko.lab3.model.PlaceFinder;
import ru.nsu.ccfit.network.g20202.kharchenko.lab3.model.response.Place;
import ru.nsu.ccfit.network.g20202.kharchenko.lab3.model.response.Sight;
import ru.nsu.ccfit.network.g20202.kharchenko.lab3.view.MainWindow;

import javax.swing.*;
import java.io.IOException;

public class Controller {
    public static void main(String[] args) throws IOException {
        Controller controller = new Controller();
        controller.run();
    }

    private void run() throws IOException {
        MainWindow mainWindow = new MainWindow();
        PlaceFinder placeFinder = new PlaceFinder();

        // Listen on search input to collect list of places
        mainWindow.getSearchComponent().addActionListener(e -> {
            placeFinder.requestPlaces(mainWindow.getSearchInput())
                    .thenAccept(places -> SwingUtilities.invokeLater(() ->
                            mainWindow.displayPlaces(places)));
        });

        // Listen on placeList input to collect info on the place
        mainWindow.getPlaceListView().addListSelectionListener(e -> {
            if (e.getValueIsAdjusting()) {
                JList<String> lsm = (JList<String>)e.getSource();
                Place selected = mainWindow.getPlace(lsm.getMinSelectionIndex());
                String lat = selected.getLat();
                String lng = selected.getLng();

                placeFinder.requestWeather(lat, lng)
                        .thenAccept(weather -> SwingUtilities.invokeLater(() -> {
                            mainWindow.displayWeather(weather);
                        }));

                placeFinder.requestSights(lat, lng)
                        .thenAccept(sights -> SwingUtilities.invokeLater(() -> {
                            mainWindow.displaySights(sights);
                        }));;
            }
        });

        // Listen on sights list to get its description
        mainWindow.getSightsListView().addListSelectionListener(e -> {
            if (e.getValueIsAdjusting()) {
                JList<String> lsm = (JList<String>)e.getSource();
                Sight selected = mainWindow.getSight(lsm.getMinSelectionIndex());
                String xid = selected.getXid();

                placeFinder.requestSightDescription(xid)
                        .thenAccept(sightDesc -> SwingUtilities.invokeLater(() -> {
                            mainWindow.displaySightDesc(sightDesc);
                        }));

            }
        });
    }
}
