package mara.mybox.controller;

import com.ibm.icu.text.MessageFormat;
import com.vladsch.flexmark.ext.tables.TablesExtension;
import com.vladsch.flexmark.html.HtmlRenderer;
import com.vladsch.flexmark.parser.Parser;
import com.vladsch.flexmark.parser.ParserEmulationProfile;
import com.vladsch.flexmark.util.ast.Node;
import com.vladsch.flexmark.util.data.MutableDataHolder;
import com.vladsch.flexmark.util.data.MutableDataSet;
import java.io.File;
import java.util.Arrays;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;
import mara.mybox.data.VisitHistory;
import mara.mybox.tools.FileTools;
import mara.mybox.tools.HtmlTools;
import mara.mybox.value.AppVariables;
import static mara.mybox.value.AppVariables.logger;
import static mara.mybox.value.AppVariables.message;
import mara.mybox.value.CommonFxValues;

/**
 * @Author Mara
 * @CreateDate 2019-10-29
 * @License Apache License Version 2.0
 */
public class MarkdownToHtmlController extends FilesBatchController {

    protected Parser parser;
    protected HtmlRenderer renderer;
    protected MutableDataHolder parserOptions;
    protected int indentSize = 4;

    @FXML
    protected ComboBox<String> emulationSelector, indentSelector, styleSelector;
    @FXML
    protected CheckBox trimCheck, appendCheck, discardCheck, linesCheck;
    @FXML
    protected TextField titleInput;

    public MarkdownToHtmlController() {
        baseTitle = AppVariables.message("MarkdownToHtml");

        SourceFileType = VisitHistory.FileType.Markdown;
        SourcePathType = VisitHistory.FileType.Markdown;
        TargetPathType = VisitHistory.FileType.Html;
        TargetFileType = VisitHistory.FileType.Html;
        AddFileType = VisitHistory.FileType.Markdown;
        AddPathType = VisitHistory.FileType.Markdown;

        sourcePathKey = "MarkdownFilePath";
        targetPathKey = "HtmlFilePath";

        sourceExtensionFilter = CommonFxValues.MarkdownExtensionFilter;
        targetExtensionFilter = CommonFxValues.HtmlExtensionFilter;
    }

    @Override
    public void initControls() {
        try {
            super.initControls();

            emulationSelector.getItems().addAll(Arrays.asList(
                    "GITHUB", "MARKDOWN", "GITHUB_DOC", "COMMONMARK", "KRAMDOWN", "PEGDOWN",
                    "FIXED_INDENT", "MULTI_MARKDOWN", "PEGDOWN_STRICT"
            ));
            emulationSelector.getSelectionModel().select(0);

            indentSelector.getItems().addAll(Arrays.asList(
                    "4", "2", "0", "6", "8"
            ));
            indentSelector.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<String>() {
                @Override
                public void changed(ObservableValue ov, String oldValue, String newValue) {
                    try {
                        int v = Integer.parseInt(newValue);
                        if (v >= 0) {
                            indentSize = v;
                        }
                    } catch (Exception e) {
                    }
                }
            });
            indentSelector.getSelectionModel().select(0);

            styleSelector.getItems().addAll(Arrays.asList(
                    message("DefaultStyle"), message("ConsoleStyle"), message("None")
            ));
            styleSelector.getSelectionModel().select(0);

        } catch (Exception e) {
            logger.debug(e.toString());
        }
    }

    @Override
    public boolean makeMoreParameters() {
        try {
            parserOptions = new MutableDataSet();
            parserOptions.setFrom(ParserEmulationProfile.valueOf(emulationSelector.getValue()));
            parserOptions.set(Parser.EXTENSIONS, Arrays.asList(
                    //                    AbbreviationExtension.create(),
                    //                    DefinitionExtension.create(),
                    //                    FootnoteExtension.create(),
                    //                    TypographicExtension.create(),
                    TablesExtension.create()
            ));
            parserOptions.set(HtmlRenderer.INDENT_SIZE, indentSize)
                    //                    .set(HtmlRenderer.PERCENT_ENCODE_URLS, true)
                    //                    .set(TablesExtension.COLUMN_SPANS, false)
                    .set(TablesExtension.TRIM_CELL_WHITESPACE, trimCheck.isSelected())
                    .set(TablesExtension.APPEND_MISSING_COLUMNS, appendCheck.isSelected())
                    .set(TablesExtension.DISCARD_EXTRA_COLUMNS, discardCheck.isSelected())
                    .set(TablesExtension.APPEND_MISSING_COLUMNS, appendCheck.isSelected());

            parser = Parser.builder(parserOptions).build();
            renderer = HtmlRenderer.builder(parserOptions).build();
        } catch (Exception e) {
            logger.error(e.toString());
            return false;
        }

        return super.makeMoreParameters();
    }

    @Override
    public boolean matchType(File file) {
        String suffix = FileTools.getFileSuffix(file.getName());
        if (suffix == null) {
            return false;
        }
        suffix = suffix.trim().toLowerCase();
        return "md".equals(suffix);
    }

    @Override
    public String handleFile(File srcFile, File targetPath) {
        try {
            countHandling(srcFile);
            File target = makeTargetFile(srcFile, targetPath);
            if (target == null) {
                return AppVariables.message("Skip");
            }
            Node document = parser.parse(FileTools.readTexts(srcFile));
            String html = renderer.render(document);
            String style;
            if (message("ConsoleStyle").equals(styleSelector.getValue())) {
                style = HtmlTools.ConsoleStyle;
            } else if (message("DefaultStyle").equals(styleSelector.getValue())) {
                style = HtmlTools.DefaultStyle;
            } else {
                style = null;
            }
            html = HtmlTools.html(titleInput.getText(), style, html);

            FileTools.writeFile(target, html);
            if (verboseCheck == null || verboseCheck.isSelected()) {
                updateLogs(MessageFormat.format(message("ConvertSuccessfully"),
                        srcFile.getAbsolutePath(), target.getAbsolutePath()));
            }
            targetFileGenerated(target);
            return AppVariables.message("Successful");
        } catch (Exception e) {
            logger.error(e.toString());
            return AppVariables.message("Failed");
        }
    }

    @Override
    public File makeTargetFile(File sourceFile, File targetPath) {
        try {
            String namePrefix = FileTools.getFilePrefix(sourceFile.getName());
            String nameSuffix = "";
            if (sourceFile.isFile()) {
                nameSuffix = ".html";
            }
            return makeTargetFile(namePrefix, nameSuffix, targetPath);
        } catch (Exception e) {
            return null;
        }
    }

}
