/*
 * Copyright 2021 HM Revenue & Customs
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package support.fixtures

import play.api.libs.json.{JsValue, Json}
import models._
import org.joda.time.LocalDate
import java.time.ZonedDateTime

trait NrsSearchFixture extends NrSubmissionId {

  val headerData: Option[JsValue] = Some(Json.parse("""{"SomeAttribute":"SomeValue"}"""))

  val bundle = Bundle("zip", 123456)

  val glacier = Glacier("12345", "1234567890")

  val nrsVatSearchResult = NrsSearchResult("businessId", "vat-return", "payloadContentType",
    ZonedDateTime.parse("1970-01-18T11:56:13.625Z"), Json.parse("{}"), "userAuthToken", headerData,
    nrSubmissionId, bundle, LocalDate.parse("1970-01-18"), glacier)

  val nrsVatRegSearchResult = NrsSearchResult("businessId", "vat-registration", "payloadContentType",
    ZonedDateTime.parse("1970-01-18T11:56:13.625Z"), Json.parse("{}"), "userAuthToken", headerData,
    nrSubmissionId, bundle, LocalDate.parse("1970-01-18"), glacier)

  val searchQuery = SearchQuery(Some("aName"), Some("aValue"), "aNotableEvent")
}
