package de.uni.freiburg.iig.telematik.jagal.traverse;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ArrayBlockingQueue;

import de.invation.code.toval.validate.ParameterException;
import de.invation.code.toval.validate.Validate;
import de.uni.freiburg.iig.telematik.jagal.graph.exception.VertexNotFoundException;
import de.uni.freiburg.iig.telematik.jagal.traverse.Traverser.TraversalMode;
import de.uni.freiburg.iig.telematik.jagal.traverse.algorithms.SCCTarjan;

public class TraversalUtils {

        private static final ArrayBlockingQueue<Object> queue = new ArrayBlockingQueue<>(10);
        private static final Set<Object> visited = new HashSet<>();

        /**
         * Returns <code>true</code>, if the given traversable structure is
         * weakly connected.<br>
         * A directed graph is called weakly connected if all pairs of edges are connected ignoring the direction of an edge
         *
         * @param <V> 顶点类型
         * @param traversableStructure 实现了接口Traversable<V>的对象，AbstractGraph类实现了该接口，因此，Graph可以是此参数的实参
         * @return
         */
        public static <V extends Object> boolean isWeaklyConnected(Traversable<V> traversableStructure) {
                Validate.notNull(traversableStructure);
                for (V node : traversableStructure.getNodes()) {
                        Set<V> nodes = new HashSet<>();  // 顶点node的邻接点集合
                        try {
                                weakConnectivityRec(traversableStructure, node, nodes);
                        } catch (ParameterException | VertexNotFoundException e) {
                                // shouldn't be happening
                                throw new RuntimeException(e);
                        }
                        // nodes: 所有节点的邻接节点集合，该集合中不含重复顶点。（如果图是弱连通的，其数量=图的顶点数）
                        if (nodes.size() < traversableStructure.getNodes().size()) {
                                return false;
                        }
                        break;
                }
                return true;
        }

        /**
         * Returns <code>true</code>, if the given traversable structure is strongly connected.<br>
         * node是否可以到达图中的其它所有节点（不检查是否是封闭的，即检查p0->p1->p2->p3,即p0到其它3个节点都可到达，但是不检查是否p0->p1->p2-p3->p0）
         * A graph component is called strongly connected, if all pairs of vertices inside a component are reachable by each other.
         *
         * @param <V> 顶点类型
         * @param traversableStructure
         * @param node 开始顶点
         * @return
         */
        public static <V extends Object> boolean isStronglyConnected(Traversable<V> traversableStructure, V node) {
                int visitedNodes = 0;
                Iterator<V> iter = new Traverser<>(traversableStructure, node, TraversalMode.DEPTHFIRST);
                while (iter.hasNext()) {
                        iter.next();
                        visitedNodes++;
                }
                return visitedNodes == traversableStructure.nodeCount();
        }

        /**
         * 获取强连通分量<br>
         * 在有向图G中，如果两个顶点间至少存在一条路径，称两个顶点强连通(strongly connected)。
         * 如果有向图G的每两个顶点都强连通，称G是一个强连通图。非强连通图有向图的极大强连通子图，称为强连通分量(strongly connected components)。
         * @param
         * @param traversableStructure
         * @return
         * @throws ParameterException
         */
        public static <V extends Object> Set<Set<V>> getStronglyConnectedComponents(Traversable<V> traversableStructure) throws ParameterException {
                SCCTarjan<V> tarjan = new SCCTarjan<>();
                return tarjan.execute(traversableStructure);
        }

        public static <V extends Object> Set<V> getSiblings(Traversable<V> traversableStructure, V node) throws VertexNotFoundException, ParameterException {
                Validate.notNull(traversableStructure);
                Set<V> result = new HashSet<>();
                for (V parent : traversableStructure.getParents(node)) {
                        result.addAll(traversableStructure.getChildren(parent));
                }
                result.remove(node);
                return result;
        }

        /**
         * Checks, if <code>queryNode</code> is a predecessor of
         * <code>baseNode</code>.
         * 检查queyNode是baseNode的前继（包括前继的前继），即父节点或父节点的父节点
         * @param <V>
         * @param traversableStructure
         * @param baseNode Basic Node for predecessor search
         * @param queryNode Query Node for predecessor search
         * @return <code>true</code> if <code>queryNode</code> is a predecessor
         * of <code>baseNode</code>; <code>false</code> otherwise.
         * @throws VertexNotFoundException
         * @throws ParameterException
         */
        public static <V extends Object> boolean isPredecessor(Traversable<V> traversableStructure, V queryNode, V baseNode) throws VertexNotFoundException, ParameterException {
                Validate.notNull(traversableStructure);
                visited.clear();
                visited.add(baseNode);
                queue.clear();
                for (V parent : traversableStructure.getParents(baseNode)) {
                        queue.offer(parent);
                }
                while (!queue.isEmpty()) {
                        if (queryNode.equals(queue.peek())) {
                                return true;
                        }
                        //Cast is safe since only objects of type V were added to the queue before.
                        for (V parent : traversableStructure.getParents((V) queue.peek())) {
                        	    if (queryNode.equals(parent)) {  // djt add, 适于querNode在一个Cycle中
                                     return true;
                                }
                                if (!visited.contains(parent) && !queue.contains(parent)) {
                                        queue.add(parent);
                                }
                        }
                        visited.add(queue.poll());
                }
                return false;
        }

        /**
         * Checks, if <code>queryNode&lt;T&gt;</code> is a successor of
         * <code>baseNode&lt;T&gt;</code>.
         * 检查queyNode是baseNode的后继（包括后继的后继），即子节点或子节点的子节点
         * @param <V>
         * @param traversableStructure
         * @param baseNode Basic Node&lt;T&gt; for successor search
         * @param queryNode Query Node&lt;T&gt; for successor search
         * @return <code>true</code> if <code>queryNode&lt;T&gt;</code> is a
         * successor of <code>baseNode&lt;T&gt;</code>; <code>false</code>
         * otherwise.
         * @throws VertexNotFoundException
         * @throws ParameterException
         */
        public static <V extends Object> boolean isSuccessor(Traversable<V> traversableStructure, V queryNode, V baseNode) throws VertexNotFoundException, ParameterException {
                Validate.notNull(traversableStructure);
                visited.clear();
                visited.add(baseNode);
                queue.clear();
                for (V child : traversableStructure.getChildren(baseNode)) {
                        queue.offer(child);
                }
                while (!queue.isEmpty()) {
                        if (queryNode.equals(queue.peek())) {
                                return true;
                        }
                        //Cast is safe since only objects of type V were added to the queue before.
                        for (V child : traversableStructure.getChildren((V) queue.peek())) {
                        	    if (queryNode.equals(child)) {  // djt add, 适于querNode在一个Cycle中
                                    return true;
                                }
                                if (!visited.contains(child) && !queue.contains(child)) {
                                        queue.add(child);
                                }
                        }
                        visited.add(queue.poll());
                }
                return false;
        }

        /**
         * 获取startNode的所有前继，即父节点及父节点的父节点
         * @param traversableStructure
         * @param startNode
         * @return
         * @throws VertexNotFoundException
         * @throws ParameterException
         */
        public static <V extends Object> Set<V> getPredecessorsFor(Traversable<V> traversableStructure, V startNode) throws VertexNotFoundException, ParameterException {
                Validate.notNull(traversableStructure);
                Set<V> visitedNodes = new HashSet<>();
                visitedNodes.add(startNode);
                queue.clear();
                for (V parent : traversableStructure.getParents(startNode)) {
                        queue.offer(parent);
                }
                while (!queue.isEmpty()) {
                        //Cast is safe since only objects of type V were added to the queue before.
                        for (V parent : traversableStructure.getParents((V) queue.peek())) {
                                if (!visitedNodes.contains(parent) && !queue.contains(parent)) {
                                        queue.add(parent);
                                }
                        }
                        visitedNodes.add((V) queue.poll());
                }
                visitedNodes.remove(startNode);
                return visitedNodes;
        }

        //  获取startNode的所有后继，即子节点及子节点的子节点
        public static <V extends Object> Set<V> getSuccessorsFor(Traversable<V> traversableStructure, V startNode) throws VertexNotFoundException, ParameterException {
                Validate.notNull(traversableStructure);
                Set<V> visitedNodes = new HashSet<>();
                visitedNodes.add(startNode);
                queue.clear();
                for (V child : traversableStructure.getChildren(startNode)) {
                        queue.offer(child);
                }
                while (!queue.isEmpty()) {
                        //Cast is safe since only objects of type V were added to the queue before.
                        for (V child : traversableStructure.getChildren((V) queue.peek())) {
                                if (!visitedNodes.contains(child) && !queue.contains(child)) {
                                        queue.add(child);
                                }
                        }
                        visitedNodes.add((V) queue.poll());
                }
                visitedNodes.remove(startNode);
                return visitedNodes;
        }

        /**
         * Checks if a vertex is contained in a cycle.<br>
         * In case the given vertex is in a cycle, it is a predecessor of itself. <br>
         * 即该节点既是本身的父节点（或父节点的父节点），也是子节点（或子节点的子节点）
         *
         * @param <V>
         * @param traversableStructure The graph that contains the vertex.
         * @param node The vertex for which the property is checked.
         * @return <code>true</code> if the given vertex is contained in a
         * cycle;<br>
         * <code>false</code>.
         * @throws VertexNotFoundException If the graph does not contain the
         * vertex.
         * @throws ParameterException
         */
        public static <V extends Object> boolean isNodeInCycle(Traversable<V> traversableStructure, V node) throws VertexNotFoundException, ParameterException {
                Validate.notNull(traversableStructure);
                return isPredecessor(traversableStructure, node, node);
        }

        /**
         * Checks if a traversable structure contains a cycle.<br>
         * 网中存在节点：该节点既是本身的父节点（或父节点的父节点），也是子节点（或子节点的子节点）
         * @param <V>
         * @param traversableStructure The graph to check for cycles.
         * @return <code>true</code> if the given structure contains at least
         * one cycle, <code>false</code> otherwise.
         * @throws ParameterException
         */
        public static <V extends Object> boolean hasCycle(Traversable<V> traversableStructure) throws ParameterException {
                Validate.notNull(traversableStructure);
                for (V node : traversableStructure.getNodes()) {
                        try {
                                if (isNodeInCycle(traversableStructure, node)) {
                                        return true;
                                }
                        } catch (VertexNotFoundException e) {
                                throw new RuntimeException(e);
                        }
                }
                return false;
        }

        /**
         * Returns a list of paths that lead from sourceVertex to targetVertex. <br>
         * 获取sourceNode到targetNode的所有路径; 如果sourceNode与targetNode相同，认为没有路径
         *
         * @param <V>
         * @param traversableStructure
         * @param sourceNode The source vertex for the desired paths.
         * @param targetNode The target vertex for the desired paths.
         * @return A list of all paths leading from sourceVertex to
         * targetVertex.
         * @throws VertexNotFoundException If the graph does not contain the
         * given vertexes.
         * @throws ParameterException
         */
        public static <V extends Object> ArrayBlockingQueue<List<V>> getDirectedPathsFor(Traversable<V> traversableStructure, V sourceNode, V targetNode) throws VertexNotFoundException, ParameterException {
                Validate.notNull(traversableStructure);

                return getDirectedPathsFor(traversableStructure, Arrays.asList(sourceNode), targetNode);
        }


        /**
         * Returns a list of paths leading from one source vertex to targetVertex.<br>
         * 获取sourceNodes到targetNode的所有路径; 如果sourceNode与targetNode相同，认为没有路径
         *
         * @param <V> 顶点类型
         * @param traversableStructure The graph that contains the vertexes.
         * @param sourceNodes The source vertexes for the desired paths.
         * @param targetNode The target vertex for the desired paths.
         * @return A list of all paths leading from sourceVertex to
         * targetVertex.
         * @throws VertexNotFoundException If the graph does not contain the
         * given vertexes.
         * @throws ParameterException
         */
        public static <V extends Object> ArrayBlockingQueue<List<V>> getDirectedPathsFor(Traversable<V> traversableStructure, List<V> sourceNodes, V targetNode) throws VertexNotFoundException, ParameterException {
                Validate.notNull(traversableStructure);
                ArrayBlockingQueue<List<V>> finalPaths = new ArrayBlockingQueue<>(10);

                if (sourceNodes.contains(targetNode)) {
                        return finalPaths;
                }

                ArrayBlockingQueue<List<V>> tempPaths = new ArrayBlockingQueue<>(10);
                List<V> firstPath = new ArrayList<>();
                firstPath.add(targetNode);
                tempPaths.offer(firstPath);

                while (!tempPaths.isEmpty()) {
                        Collection<V> parents = traversableStructure.getParents(tempPaths.peek().get(tempPaths.peek().size() - 1));
                        for (V v : parents) {
                                List<V> newPath = new ArrayList<>(tempPaths.peek());
                                if (!tempPaths.peek().contains(v)) {
                                        newPath.add(v);
                                        if (sourceNodes.contains(v)) {
                                                finalPaths.add(newPath);
                                        } else {
                                                tempPaths.offer(newPath);
                                        }
                                }
                        }
                        tempPaths.poll();
                }
                for (List<V> l : finalPaths) {
                        Collections.reverse(l);
                }
                return finalPaths;
        }

        /**
         * 找出顶点v的所有邻接点，进而，递归找出v的邻接节点的邻接点，即与v连接的所有节点集合，该集合中不含重复顶点。（如果图是弱连通的，其数量=图的顶点数）
         * this function recursively traverses all neighbours of a node. 
         * Neighbours can be retrieved by merging children and parents of a vertex. 
         *
         * @param <V> 顶点类型
         * @param graph 实现接口的图
         * @param v 顶点v
         * @param nodes 所有节点的邻接节点集合，该集合中不含重复顶点（如果图是弱连通的，其数量=图的顶点数）
         * @throws ParameterException
         * @throws VertexNotFoundException
         */
        private static <V extends Object> void weakConnectivityRec(Traversable<V> graph, V v, Set<V> nodes) throws ParameterException, VertexNotFoundException {
                Set<V> neighbours = new HashSet<>();
                neighbours.addAll(graph.getChildren(v));  // 顶点v的子节点集合
                neighbours.addAll(graph.getParents(v));   // 顶点v的父节点集合
                for (V n : neighbours) {
                        if (false == nodes.contains(n)) {
                                nodes.add(n);
                                weakConnectivityRec(graph, n, nodes);
                        }
                }
        }
}
