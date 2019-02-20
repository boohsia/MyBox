package mara.mybox.controller;

import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import javafx.beans.binding.Bindings;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TextField;
import javafx.scene.control.Toggle;
import javafx.scene.control.ToggleGroup;
import javafx.scene.control.Tooltip;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.HBox;
import mara.mybox.db.TableConvolutionKernel;
import mara.mybox.image.ImageConvert;
import mara.mybox.fxml.FxmlTools;
import static mara.mybox.fxml.FxmlTools.badStyle;
import mara.mybox.image.ImageBinary;
import mara.mybox.image.ImageConvolution;
import mara.mybox.image.ImageQuantization;
import mara.mybox.image.ImageContrast;
import mara.mybox.image.ImageGray;
import mara.mybox.image.ImageQuantization.QuantizationAlgorithm;
import mara.mybox.image.PixelsOperation;
import mara.mybox.image.PixelsOperation.OperationType;
import static mara.mybox.value.AppVaribles.getMessage;
import static mara.mybox.value.AppVaribles.logger;
import mara.mybox.value.CommonValues;
import mara.mybox.data.ConvolutionKernel;

/**
 * @Author Mara
 * @CreateDate 2018-9-23
 * @Description
 * @License Apache License Version 2.0
 */
public class ImageManufactureBatchEffectsController extends ImageManufactureBatchController {

    private OperationType effectType;
    protected int intPara1, intPara2, intPara3;
    private List<ConvolutionKernel> kernels;
    private ConvolutionKernel kernel;
    private ComboBox<String> intBox, stringBox;
    private Label intLabel, intLabel2, intLabel3, intLabel4, stringLabel;
    private CheckBox valueCheck;
    private TextField intInput, intInput2, intInput3, intInput4;
    private RadioButton radio1, radio2, radio3, radio4;
    private ToggleGroup radioGroup;
    private Button setButton;
    private QuantizationAlgorithm quantizationAlgorithm;
    private ImageContrast.ContrastAlgorithm contrastAlgorithm;

    @FXML
    protected ToggleGroup effectsGroup;
    @FXML
    protected HBox setBox;
    @FXML
    protected RadioButton thresholdingRadio, posterizingRadio, bwRadio,
            convolutionRadio, contrastRadio;

    public ImageManufactureBatchEffectsController() {

    }

    @Override
    protected void initializeNext2() {
        try {
        } catch (Exception e) {
            logger.debug(e.toString());
        }
    }

    @Override
    protected void initOptionsSection() {
        try {

            effectsGroup.selectedToggleProperty().addListener(new ChangeListener<Toggle>() {
                @Override
                public void changed(ObservableValue<? extends Toggle> ov,
                        Toggle old_toggle, Toggle new_toggle) {
                    checkEffetcsOperationType();
                }
            });
            checkEffetcsOperationType();

            FxmlTools.setComments(thresholdingRadio, new Tooltip(getMessage("ThresholdingComments")));
            FxmlTools.setComments(posterizingRadio, new Tooltip(getMessage("QuantizationComments")));
            FxmlTools.setComments(bwRadio, new Tooltip(getMessage("BWThresholdComments")));

        } catch (Exception e) {
            logger.error(e.toString());
        }
    }

    private void removeTmpControls() {
        intBox = null;
        valueCheck = null;
        intInput = intInput2 = intInput3 = intInput4 = null;
        intLabel = intLabel2 = intLabel3 = intLabel4 = stringLabel = null;
        radio1 = radio2 = radio3 = radio4 = null;
        setButton = null;
    }

    private void checkEffetcsOperationType() {
        try {
            setBox.getChildren().clear();
            operationBarController.startButton.disableProperty().unbind();
            removeTmpControls();
            stringBox = null;
            radioGroup = null;

            RadioButton selected = (RadioButton) effectsGroup.getSelectedToggle();
            String selectedString = selected.getText();
            if (getMessage("Blur").equals(selectedString)) {
                effectType = OperationType.Blur;
                makeBlurBox();

            } else if (getMessage("Sharpen").equals(selectedString)) {
                effectType = OperationType.Sharpen;
                bindStart();

            } else if (getMessage("Clarity").equals(selectedString)) {
                effectType = OperationType.Clarity;
                bindStart();

            } else if (getMessage("EdgeDetection").equals(selectedString)) {
                effectType = OperationType.EdgeDetect;
                bindStart();

            } else if (getMessage("Emboss").equals(selectedString)) {
                effectType = OperationType.Emboss;
                makeEmbossBox();

            } else if (getMessage("Posterizing").equals(selectedString)) {
                effectType = OperationType.Quantization;
                makePosterizingBox();

            } else if (getMessage("Thresholding").equals(selectedString)) {
                effectType = OperationType.Thresholding;
                makeThresholdingBox();

            } else if (getMessage("Gray").equals(selectedString)) {
                effectType = OperationType.Gray;
                bindStart();

            } else if (getMessage("BlackOrWhite").equals(selectedString)) {
                effectType = OperationType.BlackOrWhite;
                makeBlackWhiteBox();

            } else if (getMessage("Sepia").equals(selectedString)) {
                effectType = OperationType.Sepia;
                makeSepiaBox();

            } else if (getMessage("Contrast").equals(selectedString)) {
                effectType = OperationType.Contrast;
                makeContrastBox();

            } else if (getMessage("Convolution").equals(selectedString)) {
                effectType = OperationType.Convolution;
                makeConvolutionBox();

            }

        } catch (Exception e) {
            logger.error(e.toString());
        }
    }

    private void bindStart() {
        operationBarController.startButton.disableProperty().bind(
                Bindings.isEmpty(targetPathInput.textProperty())
                        .or(targetPathInput.styleProperty().isEqualTo(badStyle))
                        .or(Bindings.isEmpty(sourceFilesInformation))
        );
    }

    private void makeBlurBox() {
        try {
            intPara1 = 10;
            intLabel = new Label(getMessage("Radius"));
            intBox = new ComboBox();
            intBox.setEditable(true);
            intBox.setPrefWidth(80);
            intBox.valueProperty().addListener(new ChangeListener<String>() {
                @Override
                public void changed(ObservableValue ov, String oldValue, String newValue) {
                    try {
                        int v = Integer.valueOf(newValue);
                        if (v > 0) {
                            intPara1 = v;
                            intBox.getEditor().setStyle(null);
                        } else {
                            intBox.getEditor().setStyle(badStyle);
                        }
                    } catch (Exception e) {
                        intBox.getEditor().setStyle(badStyle);
                    }
                }
            });
            intBox.getItems().addAll(Arrays.asList("10", "5", "3", "2", "1", "8", "15", "20", "30"));
            intBox.getSelectionModel().select(0);
            stringLabel = new Label(getMessage("Algorithm"));
            stringBox = new ComboBox();
            stringBox.valueProperty().addListener(new ChangeListener<String>() {
                @Override
                public void changed(ObservableValue ov, String oldValue, String newValue) {
                    try {
                        if (getMessage("AverageBlur").equals(newValue)) {
                            kernel = ConvolutionKernel.makeAverageBlur(intPara1);
                        } else {
                            kernel = ConvolutionKernel.makeGaussKernel(intPara1);
                        }
                        stringBox.getEditor().setStyle(null);
                    } catch (Exception e) {
                        stringBox.getEditor().setStyle(badStyle);
                    }
                }
            });
            stringBox.getItems().addAll(Arrays.asList(getMessage("AverageBlur"), getMessage("GaussianBlur")));
            stringBox.getSelectionModel().select(getMessage("AverageBlur"));
            setBox.getChildren().addAll(stringLabel, stringBox, intLabel, intBox);
            operationBarController.startButton.disableProperty().bind(
                    intBox.getEditor().styleProperty().isEqualTo(badStyle)
                            .or(stringBox.getEditor().styleProperty().isEqualTo(badStyle))
                            .or(Bindings.isEmpty(targetPathInput.textProperty()))
                            .or(targetPathInput.styleProperty().isEqualTo(badStyle))
                            .or(Bindings.isEmpty(sourceFilesInformation))
            );
        } catch (Exception e) {
            logger.error(e.toString());
        }
    }

    private void makeEmbossBox() {
        try {
            intPara1 = ImageConvert.Direction.Top;
            stringLabel = new Label(getMessage("Direction"));
            stringBox = new ComboBox();
            stringBox.valueProperty().addListener(new ChangeListener<String>() {
                @Override
                public void changed(ObservableValue ov, String oldValue, String newValue) {
                    if (newValue == null || newValue.trim().isEmpty()) {
                        return;
                    }
                    if (getMessage("Top").equals(newValue)) {
                        intPara1 = ImageConvert.Direction.Top;
                    } else if (getMessage("Bottom").equals(newValue)) {
                        intPara1 = ImageConvert.Direction.Bottom;
                    } else if (getMessage("Left").equals(newValue)) {
                        intPara1 = ImageConvert.Direction.Top;
                    } else if (getMessage("Right").equals(newValue)) {
                        intPara1 = ImageConvert.Direction.Right;
                    } else if (getMessage("LeftTop").equals(newValue)) {
                        intPara1 = ImageConvert.Direction.LeftTop;
                    } else if (getMessage("RightBottom").equals(newValue)) {
                        intPara1 = ImageConvert.Direction.RightBottom;
                    } else if (getMessage("LeftBottom").equals(newValue)) {
                        intPara1 = ImageConvert.Direction.LeftBottom;
                    } else if (getMessage("RightTop").equals(newValue)) {
                        intPara1 = ImageConvert.Direction.RightTop;
                    } else {
                        intPara1 = ImageConvert.Direction.Top;
                    }
                }
            });
            stringBox.getItems().addAll(Arrays.asList(getMessage("Top"), getMessage("Bottom"),
                    getMessage("Left"), getMessage("Right"),
                    getMessage("LeftTop"), getMessage("RightBottom"),
                    getMessage("LeftBottom"), getMessage("RightTop")));
            stringBox.getSelectionModel().select(getMessage("Top"));
            intPara2 = 3;
            intLabel = new Label(getMessage("Radius"));
            intBox = new ComboBox();
            intBox.setEditable(false);
            intBox.setPrefWidth(80);
            intBox.valueProperty().addListener(new ChangeListener<String>() {
                @Override
                public void changed(ObservableValue ov, String oldValue, String newValue) {
                    try {
                        int v = Integer.valueOf(newValue);
                        if (v > 0) {
                            intPara2 = v;
                            intBox.getEditor().setStyle(null);
                        } else {
                            intBox.getEditor().setStyle(badStyle);
                        }
                    } catch (Exception e) {
                        intBox.getEditor().setStyle(badStyle);
                    }
                }
            });
            intBox.getItems().addAll(Arrays.asList("3", "5"));
            intBox.getSelectionModel().select(0);
            valueCheck = new CheckBox(getMessage("Gray"));
            valueCheck.setSelected(true);
            setBox.getChildren().addAll(stringLabel, stringBox, intLabel, intBox, valueCheck);
            operationBarController.startButton.disableProperty().bind(
                    intBox.getEditor().styleProperty().isEqualTo(badStyle)
                            .or(Bindings.isEmpty(targetPathInput.textProperty()))
                            .or(targetPathInput.styleProperty().isEqualTo(badStyle))
                            .or(Bindings.isEmpty(sourceFilesInformation))
                            .or(stringBox.getEditor().styleProperty().isEqualTo(badStyle))
            );
        } catch (Exception e) {
            logger.error(e.toString());
        }
    }

    private void makePosterizingBox() {
        try {
            quantizationAlgorithm = QuantizationAlgorithm.RGB_Uniform;
            stringLabel = new Label(getMessage("Algorithm"));
            stringBox = new ComboBox();
            stringBox.valueProperty().addListener(new ChangeListener<String>() {
                @Override
                public void changed(ObservableValue ov, String oldValue, String newValue) {
                    if (getMessage("RGBUniformQuantization").equals(newValue)) {
                        quantizationAlgorithm = QuantizationAlgorithm.RGB_Uniform;
                    } else if (getMessage("HSBUniformQuantization").equals(newValue)) {
                        quantizationAlgorithm = QuantizationAlgorithm.HSB_Uniform;
                    }
                }
            });
            stringBox.getItems().addAll(Arrays.asList(getMessage("RGBUniformQuantization"),
                    getMessage("HSBUniformQuantization")));
            stringBox.getSelectionModel().select(getMessage("RGBUniformQuantization"));
            intPara1 = 64;
            intLabel = new Label(getMessage("ColorsNumber"));
            intBox = new ComboBox();
            intBox.setEditable(false);
            intBox.setPrefWidth(120);
            intBox.valueProperty().addListener(new ChangeListener<String>() {
                @Override
                public void changed(ObservableValue ov, String oldValue, String newValue) {
                    try {
                        int v = Integer.valueOf(newValue);
                        if (v > 0) {
                            intPara1 = v;
                            intBox.getEditor().setStyle(null);
                        } else {
                            intBox.getEditor().setStyle(badStyle);
                        }
                    } catch (Exception e) {
                        intBox.getEditor().setStyle(badStyle);
                    }
                }
            });
            intBox.getItems().addAll(Arrays.asList(
                    "64", "512", "8", "4096", "216", "343", "27", "125", "1000", "729", "1728", "8000"));
            intBox.getSelectionModel().select(0);
            valueCheck = new CheckBox(getMessage("Dithering"));
            valueCheck.setSelected(true);
            FxmlTools.setComments(valueCheck, new Tooltip(getMessage("DitherComments")));
            setBox.getChildren().addAll(stringLabel, stringBox, intLabel, intBox, valueCheck);
            operationBarController.startButton.disableProperty().bind(
                    intBox.getEditor().styleProperty().isEqualTo(badStyle)
                            .or(Bindings.isEmpty(targetPathInput.textProperty()))
                            .or(targetPathInput.styleProperty().isEqualTo(badStyle))
                            .or(Bindings.isEmpty(sourceFilesInformation))
                            .or(stringBox.getEditor().styleProperty().isEqualTo(badStyle))
            );
        } catch (Exception e) {
            logger.error(e.toString());
        }

    }

    private void makeThresholdingBox() {
        try {
            intPara1 = 128;
            intLabel = new Label(getMessage("Threshold"));
            intInput = new TextField();
            intInput.textProperty().addListener(new ChangeListener<String>() {
                @Override
                public void changed(ObservableValue<? extends String> observable,
                        String oldValue, String newValue) {
                    try {
                        int v = Integer.valueOf(newValue);
                        if (v >= 0 && v <= 255) {
                            intPara1 = v;
                            intInput.setStyle(null);
                        } else {
                            popError("0~255");
                            intInput.setStyle(badStyle);
                        }
                    } catch (Exception e) {
                        popError("0~255");
                        intInput.setStyle(badStyle);
                    }
                }
            });
            intInput.setPrefWidth(100);
            intInput.setText("128");
            FxmlTools.quickTooltip(intInput, new Tooltip("0~255"));

            intPara2 = 0;
            Label smallValueLabel = new Label(getMessage("SmallValue"));
            final TextField thresholdingMinInput = new TextField();
            thresholdingMinInput.textProperty().addListener(new ChangeListener<String>() {
                @Override
                public void changed(ObservableValue<? extends String> observable,
                        String oldValue, String newValue) {
                    try {
                        int v = Integer.valueOf(newValue);
                        if (v >= 0 && v <= 255) {
                            intPara2 = v;
                            thresholdingMinInput.setStyle(null);
                        } else {
                            popError("0~255");
                            thresholdingMinInput.setStyle(badStyle);
                        }
                    } catch (Exception e) {
                        popError("0~255");
                        thresholdingMinInput.setStyle(badStyle);
                    }
                }
            });
            thresholdingMinInput.setPrefWidth(100);
            thresholdingMinInput.setText("0");
            FxmlTools.quickTooltip(thresholdingMinInput, new Tooltip("0~255"));

            intPara3 = 255;
            Label bigValueLabel = new Label(getMessage("BigValue"));
            final TextField thresholdingMaxInput = new TextField();
            thresholdingMaxInput.textProperty().addListener(new ChangeListener<String>() {
                @Override
                public void changed(ObservableValue<? extends String> observable,
                        String oldValue, String newValue) {
                    try {
                        int v = Integer.valueOf(newValue);
                        if (v >= 0 && v <= 255) {
                            intPara3 = v;
                            thresholdingMaxInput.setStyle(null);
                        } else {
                            popError("0~255");
                            thresholdingMaxInput.setStyle(badStyle);
                        }
                    } catch (Exception e) {
                        popError("0~255");
                        thresholdingMaxInput.setStyle(badStyle);
                    }
                }
            });
            thresholdingMaxInput.setPrefWidth(100);
            thresholdingMaxInput.setText("255");
            FxmlTools.quickTooltip(thresholdingMaxInput, new Tooltip("0~255"));

            setBox.getChildren().addAll(intLabel, intInput,
                    bigValueLabel, thresholdingMaxInput,
                    smallValueLabel, thresholdingMinInput);
            operationBarController.startButton.disableProperty().bind(
                    intInput.styleProperty().isEqualTo(badStyle)
                            .or(Bindings.isEmpty(targetPathInput.textProperty()))
                            .or(targetPathInput.styleProperty().isEqualTo(badStyle))
                            .or(Bindings.isEmpty(sourceFilesInformation))
                            .or(thresholdingMinInput.styleProperty().isEqualTo(badStyle))
                            .or(thresholdingMaxInput.styleProperty().isEqualTo(badStyle))
            );
        } catch (Exception e) {
            logger.error(e.toString());
        }

    }

    private void makeBlackWhiteBox() {
        try {
            intPara2 = 128;
            intInput = new TextField();
            intInput.textProperty().addListener(new ChangeListener<String>() {
                @Override
                public void changed(ObservableValue<? extends String> observable,
                        String oldValue, String newValue) {
                    try {
                        int v = Integer.valueOf(intInput.getText());
                        if (v >= 0 && v <= 255) {
                            intPara2 = v;
                            intInput.setStyle(null);
                        } else {
                            intInput.setStyle(badStyle);
                        }
                    } catch (Exception e) {
                        intInput.setStyle(badStyle);
                    }
                }
            });
            intInput.setPrefWidth(100);
            intInput.setText("128");
            FxmlTools.quickTooltip(intInput, new Tooltip("0~255"));

            intPara1 = 1;
            radioGroup = new ToggleGroup();
            radio1 = new RadioButton(getMessage("OTSU"));
            radio1.setToggleGroup(radioGroup);
            radio1.setUserData(1);
            radio2 = new RadioButton(getMessage("Default"));
            radio2.setToggleGroup(radioGroup);
            radio2.setUserData(2);
            radio3 = new RadioButton(getMessage("Threshold"));
            radio3.setToggleGroup(radioGroup);
            radio3.setUserData(3);
            radioGroup.selectedToggleProperty().addListener(new ChangeListener<Toggle>() {
                @Override
                public void changed(ObservableValue<? extends Toggle> ov,
                        Toggle old_toggle, Toggle new_toggle) {
                    if (radioGroup.getSelectedToggle() == null) {
                        return;
                    }
                    intPara1 = (int) ((RadioButton) new_toggle).getUserData();
                    intInput.setDisable(intPara1 != 3);
                }
            });
            radio1.setSelected(true);

            valueCheck = new CheckBox(getMessage("Dithering"));
            valueCheck.setSelected(true);
            FxmlTools.setComments(valueCheck, new Tooltip(getMessage("DitherComments")));

            setBox.getChildren().addAll(radio1, radio2, radio3, intInput, valueCheck);
            operationBarController.startButton.disableProperty().bind(
                    intInput.styleProperty().isEqualTo(badStyle)
            );
        } catch (Exception e) {
            logger.error(e.toString());
        }

    }

    private void makeSepiaBox() {
        try {
            intPara1 = 80;
            intLabel = new Label(getMessage("Intensity"));
            intInput = new TextField();
            intInput.textProperty().addListener(new ChangeListener<String>() {
                @Override
                public void changed(ObservableValue<? extends String> observable,
                        String oldValue, String newValue) {
                    try {
                        int v = Integer.valueOf(intInput.getText());
                        if (v >= 0 && v <= 255) {
                            intPara1 = v;
                            intInput.setStyle(null);
                        } else {
                            intInput.setStyle(badStyle);
                            popError("0~255");
                        }
                    } catch (Exception e) {
                        popError("0~255");
                        intInput.setStyle(badStyle);
                    }
                }
            });
            intInput.setPrefWidth(100);
            intInput.setText("80");
            FxmlTools.quickTooltip(intInput, new Tooltip("0~255"));

            setBox.getChildren().addAll(intLabel, intInput);
            operationBarController.startButton.disableProperty().bind(
                    intInput.styleProperty().isEqualTo(badStyle)
                            .or(Bindings.isEmpty(targetPathInput.textProperty()))
                            .or(targetPathInput.styleProperty().isEqualTo(badStyle))
                            .or(Bindings.isEmpty(sourceFilesInformation))
            );
        } catch (Exception e) {
            logger.error(e.toString());
        }

    }

    private void makeConvolutionBox() {
        try {
            stringLabel = new Label(getMessage("ConvolutionKernel"));
            stringBox = new ComboBox();
            kernel = null;
            if (kernels == null) {
                kernels = TableConvolutionKernel.read();
            }
            loadKernelsList(kernels);
            stringBox.getSelectionModel().selectedIndexProperty().addListener(new ChangeListener<Number>() {
                @Override
                public void changed(ObservableValue ov, Number oldValue, Number newValue) {
                    int index = newValue.intValue();
                    if (index < 0 || index >= kernels.size()) {
                        kernel = null;
                        stringBox.getEditor().setStyle(badStyle);
                        return;
                    }
                    kernel = kernels.get(index);
                    stringBox.getEditor().setStyle(null);
                }
            });
            FxmlTools.quickTooltip(stringBox, new Tooltip(getMessage("CTRL+k")));
            setButton = new Button(getMessage("ManageDot"));
            setButton.setOnAction(new EventHandler<ActionEvent>() {
                @Override
                public void handle(ActionEvent event) {
                    BaseController c = openStage(CommonValues.ConvolutionKernelManagerFxml, false);
                    c.setParentController(getMyController());
                    c.setParentFxml(getMyFxml());
                }
            });
            setBox.getChildren().addAll(stringLabel, stringBox, setButton);
            operationBarController.startButton.disableProperty().bind(
                    stringBox.getEditor().styleProperty().isEqualTo(badStyle)
                            .or(Bindings.isEmpty(targetPathInput.textProperty()))
                            .or(targetPathInput.styleProperty().isEqualTo(badStyle))
                            .or(Bindings.isEmpty(sourceFilesInformation))
            );
        } catch (Exception e) {
            logger.error(e.toString());
        }

    }

    private void makeContrastBox() {
        try {
            contrastAlgorithm = ImageContrast.ContrastAlgorithm.HSB_Histogram_Equalization;
            stringLabel = new Label(getMessage("Algorithm"));
            stringBox = new ComboBox();
            stringBox.getItems().addAll(Arrays.asList(
                    getMessage("HSBHistogramEqualization"),
                    getMessage("GrayHistogramEqualization"),
                    getMessage("GrayHistogramStretching"),
                    getMessage("GrayHistogramShifting")
            //                    getMessage("LumaHistogramEqualization"),
            //                    getMessage("AdaptiveHistogramEqualization")
            ));
            stringBox.getSelectionModel().select(0);
            stringBox.valueProperty().addListener(new ChangeListener<String>() {
                @Override
                public void changed(ObservableValue ov, String oldValue, String newValue) {
                    if (getMessage("GrayHistogramEqualization").equals(newValue)) {
                        contrastAlgorithm = ImageContrast.ContrastAlgorithm.Gray_Histogram_Equalization;
                    } else if (getMessage("GrayHistogramShifting").equals(newValue)) {
                        contrastAlgorithm = ImageContrast.ContrastAlgorithm.Gray_Histogram_Shifting;
                    } else if (getMessage("GrayHistogramStretching").equals(newValue)) {
                        contrastAlgorithm = ImageContrast.ContrastAlgorithm.Gray_Histogram_Stretching;
                    } else if (getMessage("HSBHistogramEqualization").equals(newValue)) {
                        contrastAlgorithm = ImageContrast.ContrastAlgorithm.HSB_Histogram_Equalization;
                    } else if (getMessage("AdaptiveHistogramEqualization").equals(newValue)) {
                        contrastAlgorithm = ImageContrast.ContrastAlgorithm.Adaptive_Histogram_Equalization;
                    }
                }
            });

            intPara1 = 80;
            intLabel = new Label(getMessage("Offset"));
            intInput = new TextField();
            intInput.textProperty().addListener(new ChangeListener<String>() {
                @Override
                public void changed(ObservableValue<? extends String> observable,
                        String oldValue, String newValue) {
                    try {
                        int v = Integer.valueOf(intInput.getText());
                        if (v >= 0 && v <= 255) {
                            intPara1 = v;
                            intInput.setStyle(null);
                        } else {
                            intInput.setStyle(badStyle);
                            popError("0~255");
                        }
                    } catch (Exception e) {
                        popError("0~255");
                    }
                }
            });
            intInput.setPrefWidth(100);
            intInput.setText("80");
            FxmlTools.quickTooltip(intInput, new Tooltip("0~255"));

            setBox.getChildren().addAll(stringLabel, stringBox, intLabel, intInput);
            operationBarController.startButton.disableProperty().bind(
                    intInput.styleProperty().isEqualTo(badStyle)
                            .or(stringBox.getEditor().styleProperty().isEqualTo(badStyle))
                            .or(Bindings.isEmpty(targetPathInput.textProperty()))
                            .or(targetPathInput.styleProperty().isEqualTo(badStyle))
                            .or(Bindings.isEmpty(sourceFilesInformation))
            );
        } catch (Exception e) {
            logger.error(e.toString());
        }
    }

    public void applyKernel(ConvolutionKernel kernel) {
        if (effectType != OperationType.Convolution || stringBox == null) {
            return;
        }
        convolutionRadio.fire();
        if (stringBox.getItems().contains(kernel.getName())) {
            stringBox.getSelectionModel().select(kernel.getName());
        } else {
            stringBox.getSelectionModel().select(-1);
        }
        this.kernel = kernel;
        okAction();
    }

    public void loadKernelsList(List<ConvolutionKernel> records) {
        if (effectType != OperationType.Convolution || stringBox == null) {
            return;
        }
        kernels = records;
        stringBox.getItems().clear();
        if (kernels != null && !kernels.isEmpty()) {
            List<String> names = new ArrayList<>();
            for (ConvolutionKernel k : kernels) {
                names.add(k.getName());
            }
            stringBox.getItems().addAll(names);
            stringBox.getSelectionModel().select(0);
            stringBox.getEditor().setStyle(null);
        } else {
            stringBox.getEditor().setStyle(badStyle);
        }
    }

    @Override
    protected void keyEventsHandler(KeyEvent event) {
        super.keyEventsHandler(event);
        if (event.isControlDown()) {
            String key = event.getText();
            if (key == null || key.isEmpty()) {
                return;
            }
            switch (key) {
                case "k":
                case "K":
                    if (stringBox != null) {
                        stringBox.show();
                    }
                    break;
            }
        }
    }

    @Override
    protected BufferedImage handleImage(BufferedImage source) {
        try {
            BufferedImage target = null;
            PixelsOperation pixelsOperation;
            ImageConvolution imageConvolution;
            if (null != effectType) {
                switch (effectType) {
                    case Contrast:
                        ImageContrast imageContrast = new ImageContrast(source, contrastAlgorithm);
                        imageContrast.setIntPara1(intPara1);
                        target = imageContrast.operate();
                        break;
                    case Convolution:
                        if (kernel == null) {
                            int index = stringBox.getSelectionModel().getSelectedIndex();
                            if (kernels == null || kernels.isEmpty() || index < 0) {
                                return null;
                            }
                            kernel = kernels.get(index);
                        }
                        imageConvolution = new ImageConvolution(source, kernel);
                        target = imageConvolution.operate();
                        break;
                    case Blur:
                        if (kernel == null) {
                            return null;
                        }
                        imageConvolution = new ImageConvolution(source, kernel);
                        target = imageConvolution.operate();
                        break;
                    case Sharpen:
                        kernel = ConvolutionKernel.makeSharpen3b();
                        imageConvolution = new ImageConvolution(source, kernel);
                        target = imageConvolution.operate();
                        break;
                    case Clarity:
                        kernel = ConvolutionKernel.makeUnsharpMasking5();
                        imageConvolution = new ImageConvolution(source, kernel);
                        target = imageConvolution.operate();
                        break;
                    case EdgeDetect:
                        kernel = ConvolutionKernel.makeEdgeDetection3b();
                        imageConvolution = new ImageConvolution(source, kernel);
                        target = imageConvolution.operate();
                        break;
                    case Emboss:
                        kernel = ConvolutionKernel.makeEmbossKernel(intPara1, intPara2, valueCheck.isSelected());
                        imageConvolution = new ImageConvolution(source, kernel);
                        target = imageConvolution.operate();
                        break;
                    case Thresholding:
                        pixelsOperation = new PixelsOperation(ImageConvert.removeAlpha(source), effectType);
                        pixelsOperation.setIntPara1(intPara1);
                        pixelsOperation.setIntPara2(intPara2);
                        pixelsOperation.setIntPara3(intPara3);
                        target = pixelsOperation.operate();
                        break;
                    case Quantization:
                        int channelSize = (int) Math.round(Math.pow(intPara1, 1.0 / 3.0));
                        ImageQuantization quantization = new ImageQuantization(ImageConvert.removeAlpha(source),
                                quantizationAlgorithm, channelSize);
                        quantization.setIsDithering(valueCheck.isSelected());
                        target = quantization.operate();
                        break;
                    case Gray:
                        target = ImageGray.byteGray(source);
                        break;
                    case BlackOrWhite:
                        ImageBinary imageBinary;
                        switch (intPara1) {
                            case 2:
                                imageBinary = new ImageBinary(source, -1);
                                break;
                            case 3:
                                imageBinary = new ImageBinary(source, intPara2);
                                break;
                            default:
                                int t = ImageBinary.calculateThreshold(source);
                                imageBinary = new ImageBinary(source, t);
                                break;
                        }
                        imageBinary.setIsDithering(valueCheck.isSelected());
                        target = imageBinary.operate();
                        break;
                    case Sepia:
                        pixelsOperation = new PixelsOperation(source, effectType);
                        pixelsOperation.setIntPara1(intPara1);
                        target = pixelsOperation.operate();
                        break;
                    default:
                        break;
                }
            }
            return target;
        } catch (Exception e) {
            logger.error(e.toString());
            return null;
        }

    }

}
