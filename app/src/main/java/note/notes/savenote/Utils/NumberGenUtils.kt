package note.notes.savenote.Utils

import note.notes.savenote.Database.CheckList

class URNG {
    fun numGen(temporaryChecklist: List<CheckList>): Int? {
        val sortedKeys = temporaryChecklist.map { it.key }.sorted()
        var left = 1
        var right = 100000
        var safeNumber: Int? = null

        while (left <= right) {
            val mid = left + (right - left) / 2
            if (sortedKeys.contains(mid)) {
                left = mid + 1
            } else {
                safeNumber = mid
                right = mid - 1
            }
        }

        return safeNumber
    }
}