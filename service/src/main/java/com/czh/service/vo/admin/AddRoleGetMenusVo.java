package com.czh.service.vo.admin;

import lombok.Data;

import java.util.LinkedList;
import java.util.List;

@Data
public class AddRoleGetMenusVo {
    private int id;
    private String name;
    private List<AddRoleGetMenusVo> child=new LinkedList<>();
}
