package com.czh.service.entity;


import com.baomidou.mybatisplus.annotation.TableId;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
/**
 * 地区表(Area)表实体类
 *
 * @author makejava
 * @since 2026-04-04 16:35:00
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Area implements Serializable {
    private static final long serialVersionUID=1L;
    //行政区划ID
    @TableId
    @NotNull
    private Integer id;
    //上级区划ID        
    @NotNull  
    private Integer pid;
    //级联树        
    @NotNull  
    private String tree;
    //地区名称        
    @NotNull  
    private String name;
}
