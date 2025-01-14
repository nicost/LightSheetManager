package org.micromanager.lightsheetmanager.model;

import mmcorej.CMMCore;
import org.micromanager.LogManager;
import org.micromanager.Studio;
import org.micromanager.lightsheetmanager.api.AutofocusSettings;
import org.micromanager.lightsheetmanager.api.LightSheetManager;
import org.micromanager.lightsheetmanager.api.TimingSettings;
import org.micromanager.lightsheetmanager.api.VolumeSettings;
import org.micromanager.lightsheetmanager.api.data.GeometryType;
import org.micromanager.lightsheetmanager.model.acquisitions.AcquisitionEngine;
import org.micromanager.lightsheetmanager.model.acquisitions.AcquisitionEngineDISPIM;
import org.micromanager.lightsheetmanager.model.acquisitions.AcquisitionEngineSCAPE;
import org.micromanager.lightsheetmanager.model.playlist.AcquisitionTableData;

import java.util.Objects;

/**
 * This is the container for all the data needed to operate a microscope with light sheet manager.
 */
public class LightSheetManagerModel implements LightSheetManager {

    private final Studio studio_;
    private final CMMCore core_;
    private final LogManager logs_;

    private String errorText_;

    private UserSettings settings_;

    private DeviceManager deviceManager_;

    private XYZGrid xyzGrid_;

    private AcquisitionEngine acqEngine_;
    private AcquisitionTableData acqTableData_;

    public LightSheetManagerModel(final Studio studio) {
        studio_ = Objects.requireNonNull(studio);
        core_ = studio_.core();
        logs_ = studio_.logs();

        settings_ = new UserSettings(this);
        xyzGrid_ = new XYZGrid(this);

        // set during setup if there is an error
        // displayed in the error ui
        errorText_ = "";
    }

    /**
     * Returns true when the model is loaded correctly.
     *
     * @return true if the model loads with no errors
     */
    public boolean setup() {

        deviceManager_ = new DeviceManager(studio_, this);

        // first we check to see if the device adapter is present
        if (!deviceManager_.hasDeviceAdapter()) {
            return false;
        }

        // setup devices
        deviceManager_.setup();

        // create different acq engine based on microscope geometry
        final GeometryType geometryType = deviceManager_
                .getDeviceAdapter().getMicroscopeGeometry();
        switch (geometryType) {
            case SCAPE:
                acqEngine_ = new AcquisitionEngineSCAPE(this);
                break;
            case DISPIM:
                acqEngine_ = new AcquisitionEngineDISPIM(this);
                break;
            default:
                studio_.logs().logError(
                        "setup error, AcquisitionEngine not implemented for " + geometryType);
                return false; // early exit => error
        }

        // load settings
        settings_.load();

        // if we made it here then everything loaded correctly
        return true;
    }

    /**
     * This sets the text to be displayed in the error ui when an error occurs during setup.
     *
     * @param text the error message
     */
    public void setErrorText(final String text) {
        errorText_ = text;
    }

    public String getErrorText() {
        return errorText_;
    }

    public AcquisitionEngine getAcquisitionEngine() {
        return acqEngine_;
    }

    public AcquisitionEngine acquisitions() {
        return acqEngine_;
    }

    public DeviceManager getDeviceManager() {
        return deviceManager_;
    }

    public DeviceManager devices() {
        return deviceManager_;
    }

    public UserSettings getUserSettings() {
        return settings_;
    }

    public CMMCore getCore() {
        return core_;
    }

    public CMMCore core() {
        return core_;
    }

    public Studio getStudio() {
        return studio_;
    }

    public Studio studio() {
        return studio_;
    }

    public XYZGrid getXYZGrid() {
        return xyzGrid_;
    }
    
    @Override
    public AutofocusSettings.Builder autofocusSettingsBuilder() {
        return null;
    }

    @Override
    public VolumeSettings.Builder volumeSettingsBuilder() {
        return null;
    }

    @Override
    public TimingSettings.Builder timingSettingsBuilder() {
        return null;
    }
}
