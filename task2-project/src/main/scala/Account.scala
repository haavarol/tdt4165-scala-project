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

    private val actorSystem = ActorSystem("Account")

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
        var bool = true // Hvis kommenter inn det nedenfor sett denne til false
        /** var listOfTrans = transactions.values.toList
         breakable {
            for(key <- listOfTrans) {
                println(key.isCompleted)
                if (key.isCompleted == false){
                    bool = true
                    break
                } 
            }
         }**/
        // Koden over kjører evig
        bool
    }

    def withdraw(amount: Double): Unit = this.synchronized {
        if(amount < 0)
            throw new IllegalAmountException("Cannot withdraw negative amount")
        else if(amount > getBalanceAmount)
            throw new NoSufficientFundsException("Cannot withdraw amount larger than balance")

        balance.amount -= amount
    } // Like in part 1
    def deposit(amount: Double): Unit = this.synchronized {
        if(amount < 0)
            throw new IllegalAmountException("Cannot deposit negative amount")

        balance.amount += amount
    } // Like in part 1
    def getBalanceAmount: Double = this.synchronized {
        balance.amount
    } // Like in part 1

    def sendTransactionToBank(t: Transaction): Unit = {
        // Should send a message containing t to the bank of this account
        println("HEA")
        print(BankManager.findBank(this.bankId))
        BankManager.findBank(this.bankId) ! t
    }

    def transferTo(accountNumber: String, amount: Double): Transaction = {
        val t = new Transaction(from = getFullAddress, to = accountNumber, amount = amount)
                        println("ASFG")

        if (reserveTransaction(t)) {
            try {
                println("GEr")
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
        println(getTransactions)
        return true
      }
      false
    }

    def handleTransaction(t: Transaction): TransactionRequestReceipt = {
        val isInternal = t.to.length <= 4
        val toBankId = if (isInternal) bankId else t.to.substring(0, 4)
        val toAccountId = if (isInternal) t.to else t.to.substring(4)
        var transactionStatus = t.status
        var status = TransactionStatus.SUCCESS
        val id = t.id
        var receiptReceived = true
        var from = t.from.substring(4)
        println(from)
        deposit(t.amount)
        t.status = TransactionStatus.SUCCESS
        val receipt = new TransactionRequestReceipt(from, id, t)
        println(receipt)
        return receipt
    }

    override def receive = {
		case IdentifyActor => sender ! this 

		case TransactionRequestReceipt(to, transactionId, t) => {
			// Process receipt
             println(this.accountId)

            transactions += (t.id) -> t

            println(t.status) 
			//transactions(transactionId).status = transaction.status
            //transactions(transactionId).receiptReceived = true
            if (t.status == TransactionStatus.FAILED) {
                this.balance.amount += t.amount
            }
		}

		case BalanceRequest => sender ! getBalanceAmount // Should return current balance

		case t: Transaction => {
			// Handle incoming transaction
            println("GALGAKSJLJGFSLGJØAFSKJGØAFSKJG")
			sender ! handleTransaction(t)
		}
		case msg => ???
    }


}
