package repositories.payment_provider_repository

import akka.Done

import java.io.File
import java.nio.file.{Files, Path, Paths}
import scala.concurrent.ExecutionContext

class FileBasedPaymentProviderRepository(rootPath: Path)(implicit ec: ExecutionContext) extends PaymentProviderRepository {
  rootPath.toFile.mkdirs()

  override def find(storeId: String): PaymentProvider = {
    val file = new File(rootPath.toFile, storeId)
    val Array(id, _, token) = new String(Files.readAllBytes(Paths.get(file.getAbsolutePath))).split(",")
    PaymentProvider(id, storeId, token)
  }

  override def save(paymentProvider: PaymentProvider): Done = {
    val file = new File(rootPath.toFile, paymentProvider.storeId)
    val id = paymentProvider.id
    val storeId = paymentProvider.storeId
    val token = paymentProvider.token
    val bytes = s"$id,$storeId,$token".getBytes
    Files.write(Paths.get(file.getAbsolutePath), bytes)
    Done
  }
}
