package org.micromanager.lightsheetmanager.model;

import org.micromanager.MultiStagePosition;
import org.micromanager.PositionList;
import org.micromanager.StagePosition;
import org.micromanager.lightsheetmanager.api.data.DISPIMDevice;
import org.micromanager.lightsheetmanager.model.devices.vendor.ASIXYStage;
import org.micromanager.lightsheetmanager.model.devices.vendor.ASIZStage;
import org.micromanager.lightsheetmanager.model.utils.GeometryUtils;
import org.micromanager.lightsheetmanager.model.utils.NumberUtils;

import java.util.Objects;

/**
 * Creates an XYZ grid and puts it in a Micro-Manager {@code PositionList}.
 * <p>
 * This adapts functionality from the Micro-Manager 1.4 ASI diSPIM plugin.
 */
public class XYZGrid {

    private boolean useX_;
    private boolean useY_;
    private boolean useZ_;

    private double startX_;
    private double startY_;
    private double startZ_;

    private double stopX_;
    private double stopY_;
    private double stopZ_;

    private double deltaX_;
    private double deltaY_;
    private double deltaZ_;

    private boolean clearYZ_;

    private LightSheetManagerModel model_;

    public XYZGrid(final LightSheetManagerModel model) {
        model_ = Objects.requireNonNull(model);
    }

    private int updateGridXCount() {
        double delta = deltaX_;
        final double range = startX_ - stopX_;
        if (Math.signum(range) != Math.signum(delta) &&
                !NumberUtils.floatsEqual(Math.abs(range), 0.0)) {
            delta *= -1;
            // gridXDeltaField_.setValue(delta); // TODO: set value
        }
        final int count = (int)Math.ceil(range/delta) + 1;
        // gridXCount_.setText(Integer.toString(count));
        return count;
    }

    private int updateGridYCount() {
        double delta = deltaY_;
        final double range = startY_ - stopY_;
        if (Math.signum(range) != Math.signum(delta) &&
                !NumberUtils.floatsEqual(Math.abs(range), 0.0)) {
            delta *= -1;
            // gridYDeltaField_.setValue(delta); // TODO: set value
        }
        final int count = (int)Math.ceil(range/delta) + 1;
        // gridYCount_.setText(Integer.toString(count));
        return count;
    }

    private int updateGridZCount() {
        double delta = deltaZ_;
        final double range = startX_ - stopX_;
        if (Math.signum(range) != Math.signum(delta) &&
                !NumberUtils.floatsEqual(Math.abs(range), 0.0)) {
            delta *= -1;
            // gridZDeltaField_.setValue(delta); // TODO: set value
        }
        final int count = (int)Math.ceil(range/delta) + 1;
        // gridZCount_.setText(Integer.toString(count));
        return count;
    }

    /**
     * Computes grid (position list as well as slices/spacing) based on current settings
     */
    public void computeGrid() {

        ASIXYStage xyStage = model_.devices().getDevice(DISPIMDevice.SAMPLE_XY);
        ASIZStage zStage = model_.devices().getDevice(DISPIMDevice.SAMPLE_Z);

        final int numX = useX_ ? updateGridXCount() : 1;
        final int numY = useY_ ? updateGridYCount() : 1;
        final int numZ = useZ_ ? updateGridZCount() : 1;

        // computer the center of each range
        double centerX = (startX_ + stopX_) / 2;
        double centerY = (startY_ + stopY_) / 2;
        double centerZ = (startZ_ + stopZ_) / 2;

        double startY = centerY - deltaY_ * (numY-1) / 2;
        double startZ = centerZ - deltaZ_ * (numZ-1) / 2;

        if (useX_) {
            // TODO: update GUI with values, aliases for asb and vsb?
            final double speedFactor = GeometryUtils.DISPIM.getStageGeometricSpeedFactor(true);
            model_.acquisitions().getAcquisitionSettingsBuilder().volumeSettingsBuilder().sliceStepSize(Math.abs(deltaX_)/speedFactor);
            model_.acquisitions().getAcquisitionSettingsBuilder().volumeSettingsBuilder().slicesPerVolume(numX);
            // move to X center if we aren't generating a position list with it
            if (!useY_ && !useZ_) {
                xyStage.setXYPosition(centerX, xyStage.getXYPosition().y); // TODO: make convenience method?
                xyStage.waitForDevice();
            }
        } else {
            // use current X value as center; this was original behavior
            centerX = xyStage.getXYPosition().x;
        }

        // if we aren't using one axis, use the current position instead of GUI position
        if (useY_ && !useZ_) {
            startZ = zStage.getPosition();
        }
        if (useZ_ && !useY_) {
            startY = xyStage.getXYPosition().y; // Note: only the Y coordinate
        }

        if (!useY_ && !useZ_ && !clearYZ_) {
            return; // early exit => YZ unused
        }

        // TODO: where to put prompt?
//        PositionList positionList = model_.studio().positions().getPositionList();
//        final boolean isPositionListEmpty = positionList.getNumberOfPositions() == 0;
//        if (!isPositionListEmpty) {
//            final boolean overwrite = false;
//            if (!overwrite) {
//                return; // early exit => nothing to do
//            }
//        }
        PositionList positionList = new PositionList();
        if (useY_ || useZ_) {
            for (int iz = 0; iz < numZ; ++iz) {
                for (int iy = 0; iy < numY; ++iy) {
                    MultiStagePosition msp = new MultiStagePosition();
                    if (useY_) {
                        msp.add(StagePosition.create2D(
                                xyStage.getDeviceName(),
                                centerX,
                                startY + iy * deltaY_));
                    }
                    if (useZ_) {
                        msp.add(StagePosition.create1D(
                                zStage.getDeviceName(),
                                startZ + iz * deltaZ_));
                    }
                    msp.setLabel("Pos_" + iz + "_" + iy);
                    positionList.addPosition(msp);
                }
            }
        }
        model_.studio().positions().setPositionList(positionList);
    }

}
