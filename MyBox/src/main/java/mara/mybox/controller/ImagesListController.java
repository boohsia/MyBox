package mara.mybox.controller;

import java.io.File;
import java.util.List;
import java.util.Optional;
import javafx.beans.binding.Bindings;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.CheckBox;
import javafx.scene.control.TableView;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import mara.mybox.image.ImageInformation;
import mara.mybox.tools.FileTools;
import mara.mybox.value.AppVariables;
import static mara.mybox.value.AppVariables.logger;
import static mara.mybox.value.AppVariables.message;

/**
 * @Author Mara
 * @CreateDate 2018-11-28
 * @Description
 * @License Apache License Version 2.0
 */
public abstract class ImagesListController extends ImageViewerController {

    protected ObservableList<ImageInformation> tableData;
    protected TableView<ImageInformation> tableView;
    protected boolean needDurationColumn = false;

    @FXML
    protected ImagesTableController tableController;
    @FXML
    protected VBox optionsBox, tableBox;
    @FXML
    protected CheckBox viewCheck;

    @Override
    public void initControls() {
        try {
            super.initControls();
            if (tableController != null) {
                tableController.parentController = this;
                tableController.parentFxml = myFxml;

                tableData = tableController.tableData;
                tableView = tableController.tableView;

            }
        } catch (Exception e) {
            logger.debug(e.toString());
        }
    }

    @Override
    public void initializeNext() {
        try {
            initOptionsSection();
        } catch (Exception e) {
            logger.error(e.toString());
        }
    }

    public void initOptionsSection() {
        try {

            if (optionsBox != null) {
                optionsBox.setDisable(true);
            }
            tableBox.setDisable(true);

            saveButton.disableProperty().bind(Bindings.isEmpty(tableController.tableData)
            );

            saveAsButton.disableProperty().bind(
                    saveButton.disableProperty()
            );

        } catch (Exception e) {
            logger.error(e.toString());
        }
    }

    @FXML
    public void createAction(ActionEvent event) {
        try {
            if (!checkSaving()) {
                return;
            }
            tableController.tableData.clear();

            final File file = chooseSaveFile(AppVariables.getUserConfigPath(sourcePathKey),
                    null, sourceExtensionFilter, true);
            if (file == null) {
                return;
            }
            recordFileWritten(file);
            sourceFile = file;
            if (optionsBox != null) {
                optionsBox.setDisable(false);
            }
            tableBox.setDisable(false);
            getMyStage().setTitle(getBaseTitle() + "  " + sourceFile.getAbsolutePath());

        } catch (Exception e) {
            logger.error(e.toString());
        }

    }

    @Override
    public void sourceFileChanged(final File file) {
        try {
            if (!checkSaving()) {
                return;
            }
            tableController.tableData.clear();

            sourceFile = file;
            if (optionsBox != null) {
                optionsBox.setDisable(false);
            }
            tableBox.setDisable(false);
            getMyStage().setTitle(getBaseTitle() + "  " + sourceFile.getAbsolutePath());
            recordFileOpened(file);
            tableController.isOpenning = true;

            tableController.addFile(0, file);
        } catch (Exception e) {
            logger.error(e.toString());
        }
    }

    @Override
    public void dataChanged() {
        super.dataChanged();
        setImageChanged(true);
    }

    public boolean checkSaving() {
        if (imageChanged && !tableController.tableData.isEmpty()) {
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle(getMyStage().getTitle());
            alert.setContentText(AppVariables.message("ImageChanged"));
            alert.getDialogPane().setMinHeight(Region.USE_PREF_SIZE);
            ButtonType buttonSave = new ButtonType(AppVariables.message("Save"));
            ButtonType buttonNotSave = new ButtonType(AppVariables.message("NotSave"));
            ButtonType buttonCancel = new ButtonType(AppVariables.message("Cancel"));
            alert.getButtonTypes().setAll(buttonSave, buttonNotSave, buttonCancel);
            Stage stage = (Stage) alert.getDialogPane().getScene().getWindow();
            stage.setAlwaysOnTop(true);
            stage.toFront();

            Optional<ButtonType> result = alert.showAndWait();
            if (result.get() == buttonSave) {
                saveAction();
                return true;
            } else if (result.get() == buttonNotSave) {
                return true;
            } else {
                return false;
            }
        } else {
            return true;
        }
    }

    @Override
    public void updateLabelTitle() {
        try {
            if (getMyStage() == null) {
                return;
            }
            String title;
            if (sourceFile != null) {
                title = getBaseTitle() + " " + sourceFile.getAbsolutePath();
                if (imageInformation != null) {
                    if (imageInformation.isIsSampled()) {
                        title += " - " + message("Sampled");
                    }
                }
            } else {
                title = getBaseTitle();
            }
            if (imageChanged) {
                title += "  " + "*";
            }
            getMyStage().setTitle(title);

        } catch (Exception e) {
            logger.debug(e.toString());
        }
    }

    @FXML
    @Override
    public void saveAction() {
        if (sourceFile == null) {
            final File file = chooseSaveFile(AppVariables.getUserConfigPath(targetPathKey),
                    null, targetExtensionFilter, true);
            if (file == null) {
                return;
            }
            recordFileWritten(file);
            sourceFile = file;
        }
        saveFile(sourceFile);
    }

    @FXML
    @Override
    public void saveAsAction() {
        try {
//            if (!checkSaving()) {
//                return;
//            }

            String name = null;
            if (sourceFile != null) {
                name = FileTools.getFilePrefix(sourceFile.getName());
            }
            final File file = chooseSaveFile(AppVariables.getUserConfigPath(targetPathKey),
                    name, targetExtensionFilter, true);
            if (file == null) {
                return;
            }
            recordFileWritten(file);
            saveFile(file);
        } catch (Exception e) {
            logger.error(e.toString());
        }

    }

    public void saveFile(final File outFile) {
        if (outFile == null || tableController.tableData.isEmpty()) {
            return;
        }
        if (tableController.hasSampled()) {
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle(getMyStage().getTitle());
            alert.setContentText(AppVariables.message("SureSampled"));
            alert.getDialogPane().setMinHeight(Region.USE_PREF_SIZE);
            ButtonType buttonSure = new ButtonType(AppVariables.message("Sure"));
            ButtonType buttonCancel = new ButtonType(AppVariables.message("Cancel"));
            alert.getButtonTypes().setAll(buttonSure, buttonCancel);
            Stage stage = (Stage) alert.getDialogPane().getScene().getWindow();
            stage.setAlwaysOnTop(true);
            stage.toFront();

            Optional<ButtonType> result = alert.showAndWait();
            if (result.get() == buttonSure) {
                saveFileDo(outFile);
            }
        } else {
            saveFileDo(outFile);
        }
    }

    public void saveFileDo(final File outFile) {

    }

    public void loadFile(final File file, List<ImageInformation> infos) {
        try {
            sourceFile = file;
            if (optionsBox != null) {
                optionsBox.setDisable(false);
            }
            tableBox.setDisable(false);
            getMyStage().setTitle(getBaseTitle() + "  " + sourceFile.getAbsolutePath());

            isSettingValues = true;
            tableController.tableData.clear();
            tableController.tableData.addAll(infos);
            tableController.tableView.refresh();
            isSettingValues = false;
            tableController.tableChanged();
            tableController.tableSelected();
        } catch (Exception e) {
            logger.error(e.toString());
        }
    }

    public void loadFiles(List<File> files) {
        tableController.tableData.clear();
        tableController.addFiles(0, files);
    }

    @Override
    public ImagesListController refresh() {
        File oldfile = sourceFile;
        List<ImageInformation> oldInfo = tableData;

        ImagesListController c = (ImagesListController) refreshBase();
        if (c == null) {
            return null;
        }
        c.loadFile(oldfile, oldInfo);
        return c;
    }

}
