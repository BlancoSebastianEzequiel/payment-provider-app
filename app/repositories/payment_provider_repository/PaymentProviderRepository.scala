package repositories.payment_provider_repository

import akka.Done

case class PaymentProvider(id: String, storeId: String, token: String)

trait PaymentProviderRepository {
  def find(storeId: String): PaymentProvider
  def save(paymentProvider: PaymentProvider): Done
}
