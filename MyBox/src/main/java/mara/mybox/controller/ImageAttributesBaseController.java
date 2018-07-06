package mara.mybox.controller;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TextField;
import javafx.scene.control.Toggle;
import javafx.scene.control.ToggleGroup;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.HBox;
import static mara.mybox.controller.BaseController.logger;
import mara.mybox.objects.AppVaribles;
import static mara.mybox.objects.AppVaribles.getMessage;
import mara.mybox.objects.ImageAttributes;
import mara.mybox.tools.FxmlTools;
import mara.mybox.image.ImageValueTools;
import org.apache.pdfbox.rendering.ImageType;

/**
 * @Author Mara
 * @CreateDate 2018-6-26
 * @Description
 * @License Apache License Version 2.0
 */
public class ImageAttributesBaseController extends BaseController {

    @FXML
    protected ToggleGroup ImageFormatGroup, ImageColorGroup, QualityGroup, CompressionGroup, ColorConversionGroup;
    @FXML
    protected RadioButton RGB, ARGB, Gray, Binary, fullQuality;
    @FXML
    protected TextField qualityInput, thresholdInput;
    @FXML
    protected HBox qualityBox, compressBox, colorBox;
    @FXML
    protected RadioButton rawSelect, pcxSelect;

    protected ImageAttributes attributes = new ImageAttributes();

    public static class ColorConversion {

        public static int DEFAULT = 0;
        public static int OTSU = 1;
        public static int THRESHOLD = 9;
    }

    @Override
    protected void initializeNext() {
        attributes = new ImageAttributes();

        ImageFormatGroup.selectedToggleProperty().addListener(new ChangeListener<Toggle>() {
            @Override
            public void changed(ObservableValue<? extends Toggle> ov,
                    Toggle old_toggle, Toggle new_toggle) {
                checkImageFormat();
            }
        });
        FxmlTools.setRadioSelected(ImageFormatGroup, AppVaribles.getConfigValue("imageFormat", "png"));
        checkImageFormat();

        ImageColorGroup.selectedToggleProperty().addListener(new ChangeListener<Toggle>() {
            @Override
            public void changed(ObservableValue<? extends Toggle> ov,
                    Toggle old_toggle, Toggle new_toggle) {
                checkImageColor();
            }
        });
        FxmlTools.setRadioSelected(ImageColorGroup, AppVaribles.getConfigValue("imageColor", AppVaribles.getMessage("Color")));
        checkImageColor();

        QualityGroup.selectedToggleProperty().addListener(new ChangeListener<Toggle>() {
            @Override
            public void changed(ObservableValue<? extends Toggle> ov,
                    Toggle old_toggle, Toggle new_toggle) {
                checkQuality();
            }
        });
        FxmlTools.setRadioSelected(QualityGroup, AppVaribles.getConfigValue("quality", "100%"));
        checkQuality();

        qualityInput.textProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> observable,
                    String oldValue, String newValue) {
                checkQuality();
            }
        });
        qualityInput.setText(AppVaribles.getConfigValue("qualityInput", null));

        ColorConversionGroup.selectedToggleProperty().addListener(new ChangeListener<Toggle>() {
            @Override
            public void changed(ObservableValue<? extends Toggle> ov,
                    Toggle old_toggle, Toggle new_toggle) {
                checkColorConversion();
            }
        });
        FxmlTools.setRadioSelected(ColorConversionGroup, AppVaribles.getConfigValue("colorConversion", AppVaribles.getMessage("Default")));

        thresholdInput.textProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> observable,
                    String oldValue, String newValue) {
                checkColorConversion();
            }
        });
        thresholdInput.setText(AppVaribles.getConfigValue("thresholdInput", null));

        if (pcxSelect != null) {
            FxmlTools.quickTooltip(pcxSelect, new Tooltip(getMessage("PcxComments")));
        }

        initializeNext2();
    }

    protected void initializeNext2() {

    }

    protected void checkImageFormat() {
        try {
            RadioButton selected = (RadioButton) ImageFormatGroup.getSelectedToggle();
            String imageFormat = selected.getText();
            attributes.setImageFormat(imageFormat);
            AppVaribles.setConfigValue("imageFormat", imageFormat);

            String[] compressionTypes = ImageValueTools.getCompressionTypes(imageFormat, attributes.getColorSpace());
            checkCompressionTypes(compressionTypes);

            if (compressionTypes != null && "jpg".equals(imageFormat)) {
                qualityBox.setDisable(false);
//                if (qualityInput.getStyle().equals(FxmlTools.badStyle)) {
//                    fullQuality.setSelected(true);
//                }
            } else {
                qualityInput.setStyle(null);
                qualityBox.setDisable(true);
                fullQuality.setSelected(true);
            }

            switch (imageFormat) {
                case "wbmp":
                    Binary.setDisable(false);
                    RGB.setDisable(true);
                    ARGB.setDisable(true);
                    Gray.setDisable(true);
                    Binary.setSelected(true);
                    break;
                case "jpg":
                case "bmp":
                case "pnm":
                    ARGB.setDisable(true);
                    Binary.setDisable(false);
                    RGB.setDisable(false);
                    Gray.setDisable(false);
                    if (ARGB.isSelected()) {
                        RGB.setSelected(true);
                    }
                    break;
                default:
                    ARGB.setDisable(false);
                    Binary.setDisable(false);
                    RGB.setDisable(false);
                    Gray.setDisable(false);
                    break;
            }

        } catch (Exception e) {
            logger.error(e.toString());
        }
    }

    protected void checkImageColor() {
        try {
            RadioButton selected = (RadioButton) ImageColorGroup.getSelectedToggle();
            String s = selected.getText();
            AppVaribles.setConfigValue("imageColor", s);
            if (getMessage("Color").equals(s)) {
                attributes.setColorSpace(ImageType.RGB);
            } else if (getMessage("ColorAlpha").equals(s)) {
                attributes.setColorSpace(ImageType.ARGB);
            } else if (getMessage("ShadesOfGray").equals(s)) {
                attributes.setColorSpace(ImageType.GRAY);
            } else if (getMessage("BlackOrWhite").equals(s)) {
                attributes.setColorSpace(ImageType.BINARY);
            } else {
                attributes.setColorSpace(ImageType.RGB);
            }

//            if ("tif".equals(imageFormat) || "bmp".equals(imageFormat)) {
            String[] compressionTypes = ImageValueTools.getCompressionTypes(attributes.getImageFormat(), attributes.getColorSpace());
            checkCompressionTypes(compressionTypes);
//            }

            if (attributes.getColorSpace() == ImageType.BINARY) {
                colorBox.setDisable(false);
                checkColorConversion();
            } else {
                colorBox.setDisable(true);
                if (thresholdInput.getStyle().equals(FxmlTools.badStyle)) {
                    FxmlTools.setRadioFirstSelected(ColorConversionGroup);
                }
            }

//            logger.debug(imageColor);
        } catch (Exception e) {
            logger.error(e.toString());
        }
    }

    protected void checkCompressionTypes(String[] types) {
        compressBox.getChildren().removeAll(compressBox.getChildren());
        CompressionGroup = new ToggleGroup();
        if (types == null) {
            RadioButton newv = new RadioButton(AppVaribles.getMessage("None"));
            newv.setToggleGroup(CompressionGroup);
            compressBox.getChildren().add(newv);
            newv.setSelected(true);
        } else {
            boolean cSelected = false;
            for (String ctype : types) {
                RadioButton newv = new RadioButton(ctype);
                newv.setToggleGroup(CompressionGroup);
                compressBox.getChildren().add(newv);
                if (!cSelected) {
                    newv.setSelected(true);
                    cSelected = true;
                }
            }
        }

        CompressionGroup.selectedToggleProperty().addListener(new ChangeListener<Toggle>() {
            @Override
            public void changed(ObservableValue<? extends Toggle> ov,
                    Toggle old_toggle, Toggle new_toggle) {
                checkCompressionType();
            }
        });
        FxmlTools.setRadioSelected(CompressionGroup, AppVaribles.getConfigValue("compressionType", AppVaribles.getMessage("None")));
        checkCompressionType();
    }

    protected void checkCompressionType() {
        try {
            RadioButton selected = (RadioButton) CompressionGroup.getSelectedToggle();
            attributes.setCompressionType(selected.getText());
            AppVaribles.setConfigValue("compressionType", selected.getText());
        } catch (Exception e) {
            attributes.setCompressionType(null);
        }
    }

    protected void checkColorConversion() {
        thresholdInput.setStyle(null);
        try {
            RadioButton selected = (RadioButton) ColorConversionGroup.getSelectedToggle();
            String s = selected.getText();

            if (getMessage("Threshold").equals(s)) {
                attributes.setBinaryConversion(ImageAttributes.BinaryConversion.BINARY_THRESHOLD);
            } else if (getMessage("OTSU").equals(s)) {
                attributes.setBinaryConversion(ImageAttributes.BinaryConversion.BINARY_OTSU);
                AppVaribles.setConfigValue("colorConversion", s);
            } else {
                attributes.setBinaryConversion(ImageAttributes.BinaryConversion.DEFAULT);
                AppVaribles.setConfigValue("colorConversion", s);
            }

            int inputValue;
            try {
                inputValue = Integer.parseInt(thresholdInput.getText());
                if (inputValue >= 0 && inputValue <= 100) {
                    AppVaribles.setConfigValue("thresholdInput", inputValue + "");
                } else {
                    inputValue = -1;
                }
            } catch (Exception e) {
                inputValue = -1;
            }

            if (attributes.getBinaryConversion() == ImageAttributes.BinaryConversion.BINARY_THRESHOLD) {
                if (inputValue >= 0) {
                    attributes.setThreshold(inputValue);
                    AppVaribles.setConfigValue("colorConversion", s);
                } else {
                    thresholdInput.setStyle(FxmlTools.badStyle);
                }
            }
        } catch (Exception e) {
            logger.error(e.toString());
        }
    }

    protected void checkQuality() {
        try {
            RadioButton selected = (RadioButton) ImageFormatGroup.getSelectedToggle();
            if ("jpg".equals(selected.getText())) {
                qualityBox.setDisable(false);
            } else {
                qualityInput.setStyle(null);
                qualityBox.setDisable(true);
                fullQuality.setSelected(true);
                attributes.setQuality(100);
                return;
            }
            selected = (RadioButton) QualityGroup.getSelectedToggle();
            String s = selected.getText();
            qualityInput.setStyle(null);
            int inputValue;
            try {
                inputValue = Integer.parseInt(qualityInput.getText());
                if (inputValue >= 0 && inputValue <= 100) {
                    AppVaribles.setConfigValue("qualityInput", inputValue + "");
                } else {
                    inputValue = -1;
                }
            } catch (Exception e) {
                inputValue = -1;
            }
            if (getMessage("InputValue").equals(s)) {
                if (inputValue >= 0) {
                    attributes.setQuality(inputValue);
                    AppVaribles.setConfigValue("quality", s);
                } else {
                    qualityInput.setStyle(FxmlTools.badStyle);
                }
            } else {
                attributes.setQuality(Integer.parseInt(s.substring(0, s.length() - 1)));
                AppVaribles.setConfigValue("quality", s);
            }
        } catch (Exception e) {
            logger.error(e.toString());
        }
    }

    public ToggleGroup getImageFormatGroup() {
        return ImageFormatGroup;
    }

    public void setImageFormatGroup(ToggleGroup ImageFormatGroup) {
        this.ImageFormatGroup = ImageFormatGroup;
    }

    public RadioButton getRGB() {
        return RGB;
    }

    public void setRGB(RadioButton RGB) {
        this.RGB = RGB;
    }

    public ToggleGroup getImageColorGroup() {
        return ImageColorGroup;
    }

    public void setImageColorGroup(ToggleGroup ImageColorGroup) {
        this.ImageColorGroup = ImageColorGroup;
    }

    public RadioButton getARGB() {
        return ARGB;
    }

    public void setARGB(RadioButton ARGB) {
        this.ARGB = ARGB;
    }

    public HBox getQualityBox() {
        return qualityBox;
    }

    public void setQualityBox(HBox qualityBox) {
        this.qualityBox = qualityBox;
    }

    public ToggleGroup getQualityGroup() {
        return QualityGroup;
    }

    public void setQualityGroup(ToggleGroup QualityGroup) {
        this.QualityGroup = QualityGroup;
    }

    public TextField getQualityInput() {
        return qualityInput;
    }

    public void setQualityInput(TextField qualityInput) {
        this.qualityInput = qualityInput;
    }

    public HBox getCompressBox() {
        return compressBox;
    }

    public void setCompressBox(HBox compressBox) {
        this.compressBox = compressBox;
    }

    public ToggleGroup getCompressionGroup() {
        return CompressionGroup;
    }

    public void setCompressionGroup(ToggleGroup CompressionGroup) {
        this.CompressionGroup = CompressionGroup;
    }

    public HBox getColorBox() {
        return colorBox;
    }

    public void setColorBox(HBox colorBox) {
        this.colorBox = colorBox;
    }

    public ToggleGroup getColorConversionGroup() {
        return ColorConversionGroup;
    }

    public void setColorConversionGroup(ToggleGroup ColorConversionGroup) {
        this.ColorConversionGroup = ColorConversionGroup;
    }

    public TextField getThresholdInput() {
        return thresholdInput;
    }

    public void setThresholdInput(TextField thresholdInput) {
        this.thresholdInput = thresholdInput;
    }

    public ImageAttributes getAttributes() {
        return attributes;
    }

    public void setAttributes(ImageAttributes attributes) {
        this.attributes = attributes;
    }

    public RadioButton getRawSelect() {
        return rawSelect;
    }

    public void setRawSelect(RadioButton rawSelect) {
        this.rawSelect = rawSelect;
    }

}
