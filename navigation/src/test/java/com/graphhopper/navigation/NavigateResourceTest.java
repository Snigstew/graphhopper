package com.graphhopper.navigation;

import com.graphhopper.util.TranslationMap;
import com.graphhopper.util.shapes.GHPoint;
import com.graphhopper.util.PointList;
import com.graphhopper.routing.util.EncodingManager;
import com.graphhopper.GraphHopper;
import com.graphhopper.GraphHopperConfig;
import com.graphhopper.GHRequest;
import com.graphhopper.GHResponse;
import com.graphhopper.ResponsePath;

import org.junit.jupiter.api.Test;

import static org.mockito.Mockito.*;
import com.github.javafaker.Faker;

import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Locale;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.core.UriInfo;
import jakarta.ws.rs.core.Response;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class NavigateResourceTest {


    @Test
    public void voiceInstructionsTest() {

        List<Double> bearings = NavigateResource.getBearing("");
        assertEquals(0, bearings.size());
        assertEquals(Collections.EMPTY_LIST, bearings);

        bearings = NavigateResource.getBearing("100,1");
        assertEquals(1, bearings.size());
        assertEquals(100, bearings.get(0), .1);

        bearings = NavigateResource.getBearing(";100,1;;");
        assertEquals(4, bearings.size());
        assertEquals(100, bearings.get(1), .1);
    }

    //Ensures invalid input for bearings throws exceptions:
    //  non-numerical values
    //  angle values without corresponding range
    //  comma only
    @Test
    public void invalidBearingsThrowsException() {

        Random random = new Random();
              
        //String throws exception
        assertThrows(IllegalArgumentException.class, () -> {
            NavigateResource.getBearing("abc,2");
        });

        assertThrows(IllegalArgumentException.class, () -> {
            NavigateResource.getBearing((random.nextDouble()*360) + ",10;no,15;;");
        });

        //Single value throws exception
        assertThrows(IllegalArgumentException.class, () -> {
            NavigateResource.getBearing(String.valueOf(random.nextDouble()*360));
        });

        //Comma throws exception
        assertThrows(ArrayIndexOutOfBoundsException.class, () -> {
            NavigateResource.getBearing(",");
        });

        assertThrows(IllegalArgumentException.class, () -> {
            NavigateResource.getBearing((random.nextDouble()*360) + ",30;," + (random.nextDouble()*360));
        });
    }

    //Ensures invalid or unsupported parameters in doPost
    //throws IllegalArgumentExceptions. Only specific values
    //are supported, therefore important to test all invalid
    //entries
    @Test
    public void invalidDoPostThrowsException() {

        Faker faker = new Faker();

        HttpServletRequest req = mock(HttpServletRequest.class);
        GraphHopper hopper = mock(GraphHopper.class);
        TranslationMap map = new TranslationMap();
        GraphHopperConfig gconf = new GraphHopperConfig();

        NavigateResource res = new NavigateResource(hopper, map, gconf);
        
        assertThrows(IllegalArgumentException.class, () -> {
            res.doPost(new GHRequest().putHint("geometry", "not_polyline6"), req);
        });
        
        assertThrows(IllegalArgumentException.class, () -> {
            res.doPost(new GHRequest().putHint("steps", false), req);
        });
        
        assertThrows(IllegalArgumentException.class, () -> {
            res.doPost(new GHRequest().putHint("roundabout_exits", false), req);
        });
        
        assertThrows(IllegalArgumentException.class, () -> {
            res.doPost(new GHRequest().putHint("voice_instructions", false), req);
        });

        assertThrows(IllegalArgumentException.class, () -> {
            res.doPost(new GHRequest().putHint("banner_instructions", false), req);
        });
        
        assertThrows(IllegalArgumentException.class, () -> {
            res.doPost(new GHRequest().putHint("elevation", true), req);
        });
        
        assertThrows(IllegalArgumentException.class, () -> {
            res.doPost(new GHRequest().putHint("overview", "empty"), req);
        });

        assertThrows(IllegalArgumentException.class, () -> {
            res.doPost(new GHRequest().putHint("language", "fr"), req);
        });

        assertThrows(IllegalArgumentException.class, () -> {
            res.doPost(new GHRequest().putHint("points_encoded", false), req);
        });

        assertThrows(IllegalArgumentException.class, () -> {
            res.doPost(new GHRequest().putHint("points_encoded_multiplier", 1e8), req);
        });
        
        assertThrows(IllegalArgumentException.class, () -> {
            res.doPost(new GHRequest().putHint("type", "not_mapbox"), req);
        });
        
    }

    //Ensures invalid or unsupported parameters in doGet
    //throws IllegalArgumentExceptions. The following
    //attributes must be true:
    //  - steps
    //  - roundaboutExits
    //  - bannerInstructions
    //  - voiceInstructions
    //And geometries must be equal to "polyline6"
    //All these cases are tested below
    @Test
    public void invalidDoGetParameterThrowsException() {

        Faker faker = new Faker();

        HttpServletRequest req = mock(HttpServletRequest.class);
        ContainerRequestContext rc = mock(ContainerRequestContext.class);
        UriInfo uriInfo = mock(UriInfo.class);

        GraphHopper hopper = mock(GraphHopper.class);
        TranslationMap map = new TranslationMap();
        GraphHopperConfig gconf = new GraphHopperConfig();

        NavigateResource res = new NavigateResource(hopper, map, gconf);

        //Check polyline6
        assertThrows(IllegalArgumentException.class, () -> {
            res.doGet(req, uriInfo, rc, true, true, true, true, "metric", "simplified", "not_polyline6", "", "en", "driving");
        });
        //Check enable steps
        assertThrows(IllegalArgumentException.class, () -> {
            res.doGet(req, uriInfo, rc, false, true, true, true, "metric", "simplified", "polyline6", "", "en", "driving");
        });
        //Check roundabout exits
        assertThrows(IllegalArgumentException.class, () -> {
            res.doGet(req, uriInfo, rc, true, true, true, false, "metric", "simplified", "polyline6", "", "en", "driving");
        });
        //Check enable voice instructions
        assertThrows(IllegalArgumentException.class, () -> {
            res.doGet(req, uriInfo, rc, true, false, true, true, "metric", "simplified", "polyline6", "", "en", "driving");
        });
        //Check enable banner instructions
        assertThrows(IllegalArgumentException.class, () -> {
            res.doGet(req, uriInfo, rc, true, true, false, true, "metric", "simplified", "polyline6", "", "en", "driving");
        });
        
    }

    //Ensures invalid response from HttpServletRequest
    //throws IllegalArgumentExceptions. Here javafaker
    //and Mockito are used to mock several class
    //behaviours in order to ensure the errors thrown
    //in the constructor of doGet are thrown under the
    //proper conditions. Cases include:
    //  - wrong coordinates
    //  - wrong profile name
    //  - wrong path
    @Test
    public void doGetConstructorTest() {

        Faker faker = new Faker();

        HttpServletRequest req = mock(HttpServletRequest.class);
        ContainerRequestContext rc = mock(ContainerRequestContext.class);
        UriInfo uriInfo = mock(UriInfo.class);

        GraphHopper hopper = mock(GraphHopper.class);
        TranslationMap map = new TranslationMap();
        GraphHopperConfig gconf = new GraphHopperConfig();

        NavigateResource res = new NavigateResource(hopper, map, gconf);

        String profile = faker.name().username();
        String urlStart = "/navigate/directions/v5/gh/" + profile + "/";
        String urlStartBadProfile = "/navigate/directions/v5/gh/Bob/";
        String urlStartBadPath = "/nav/dir/v5/gh/" + profile + "/";
        String coord1 = faker.address().longitude() + "," + faker.address().latitude();
        String coord2 = faker.address().longitude() + "," + faker.address().latitude();

        //Bad coords
        when(req.getRequestURI()).thenReturn(urlStart + coord1 + ";" + coord2);
        assertThrows(IllegalArgumentException.class, () -> {
            res.doGet(req, uriInfo, rc, false, false, false, false, "metric", "simplified", "polyline6", "", "en", "driving");
        });

        //Bad profile
        when(req.getRequestURI()).thenReturn(urlStartBadProfile + coord1 + ";" + coord2);

        assertThrows(IllegalArgumentException.class, () -> {
            res.doGet(req, uriInfo, rc, false, false, false, false, "metric", "simplified", "polyline6", "", "en", "driving");
        });

        //Bad path
        when(req.getRequestURI()).thenReturn(urlStartBadPath + coord1 + ";" + coord2);

        assertThrows(IllegalArgumentException.class, () -> {
            res.doGet(req, uriInfo, rc, false, false, false, false, "metric", "simplified", "polyline6", "", "en", "driving");
        });

    }

    //Ensures doGet returns 422 when GHRequest has errors
    //
    @Test
    public void doGetWithErrorsReturns422() {

        Faker faker = new Faker();
        
        HttpServletRequest req = mock(HttpServletRequest.class);
        ContainerRequestContext rc = mock(ContainerRequestContext.class);
        UriInfo uriInfo = mock(UriInfo.class);

        GraphHopper hopper = mock(GraphHopper.class);
        TranslationMap map = new TranslationMap();
        GraphHopperConfig gconf = new GraphHopperConfig();
        GHResponse gh_res = mock(GHResponse.class);

        String profile = faker.name().username();
        String urlStart = "/navigate/directions/v5/gh/" + profile + "/";
        String coord1 = faker.address().longitude() + "," + faker.address().latitude();
        String coord2 = faker.address().longitude() + "," + faker.address().latitude();

        when(req.getRequestURI()).thenReturn(urlStart + coord1 + ";" + coord2);
        
        when(gh_res.hasErrors()).thenReturn(true);
        when(gh_res.getErrors()).thenReturn(List.of(new RuntimeException("Routing error")));
        
        when(hopper.route(any(GHRequest.class))).thenReturn(gh_res);

        NavigateResource res = new NavigateResource(hopper, map, gconf);

        Response resp = res.doGet(req, uriInfo, rc, true, true, true, true, "metric", "simplified", "polyline6", "", "en", profile);

        assertEquals(422, resp.getStatus());
    }

    //Ensures doPost returns 200 when GHResponse has no errors
    @Test
    public void doPostReturns200() {

        HttpServletRequest req = mock(HttpServletRequest.class);
        GraphHopper hopper = mock(GraphHopper.class);
        TranslationMap map = new TranslationMap();
        GraphHopperConfig gconf = new GraphHopperConfig();
        GHResponse gh_res = mock(GHResponse.class);
        EncodingManager enc = mock(EncodingManager.class);
        ResponsePath path = mock(ResponsePath.class);
        PointList p = new PointList();

        when(path.getWaypoints()).thenReturn(p);
        when(enc.hasEncodedValue(anyString())).thenReturn(true);

        when(gh_res.hasErrors()).thenReturn(false);
        when(gh_res.getBest()).thenReturn(path);
        
        when(hopper.route(any(GHRequest.class))).thenReturn(gh_res);
        when(hopper.getEncodingManager()).thenReturn(enc);
        when(hopper.getNavigationMode(anyString()))
            .thenReturn(com.graphhopper.routing.util.TransportationMode.CAR);


        NavigateResource res = new NavigateResource(hopper, map, gconf);

        GHRequest gh_req = new GHRequest();
        gh_req.putHint("type", "mapbox");

        Response resp = res.doPost(gh_req, req);

        assertEquals(200, resp.getStatus());
    }
}

/*
Mutation score before additional test methods:

>> Line Coverage (for mutated classes only): 400/509 (79%)
>> Generated 213 mutations Killed 113 (53%)
>> Mutations with no coverage 48. Test strength 68%
>> Ran 105 tests (0.49 tests per mutation)

Mutation score after adding additional test methods
    doPostReturns200
    doGetWithErrorsReturns422
    doGetConstructorTest
    invalidDoGetParameterThrowsException
    invalidDoPostThrowsException
    invalidBearingsThrowsException
    
>> Line Coverage (for mutated classes only): 454/509 (89%)
>> Generated 213 mutations Killed 154 (72%)
>> Mutations with no coverage 27. Test strength 83%
>> Ran 542 tests (2.54 tests per mutation)
*/
