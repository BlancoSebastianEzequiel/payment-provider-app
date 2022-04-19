package payment_provider_repository

import org.scalatest.concurrent.ScalaFutures
import org.scalatestplus.play.PlaySpec
import repositories.payment_provider_repository.{PaymentProvider, PaymentProviderRepository}

import java.nio.file.NoSuchFileException
import java.util.UUID

trait PaymentProviderRepositoryTest extends PlaySpec with ScalaFutures {
  val repository: PaymentProviderRepository

  def createId(): String = UUID.randomUUID.toString

  "findLoyalty" should {
    "return nothing if the id does not exist" in {
      intercept[NoSuchFileException] { repository.find(createId()) }
    }
    "return the loyalty if it exists" in {
      val id = createId()
      val paymentProvider = PaymentProvider(id, "token")
      repository.save(paymentProvider)
      val result = repository.find(id)
      assert(result === paymentProvider)
    }
    "return the correct loyalty if multiples exist" in {
      val id1 = createId()
      val paymentProvider1 = PaymentProvider(id1, "token")

      val id2 = createId()
      val paymentProvider2 = PaymentProvider(id2, "token")

      repository.save(paymentProvider1)
      repository.save(paymentProvider2)
      val result = repository.find(id1)
      assert(result === paymentProvider1)
    }
  }

  "updateLoyalty" should {
    "overwrite an existing value" in {
      val id = createId()
      val paymentProvider1 = PaymentProvider(id, "token1")
      val paymentProvider2 = PaymentProvider(id, "token2")

      repository.save(paymentProvider1)
      repository.save(paymentProvider2)
      val result = repository.find(id)
      assert(result === paymentProvider2)
    }
  }
}
