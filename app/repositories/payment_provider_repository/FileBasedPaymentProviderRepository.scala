package repositories.payment_provider_repository

import akka.Done

import java.io.File
import java.nio.file.{Files, Path, Paths}
import scala.concurrent.ExecutionContext

class FileBasedPaymentProviderRepository(rootPath: Path)(implicit ec: ExecutionContext) extends PaymentProviderRepository {
  rootPath.toFile.mkdirs()

  override def find(id: String): PaymentProvider = {
    val file = new File(rootPath.toFile, id)
    val token = new String(Files.readAllBytes(Paths.get(file.getAbsolutePath)))
    PaymentProvider(id, token)
  }

  override def save(paymentProvider: PaymentProvider): Done = {
    val file = new File(rootPath.toFile, paymentProvider.id)
    Files.write(Paths.get(file.getAbsolutePath), paymentProvider.token.getBytes)
    Done
  }
}
