package org.micromanager.lightsheetmanager.gui.tabs;

import org.micromanager.lightsheetmanager.api.data.GeometryType;
import org.micromanager.lightsheetmanager.model.DeviceManager;

import org.micromanager.lightsheetmanager.gui.frames.SetupPathFrame;
import org.micromanager.lightsheetmanager.gui.components.Button;
import org.micromanager.lightsheetmanager.gui.components.Panel;

import javax.swing.JLabel;
import java.util.ArrayList;

/**
 *
 */
public class DeviceTab extends Panel {

    private Button btnOpenNavigationPanel_;

    private ArrayList<Button> setupPanelButtons_;
    private ArrayList<SetupPathFrame> setupPathFrames_;

    private DeviceManager devices_;

    public DeviceTab(final DeviceManager devices) {
        setupPanelButtons_ = new ArrayList<>();
        setupPathFrames_ = new ArrayList<>();
        devices_ = devices;
        setMigLayout(
            "",
            "[]0[]",
            "[]0[]"
        );
        init();
    }

    private void init() {

        final GeometryType geometryType = devices_.getDeviceAdapter().getMicroscopeGeometry();
        final int numImagingPaths = devices_.getDeviceAdapter().getNumImagingPaths();

        final JLabel lblGeometryType = new JLabel("Microscope Geometry: " + geometryType);
        final JLabel lblLightSheetType = new JLabel("Light Sheet Type: " + devices_.getDeviceAdapter().getLightSheetType());
        final JLabel lblNumImagingPaths = new JLabel("Imaging Paths: " + numImagingPaths);
        final JLabel lblNumIlluminationPaths = new JLabel("Illumination Paths: " + devices_.getDeviceAdapter().getNumIlluminationPaths());
        final JLabel lblNumSimultaneousCameras = new JLabel("Simultaneous Cameras: " + devices_.getDeviceAdapter().getNumSimultaneousCameras());
        //final JLabel lblDeviceAdapterVersion = new JLabel("Device Adapter Version: " + devices_.getVersionNumber());

        Button.setDefaultSize(150, 30);
        btnOpenNavigationPanel_ = new Button("Open Navigation Panel");

        // create buttons for the setup panels of each imaging path
        if (geometryType == GeometryType.DISPIM) {
            for (int i = 0; i < numImagingPaths; i++) {
                // create the button
                Button button = new Button("Setup Path " + (i+1), 150, 30);
                setupPanelButtons_.add(button);
                // create the frame it opens
                SetupPathFrame frame = new SetupPathFrame(i+1);
                setupPathFrames_.add(frame);
            }
        }

        createEventHandlers();

        add(lblGeometryType, "wrap");
        add(lblLightSheetType, "wrap");
        add(lblNumImagingPaths, "wrap");
        add(lblNumIlluminationPaths, "wrap");
        add(lblNumSimultaneousCameras, "wrap");
        //add(lblDeviceAdapterVersion, "");

        add(btnOpenNavigationPanel_, "wrap");
        for (Button button : setupPanelButtons_) {
            add(button, "wrap");
        }
    }

    private void createEventHandlers() {
        // open the navigation panel
        btnOpenNavigationPanel_.registerListener(e -> {

        });

        // create buttons to open the setup panel frames
        for (int i = 0; i < setupPanelButtons_.size(); i++) {
            final int index = i; // needs to be final for inner lambda expression
            Button button = setupPanelButtons_.get(index);
            button.registerListener(e -> {
                setupPathFrames_.get(index).setVisible(true);
            });
        }
    }
}
