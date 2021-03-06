
package com.capitalone.dashboard.collector;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * Bean to hold settings specific to the Feature collector.
 * 
 * @author KFK884
 */
@Component
@ConfigurationProperties(prefix = "feature")
public class FeatureSettings {
	private String cron;
	private int pageSize;
	private String deltaStartDate;
	private String deltaCollectorItemStartDate;
	private String masterStartDate;
	private String queryFolder;
	private String storyQuery;
	private String epicQuery;
	private String projectQuery;
	private String memberQuery;
	private String sprintQuery;
	private String teamQuery;
	private String trendingQuery;
	private String projectObjectId;
	private int sprintDays;
	private int sprintEndPrior;
	private int scheduledPriorMin;
	private String versionOneProxyUrl;
	private String rallyBaseUri;
	private String rallyApiKey;
	private int maxKanbanIterationLength;
	private String filter;

	@Autowired
	public void setApplicationName(@Value("${feature.release}") String release) {
		this.filter = release;
	}

	public String getFilter() {
		return filter;
	}

	public String getCron() {
		return cron;
	}

	public void setCron(String cron) {
		this.cron = cron;
	}

	public int getPageSize() {
		return pageSize;
	}

	public void setPageSize(int pageSize) {
		this.pageSize = pageSize;
	}

	public String getDeltaStartDate() {
		return deltaStartDate;
	}

	public String getProjectObjectId() {
		return this.projectObjectId;
	}

	public void setProjectObjectId(String projectObjectId) {
		this.projectObjectId = projectObjectId;
	}

	public void setDeltaStartDate(String deltaStartDate) {
		this.deltaStartDate = deltaStartDate;
	}

	public void setDeltaCollectorItemStartDate(String deltaCollectorItemStartDate) {
		this.deltaCollectorItemStartDate = deltaCollectorItemStartDate;
	}

	public String getDeltaCollectorItemStartDate() {
		return this.deltaCollectorItemStartDate;
	}

	public String getMasterStartDate() {
		return masterStartDate;
	}

	public void setMasterStartDate(String masterStartDate) {
		this.masterStartDate = masterStartDate;
	}

	public String getQueryFolder() {
		return queryFolder;
	}

	public void setQueryFolder(String queryFolder) {
		this.queryFolder = queryFolder;
	}

	public String getStoryQuery() {
		return storyQuery;
	}

	public void setStoryQuery(String storyQuery) {
		this.storyQuery = storyQuery;
	}

	public String getEpicQuery() {
		return epicQuery;
	}

	public void setEpicQuery(String epicQuery) {
		this.epicQuery = epicQuery;
	}

	public String getProjectQuery() {
		return projectQuery;
	}

	public void setProjectQuery(String projectQuery) {
		this.projectQuery = projectQuery;
	}

	public String getMemberQuery() {
		return memberQuery;
	}

	public void setMemberQuery(String memberQuery) {
		this.memberQuery = memberQuery;
	}

	public String getSprintQuery() {
		return sprintQuery;
	}

	public void setSprintQuery(String sprintQuery) {
		this.sprintQuery = sprintQuery;
	}

	public String getTeamQuery() {
		return teamQuery;
	}

	public void setTeamQuery(String teamQuery) {
		this.teamQuery = teamQuery;
	}

	public String getTrendingQuery() {
		return trendingQuery;
	}

	public void setTrendingQuery(String trendingQuery) {
		this.trendingQuery = trendingQuery;
	}

	public int getSprintDays() {
		return sprintDays;
	}

	public void setSprintDays(int sprintDays) {
		this.sprintDays = sprintDays;
	}

	public int getSprintEndPrior() {
		return sprintEndPrior;
	}

	public void setSprintEndPrior(int sprintEndPrior) {
		this.sprintEndPrior = sprintEndPrior;
	}

	public int getScheduledPriorMin() {
		return scheduledPriorMin;
	}

	public void setScheduledPriorMin(int scheduledPriorMin) {
		this.scheduledPriorMin = scheduledPriorMin;
	}

	public String getVersionOneProxyUrl() {
		return this.versionOneProxyUrl;
	}

	public String getRallyBaseUri() {
		return this.rallyBaseUri;
	}

	public String getRallyApiKey() {
		return this.rallyApiKey;
	}

	public void setVersionOneProxyUrl(String versionOneProxyUrl) {
		this.versionOneProxyUrl = versionOneProxyUrl;
	}

	public void setRallyBaseUri(String rallyBaseUri) {
		this.rallyBaseUri = rallyBaseUri;
	}

	public void setRallyApiKey(String rallyApiKey) {
		this.rallyApiKey = rallyApiKey;
	}

	public int getMaxKanbanIterationLength() {
		return maxKanbanIterationLength;
	}

	public void setMaxKanbanIterationLength(int maxKanbanIterationLength) {
		this.maxKanbanIterationLength = maxKanbanIterationLength;
	}

}