package net.risesoft.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

@Data
public class CategoryResult extends Result{

    @JsonProperty("base_idcode_list")
    private List<Category> list;
}