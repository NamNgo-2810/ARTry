package com.namnv.artry.utils

import android.os.Build
import androidx.annotation.RequiresApi
import java.util.*

class PathFinding
@RequiresApi(Build.VERSION_CODES.N) constructor(
    map: Array<IntArray>,
    start: IntArray,
    end: IntArray
) {
    private var row = 0
    private var col = 0
    private var startNode: Node
    private var endNode: Node
    private var openList: PriorityQueue<Node>
    private var visitedSet: HashSet<Node?>
    private var NodeMap: Array<Array<Node>>

    init {
        row = map.size
        col = map[0].size
        openList = PriorityQueue { p0, p1 ->
            if (p0.getF() == p1.getF()) {
                p0.getH().compareTo(p1.getH())
            } else p0.getF().compareTo(p1.getF())
        }
        visitedSet = HashSet()
        NodeMap = Array(row) {i -> Array(col) {j -> Node(i, j)} }
        for (i in 0 until row) {
            for (j in 0 until col) {
                NodeMap[i][j] = Node(i, j)
                if (map[i][j] == 0) NodeMap[i][j].setBlock(false)
                else NodeMap[i][j].setBlock(true)
            }
        }
        startNode = NodeMap[start[0]][start[1]]
        endNode = NodeMap[end[0]][end[1]]
    }

    fun search(): Boolean {
        if (endNode.getBlock()) {
            println("Your can't set endpoint at block")
            return false
        }
        if (startNode.getBlock()) {
            println("Your can't set startPoint at block")
            return false
        }
        openList.add(startNode)
        visitedSet.add(startNode)
        while (!openList.isEmpty()) {
            val current = openList.poll()
            visitedSet.add(current)
            val neighbors = current?.let { getAdj(it) }
            if (neighbors != null) {
                for (neighbor in neighbors) {
                    if (!visitedSet.contains(neighbor)) {
                        val g = neighbor.calculateG(startNode)
                        val h = neighbor.calculateH(endNode)
                        val tempf = g + h
                        if (openList.contains(neighbor)) {
                            val f = neighbor.getF()
                            if (f > tempf) {
                                openList.remove(neighbor)
                                neighbor.setF(tempf)
                                neighbor.setH(h)
                                neighbor.setG(g)
                                neighbor.setParent(current)
                                openList.add(neighbor)
                            }
                        } else {
                            neighbor.setF(tempf)
                            neighbor.setH(h)
                            neighbor.setG(g)
                            neighbor.setParent(current)
                            openList.add(neighbor)
                        }
                    }
                    if (neighbor == endNode) return true
                }
            }
        }
        return false
    }

    fun findPath(): Deque<Node> {
        var current = endNode
        val path: Deque<Node> = ArrayDeque()
        path.add(endNode)
        println(current.toString())
        while (current != startNode) {
            current = current.getParent()
            path.add(current)
        }
        return path
    }

    private fun getAdj(current: Node): List<Node> {
        val L: MutableList<Node> = ArrayList()
        val Noderow = current.getRow()
        val Nodecol: Int = current.getCol()
        if (Noderow > 0) {
            val node = NodeMap[Noderow - 1][Nodecol]
            if (!node.getBlock()) L.add(node)
        }
        if (Nodecol > 0) {
            val node = NodeMap[Noderow][Nodecol - 1]
            if (!node.getBlock()) L.add(node)
        }
        if (Noderow < row - 1) {
            val node = NodeMap[Noderow + 1][Nodecol]
            if (!node.getBlock()) L.add(node)
        }
        if (Nodecol < col - 1) {
            val node = NodeMap[Noderow][Nodecol + 1]
            if (!node.getBlock()) L.add(node)
        }
        return L
    }

}