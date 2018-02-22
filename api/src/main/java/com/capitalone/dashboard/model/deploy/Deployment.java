package com.capitalone.dashboard.model.deploy;

import java.util.Date;

public class Deployment {

	private Date deployDate;

	private String farm;

	private String clusterName;
	private String environment;
	private String application;
	private String appVersion;
	private String blJenkins;
	private String blDeploy;
	private String blSmokeTest;
	private String blValidation;
	private String appType;

	public Date getDeployDate() {
		return deployDate;
	}

	public void setDeployDate(Date deployDate) {
		this.deployDate = deployDate;
	}

	public String getFarm() {
		return farm;
	}

	public void setFarm(String farm) {
		this.farm = farm;
	}

	public String getClusterName() {
		return clusterName;
	}

	public void setClusterName(String clusterName) {
		this.clusterName = clusterName;
	}

	public String getEnvironment() {
		return environment;
	}

	public void setEnvironment(String environment) {
		this.environment = environment;
	}

	public String getApplication() {
		return application;
	}

	public void setApplication(String application) {
		this.application = application;
	}

	public String getAppVersion() {
		return appVersion;
	}

	public void setAppVersion(String appVersion) {
		this.appVersion = appVersion;
	}

	public String getBlJenkins() {
		return blJenkins;
	}

	public void setBlJenkins(String blJenkins) {
		this.blJenkins = blJenkins;
	}

	public String getBlDeploy() {
		return blDeploy;
	}

	public void setBlDeploy(String blDeploy) {
		this.blDeploy = blDeploy;
	}

	public String getBlSmokeTest() {
		return blSmokeTest;
	}

	public void setBlSmokeTest(String blSmokeTest) {
		this.blSmokeTest = blSmokeTest;
	}

	public String getBlValidation() {
		return blValidation;
	}

	public void setBlValidation(String blValidation) {
		this.blValidation = blValidation;
	}

	public String getAppType() {
		return appType;
	}

	public void setAppType(String appType) {
		this.appType = appType;
	}

}