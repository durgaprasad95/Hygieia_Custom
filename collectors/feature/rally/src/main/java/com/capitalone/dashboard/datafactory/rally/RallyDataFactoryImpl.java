package com.capitalone.dashboard.datafactory.rally;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;

import com.capitalone.dashboard.collector.FeatureSettings;
import com.capitalone.dashboard.model.Feature;
import com.capitalone.dashboard.model.Iteration;
import com.capitalone.dashboard.model.Scope;
import com.capitalone.dashboard.model.ScopeOwnerCollectorItem;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.rallydev.rest.RallyRestApi;
import com.rallydev.rest.request.QueryRequest;
import com.rallydev.rest.response.QueryResponse;
import com.rallydev.rest.util.Fetch;
import com.rallydev.rest.util.QueryFilter;

public class RallyDataFactoryImpl implements RallyDataFactory {
	private FeatureSettings featureSettings;

	@Autowired
	public RallyDataFactoryImpl(FeatureSettings featureSettings) {
		this.featureSettings = featureSettings;
	}

	public RallyRestApi getRallyClient() throws URISyntaxException {
		URI url = new URI(featureSettings.getRallyBaseUri());
		String key = featureSettings.getRallyApiKey();
		return new RallyRestApi(url, key);
	}

	public List<Scope> getProjects() throws URISyntaxException, IOException {
		List<Scope> projects = new ArrayList<>();
		RallyRestApi restApi = getRallyClient();
		QueryRequest request = new QueryRequest("Projects");
		request.setFetch(new Fetch("Name", "ObjectID", "CreationDate", "State"));
		request.setScopedDown(true);
		// request.setQueryFilter(new QueryFilter("State","=","Planning"));
		QueryResponse response = restApi.query(request);
		if (response.getResults() != null) {
			for (JsonElement result : response.getResults()) {
				JsonObject project = result.getAsJsonObject();
				/*
				 * if(project.get("Name").getAsString().contains(featureSettings.
				 * getApplicationName())){ }
				 */
				Scope scope = new Scope();
				scope.setpId(project.get("ObjectID").getAsString());
				scope.setName(project.get("Name").getAsString());
				scope.setBeginDate(project.get("CreationDate").getAsString());
				scope.setAssetState("Active");
				projects.add(scope);
			}
		}
		restApi.close();
		return projects;
	}

	public List<ScopeOwnerCollectorItem> getUsers() throws URISyntaxException, IOException {
		List<ScopeOwnerCollectorItem> users = new ArrayList<>();
		RallyRestApi restApi = getRallyClient();
		QueryRequest request = new QueryRequest("Projects");
		request.setFetch(new Fetch("Name", "ObjectID", "CreationDate", "State"));
		request.setScopedDown(true);
		// request.setQueryFilter(new QueryFilter("State","=","Planning"));
		QueryResponse response = restApi.query(request);
		if (response.getResults() != null) {
			for (JsonElement result : response.getResults()) {
				JsonObject project = result.getAsJsonObject();
				/*
				 * if(project.get("Name").getAsString().contains(featureSettings.
				 * getApplicationName())){ }
				 */
				ScopeOwnerCollectorItem user = new ScopeOwnerCollectorItem();
				user.setTeamId((project.get("ObjectID").getAsString()));
				user.setName(project.get("Name").getAsString());
				user.setAssetState("Active");
				users.add(user);
			}
		}
		return users;
	}

	@SuppressWarnings("unchecked")
	public List<Feature> getStories(String agileType, String storyType) throws URISyntaxException, IOException {
		List<Feature> stories = new ArrayList<>();
		RallyRestApi restApi = getRallyClient();
		String projectRef = "/project/";
		for (ScopeOwnerCollectorItem user : getUsers()) {
			int i = 0;
			String objectID = user.getTeamId();
			QueryRequest request = null;
			if (storyType.equals("story")) {
				request = new QueryRequest("HierarchicalRequirement");
			} else if (storyType.equals("defect")) {
				request = new QueryRequest("Defects");
			}
			request.setFetch(new Fetch("Name", "ObjectID", "FormattedID", "PlanEstimate", "TaskStatus",
					"LastUpdateDate", "ScheduleState", "CreationDate", "Owner", "Project", "Iteration", "Parent"));
			request.setScopedDown(true);
			request.setProject(projectRef + objectID);
			System.out.println(objectID);
			for (String iterationName : getFilterItearations(restApi, projectRef, objectID,
					featureSettings.getFilter())) {
				if (agileType.equals("scrum")) {
					request.setQueryFilter(new QueryFilter("Iteration.Name", "=", iterationName));
				} else if (agileType.equals("kanban")) {
					request.setQueryFilter(new QueryFilter("Iteration.Name", "=", null)
							.and(new QueryFilter("ScheduleState", "!=", "Backlog")));
				}
				QueryResponse response = restApi.query(request);
				if (response.getResults() != null) {
					for (JsonElement result : response.getResults()) {
						System.out.println(++i);
						Feature feature = new Feature();
						JsonObject project = result.getAsJsonObject();
						feature.setsId(project.get("ObjectID").getAsString());
						feature.setsNumber(project.get("FormattedID").getAsString());
						feature.setsName(project.get("Name").getAsString());
						// feature.setsTypeId(project.get("").getAsString());
						feature.setsTypeName(project.get("_type").getAsString());
						feature.setsStatus(project.get("ScheduleState").getAsString());
						if (!project.get("TaskStatus").isJsonNull()) {
							feature.setsState(project.get("TaskStatus").getAsString());
						}
						if (!project.get("PlanEstimate").isJsonNull()) {
							feature.setsEstimate(project.get("PlanEstimate").getAsString());
							// feature.setsEstimateTime(project.get("").getAsInt());
						}
						feature.setsUrl(project.get("_ref").getAsString());
						if (feature.getsTypeName().equals("HierarchiaclRequirement")) {
							feature.setChangeDate(project.get("LastUpdateDate").getAsString());
						} else {
							feature.setChangeDate(project.get("CreationDate").getAsString());
						}
						feature.setIsDeleted("False");
						if (!project.getAsJsonObject("Project").get("_ref").getAsString().equals(null)) {
							String projectUrl = project.getAsJsonObject("Project").get("_ref").getAsString();
							Scope temp = getCurrentProject(projectUrl);
							feature.setsProjectPath(projectUrl);
							feature.setsTeamAssetState(temp.getAssetState());
							feature.setsTeamName(temp.getName());
							feature.setsTeamID(temp.getpId());
							feature.setsProjectName(temp.getName());
							feature.setsProjectID(temp.getpId());
							feature.setsProjectState(temp.getAssetState());
						}
						try {
							if (!project.get("Parent").isJsonNull()) {
								try {
									if (!project.getAsJsonObject("Parent").get("_ref").getAsString().equals(null))
										feature.setsEpicUrl(
												project.getAsJsonObject("Parent").get("_ref").getAsString());
								} catch (Exception e) {
									System.out.println("Cannot get the Parent Story Details for this Story");
								}
								Feature tempFeature = getSuperStory(feature.getsEpicUrl());
								feature.setsEpicID(tempFeature.getsEpicID());
								feature.setsEpicName(tempFeature.getsEpicName());
								feature.setsEpicBeginDate(tempFeature.getsEpicBeginDate());
								feature.setsEpicNumber(tempFeature.getsEpicNumber());
								feature.setsEpicAssetState(tempFeature.getsEpicAssetState());
								feature.setsEpicChangeDate(tempFeature.getsEpicChangeDate());
								feature.setsEpicIsDeleted("false");
								if (storyType.equals("story"))
									feature.setsEpicType("HierarchicalRequirement");
								else
									feature.setsEpicType("Defect");
							}
						} catch (Exception e) {
							feature.setsEpicID(null);
							feature.setsEpicIsDeleted("true");
						}

						try {
							if (!project.get("Iteration").isJsonNull()) {
								try {
									if (!project.getAsJsonObject("Iteration").get("_ref").getAsString().equals(null))
										feature.setsSprintUrl(
												project.getAsJsonObject("Iteration").get("_ref").getAsString());
								} catch (Exception e) {
									System.out.println(
											"Cannot get the Details of Iteration for " + feature.getsId() + " Story");
								}
								Iteration it = getCurrentIteration(feature.getsSprintUrl());
								feature.setsSprintID(it.getID());
								feature.setsSprintName(it.getName());
								feature.setsSprintBeginDate(it.getBeginDate());
								feature.setsSprintEndDate(it.getEndDate());
								feature.setsSprintIsDeleted("false");
								feature.setsSprintAssetState(it.getState());
							} else {
								// Issue #678 - leave sprint blank. Not having a sprint does not imply kanban
								// as a story on a scrum board without a sprint is really on the backlog
								// Instead the feature service is responsible for deducing if a sprint is part
								// of
								// kanban - see service for more details
								feature.setsSprintIsDeleted("false");
								feature.setsSprintID("");
								feature.setsSprintName("");
								feature.setsSprintBeginDate("");
								feature.setsSprintEndDate("");
								// feature.setsSprintAssetState(getJSONString(dataMainObj,
								// "Timebox.AssetState"));
							}
						} catch (Exception e) {
							e.printStackTrace();
						}
						stories.add(feature);
					}
				}
			}
		}
		restApi.close();
		return stories;

	}

	public Iteration getCurrentIteration(String url) throws IOException, URISyntaxException {
		RallyRestApi restApi = getRallyClient();
		Iteration it = new Iteration();
		QueryRequest iterationRequest = new QueryRequest("Iteration/" + url.substring(url.lastIndexOf("/") + 1));
		iterationRequest.setFetch(new Fetch("Name", "ObjectID", "StartDate", "State", "EndDate"));
		iterationRequest.setScopedDown(true);
		JsonObject iteration = ((JsonObject) new JsonParser()
				.parse(restApi.getClient().doGet(iterationRequest.toUrl()))).getAsJsonObject("Iteration");
		if (iteration != null) {
			if (iteration.get("_ref").getAsString().equals(url)) {
				it.setID(iteration.get("ObjectID").getAsString());
				it.setName(iteration.get("Name").getAsString());
				it.setStartDate(iteration.get("StartDate").getAsString());
				it.setEndDate(iteration.get("EndDate").getAsString());
				it.setState(iteration.get("State").getAsString());
			}
		}
		restApi.close();
		return it;
	}

	public Scope getCurrentProject(String url) throws IOException, URISyntaxException {
		Scope temp = new Scope();
		RallyRestApi restApi = getRallyClient();
		QueryRequest request = new QueryRequest("Project/" + url.substring(url.lastIndexOf("/") + 1));
		request.setFetch(new Fetch("Name", "ObjectID", "CreationDate", "State"));
		request.setScopedDown(true);
		JsonObject project = ((JsonObject) new JsonParser().parse(restApi.getClient().doGet(request.toUrl())))
				.getAsJsonObject("Project");
		if (project != null) {
			temp.setpId(project.get("ObjectID").getAsString());
			temp.setName(project.get("Name").getAsString());
			temp.setBeginDate(project.get("CreationDate").getAsString());
			temp.setAssetState(project.get("State").getAsString());
		}
		restApi.close();
		return temp;
	}

	public Feature getSuperStory(String url) throws IOException, URISyntaxException {
		Feature temp = new Feature();
		RallyRestApi restApi = getRallyClient();
		QueryRequest request = new QueryRequest("HierarchicalRequirement/" + url.substring(url.lastIndexOf("/") + 1));
		request.setFetch(
				new Fetch("Name", "ObjectID", "CreationDate", "ScheduleState", "FormattedID", "LastUpdateDate"));
		request.setScopedDown(true);
		JsonObject feature = ((JsonObject) new JsonParser().parse(restApi.getClient().doGet(request.toUrl())))
				.getAsJsonObject("HierarchicalRequirement");
		if (!feature.equals(null)) {
			temp.setsEpicID(feature.get("ObjectID").getAsString());
			temp.setsEpicName(feature.get("Name").getAsString());
			temp.setsEpicBeginDate(feature.get("CreationDate").getAsString());
			temp.setsEpicNumber(feature.get("FormattedID").getAsString());
			temp.setsEpicAssetState(feature.get("ScheduleState").getAsString());
			temp.setsEpicChangeDate(feature.get("LastUpdateDate").getAsString());
		}
		restApi.close();
		return temp;
	}

	private List<String> getFilterItearations(RallyRestApi restApi, String projectRef, String ObjectId, String filter)
			throws IOException, URISyntaxException {
		System.out.println(filter);
		List<String> filterList = new ArrayList<String>();
		QueryRequest request = new QueryRequest("project/" + ObjectId + "/Iterations");
		request.setScopedDown(true);
		request.setProject(projectRef + "/" + ObjectId);
		QueryResponse response = restApi.query(request);
		if (response.getResults() != null)
			for (JsonElement result : response.getResults()) {
				String temp = result.getAsJsonObject().get("Name").getAsString();
				if (temp.contains(filter)) {
					filterList.add(temp);
				}
			}
		return filterList;
	}

}