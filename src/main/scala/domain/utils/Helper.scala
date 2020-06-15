package domain.utils

import java.text.SimpleDateFormat
import java.util.Date

import com.google.inject.Inject
import config.AppConfig
import javax.inject.Singleton

@Singleton
class Helper @Inject() (config: AppConfig) {
  def parseStringToLong(input : String) : Long = {
    val dateFmt = config.dateFmt
    val dateFormat = new SimpleDateFormat(dateFmt)
    println(s"Parsed Date : $dateFormat")

    dateFormat.parse(input).getTime
  }

}

object Helper extends Helper(AppConfig)
