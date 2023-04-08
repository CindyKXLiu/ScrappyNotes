module shared {
    requires kotlin.stdlib;
    requires exposed.core;
    requires exposed.java.time;
    requires transitive java.logging;
    requires transitive java.sql;
    requires transitive java.sql.rowset;
    requires org.slf4j;
    requires java.net.http;
    requires com.google.gson;
    requires org.jsoup;
    opens cs346.shared to com.google.gson;
    exports cs346.shared;
}