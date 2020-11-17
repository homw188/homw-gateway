package com.homw.gateway.admin.job;

import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.quartz.QuartzJobBean;

import com.homw.gateway.admin.bean.ScheduleJob;
import com.homw.gateway.admin.util.ApplicationContextHelper;
import com.homw.gateway.common.dao.JobMessageDao;
import com.homw.gateway.common.util.StringUtil;
import com.homw.gateway.entity.JobMessage;

public abstract class AbstractScheduleJob extends QuartzJobBean {

	protected Logger logger = LoggerFactory.getLogger(this.getClass());

	@Override
	protected final void executeInternal(JobExecutionContext context) throws JobExecutionException {
		final ScheduleJob job = convert(context);
		new ScheduleJobCommand() {
			@Override
			protected void success() {
				logSuccess(job, "成功");
			}

			@Override
			protected void fail(Throwable t) {
				logFail(job, t.getMessage());
			}

			@Override
			protected boolean run() {
				AbstractScheduleJob.this.execute();
				return true;
			}
		}.exec();
	}

	protected ScheduleJob convert(JobExecutionContext context) {
//		Long buinessId = (Long) context.getJobDetail().getJobDataMap().get("buinessId");
//		String buinessType = (String) context.getJobDetail().getJobDataMap().get("buinessType");
		String jobName = context.getJobDetail().getKey().getName();
		String groupName = context.getJobDetail().getKey().getGroup();

		ScheduleJob job = new ScheduleJob();
//		job.setBusinessId(buinessId);
//		job.setBusinessType(buinessType);
		job.setGroupName(groupName);
		job.setJobName(jobName);
		return job;
	}

	protected void logFail(ScheduleJob job, String message) {
		JobMessage jobMsg = new JobMessage();
		jobMsg.setRemarks(message);
		jobMsg.setJobStatus("FAIL");
		jobMsg.setCreateTime(System.currentTimeMillis());
//		jobMsg.setBuinessId(job.getBusinessId());
//		jobMsg.setBuinessType(job.getBusinessType());
		jobMsg.setJobGroup("AUTOGROUP");
		jobMsg.setJobName(job.getJobName());
		ApplicationContextHelper.getBean(JobMessageDao.class).insertSelective(jobMsg);
	}

	protected void logSuccess(ScheduleJob job, String message) {
		JobMessage jobMsg = new JobMessage();
		jobMsg.setRemarks(message);
		jobMsg.setJobStatus("SUCCESS");
		jobMsg.setCreateTime(System.currentTimeMillis());
//		jobMsg.setBuinessId(job.getBusinessId());
//		jobMsg.setBuinessType(job.getBusinessType());
		jobMsg.setJobGroup("AUTOGROUP");
		jobMsg.setJobDescription(StringUtil.formatTimestamp(System.currentTimeMillis(), "yyyyMMdd HH:mm:ss.SSS"));
		jobMsg.setJobName(job.getJobName());
		ApplicationContextHelper.getBean(JobMessageDao.class).insertSelective(jobMsg);
	}

	protected abstract void execute();
}
