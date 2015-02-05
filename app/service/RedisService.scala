package service


import java.lang.Long.parseLong

import scala.concurrent.Future
import scalaz.syntax.applicative.ToApplyOps

import play.api.libs.concurrent.Execution.Implicits._
import scalaz._
import Scalaz._

import org.mindrot.jbcrypt.BCrypt

import utils.{Logging, Utils}
import Utils._
import entities._

/**
 * Global object, handles actual interaction with Redis.
 * All methods are non-blocking and thread safe.
 */
object RedisService extends RedisConfig with Logging {

  /**
   * Add new text for speech synthesis
   */
  private def submit_tts(id: String, channel: String, orderId: Int, hashCode: String, text: String, synth: String, voice: String): Future[Unit] = {

    redis.hmSetFromMap(RedisSchema.tts_input(), Map(
      "id" -> id,
      "channel" -> channel,
      "orderId" -> orderId,
      "hashCode" -> hashCode,
      "text" -> text,
      "synth" -> synth,
      "voice" -> voice
    ))
  }

  /**
   * Load tts record
   * @param id record id to load
   * @return (Future of) Some message if the given post_id maps to an actual message else None
   */
  def loadRecord(id: String): Future[Option[Msg]] = {
    for {
      map <- redis.hmGetAsMap[String](RedisSchema.getRecord(id))("timestamp", "author", "body")
    } yield {
      for{
        timestamp <- map.get("timestamp")
        author <- map.get("author")
        body <- map.get("body")
      } yield Msg(post_id, parseLong(timestamp), UserId(author), body)
    }
  }


}