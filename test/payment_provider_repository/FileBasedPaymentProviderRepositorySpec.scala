package payment_provider_repository

import org.scalatest.BeforeAndAfterAll
import repositories.payment_provider_repository.{FileBasedPaymentProviderRepository, PaymentProviderRepository}

import java.nio.file.{Files, Path}

import scala.concurrent.ExecutionContext.Implicits.global

class FileBasedPaymentProviderRepositorySpec extends PaymentProviderRepositoryTest with BeforeAndAfterAll {
  val tmpDir: Path = Files.createTempDirectory("filebasedrepotest")
  tmpDir.toFile.deleteOnExit()
  override val repository: PaymentProviderRepository = new FileBasedPaymentProviderRepository(tmpDir)

  override protected def afterAll(): Unit = {
    super.afterAll()
    tmpDir.toFile.listFiles().foreach(_.deleteOnExit())
    tmpDir.toFile.deleteOnExit()
  }
}
