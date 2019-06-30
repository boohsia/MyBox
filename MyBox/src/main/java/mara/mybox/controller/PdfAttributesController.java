package mara.mybox.controller;

import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.Optional;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.CheckBox;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.control.TextInputDialog;
import javafx.scene.layout.Region;
import javafx.stage.Modality;
import mara.mybox.controller.base.BaseController;
import mara.mybox.data.PdfInformation;
import mara.mybox.data.VisitHistory;
import static mara.mybox.fxml.FxmlControl.badStyle;
import mara.mybox.tools.DateTools;
import mara.mybox.tools.FileTools;
import mara.mybox.tools.PdfTools;
import mara.mybox.value.AppVaribles;
import static mara.mybox.value.AppVaribles.getMessage;
import static mara.mybox.value.AppVaribles.logger;
import mara.mybox.value.CommonValues;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.encryption.AccessPermission;
import org.apache.pdfbox.pdmodel.encryption.InvalidPasswordException;

/**
 * @Author Mara
 * @CreateDate 2018-9-10
 * @Description
 * @License Apache License Version 2.0
 */
public class PdfAttributesController extends BaseController {

    private PdfInformation pdfInfo;
    private float version;
    private Date createTime, modifyTime;

    @FXML
    protected TextField titleInput, subjectInput, authorInput, creatorInput, producerInput,
            createTimeInput, modifyTimeInput, keywordInput, versionInput;
    @FXML
    protected PasswordField userPasswordInput, userPasswordInput2, ownerPasswordInput, ownerPasswordInput2;
    @FXML
    protected CheckBox assembleCheck, extractCheck, modifyCheck, fillCheck, printCheck,
            viewPasswordCheck;

    public PdfAttributesController() {
        baseTitle = AppVaribles.getMessage("PDFAttributes");

        SourceFileType = VisitHistory.FileType.PDF;
        SourcePathType = VisitHistory.FileType.PDF;
        TargetPathType = VisitHistory.FileType.PDF;
        TargetFileType = VisitHistory.FileType.PDF;
        AddFileType = VisitHistory.FileType.PDF;
        AddPathType = VisitHistory.FileType.PDF;

        targetPathKey = "PdfTargetPath";
        sourcePathKey = "PdfSourcePath";

        fileExtensionFilter = CommonValues.PdfExtensionFilter;

    }

    @Override
    public void initializeNext() {
        versionInput.textProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue ov, String oldValue, String newValue) {
                try {
                    Float f = Float.parseFloat(newValue);
                    if (f >= 0) {
                        versionInput.setStyle(null);
                        version = f;
                    } else {
                        versionInput.setStyle(badStyle);
                    }
                } catch (Exception e) {
                    versionInput.setStyle(badStyle);
                }
            }
        });

        createTimeInput.textProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue ov, String oldValue, String newValue) {
                if (newValue == null || newValue.trim().isEmpty()) {
                    createTime = null;
                    createTimeInput.setStyle(null);
                    return;
                }
                try {
                    Date d = DateTools.stringToDatetime(newValue);
                    if (d != null) {
                        createTimeInput.setStyle(null);
                        createTime = d;
                    } else {
                        createTimeInput.setStyle(badStyle);
                    }
                } catch (Exception e) {
                    createTimeInput.setStyle(badStyle);
                }
            }
        });

        modifyTimeInput.textProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue ov, String oldValue, String newValue) {
                if (newValue == null || newValue.trim().isEmpty()) {
                    modifyTime = null;
                    modifyTimeInput.setStyle(null);
                    return;
                }
                try {
                    Date d = DateTools.stringToDatetime(newValue);
                    if (d != null) {
                        modifyTimeInput.setStyle(null);
                        modifyTime = d;
                    } else {
                        modifyTimeInput.setStyle(badStyle);
                    }
                } catch (Exception e) {
                    modifyTimeInput.setStyle(badStyle);
                }
            }
        });

        userPasswordInput.textProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue ov, String oldValue, String newValue) {
                checkUserPassword();
            }
        });

        userPasswordInput2.textProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue ov, String oldValue, String newValue) {
                checkUserPassword();
            }
        });

        ownerPasswordInput.textProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue ov, String oldValue, String newValue) {
                checkOwnerPassword();
            }
        });

        ownerPasswordInput2.textProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue ov, String oldValue, String newValue) {
                checkOwnerPassword();
            }
        });

        saveButton.disableProperty().bind(
                sourceFileInput.styleProperty().isEqualTo(badStyle)
                        .or(versionInput.styleProperty().isEqualTo(badStyle))
                        .or(createTimeInput.styleProperty().isEqualTo(badStyle))
                        .or(modifyTimeInput.styleProperty().isEqualTo(badStyle))
                        .or(userPasswordInput2.styleProperty().isEqualTo(badStyle))
                        .or(ownerPasswordInput2.styleProperty().isEqualTo(badStyle))
        );

    }

    @Override
    public void sourceFileChanged(final File file) {
        sourceFileInput.setStyle(badStyle);
        if (!FileTools.isPDF(file.getAbsolutePath())) {
            return;
        }
        sourceFile = file;
        loadPdfInformation(null);
    }

    public void checkUserPassword() {
        String p1 = userPasswordInput.getText();
        String p2 = userPasswordInput2.getText();
        boolean valid;
        if (p1 == null || p1.isEmpty()) {
            valid = p2 == null || p2.isEmpty();
        } else {
            valid = p1.equals(p2);
        }
        if (valid) {
            userPasswordInput.setStyle(null);
            userPasswordInput2.setStyle(null);
        } else {
            userPasswordInput2.setStyle(badStyle);
        }
    }

    public void checkOwnerPassword() {
        String p1 = ownerPasswordInput.getText();
        String p2 = ownerPasswordInput2.getText();
        boolean valid;
        if (p1 == null || p1.isEmpty()) {
            valid = p2 == null || p2.isEmpty();
        } else {
            valid = p1.equals(p2);
        }
        if (valid) {
            ownerPasswordInput.setStyle(null);
            ownerPasswordInput2.setStyle(null);
        } else {
            ownerPasswordInput2.setStyle(badStyle);
        }
    }

    public void loadPdfInformation(final String password) {
        infoButton.setDisable(true);
        pdfInfo = null;
        version = -1;
        createTime = null;
        modifyTime = null;
        if (sourceFile == null) {
            return;
        }
        pdfInfo = new PdfInformation(sourceFile);
        if (task != null && task.isRunning()) {
            task.cancel();
        }
        task = new Task<Void>() {
            private boolean ok, pop;

            @Override
            public Void call() throws Exception {
                ok = false;
                pop = false;
                try {
                    try (PDDocument doc = PDDocument.load(sourceFile, password, AppVaribles.pdfMemUsage)) {
                        pdfInfo.setOwnerPassword(password);
                        pdfInfo.readInfo(doc);
                        ok = true;
                    }
                } catch (InvalidPasswordException e) {
                    pop = true;
                } catch (IOException e) {
                }
                if (pop) {
                    Platform.runLater(new Runnable() {
                        @Override
                        public void run() {
                            TextInputDialog dialog = new TextInputDialog();
                            dialog.setHeaderText(AppVaribles.getMessage("OwnerPasswordComments"));
                            dialog.setContentText(AppVaribles.getMessage("OwnerPassword"));
                            Optional<String> result = dialog.showAndWait();
                            if (result.isPresent()) {
                                loadPdfInformation(result.get());
                            }
                        }
                    });
                }
                return null;
            }

            @Override
            public void succeeded() {
                super.succeeded();
                if (!ok) {
                    return;
                }
                Platform.runLater(new Runnable() {
                    @Override
                    public void run() {
                        sourceFileInput.setStyle(null);
                        infoButton.setDisable(false);
                        resetAction();
                    }
                });
            }
        };
        openHandlingStage(task, Modality.WINDOW_MODAL);
        Thread thread = new Thread(task);
        thread.setDaemon(true);
        thread.start();
    }

    @FXML
    public void resetAction() {
        if (pdfInfo == null) {
            return;
        }
        titleInput.setText(pdfInfo.getTitle());
        subjectInput.setText(pdfInfo.getSubject());
        authorInput.setText(pdfInfo.getAuthor());
        creatorInput.setText(pdfInfo.getCreator());
        producerInput.setText(pdfInfo.getProducer());
        createTimeInput.setText(DateTools.datetimeToString(pdfInfo.getCreateTime()));
        modifyTimeInput.setText(DateTools.datetimeToString(pdfInfo.getModifyTime()));
        keywordInput.setText(pdfInfo.getKeywords());
        versionInput.setText(pdfInfo.getVersion() + "");
        AccessPermission acc = pdfInfo.getAccess();
        if (acc != null) {
            assembleCheck.setSelected(acc.canAssembleDocument());
            extractCheck.setSelected(acc.canExtractContent());
            modifyCheck.setSelected(acc.canModify());
            fillCheck.setSelected(acc.canFillInForm());
            printCheck.setSelected(acc.canPrint());
        }
        ownerPasswordInput.setText(pdfInfo.getOwnerPassword());
        ownerPasswordInput2.setText(pdfInfo.getOwnerPassword());
        userPasswordInput.setText("");
        userPasswordInput2.setText("");

    }

    @FXML
    @Override
    public void infoAction() {
        if (pdfInfo == null) {
            return;
        }
        try {
            final PdfInformationController controller = (PdfInformationController) openStage(CommonValues.PdfInformationFxml);
            controller.setInformation(pdfInfo);

        } catch (Exception e) {
            logger.error(e.toString());
        }
    }

    @FXML
    public void nowCreateTime() {
        createTimeInput.setText(DateTools.nowString());
    }

    @FXML
    public void nowModifyTime() {
        modifyTimeInput.setText(DateTools.nowString());
    }

    @FXML
    @Override
    public void saveAction() {
        if (sourceFile == null) {
            return;
        }
        if ((userPasswordInput.getText() != null && !userPasswordInput.getText().isEmpty())
                || (ownerPasswordInput.getText() != null && !ownerPasswordInput.getText().isEmpty())) {
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle(myStage.getTitle());
            alert.setContentText(AppVaribles.getMessage("SureSetPassword"));
            alert.getDialogPane().setMinHeight(Region.USE_PREF_SIZE);
            Optional<ButtonType> result = alert.showAndWait();
            if (result.get() != ButtonType.OK) {
                return;
            }
        }

        PdfInformation modify = new PdfInformation(sourceFile);
        modify.setAuthor(authorInput.getText());
        modify.setTitle(titleInput.getText());
        modify.setSubject(subjectInput.getText());
        modify.setCreator(creatorInput.getText());
        modify.setProducer(producerInput.getText());
        modify.setKeywords(keywordInput.getText());
        modify.setModifyTime(modifyTime);
        modify.setCreateTime(createTime);
        modify.setVersion(version);
        modify.setUserPassword(userPasswordInput.getText());
        modify.setOwnerPassword(ownerPasswordInput.getText());
        AccessPermission acc = AccessPermission.getOwnerAccessPermission();
        acc.setCanAssembleDocument(assembleCheck.isSelected());
        acc.setCanExtractContent(extractCheck.isSelected());
        acc.setCanExtractForAccessibility(extractCheck.isSelected());
        acc.setCanFillInForm(fillCheck.isSelected());
        acc.setCanModify(modifyCheck.isSelected());
        acc.setCanModifyAnnotations(modifyCheck.isSelected());
        acc.setCanPrint(printCheck.isSelected());
        acc.setCanPrintDegraded(printCheck.isSelected());
        modify.setAccess(acc);
        if (PdfTools.setAttributes(sourceFile, pdfInfo.getOwnerPassword(), modify)) {
            loadPdfInformation(ownerPasswordInput.getText());
            popInformation(getMessage("Successful"));
        } else {
            popError(getMessage("Failed"));
        }

    }

}
