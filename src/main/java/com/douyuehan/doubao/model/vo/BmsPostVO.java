package com.douyuehan.doubao.model.vo;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import java.io.Serializable;
import java.util.Date;

public class BmsPostVO implements Serializable {

    public static long getSerialVersionUID() {
        return serialVersionUID;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public Integer getComments() {
        return comments;
    }

    public void setComments(Integer comments) {
        this.comments = comments;
    }

    public Integer getCollects() {
        return collects;
    }

    public void setCollects(Integer collects) {
        this.collects = collects;
    }

    public Integer getView() {
        return view;
    }

    public void setView(Integer view) {
        this.view = view;
    }

    public Integer getSectionId() {
        return sectionId;
    }

    public void setSectionId(Integer sectionId) {
        this.sectionId = sectionId;
    }

    public Boolean getTop() {
        return top;
    }

    public void setTop(Boolean top) {
        this.top = top;
    }

    public Boolean getEssence() {
        return essence;
    }

    public void setEssence(Boolean essence) {
        this.essence = essence;
    }

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

    public Date getModifyTime() {
        return modifyTime;
    }

    public void setModifyTime(Date modifyTime) {
        this.modifyTime = modifyTime;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    private static final long serialVersionUID = 1L;

    /**
     * ??????
     */
    @TableId(value = "id", type = IdType.ASSIGN_ID)
    private String id;
    /**
     * ??????
     */
    @NotBlank(message = "?????????????????????")
    @TableField(value = "title")
    private String title;
    /**
     * markdown
     */
    @NotBlank(message = "?????????????????????")
    @TableField("`content`")
    private String content;

    /**
     * ??????ID
     */
    @TableField("user_id")
    private String userId;

    /**
     * ?????????
     */
    @TableField("comments")
    @Builder.Default
    private Integer comments = 0;

    /**
     * ?????????
     */
    @TableField("collects")
    @Builder.Default
    private Integer collects = 0;

    /**
     * ?????????
     */
    @TableField("view")
    @Builder.Default
    private Integer view = 0;

    /**
     * ??????ID??????????????????
     */
    @TableField("section_id")
    @Builder.Default
    private Integer sectionId = 0;

    /**
     * ??????
     */
    @TableField("top")
    @Builder.Default
    private Boolean top = false;

    /**
     * ??????
     */
    @TableField("essence")
    @Builder.Default
    private Boolean essence = false;

    @TableField("username")
    private String username;

    /**
     * ????????????
     */
    @TableField(value = "create_time", fill = FieldFill.INSERT)
    private Date createTime;

    /**
     * ????????????
     */
    @TableField(value = "modify_time", fill = FieldFill.UPDATE)
    private Date modifyTime;

    private String status;
}
