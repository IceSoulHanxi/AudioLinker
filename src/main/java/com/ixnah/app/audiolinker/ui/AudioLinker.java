package com.ixnah.app.audiolinker.ui;

import com.ixnah.app.audiolinker.Metadata;
import javafx.application.Application;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AudioLinker extends Application {
    public static Logger LOG = LoggerFactory.getLogger(AudioLinker.class);

    @Override
    public void start(Stage stage) {
        LOG.info("start");
        stage.setTitle(Metadata.AppName);
        stage.show();
    }

}
