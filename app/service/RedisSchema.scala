package service

import entities.{UserId}


/**
* Encapsulates generation of redis keys from domain objects
**/
object RedisSchema {

   def tts_input() = "tts:in"
   def tts_output(childHashCode: String) = "tts:out:${childHashCode}"
   def tts_output_links(parentHashCode: String) = "tts:out:links:${parentHashCode}"
   def tts_feed(uid: UserId) = "tts:feed:${uid.uid}"
}