package com.montecarlo.domain

import edu.uci.ics.jung.algorithms.layout.Layout
import edu.uci.ics.jung.visualization.layout.PersistentLayout
import edu.uci.ics.jung.visualization.layout.PersistentLayoutImpl
import java.io.*

class MyPersist<T, Y>(layout: Layout<T, Y>) : PersistentLayoutImpl<T, Y>(layout) {

    fun persistJ(fileName: String) {

        locations = locations.toMutableMap()
        graph.vertices.forEach { v ->
            locations[v] = PersistentLayout.Point(transform(v))
        }
        initializeLocations()

        val oos = ObjectOutputStream(FileOutputStream(fileName))
        oos.writeObject(locations)
        oos.close()
    }

    override fun restore(fileName: String) {
        val ois = ObjectInputStream(FileInputStream(fileName))
        //locations = locations.toMutableMap()
        val readObject = ois.readObject()
        print("")
        locations = readObject as Map<T, PersistentLayout.Point>
        ois.close()
        locked = true
        fireStateChanged()
        initializeLocations()
    }
}