package mara.mybox.controller;

import com.recognition.software.jdeskew.ImageDeskew;
import java.awt.image.BufferedImage;
import java.io.File;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.RadioButton;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.TextField;
import javafx.scene.control.Toggle;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import mara.mybox.data.ConvolutionKernel;
import mara.mybox.fxml.FxmlControl;
import static mara.mybox.fxml.FxmlControl.badStyle;
import mara.mybox.image.ImageBinary;
import mara.mybox.image.ImageContrast;
import mara.mybox.image.ImageConvolution;
import mara.mybox.image.ImageManufacture;
import mara.mybox.image.PixelsOperation;
import mara.mybox.tools.FileTools;
import mara.mybox.tools.OCRTools;
import mara.mybox.value.AppVariables;
import static mara.mybox.value.AppVariables.logger;
import static mara.mybox.value.AppVariables.message;
import mara.mybox.value.CommonValues;
import net.sourceforge.tess4j.ITesseract;
import net.sourceforge.tess4j.Tesseract;
import net.sourceforge.tess4j.util.ImageHelper;
import org.apache.pdfbox.cos.COSName;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDResources;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;
import org.apache.pdfbox.rendering.ImageType;
import org.apache.pdfbox.rendering.PDFRenderer;

/**
 * @Author Mara
 * @CreateDate 2019-9-18
 * @Description
 * @License Apache License Version 2.0
 */
public class PdfOcrBatchController extends PdfBatchController {

    protected String separator;
    protected String ocrTexts;
    protected File tmpFile;
    protected String ocrPath;
    protected BufferedImage lastImage;
    protected ITesseract OCRinstance;
    protected PDFRenderer renderer;
    protected int dpi, threshold, rotate;
    protected float scale;
    protected String selectedLanguages;

    @FXML
    protected ToggleGroup getImageType;
    @FXML
    protected CheckBox separatorCheck, deskewCheck, invertCheck;
    @FXML
    protected TextField separatorInput;
    @FXML
    protected ComboBox<String> dpiSelector, enhancementSelector, rotateSelector,
            binarySelector, scaleSelector;
    @FXML
    protected RadioButton convertRadio, extractRadio;
    @FXML
    protected HBox scaleBox, dpiBox;
    @FXML
    protected FlowPane imageOptionsPane;
    @FXML
    protected ListView languageList;
    @FXML
    protected Label currentOCRFilesLabel;

    public PdfOcrBatchController() {
        baseTitle = AppVariables.message("PdfOCRBatch");
        browseTargets = false;
    }

    @Override
    public void initOptionsSection() {
        try {
            initPreprocessBox();
            initOCROptionsBox();
        } catch (Exception e) {
            logger.debug(e.toString());
        }
    }

    protected void initPreprocessBox() {
        try {
            getImageType.selectedToggleProperty().addListener(new ChangeListener<Toggle>() {
                @Override
                public void changed(ObservableValue<? extends Toggle> v, Toggle oldV, Toggle newV) {
                    checkGetImageType();
                }
            });
            checkGetImageType();

            dpi = 96;
            dpiSelector.getItems().addAll(Arrays.asList(
                    "96", "72", "300", "160", "240", "120", "600", "400"
            ));
            dpiSelector.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<String>() {
                @Override
                public void changed(ObservableValue<? extends String> v, String oldV, String newV) {
                    try {
                        if (newV == null || newV.isEmpty()) {
                            return;
                        }
                        int i = Integer.valueOf(newV);
                        if (i > 0) {
                            dpi = i;
                            dpiSelector.getEditor().setStyle(null);
                            AppVariables.setUserConfigInt("PdfOcrDpi", dpi);
                        } else {
                            dpiSelector.getEditor().setStyle(badStyle);
                        }
                    } catch (Exception e) {
                        dpiSelector.getEditor().setStyle(badStyle);
                    }
                }
            });
            dpiSelector.getSelectionModel().select(AppVariables.getUserConfigValue("PdfOcrDpi", "96"));

            scale = 1.0f;
            scaleSelector.getItems().addAll(Arrays.asList(
                    "1.0", "1.5", "2.0", "2.5", "3.0", "5.0", "10.0"
            ));
            scaleSelector.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<String>() {
                @Override
                public void changed(ObservableValue<? extends String> v, String oldV, String newV) {
                    try {
                        if (newV == null || newV.isEmpty()) {
                            scale = 1;
                            return;
                        }
                        float f = Float.valueOf(newV);
                        if (f > 0) {
                            scale = f;
                            scaleSelector.getEditor().setStyle(null);
                        } else {
                            scaleSelector.getEditor().setStyle(badStyle);
                        }
                    } catch (Exception e) {
                        scaleSelector.getEditor().setStyle(badStyle);
                    }
                }
            });
            scaleSelector.getSelectionModel().select(0);

            threshold = 0;
            binarySelector.getItems().addAll(Arrays.asList(
                    "65", "50", "75", "45", "30", "80", "85", "15"
            ));
            binarySelector.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<String>() {
                @Override
                public void changed(ObservableValue<? extends String> v, String oldV, String newV) {
                    try {
                        if (newV == null || newV.isEmpty()) {
                            threshold = 0;
                            return;
                        }
                        int i = Integer.valueOf(newV);
                        if (i > 0) {
                            threshold = i;
                            binarySelector.getEditor().setStyle(null);
                        } else {
                            binarySelector.getEditor().setStyle(badStyle);
                        }
                    } catch (Exception e) {
                        binarySelector.getEditor().setStyle(badStyle);
                    }
                }
            });

            enhancementSelector.getItems().addAll(Arrays.asList(
                    message("HSBHistogramEqualization"), message("GrayHistogramEqualization"),
                    message("GrayHistogramStretching"), message("GrayHistogramShifting"),
                    message("UnsharpMasking"),
                    message("FourNeighborLaplace"), message("EightNeighborLaplace"),
                    message("GaussianBlur"), message("AverageBlur")
            ));

            rotate = 0;
            rotateSelector.getItems().addAll(Arrays.asList(
                    "0", "90", "45", "15", "30", "60", "75", "180", "105", "135", "120", "150", "165", "270", "300", "315"
            ));
            rotateSelector.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<String>() {
                @Override
                public void changed(ObservableValue<? extends String> v, String oldV, String newV) {
                    try {
                        if (newV == null || newV.isEmpty()) {
                            rotate = 0;
                            return;
                        }
                        rotate = Integer.valueOf(newV);
                    } catch (Exception e) {

                    }
                }
            });

            deskewCheck.selectedProperty().addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
                    AppVariables.setUserConfigValue("ImageOCRDeskew", newValue);
                }
            });
            deskewCheck.setSelected(AppVariables.getUserConfigBoolean("ImageOCRDeskew", false));

        } catch (Exception e) {
            logger.debug(e.toString());
        }
    }

    protected void checkGetImageType() {
        if (convertRadio.isSelected()) {
            if (imageOptionsPane.getChildren().contains(scaleBox)) {
                imageOptionsPane.getChildren().remove(scaleBox);
            }
            if (!imageOptionsPane.getChildren().contains(dpiBox)) {
                imageOptionsPane.getChildren().add(dpiBox);
            }
            scale = 1.0f;
        } else if (extractRadio.isSelected()) {

            if (imageOptionsPane.getChildren().contains(dpiBox)) {
                imageOptionsPane.getChildren().remove(dpiBox);
            }
            if (!imageOptionsPane.getChildren().contains(scaleBox)) {
                imageOptionsPane.getChildren().add(scaleBox);
            }
            scale = Float.valueOf(scaleSelector.getValue());
        }
    }

    protected void initOCROptionsBox() {
        try {
            languageList.getItems().clear();
            languageList.getItems().addAll(OCRTools.namesList());
            languageList.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
            languageList.setPrefHeight(200);
            languageList.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<String>() {
                @Override
                public void changed(ObservableValue<? extends String> ov, String oldVal, String newVal) {
                    checkLanguages();
                }
            });
            selectedLanguages = AppVariables.getUserConfigValue("ImageOCRLanguages", null);
            if (selectedLanguages != null && !selectedLanguages.isEmpty()) {
                currentOCRFilesLabel.setText(
                        MessageFormat.format(message("CurrentDataFiles"), selectedLanguages));
                isSettingValues = true;
                String[] langs = selectedLanguages.split("\\+");
                Map<String, String> codes = OCRTools.codeName();
                for (String code : langs) {
                    String name = codes.get(code);
                    if (name == null) {
                        name = code;
                    }
                    languageList.getSelectionModel().select(name);
                }
                isSettingValues = false;
            } else {
                currentOCRFilesLabel.setText(
                        MessageFormat.format(message("CurrentDataFiles"), ""));
            }

            FxmlControl.setTooltip(separatorInput, message("InsertPageSeparatorComments"));

        } catch (Exception e) {
            logger.debug(e.toString());
        }
    }

    public void checkLanguages() {
        if (isSettingValues) {
            return;
        }
        List<String> langsList = languageList.getSelectionModel().getSelectedItems();
        selectedLanguages = null;
        Map<String, String> names = OCRTools.nameCode();
        for (String name : langsList) {
            String code = names.get(name);
            if (code == null) {
                code = name;
            }
            if (selectedLanguages == null) {
                selectedLanguages = code;
            } else {
                selectedLanguages += "+" + code;
            }
        }
        if (selectedLanguages != null) {
            AppVariables.setUserConfigValue("ImageOCRLanguages", selectedLanguages);
            currentOCRFilesLabel.setText(
                    MessageFormat.format(message("CurrentDataFiles"), selectedLanguages));
        } else {
            currentOCRFilesLabel.setText(
                    MessageFormat.format(message("CurrentDataFiles"), ""));
        }
    }

    @FXML
    public void settingsAction() {
        SettingsController controller = (SettingsController) openStage(CommonValues.SettingsFxml);
        controller.setParentController(this);
        controller.setParentFxml(myFxml);
        controller.tabPane.getSelectionModel().select(controller.ocrTab);
    }

    @FXML
    public void upAction() {
        List<Integer> selected = new ArrayList<>();
        selected.addAll(languageList.getSelectionModel().getSelectedIndices());
        if (selected.isEmpty()) {
            return;
        }
        isSettingValues = true;
        List<Integer> newselected = new ArrayList<>();
        for (Integer index : selected) {
            if (index == 0 || newselected.contains(index - 1)) {
                newselected.add(index);
                continue;
            }
            String lang = (String) languageList.getItems().get(index);
            languageList.getItems().set(index, languageList.getItems().get(index - 1));
            languageList.getItems().set(index - 1, lang);
            newselected.add(index - 1);
        }
        languageList.getSelectionModel().clearSelection();
        for (int index : newselected) {
            languageList.getSelectionModel().select(index);
        }
        languageList.refresh();
        isSettingValues = false;
        checkLanguages();
    }

    @FXML
    public void downAction() {
        List<Integer> selected = new ArrayList<>();
        selected.addAll(languageList.getSelectionModel().getSelectedIndices());
        if (selected.isEmpty()) {
            return;
        }
        isSettingValues = true;
        List<Integer> newselected = new ArrayList<>();
        for (int i = selected.size() - 1; i >= 0; --i) {
            int index = selected.get(i);
            if (index == languageList.getItems().size() - 1
                    || newselected.contains(index + 1)) {
                newselected.add(index);
                continue;
            }
            String lang = (String) languageList.getItems().get(index);
            languageList.getItems().set(index, languageList.getItems().get(index + 1));
            languageList.getItems().set(index + 1, lang);
            newselected.add(index + 1);
        }
        languageList.getSelectionModel().clearSelection();
        for (int index : newselected) {
            languageList.getSelectionModel().select(index);
        }
        languageList.refresh();
        isSettingValues = false;
        checkLanguages();
    }

    @FXML
    public void topAction() {
        List<Integer> selectedIndices = new ArrayList<>();
        selectedIndices.addAll(languageList.getSelectionModel().getSelectedIndices());
        if (selectedIndices.isEmpty()) {
            return;
        }
        List<String> selected = new ArrayList<>();
        selected.addAll(languageList.getSelectionModel().getSelectedItems());
        isSettingValues = true;
        int size = selectedIndices.size();
        for (int i = size - 1; i >= 0; --i) {
            int index = selectedIndices.get(i);
            languageList.getItems().remove(index);
        }
        languageList.getSelectionModel().clearSelection();
        languageList.getItems().addAll(0, selected);
        languageList.getSelectionModel().selectRange(0, size);
        languageList.refresh();
        isSettingValues = false;
        checkLanguages();
    }

    @Override
    public boolean makeActualParameters() {
        if (!super.makeActualParameters()) {
            return false;
        }
        separator = separatorInput.getText();
        if (!separatorCheck.isSelected() || separator == null || separator.isEmpty()) {
            separator = null;
        }
        try {
            OCRinstance = new Tesseract();
            // https://stackoverflow.com/questions/58286373/tess4j-pdf-to-tiff-to-tesseract-warning-invalid-resolution-0-dpi-using-70/58296472#58296472
            if (convertRadio.isSelected()) {
                OCRinstance.setTessVariable("user_defined_dpi", dpi + "");
            } else {
                OCRinstance.setTessVariable("user_defined_dpi", "96");
            }
            OCRinstance.setTessVariable("debug_file", "/dev/null");

            String path = AppVariables.getUserConfigValue("TessDataPath", null);
            if (path != null) {
                OCRinstance.setDatapath(path);
            }
            OCRinstance.setLanguage(selectedLanguages);
            return true;
        } catch (Exception e) {
            logger.error(e.toString());
            return false;
        }
    }

    @Override
    public boolean preHandlePages() {
        try {
            ocrTexts = "";
            renderer = new PDFRenderer(doc);
            return true;
        } catch (Exception e) {
            logger.error(e.toString());
            return false;
        }
    }

    @Override
    public int handleCurrentPage() {
        int num;
        if (convertRadio.isSelected()) {
            num = convertPage();
        } else {
            num = extractPage();
        }
        if (num > 0 && separatorCheck.isSelected()) {
            String s = separator.replace("<Page Number>", currentParameters.currentPage + " ");
            s = s.replace("<Total Number>", doc.getNumberOfPages() + "");
            ocrTexts += s + System.getProperty("line.separator");
        }
        return num;
    }

    protected int convertPage() {
        try {
            // ImageType.BINARY work bad while ImageType.RGB works best
            BufferedImage bufferedImage
                    = renderer.renderImageWithDPI(currentParameters.currentPage - 1, dpi, ImageType.RGB);    // 0-based

            if (handleImage(bufferedImage)) {
                lastImage = bufferedImage;
                return 1;
            } else {
                return 0;
            }
        } catch (Exception e) {
            logger.error(e.toString());
            return 0;
        }
    }

    protected int extractPage() {
        int index = 0;
        try {
            PDPage page = doc.getPage(currentParameters.currentPage - 1);  // 0-based
            PDResources pdResources = page.getResources();
            Iterable<COSName> iterable = pdResources.getXObjectNames();
            if (iterable != null) {
                Iterator<COSName> pageIterator = iterable.iterator();
                while (pageIterator.hasNext()) {
                    if (task.isCancelled()) {
                        break;
                    }
                    COSName cosName = pageIterator.next();
                    if (!pdResources.isImageXObject(cosName)) {
                        continue;
                    }
                    PDImageXObject pdxObject = (PDImageXObject) pdResources.getXObject(cosName);
                    BufferedImage bufferedImage = pdxObject.getImage();
                    if (handleImage(bufferedImage)) {
                        lastImage = bufferedImage;
                        if (isPreview) {
                            break;
                        }
                        index++;
                    }
                }
            }

        } catch (Exception e) {
            logger.error(e.toString());
        }
        return index;
    }

    protected boolean handleImage(BufferedImage bufferedImage) {
        try {
            lastImage = bufferedImage;

            if (threshold > 0) {
                ImageBinary bin = new ImageBinary(lastImage, threshold);
                lastImage = bin.operateImage();
            }

            if (rotate != 0) {
                lastImage = ImageManufacture.rotateImage(lastImage, rotate);
            }
            if (scale > 0 && scale != 1) {
                lastImage = ImageManufacture.scaleImage(lastImage, scale);
            }

            String enhance = enhancementSelector.getValue();
            if (enhance == null || enhance.trim().isEmpty()) {
            } else if (message("GrayHistogramEqualization").equals(enhance)) {
                ImageContrast imageContrast = new ImageContrast(lastImage,
                        ImageContrast.ContrastAlgorithm.Gray_Histogram_Equalization);
                lastImage = imageContrast.operateImage();

            } else if (message("GrayHistogramStretching").equals(enhance)) {
                ImageContrast imageContrast = new ImageContrast(lastImage,
                        ImageContrast.ContrastAlgorithm.Gray_Histogram_Stretching);
                imageContrast.setIntPara1(100);
                imageContrast.setIntPara2(100);
                lastImage = imageContrast.operateImage();

            } else if (message("GrayHistogramShifting").equals(enhance)) {
                ImageContrast imageContrast = new ImageContrast(lastImage,
                        ImageContrast.ContrastAlgorithm.Gray_Histogram_Shifting);
                imageContrast.setIntPara1(80);
                lastImage = imageContrast.operateImage();

            } else if (message("HSBHistogramEqualization").equals(enhance)) {
                ImageContrast imageContrast = new ImageContrast(lastImage,
                        ImageContrast.ContrastAlgorithm.HSB_Histogram_Equalization);
                lastImage = imageContrast.operateImage();

            } else if (message("UnsharpMasking").equals(enhance)) {
                ConvolutionKernel kernel = ConvolutionKernel.makeUnsharpMasking(3);
                ImageConvolution imageConvolution = ImageConvolution.create().
                        setImage(lastImage).setKernel(kernel);
                lastImage = imageConvolution.operateImage();

            } else if (message("FourNeighborLaplace").equals(enhance)) {
                ConvolutionKernel kernel = ConvolutionKernel.MakeSharpenFourNeighborLaplace();
                ImageConvolution imageConvolution = ImageConvolution.create().
                        setImage(lastImage).setKernel(kernel);
                lastImage = imageConvolution.operateImage();

            } else if (message("EightNeighborLaplace").equals(enhance)) {
                ConvolutionKernel kernel = ConvolutionKernel.MakeSharpenEightNeighborLaplace();
                ImageConvolution imageConvolution = ImageConvolution.create().
                        setImage(lastImage).setKernel(kernel);
                lastImage = imageConvolution.operateImage();

            } else if (message("GaussianBlur").equals(enhance)) {
                ConvolutionKernel kernel = ConvolutionKernel.makeGaussBlur(3);
                ImageConvolution imageConvolution = ImageConvolution.create().
                        setImage(lastImage).setKernel(kernel);
                lastImage = imageConvolution.operateImage();

            } else if (message("AverageBlur").equals(enhance)) {
                ConvolutionKernel kernel = ConvolutionKernel.makeAverageBlur(1);
                ImageConvolution imageConvolution = ImageConvolution.create().
                        setImage(lastImage).setKernel(kernel);
                lastImage = imageConvolution.operateImage();

            }

            if (deskewCheck.isSelected()) {
                ImageDeskew id = new ImageDeskew(lastImage);
                double imageSkewAngle = id.getSkewAngle();
                if ((imageSkewAngle > OCRTools.MINIMUM_DESKEW_THRESHOLD
                        || imageSkewAngle < -(OCRTools.MINIMUM_DESKEW_THRESHOLD))) {
                    lastImage = ImageHelper.rotateImage(lastImage, -imageSkewAngle);
                }
            }

            if (invertCheck.isSelected()) {
                PixelsOperation pixelsOperation = PixelsOperation.create(lastImage,
                        null, PixelsOperation.OperationType.RGB, PixelsOperation.ColorActionType.Invert);
                lastImage = pixelsOperation.operateImage();
            }

            if (deskewCheck.isSelected()) {
                ImageDeskew id = new ImageDeskew(bufferedImage);
                double imageSkewAngle = id.getSkewAngle();
                if ((imageSkewAngle > OCRTools.MINIMUM_DESKEW_THRESHOLD
                        || imageSkewAngle < -(OCRTools.MINIMUM_DESKEW_THRESHOLD))) {
                    bufferedImage = ImageHelper.rotateImage(bufferedImage, -imageSkewAngle);
                }
            }

            String result = OCRinstance.doOCR(bufferedImage);
            if (result != null) {
                ocrTexts += result + System.getProperty("line.separator");
                return true;
            } else {
                return false;
            }

        } catch (Exception e) {
            logger.error(e.toString());
            return false;
        }
    }

    @Override
    public void postHandlePages() {
        try {
            File tFile = makeTargetFile(FileTools.getFilePrefix(currentParameters.currentSourceFile.getName()),
                    ".txt", currentParameters.currentTargetPath);
            currentTargetFile = tFile.getAbsolutePath();
            if (FileTools.writeFile(tFile, ocrTexts) != null) {
                targetFileGenerated(tFile);
            }
        } catch (Exception e) {
            logger.error(e.toString());
        }
    }

    @Override
    public void quitProcess() {
        OCRinstance = null;
    }

}
