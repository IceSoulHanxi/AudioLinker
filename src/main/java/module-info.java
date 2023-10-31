module com.ixnah.app.audiolinker {
    requires com.sun.jna.platform;
    requires com.sun.jna;
    requires io.netty.common;
    requires io.netty.buffer;
    requires io.netty.codec;
    requires io.netty.codec.http;
    requires io.netty.transport;
    requires javafx.controls;
    requires jdk.unsupported;
    requires org.slf4j;

    exports com.ixnah.app.audiolinker;
    exports com.ixnah.app.audiolinker.ui;
}