module shared {
    requires kotlin.stdlib;
    requires exposed.core;
    requires exposed.java.time;
    requires transitive java.logging;
    requires transitive java.sql;
    requires transitive java.sql.rowset;
    requires org.slf4j;
    exports cs346.shared;
}