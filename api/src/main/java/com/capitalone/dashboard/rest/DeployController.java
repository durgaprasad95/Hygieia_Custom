package com.capitalone.dashboard.rest;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.web.bind.annotation.RequestMethod.GET;

import java.io.IOException;
import java.net.URL;
import java.util.Arrays;
import java.util.List;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.capitalone.dashboard.model.deploy.Deployment;
import com.fasterxml.jackson.core.JsonParser.Feature;
import com.fasterxml.jackson.databind.ObjectMapper;

@RestController
public class DeployController {

	/**
	 * REST endpoint for retrieving Deployments from BladeLogic Server
	 * 
	 * @return A list of type Deployment containing all deploy data
	 */
	@RequestMapping(value = "/deploy/bladelogic/collect", method = GET, produces = APPLICATION_JSON_VALUE)
	public List<Deployment> BladeLogicData() {
		List<Deployment> deploys = null;
		ObjectMapper objectMapper = null;
		objectMapper = new ObjectMapper();
		objectMapper.configure(Feature.AUTO_CLOSE_SOURCE, true);
		try {
			URL url = new URL("http://localhost:8083/collect");
			Deployment[] temp = objectMapper.readValue(url, Deployment[].class);
			deploys = Arrays.asList(temp);
		} catch (IOException e) {
			System.out.println(e.getClass().getSimpleName());
		}
		return deploys;
	}

}