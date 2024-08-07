package net.risesoft.model;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;

/**
 * @author qinman
 */
@Data
public class CodeUseInfoResult extends Result {

    @JsonProperty("codeuse_list")
    private List<CodeUseInfo> list;
}
