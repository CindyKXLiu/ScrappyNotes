module application {
    requires kotlin.stdlib;
    requires javafx.controls;
    requires kotlinx.coroutines.core.jvm;
    requires shared;
    requires kotlinx.serialization.core;
    requires kotlinx.serialization.json;
    requires javafx.web;
    requires org.jsoup;
    exports cs346.application;
}