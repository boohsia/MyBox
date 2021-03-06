package mara.mybox.data;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import mara.mybox.tools.DateTools;
import mara.mybox.tools.FileTools;
import mara.mybox.value.AppVariables;
import static mara.mybox.value.AppVariables.logger;
import static mara.mybox.value.AppVariables.message;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.csv.CSVRecord;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFCellStyle;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

/**
 * @Author Mara
 * @CreateDate 2020-2-2
 * @License Apache License Version 2.0
 */
public class EpidemicReport {

    protected long epid = -1, locationid = -1;
    protected String dataSet, locationFullName;
    private GeographyCode location;
    protected double healedConfirmedPermillage, deadConfirmedPermillage,
            confirmedPopulationPermillage, deadPopulationPermillage, healedPopulationPermillage,
            confirmedAreaPermillage, deadAreaPermillage, healedAreaPermillage;
    protected long confirmed, healed, dead,
            increasedConfirmed, increasedHealed, increasedDead;
    protected long time = -1;
    protected int source;
    protected String sourceName;
    protected SourceType sourceType;
    public static String COVID19JHU = "COVID-19_JHU";
    public static String COVID19TIME = " 23:59:00";

    public enum ValueName {
        Confirmed, Healed, Dead,
        IncreasedConfirmed, IncreasedHealed, IncreasedDead
    }

    // 1:predefined 2:inputted 3:filled 4:statistic others:unknown
    public enum SourceType {
        PredefinedData, InputtedData, FilledData, StatisticData, Unknown
    }

    public EpidemicReport() {
        epid = -1;
        locationid = -1;
        dataSet = null;
        location = null;
        healedConfirmedPermillage = deadConfirmedPermillage = 0;
        confirmed = healed = dead = increasedConfirmed
                = increasedHealed = increasedDead = 0;
        healedConfirmedPermillage = deadConfirmedPermillage
                = confirmedPopulationPermillage = deadPopulationPermillage = healedPopulationPermillage
                = confirmedAreaPermillage = deadAreaPermillage = healedAreaPermillage = 0;
        time = -1;
        source = 2;
    }

    public boolean isValid() {
        return getLocationid() > 0
                && (confirmed > 0 || healed > 0 || dead > 0);
    }

    public static EpidemicReport create() {
        return new EpidemicReport();
    }

    // Only copy base attributes.
    public EpidemicReport copy() {
        try {
            EpidemicReport cloned = EpidemicReport.create()
                    .setDataSet(dataSet).setLocation(getLocation()).setLocationid(getLocationid())
                    .setConfirmed(confirmed).setHealed(healed).setDead(dead)
                    .setTime(time).setSource(source);
            return cloned;
        } catch (Exception e) {
            return null;
        }
    }

    public Number value(String name) {
        try {
            if (name == null || name.isBlank()) {
                return null;
            }
            if (message("Confirmed").equals(name) || "confirmed".equals(name)) {
                return getConfirmed();

            } else if (message("Healed").equals(name) || "healed".equals(name)) {
                return getHealed();

            } else if (message("Dead").equals(name) || "dead".equals(name)) {
                return getDead();

            } else if (message("IncreasedConfirmed").equals(name) || "increased_confirmed".equals(name)) {
                return getIncreasedConfirmed();

            } else if (message("IncreasedHealed").equals(name) || "increased_healed".equals(name)) {
                return getIncreasedHealed();

            } else if (message("IncreasedDead").equals(name) || "increased_dead".equals(name)) {
                return getIncreasedDead();

            } else if (message("HealedConfirmedPermillage").equals(name) || "healed_confirmed_permillage".equals(name)) {
                return getHealedConfirmedPermillage();

            } else if (message("DeadConfirmedPermillage").equals(name) || "dead_confirmed_permillage".equals(name)) {
                return getDeadConfirmedPermillage();

            } else if (message("ConfirmedPopulationPermillage").equals(name) || "confirmed_population_permillage".equals(name)) {
                return getConfirmedPopulationPermillage();

            } else if (message("HealedPopulationPermillage").equals(name) || "healed_population_permillage".equals(name)) {
                return getHealedPopulationPermillage();

            } else if (message("DeadPopulationPermillage").equals(name) || "dead_population_permillage".equals(name)) {
                return getDeadPopulationPermillage();

            } else if (message("ConfirmedAreaPermillage").equals(name) || "confirmed_area_permillage".equals(name)) {
                return getConfirmedAreaPermillage();

            } else if (message("HealedAreaPermillage").equals(name) || "healed_area_permillage".equals(name)) {
                return getHealedAreaPermillage();

            } else if (message("DeadAreaPermillage").equals(name) || "dead_area_permillage".equals(name)) {
                return getDeadAreaPermillage();

            } else {
                return null;
            }

        } catch (Exception e) {
            logger.debug(e.toString());
            return null;
        }
    }

//    public String sourceAbbreviation() {
//        switch (source) {
//            case 1:
//                return "P";
//            case 2:
//                return "I";
//            case 3:
//                return "F";
//            case 4:
//                return "S";
//            default:
//                return "U";
//        }
//    }
//
//    public static int abbreviationValue(String source) {
//        switch (source) {
//            case "P":
//                return 1;
//            case "I":
//                return 2;
//            case "F":
//                return 3;
//            case "S":
//                return 4;
//            default:
//                return -1;
//        }
//    }

    /*
        Static methods
     */
    public static void sortAsTimeAscent(List<EpidemicReport> reports) {
        if (reports == null || reports.isEmpty()) {
            return;
        }
        Collections.sort(reports, (EpidemicReport r1, EpidemicReport r2) -> {
            long diff = r1.getTime() - r2.getTime();
            if (diff > 0) {
                return 1;
            } else if (diff < 0) {
                return -1;
            } else {
                return 0;
            }
        });
    }

    public static SourceType sourceType(int value) {
        switch (value) {
            case 1:
                return SourceType.PredefinedData;
            case 2:
                return SourceType.InputtedData;
            case 3:
                return SourceType.FilledData;
            case 4:
                return SourceType.StatisticData;
            default:
                return SourceType.Unknown;
        }
    }

    public static String sourceName(int value) {
        return sourceType(value).name();
    }

    public static int source(SourceType source) {
        return sourceValue(source.name());
    }

    public static int sourceValue(String source) {
        return sourceValue(AppVariables.getLanguage(), source);
    }

    public static int sourceValue(String lang, String source) {
        if (message(lang, "PredefinedData").equals(source) || "PredefinedData".equals(source)) {
            return 1;
        } else if (message(lang, "InputtedData").equals(source) || "InputtedData".equals(source)) {
            return 2;
        } else if (message(lang, "FilledData").equals(source) || "FilledData".equals(source)) {
            return 3;
        } else if (message(lang, "StatisticData").equals(source) || "StatisticData".equals(source)) {
            return 4;
        } else {
            return 0;
        }
    }

    public static int PredefinedData() {
        return source(SourceType.PredefinedData);
    }

    public static int InputtedData() {
        return source(SourceType.InputtedData);
    }

    public static int FilledData() {
        return source(SourceType.FilledData);
    }

    public static int StatisticData() {
        return source(SourceType.StatisticData);
    }

    /*
        import
     */
    // DataSet,Time,Locationid,Confirmed,Healed,Dead,IncreasedConfirmed,IncreasedHealed,IncreasedDead,DataSource
    public static EpidemicReport readIntenalRecord(List<String> names, CSVRecord record) {
        try {
            EpidemicReport report = new EpidemicReport();
            report.setDataSet(record.get("DataSet"));
            report.setTime(DateTools.stringToDatetime(record.get("Time")).getTime());
            report.setLocationid(Long.valueOf(record.get("Locationid")));
            report.setConfirmed(Long.valueOf(record.get("Confirmed")));
            report.setHealed(Long.valueOf(record.get("Healed")));
            report.setDead(Long.valueOf(record.get("Dead")));
            report.setIncreasedConfirmed(Long.valueOf(record.get("IncreasedConfirmed")));
            report.setIncreasedHealed(Long.valueOf(record.get("IncreasedHealed")));
            report.setIncreasedDead(Long.valueOf(record.get("IncreasedDead")));
            report.setSource(Integer.valueOf(record.get("DataSource")));
            return report;
        } catch (Exception e) {
            logger.debug(e.toString());
            return null;
        }
    }

    // Data Set,Time,Confirmed,Healed,Dead,Increased Confirmed,Increased Healed,Increased Dead,Data Source,
    // Level,Continent,Country,Province,City,County,Town,Village,Building,Longitude,Latitude
    // 数据集,时间,确认,治愈,死亡,新增确诊,新增治愈,新增死亡,数据源,级别,洲,国家,省,市,区县,乡镇,村庄,建筑物,经度,纬度
    public static Map<String, Object> readExtenalRecord(Connection conn,
            PreparedStatement geoInsert, String lang, List<String> names, CSVRecord record) {
        Map<String, Object> ret = new HashMap<>();
        try {
            EpidemicReport report = new EpidemicReport();
            if (names.contains("DataSet")) {
                report.setDataSet(record.get("DataSet"));
            } else if (names.contains(message(lang, "DataSet"))) {
                report.setDataSet(record.get(message(lang, "DataSet")));
            } else {
                ret.put("message", "Miss DataSet");
                return ret;
            }
            if (names.contains("Time")) {
                try {
                    report.setTime(DateTools.stringToDatetime(record.get("Time")).getTime());
                } catch (Exception e) {
                    ret.put("message", "Miss Time1");
                    return ret;
                }
            } else if (names.contains(message(lang, "Time"))) {
                try {
                    report.setTime(DateTools.stringToDatetime(record.get(message(lang, "Time"))).getTime());
                } catch (Exception e) {
                    ret.put("message", "Miss Time2");
                    return ret;
                }
            } else {
                ret.put("message", "Miss Time3");
                return ret;
            }
            if (names.contains("Confirmed")) {
                try {
                    report.setConfirmed(Long.valueOf(record.get("Confirmed")));
                } catch (Exception e) {
                }
            } else if (names.contains(message(lang, "Confirmed"))) {
                try {
                    report.setConfirmed(Long.valueOf(record.get(message(lang, "Confirmed"))));
                } catch (Exception e) {
                }
            }
            if (names.contains("Healed")) {
                try {
                    report.setHealed(Long.valueOf(record.get("Healed")));
                } catch (Exception e) {
                }
            } else if (names.contains(message(lang, "Healed"))) {
                try {
                    report.setHealed(Long.valueOf(record.get(message(lang, "Healed"))));
                } catch (Exception e) {
                }
            }
            if (names.contains("Dead")) {
                try {
                    report.setDead(Long.valueOf(record.get("Dead")));
                } catch (Exception e) {
                }
            } else if (names.contains(message(lang, "Dead"))) {
                try {
                    report.setDead(Long.valueOf(record.get(message(lang, "Dead"))));
                } catch (Exception e) {
                }
            }
            if (names.contains("IncreasedConfirmed")) {
                try {
                    report.setIncreasedConfirmed(Long.valueOf(record.get("IncreasedConfirmed")));
                } catch (Exception e) {
                }
            } else if (names.contains(message(lang, "IncreasedConfirmed"))) {
                try {
                    report.setIncreasedConfirmed(Long.valueOf(record.get(message(lang, "IncreasedConfirmed"))));
                } catch (Exception e) {
                }
            }
            if (names.contains("IncreasedHealed")) {
                try {
                    report.setIncreasedHealed(Long.valueOf(record.get("IncreasedHealed")));
                } catch (Exception e) {
                }
            } else if (names.contains(message(lang, "IncreasedHealed"))) {
                try {
                    report.setIncreasedHealed(Long.valueOf(record.get(message(lang, "IncreasedHealed"))));
                } catch (Exception e) {
                }
            }
            if (names.contains("IncreasedDead")) {
                try {
                    report.setIncreasedDead(Long.valueOf(record.get("IncreasedDead")));
                } catch (Exception e) {
                }
            } else if (names.contains(message(lang, "IncreasedDead"))) {
                try {
                    report.setIncreasedDead(Long.valueOf(record.get(message(lang, "IncreasedDead"))));
                } catch (Exception e) {
                }
            }
            if (names.contains("DataSource")) {
                try {
                    report.setSource(sourceValue(lang, record.get("DataSource")));
                } catch (Exception e) {
                }
            } else if (names.contains(message(lang, "DataSource"))) {
                try {
                    report.setSource(sourceValue(lang, record.get(message(lang, "DataSource"))));
                } catch (Exception e) {
                }
            }
            GeographyCodeLevel levelCode;
            if (names.contains("Level")) {
                levelCode = new GeographyCodeLevel(record.get("Level"));
            } else if (names.contains(message(lang, "Level"))) {
                levelCode = new GeographyCodeLevel(record.get(message(lang, "Level")));
            } else {
                ret.put("message", "Miss level");
                return ret;
            }
            double longitude = -200;
            if (names.contains("Longitude")) {
                try {
                    longitude = Double.valueOf(record.get("Longitude"));
                } catch (Exception e) {
                }
            } else if (names.contains(message(lang, "Longitude"))) {
                try {
                    longitude = Double.valueOf(record.get(message(lang, "Longitude")));
                } catch (Exception e) {
                }
            }
            double latitude = -200;
            if (names.contains("Latitude")) {
                try {
                    latitude = Double.valueOf(record.get("Latitude"));
                } catch (Exception e) {
                }
            } else if (names.contains(message(lang, "Latitude"))) {
                try {
                    latitude = Double.valueOf(record.get(message(lang, "latitude")));
                } catch (Exception e) {
                }
            }
            String continent = names.contains(message(lang, "Continent")) ? record.get(message(lang, "Continent"))
                    : (names.contains("Continent") ? record.get("Continent") : null);
            String country = names.contains(message(lang, "Country")) ? record.get(message(lang, "Country"))
                    : (names.contains("Country") ? record.get("Country") : null);
            String province = names.contains(message(lang, "Province")) ? record.get(message(lang, "Province"))
                    : (names.contains("Province") ? record.get("Province") : null);
            String city = names.contains(message(lang, "City")) ? record.get(message(lang, "City"))
                    : (names.contains("City") ? record.get("City") : null);
            String county = names.contains(message(lang, "County")) ? record.get(message(lang, "County"))
                    : (names.contains("County") ? record.get("County") : null);
            String town = names.contains(message(lang, "Town")) ? record.get(message(lang, "Town"))
                    : (names.contains("Town") ? record.get("Town") : null);
            String village = names.contains(message(lang, "Village")) ? record.get(message(lang, "Village"))
                    : (names.contains("Village") ? record.get("Village") : null);
            String building = names.contains(message(lang, "Building")) ? record.get(message(lang, "Building"))
                    : (names.contains("Building") ? record.get("Building") : null);
            Map<String, Object> codeRet = GeographyCode.code(conn, geoInsert,
                    levelCode.getLevel(), longitude, latitude, continent, country, province, city,
                    county, town, village, building, true);
            if (codeRet.get("message") != null) {
                String msg = (String) codeRet.get("message");
                if (!msg.trim().isBlank()) {
                    ret.put("message", codeRet.get("message"));
                }
            }
            if (codeRet.get("code") == null) {
                return ret;
            }
            GeographyCode code = (GeographyCode) codeRet.get("code");
            report.setLocationid(code.getGcid());
            report.setLocation(code);
            ret.put("report", report);
            return ret;
        } catch (Exception e) {
            logger.debug(e.toString());
            ret.put("message", e.toString());
            return ret;
        }
    }


    /*
        export
     */
    public static void writeInternalCSVHeader(CSVPrinter printer) {
        try {
            printer.printRecord("DataSet", "Time", "Locationid",
                    "Confirmed", "Healed", "Dead",
                    "IncreasedConfirmed", "IncreasedHealed", "IncreasedDead",
                    "DataSource");
        } catch (Exception e) {
            logger.debug(e.toString());
        }
    }

    public static void writeInternalCSV(CSVPrinter printer, EpidemicReport report) {
        try {
            printer.printRecord(report.getDataSet(),
                    DateTools.datetimeToString(report.getTime()),
                    report.getLocationid(),
                    report.getConfirmed(),
                    report.getHealed(),
                    report.getDead(),
                    report.getIncreasedConfirmed(),
                    report.getIncreasedHealed(),
                    report.getIncreasedDead(),
                    report.getSource()
            );
        } catch (Exception e) {
            logger.debug(e.toString());
        }
    }

    public static void writeInternalCSV(File file, List<EpidemicReport> reports) {
        try ( CSVPrinter printer = new CSVPrinter(new FileWriter(file, Charset.forName("utf-8")), CSVFormat.DEFAULT)) {
            writeInternalCSVHeader(printer);
            for (EpidemicReport report : reports) {
                writeInternalCSV(printer, report);
            }
        } catch (Exception e) {
            logger.debug(e.toString());
        }
    }

    public static List<String> externalNames(List<String> extraFields) {
        try {
            List<String> columns = new ArrayList<>();
            columns.addAll(Arrays.asList(
                    message("DataSet"), message("Time"),
                    message("Confirmed"), message("Healed"), message("Dead")
            ));
            if (extraFields != null) {
                for (String name : extraFields) {
                    if (null != name) {
                        switch (name) {
                            case "Source":
                                columns.add(message("DataSource"));
                                break;
                            case "IncreasedConfirmed":
                                columns.add(message("IncreasedConfirmed"));
                                break;
                            case "IncreasedHealed":
                                columns.add(message("IncreasedHealed"));
                                break;
                            case "IncreasedDead":
                                columns.add(message("IncreasedDead"));
                                break;
                            case "HealedConfirmedPermillage":
                                columns.add(message("HealedConfirmedPermillage"));
                                break;
                            case "DeadConfirmedPermillage":
                                columns.add(message("DeadConfirmedPermillage"));
                                break;
                            case "ConfirmedPopulationPermillage":
                                columns.add(message("ConfirmedPopulationPermillage"));
                                break;
                            case "DeadPopulationPermillage":
                                columns.add(message("DeadPopulationPermillage"));
                                break;
                            case "HealedPopulationPermillage":
                                columns.add(message("HealedPopulationPermillage"));
                                break;
                            case "ConfirmedAreaPermillage":
                                columns.add(message("ConfirmedAreaPermillage"));
                                break;
                            case "HealedAreaPermillage":
                                columns.add(message("HealedAreaPermillage"));
                                break;
                            case "DeadAreaPermillage":
                                columns.add(message("DeadAreaPermillage"));
                                break;
                            default:
                                break;
                        }
                    }
                }
            }
            columns.addAll(Arrays.asList(
                    message("Level"), message("Continent"), message("Country"), message("Province"), message("City"),
                    message("County"), message("Town"), message("Village"), message("Building"),
                    message("Longitude"), message("Latitude")
            ));
            return columns;
        } catch (Exception e) {
            return null;
        }
    }

    public static List<String> values(EpidemicReport report, List<String> extraFields) {
        GeographyCode gcode = report.getLocation();
        boolean valid = gcode.validCoordinate();
        List<String> row = new ArrayList<>();
        row.addAll(Arrays.asList(
                report.getDataSet(), DateTools.datetimeToString(report.getTime()),
                (report.getConfirmed() + ""),
                (report.getHealed() + ""),
                (report.getDead() + "")
        ));
        if (extraFields != null) {
            for (String name : extraFields) {
                if (null != name) {
                    switch (name) {
                        case "Source":
                            row.add(report.getSourceName());
                            break;
                        case "IncreasedConfirmed":
                            row.add(report.getIncreasedConfirmed() + "");
                            break;
                        case "IncreasedHealed":
                            row.add(report.getIncreasedHealed() + "");
                            break;
                        case "IncreasedDead":
                            row.add(report.getIncreasedDead() + "");
                            break;
                        case "HealedConfirmedPermillage":
                            row.add(report.getHealedConfirmedPermillage() + "");
                            break;
                        case "DeadConfirmedPermillage":
                            row.add(report.getDeadConfirmedPermillage() + "");
                            break;
                        case "ConfirmedPopulationPermillage":
                            row.add(report.getConfirmedPopulationPermillage() + "");
                            break;
                        case "DeadPopulationPermillage":
                            row.add(report.getDeadPopulationPermillage() + "");
                            break;
                        case "HealedPopulationPermillage":
                            row.add(report.getHealedPopulationPermillage() + "");
                            break;
                        case "ConfirmedAreaPermillage":
                            row.add(report.getConfirmedAreaPermillage() + "");
                            break;
                        case "HealedAreaPermillage":
                            row.add(report.getHealedAreaPermillage() + "");
                            break;
                        case "DeadAreaPermillage":
                            row.add(report.getDeadAreaPermillage() + "");
                            break;
                        default:
                            break;
                    }
                }
            }
        }
        row.addAll(Arrays.asList(
                gcode.getLevelCode() != null ? gcode.getLevelCode().getName() : "",
                gcode.getContinentName() != null ? gcode.getContinentName() : "",
                gcode.getCountryName() != null ? gcode.getCountryName() : "",
                gcode.getProvinceName() != null ? gcode.getProvinceName() : "",
                gcode.getCityName() != null ? gcode.getCityName() : "",
                gcode.getCountyName() != null ? gcode.getCountyName() : "",
                gcode.getTownName() != null ? gcode.getTownName() : "",
                gcode.getVillageName() != null ? gcode.getVillageName() : "",
                gcode.getBuildingName() != null ? gcode.getBuildingName() : "",
                (valid ? gcode.getLongitude() + "" : ""),
                (valid ? gcode.getLatitude() + "" : "")
        ));
        return row;
    }

    public static void writeExternalCSVHeader(CSVPrinter printer, List<String> extraFields) {
        try {
            printer.printRecord(externalNames(extraFields));
        } catch (Exception e) {
            logger.debug(e.toString());
        }
    }

    public static void writeExternalCSV(CSVPrinter printer, EpidemicReport report, List<String> extraFields) {
        try {
            printer.printRecord(values(report, extraFields));
        } catch (Exception e) {
            logger.debug(e.toString());
        }
    }

    public static void writeExternalCSV(File file, List<EpidemicReport> reports, List<String> extraFields) {
        try ( CSVPrinter printer = new CSVPrinter(new FileWriter(file, Charset.forName("utf-8")), CSVFormat.DEFAULT)) {
            writeExternalCSVHeader(printer, extraFields);
            for (EpidemicReport report : reports) {
                writeExternalCSV(printer, report, extraFields);
            }
        } catch (Exception e) {
            logger.debug(e.toString());
        }
    }

    public static void writeExcel(File file, List<EpidemicReport> reports, List<String> extraFields) {
        try {
            if (file == null || reports == null || reports.isEmpty()) {
                return;
            }
            XSSFWorkbook wb = new XSSFWorkbook();
            XSSFSheet sheet = wb.createSheet("sheet1");
            List<String> columns = writeExcelHeader(wb, sheet, extraFields);
            for (int i = 0; i < reports.size(); i++) {
                EpidemicReport report = reports.get(i);
                writeExcel(sheet, i, report, extraFields);
            }
            for (int i = 0; i < columns.size(); i++) {
                sheet.autoSizeColumn(i);
            }
            try ( OutputStream fileOut = new FileOutputStream(file)) {
                wb.write(fileOut);
            }
        } catch (Exception e) {

        }
    }

    public static List<String> writeExcelHeader(XSSFWorkbook wb, XSSFSheet sheet, List<String> extraFields) {
        try {
            List<String> columns = externalNames(extraFields);
            sheet.setDefaultColumnWidth(20);
            XSSFRow titleRow = sheet.createRow(0);
            XSSFCellStyle horizontalCenter = wb.createCellStyle();
            horizontalCenter.setAlignment(HorizontalAlignment.CENTER);
            for (int i = 0; i < columns.size(); i++) {
                XSSFCell cell = titleRow.createCell(i);
                cell.setCellValue(columns.get(i));
                cell.setCellStyle(horizontalCenter);
            }
            return columns;
        } catch (Exception e) {
            return null;
        }
    }

    public static void writeExcel(XSSFSheet sheet, int i, EpidemicReport report, List<String> extraFields) {
        try {
            List<String> row = values(report, extraFields);
            XSSFRow sheetRow = sheet.createRow(i + 1);
            for (int j = 0; j < row.size(); j++) {
                XSSFCell cell = sheetRow.createCell(j);
                cell.setCellValue(row.get(j));
            }
        } catch (Exception e) {
        }
    }

    public static void writeJson(File file, List<EpidemicReport> reports, List<String> extraFields) {
        if (file == null || reports == null || reports.isEmpty()) {
            return;
        }
        String indent = "    ";
        try ( FileWriter writer = new FileWriter(file, Charset.forName("utf-8"))) {
            StringBuilder s = new StringBuilder();
            s.append("{\"EpidemicReports\": [\n");
            writer.write(s.toString());
            for (int i = 0; i < reports.size(); i++) {
                EpidemicReport report = reports.get(i);
                s = writeJson(writer, indent, report, extraFields);
                if (i == reports.size() - 1) {
                    s.append(indent).append("}\n");
                } else {
                    s.append(indent).append("},\n");
                }
                writer.write(s.toString());
            }
            writer.write("]}\n");
        } catch (Exception e) {
            logger.error(e.toString());
        }
    }

    public static StringBuilder writeJson(FileWriter writer, String indent, EpidemicReport report, List<String> extraFields) {
        try {
            GeographyCode gcode = report.getLocation();
            boolean valid = gcode.validCoordinate();
            StringBuilder s = new StringBuilder();
            s.append(indent).append("{\"DataSet\":\"").append(report.getDataSet()).append("\"")
                    .append(",\"Time\":\"").append(DateTools.datetimeToString(report.getTime())).append("\"")
                    .append(",\"Confirmed\":").append(report.getConfirmed())
                    .append(",\"Healed\":").append(report.getHealed())
                    .append(",\"Dead\":").append(report.getDead());
            if (extraFields != null) {
                for (String name : extraFields) {
                    if (null != name) {
                        switch (name) {
                            case "Source":
                                s.append(",\"DataSource\":\"").append(report.getSourceName()).append("\"");
                                break;
                            case "IncreasedConfirmed":
                                if (report.getIncreasedConfirmed() > 0) {
                                    s.append(",\"IncreasedConfirmed\":").append(report.getIncreasedConfirmed());
                                }
                                break;
                            case "IncreasedHealed":
                                if (report.getIncreasedHealed() > 0) {
                                    s.append(",\"IncreasedHealed\":").append(report.getIncreasedHealed());
                                }
                                break;
                            case "IncreasedDead":
                                if (report.getIncreasedDead() > 0) {
                                    s.append(",\"IncreasedDead\":").append(report.getIncreasedDead());
                                }
                                break;
                            case "HealedConfirmedPermillage":
                                if (report.getHealedConfirmedPermillage() > 0) {
                                    s.append(",\"HealedConfirmedPermillage\":").append(report.getHealedConfirmedPermillage());
                                }
                                break;
                            case "DeadConfirmedPermillage":
                                if (report.getDeadConfirmedPermillage() > 0) {
                                    s.append(",\"DeadConfirmedPermillage\":").append(report.getDeadConfirmedPermillage());
                                }
                                break;
                            case "ConfirmedPopulationPermillage":
                                if (report.getConfirmedPopulationPermillage() > 0) {
                                    s.append(",\"ConfirmedPopulationPermillage\":").append(report.getConfirmedPopulationPermillage());
                                }
                                break;
                            case "DeadPopulationPermillage":
                                if (report.getDeadPopulationPermillage() > 0) {
                                    s.append(",\"DeadPopulationPermillage\":").append(report.getDeadPopulationPermillage());
                                }
                                break;
                            case "HealedPopulationPermillage":
                                if (report.getHealedPopulationPermillage() > 0) {
                                    s.append(",\"HealedPopulationPermillage\":").append(report.getHealedPopulationPermillage());
                                }
                                break;
                            case "ConfirmedAreaPermillage":
                                if (report.getConfirmedAreaPermillage() > 0) {
                                    s.append(",\"ConfirmedAreaPermillage\":").append(report.getConfirmedAreaPermillage());
                                }
                                break;
                            case "HealedAreaPermillage":
                                if (report.getHealedAreaPermillage() > 0) {
                                    s.append(",\"HealedAreaPermillage\":").append(report.getHealedAreaPermillage());
                                }
                                break;
                            case "DeadAreaPermillage":
                                if (report.getDeadAreaPermillage() > 0) {
                                    s.append(",\"DeadAreaPermillage\":").append(report.getDeadAreaPermillage());
                                }
                                break;
                            default:
                                break;
                        }
                    }
                }
            }
            if (gcode.getLevelCode() != null) {
                s.append(",\"Level\":\"").append(gcode.getLevelCode().getName()).append("\"");
            }
            if (gcode.getContinentName() != null) {
                s.append(",\"Continent\":\"").append(gcode.getContinentName()).append("\"");
            }
            if (gcode.getCountryName() != null) {
                s.append(",\"Country\":\"").append(gcode.getCountryName()).append("\"");
            }
            if (gcode.getProvinceName() != null) {
                s.append(",\"Province\":\"").append(gcode.getProvinceName()).append("\"");
            }
            if (gcode.getCityName() != null) {
                s.append(",\"City\":\"").append(gcode.getCityName()).append("\"");
            }
            if (gcode.getCountyName() != null) {
                s.append(",\"County\":\"").append(gcode.getCountyName()).append("\"");
            }
            if (gcode.getTownName() != null) {
                s.append(",\"Town\":\"").append(gcode.getTownName()).append("\"");
            }
            if (gcode.getVillageName() != null) {
                s.append(",\"Village\":\"").append(gcode.getVillageName()).append("\"");
            }
            if (gcode.getBuildingName() != null) {
                s.append(",\"Building\":\"").append(gcode.getBuildingName()).append("\"");
            }
            if (valid) {
                s.append(",\"Longitude\":\"").append(gcode.getLongitude()).append("\"");
                s.append(",\"Latitude\":\"").append(gcode.getLatitude()).append("\"");
            }
            return s;
        } catch (Exception e) {
            logger.error(e.toString());
            return null;
        }
    }

    public static void writeXml(File file, List<EpidemicReport> reports, List<String> extraFields) {
        if (file == null || reports == null || reports.isEmpty()) {
            return;
        }
        String indent = "    ";
        try ( FileWriter writer = new FileWriter(file, Charset.forName("utf-8"))) {
            StringBuilder s = new StringBuilder();
            s.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n").
                    append("<EpidemicReports>\n");
            writer.write(s.toString());
            for (EpidemicReport report : reports) {
                writeXml(writer, indent, report, extraFields);
            }
            writer.write("</EpidemicReports>\n");
        } catch (Exception e) {
            logger.error(e.toString());
        }
    }

    public static void writeXml(FileWriter writer, String indent, EpidemicReport report, List<String> extraFields) {
        try {
            GeographyCode gcode = report.getLocation();
            boolean valid = gcode.validCoordinate();
            StringBuilder s = new StringBuilder();
            s.append(indent).append("<EpidemicReport ");
            s.append(" DataSet=\"").append(report.getDataSet()).append("\"")
                    .append(" Time=\"").append(DateTools.datetimeToString(report.getTime())).append("\"")
                    .append(" Confirmed=\"").append(report.getConfirmed()).append("\"")
                    .append(" Healed=\"").append(report.getHealed()).append("\"")
                    .append(" Dead=\"").append(report.getDead()).append("\"");
            if (extraFields != null) {
                for (String name : extraFields) {
                    if (null != name) {
                        switch (name) {
                            case "Source":
                                s.append(" DataSource=\"").append(report.getSourceName()).append("\"");
                                break;
                            case "IncreasedConfirmed":
                                s.append(" IncreasedConfirmed=\"").append(report.getIncreasedConfirmed()).append("\"");
                                break;
                            case "IncreasedHealed":
                                s.append(" IncreasedHealed=\"").append(report.getIncreasedHealed()).append("\"");
                                break;
                            case "IncreasedDead":
                                s.append(" IncreasedDead=\"").append(report.getIncreasedDead()).append("\"");
                                break;
                            case "HealedConfirmedPermillage":
                                s.append(" HealedConfirmedPermillage=\"").append(report.getHealedConfirmedPermillage()).append("\"");
                                break;
                            case "DeadConfirmedPermillage":
                                s.append(" DeadConfirmedPermillage=\"").append(report.getDeadConfirmedPermillage()).append("\"");
                                break;
                            case "ConfirmedPopulationPermillage":
                                s.append(" ConfirmedPopulationPermillage=\"").append(report.getConfirmedPopulationPermillage()).append("\"");
                                break;
                            case "DeadPopulationPermillage":
                                s.append(" DeadPopulationPermillage=\"").append(report.getDeadPopulationPermillage()).append("\"");
                                break;
                            case "HealedPopulationPermillage":
                                s.append(" HealedPopulationPermillage=\"").append(report.getHealedPopulationPermillage()).append("\"");
                                break;
                            case "ConfirmedAreaPermillage":
                                s.append(" ConfirmedAreaPermillage=\"").append(report.getConfirmedAreaPermillage()).append("\"");
                                break;
                            case "HealedAreaPermillage":
                                s.append(" HealedAreaPermillage=\"").append(report.getHealedAreaPermillage()).append("\"");
                                break;
                            case "DeadAreaPermillage":
                                s.append(" DeadAreaPermillage=\"").append(report.getDeadAreaPermillage()).append("\"");
                                break;
                            default:
                                break;
                        }
                    }
                }
            }
            if (gcode.getLevelCode() != null) {
                s.append(" Level=\"").append(gcode.getLevelCode().getName()).append("\"");
            }
            if (gcode.getContinentName() != null) {
                s.append(" Continent=\"").append(gcode.getContinentName()).append("\"");
            }
            if (gcode.getCountryName() != null) {
                s.append(" Country=\"").append(gcode.getCountryName()).append("\"");
            }
            if (gcode.getProvinceName() != null) {
                s.append(" Province=\"").append(gcode.getProvinceName()).append("\"");
            }
            if (gcode.getCityName() != null) {
                s.append(" City=\"").append(gcode.getCityName()).append("\"");
            }
            if (gcode.getCountyName() != null) {
                s.append(" County=\"").append(gcode.getCountyName()).append("\"");
            }
            if (gcode.getTownName() != null) {
                s.append(" Town=\"").append(gcode.getTownName()).append("\"");
            }
            if (gcode.getVillageName() != null) {
                s.append(" Village=\"").append(gcode.getVillageName()).append("\"");
            }
            if (gcode.getBuildingName() != null) {
                s.append(" Building=\"").append(gcode.getBuildingName()).append("\"");
            }
            if (valid) {
                s.append(" Longitude=\"").append(gcode.getLongitude()).append("\"");
                s.append(" Latitude=\"").append(gcode.getLatitude()).append("\"");
            }
            s.append(" />\n");
            writer.write(s.toString());
        } catch (Exception e) {
        }
    }

    public static void writeHtml(File file, String title, List<EpidemicReport> reports, List<String> extraFields) {
        try {
            if (file == null || reports == null || reports.isEmpty()) {
                return;
            }
            List<String> names = externalNames(extraFields);
            StringTable table = new StringTable(names, title);
            for (EpidemicReport report : reports) {
                List<String> row = values(report, extraFields);
                table.add(row);
            }
            FileTools.writeFile(file, StringTable.tableHtml(table));
        } catch (Exception e) {
        }
    }

    /*
        custmized get/set
     */
    public long getLocationid() {
        if (locationid <= 0 && location != null) {
            locationid = location.getGcid();
        }
        return locationid;
    }

    public GeographyCode getLocation() {
//        if (location == null && locationid > 0) {
//            location = TableGeographyCode.readCode(locationid, false);
//        }
        return location;
    }

    public String getLocationFullName() {
        if (locationFullName == null && getLocation() != null) {
            locationFullName = location.getFullName();
        }
        return locationFullName;
    }

    public String getSourceName() {
        sourceName = sourceName(source);
        return message(sourceName);
    }

    public SourceType getSourceType() {
        sourceType = sourceType(source);
        return sourceType;
    }

    /*
        get/set
     */
    public long getEpid() {
        return epid;
    }

    public EpidemicReport setEpid(long epid) {
        this.epid = epid;
        return this;
    }

    public EpidemicReport setLocationid(long locationid) {
        this.locationid = locationid;
        return this;
    }

    public String getDataSet() {
        return dataSet;
    }

    public EpidemicReport setDataSet(String dataSet) {
        this.dataSet = dataSet;
        return this;
    }

    public EpidemicReport setLocation(GeographyCode location) {
        this.location = location;
        return this;
    }

    public long getConfirmed() {
        return confirmed;
    }

    public EpidemicReport setConfirmed(long confirmed) {
        this.confirmed = confirmed;
        return this;
    }

    public long getHealed() {
        return healed;
    }

    public EpidemicReport setHealed(long healed) {
        this.healed = healed;
        return this;
    }

    public long getDead() {
        return dead;
    }

    public EpidemicReport setDead(long dead) {
        this.dead = dead;
        return this;
    }

    public long getIncreasedConfirmed() {
        return increasedConfirmed;
    }

    public EpidemicReport setIncreasedConfirmed(long increasedConfirmed) {
        this.increasedConfirmed = increasedConfirmed;
        return this;
    }

    public long getIncreasedHealed() {
        return increasedHealed;
    }

    public EpidemicReport setIncreasedHealed(long increasedHealed) {
        this.increasedHealed = increasedHealed;
        return this;
    }

    public long getIncreasedDead() {
        return increasedDead;
    }

    public EpidemicReport setIncreasedDead(long increasedDead) {
        this.increasedDead = increasedDead;
        return this;
    }

    public long getTime() {
        return time;
    }

    public EpidemicReport setTime(long time) {
        this.time = time;
        return this;
    }

    public void setLocationFullName(String locationFullName) {
        this.locationFullName = locationFullName;
    }

    public void setHealedConfirmedPermillage(double healedConfirmedPermillage) {
        this.healedConfirmedPermillage = healedConfirmedPermillage;
    }

    public void setDeadConfirmedPermillage(double deadConfirmedPermillage) {
        this.deadConfirmedPermillage = deadConfirmedPermillage;
    }

    public int getSource() {
        return source;
    }

    public EpidemicReport setSource(int source) {
        this.source = source;
        return this;
    }

    public EpidemicReport setSourceName(String sourceName) {
        this.sourceName = sourceName;
        return this;
    }

    public void setSourceType(SourceType sourceType) {
        this.sourceType = sourceType;
    }

    public void setConfirmedPopulationPermillage(double confirmedPopulationPermillage) {
        this.confirmedPopulationPermillage = confirmedPopulationPermillage;
    }

    public void setDeadPopulationPermillage(double deadPopulationPermillage) {
        this.deadPopulationPermillage = deadPopulationPermillage;
    }

    public void setHealedPopulationPermillage(double healedPopulationPermillage) {
        this.healedPopulationPermillage = healedPopulationPermillage;
    }

    public void setConfirmedAreaPermillage(double confirmedAreaPermillage) {
        this.confirmedAreaPermillage = confirmedAreaPermillage;
    }

    public void setDeadAreaPermillage(double deadAreaPermillage) {
        this.deadAreaPermillage = deadAreaPermillage;
    }

    public void setHealedAreaPermillage(double healedAreaPermillage) {
        this.healedAreaPermillage = healedAreaPermillage;
    }

    public double getHealedConfirmedPermillage() {
        return healedConfirmedPermillage;
    }

    public double getDeadConfirmedPermillage() {
        return deadConfirmedPermillage;
    }

    public double getConfirmedPopulationPermillage() {
        return confirmedPopulationPermillage;
    }

    public double getDeadPopulationPermillage() {
        return deadPopulationPermillage;
    }

    public double getHealedPopulationPermillage() {
        return healedPopulationPermillage;
    }

    public double getConfirmedAreaPermillage() {
        return confirmedAreaPermillage;
    }

    public double getDeadAreaPermillage() {
        return deadAreaPermillage;
    }

    public double getHealedAreaPermillage() {
        return healedAreaPermillage;
    }

}
