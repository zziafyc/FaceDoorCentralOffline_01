package com.example.facedoor.model;

import java.io.Serializable;

/**
 * Created by fyc on 2017/9/1.
 */

public class Group implements Serializable {
    private String id;
    private String name;
    private boolean choose;

    public Group(String id, String name) {
        this.id = id;
        this.name = name;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean isChoose() {
        return choose;
    }

    public void setChoose(boolean choose) {
        this.choose = choose;
    }
}
