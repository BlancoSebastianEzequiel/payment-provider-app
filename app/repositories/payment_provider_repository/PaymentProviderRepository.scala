package repositories.payment_provider_repository

import akka.Done

case class PaymentProvider(id: String, token: String)

trait PaymentProviderRepository {
  def find(id: String): PaymentProvider
  def save(paymentProvider: PaymentProvider): Done
}
