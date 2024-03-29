package org.riv.mesh.rest;

import java.util.LinkedHashMap;
import java.util.Map;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.riv.node.PeerInfo;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import listener.ScheduleYggdrasilPeerReaderListener;

@Path("/peers.json")
public class PeersService {
	
	Gson gson = new GsonBuilder().setPrettyPrinting().create();

    @GET
    @Produces({MediaType.APPLICATION_JSON})
    public Response getPeers() {
    	Map<String, LinkedHashMap<String, PeerInfo>> map = ScheduleYggdrasilPeerReaderListener.peersPerCountry;
        return Response.status(Status.OK).entity(gson.toJson(map)).build();
    }

}