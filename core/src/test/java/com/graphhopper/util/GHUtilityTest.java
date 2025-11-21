/*
 *  Licensed to GraphHopper GmbH under one or more contributor
 *  license agreements. See the NOTICE file distributed with this work for
 *  additional information regarding copyright ownership.
 *
 *  GraphHopper GmbH licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except in
 *  compliance with the License. You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package com.graphhopper.util;

import com.graphhopper.storage.RoutingCHEdgeIterator;
import com.graphhopper.storage.NodeAccess;
import com.graphhopper.storage.Graph;
//import static com.graphhopper.util.DistancePlaneProjection;

import com.graphhopper.coll.GHIntLongHashMap;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.util.LinkedHashSet;
import java.util.Set;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import static org.mockito.Mockito.*;

/**
 * @author Peter Karich
 */
@ExtendWith(FailureWatcher.class)
public class GHUtilityTest {

    @Test
    public void testEdgeStuff() {
        assertEquals(2, GHUtility.createEdgeKey(1, false));
        assertEquals(3, GHUtility.createEdgeKey(1, true));
    }

    @Test
    public void testZeroValue() {
        GHIntLongHashMap map1 = new GHIntLongHashMap();
        assertFalse(map1.containsKey(0));
        // assertFalse(map1.containsValue(0));
        map1.put(0, 3);
        map1.put(1, 0);
        map1.put(2, 1);

        // assertTrue(map1.containsValue(0));
        assertEquals(3, map1.get(0));
        assertEquals(0, map1.get(1));
        assertEquals(1, map1.get(2));

        // instead of assertEquals(-1, map1.get(3)); with hppc we have to check before:
        assertTrue(map1.containsKey(0));

        // trove4j behaviour was to return -1 if non existing:
//        TIntLongHashMap map2 = new TIntLongHashMap(100, 0.7f, -1, -1);
//        assertFalse(map2.containsKey(0));
//        assertFalse(map2.containsValue(0));
//        map2.add(0, 3);
//        map2.add(1, 0);
//        map2.add(2, 1);
//        assertTrue(map2.containsKey(0));
//        assertTrue(map2.containsValue(0));
//        assertEquals(3, map2.get(0));
//        assertEquals(0, map2.get(1));
//        assertEquals(1, map2.get(2));
//        assertEquals(-1, map2.get(3));
    }

    @Test
    public void testCount() {

        EdgeIterator iter = mock(EdgeIterator.class);
        when(iter.next()).thenReturn(true).thenReturn(true).thenReturn(true).thenReturn(false);
        assertEquals(3, GHUtility.count(iter));

        RoutingCHEdgeIterator r_iter = mock(RoutingCHEdgeIterator.class);
        when(r_iter.next()).thenReturn(true).thenReturn(true).thenReturn(true).thenReturn(false);
        assertEquals(3, GHUtility.count(r_iter));
    }

    @Test
    public void testGetNeighbors() {

        Set<Integer> list = new LinkedHashSet<>();
        list.add(3);
        list.add(6);
        list.add(7);
        list.add(8);

        EdgeIterator iter = mock(EdgeIterator.class);
        when(iter.next())
            .thenReturn(true)
            .thenReturn(true)
            .thenReturn(true)
            .thenReturn(true)
            .thenReturn(false);
        when(iter.getAdjNode())
            .thenReturn(3)
            .thenReturn(6)
            .thenReturn(7)
            .thenReturn(8);
        
        assertEquals(list, GHUtility.getNeighbors(iter));
    }
    @Test
    public void testGetEdgeIds() {

        List<Integer> list = new ArrayList<>();
        list.add(11);
        list.add(4);
        list.add(8);

        EdgeIterator iter = mock(EdgeIterator.class);
        when(iter.next())
            .thenReturn(true)
            .thenReturn(true)
            .thenReturn(true)
            .thenReturn(false);
        when(iter.getEdge())
            .thenReturn(11)
            .thenReturn(4)
            .thenReturn(8);

        assertEquals(list, GHUtility.getEdgeIds(iter));
    }
    @Test
    public void testGetDistance() {

        int id_from = 1;
        int id_to = 2;

        NodeAccess na = mock(NodeAccess.class);
        when(na.getLat(id_from)).thenReturn(13.0);
        when(na.getLon(id_from)).thenReturn(55.0);
        when(na.getLat(id_to)).thenReturn(42.0);
        when(na.getLon(id_to)).thenReturn(30.0);

        assertEquals(DistancePlaneProjection.DIST_PLANE.calcDist(13.0, 55.0, 42.0, 30.0), GHUtility.getDistance(1, 2, na));
    }
    @Test
    public void testGetEdge_OneEdge() {

        int base = 1;
        int adj = 2;

        EdgeExplorer ex = mock(EdgeExplorer.class);
        when(ex.setBaseNode(base)).thenAnswer(inv -> {
            EdgeIterator iter = mock(EdgeIterator.class);
            when(iter.next()).thenReturn(true).thenReturn(false);
            when(iter.getAdjNode()).thenReturn(adj);
            return iter;
        });
        
        Graph g = mock(Graph.class);
        when(g.createEdgeExplorer()).thenReturn(ex);

        assertTrue(GHUtility.getEdge(g, base, adj) instanceof EdgeIterator);
    }
    @Test
    public void testGetEdge_NullEdge() {

        int base = 1;
        int adj = 2;

        EdgeExplorer ex = mock(EdgeExplorer.class);
        when(ex.setBaseNode(base)).thenAnswer(inv -> {
            EdgeIterator iter = mock(EdgeIterator.class);
            when(iter.next()).thenReturn(false);
            return iter;
        });
        
        Graph g = mock(Graph.class);
        when(g.createEdgeExplorer()).thenReturn(ex);

        assertEquals(GHUtility.getEdge(g, base, adj), null);
    }
    @Test
    public void testGetEdge_NoEdge() {

        int base = 1;
        int adj = 2;
        int adj2 = 3;

        EdgeExplorer ex = mock(EdgeExplorer.class);
        when(ex.setBaseNode(base)).thenAnswer(inv -> {
            EdgeIterator iter = mock(EdgeIterator.class);
            when(iter.next()).thenReturn(true).thenReturn(false);
            when(iter.getAdjNode()).thenReturn(adj);
            return iter;
        })
            .thenAnswer(inv -> {
            EdgeIterator iter = mock(EdgeIterator.class);
            when(iter.next()).thenReturn(true).thenReturn(false);
            when(iter.getAdjNode()).thenReturn(adj2);
            return iter;
        });
        
        Graph g = mock(Graph.class);
        when(g.createEdgeExplorer()).thenReturn(ex);

        assertThrows(IllegalStateException.class, () -> {GHUtility.getEdge(g, base, adj);});
    }
    @Test
    public void testGetEdge_MultipleEdges() {

        int base = 1;
        int adj = 2;

        EdgeExplorer ex = mock(EdgeExplorer.class);
        when(ex.setBaseNode(base)).thenAnswer(inv -> {
            EdgeIterator iter = mock(EdgeIterator.class);
            when(iter.next()).thenReturn(true).thenReturn(true).thenReturn(false);
            when(iter.getAdjNode()).thenReturn(adj).thenReturn(adj);
            return iter;
        });
        
        Graph g = mock(Graph.class);
        when(g.createEdgeExplorer()).thenReturn(ex);

        assertThrows(IllegalArgumentException.class, () -> {GHUtility.getEdge(g, base, adj);});
    }
}
