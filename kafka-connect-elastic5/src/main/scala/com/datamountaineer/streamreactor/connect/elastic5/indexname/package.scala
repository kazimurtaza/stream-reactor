package com.datamountaineer.streamreactor.connect.elastic5

package object indexname {

  implicit class StringToOption(text: String) {
    def toOption: Option[String] = if (text.nonEmpty) Some(text) else None
  }
}
