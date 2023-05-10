package note.notes.savenote.Utils

import android.os.Build
import androidx.annotation.RequiresApi
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.Period
import java.time.format.DateTimeFormatter

class DateUtils{
    @RequiresApi(Build.VERSION_CODES.O)
    val formatter: DateTimeFormatter = DateTimeFormatter.ofPattern("dd MMMM yy, EEEE, HH:mm")

    @RequiresApi(Build.VERSION_CODES.O)
    val current: String? = LocalDateTime.now().format(formatter)

    @RequiresApi(Build.VERSION_CODES.O)
    fun dateTimeDisplay(date: String):String {

        val firstDate: LocalDate = LocalDate.parse(current, formatter)
        val secondDate: LocalDate = LocalDate.parse(date, formatter)
        val period = Period.between(secondDate, firstDate)
        val days = period.days
        val time = date.split(", ")

        return when (days) {
            0 -> "Today ${time.last()}"
            1 -> "Yesterday ${time.last()}"
            in 2..6 -> "${time[1]} ${time.last()}"
            else -> time.first()
        }
    }
}