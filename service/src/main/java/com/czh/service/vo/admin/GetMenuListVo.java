package com.czh.service.vo.admin;

import lombok.Data;

import java.util.LinkedList;
import java.util.List;

@Data
public class GetMenuListVo {
    private int id;
    private int pid;
    private String icon;
    private String name;
    private boolean open=false;
    private List<GetMenuListVo> child=new LinkedList<>();
    private String route;
    private String path;
    private int level;
    private int display;
    private int sort;
}
