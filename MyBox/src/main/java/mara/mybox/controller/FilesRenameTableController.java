package mara.mybox.controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.cell.PropertyValueFactory;
import mara.mybox.data.FileInformation;
import mara.mybox.fxml.FxmlControl;
import mara.mybox.value.AppVariables;
import static mara.mybox.value.AppVariables.logger;

/**
 * @Author Mara
 * @CreateDate 2018-7-4
 * @Description
 * @License Apache License Version 2.0
 */
public class FilesRenameTableController extends FilesTableController {

    @FXML
    protected TableColumn<FileInformation, String> newColumn;
    @FXML
    protected Button recoveryAllButton, recoverySelectedButton, yesButton;
    @FXML
    protected Label commentsLabel;

    public FilesRenameTableController() {
    }

    @Override
    public void initTable() {
        try {
            super.initTable();

            newColumn.setCellValueFactory(new PropertyValueFactory<>("data"));
            yesButton.disableProperty().bind(tableView.itemsProperty().isNull());

        } catch (Exception e) {
            logger.error(e.toString());
        }
    }

    @Override
    public void moreAction() {
        if (moreButton.isSelected()) {
            if (!thisPane.getChildren().contains(commentsLabel)) {
                thisPane.getChildren().add(1, commentsLabel);
            }
            if (!thisPane.getChildren().contains(tableLabel)) {
                thisPane.getChildren().add(3, tableLabel);
            }
            if (!thisPane.getChildren().contains(selectPane)) {
                thisPane.getChildren().add(4, selectPane);
            }
        } else {
            thisPane.getChildren().removeAll(commentsLabel, selectPane, tableLabel);
        }
        FxmlControl.refreshStyle(thisPane);
        AppVariables.setUserConfigValue("FileTableMore", moreButton.isSelected());
    }

    @Override
    public void tableChanged() {
        super.tableChanged();
        if (tableData.isEmpty()) {
            recoverySelectedButton.setDisable(true);
            recoveryAllButton.setDisable(true);
            addFilesButton.setDisable(false);
            addDirectoryButton.setDisable(false);
        }
    }

    @Override
    public void tableSelected() {
        super.tableSelected();
        FileInformation selected = tableView.getSelectionModel().getSelectedItem();
        boolean none = (selected == null);
        if (none) {
            recoverySelectedButton.setDisable(true);
            recoveryAllButton.setDisable(true);
        }
    }

    public void setButtonsAfterRenamed() {
        addFilesButton.setDisable(true);
        addDirectoryButton.setDisable(true);
        insertFilesButton.setDisable(true);
        insertDirectoryButton.setDisable(true);
        deleteFilesButton.setDisable(true);
        recoveryAllButton.setDisable(false);
        recoverySelectedButton.setDisable(false);
    }

    @FXML
    protected void recoveryAllAction(ActionEvent event) {
        if (parentController != null) {
            FilesRenameController p = (FilesRenameController) parentController;
            p.recoveryAllAction();
            tableView.refresh();
            addFilesButton.setDisable(false);
            addDirectoryButton.setDisable(false);
            insertFilesButton.setDisable(false);
            insertDirectoryButton.setDisable(false);
            deleteFilesButton.setDisable(false);
            upFilesButton.setDisable(false);
            downFilesButton.setDisable(false);
            recoverySelectedButton.setDisable(true);
            recoveryAllButton.setDisable(true);
        }
    }

    @FXML
    protected void recoverySelectedAction(ActionEvent event) {
        if (parentController != null) {
            FilesRenameController p = (FilesRenameController) parentController;
            p.recoverySelectedAction();
        }
        tableView.refresh();
        addFilesButton.setDisable(true);
        addDirectoryButton.setDisable(true);
        insertFilesButton.setDisable(true);
        insertDirectoryButton.setDisable(true);
        deleteFilesButton.setDisable(true);
    }

    @FXML
    protected void okAction(ActionEvent event) {
        if (parentController == null) {
            return;
        }
        FilesRenameController p = (FilesRenameController) parentController;
        p.okAction();
        tableView.refresh();
        addFilesButton.setDisable(false);
        addDirectoryButton.setDisable(false);
        insertFilesButton.setDisable(false);
        insertDirectoryButton.setDisable(false);
        deleteFilesButton.setDisable(false);
        upFilesButton.setDisable(false);
        downFilesButton.setDisable(false);
        recoverySelectedButton.setDisable(true);
        recoveryAllButton.setDisable(true);

    }

}
