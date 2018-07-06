package mara.mybox.controller;

import java.io.File;
import java.util.List;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.Pane;
import javafx.stage.FileChooser;
import mara.mybox.objects.AppVaribles;
import mara.mybox.objects.FileInformation;

/**
 * @Author Mara
 * @CreateDate 2018-7-6
 * @Description
 * @License Apache License Version 2.0
 */
public class FilesTableController extends BaseController {

    protected ObservableList<FileInformation> tableData = FXCollections.observableArrayList();

    @FXML
    protected Pane filesTablePane;
    @FXML
    protected Button addButton, clearButton, deleteButton, recoveryAllButton, recoverySelectedButton;
    @FXML
    protected TableView<FileInformation> filesTableView;
    @FXML
    protected TableColumn<FileInformation, String> handledColumn;
    @FXML
    protected TableColumn<FileInformation, String> fileColumn;
    @FXML
    protected TableColumn<FileInformation, String> newColumn;
    @FXML
    protected TableColumn<FileInformation, String> modifyTimeColumn;
    @FXML
    protected TableColumn<FileInformation, String> createTimeColumn;
    @FXML
    protected TableColumn<FileInformation, String> typeColumn;
    @FXML
    protected TableColumn<FileInformation, Long> sizeColumn;

    @Override
    protected void initializeNext() {
        try {

            handledColumn.setCellValueFactory(new PropertyValueFactory<FileInformation, String>("handled"));
            fileColumn.setCellValueFactory(new PropertyValueFactory<FileInformation, String>("fileName"));
            if (newColumn != null) {
                newColumn.setCellValueFactory(new PropertyValueFactory<FileInformation, String>("newName"));
            }
            modifyTimeColumn.setCellValueFactory(new PropertyValueFactory<FileInformation, String>("modifyTime"));
            createTimeColumn.setCellValueFactory(new PropertyValueFactory<FileInformation, String>("createTime"));
            typeColumn.setCellValueFactory(new PropertyValueFactory<FileInformation, String>("fileType"));
            sizeColumn.setCellValueFactory(new PropertyValueFactory<FileInformation, Long>("fileSize"));

            filesTableView.setItems(tableData);
            filesTableView.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);

        } catch (Exception e) {
            logger.error(e.toString());
        }
    }

    @FXML
    void addAction(ActionEvent event) {
        try {
            final FileChooser fileChooser = new FileChooser();
            File defaultPath = new File(AppVaribles.getConfigValue(parentController.sourcePathKey, System.getProperty("user.home")));
            if (!defaultPath.isDirectory()) {
                defaultPath = new File(System.getProperty("user.home"));
            }
            fileChooser.setInitialDirectory(defaultPath);
            fileChooser.getExtensionFilters().addAll(parentController.fileExtensionFilter);
            List<File> files = fileChooser.showOpenMultipleDialog(getMyStage());
            if (files == null || files.isEmpty()) {
                return;
            }

            String path = files.get(0).getParent();
            AppVaribles.setConfigValue("LastPath", path);
            AppVaribles.setConfigValue(parentController.sourcePathKey, path);
            for (File file : files) {
                if (findData(file.getAbsolutePath()) != null) {
                    continue;
                }
                FileInformation newFile = new FileInformation(file);
                tableData.add(newFile);
            }

        } catch (Exception e) {
//            logger.error(e.toString());
        }
    }

    @FXML
    void clearAction(ActionEvent event) {
        tableData.clear();
        addButton.setDisable(false);
        deleteButton.setDisable(false);
        recoveryAllButton.setDisable(true);
        recoverySelectedButton.setDisable(true);
    }

    @FXML
    void deleteAction(ActionEvent event) {
        ObservableList<FileInformation> selected = filesTableView.getSelectionModel().getSelectedItems();
        if (selected == null || selected.isEmpty()) {
            return;
        }
        for (FileInformation d : selected) {
            tableData.remove(d);
        }
    }

    @FXML
    void recoveryAllAction(ActionEvent event) {
        if (tableData == null || tableData.isEmpty()) {
            return;
        }
        for (FileInformation f : tableData) {
            String originalName = f.getFileName();
            File newName = new File(f.getNewName());
            if (newName.exists()) {
                if (newName.renameTo(new File(originalName))) {
                    f.setHandled(AppVaribles.getMessage("Recovered"));
                    f.setFileName(originalName);
                    f.setNewName("");
                } else {
                    f.setHandled(AppVaribles.getMessage("FailRecovered"));
                }
            }
        }
        filesTableView.refresh();
        addButton.setDisable(false);
        deleteButton.setDisable(false);
    }

    @FXML
    void recoverySelectedAction(ActionEvent event) {
        ObservableList<FileInformation> selected = filesTableView.getSelectionModel().getSelectedItems();
        if (selected == null || selected.isEmpty()) {
            return;
        }
        for (FileInformation f : selected) {
            String originalName = f.getFileName();
            File newName = new File(f.getNewName());
            if (newName.exists()) {
                if (newName.renameTo(new File(originalName))) {
                    f.setHandled(AppVaribles.getMessage("Recovered"));
                    f.setFileName(originalName);
                    f.setNewName("");
                } else {
                    f.setHandled(AppVaribles.getMessage("FailRecovered"));
                }
            }
        }
        filesTableView.refresh();
        addButton.setDisable(true);
        deleteButton.setDisable(true);
    }

    public FileInformation findData(String filename) {
        for (FileInformation d : tableData) {
            if (d.getFileName().equals(filename)) {
                return d;
            }
        }
        return null;
    }

    public ObservableList<FileInformation> getTableData() {
        return tableData;
    }

    public void setTableData(ObservableList<FileInformation> tableData) {
        this.tableData = tableData;
    }

    public Pane getFilesTablePane() {
        return filesTablePane;
    }

    public void setFilesTablePane(Pane filesTablePane) {
        this.filesTablePane = filesTablePane;
    }

    public Button getAddButton() {
        return addButton;
    }

    public void setAddButton(Button addButton) {
        this.addButton = addButton;
    }

    public Button getClearButton() {
        return clearButton;
    }

    public void setClearButton(Button clearButton) {
        this.clearButton = clearButton;
    }

    public Button getDeleteButton() {
        return deleteButton;
    }

    public void setDeleteButton(Button deleteButton) {
        this.deleteButton = deleteButton;
    }

    public TableView<FileInformation> getFilesTableView() {
        return filesTableView;
    }

    public void setFilesTableView(TableView<FileInformation> filesTableView) {
        this.filesTableView = filesTableView;
    }

    public TableColumn<FileInformation, String> getFileColumn() {
        return fileColumn;
    }

    public void setFileColumn(TableColumn<FileInformation, String> fileColumn) {
        this.fileColumn = fileColumn;
    }

    public TableColumn<FileInformation, String> getModifyTimeColumn() {
        return modifyTimeColumn;
    }

    public void setModifyTimeColumn(TableColumn<FileInformation, String> modifyTimeColumn) {
        this.modifyTimeColumn = modifyTimeColumn;
    }

    public TableColumn<FileInformation, String> getCreateTimeColumn() {
        return createTimeColumn;
    }

    public void setCreateTimeColumn(TableColumn<FileInformation, String> createTimeColumn) {
        this.createTimeColumn = createTimeColumn;
    }

    public TableColumn<FileInformation, String> getTypeColumn() {
        return typeColumn;
    }

    public void setTypeColumn(TableColumn<FileInformation, String> typeColumn) {
        this.typeColumn = typeColumn;
    }

    public TableColumn<FileInformation, Long> getSizeColumn() {
        return sizeColumn;
    }

    public void setSizeColumn(TableColumn<FileInformation, Long> sizeColumn) {
        this.sizeColumn = sizeColumn;
    }

}
