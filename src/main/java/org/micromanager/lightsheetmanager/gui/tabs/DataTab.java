package org.micromanager.lightsheetmanager.gui.tabs;

import java.awt.*;
import java.io.File;
import java.util.EventObject;

import org.micromanager.internal.utils.FileDialogs;
import org.micromanager.lightsheetmanager.api.internal.DefaultAcquisitionSettingsDISPIM;
import org.micromanager.lightsheetmanager.gui.LightSheetManagerFrame;
import org.micromanager.lightsheetmanager.gui.components.*;
import org.micromanager.lightsheetmanager.gui.components.Button;
import org.micromanager.lightsheetmanager.gui.components.Label;
import org.micromanager.lightsheetmanager.gui.components.Panel;
import org.micromanager.lightsheetmanager.gui.components.TextField;
import org.micromanager.lightsheetmanager.model.DataStorage;
import org.micromanager.lightsheetmanager.model.LightSheetManagerModel;

import javax.swing.JLabel;
import java.util.Objects;

public class DataTab extends Panel {

    private TextField txtSaveDirectory_;
    private TextField txtSaveFileName_;

    private CheckBox cbxSaveWhileAcquiring_;
    private Button btnBrowse_;
    private RadioButton radSaveMode_;

    private final FileDialogs.FileType directorySelect_;

    private LightSheetManagerModel model_;
    private LightSheetManagerFrame frame_;

    public DataTab(final LightSheetManagerModel model,
                   final LightSheetManagerFrame frame) {
        model_ = Objects.requireNonNull(model);
        frame_ = Objects.requireNonNull(frame);

        // file type filter
        directorySelect_ = new FileDialogs.FileType(
                "SAVE_DIRECTORY",
                "All Directories",
                "",
                false,
                ""
        );

        createUserInterface();
        createEventHandlers();
    }

    private void createUserInterface() {
        final Label lblTitle = new Label("Data Settings", Font.BOLD, 18);


        final DefaultAcquisitionSettingsDISPIM acqSettings =
                model_.acquisitions().settings();

        final Panel pnlSave = new Panel("Image Save Settings");
        final Panel pnlDatastore = new Panel("Datastore Save Mode");

        final JLabel lblSaveDirectory = new JLabel("Save Directory:");
        final JLabel lblSaveFileName = new JLabel("Save File Name:");

        radSaveMode_ = new RadioButton(DataStorage.SaveMode.toArray(),
                acqSettings.saveMode().toString());

        txtSaveDirectory_ = new TextField();
        txtSaveDirectory_.setEditable(false);
        txtSaveDirectory_.setColumns(24);
        txtSaveDirectory_.setForeground(Color.BLACK);
        txtSaveDirectory_.setText(acqSettings.saveDirectory());

        txtSaveFileName_ = new TextField();
        //txtSaveFileName_.setEditable(false);
        txtSaveFileName_.setColumns(24);
        txtSaveFileName_.setForeground(Color.WHITE);
        txtSaveFileName_.setText(acqSettings.saveNamePrefix());

        cbxSaveWhileAcquiring_ = new CheckBox("Save images during acquisition",
                acqSettings.isSavingImagesDuringAcquisition());

        btnBrowse_ = new Button("Browse...", 80, 20);

        pnlSave.add(lblSaveDirectory, "");
        pnlSave.add(txtSaveDirectory_, "");
        pnlSave.add(btnBrowse_, "wrap");
        pnlSave.add(lblSaveFileName, "");
        pnlSave.add(txtSaveFileName_, "wrap");
        pnlSave.add(cbxSaveWhileAcquiring_, "span 2, wrap");

        pnlDatastore.add(radSaveMode_, "");

        add(lblTitle, "wrap");
        add(pnlSave, "wrap");
        add(pnlDatastore, "growx");
    }

    private void createEventHandlers() {

        btnBrowse_.registerListener((EventObject e) -> {
            //final String path = fileSelect_.openDialogBox(this, new File(""));
            final File result = FileDialogs.openDir(frame_,
                    "Please select the directory to save images to...",
                    directorySelect_
            );
            if (result != null) {
                model_.acquisitions().settingsBuilder().saveDirectory(result.toString());
                txtSaveDirectory_.setText(result.toString());
            }
            //System.out.println("getSaveDirectory: " + model_.acquisitions().getAcquisitionSettings().getSaveDirectory());
        });

        cbxSaveWhileAcquiring_.registerListener(e -> {
            model_.acquisitions().settingsBuilder().saveImagesDuringAcquisition(cbxSaveWhileAcquiring_.isSelected());
            //System.out.println("isSavingWhileAcquiring: " + model_.acquisitions().getAcquisitionSettings().isSavingWhileAcquiring());
        });

        txtSaveFileName_.registerListener(e -> {
            model_.acquisitions().settingsBuilder().saveNamePrefix(txtSaveFileName_.getText());
            //System.out.println("getSaveNamePrefix: " + model_.acquisitions().getAcquisitionSettings().getSaveNamePrefix());
        });

        radSaveMode_.registerListener(e -> {
            model_.acquisitions().settingsBuilder().saveMode(DataStorage.SaveMode.fromString(radSaveMode_.getSelectedButtonText()));
            System.out.println("radSaveMode: " + radSaveMode_.getSelectedButtonText());
        });
    }

    public String getSaveDir() {
        return txtSaveDirectory_.getText();
    }

    public String getSaveName() {
        return txtSaveFileName_.getText();
    }

}