package com.namnv.artry.utils

import kotlin.math.abs

class Node(private var row: Int, private var col: Int) {
    private var G: Int = 0
        fun getG(): Int {
            return this.G
        }
        fun setG(value: Int) {
            this.G = value
        }
    private var H: Int = 0
        fun getH(): Int {
            return this.H
        }
        fun setH(value: Int) {
            this.H = value
        }
    private var F: Int = 0
        fun getF(): Int {
            return this.F
        }
        fun setF(value: Int) {
            this.F = value
        }

    fun getRow(): Int {
            return this.row
        }
        fun setRow(value: Int) {
            this.row = value
        }

    fun getCol(): Int {
            return this.col
        }
        fun setCol(value: Int) {
            this.col = value
        }
    private lateinit var parent: Node
        fun getParent(): Node {
            return this.parent
        }
        fun setParent(value: Node) {
            this.parent = value
        }
    private var block: Boolean = false
        fun getBlock(): Boolean {
            return this.block
        }
        fun setBlock(value: Boolean) {
            this.block = value
        }

    @Override
    override fun equals(any: Any?): Boolean {
        val other: Node = any as Node
        return this.row == other.row && this.col == other.col
    }

    fun calculateH(endNode: Node): Int {
        val h = abs(endNode.row - this.row)*abs(endNode.row - this.row) +
                abs(endNode.col - this.col)*abs(endNode.col - this.col)/2
        return h
    }

    fun calculateG(endNode: Node): Int {
        val g = abs(endNode.row - this.row)*abs(endNode.row - this.row) +
                abs(endNode.col - this.col)*abs(endNode.col - this.col)
        return g
    }

    override fun toString(): String {
        return "Node(G=$G, H=$H, F=$F, row=$row, column=$col)"
    }

    companion object {
        fun compare(node1: Node, node2: Node): Int {
            if (node1.getF() == node2.getF()) {
                return node1.getH().compareTo(node2.getH())
            }
            return node1.getF().compareTo(node2.getF())
        }
    }



}