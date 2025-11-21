package com.graphhopper.routing.querygraph;

import com.graphhopper.storage.IntsRef;
import com.graphhopper.storage.index.Snap;
import com.graphhopper.util.EdgeIteratorState;
import com.graphhopper.util.FetchMode;
import com.graphhopper.util.PointList;
import com.graphhopper.util.shapes.GHPoint;
import com.graphhopper.util.shapes.GHPoint3D;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.Collections;
import java.util.List;

import static org.mockito.Mockito.*;

public class QueryOverlayBuilderTest {

    private QueryOverlay qo;
    
    @Test
    public void noVirtualNodeIfConsideredEqual() {

        //arbitriry points
        PointList pl = new PointList(2, true);
        pl.add(47.0, 10.0, 0.0);
        pl.add(49.0, 12.0, 0.0);
        //arbirtrarily close snap coordinates betweeen points
        double s_lat1 = 48.123456700;
        double s_lon1 = 11.987654300;
        double s_lat2 = 48.123456705;
        double s_lon2 = 11.987654301;
        //user coordinates between points
        double u_lat = 48.0;
        double u_lon = 12.0;
    
        //mocked edge
        EdgeIteratorState edge = mock(EdgeIteratorState.class);
        when(edge.getFlags()).thenReturn(new IntsRef(1));
        when(edge.getBaseNode()).thenReturn(0);
        when(edge.getAdjNode()).thenReturn(1);
        when(edge.getEdge()).thenReturn(5);
        when(edge.fetchWayGeometry(FetchMode.ALL)).thenReturn(pl);
        when(edge.getEdgeKey()).thenReturn(77);
        when(edge.getReverseEdgeKey()).thenReturn(78);
            
        Snap s1 = new Snap(u_lat, u_lon);
        s1.setClosestEdge(edge);
        s1.setSnappedPosition(Snap.Position.EDGE);
        s1.setSnappedPoint(new GHPoint3D(s_lat1, s_lon1, 0.0));
        s1.setWayIndex(0);
     
        Snap s2 = new Snap(u_lat, u_lon);
        s2.setClosestEdge(edge);
        s2.setSnappedPosition(Snap.Position.EDGE);
        s2.setSnappedPoint(new GHPoint3D(s_lat2, s_lon2, 0.0));
        s2.setWayIndex(1);
       
        int firstVirtNode = 1000;
            
        List<Snap> snaps = List.of(s1, s2);
        qo = QueryOverlayBuilder.build(firstVirtNode, 2000, true, snaps);
    
        assertEquals(firstVirtNode, s1.getClosestNode());
        assertEquals(firstVirtNode, s2.getClosestNode());
        assertEquals(1, qo.getVirtualNodes().size());
        assertEquals(4, qo.getNumVirtualEdges());
    
    }
    @Test
    public void testDistanceBasedWayIndexOrder() {

        //arbitriry points
        PointList pl = new PointList(3, true);
        pl.add(47.0, 10.0, 0.0);
        pl.add(49.0, 12.0, 0.0);
        pl.add(51.0, 14.0, 0.0);
        //ordered snap coordinates on line segment
        double s_lat0 = 48.5;
        double s_lon0 = 11.5;
        double s_lat1 = 49.0;
        double s_lon1 = 12.0;
        double s_lat2 = 50.0;
        double s_lon2 = 13.0;
        //user coordinates between points
        double u_lat = 48.0;
        double u_lon = 12.0;
    
        //mocked edge
        EdgeIteratorState edge = mock(EdgeIteratorState.class);
        when(edge.getFlags()).thenReturn(new IntsRef(1));
        when(edge.getBaseNode()).thenReturn(0);
        when(edge.getAdjNode()).thenReturn(1);
        when(edge.getEdge()).thenReturn(5);
        when(edge.fetchWayGeometry(FetchMode.ALL)).thenReturn(pl);
        when(edge.getEdgeKey()).thenReturn(77);
        when(edge.getReverseEdgeKey()).thenReturn(78);
            
        Snap s0 = new Snap(u_lat, u_lon);
        s0.setClosestEdge(edge);
        s0.setSnappedPosition(Snap.Position.EDGE);
        s0.setSnappedPoint(new GHPoint3D(s_lat0, s_lon0, 0.0));
        s0.setWayIndex(0);
        
        Snap s1 = new Snap(u_lat, u_lon);
        s1.setClosestEdge(edge);
        s1.setSnappedPosition(Snap.Position.EDGE);
        s1.setSnappedPoint(new GHPoint3D(s_lat1, s_lon1, 0.0));
        s1.setWayIndex(0);

        Snap s2 = new Snap(u_lat, u_lon);
        s2.setClosestEdge(edge);
        s2.setSnappedPosition(Snap.Position.EDGE);
        s2.setSnappedPoint(new GHPoint3D(s_lat2, s_lon2, 0.0));
        s2.setWayIndex(0);
     
        int firstVirtNode = 1000;
            
        List<Snap> snaps = List.of(s1, s2, s0);
        
        qo = QueryOverlayBuilder.build(firstVirtNode, 2000, true, snaps);

        assertEquals(qo.getVirtualNodes().getLat(0), s0.getSnappedPoint().lat, 1e-6);
        assertEquals(qo.getVirtualNodes().getLon(0), s0.getSnappedPoint().lon, 1e-6);
        
        assertEquals(qo.getVirtualNodes().getLat(1), s1.getSnappedPoint().lat, 1e-6);
        assertEquals(qo.getVirtualNodes().getLon(1), s1.getSnappedPoint().lon, 1e-6);
        
        assertEquals(qo.getVirtualNodes().getLat(2), s2.getSnappedPoint().lat, 1e-6);
        assertEquals(qo.getVirtualNodes().getLon(2), s2.getSnappedPoint().lon, 1e-6);

    }
}