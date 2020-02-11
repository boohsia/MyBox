package mara.mybox.db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import mara.mybox.data.EpidemicReport;
import static mara.mybox.db.DerbyBase.dbHome;
import static mara.mybox.db.DerbyBase.failed;
import static mara.mybox.db.DerbyBase.login;
import static mara.mybox.db.DerbyBase.protocol;
import mara.mybox.tools.DateTools;
import static mara.mybox.value.AppVariables.logger;

/**
 * @Author Mara
 * @CreateDate 2020-2-2
 * @License Apache License Version 2.0
 */
public class TableEpidemicReport extends DerbyBase {

    public TableEpidemicReport() {
        Table_Name = "Epidemic_Report";
        Keys = new ArrayList<>() {
            {
                add("dataid");
            }
        };
        Create_Table_Statement
                = " CREATE TABLE Epidemic_Report ( "
                + "  dataid BIGINT NOT NULL GENERATED BY DEFAULT AS IDENTITY (START WITH 1, INCREMENT BY 1), "
                + "  data_set VARCHAR(1024) NOT NULL, "
                + "  data_label VARCHAR(1024), "
                + "  country VARCHAR(1024), "
                + "  province VARCHAR(1024), "
                + "  city VARCHAR(1024), "
                + "  confirmed INTEGER, "
                + "  suspected INTEGER, "
                + "  healed INTEGER, "
                + "  dead INTEGER, "
                + "  longitude DOUBLE, "
                + "  latitude DOUBLE, "
                + "  comments VARCHAR(32672), "
                + "  time TIMESTAMP, "
                + "  PRIMARY KEY (dataid)"
                + " )";

    }

    public static int sizeQuery(String sql) {
        try ( Connection conn = DriverManager.getConnection(protocol + dbHome() + login);
                 Statement statement = conn.createStatement()) {
            ResultSet results = statement.executeQuery(sql);
            if (results.next()) {
                return results.getInt(1);
            } else {
                return 0;
            }
        } catch (Exception e) {
            failed(e);
            return 0;
        }
    }

    public static List<EpidemicReport> dataQuery(String sql) {
        List<EpidemicReport> reports = new ArrayList<>();
        try ( Connection conn = DriverManager.getConnection(protocol + dbHome() + login);
                 Statement statement = conn.createStatement()) {
            ResultSet results = statement.executeQuery(sql);
            while (results.next()) {
                EpidemicReport location = read(results);
                reports.add(location);
            }
        } catch (Exception e) {
            failed(e);
            // logger.debug(e.toString());
        }
        return reports;
    }

    public static List<String> datasets() {
        try ( Connection conn = DriverManager.getConnection(protocol + dbHome() + login);
                 Statement statement = conn.createStatement()) {
            List<String> datasets = new ArrayList();
            String sql = " SELECT DISTINCT data_set FROM Epidemic_Report";
            ResultSet results = statement.executeQuery(sql);
            while (results.next()) {
                datasets.add(results.getString("data_set"));
            }
            return datasets;
        } catch (Exception e) {
            failed(e);
//            // logger.debug(e.toString());
            return null;
        }
    }

    public static List<Date> times() {
        List<Date> times = new ArrayList<>();
        try ( Connection conn = DriverManager.getConnection(protocol + dbHome() + login);
                 Statement statement = conn.createStatement()) {
            String sql = " SELECT DISTINCT time FROM Epidemic_Report";
            ResultSet results = statement.executeQuery(sql);
            while (results.next()) {
                Date d = results.getTimestamp("time");
                if (d != null) {
                    times.add(d);
                }
            }
            return times;
        } catch (Exception e) {
            failed(e);
//            // logger.debug(e.toString());
            return null;
        }
    }

    public static List<Date> times(String dataset) {
        List<Date> times = new ArrayList<>();
        if (dataset == null) {
            return times;
        }
        try ( Connection conn = DriverManager.getConnection(protocol + dbHome() + login);
                 Statement statement = conn.createStatement()) {
            String sql = " SELECT DISTINCT time FROM Epidemic_Report WHERE data_set='" + dataset + "'";
            ResultSet results = statement.executeQuery(sql);
            while (results.next()) {
                Date d = results.getTimestamp("time");
                if (d != null) {
                    times.add(d);
                }
            }
            return times;
        } catch (Exception e) {
            failed(e);
//            // logger.debug(e.toString());
            return null;
        }
    }

    public static List<String> countries(String dataset) {
        List<String> countries = new ArrayList<>();
        if (dataset == null) {
            return countries;
        }
        try ( Connection conn = DriverManager.getConnection(protocol + dbHome() + login);
                 Statement statement = conn.createStatement()) {
            String sql = " SELECT DISTINCT country FROM Epidemic_Report WHERE data_set='" + dataset + "'";
            ResultSet results = statement.executeQuery(sql);
            while (results.next()) {
                String c = results.getString("country");
                if (c != null) {
                    countries.add(c);
                }
            }
            return countries;
        } catch (Exception e) {
            failed(e);
//            // logger.debug(e.toString());
            return null;
        }
    }

    public static List<String> provinces(String dataset, String country) {
        List<String> provinces = new ArrayList<>();
        if (dataset == null) {
            return provinces;
        }
        try ( Connection conn = DriverManager.getConnection(protocol + dbHome() + login);
                 Statement statement = conn.createStatement()) {
            String sql = " SELECT DISTINCT province FROM Epidemic_Report WHERE "
                    + "data_set='" + dataset + "' AND country='" + country + "'";
            ResultSet results = statement.executeQuery(sql);
            while (results.next()) {
                String c = results.getString("province");
                if (c != null) {
                    provinces.add(c);
                }
            }
            return provinces;
        } catch (Exception e) {
            failed(e);
//            // logger.debug(e.toString());
            return null;
        }
    }

    public static List<EpidemicReport> read(String dataset, int max) {
        List<EpidemicReport> locations = new ArrayList<>();
        if (dataset == null) {
            return locations;
        }
        try ( Connection conn = DriverManager.getConnection(protocol + dbHome() + login);
                 Statement statement = conn.createStatement()) {
            statement.setMaxRows(max);
            String sql = "SELECT * FROM Epidemic_Report WHERE data_set='" + dataset + "'";
            ResultSet results = statement.executeQuery(sql);
            while (results.next()) {
                EpidemicReport location = read(results);
                locations.add(location);
            }
        } catch (Exception e) {
            failed(e);
            // logger.debug(e.toString());
        }
        return locations;
    }

    public static List<EpidemicReport> read(String dataset, long time, int max) {
        List<EpidemicReport> locations = new ArrayList<>();
        if (dataset == null) {
            return locations;
        }
        try ( Connection conn = DriverManager.getConnection(protocol + dbHome() + login);
                 Statement statement = conn.createStatement()) {
            statement.setMaxRows(max);
            String sql = "SELECT * FROM Epidemic_Report WHERE "
                    + "data_set='" + dataset + "' AND time='" + DateTools.datetimeToString(time) + "'";
            ResultSet results = statement.executeQuery(sql);
            while (results.next()) {
                EpidemicReport location = read(results);
                locations.add(location);
            }
        } catch (Exception e) {
            failed(e);
            // logger.debug(e.toString());
        }
        return locations;
    }

    public static EpidemicReport read(ResultSet results) {
        if (results == null) {
            return null;
        }
        try {
            EpidemicReport report = new EpidemicReport();
            report.setDataid(results.getLong("dataid"));
            report.setDataSet(results.getString("data_set"));
            report.setDataLabel(results.getString("data_label"));
            report.setCountry(results.getString("country"));
            report.setProvince(results.getString("province"));
            report.setCity(results.getString("city"));
            report.setConfirmed(results.getInt("confirmed"));
            report.setSuspected(results.getInt("suspected"));
            report.setHealed(results.getInt("healed"));
            report.setDead(results.getInt("dead"));
            report.setLongitude(results.getDouble("longitude"));
            report.setLatitude(results.getDouble("latitude"));
            report.setComments(results.getString("comments"));
            Date d = results.getTimestamp("time");
            if (d != null) {
                report.setTime(d.getTime());
            }
            return report;
        } catch (Exception e) {
            failed(e);
            logger.debug(e.toString());
            return null;
        }
    }

    public static boolean write(EpidemicReport report) {
        if (report == null) {
            return false;
        }
        try ( Connection conn = DriverManager.getConnection(protocol + dbHome() + login);
                 Statement statement = conn.createStatement()) {
            if (report.getDataid() < 0) {
                create(statement, report);
            } else {
                update(statement, report);
            }
            return true;
        } catch (Exception e) {
            failed(e);
            logger.debug(e.toString());
            return false;
        }
    }

    public static boolean write(List<EpidemicReport> reports) {
        if (reports == null || reports.isEmpty()) {
            return false;
        }
        try ( Connection conn = DriverManager.getConnection(protocol + dbHome() + login);
                 Statement statement = conn.createStatement()) {
            conn.setAutoCommit(false);
            for (EpidemicReport report : reports) {
                if (report.getDataid() < 0) {
                    create(statement, report);
                } else {
                    update(statement, report);
                }
            }
            conn.commit();
            return true;
        } catch (Exception e) {
            failed(e);
            logger.debug(e.toString());
            return false;
        }
    }

    public static boolean summary() {
        try ( Connection conn = DriverManager.getConnection(protocol + dbHome() + login);
                 Statement statement = conn.createStatement()) {
            List<String> datasets = new ArrayList();
            String sql = " SELECT DISTINCT data_set FROM Epidemic_Report";
            ResultSet datasetsResults = statement.executeQuery(sql);
            while (datasetsResults.next()) {
                datasets.add(datasetsResults.getString("data_set"));
            }
            for (String dataset : datasets) {
                summary(statement, dataset);
            }
            return true;
        } catch (Exception e) {
            failed(e);
            logger.debug(e.toString());
            return false;
        }
    }

    public static boolean summary(String dataset) {
        if (dataset == null) {
            return false;
        }
        try ( Connection conn = DriverManager.getConnection(protocol + dbHome() + login);
                 Statement statement = conn.createStatement()) {
            summary(statement, dataset);
            return true;
        } catch (Exception e) {
            failed(e);
            logger.debug(e.toString());
            return false;
        }
    }

    public static boolean summary(Statement statement, String dataset) {
        if (dataset == null) {
            return false;
        }
        try {
            List<String> countries = new ArrayList<>();
            String sql = " SELECT DISTINCT country FROM Epidemic_Report WHERE data_set='" + dataset + "'";
            ResultSet countriesResults = statement.executeQuery(sql);
            while (countriesResults.next()) {
                String c = countriesResults.getString("country");
                if (c != null) {
                    countries.add(c);
                }
            }

            List<Date> times = new ArrayList<>();
            sql = " SELECT DISTINCT time FROM Epidemic_Report WHERE data_set='" + dataset + "'";
            ResultSet timesResults = statement.executeQuery(sql);
            while (timesResults.next()) {
                Date d = timesResults.getTimestamp("time");
                if (d != null) {
                    times.add(d);
                }
            }

            summary(statement, dataset, times, countries);
            summary(statement, dataset, times);

            return true;
        } catch (Exception e) {
            failed(e);
            logger.debug(e.toString());
            return false;
        }
    }

    public static boolean summary(Statement statement, String dataset,
            List<Date> times) {
        try {
            int confirmed, healed, suspected, dead;
            for (Date d : times) {
                String sql = " SELECT sum(confirmed) FROM Epidemic_Report WHERE data_set='"
                        + dataset + "' AND time='" + DateTools.datetimeToString(d) + "' "
                        + " AND country IS NOT NULL AND province IS NULL";
                ResultSet confirmedResults = statement.executeQuery(sql);
                if (confirmedResults.next()) {
                    confirmed = confirmedResults.getInt(1);
                } else {
                    confirmed = 0;
                }
                sql = " SELECT sum(suspected) FROM Epidemic_Report WHERE data_set='"
                        + dataset + "' AND time='" + DateTools.datetimeToString(d) + "' "
                        + " AND country IS NOT NULL AND province IS NULL";
                ResultSet suspectedResults = statement.executeQuery(sql);
                if (suspectedResults.next()) {
                    suspected = suspectedResults.getInt(1);
                } else {
                    suspected = 0;
                }
                sql = " SELECT sum(healed) FROM Epidemic_Report WHERE data_set='"
                        + dataset + "' AND time='" + DateTools.datetimeToString(d) + "' "
                        + " AND country IS NOT NULL AND province IS NULL";
                ResultSet healedResults = statement.executeQuery(sql);
                if (healedResults.next()) {
                    healed = healedResults.getInt(1);
                } else {
                    healed = 0;
                }
                sql = " SELECT sum(dead) FROM Epidemic_Report WHERE data_set='"
                        + dataset + "' AND time='" + DateTools.datetimeToString(d) + "' "
                        + " AND country IS NOT NULL AND province IS NULL";
                ResultSet deadResults = statement.executeQuery(sql);
                if (deadResults.next()) {
                    dead = deadResults.getInt(1);
                } else {
                    dead = 0;
                }
                EpidemicReport report = EpidemicReport.create().setDataSet(dataset)
                        .setConfirmed(confirmed).setSuspected(suspected)
                        .setHealed(healed).setDead(dead)
                        .setTime(d.getTime());
                sql = " SELECT dataid FROM Epidemic_Report WHERE data_set='"
                        + dataset + "' AND time='" + DateTools.datetimeToString(d) + "' "
                        + " AND  country IS NULL AND province IS NULL";
                ResultSet allResults = statement.executeQuery(sql);
                if (allResults.next()) {
                    update(statement, report);
                } else {
                    create(statement, report);
                }
            }
            return true;
        } catch (Exception e) {
            failed(e);
            logger.debug(e.toString());
            return false;
        }
    }

    public static boolean summary(Statement statement, String dataset,
            List<Date> times, List<String> countries) {
        try {
            Map<String, Double> longitudes = new HashMap<>();
            Map<String, Double> latitudes = new HashMap<>();
            for (String c : countries) {
                String sql = "SELECT * FROM Geography_Code WHERE address='" + c + "'";
                ResultSet results = statement.executeQuery(sql);
                if (results.next()) {
                    longitudes.put(c, results.getDouble("longitude"));
                    latitudes.put(c, results.getDouble("latitude"));
                }
            }
            int confirmed, healed, suspected, dead;
            for (Date d : times) {
                for (String c : countries) {
                    String sql = " SELECT sum(confirmed) FROM Epidemic_Report WHERE data_set='"
                            + dataset + "' AND time='" + DateTools.datetimeToString(d) + "' "
                            + " AND country='" + c + "'  AND province IS NOT NULL";
                    ResultSet confirmedResults = statement.executeQuery(sql);
                    if (confirmedResults.next()) {
                        confirmed = confirmedResults.getInt(1);
                    } else {
                        confirmed = 0;
                    }
                    sql = " SELECT sum(suspected) FROM Epidemic_Report WHERE data_set='"
                            + dataset + "' AND time='" + DateTools.datetimeToString(d) + "' "
                            + " AND country='" + c + "'  AND province IS NOT NULL";
                    ResultSet suspectedResults = statement.executeQuery(sql);
                    if (suspectedResults.next()) {
                        suspected = suspectedResults.getInt(1);
                    } else {
                        suspected = 0;
                    }
                    sql = " SELECT sum(healed) FROM Epidemic_Report WHERE data_set='"
                            + dataset + "' AND time='" + DateTools.datetimeToString(d) + "' "
                            + " AND country='" + c + "'  AND province IS NOT NULL";
                    ResultSet healedResults = statement.executeQuery(sql);
                    if (healedResults.next()) {
                        healed = healedResults.getInt(1);
                    } else {
                        healed = 0;
                    }
                    sql = " SELECT sum(dead) FROM Epidemic_Report WHERE data_set='"
                            + dataset + "' AND time='" + DateTools.datetimeToString(d) + "' "
                            + " AND country='" + c + "'  AND province IS NOT NULL";
                    ResultSet deadResults = statement.executeQuery(sql);
                    if (deadResults.next()) {
                        dead = deadResults.getInt(1);
                    } else {
                        dead = 0;
                    }
                    EpidemicReport report = EpidemicReport.create().setDataSet(dataset)
                            .setCountry(c)
                            .setConfirmed(confirmed).setSuspected(suspected)
                            .setHealed(healed).setDead(dead)
                            .setTime(d.getTime());
                    if (longitudes.get(c) != null) {
                        report.setLongitude(longitudes.get(c));
                    }
                    if (latitudes.get(c) != null) {
                        report.setLatitude(latitudes.get(c));
                    }
                    sql = " SELECT dataid FROM Epidemic_Report WHERE data_set='"
                            + dataset + "' AND time='" + DateTools.datetimeToString(d) + "' "
                            + " AND country='" + c + "'  AND province IS NULL";
                    ResultSet allResults = statement.executeQuery(sql);
                    if (allResults.next()) {
                        update(statement, report);
                    } else {
                        create(statement, report);
                    }
                }
            }
            return true;
        } catch (Exception e) {
            failed(e);
            logger.debug(e.toString());
            return false;
        }
    }

    public static boolean create(Statement statement, EpidemicReport report) {
        if (statement == null || report == null) {
            return false;
        }
        try {
            String sql = "INSERT INTO Epidemic_Report(data_set, data_label, country, province, city, "
                    + "confirmed, suspected, healed, dead, longitude, latitude, comments,time) VALUES(";
            sql += "'" + report.getDataSet() + "', ";
            if (report.getDataLabel() != null) {
                sql += "'" + report.getDataLabel() + "', ";
            } else {
                sql += "null, ";
            }
            if (report.getCountry() != null) {
                sql += "'" + report.getCountry() + "', ";
            } else {
                sql += "null, ";
            }
            if (report.getProvince() != null) {
                sql += "'" + report.getProvince() + "', ";
            } else {
                sql += "null, ";
            }
            if (report.getCity() != null) {
                sql += "'" + report.getCity() + "', ";
            } else {
                sql += "null, ";
            }
            sql += report.getConfirmed() + ", ";
            sql += report.getSuspected() + ", ";
            sql += report.getHealed() + ", ";
            sql += report.getDead() + ", ";
            sql += report.getLongitude() + ", ";
            sql += report.getLatitude() + ", ";
            if (report.getComments() != null) {
                sql += "'" + report.getComments() + "', ";
            } else {
                sql += "null, ";
            }
            if (report.getTime() > 0) {
                sql += "'" + DateTools.datetimeToString(report.getTime()) + "' ";
            } else {
                sql += "null ";
            }
            sql += " )";
            statement.executeUpdate(sql);
            return true;
        } catch (Exception e) {
            failed(e);
            logger.debug(e.toString());
            return false;
        }
    }

    public static boolean update(Statement statement, EpidemicReport report) {
        if (statement == null || report == null || report.getDataid() < 0) {
            return false;
        }
        try {
            statement.setMaxRows(1);
            String sql = "UPDATE Epidemic_Report SET ";
            sql += "data_set='" + report.getDataSet() + "', ";
            if (report.getDataLabel() != null) {
                sql += "data_label='" + report.getDataLabel() + "', ";
            } else {
                sql += "data_label=null, ";
            }
            if (report.getCountry() != null) {
                sql += "country='" + report.getCountry() + "', ";
            } else {
                sql += "country=null, ";
            }
            if (report.getProvince() != null) {
                sql += "province='" + report.getProvince() + "', ";
            } else {
                sql += "province=null, ";
            }
            if (report.getCity() != null) {
                sql += "city='" + report.getCity() + "', ";
            } else {
                sql += "city=null, ";
            }
            sql += "confirmed=" + report.getConfirmed() + ", ";
            sql += "suspected=" + report.getSuspected() + ", ";
            sql += "healed=" + report.getHealed() + ", ";
            sql += "dead=" + report.getDead() + ", ";
            sql += "longitude=" + report.getLongitude() + ", ";
            sql += "latitude=" + report.getLatitude() + ", ";
            if (report.getComments() != null) {
                sql += "comments='" + report.getComments() + "', ";
            } else {
                sql += "comments=null, ";
            }
            if (report.getTime() > 0) {
                sql += "time='" + DateTools.datetimeToString(report.getTime()) + "' ";
            } else {
                sql += "time=null  ";
            }
            sql += "WHERE dataid=" + report.getDataid();
            statement.executeUpdate(sql);
            return true;
        } catch (Exception e) {
            failed(e);
            logger.debug(e.toString());
            return false;
        }
    }

    public static boolean delete(long dataid) {
        try ( Connection conn = DriverManager.getConnection(protocol + dbHome() + login);
                 Statement statement = conn.createStatement()) {
            String sql = "DELETE FROM Epidemic_Report WHERE dataid=" + dataid;
            statement.executeUpdate(sql);
            return true;
        } catch (Exception e) {
            failed(e);
            // logger.debug(e.toString());
            return false;
        }
    }

    public static boolean delete(String dataset) {
        if (dataset == null) {
            return false;
        }
        try ( Connection conn = DriverManager.getConnection(protocol + dbHome() + login);
                 Statement statement = conn.createStatement()) {
            String sql = "DELETE FROM Epidemic_Report WHERE data_set='" + dataset + "'";
            statement.executeUpdate(sql);
            return true;
        } catch (Exception e) {
            failed(e);
            // logger.debug(e.toString());
            return false;
        }
    }

    public static boolean deleteData(List<EpidemicReport> values) {
        if (values == null || values.isEmpty()) {
            return false;
        }
        try ( Connection conn = DriverManager.getConnection(protocol + dbHome() + login);
                 Statement statement = conn.createStatement()) {
            String inStr = "( " + values.get(0).getDataid();
            for (int i = 1; i < values.size(); ++i) {
                inStr += ", " + values.get(i).getDataid();
            }
            inStr += " )";
            String sql = "DELETE FROM Epidemic_Report WHERE dataid IN " + inStr;
            statement.executeUpdate(sql);
            return true;
        } catch (Exception e) {
            failed(e);
//            // logger.debug(e.toString());
            return false;
        }
    }

}