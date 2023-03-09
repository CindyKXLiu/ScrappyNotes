module application {
    requires kotlin.stdlib;
    requires javafx.controls;
    requires kotlinx.coroutines.core.jvm;
    requires shared;
    requires kotlinx.serialization.core;
    requires kotlinx.serialization.json;
    exports cs346.application;
}