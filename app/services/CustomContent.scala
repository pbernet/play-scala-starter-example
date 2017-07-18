package services

import java.util.concurrent.atomic.AtomicInteger
import javax.inject._

@Singleton
class CustomContent  {

  def content(): Int = 1
}
