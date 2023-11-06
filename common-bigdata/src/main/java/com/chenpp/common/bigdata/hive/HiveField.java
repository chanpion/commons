package com.chenpp.common.bigdata.hive;

import lombok.Data;

import java.io.Serializable;

/**
 * @author li.minqiang
 * @date 2019/12/5
 */
@Data
public class HiveField implements Serializable {

    private static final long serialVersionUID = -5727004720618815159L;
    private boolean partition;
    private String name;
    private String type;
    private String comment;

    public HiveField() {
    }

    public HiveField(String name, String type, String comment) {
        this.name = name;
        this.type = type;
        this.comment = comment;
    }
}