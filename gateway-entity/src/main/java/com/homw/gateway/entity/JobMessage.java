package com.homw.gateway.entity;

public class JobMessage {
    /**
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column device_job_message.JOB_MESSAGE_ID
     *
     * @mbggenerated
     */
    private Long jobMessageId;

    /**
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column device_job_message.JOB_NAME
     *
     * @mbggenerated
     */
    private String jobName;

    /**
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column device_job_message.JOB_GROUP
     *
     * @mbggenerated
     */
    private String jobGroup;

    /**
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column device_job_message.JOB_CRONEXPRESSION
     *
     * @mbggenerated
     */
    private String jobCronexpression;

    /**
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column device_job_message.JOB_DESCRIPTION
     *
     * @mbggenerated
     */
    private String jobDescription;

    /**
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column device_job_message.BUINESS_TYPE
     *
     * @mbggenerated
     */
    private String buinessType;

    /**
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column device_job_message.BUINESS_ID
     *
     * @mbggenerated
     */
    private Long buinessId;

    /**
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column device_job_message.JOB_STATUS
     *
     * @mbggenerated
     */
    private String jobStatus;

    /**
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column device_job_message.REMARKS
     *
     * @mbggenerated
     */
    private String remarks;

    /**
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column device_job_message.STATUS
     *
     * @mbggenerated
     */
    private Short status;

    /**
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column device_job_message.VERSION
     *
     * @mbggenerated
     */
    private Integer version;

    /**
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column device_job_message.CREATE_USER_ID
     *
     * @mbggenerated
     */
    private Long createUserId;

    /**
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column device_job_message.CREATE_TIME
     *
     * @mbggenerated
     */
    private Long createTime;

    /**
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column device_job_message.CREATE_USER_TYPE
     *
     * @mbggenerated
     */
    private String createUserType;

    /**
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column device_job_message.UPDATE_USER_ID
     *
     * @mbggenerated
     */
    private Long updateUserId;

    /**
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column device_job_message.UPDATE_TIME
     *
     * @mbggenerated
     */
    private Long updateTime;

    /**
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column device_job_message.UPDATE_USER_TYPE
     *
     * @mbggenerated
     */
    private String updateUserType;

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column device_job_message.JOB_MESSAGE_ID
     *
     * @return the value of device_job_message.JOB_MESSAGE_ID
     *
     * @mbggenerated
     */
    public Long getJobMessageId() {
        return jobMessageId;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column device_job_message.JOB_MESSAGE_ID
     *
     * @param jobMessageId the value for device_job_message.JOB_MESSAGE_ID
     *
     * @mbggenerated
     */
    public void setJobMessageId(Long jobMessageId) {
        this.jobMessageId = jobMessageId;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column device_job_message.JOB_NAME
     *
     * @return the value of device_job_message.JOB_NAME
     *
     * @mbggenerated
     */
    public String getJobName() {
        return jobName;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column device_job_message.JOB_NAME
     *
     * @param jobName the value for device_job_message.JOB_NAME
     *
     * @mbggenerated
     */
    public void setJobName(String jobName) {
        this.jobName = jobName == null ? null : jobName.trim();
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column device_job_message.JOB_GROUP
     *
     * @return the value of device_job_message.JOB_GROUP
     *
     * @mbggenerated
     */
    public String getJobGroup() {
        return jobGroup;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column device_job_message.JOB_GROUP
     *
     * @param jobGroup the value for device_job_message.JOB_GROUP
     *
     * @mbggenerated
     */
    public void setJobGroup(String jobGroup) {
        this.jobGroup = jobGroup == null ? null : jobGroup.trim();
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column device_job_message.JOB_CRONEXPRESSION
     *
     * @return the value of device_job_message.JOB_CRONEXPRESSION
     *
     * @mbggenerated
     */
    public String getJobCronexpression() {
        return jobCronexpression;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column device_job_message.JOB_CRONEXPRESSION
     *
     * @param jobCronexpression the value for device_job_message.JOB_CRONEXPRESSION
     *
     * @mbggenerated
     */
    public void setJobCronexpression(String jobCronexpression) {
        this.jobCronexpression = jobCronexpression == null ? null : jobCronexpression.trim();
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column device_job_message.JOB_DESCRIPTION
     *
     * @return the value of device_job_message.JOB_DESCRIPTION
     *
     * @mbggenerated
     */
    public String getJobDescription() {
        return jobDescription;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column device_job_message.JOB_DESCRIPTION
     *
     * @param jobDescription the value for device_job_message.JOB_DESCRIPTION
     *
     * @mbggenerated
     */
    public void setJobDescription(String jobDescription) {
        this.jobDescription = jobDescription == null ? null : jobDescription.trim();
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column device_job_message.BUINESS_TYPE
     *
     * @return the value of device_job_message.BUINESS_TYPE
     *
     * @mbggenerated
     */
    public String getBuinessType() {
        return buinessType;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column device_job_message.BUINESS_TYPE
     *
     * @param buinessType the value for device_job_message.BUINESS_TYPE
     *
     * @mbggenerated
     */
    public void setBuinessType(String buinessType) {
        this.buinessType = buinessType == null ? null : buinessType.trim();
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column device_job_message.BUINESS_ID
     *
     * @return the value of device_job_message.BUINESS_ID
     *
     * @mbggenerated
     */
    public Long getBuinessId() {
        return buinessId;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column device_job_message.BUINESS_ID
     *
     * @param buinessId the value for device_job_message.BUINESS_ID
     *
     * @mbggenerated
     */
    public void setBuinessId(Long buinessId) {
        this.buinessId = buinessId;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column device_job_message.JOB_STATUS
     *
     * @return the value of device_job_message.JOB_STATUS
     *
     * @mbggenerated
     */
    public String getJobStatus() {
        return jobStatus;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column device_job_message.JOB_STATUS
     *
     * @param jobStatus the value for device_job_message.JOB_STATUS
     *
     * @mbggenerated
     */
    public void setJobStatus(String jobStatus) {
        this.jobStatus = jobStatus == null ? null : jobStatus.trim();
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column device_job_message.REMARKS
     *
     * @return the value of device_job_message.REMARKS
     *
     * @mbggenerated
     */
    public String getRemarks() {
        return remarks;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column device_job_message.REMARKS
     *
     * @param remarks the value for device_job_message.REMARKS
     *
     * @mbggenerated
     */
    public void setRemarks(String remarks) {
        this.remarks = remarks == null ? null : remarks.trim();
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column device_job_message.STATUS
     *
     * @return the value of device_job_message.STATUS
     *
     * @mbggenerated
     */
    public Short getStatus() {
        return status;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column device_job_message.STATUS
     *
     * @param status the value for device_job_message.STATUS
     *
     * @mbggenerated
     */
    public void setStatus(Short status) {
        this.status = status;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column device_job_message.VERSION
     *
     * @return the value of device_job_message.VERSION
     *
     * @mbggenerated
     */
    public Integer getVersion() {
        return version;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column device_job_message.VERSION
     *
     * @param version the value for device_job_message.VERSION
     *
     * @mbggenerated
     */
    public void setVersion(Integer version) {
        this.version = version;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column device_job_message.CREATE_USER_ID
     *
     * @return the value of device_job_message.CREATE_USER_ID
     *
     * @mbggenerated
     */
    public Long getCreateUserId() {
        return createUserId;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column device_job_message.CREATE_USER_ID
     *
     * @param createUserId the value for device_job_message.CREATE_USER_ID
     *
     * @mbggenerated
     */
    public void setCreateUserId(Long createUserId) {
        this.createUserId = createUserId;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column device_job_message.CREATE_TIME
     *
     * @return the value of device_job_message.CREATE_TIME
     *
     * @mbggenerated
     */
    public Long getCreateTime() {
        return createTime;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column device_job_message.CREATE_TIME
     *
     * @param createTime the value for device_job_message.CREATE_TIME
     *
     * @mbggenerated
     */
    public void setCreateTime(Long createTime) {
        this.createTime = createTime;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column device_job_message.CREATE_USER_TYPE
     *
     * @return the value of device_job_message.CREATE_USER_TYPE
     *
     * @mbggenerated
     */
    public String getCreateUserType() {
        return createUserType;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column device_job_message.CREATE_USER_TYPE
     *
     * @param createUserType the value for device_job_message.CREATE_USER_TYPE
     *
     * @mbggenerated
     */
    public void setCreateUserType(String createUserType) {
        this.createUserType = createUserType == null ? null : createUserType.trim();
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column device_job_message.UPDATE_USER_ID
     *
     * @return the value of device_job_message.UPDATE_USER_ID
     *
     * @mbggenerated
     */
    public Long getUpdateUserId() {
        return updateUserId;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column device_job_message.UPDATE_USER_ID
     *
     * @param updateUserId the value for device_job_message.UPDATE_USER_ID
     *
     * @mbggenerated
     */
    public void setUpdateUserId(Long updateUserId) {
        this.updateUserId = updateUserId;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column device_job_message.UPDATE_TIME
     *
     * @return the value of device_job_message.UPDATE_TIME
     *
     * @mbggenerated
     */
    public Long getUpdateTime() {
        return updateTime;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column device_job_message.UPDATE_TIME
     *
     * @param updateTime the value for device_job_message.UPDATE_TIME
     *
     * @mbggenerated
     */
    public void setUpdateTime(Long updateTime) {
        this.updateTime = updateTime;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column device_job_message.UPDATE_USER_TYPE
     *
     * @return the value of device_job_message.UPDATE_USER_TYPE
     *
     * @mbggenerated
     */
    public String getUpdateUserType() {
        return updateUserType;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column device_job_message.UPDATE_USER_TYPE
     *
     * @param updateUserType the value for device_job_message.UPDATE_USER_TYPE
     *
     * @mbggenerated
     */
    public void setUpdateUserType(String updateUserType) {
        this.updateUserType = updateUserType == null ? null : updateUserType.trim();
    }
}