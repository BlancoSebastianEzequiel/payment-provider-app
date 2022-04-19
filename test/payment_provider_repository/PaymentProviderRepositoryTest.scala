package payment_provider_repository

import org.scalatest.concurrent.ScalaFutures
import org.scalatestplus.play.PlaySpec
import repositories.payment_provider_repository.{PaymentProvider, PaymentProviderRepository}

import java.nio.file.NoSuchFileException
import java.util.UUID

trait PaymentProviderRepositoryTest extends PlaySpec with ScalaFutures {
  val repository: PaymentProviderRepository

  def createId(): String = UUID.randomUUID.toString

  "find" should {
    "return nothing if the id does not exist" in {
      intercept[NoSuchFileException] { repository.find(createId()) }
    }

    "return the provider if it exists" in {
      val storeId = createId()
      val paymentProvider = PaymentProvider("id", storeId, "token")
      repository.save(paymentProvider)
      assert(repository.find(storeId) === paymentProvider)
    }

    "return the correct provider if multiples exist" in {
      val storeId1 = createId()
      val paymentProvider1 = PaymentProvider("id1", storeId1, "token")

      val storeId2 = createId()
      val paymentProvider2 = PaymentProvider("id2", storeId2, "token")

      repository.save(paymentProvider1)
      repository.save(paymentProvider2)
      assert(repository.find(storeId1) === paymentProvider1)
    }
  }

  "save" should {
    "overwrite an existing value" in {
      val storeId = createId()
      val paymentProvider1 = PaymentProvider("id", storeId, "token1")
      val paymentProvider2 = PaymentProvider("id", storeId, "token2")

      repository.save(paymentProvider1)
      repository.save(paymentProvider2)
      assert(repository.find(storeId) === paymentProvider2)
    }
  }
}
