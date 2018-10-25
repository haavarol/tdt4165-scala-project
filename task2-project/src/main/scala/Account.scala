import akka.actor._
import exceptions._
import scala.collection.immutable.HashMap
import scala.util.control.Breaks._

case class TransactionRequest(toAccountNumber: String, amount: Double)

case class TransactionRequestReceipt(toAccountNumber: String,
                                     transactionId: String,
                                     transaction: Transaction)

case class BalanceRequest()

class Account(val accountId: String, val bankId: String, val initialBalance: Double = 0) extends Actor {

    private var transactions = HashMap[String, Transaction]()

    class Balance(var amount: Double) {}

    val balance = new Balance(initialBalance)

    def getFullAddress: String = {
        bankId + accountId
    }

    def getTransactions: List[Transaction] = {
        // Should return a list of all Transaction-objects stored in transactions
        transactions.values.toList
    }

    def allTransactionsCompleted: Boolean = {
        // Should return whether all Transaction-objects in transactions are completed
        var bool = true // Hvis kommenter inn det nedenfor sett denne til true
        var listOfTrans = transactions.values.toList
        breakable {
            for(key <- listOfTrans) {
                if (key.isCompleted == false){
                    bool = false
                    break
                } 
            }
        }
        return bool
    }

    // Like in part 1
    def withdraw(amount: Double): Unit = this.synchronized {
        if(amount < 0)
            throw new IllegalAmountException("Cannot withdraw negative amount")
        else if(amount > getBalanceAmount)
            throw new NoSufficientFundsException("Cannot withdraw amount larger than balance")

        balance.amount -= amount
    }

    // Like in part 1
    def deposit(amount: Double): Unit = this.synchronized {
        if(amount < 0)
            throw new IllegalAmountException("Cannot deposit negative amount")

        balance.amount += amount
    }

    // Like in part 1
    def getBalanceAmount: Double = this.synchronized {
        balance.amount
    }

    def sendTransactionToBank(t: Transaction): Unit = {
        // Should send a message containing t to the bank of this account
        BankManager.findBank(this.bankId) ! t
    }

    def transferTo(accountNumber: String, amount: Double): Transaction = {
        val t = new Transaction(from = getFullAddress, to = accountNumber, amount = amount)
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

    def handleTransaction(t: Transaction): TransactionRequestReceipt = {
        val id = t.id
        val to = t.from.substring(4)
        deposit(t.amount)
        t.status = TransactionStatus.SUCCESS
        val receipt = new TransactionRequestReceipt(to, id, t)
        return receipt
    }

    override def receive = {
		case IdentifyActor => sender ! this 

		case TransactionRequestReceipt(to, transactionId, t) => {
			// Process receipt
            transactions += (t.id) -> t
            if (t.status == TransactionStatus.FAILED) {
                this.balance.amount += t.amount
            }
		}

		case BalanceRequest => sender ! getBalanceAmount // Should return current balance

		case t: Transaction => {
			// Handle incoming transaction
			sender ! handleTransaction(t)
		}
		case msg => print("Default")
    }


}
