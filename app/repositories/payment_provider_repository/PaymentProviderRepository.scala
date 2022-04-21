package repositories.payment_provider_repository

import akka.Done

case class PaymentProvider(id: String, storeId: Int, token: String)

trait PaymentProviderRepository {
  def find(storeId: Int): PaymentProvider
  def save(paymentProvider: PaymentProvider): Done
}
