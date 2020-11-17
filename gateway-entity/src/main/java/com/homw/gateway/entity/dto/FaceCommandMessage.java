package com.homw.gateway.entity.dto;

public class FaceCommandMessage extends CommandMessage {
	private String faceUserName;
	private String facePhone;
	/**
	 * 用户人脸底图base64编码格式
	 */
	private String facePhoto;
	private String faceStartTime;
	private String faceEndTime;
	/**
	 * 0:员工, 1:访客, 2: VIP访客, 3: 黄名单 如果subject_type不等于0，必须要指定start_time，end_time
	 */
	private String faceUserType;

	private String facePhotoFileName;

	public String getFacePhotoFileName() {
		return facePhotoFileName;
	}

	public void setFacePhotoFileName(String facePhotoFileName) {
		this.facePhotoFileName = facePhotoFileName;
	}

	/**
	 * 项目：项目+合同（SPACE:3:AGREEMENT:2074）<br>
	 * 会议室：项目+会议室预订记录（SPACE:3:MEETINGAPPT:816）
	 */
	private String faceUserGroupName;
	/**
	 * 项目：项目（SPACE:3）<br>
	 * 会议室：项目+会议室（SPACE:3:MEETINGROOM:11）
	 */
	private String faceDeviceGroupName;

	public String getFaceUserGroupName() {
		return faceUserGroupName;
	}

	public void setFaceUserGroupName(String faceUserGroupName) {
		this.faceUserGroupName = faceUserGroupName;
	}

	public String getFaceDeviceGroupName() {
		return faceDeviceGroupName;
	}

	public void setFaceDeviceGroupName(String faceDeviceGroupName) {
		this.faceDeviceGroupName = faceDeviceGroupName;
	}

	/**
	 * 访问时间段：{@code list<list<pair(long, long)>> }长度等于7的三维数组<br>
	 * {@code pair=time_range[i][j] }表示每周第i天的第j项设置<br>
	 * {@code pair[0] }是开始时间，{@code pair[1] }为结束时间。值为距离0点经过的秒数
	 */
	private String faceTimeRangeStart;
	private String faceTimeRangeEnd;

	public String getFaceTimeRangeStart() {
		return faceTimeRangeStart;
	}

	public void setFaceTimeRangeStart(String faceTimeRangeStart) {
		this.faceTimeRangeStart = faceTimeRangeStart;
	}

	public String getFaceTimeRangeEnd() {
		return faceTimeRangeEnd;
	}

	public void setFaceTimeRangeEnd(String faceTimeRangeEnd) {
		this.faceTimeRangeEnd = faceTimeRangeEnd;
	}

	public String getFaceUserName() {
		return faceUserName;
	}

	public void setFaceUserName(String faceUserName) {
		this.faceUserName = faceUserName;
	}

	public String getFacePhone() {
		return facePhone;
	}

	public void setFacePhone(String facePhone) {
		this.facePhone = facePhone;
	}

	public String getFacePhoto() {
		return facePhoto;
	}

	public void setFacePhoto(String facePhoto) {
		this.facePhoto = facePhoto;
	}

	public String getFaceStartTime() {
		return faceStartTime;
	}

	public void setFaceStartTime(String faceStartTime) {
		this.faceStartTime = faceStartTime;
	}

	public String getFaceEndTime() {
		return faceEndTime;
	}

	public void setFaceEndTime(String faceEndTime) {
		this.faceEndTime = faceEndTime;
	}

	public String getFaceUserType() {
		return faceUserType;
	}

	public void setFaceUserType(String faceUserType) {
		this.faceUserType = faceUserType;
	}

}
