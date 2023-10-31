package com.ixnah.app.audiolinker.jna.util;

import com.sun.jna.DefaultTypeMapper;

public class AppTypeMapper extends DefaultTypeMapper {
    public AppTypeMapper() {
        addTypeConverter(IValueEnum.class, new ValueEnumConverter());
    }
}
