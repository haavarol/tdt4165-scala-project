import akka.actor._
import exceptions._
import scala.collection.immutable.HashMap

case class TransactionRequest(toAccountNumber: String, amount: Double)

case class TransactionRequestReceipt(toAccountNumber: String,
                                     transactionId: String,
                                     transaction: Transaction)

case class BalanceRequest()

class Account(val accountId: String, val bankId: Bank, val initialBalance: Double = 0) extends Actor {

    private var transactions = HashMap[String, Transaction]()

    class Balance(var amount: Double) {}
    val uid = bankId.generateAccountId

    val balance = new Balance(initialBalance)

    def getFullAddress: String = {
        bankId + accountId
    }

    def getTransactions: List[Transaction] = {
        // Should return a list of all Transaction-objects stored in transactions
        ???
    }

    def allTransactionsCompleted: Boolean = {
        // Should return whether all Transaction-objects in transactions are completed
        ???
    }

    def withdraw(amount: Double): Unit = this.synchronized {
        if(amount < 0)
            throw new IllegalAmountException("Cannot withdraw negative amount")
        else if(amount > getBalanceAmount)
            throw new NoSufficientFundsException("Cannot withdraw amount larger than balance")

        balance.amount -= amount
    }    
    
    def deposit(amount: Double): Unit = this.synchronized {
        if(amount < 0)
            throw new IllegalAmountException("Cannot deposit negative amount")

        balance.amount += amount
    }

    def getBalanceAmount: Double = this.synchronized {
        balance.amount
    }

    def sendTransactionToBank(t: Transaction): Unit = {
        // Should send a message containing t to the bank of this account
        ???
    }

    def transferTo(accountNumber: String, amount: Double): Transaction = {

        val t = new Transaction(from = getFullAddress, to = accountNumber, amount = amount, allowedAttempts = 3)

        if (reserveTransaction(t)) {
            try {
                withdraw(amount)
                sendTransactionToBank(t)

            } catch {
                case _: NoSufficientFundsException | _: IllegalAmountException =>
                    t.status = TransactionStatus.FAILED
            }
        }

        t

    }

    def reserveTransaction(t: Transaction): Boolean = {
      if (!transactions.contains(t.id)) {
        transactions += (t.id -> t)
        return true
      }
      false
    }

    override def receive = {
		case IdentifyActor => sender ! this

		case TransactionRequestReceipt(to, transactionId, transaction) => {
			// Process receipt
			???
		}

		case BalanceRequest => ??? // Should return current balance

		case t: Transaction => {
			// Handle incoming transaction
			???
		}

		case msg => ???
    }


}
